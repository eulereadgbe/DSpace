#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys
import requests
import json
import xml.etree.ElementTree as ElementTree
from javax.servlet.http import HttpServlet

from net.sf.ehcache import Cache;
from net.sf.ehcache import CacheManager;
from net.sf.ehcache import Element;


class ImpactServlet(HttpServlet):
    # This doesn't work:
    #  def init(self):
    # so we use the other constructor:
    def init(self, config):
        # Create cache used by CachedImpactService
        impactCache = Cache("impactCache",
                            2000,       # max elements
                            False,      # don't store to disk
                            False,      # don't cache forever
                            3600*24*7,  # cache for 1 week (from WoS doc)
                            0)          # don't expire elements not accessed
        CacheManager.getInstance().addCache(impactCache)
        config.getServletContext().setAttribute("impactCache", impactCache)

        # This doesn't work:
        #  HttpServlet.init(self, config)
        # so we have to store the ServletConfig by ourselves:
        self._config = config

    def doGet(self, request, response):
        self.doPost(request, response)

    def doPost(self, request, response):
        try:
            impactService = self._getImpactService(request)
        except ValueError as e:
            response.sendError(response.SC_NOT_IMPLEMENTED, e.message)
            return

        response.setContentType("application/json")
        response.setCharacterEncoding("UTF-8")
        response.getWriter().println(impactService.query())

    def _getImpactService(self, request):
        """
        Returns the ``ImpactService`` instance corresponding to the one
        specified in the request.

        Services are specified in the ``service`` parameter. Possible values:
         - ``scopus``
         - ``wos`` (Web of Science)

        :param request: ``HttpRequest`` received by the servlet.
        :raises InvalidArgumentException: if the service is not one of the
            supported.
        """
        serviceToQuery = request.getParameter("service")

        if serviceToQuery == "scopus":
            impactService = Scopus(request)
        elif serviceToQuery == "wos":
            impactService = WebOfScience(request)
        else:
            raise ValueError("Invalid service '{0}'.".format(serviceToQuery))

        cache = self._config.getServletContext().getAttribute("impactCache")
        return CachedImpactService(impactService, cache, request)


class ImpactService(object):
    """
    Interface definition for impact services.
    """
    def __init__(self, request):
        """
        Creates an ``ImpactService`` that will process the impact request in
        ``request``.

        :param request: ``HttpRequest`` received by the servlet.
        """
        raise NotImplementedError()

    def query(self):
        """
        Returns the impact information returned by the impact service.
        """
        serviceResponse = self._getServiceResponse()
        return self._buildResponseToClient(serviceResponse)

    def _getServiceResponse(self):
        """
        Returns a string with the response from the impact service.
        """
        raise NotImplementedError()

    def _buildResponseToClient(self, serviceResponse):
        """
        Extracts the citation count and link back from the impact service
        response and returns it as a JSON document.

        Example:
          {
          "citationCount": "12",
          "linkBack": "http://gateway.webofknowledge.com/foo"
          }

        :param serviceResponse: string with the response returned by the impact
            service.
        :returns: JSON document as a string with impact information.
        """
        raise NotImplementedError()

    def _getJsonDocument(self, citationCount, linkBack):
        """
        Returns a JSON document with the citation count and the link back specified.
        """
        jsonObject = {}
        jsonObject["citationCount"] = int(citationCount)
        jsonObject["linkBack"] = linkBack
        return json.dumps(jsonObject)


