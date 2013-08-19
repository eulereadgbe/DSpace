<?xml version="1.0" encoding="UTF-8"?>
<!--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

-->
<!--
    TODO: Describe this XSL file
    Author: Alexey Maslov

-->

<xsl:stylesheet xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
                xmlns:dri="http://di.tamu.edu/DRI/1.0/"
                xmlns:mets="http://www.loc.gov/METS/"
                xmlns:xlink="http://www.w3.org/TR/xlink/"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:dim="http://www.dspace.org/xmlns/dspace/dim"
                xmlns:xhtml="http://www.w3.org/1999/xhtml"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:confman="org.dspace.core.ConfigurationManager"
                xmlns:util="org.dspace.app.xmlui.utils.XSLUtils"
                xmlns="http://www.w3.org/1999/xhtml"
                xmlns:str="http://exslt.org/strings"
                exclude-result-prefixes="i18n dri mets xlink xsl dim xhtml mods dc util confman">

    <xsl:import href="../Mirage/Mirage.xsl"/>
    <xsl:import href="itemSummaryView.xsl"/>
    <xsl:import href="itemDetailView.xsl"/>
    <xsl:import href="discovery.xsl"/>
    <xsl:output indent="yes"/>

    <xsl:template match="dri:referenceSet[@type = 'summaryList' and @n='community-browser']" priority="3">
        <div id="sidetree">
            <div class="treeheader">&#160;</div>
            <ul id="tree">
                <xsl:apply-templates select="*[not(name()='head')]" mode="summaryList"/>
            </ul>
        </div>
    </xsl:template>

    <xsl:template name="buildHeader">
        <div id="ds-header-wrapper">
            <div id="ds-header" class="clearfix">
                <a id="ds-header-logo-link">
                    <xsl:attribute name="href">
                        <xsl:value-of
                                select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
                        <xsl:text>/</xsl:text>
                    </xsl:attribute>
                    <span id="ds-header-logo">&#160;</span>
                    <span id="ds-header-logo-text">SEAFDEC/AQD Institutional Repository</span>
                </a>
                <h1 class="pagetitle visuallyhidden">
                    <xsl:choose>
                        <!-- protection against an empty page title -->
                        <xsl:when test="not(/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='title'])">
                            <xsl:text> </xsl:text>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:copy-of
                                    select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='title']/node()"/>
                        </xsl:otherwise>
                    </xsl:choose>

                </h1>
                <h2 class="static-pagetitle visuallyhidden">
                    <i18n:text>xmlui.dri2xhtml.structural.head-subtitle</i18n:text>
                </h2>
            </div>
        </div>
    </xsl:template>

    <xsl:template name="buildHead">
        <head>
            <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>

            <!-- Always force latest IE rendering engine (even in intranet) & Chrome Frame -->
            <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>

            <!--  Mobile Viewport Fix
                  j.mp/mobileviewport & davidbcalhoun.com/2010/viewport-metatag
            device-width : Occupy full width of the screen in its current orientation
            initial-scale = 1.0 retains dimensions instead of zooming out if page height > device height
            maximum-scale = 1.0 retains dimensions instead of zooming in if page width < device width
            -->
            <meta name="viewport" content="width=device-width; initial-scale=1.0; maximum-scale=1.0;"/>

            <link rel="shortcut icon">
                <xsl:attribute name="href">
                    <xsl:value-of
                            select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
                    <xsl:text>/themes/</xsl:text>
                    <xsl:value-of
                            select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='theme'][@qualifier='path']"/>
                    <xsl:text>/images/favicon.ico</xsl:text>
                </xsl:attribute>
            </link>
            <link rel="apple-touch-icon">
                <xsl:attribute name="href">
                    <xsl:value-of
                            select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
                    <xsl:text>/themes/</xsl:text>
                    <xsl:value-of
                            select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='theme'][@qualifier='path']"/>
                    <xsl:text>/images/apple-touch-icon.png</xsl:text>
                </xsl:attribute>
            </link>

            <meta name="Generator">
                <xsl:attribute name="content">
                    <xsl:text>DSpace</xsl:text>
                    <xsl:if test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='dspace'][@qualifier='version']">
                        <xsl:text> </xsl:text>
                        <xsl:value-of
                                select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='dspace'][@qualifier='version']"/>
                    </xsl:if>
                </xsl:attribute>
            </meta>
            <!-- Add stylsheets -->
            <xsl:for-each select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='stylesheet']">
                <link rel="stylesheet" type="text/css">
                    <xsl:attribute name="media">
                        <xsl:value-of select="@qualifier"/>
                    </xsl:attribute>
                    <xsl:attribute name="href">
                        <xsl:value-of
                                select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
                        <xsl:text>/themes/</xsl:text>
                        <xsl:value-of
                                select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='theme'][@qualifier='path']"/>
                        <xsl:text>/</xsl:text>
                        <xsl:value-of select="."/>
                    </xsl:attribute>
                </link>
            </xsl:for-each>

            <!-- Add syndication feeds -->
            <xsl:for-each select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='feed']">
                <link rel="alternate" type="application">
                    <xsl:attribute name="type">
                        <xsl:text>application/</xsl:text>
                        <xsl:value-of select="@qualifier"/>
                    </xsl:attribute>
                    <xsl:attribute name="href">
                        <xsl:value-of select="."/>
                    </xsl:attribute>
                </link>
            </xsl:for-each>

            <!--  Add OpenSearch auto-discovery link -->
            <xsl:if test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='opensearch'][@qualifier='shortName']">
                <link rel="search" type="application/opensearchdescription+xml">
                    <xsl:attribute name="href">
                        <xsl:value-of
                                select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request'][@qualifier='scheme']"/>
                        <xsl:text>://</xsl:text>
                        <xsl:value-of
                                select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request'][@qualifier='serverName']"/>
                        <xsl:text>:</xsl:text>
                        <xsl:value-of
                                select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request'][@qualifier='serverPort']"/>
                        <xsl:value-of select="$context-path"/>
                        <xsl:text>/</xsl:text>
                        <xsl:value-of
                                select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='opensearch'][@qualifier='autolink']"/>
                    </xsl:attribute>
                    <xsl:attribute name="title">
                        <xsl:value-of
                                select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='opensearch'][@qualifier='shortName']"/>
                    </xsl:attribute>
                </link>
            </xsl:if>

            <!-- The following javascript removes the default text of empty text areas when they are focused on or submitted -->
            <!-- There is also javascript to disable submitting a form when the 'enter' key is pressed. -->
            <script type="text/javascript">
                //Clear default text of empty text areas on focus
                function tFocus(element)
                {
                if (element.value == '<i18n:text>xmlui.dri2xhtml.default.textarea.value</i18n:text>'){element.value='';}
                }
                //Clear default text of empty text areas on submit
                function tSubmit(form)
                {
                var defaultedElements = document.getElementsByTagName("textarea");
                for (var i=0; i != defaultedElements.length; i++){
                if (defaultedElements[i].value == '<i18n:text>xmlui.dri2xhtml.default.textarea.value</i18n:text>'){
                defaultedElements[i].value='';}}
                }
                //Disable pressing 'enter' key to submit a form (otherwise pressing 'enter' causes a submission to start over)
                function disableEnterKey(e)
                {
                var key;

                if(window.event)
                key = window.event.keyCode; //Internet Explorer
                else
                key = e.which; //Firefox and Netscape

                if(key == 13) //if "Enter" pressed, then disable!
                return false;
                else
                return true;
                }

                function FnArray()
                {
                this.funcs = new Array;
                }

                FnArray.prototype.add = function(f)
                {
                if( typeof f!= "function" )
                {
                f = new Function(f);
                }
                this.funcs[this.funcs.length] = f;
                };

                FnArray.prototype.execute = function()
                {
                for( var i=0; i
                <xsl:text disable-output-escaping="yes">&lt;</xsl:text> this.funcs.length; i++ )
                {
                this.funcs[i]();
                }
                };

                var runAfterJSImports = new FnArray();
            </script>

            <!-- Modernizr enables HTML5 elements & feature detects -->
            <script type="text/javascript">
                <xsl:attribute name="src">
                    <xsl:value-of
                            select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
                    <xsl:text>/themes/</xsl:text>
                    <xsl:value-of
                            select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='theme'][@qualifier='path']"/>
                    <xsl:text>/lib/js/modernizr-1.7.min.js</xsl:text>
                </xsl:attribute>
                &#160;
            </script>

            <!-- Add the title in -->
            <xsl:variable name="page_title"
                          select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='title']"/>
            <title>
                <xsl:choose>
                    <xsl:when test="starts-with($request-uri, 'page/about')">
                        <xsl:text>About This Repository</xsl:text>
                    </xsl:when>
                    <xsl:when test="not($page_title)">
                        <xsl:text>  </xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:copy-of select="$page_title/node()"/>
                    </xsl:otherwise>
                </xsl:choose>
            </title>

            <!-- Head metadata in item pages -->
            <xsl:if test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='xhtml_head_item']">
                <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='xhtml_head_item']"
                              disable-output-escaping="yes"/>
            </xsl:if>

            <!-- Add all Google Scholar Metadata values -->
            <xsl:for-each
                    select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[substring(@element, 1, 9) = 'citation_']">
                <meta name="{@element}" content="{.}"></meta>
            </xsl:for-each>

        </head>
    </xsl:template>

    <!-- Like the header, the footer contains various miscellaneous text, links, and image placeholders -->
    <xsl:template name="buildFooter">
        <div id="ds-footer-wrapper">
            <div id="ds-footer">
                <div id="ds-footer-signature">
                    <xsl:text>Library &amp; Data Banking Services Section | Training &amp; Information Division</xsl:text>
                    <br/>
                    <xsl:text>Aquaculture Department | Southeast Asian Fisheries Development Center (SEAFDEC)</xsl:text>
                    <br/>
                    <xsl:text>Tigbauan, Iloilo 5021 Philippines | Tel. 63 33 5119170, 5119171 | Fax. 63 33 5119174, 5118709</xsl:text>
                    <br/>
                    <xsl:text>Website: </xsl:text>
                    <a href="http://www.seafdec.org.ph" target="_blank">http://www.seafdec.org.ph</a>
                    | Email:
                    <a href="mailto:library@seafdec.org.ph" target="_blank">library@seafdec.org.ph</a>
                    <br/>
                    <a>
                        <xsl:attribute name="href">
                            <xsl:value-of
                                    select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
                            <xsl:text>/contact</xsl:text>
                        </xsl:attribute>
                        <i18n:text>xmlui.dri2xhtml.structural.contact-link</i18n:text>
                    </a>
                    <xsl:text> | </xsl:text>
                    <a>
                        <xsl:attribute name="href">
                            <xsl:value-of
                                    select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
                            <xsl:text>/feedback</xsl:text>
                        </xsl:attribute>
                        <i18n:text>xmlui.dri2xhtml.structural.feedback-link</i18n:text>
                    </a>
                </div>
                <!--Invisible link to HTML sitemap (for search engines) -->
                <a class="hidden">
                    <xsl:attribute name="href">
                        <xsl:value-of
                                select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
                        <xsl:text>/htmlmap</xsl:text>
                    </xsl:attribute>
                    <xsl:text>&#160;</xsl:text>
                </a>
            </div>
        </div>
    </xsl:template>

    <!--give nested navigation list the class sublist-->
    <xsl:template match="dri:options/dri:list/dri:list" priority="3" mode="nested">
        <li>
            <xsl:apply-templates select="dri:head" mode="nested"/>
            <ul class="ds-simple-list sublist">
                <xsl:apply-templates select="dri:item" mode="nested"/>
            </ul>
        </li>
    </xsl:template>

    <xsl:template name="buildTrail">
        <div class="clearfix" id="ds-trail-wrapper">
            <ul id="ds-trail">
                <xsl:choose>
                    <xsl:when test="starts-with($request-uri, 'page/about')">
                        <xsl:text>About This Repository</xsl:text>
                    </xsl:when>
                    <xsl:when test="count(/dri:document/dri:meta/dri:pageMeta/dri:trail) = 0">
                        <li class="ds-trail-link first-link">-</li>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:apply-templates select="/dri:document/dri:meta/dri:pageMeta/dri:trail"/>
                    </xsl:otherwise>
                </xsl:choose>
            </ul>
        </div>
    </xsl:template>

    <xsl:template match="dri:trail">
        <!--put an arrow between the parts of the trail-->
        <xsl:if test="position()>1">
            <li class="ds-trail-arrow">
                <xsl:text>&#8594;</xsl:text>
            </li>
        </xsl:if>
        <li>
            <xsl:attribute name="class">
                <xsl:text>ds-trail-link </xsl:text>
                <xsl:if test="position()=1">
                    <xsl:text>first-link </xsl:text>
                </xsl:if>
                <xsl:if test="position()=last()">
                    <xsl:text>last-link</xsl:text>
                </xsl:if>
            </xsl:attribute>
            <!-- Determine whether we are dealing with a link or plain text trail link -->
            <xsl:choose>
                <xsl:when test="./@target">
                    <a>
                        <xsl:attribute name="href">
                            <xsl:value-of select="./@target"/>
                        </xsl:attribute>
                        <xsl:apply-templates/>
                    </a>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates/>
                </xsl:otherwise>
            </xsl:choose>
        </li>
    </xsl:template>

    <xsl:template name="addJavascript">
        <xsl:variable name="jqueryVersion">
            <xsl:text>1.7</xsl:text>
        </xsl:variable>

        <xsl:variable name="protocol">
            <xsl:choose>
                <xsl:when test="starts-with(confman:getProperty('dspace.baseUrl'), 'https://')">
                    <xsl:text>https://</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>http://</xsl:text>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <script type="text/javascript" src="{concat($protocol, 'ajax.googleapis.com/ajax/libs/jquery/', $jqueryVersion ,'/jquery.min.js')}">&#160;</script>

        <xsl:variable name="localJQuerySrc">
            <xsl:value-of
                    select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
            <xsl:text>/static/js/jquery-</xsl:text>
            <xsl:value-of select="$jqueryVersion"/>
            <xsl:text>.min.js</xsl:text>
        </xsl:variable>

        <script type="text/javascript">
            <xsl:text disable-output-escaping="yes">!window.jQuery &amp;&amp; document.write('&lt;script type="text/javascript" src="</xsl:text><xsl:value-of
                select="$localJQuerySrc"/><xsl:text
                disable-output-escaping="yes">"&gt;&#160;&lt;\/script&gt;')</xsl:text>
        </script>

        <!-- CDN for other Javascript Libraries + Fallback -->

        <xsl:variable name="localJS">
            <xsl:value-of
                    select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
            <xsl:text>/static/js/</xsl:text>
        </xsl:variable>

        <script type="text/javascript" src="//cdnjs.cloudflare.com/ajax/libs/jquery-backstretch/2.0.4/jquery.backstretch.min.js">//empty comment</script>

        <script type="text/javascript">
            <xsl:text disable-output-escaping="yes">
                if (typeof $.backstretch === 'undefined') {
                document.write('&lt;script type="text/javascript" src="</xsl:text>
            <xsl:value-of select="$localJS"/><xsl:text>jquery.backstretch.js</xsl:text>
            <xsl:text disable-output-escaping="yes">"&gt;&#160;&lt;\/script&gt;')}</xsl:text>
        </script>

        <script type="text/javascript" src="//cdnjs.cloudflare.com/ajax/libs/jquery-easing/1.3/jquery.easing.min.js">//empty comment</script>

        <script type="text/javascript">
            <xsl:text disable-output-escaping="yes">
            if (typeof $.easing === 'undefined') {
                document.write('&lt;script type="text/javascript" src="</xsl:text>
            <xsl:value-of select="$localJS"/><xsl:text>jquery.easing.1.3.js</xsl:text>
            <xsl:text disable-output-escaping="yes">"&gt;&#160;&lt;\/script&gt;')}</xsl:text>
        </script>

        <script type="text/javascript" src="//rawgithub.com/davatron5000/FitText.js/master/jquery.fittext.js">//empty comment</script>

        <script type="text/javascript">
            <xsl:text disable-output-escaping="yes">
            if (typeof $.fitText === 'undefined') {
                document.write('&lt;script type="text/javascript" src="</xsl:text>
            <xsl:value-of select="$localJS"/><xsl:text>jquery.fittext.js</xsl:text>
            <xsl:text disable-output-escaping="yes">"&gt;&#160;&lt;\/script&gt;')}</xsl:text>
        </script>

        <script type="text/javascript" src="//cdnjs.cloudflare.com/ajax/libs/jquery-cookie/1.3.1/jquery.cookie.min.js">//empty comment</script>

        <script type="text/javascript">
            <xsl:text disable-output-escaping="yes">
            if (typeof $.cookie === 'undefined') {
                document.write('&lt;script type="text/javascript" src="</xsl:text>
            <xsl:value-of select="$localJS"/><xsl:text>jquery.cookie.js</xsl:text>
            <xsl:text disable-output-escaping="yes">"&gt;&#160;&lt;\/script&gt;')}</xsl:text>
        </script>
        <!-- End of CDN libraries and fallback -->

        <!-- Add theme javascipt  -->
        <xsl:for-each
                select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='javascript'][@qualifier='url']">
            <script type="text/javascript">
                <xsl:attribute name="src">
                    <xsl:value-of select="."/>
                </xsl:attribute>
                &#160;
            </script>
        </xsl:for-each>

        <xsl:for-each select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='javascript'][not(@qualifier)]">
            <script type="text/javascript">
                <xsl:attribute name="src">
                    <xsl:value-of
                            select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
                    <xsl:text>/themes/</xsl:text>
                    <xsl:value-of
                            select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='theme'][@qualifier='path']"/>
                    <xsl:text>/</xsl:text>
                    <xsl:value-of select="."/>
                </xsl:attribute>
                &#160;
            </script>
        </xsl:for-each>

        <!-- add "shared" javascript from static, path is relative to webapp root -->
        <xsl:for-each
                select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='javascript'][@qualifier='static']">
            <!--This is a dirty way of keeping the scriptaculous stuff from choice-support
            out of our theme without modifying the administrative and submission sitemaps.
            This is obviously not ideal, but adding those scripts in those sitemaps is far
            from ideal as well-->
            <xsl:choose>
                <xsl:when test="text() = 'static/js/choice-support.js'">
                    <script type="text/javascript">
                        <xsl:attribute name="src">
                            <xsl:value-of
                                    select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
                            <xsl:text>/themes/</xsl:text>
                            <xsl:value-of
                                    select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='theme'][@qualifier='path']"/>
                            <xsl:text>/lib/js/choice-support.js</xsl:text>
                        </xsl:attribute>
                        &#160;
                    </script>
                </xsl:when>
                <xsl:when test="not(starts-with(text(), 'static/js/scriptaculous'))">
                    <script type="text/javascript">
                        <xsl:attribute name="src">
                            <xsl:value-of
                                    select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
                            <xsl:text>/</xsl:text>
                            <xsl:value-of select="."/>
                        </xsl:attribute>
                        &#160;
                    </script>
                </xsl:when>
            </xsl:choose>
        </xsl:for-each>

        <!-- add setup JS code if this is a choices lookup page -->
        <xsl:if test="dri:body/dri:div[@n='lookup']">
            <xsl:call-template name="choiceLookupPopUpSetup"/>
        </xsl:if>

        <!--PNG Fix for IE6-->
        <xsl:text disable-output-escaping="yes">&lt;!--[if lt IE 7 ]&gt;</xsl:text>
        <script type="text/javascript">
            <xsl:attribute name="src">
                <xsl:value-of
                        select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
                <xsl:text>/themes/</xsl:text>
                <xsl:value-of
                        select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='theme'][@qualifier='path']"/>
                <xsl:text>/lib/js/DD_belatedPNG_0.0.8a.js?v=1</xsl:text>
            </xsl:attribute>
            &#160;
        </script>
        <script type="text/javascript">
            <xsl:text>DD_belatedPNG.fix('#ds-header-logo');DD_belatedPNG.fix('#ds-footer-logo');$.each($('img[src$=png]'), function() {DD_belatedPNG.fixPng(this);});</xsl:text>
        </script>
        <xsl:text disable-output-escaping="yes">&lt;![endif]--&gt;</xsl:text>

        <script type="text/javascript">
            runAfterJSImports.execute();
        </script>

        <!-- Add a google analytics script if the key is present -->
        <xsl:if test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='google'][@qualifier='analytics']">
            <script type="text/javascript"><xsl:text>
                   var _gaq = _gaq || [];
                   _gaq.push(['_setAccount', '</xsl:text><xsl:value-of
                    select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='google'][@qualifier='analytics']"/><xsl:text>']);
                   _gaq.push(['_trackPageview']);

                   (function() {
                       var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
                       ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
                       var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
                   })();
           </xsl:text>
            </script>
        </xsl:if>
    </xsl:template>

    <xsl:template match="dri:document">
        <html class="no-js" lang="en">
            <!-- First of all, build the HTML head element -->
            <xsl:call-template name="buildHead"/>
            <!-- Then proceed to the body -->

            <!--paulirish.com/2008/conditional-stylesheets-vs-css-hacks-answer-neither/-->
            <xsl:text disable-output-escaping="yes">&lt;!--[if lt IE 7 ]&gt; &lt;body class="ie6"&gt; &lt;![endif]--&gt;
                &lt;!--[if IE 7 ]&gt;    &lt;body class="ie7"&gt; &lt;![endif]--&gt;
                &lt;!--[if IE 8 ]&gt;    &lt;body class="ie8"&gt; &lt;![endif]--&gt;
                &lt;!--[if IE 9 ]&gt;    &lt;body class="ie9"&gt; &lt;![endif]--&gt;
                &lt;!--[if (gt IE 9)|!(IE)]&gt;&lt;!--&gt;&lt;body&gt;&lt;!--&lt;![endif]--&gt;</xsl:text>

            <xsl:choose>
                <xsl:when
                        test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='framing'][@qualifier='popup']">
                    <xsl:apply-templates select="dri:body/*"/>
                </xsl:when>
                <xsl:otherwise>
                    <div id="ds-main">
                        <!--The header div, complete with title, subtitle and other junk-->
                        <xsl:call-template name="buildHeader"/>

                        <!--The trail is built by applying a template over pageMeta's trail children. -->
                        <xsl:call-template name="buildTrail"/>

                        <!--javascript-disabled warning, will be invisible if javascript is enabled-->
                        <div id="no-js-warning-wrapper" class="hidden">
                            <div id="no-js-warning">
                                <div class="notice failure">
                                    <xsl:text>JavaScript is disabled for your browser. Some features of this site may not work without it.</xsl:text>
                                </div>
                            </div>
                        </div>


                        <!--ds-content is a groups ds-body and the navigation together and used to put the clearfix on, center, etc.
                            ds-content-wrapper is necessary for IE6 to allow it to center the page content-->
                        <div id="ds-content-wrapper">
                            <div id="ds-content" class="clearfix">
                                <!--
                               Goes over the document tag's children elements: body, options, meta. The body template
                               generates the ds-body div that contains all the content. The options template generates
                               the ds-options div that contains the navigation and action options available to the
                               user. The meta element is ignored since its contents are not processed directly, but
                               instead referenced from the different points in the document. -->
                                <xsl:apply-templates/>
                            </div>
                        </div>




                    </div>
                        <!--
                            The footer div, dropping whatever extra information is needed on the page. It will
                            most likely be something similar in structure to the currently given example. -->
                        <xsl:call-template name="buildFooter"/>
                </xsl:otherwise>
            </xsl:choose>
            <!-- Javascript at the bottom for fast page loading -->
            <xsl:call-template name="addJavascript"/>

            <xsl:text disable-output-escaping="yes">&lt;/body&gt;</xsl:text>
        </html>
    </xsl:template>

    <!-- Remove search box from the front page & community/collection pages -->
    <xsl:template match="dri:div[@id='aspect.discovery.CollectionSearch.div.collection-search'] | dri:div[@id='aspect.discovery.CommunitySearch.div.community-search']
     | dri:div[@id='aspect.discovery.SiteViewer.div.front-page-search']">
    </xsl:template>

    <!-- Add each RSS feed from meta to a list -->
    <xsl:template name="addRSSLinks">
        <xsl:for-each select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='feed']">
            <li>
                <a>
                    <xsl:attribute name="href">
                        <xsl:value-of select="."/>
                    </xsl:attribute>
                    <xsl:choose>
                        <xsl:when test="contains(., 'rss_1.0')">
                            <xsl:text>RSS 1.0</xsl:text>
                        </xsl:when>
                        <xsl:when test="contains(., 'rss_2.0')">
                            <xsl:text>RSS 2.0</xsl:text>
                        </xsl:when>
                        <xsl:when test="contains(., 'atom_1.0')">
                            <xsl:text>Atom</xsl:text>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="@qualifier"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </a>
            </li>
        </xsl:for-each>
    </xsl:template>

    <!--Override Item list and Related Items display -->
    <xsl:template match="dim:dim" mode="itemSummaryList-DIM-metadata">
        <xsl:param name="href"/>
        <div class="artifact-info">
            <span class="author">
                <xsl:choose>
                    <xsl:when test="dim:field[@element='contributor'][@qualifier='author']">
                        <xsl:for-each select="dim:field[@element='contributor'][@qualifier='author']">
                            <span>
                                <xsl:if test="@authority">
                                    <xsl:attribute name="class">
                                        <xsl:text>ds-dc_contributor_author-authority</xsl:text>
                                    </xsl:attribute>
                                </xsl:if>

                                <xsl:variable name="firstname">
                                    <xsl:value-of select="substring-after(., ', ')"/>
                                </xsl:variable>
                                <xsl:if test="position() = last() and position() != 1">
                                    <xsl:text>&amp; </xsl:text>
                                </xsl:if>
                                <xsl:if test="position() &lt;=6 or position() = last()">
                                    <xsl:choose>
                                        <xsl:when test="substring($firstname,2,1) = '.'">
                                            <xsl:value-of select="concat(str:tokenize(.,','), ', ')"/>
                                            <xsl:for-each select="str:tokenize($firstname,'.')">
                                                <xsl:value-of select="concat(.,'.')"/>
                                                <xsl:if test="position() != last()">
                                                    <xsl:text> </xsl:text>
                                                </xsl:if>
                                            </xsl:for-each>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="concat(str:tokenize(.,','), ', ')"/>
                                            <xsl:for-each select="str:tokenize($firstname,' ')">
                                                <xsl:choose>
                                                    <xsl:when
                                                            test="contains($firstname,'Jr.') or contains($firstname,'III')">
                                                        <xsl:choose>
                                                            <xsl:when test="position() = last()">
                                                                <xsl:value-of select="substring(.,1,3)"/>
                                                            </xsl:when>
                                                            <xsl:when test="position() != 1">
                                                                <xsl:value-of select="concat(substring(.,1,1),'.,')"/>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                                <xsl:value-of select="concat(substring(.,1,1),'.')"/>
                                                            </xsl:otherwise>
                                                        </xsl:choose>
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        <xsl:value-of select="concat(substring(.,1,1),'.')"/>
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                                <xsl:if test="position() != last()">
                                                    <xsl:text> </xsl:text>
                                                </xsl:if>
                                            </xsl:for-each>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:if>
                                <xsl:if test="position() = 7 and position() != last()">
                                    <xsl:text>...</xsl:text>
                                </xsl:if>
                                <xsl:if test="position() != last() and position() &lt; 7">
                                    <xsl:text>, </xsl:text>
                                </xsl:if>
                            </span>
                        </xsl:for-each>
                    </xsl:when>
                    <xsl:when test="dim:field[@element='creator']">
                        <xsl:for-each select="dim:field[@element='creator']">
                            <xsl:copy-of select="node()"/>
                            <xsl:if test="count(following-sibling::dim:field[@element='creator']) != 0">
                                <xsl:text>; </xsl:text>
                            </xsl:if>
                        </xsl:for-each>
                    </xsl:when>
                    <xsl:when test="dim:field[@element='contributor']">
                        <xsl:for-each select="dim:field[@element='contributor']">
                            <xsl:copy-of select="node()"/>
                            <xsl:if test="count(following-sibling::dim:field[@element='contributor']) != 0">
                                <xsl:text>; </xsl:text>
                            </xsl:if>
                        </xsl:for-each>
                    </xsl:when>
                    <xsl:otherwise>
                        <!-- <i18n:text>xmlui.dri2xhtml.METS-1.0.no-author</i18n:text> -->
                        <xsl:text>Anon.</xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
            </span>
            <span class="date">
                <xsl:value-of
                        select="concat('(',substring(dim:field[@element='date' and @qualifier='issued']/node(),1,4),').')"/>
            </span>
            <!--
            <xsl:if test="dim:field[@element='date' and @qualifier='issued'] or dim:field[@element='publisher']">
                <span class="publisher-date">
                    <xsl:text>(</xsl:text>
                    <xsl:if test="dim:field[@element='publisher']">
                        <span class="publisher">
                            <xsl:copy-of select="dim:field[@element='publisher']/node()"/>
                        </span>
                        <xsl:text>, </xsl:text>
                    </xsl:if>
                    <span class="date">
                        <xsl:value-of select="substring(dim:field[@element='date' and @qualifier='issued']/node(),1,10)"/>
                    </span>
                    <xsl:text>)</xsl:text>
                </span>
            </xsl:if>
            -->
            <span class="artifact-description">
                <span class="artifact-title" style="font-size: 100%">
                    <xsl:element name="a">
                        <xsl:attribute name="href">
                            <xsl:value-of select="$href"/>
                        </xsl:attribute>
                        <xsl:choose>
                            <xsl:when test="dim:field[@element='title']">
                                <xsl:value-of select="dim:field[@element='title'][1]/node()"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <i18n:text>xmlui.dri2xhtml.METS-1.0.no-title</i18n:text>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:element>
                    <xsl:choose>
                        <xsl:when
                                test="dim:field[@element='citation' and @qualifier='journalTitle'] and dim:field[@element='date' and @qualifier='issued']">
                            <span class="publisher-date">
                                <xsl:text disable-output-escaping="yes">&lt;i&gt;</xsl:text>
                                <xsl:value-of
                                        select="dim:field[@element='citation' and @qualifier='journalTitle']/node()"/>
                                <xsl:text disable-output-escaping="yes">&lt;/i&gt;</xsl:text>
                            </span>
                            <xsl:text>, </xsl:text>
                            <xsl:choose>
                                <xsl:when
                                        test="dim:field[@element='citation' and @qualifier='volume'] and dim:field[@element='citation' and @qualifier='issue']">
                                    <span class="publisher">
                                        <xsl:text disable-output-escaping="yes">&lt;i&gt;</xsl:text>
                                        <xsl:value-of select="dim:field[@element='citation' and @qualifier='volume']"/>
                                        <xsl:text disable-output-escaping="yes">&lt;/i&gt;</xsl:text>
                                        <xsl:text>(</xsl:text>
                                        <xsl:value-of select="dim:field[@element='citation' and @qualifier='issue']"/>
                                        <xsl:text>)</xsl:text>
                                        <xsl:text>, </xsl:text>
                                    </span>
                                </xsl:when>
                                <xsl:when
                                        test="dim:field[@element='citation' and @qualifier='volume'] and dim:field[not(@element='citation' and @qualifier='issue')]">
                                    <span class="publisher">
                                        <xsl:text disable-output-escaping="yes">&lt;i&gt;</xsl:text>
                                        <xsl:value-of select="dim:field[@element='citation' and @qualifier='volume']"/>
                                        <xsl:text disable-output-escaping="yes">&lt;/i&gt;</xsl:text>
                                        <xsl:text>, </xsl:text>
                                    </span>
                                </xsl:when>
                                <xsl:when
                                        test="dim:field[@element='citation' and @qualifier='issue'] and dim:field[not(@element='citation' and @qualifier='volume')]">
                                    <span class="publisher">
                                        <xsl:text>(</xsl:text>
                                        <xsl:value-of select="dim:field[@element='citation' and @qualifier='issue']"/>
                                        <xsl:text>)</xsl:text>
                                        <xsl:text>, </xsl:text>
                                    </span>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="dim:field[@element='citation' and @qualifier='issue']"/>
                                    <xsl:text>, </xsl:text>
                                </xsl:otherwise>
                            </xsl:choose>
                            <xsl:choose>
                                <xsl:when
                                        test="dim:field[@element='citation' and @qualifier='spage'] and dim:field[@element='citation' and @qualifier='epage']">
                                    <span class="publisher">
                                        <xsl:value-of select="dim:field[@element='citation' and @qualifier='spage']"/>
                                        <xsl:text>-</xsl:text>
                                        <xsl:value-of select="dim:field[@element='citation' and @qualifier='epage']"/>
                                        <xsl:text>.</xsl:text>
                                    </span>
                                </xsl:when>
                                <xsl:otherwise>
                                    <span class="publisher">
                                        <xsl:value-of select="dim:field[@element='citation' and @qualifier='spage']"/>
                                        <xsl:text>.</xsl:text>
                                    </span>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
                        <xsl:otherwise>
                            <span class="publisher-date">
                                <xsl:if test="dim:field[@element='publisher']">
                                    <span class="publisher">
                                        <xsl:for-each select="dim:field[@element='publisher']">
                                            <xsl:copy-of select="node()"/>
                                            <xsl:if test="count(following-sibling::dim:field[@element='publisher']) != 0">
                                                <xsl:text>; </xsl:text>
                                            </xsl:if>
                                        </xsl:for-each>
                                    </span>
                                    <xsl:if test="substring(dim:field[@element='publisher'], string-length(dim:field[@element='publisher'])) != '.'">
                                        <xsl:text>.</xsl:text>
                                    </xsl:if>
                                </xsl:if>
                            </span>
                        </xsl:otherwise>
                    </xsl:choose>
                    <span class="Z3988">
                        <xsl:attribute name="title">
                            <xsl:call-template name="renderCOinS"/>
                        </xsl:attribute>
                        &#xFEFF; <!-- non-breaking space to force separating the end tag -->
                    </span>
                </span>
                <xsl:if test="dim:field[@element = 'description' and @qualifier='abstract']">
                    <xsl:variable name="abstract"
                                  select="dim:field[@element = 'description' and @qualifier='abstract']/node()"/>
                    <div class="artifact-abstract">
                        <xsl:value-of select="util:shortenString($abstract, 220, 10)" disable-output-escaping="yes"/>
                    </div>
                </xsl:if>
            </span>
        </div>
    </xsl:template>

</xsl:stylesheet>