class CachedImpactService(ImpactService):
    """
    Decorator to cache responses from ``ImpactService``.

    The cache can be skipped by adding "cache=skip" parameter to the request.
    """
    def __init__(self, impactService, cache, request):
        """
        Constructs a ``CachedImpactService`` that caches the response from
        ``impactService`` in ``cache``.

        :param impactService: ``ImpactService`` from where the response will be
            cached.
        :param cache: EhCache ``Cache`` object where the response will be cached.
        :param request: ``HttpRequest`` received by the servlet.
        """
        self._impactService = impactService
        self._cache = cache
        self._request = request

    def query(self):
        """
        Returns impact information returned by the impact service.
        """
        cachedImpact = self._cache.get(self._generateCacheKey())

        if not cachedImpact or self._shouldSkipCache():
            responseData = self._impactService.query()
            cachedImpact = Element(self._generateCacheKey(), responseData)
            self._cache.put(cachedImpact)
        else:
            responseData = cachedImpact.getObjectValue()

        if self._showCacheStatistics():
            responseData = self._getStatistics()

        return responseData

    def _generateCacheKey(self):
        """
        Returns a cache key build from the ``HttpRequest``.
        """
        serviceToQuery = self._request.getParameter("service")
        doi = self._request.getParameter("doi")
        return "{0}:{1}".format(serviceToQuery, doi)

    def _shouldSkipCache(self):
        """
        Returns true if the user has requested to skip the cache.

        :returns: Returns true if and only if the ``HttpRequest`` contains
            the parameter "cache=skip".
        """
        cacheParam = self._request.getParameter("cache")
        return cacheParam == "skip"

    def _showCacheStatistics(self):
        """
        Returns true if the user has requested to show cache statistics.

        :returns: Returns true if and only if the ``HttpRequest`` contains
            the parameter "cache=stats".
        """
        cacheParam = self._request.getParameter("cache")
        return cacheParam == "stats"

    def _getStatistics(self):
        size = self._cache.getStatistics().getSize()
        sizeBytes = self._cache.getStatistics().getLocalHeapSizeInBytes()
        hitCount = self._cache.getStatistics().cacheHitCount()
        hitRatio = self._cache.getStatistics().cacheHitRatio()
        missCount = self._cache.getStatistics().cacheMissCount()
        expiredCount = self._cache.getStatistics().cacheExpiredCount()
        return """Statistics:
               Size: {0} ({1} bytes)
               Hit count: {2} ({3} ratio)
               Miss count: {4}
               Expired count: {5}
               """.format(size, sizeBytes, hitCount, hitRatio, missCount, expiredCount)


class Scopus(ImpactService):
    QUERY_URL = "http://api.elsevier.com/content/abstract/citation-count?httpAccept=application/json&apiKey={0}&doi={1}"
    API_KEY = "83df0327ddc62a996eab2976d9361e54"

    def __init__(self, request):
        self.doi = request.getParameter("doi")

    def _getServiceResponse(self):
        return requests.get(self.QUERY_URL.format(self.API_KEY, self.doi))

    def _buildResponseToClient(self, serviceResponse):
        serviceResponseObject = json.loads(serviceResponse.text)
        citationCount = serviceResponseObject["citation-count-response"]\
                                             ["document"]["citation-count"]
        linkBack = self._getLinkBack(serviceResponseObject)
        return self._getJsonDocument(citationCount, linkBack)

    def _getLinkBack(self, scopusData):
        linkList = scopusData["citation-count-response"]["document"]["link"]
        linkBackObject = next(link for link in linkList if link["@rel"] == "scopus-citedby")
        return linkBackObject["@href"]


class WebOfScience(ImpactService):
    QUERY_URL = "http://gateway.webofknowledge.com/gateway/Gateway.cgi"
    QUERY_DOC = """<?xml version="1.0" encoding="UTF-8"?>
                   <request xmlns="http://www.isinet.com/xrpc42">
                       <fn name="LinksAMR.retrieve">
                           <list>
                               <map>
                                   <val name="username">your-username</val>
                                   <val name="password">your-password</val>
                               </map>
                               <map>
                                   <list name="WOS">
                                       <val>timesCited</val>
                                       <val>citingArticlesURL</val>
                                   </list>
                               </map>
                               <map>
                                   <map name="cite_1">
                                       <val name="doi">{0}</val>
                                   </map>
                               </map>
                           </list>
                       </fn>
                   </request>
                """
    CITATION_COUNT_XPATH = ".//wos:val[@name='timesCited']"
    LINKBACK_XPATH = ".//wos:val[@name='citingArticlesURL']"

    def __init__(self, request):
        self.doi = request.getParameter("doi")

    def _getServiceResponse(self):
        return requests.post(self.QUERY_URL, data=self.QUERY_DOC.format(self.doi))

    def _buildResponseToClient(self, serviceResponse):
        root = ElementTree.fromstring(serviceResponse.text)
        namespaces = {"wos": "http://www.isinet.com/xrpc42"}
        citationCount = root.findtext(self.CITATION_COUNT_XPATH, namespaces=namespaces)
        linkBack = root.findtext(self.LINKBACK_XPATH, namespaces=namespaces)
        return self._getJsonDocument(citationCount, linkBack)
