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
	xmlns="http://www.w3.org/1999/xhtml"
    xmlns:util="org.dspace.app.xmlui.utils.XSLUtils"
    xmlns:confman="org.dspace.core.ConfigurationManager"
    xmlns:str="http://exslt.org/strings"
    exclude-result-prefixes="i18n dri mets xlink xsl dim xhtml mods dc util confman">

    <!--<xsl:import href="../dri2xhtml-alt/dri2xhtml.xsl"/>-->
    <xsl:import href="aspect/artifactbrowser/artifactbrowser.xsl"/>
    <xsl:import href="core/global-variables.xsl"/>
    <xsl:import href="core/elements.xsl"/>
    <xsl:import href="core/forms.xsl"/>
    <xsl:import href="core/page-structure.xsl"/>
    <xsl:import href="core/navigation.xsl"/>
    <xsl:import href="core/attribute-handlers.xsl"/>
    <xsl:import href="core/utils.xsl"/>
    <xsl:import href="aspect/general/choice-authority-control.xsl"/>
    <xsl:import href="aspect/general/vocabulary-support.xsl"/>
    <!--<xsl:import href="xsl/aspect/administrative/administrative.xsl"/>-->
    <xsl:import href="aspect/artifactbrowser/common.xsl"/>
    <xsl:import href="aspect/artifactbrowser/item-list.xsl"/>
    <xsl:import href="aspect/artifactbrowser/item-view.xsl"/>
    <xsl:import href="aspect/artifactbrowser/community-list.xsl"/>
    <xsl:import href="aspect/artifactbrowser/collection-list.xsl"/>
    <xsl:import href="aspect/artifactbrowser/browse.xsl"/>
    <xsl:import href="aspect/discovery/discovery.xsl"/>
    <xsl:import href="aspect/artifactbrowser/one-offs.xsl"/>
    <xsl:import href="aspect/submission/submission.xsl"/>
    <xsl:output indent="yes"/>

    <xsl:template match="dri:document">

        <xsl:choose>
            <xsl:when test="not($isModal)">


            <xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html&gt;
            </xsl:text>
            <xsl:text disable-output-escaping="yes">&lt;!--[if lt IE 7]&gt; &lt;html class=&quot;no-js lt-ie9 lt-ie8 lt-ie7&quot; lang=&quot;en&quot;&gt; &lt;![endif]--&gt;
            &lt;!--[if IE 7]&gt;    &lt;html class=&quot;no-js lt-ie9 lt-ie8&quot; lang=&quot;en&quot;&gt; &lt;![endif]--&gt;
            &lt;!--[if IE 8]&gt;    &lt;html class=&quot;no-js lt-ie9&quot; lang=&quot;en&quot;&gt; &lt;![endif]--&gt;
            &lt;!--[if gt IE 8]&gt;&lt;!--&gt; &lt;html class=&quot;no-js&quot; lang=&quot;en&quot;&gt; &lt;!--&lt;![endif]--&gt;
            </xsl:text>

                <!-- First of all, build the HTML head element -->

                <xsl:call-template name="buildHead"/>

                <!-- Then proceed to the body -->
                <body>
                    <!-- Prompt IE 6 users to install Chrome Frame. Remove this if you support IE 6.
                   chromium.org/developers/how-tos/chrome-frame-getting-started -->
                    <!--[if lt IE 7]><p class=chromeframe>Your browser is <em>ancient!</em> <a href="http://browsehappy.com/">Upgrade to a different browser</a> or <a href="http://www.google.com/chromeframe/?redirect=true">install Google Chrome Frame</a> to experience this site.</p><![endif]-->
                    <xsl:choose>
                        <xsl:when
                                test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='framing'][@qualifier='popup']">
                            <xsl:apply-templates select="dri:body/*"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:call-template name="buildHeader"/>
                            <xsl:call-template name="buildTrail"/>
                            <!--javascript-disabled warning, will be invisible if javascript is enabled-->
                            <div id="no-js-warning-wrapper" class="hidden">
                                <div id="no-js-warning">
                                    <div class="notice failure">
                                        <xsl:text>JavaScript is disabled for your browser. Some features of this site may not work without it.</xsl:text>
                                    </div>
                                </div>
                            </div>

                            <div id="main-container" class="container">

                                <div class="row row-offcanvas row-offcanvas-right">
                                    <div class="horizontal-slider clearfix">
                                        <div class="col-xs-12 col-sm-12 col-md-9 main-content">
                                            <xsl:apply-templates select="*[not(self::dri:options)]"/>
                                            <div class="visible-xs">
                                                <xsl:call-template name="buildFooterSmall"/>
                                            </div>
                                        </div>
                                        <div class="col-xs-6 col-sm-3 sidebar-offcanvas" id="sidebar" role="navigation">
                                            <xsl:apply-templates select="dri:options"/>
                                        </div>

                                    </div>
                                </div>

                                <!--
                            The footer div, dropping whatever extra information is needed on the page. It will
                            most likely be something similar in structure to the currently given example. -->
                                <div class="hidden-xs">
                                    <xsl:call-template name="buildFooter"/>
                                </div>
                            </div>


                        </xsl:otherwise>
                    </xsl:choose>
                    <!-- Javascript at the bottom for fast page loading -->
                    <xsl:call-template name="addJavascript"/>
                </body>
                <xsl:text disable-output-escaping="yes">&lt;/html&gt;</xsl:text>

            </xsl:when>
            <xsl:otherwise>
                <!-- This is only a starting point. If you want to use this feature you need to implement
                JavaScript code and a XSLT template by yourself. Currently this is used for the DSpace Value Lookup -->
                <xsl:apply-templates select="dri:body" mode="modal"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="dri:options">
        <div id="ds-options" class="word-break hidden-print">
            <xsl:apply-templates/>
            <!-- DS-984 Add RSS Links to Options Box -->
            <xsl:if test="count(/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='feed']) != 0">
                <div>
                    <h2 class="ds-option-set-head h6">
                        <i18n:text>xmlui.feed.header</i18n:text>
                    </h2>
                    <div id="ds-feed-option" class="ds-option-set list-group">
                        <xsl:call-template name="addRSSLinks"/>
                    </div>
                </div>

            </xsl:if>
        </div>
    </xsl:template>

    <xsl:template name="buildHeader">


        <header>
            <div class="navbar navbar-default navbar-fixed-top" role="navigation">
                <div class="container">
                    <div class="navbar-header">

                        <button type="button" class="navbar-toggle" data-toggle="offcanvas">
                            <span class="sr-only">
                                <i18n:text>xmlui.mirage2.page-structure.toggleNavigation</i18n:text>
                            </span>
                            <span class="icon-bar"></span>
                            <span class="icon-bar"></span>
                            <span class="icon-bar"></span>
                        </button>

                        <div>
                            <a href="{$context-path}/" class="navbar-brand">
                                <img class="hidden-lg img-responsive" src="{$theme-path}/images/seafdec-sm.svg" />
                                <img class="visible-lg" src="{$theme-path}/images/seafdec-lg.svg" />
                            </a>
                        </div>


                        <div class="navbar-header pull-right visible-xs hidden-sm hidden-md hidden-lg">
                            <ul class="nav nav-pills pull-right">

                                <xsl:if test="count(/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='page'][@qualifier='supportedLocale']) &gt; 1">
                                    <li id="ds-language-selection-xs" class="dropdown">
                                        <xsl:variable name="active-locale" select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='page'][@qualifier='currentLocale']"/>
                                        <button id="language-dropdown-toggle-xs" href="#" role="button" class="dropdown-toggle navbar-toggle navbar-link" data-toggle="dropdown">
                                            <b class="visible-xs glyphicon glyphicon-globe" aria-hidden="true"/>
                                        </button>
                                        <ul class="dropdown-menu pull-right" role="menu" aria-labelledby="language-dropdown-toggle-xs" data-no-collapse="true">
                                            <xsl:for-each
                                                    select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='page'][@qualifier='supportedLocale']">
                                                <xsl:variable name="locale" select="."/>
                                                <li role="presentation">
                                                    <xsl:if test="$locale = $active-locale">
                                                        <xsl:attribute name="class">
                                                            <xsl:text>disabled</xsl:text>
                                                        </xsl:attribute>
                                                    </xsl:if>
                                                    <a>
                                                        <xsl:attribute name="href">
                                                            <xsl:value-of select="$current-uri"/>
                                                            <xsl:text>?locale-attribute=</xsl:text>
                                                            <xsl:value-of select="$locale"/>
                                                        </xsl:attribute>
                                                        <xsl:value-of
                                                                select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='supportedLocale'][@qualifier=$locale]"/>
                                                    </a>
                                                </li>
                                            </xsl:for-each>
                                        </ul>
                                    </li>
                                </xsl:if>

                                <xsl:if test="not(contains(/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request'][@qualifier='URI'], 'discover'))">
                                    <li class="dropdown">
                                    <button class="dropdown-toggle navbar-toggle navbar-link" id="search-dropdown-toggle-xs" href="#" role="button"  data-toggle="dropdown">
                                        <b class="visible-xs glyphicon glyphicon-search" aria-hidden="true"/>
                                    </button>
                                    <ul class="dropdown-menu pull-right" role="menu"
                                        aria-labelledby="search-dropdown-toggle-xs" data-no-collapse="true">
                                        <li>
                                            <form id="ds-search-form" class="" method="post">
                                                <xsl:attribute name="action">
                                                    <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath']"/>
                                                    <xsl:value-of
                                                            select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='search'][@qualifier='simpleURL']"/>
                                                </xsl:attribute>
                                                <fieldset>
                                                    <div class="input-group">
                                                        <input class="ds-text-field form-control" type="text" placeholder="xmlui.general.search"
                                                               i18n:attr="placeholder">
                                                            <xsl:attribute name="name">
                                                                <xsl:value-of
                                                                        select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='search'][@qualifier='queryField']"/>
                                                            </xsl:attribute>
                                                        </input>
                                                        <span class="input-group-btn">
                                                            <button class="ds-button-field btn btn-primary" title="xmlui.general.go" i18n:attr="title">
                                                                <span class="glyphicon glyphicon-search" aria-hidden="true"/>
                                                                <xsl:attribute name="onclick">
                                                    <xsl:text>
                                                        var radio = document.getElementById(&quot;ds-search-form-scope-container&quot;);
                                                        if (radio != undefined &amp;&amp; radio.checked)
                                                        {
                                                        var form = document.getElementById(&quot;ds-search-form&quot;);
                                                        form.action=
                                                    </xsl:text>
                                                                    <xsl:text>&quot;</xsl:text>
                                                                    <xsl:value-of
                                                                            select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath']"/>
                                                                    <xsl:text>/handle/&quot; + radio.value + &quot;</xsl:text>
                                                                    <xsl:value-of
                                                                            select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='search'][@qualifier='simpleURL']"/>
                                                                    <xsl:text>&quot; ; </xsl:text>
                                                    <xsl:text>
                                                        }
                                                    </xsl:text>
                                                                </xsl:attribute>
                                                            </button>
                                                        </span>
                                                    </div>

                                                    <xsl:if test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='focus'][@qualifier='container']">
                                                        <div class="radio">
                                                            <label>
                                                                <input id="ds-search-form-scope-all" type="radio" name="scope" value=""
                                                                       checked="checked"/>
                                                                <i18n:text>xmlui.dri2xhtml.structural.search</i18n:text>
                                                            </label>
                                                        </div>
                                                        <div class="radio">
                                                            <label>
                                                                <input id="ds-search-form-scope-container" type="radio" name="scope">
                                                                    <xsl:attribute name="value">
                                                                        <xsl:value-of
                                                                                select="substring-after(/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='focus'][@qualifier='container'],':')"/>
                                                                    </xsl:attribute>
                                                                </input>
                                                                <xsl:choose>
                                                                    <xsl:when
                                                                            test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='focus'][@qualifier='containerType']/text() = 'type:community'">
                                                                        <i18n:text>xmlui.dri2xhtml.structural.search-in-community</i18n:text>
                                                                    </xsl:when>
                                                                    <xsl:otherwise>
                                                                        <i18n:text>xmlui.dri2xhtml.structural.search-in-collection</i18n:text>
                                                                    </xsl:otherwise>

                                                                </xsl:choose>
                                                            </label>
                                                        </div>
                                                    </xsl:if>
                                                </fieldset>
                                            </form>
                                        </li>
                                    </ul>
                                </li>
                                </xsl:if>

                                <xsl:choose>
                                    <xsl:when test="/dri:document/dri:meta/dri:userMeta/@authenticated = 'yes'">
                                        <li class="dropdown">
                                            <button class="dropdown-toggle navbar-toggle navbar-link" id="user-dropdown-toggle-xs" href="#" role="button"  data-toggle="dropdown">
                                                <b class="visible-xs glyphicon glyphicon-user" aria-hidden="true"/>
                                            </button>
                                            <ul class="dropdown-menu pull-right" role="menu"
                                                aria-labelledby="user-dropdown-toggle-xs" data-no-collapse="true">
                                                <li>
                                                    <a href="{/dri:document/dri:meta/dri:userMeta/
                            dri:metadata[@element='identifier' and @qualifier='url']}">
                                                        <i18n:text>xmlui.EPerson.Navigation.profile</i18n:text>
                                                    </a>
                                                </li>
                                                <li>
                                                    <a href="{/dri:document/dri:meta/dri:userMeta/
                            dri:metadata[@element='identifier' and @qualifier='logoutURL']}">
                                                        <i18n:text>xmlui.dri2xhtml.structural.logout</i18n:text>
                                                    </a>
                                                </li>
                                            </ul>
                                        </li>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <li>
                                            <form style="display: inline" action="{/dri:document/dri:meta/dri:userMeta/
                            dri:metadata[@element='identifier' and @qualifier='loginURL']}" method="get">
                                                <button class="navbar-toggle navbar-link">
                                                    <b class="visible-xs glyphicon glyphicon-user" aria-hidden="true"/>
                                                </button>
                                            </form>
                                        </li>
                                    </xsl:otherwise>
                                </xsl:choose>

                            </ul>
                        </div>
                    </div>

                    <div class="navbar-header pull-right hidden-xs">
                        <ul class="nav navbar-nav pull-left">
                            <xsl:call-template name="languageSelection"/>
                        </ul>
                        <xsl:if test="not(contains(/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request'][@qualifier='URI'], 'discover'))">
                            <form id="ds-search-form" class="navbar-form navbar-left" method="post">
                                                <xsl:attribute name="action">
                                                    <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath']"/>
                                                    <xsl:value-of
                                                            select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='search'][@qualifier='simpleURL']"/>
                                                </xsl:attribute>
                                                <fieldset>
                                                    <div class="input-group">
                                                        <input class="ds-text-field form-control" type="text" placeholder="xmlui.general.search"
                                                               i18n:attr="placeholder">
                                                            <xsl:attribute name="name">
                                                                <xsl:value-of
                                                                        select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='search'][@qualifier='queryField']"/>
                                                            </xsl:attribute>
                                                        </input>
                                                        <span class="input-group-btn">
                                                            <button class="ds-button-field btn btn-primary" title="xmlui.general.go" i18n:attr="title">
                                                                <span class="glyphicon glyphicon-search" aria-hidden="true"/>
                                                                <xsl:attribute name="onclick">
                                                    <xsl:text>
                                                        var form = document.getElementById(&quot;ds-search-form&quot;);
                                                        form.action=
                                                    </xsl:text>
                                                                    <xsl:text>&quot;</xsl:text>
                                                                    <xsl:value-of
                                                                            select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath']"/>
                                                    <xsl:text>/handle/&quot;</xsl:text>
                                                                    <xsl:value-of
                                                                            select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='search'][@qualifier='simpleURL']"/>
                                                                    <xsl:text>&quot; ; </xsl:text>
                                                                </xsl:attribute>
                                                            </button>
                                                        </span>
                                                    </div>
                                                </fieldset>
                                            </form>
                        </xsl:if>
                        <ul class="nav navbar-nav pull-left">
                            <xsl:choose>
                                <xsl:when test="/dri:document/dri:meta/dri:userMeta/@authenticated = 'yes'">
                                    <li class="dropdown">
                                        <a id="user-dropdown-toggle" href="#" role="button" class="dropdown-toggle"
                                           data-toggle="dropdown">
                                            <span class="hidden-xs">
                                                <xsl:value-of select="/dri:document/dri:meta/dri:userMeta/
                            dri:metadata[@element='identifier' and @qualifier='firstName']"/>
                                                <xsl:text> </xsl:text>
                                                <xsl:value-of select="/dri:document/dri:meta/dri:userMeta/
                            dri:metadata[@element='identifier' and @qualifier='lastName']"/>&#160;<b class="caret"/>
                                            </span>
                                        </a>
                                        <ul class="dropdown-menu pull-right" role="menu"
                                            aria-labelledby="user-dropdown-toggle" data-no-collapse="true">
                                            <li>
                                                <a href="{/dri:document/dri:meta/dri:userMeta/
                            dri:metadata[@element='identifier' and @qualifier='url']}">
                                                    <i18n:text>xmlui.EPerson.Navigation.profile</i18n:text>
                                                </a>
                                            </li>
                                            <li>
                                                <a href="{/dri:document/dri:meta/dri:userMeta/
                            dri:metadata[@element='identifier' and @qualifier='logoutURL']}">
                                                    <i18n:text>xmlui.dri2xhtml.structural.logout</i18n:text>
                                                </a>
                                            </li>
                                        </ul>
                                    </li>
                                </xsl:when>
                                <xsl:otherwise>
                                    <li>
                                        <a href="{/dri:document/dri:meta/dri:userMeta/
                            dri:metadata[@element='identifier' and @qualifier='loginURL']}">
                                            <span class="hidden-xs">
                                                <i18n:text>xmlui.dri2xhtml.structural.login</i18n:text>
                                            </span>
                                        </a>
                                    </li>
                                </xsl:otherwise>
                            </xsl:choose>
                        </ul>

                        <button data-toggle="offcanvas" class="navbar-toggle visible-sm" type="button">
                            <span class="sr-only"><i18n:text>xmlui.mirage2.page-structure.toggleNavigation</i18n:text></span>
                            <span class="icon-bar"></span>
                            <span class="icon-bar"></span>
                            <span class="icon-bar"></span>
                        </button>
                    </div>
                </div>
            </div>

        </header>

    </xsl:template>

    <!--The Trail-->
    <xsl:template match="dri:trail">
        <!--put an arrow between the parts of the trail-->
        <li>
            <xsl:if test="position()=1">
                <i class="fa fa-institution" aria-hidden="true"/>&#160;
            </xsl:if>
            <!-- Determine whether we are dealing with a link or plain text trail link -->
            <xsl:choose>
                <xsl:when test="./@target">
                    <a>
                        <xsl:attribute name="href">
                            <xsl:value-of select="./@target"/>
                        </xsl:attribute>
                        <xsl:apply-templates />
                    </a>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="class">active</xsl:attribute>
                    <xsl:apply-templates />
                </xsl:otherwise>
            </xsl:choose>
        </li>
    </xsl:template>

    <xsl:template match="dri:trail" mode="dropdown">
        <!--put an arrow between the parts of the trail-->
        <li role="presentation">
            <!-- Determine whether we are dealing with a link or plain text trail link -->
            <xsl:choose>
                <xsl:when test="./@target">
                    <a role="menuitem">
                        <xsl:attribute name="href">
                            <xsl:value-of select="./@target"/>
                        </xsl:attribute>
                        <xsl:if test="position()=1">
                            <i class="fa fa-institution" aria-hidden="true"/>&#160;
                        </xsl:if>
                        <xsl:apply-templates />
                    </a>
                </xsl:when>
                <xsl:when test="position() > 1 and position() = last()">
                    <xsl:attribute name="class">disabled</xsl:attribute>
                    <a role="menuitem" href="#">
                        <xsl:apply-templates />
                    </a>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="class">active</xsl:attribute>
                    <xsl:if test="position()=1">
                        <i class="fa fa-institution" aria-hidden="true"/>&#160;
                    </xsl:if>
                    <xsl:apply-templates />
                </xsl:otherwise>
            </xsl:choose>
        </li>
    </xsl:template>

    <!-- Like the header, the footer contains various miscellaneous text, links, and image placeholders -->
    <xsl:template name="buildFooter">
        <footer>
            <div class="row">
                <hr/>
                <div align="center">
                    <div>Library &amp; Data Banking Services Section | Training &amp; Information Division</div>
                    <div>Aquaculture Department | Southeast Asian Fisheries Development Center (SEAFDEC)</div>
                    <div>Tigbauan, Iloilo 5021 Philippines | Tel: (63-33) 330 7088, (63-33) 330 7000 loc 1340 | Fax: (63-33) 330 7088</div>
                    <div>Follow us on:
                        <a href="http://www.facebook.com/seafdecaqdlib" class="facebook">
                            <i aria-hidden="true">
                                <xsl:attribute name="class">
                                    <xsl:text>fa </xsl:text>
                                    <xsl:text>fa-facebook-square</xsl:text>
                                </xsl:attribute>
                            </i>
                            <xsl:text> Facebook</xsl:text>
                        </a>
                        <xsl:text> | </xsl:text>
                        <a href="http://twitter.com/seafdecaqdlib" class="twitter">
                            <i aria-hidden="true">
                                <xsl:attribute name="class">
                                    <xsl:text>fa </xsl:text>
                                    <xsl:text>fa-twitter-square</xsl:text>
                                </xsl:attribute>
                            </i>
                            <xsl:text> Twitter</xsl:text>
                        </a>
                        <xsl:text> | </xsl:text>
                        <a href="https://plus.google.com/111749266242133800967" class="google-plus">
                            <i aria-hidden="true">
                                <xsl:attribute name="class">
                                    <xsl:text>fa </xsl:text>
                                    <xsl:text>fa-google-plus-square</xsl:text>
                                </xsl:attribute>
                            </i>
                            <xsl:text> Google+</xsl:text>
                        </a>
                        <xsl:text> | </xsl:text>
                        <a href="http://foursquare.com/seafdecaqdlib" class="foursquare">
                            <i aria-hidden="true">
                                <xsl:attribute name="class">
                                    <xsl:text>fa </xsl:text>
                                    <xsl:text>fa-foursquare</xsl:text>
                                </xsl:attribute>
                            </i>
                            <xsl:text> Foursquare</xsl:text>
                        </a>
                        <xsl:text> | </xsl:text>
                        <a href="http://instagram.com/seafdecaqdlib" class="instagram">
                            <i aria-hidden="true">
                                <xsl:attribute name="class">
                                    <xsl:text>fa </xsl:text>
                                    <xsl:text>fa-instagram</xsl:text>
                                </xsl:attribute>
                            </i>
                            <xsl:text> Instagram</xsl:text>
                        </a>
                    </div>
                    <div>Website: <a target="_blank" href="http://www.seafdec.org.ph">www.seafdec.org.ph</a>
                        | Email: <a target="_blank" href="mailto:library@seafdec.org.ph">library@seafdec.org.ph</a>
                    </div>
                    <div class="hidden-print">
                        <a class="contact">
                            <xsl:attribute name="href">
                                <xsl:value-of
                                        select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
                                <xsl:text>/contact</xsl:text>
                            </xsl:attribute>
                            <i18n:text>xmlui.dri2xhtml.structural.contact-link</i18n:text>
                        </a>
                        <xsl:text> | </xsl:text>
                        <a class="feedback">
                            <xsl:attribute name="href">
                                <xsl:value-of
                                        select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
                                <xsl:text>/feedback</xsl:text>
                            </xsl:attribute>
                            <i18n:text>xmlui.dri2xhtml.structural.feedback-link</i18n:text>
                        </a>
                    </div>
                </div>
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
            <p>&#160;</p>
        </footer>
    </xsl:template>

    <xsl:template name="buildFooterSmall">
        <footer>
            <div class="row">
                <hr/>
                <div class="footer-small" align="center">
                    <div>
                        <i aria-hidden="true">
                            <xsl:attribute name="class">
                                <xsl:text>flaticon </xsl:text>
                                <xsl:text>flaticon-world89 fa-lg</xsl:text>
                            </xsl:attribute>
                        </i>
                        <xsl:text> </xsl:text>
                        <a target="_blank" href="http://www.seafdec.org.ph">
                            <xsl:text>www.seafdec.org.ph</xsl:text>
                        </a>
                    </div>
                    <div>
                        <i aria-hidden="true">
                            <xsl:attribute name="class">
                                <xsl:text>flaticon </xsl:text>
                                <xsl:text>flaticon-email118</xsl:text>
                            </xsl:attribute>
                        </i>
                        <xsl:text> </xsl:text>
                        <a target="_blank" href="mailto:library@seafdec.org.ph">
                            <xsl:text>library@seafdec.org.ph</xsl:text>
                        </a>
                    </div>
                    <div>
                        <i aria-hidden="true">
                            <xsl:attribute name="class">
                                <xsl:text>glyphicon </xsl:text>
                                <xsl:text>glyphicon-phone-alt</xsl:text>
                            </xsl:attribute>
                        </i>
                        <xsl:text> (63-33) 330 7088, (63-33) 330 7000 loc 1340</xsl:text>
                        </div>
                    <div>
                        <i aria-hidden="true">
                            <xsl:attribute name="class">
                                <xsl:text>fa </xsl:text>
                                <xsl:text>fa-fax</xsl:text>
                            </xsl:attribute>
                        </i>
                        <xsl:text> (63 33) 330 7088</xsl:text>
                    </div>
                    <div>
                        <ul class="social-links fa-ul">
                            <li class="facebook">
                                <a href="http://www.facebook.com/seafdecaqdlib">
                                    <i aria-hidden="true">
                                        <xsl:attribute name="class">
                                            <xsl:text>fa </xsl:text>
                                            <xsl:text>fa-facebook-square fa-lg</xsl:text>
                                        </xsl:attribute>
                                    </i>
                                </a>
                            </li>
                            <li class="twitter">
                                <a href="http://twitter.com/seafdecaqdlib">
                                    <i aria-hidden="true">
                                        <xsl:attribute name="class">
                                            <xsl:text>fa </xsl:text>
                                            <xsl:text>fa-twitter-square fa-lg</xsl:text>
                                        </xsl:attribute>
                                    </i>
                                </a>
                            </li>
                            <li class="google-plus">
                                <a href="https://plus.google.com/111749266242133800967">
                                    <i aria-hidden="true">
                                        <xsl:attribute name="class">
                                            <xsl:text>fa </xsl:text>
                                            <xsl:text>fa-google-plus-square fa-lg</xsl:text>
                                        </xsl:attribute>
                                    </i>
                                </a>
                            </li>
                            <li class="foursquare">
                                <a href="http://foursquare.com/seafdecaqdlib">
                                    <i aria-hidden="true">
                                        <xsl:attribute name="class">
                                            <xsl:text>fa </xsl:text>
                                            <xsl:text>fa-foursquare fa-lg</xsl:text>
                                        </xsl:attribute>
                                    </i>
                                </a>
                            </li>
                            <li class="instagram">
                                <a href="http://instagram.com/seafdecaqdlib">
                                    <i aria-hidden="true">
                                        <xsl:attribute name="class">
                                            <xsl:text>fa </xsl:text>
                                            <xsl:text>fa-instagram fa-lg</xsl:text>
                                        </xsl:attribute>
                                    </i>
                                </a>
                            </li>
                            <li class="contact">
                                <a>
                                    <xsl:attribute name="href">
                                        <xsl:value-of
                                                select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
                                        <xsl:text>/contact</xsl:text>
                                    </xsl:attribute>
                                    <i aria-hidden="true">
                                        <xsl:attribute name="class">
                                            <xsl:text>fa </xsl:text>
                                            <xsl:text>fa-envelope fa-lg</xsl:text>
                                        </xsl:attribute>
                                    </i>
                                </a>
                            </li>
                            <li class="feedback">
                                <a>
                                    <xsl:attribute name="href">
                                        <xsl:value-of
                                                select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
                                        <xsl:text>/feedback</xsl:text>
                                    </xsl:attribute>
                                    <i aria-hidden="true">
                                        <xsl:attribute name="class">
                                            <xsl:text>fa </xsl:text>
                                            <xsl:text>fa-comments fa-lg</xsl:text>
                                        </xsl:attribute>
                                    </i>
                                </a>
                            </li>
                        </ul>
                    </div>
                </div>
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
            <p>&#160;</p>
        </footer>
    </xsl:template>

    <xsl:template match="dim:dim" mode="itemSummaryView-DIM">
        <div class="item-summary-view-metadata">
            <xsl:call-template name="itemSummaryView-DIM-title"/>
            <div class="row">
                <div class="col-sm-4">
                    <div class="row">
                        <div class="col-xs-6 col-sm-12">
                            <xsl:call-template name="itemSummaryView-DIM-thumbnail"/>
                        </div>
                        <div class="col-xs-6 col-sm-12">
                            <xsl:call-template name="itemSummaryView-DIM-file-section"/>
                        </div>
                    </div>
                    <xsl:call-template name="itemSummaryView-DIM-date"/>
                    <xsl:call-template name="itemSummaryView-DIM-authors"/>
                    <xsl:if test="$ds_item_view_toggle_url != ''">
                        <xsl:call-template name="itemSummaryView-show-full"/>
                    </xsl:if>
                    <xsl:call-template name="itemSummaryView-DIM-share"/>
                </div>
                <div class="col-sm-8">
                    <xsl:call-template name="itemSummaryView-DIM-abstract"/>
                    <xsl:call-template name="itemSummaryView-DIM-description"/>
                    <xsl:call-template name="itemSummaryView-DIM-URI"/>
                    <xsl:call-template name="itemSummaryView-DIM-contents"/>
                    <xsl:choose>
                        <xsl:when test="dim:field[@element='identifier' and @qualifier='citation']">
                            <xsl:call-template name="itemSummaryView-DIM-citation"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:call-template name="itemSummaryView-DIM-ispartof"/>
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:choose>
                        <xsl:when test="dim:field[@element='citation' and @qualifier='journalTitle']">
                            <xsl:call-template name="itemSummaryView-DIM-journal"/>
                        </xsl:when>
                        <xsl:otherwise>
                    <xsl:call-template name="itemSummaryView-DIM-publisher"/>
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:call-template name="itemSummaryView-DIM-subject"/>
                    <div class="row">
                        <xsl:call-template name="itemSummaryView-DIM-type"/>
                        <xsl:call-template name="itemSummaryView-DIM-DOI"/>
                        <xsl:call-template name="itemSummaryView-DIM-ISSN"/>
                        <xsl:call-template name="itemSummaryView-DIM-ISBN"/>
                        <xsl:call-template name="itemSummaryView-DIM-format"/>
                    </div>
                    <xsl:call-template name="itemSummaryView-DIM-sponsorship"/>
                    <xsl:call-template name="itemSummaryView-collections"/>
                </div>
            </div>
        </div>
    </xsl:template>

    <xsl:template name="itemSummaryView-DIM-abstract">
        <xsl:if test="dim:field[@element='description' and @qualifier='abstract']">
            <div class="simple-item-view-description item-page-field-wrapper table">
                <h5 class="visible-xs"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-abstract</i18n:text></h5>
                <div>
                    <xsl:for-each select="dim:field[@element='description' and @qualifier='abstract']">
                        <xsl:choose>
                            <xsl:when test="node()">
                                <xsl:call-template name="break">
                                    <xsl:with-param name="text" select="./node()"/>
                                </xsl:call-template>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text>&#160;</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                        <xsl:if test="count(following-sibling::dim:field[@element='description' and @qualifier='abstract']) != 0">
                            <div class="spacer">&#160;</div>
                        </xsl:if>
                    </xsl:for-each>
                    <xsl:if test="count(dim:field[@element='description' and @qualifier='abstract']) &gt; 1">
                        <div class="spacer">&#160;</div>
                    </xsl:if>
                </div>
            </div>
        </xsl:if>
    </xsl:template>

    <xsl:template name="itemSummaryView-DIM-description">
        <xsl:if test="dim:field[@element='description' and not(@qualifier)]">
            <div class="simple-item-view-description item-page-field-wrapper table">
                <h5><i18n:text>xmlui.dri2xhtml.METS-1.0.item-description</i18n:text></h5>
                <div>
                    <xsl:for-each select="dim:field[@element='description' and not(@qualifier)]">
                        <xsl:choose>
                            <xsl:when test="node()">
                                <xsl:call-template name="break">
                                    <xsl:with-param name="text" select="./node()"/>
                                </xsl:call-template>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text>&#160;</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                        <xsl:if test="count(following-sibling::dim:field[@element='description' and not(@qualifier)]) != 0">
                            <div class="spacer">&#160;</div>
                        </xsl:if>
                    </xsl:for-each>
                    <xsl:if test="count(dim:field[@element='description' and not(@qualifier)]) &gt; 1">
                        <div class="spacer">&#160;</div>
                    </xsl:if>
                </div>
            </div>
        </xsl:if>
    </xsl:template>

    <xsl:template name="itemSummaryView-DIM-contents">
        <xsl:if test="dim:field[@element='description' and @qualifier='tableofcontents']">
            <div class="simple-item-view-description item-page-field-wrapper table">
                <h5><i18n:text>xmlui.dri2xhtml.METS-1.0.item-tableofcontents</i18n:text></h5>
                <div>
                    <xsl:for-each select="dim:field[@element='description' and @qualifier='tableofcontents']">
                        <xsl:choose>
                            <xsl:when test="node()">
                                <xsl:call-template name="break">
                                    <xsl:with-param name="text" select="./node()"/>
                                </xsl:call-template>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text>&#160;</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                        <xsl:if test="count(following-sibling::dim:field[@element='description' and @qualifier='tableofcontents']) != 0">
                            <div class="spacer">&#160;</div>
                        </xsl:if>
                    </xsl:for-each>
                    <xsl:if test="count(dim:field[@element='description' and @qualifier='tableofcontents']) &gt; 1">
                        <div class="spacer">&#160;</div>
                    </xsl:if>
                </div>
            </div>
        </xsl:if>
    </xsl:template>

    <xsl:template name="itemSummaryView-DIM-sponsorship">
        <xsl:if test="dim:field[@element='description' and @qualifier='sponsorship']">
            <div class="simple-item-view-description item-page-field-wrapper table">
                <h5><i18n:text>xmlui.dri2xhtml.METS-1.0.item-sponsorship</i18n:text></h5>
                <div>
                    <xsl:for-each select="dim:field[@element='description' and @qualifier='sponsorship']">
                        <xsl:choose>
                            <xsl:when test="node()">
                                <xsl:call-template name="break">
                                    <xsl:with-param name="text" select="./node()"/>
                                </xsl:call-template>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text>&#160;</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                        <xsl:if test="count(following-sibling::dim:field[@element='description' and @qualifier='sponsorship']) != 0">
                            <div class="spacer">&#160;</div>
                        </xsl:if>
                    </xsl:for-each>
                    <xsl:if test="count(dim:field[@element='description' and @qualifier='sponsorship']) &gt; 1">
                        <div class="spacer">&#160;</div>
                    </xsl:if>
                </div>
            </div>
        </xsl:if>
    </xsl:template>

    <xsl:template name="itemSummaryView-DIM-citation">
        <xsl:if test="dim:field[@element='identifier' and @qualifier='citation']">
            <div class="simple-item-view-description item-page-field-wrapper table">
                <h5><i18n:text>xmlui.dri2xhtml.METS-1.0.item-citation</i18n:text></h5>
                <div>
                    <xsl:for-each select="dim:field[@element='identifier' and @qualifier='citation']">
                        <xsl:choose>
                            <xsl:when test="node()">
                                <xsl:copy-of select="node()"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text>&#160;</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                        <xsl:if test="count(following-sibling::dim:field[@element='identifier' and @qualifier='citation']) != 0">
                            <div class="spacer">&#160;</div>
                        </xsl:if>
                    </xsl:for-each>
                    <xsl:if test="count(dim:field[@element='identifier' and @qualifier='citation']) &gt; 1">
                        <div class="spacer">&#160;</div>
                    </xsl:if>
                </div>
            </div>
        </xsl:if>
    </xsl:template>

    <xsl:template name="itemSummaryView-DIM-ispartof">
        <xsl:if test="dim:field[@element='relation' and @qualifier='ispartof']">
            <div class="simple-item-view-description item-page-field-wrapper table">
                <h5><i18n:text>xmlui.dri2xhtml.METS-1.0.item-ispartof</i18n:text></h5>
                <div>
                    <xsl:for-each select="dim:field[@element='relation' and @qualifier='ispartof']">
                        <xsl:choose>
                            <xsl:when test="node()">
                                <xsl:copy-of select="node()"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text>&#160;</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                        <xsl:if test="count(following-sibling::dim:field[@element='relation' and @qualifier='ispartof']) != 0">
                            <div class="spacer">&#160;</div>
                        </xsl:if>
                    </xsl:for-each>
                    <xsl:if test="count(dim:field[@element='relation' and @qualifier='ispartof']) &gt; 1">
                        <div class="spacer">&#160;</div>
                    </xsl:if>
                </div>
            </div>
        </xsl:if>
    </xsl:template>


    <!-- This is to render line breaks in abstract and description fields in item simple view -->
    <xsl:template name="break">
        <xsl:param name="text" select="."/>
        <xsl:choose>
            <xsl:when test="contains($text, '&#xa;')">
                <xsl:value-of select="substring-before($text, '&#xa;')" disable-output-escaping="yes"/>
                <p/>
                <xsl:call-template name="break">
                    <xsl:with-param name="text" select="substring-after($text, '&#xa;')"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$text" disable-output-escaping="yes"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="text()" mode="literal">
        <xsl:value-of select="." disable-output-escaping="yes" />
    </xsl:template>

    <!-- This is to render html entities in abstract and description fields in item summary list -->
    <xsl:template match="dim:dim" mode="itemSummaryList-DIM-metadata">
        <xsl:param name="href"/>
        <div class="artifact-description">
            <h4 class="artifact-title">
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
                <span class="Z3988">
                    <xsl:attribute name="title">
                        <xsl:call-template name="renderCOinS"/>
                    </xsl:attribute>
                    &#xFEFF; <!-- non-breaking space to force separating the end tag -->
                </span>
            </h4>
            <div class="artifact-info">
                <span class="author h4">
                    <small>
                        <xsl:choose>
                            <xsl:when test="dim:field[@element='contributor'][@qualifier='author']">
                                <xsl:for-each select="dim:field[@element='contributor'][@qualifier='author']">
                                    <xsl:variable name="parseNames">
                                        <xsl:for-each select="str:tokenize(substring-after(., ', '),' -.')">
                                            <xsl:value-of select="substring(., 1, 1)" />
                                        </xsl:for-each>
                                        <xsl:text> </xsl:text>
                                        <xsl:value-of select="substring-before(., ', ')" />
                                    </xsl:variable>
                                    <span>
                                        <a>
                                        <xsl:if test="@authority">
                                            <xsl:attribute name="class"><xsl:text>ds-dc_contributor_author-authority</xsl:text></xsl:attribute>
                                        </xsl:if>
                                            <xsl:attribute name="href">
                                                <xsl:text>/discover?filtertype=author&amp;filter_relational_operator=equals&amp;filter=</xsl:text>
                                        <xsl:copy-of select="node()"/>
                                            </xsl:attribute>
                                            <xsl:choose>
                                                <xsl:when test="not(contains(.,','))">
                                            <xsl:copy-of select="node()"/>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:value-of select="$parseNames"/>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </a>
                                    </span>
                                    <xsl:if test="count(following-sibling::dim:field[@element='contributor'][@qualifier='author']) = 1">
                                        <xsl:text> &amp; </xsl:text>
                                    </xsl:if>
                                    <xsl:if test="count(following-sibling::dim:field[@element='contributor'][@qualifier='author']) &gt; 1">
                                    <xsl:text>, </xsl:text>
                                    </xsl:if>
                                </xsl:for-each>
                            </xsl:when>
                            <xsl:when test="dim:field[@element='contributor'][@qualifier='editor']">
                                <xsl:for-each select="dim:field[@element='contributor'][@qualifier='editor']">
                                    <xsl:variable name="parseNames">
                                        <xsl:for-each select="str:tokenize(substring-after(., ', '),' -.')">
                                            <xsl:value-of select="substring(., 1, 1)" />
                                        </xsl:for-each>
                                        <xsl:text> </xsl:text>
                                        <xsl:value-of select="substring-before(., ', ')" />
                                    </xsl:variable>
                                    <xsl:value-of select="$parseNames"/>
                                    <xsl:if test="count(following-sibling::dim:field[@element='contributor'][@qualifier='editor']) = 1">
                                        <xsl:text> &amp; </xsl:text>
                                    </xsl:if>
                                    <xsl:if test="count(following-sibling::dim:field[@element='contributor'][@qualifier='editor']) &gt; 1">
                                        <xsl:text>, </xsl:text>
                                    </xsl:if>
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
                                <i18n:text>xmlui.dri2xhtml.METS-1.0.no-author</i18n:text>
                            </xsl:otherwise>
                        </xsl:choose>
                    </small>
                </span>
                <xsl:text> </xsl:text>
                <xsl:choose>
                    <xsl:when test="dim:field[@element='citation' and @qualifier='journalTitle']">
                    <span class="publisher-date h4">  <small>
                            <xsl:if test="dim:field[@element='publisher']">
                                <span class="publisher">
                                    <xsl:text> - </xsl:text>
                                    <xsl:text disable-output-escaping="yes">&lt;i&gt;</xsl:text>
                                    <xsl:copy-of select="dim:field[@element='citation' and @qualifier='journalTitle']"/>
                                    <xsl:text disable-output-escaping="yes">&lt;/i&gt;</xsl:text>
                                    <xsl:text>, </xsl:text>
                                    <span class="date">
                                        <xsl:value-of select="substring(dim:field[@element='date' and @qualifier='issued']/node(),1,10)"/>
                                    </span>
                                    <xsl:text> - </xsl:text>
                                    <xsl:copy-of select="dim:field[@element='publisher']/node()"/>
                                </span>
                            </xsl:if>
                        </small></span>
                    </xsl:when>
                    <xsl:when test="dim:field[@element='citation' and @qualifier='bookTitle']">
                        <span class="publisher-date h4">  <small>
                            <xsl:if test="dim:field[@element='publisher']">
                                <span class="publisher">
                                    <xsl:text> - </xsl:text>
                                    <xsl:text disable-output-escaping="yes">&lt;i&gt;</xsl:text>
                                    <xsl:copy-of select="dim:field[@element='citation' and @qualifier='bookTitle']"/>
                                    <xsl:text disable-output-escaping="yes">&lt;/i&gt;</xsl:text>
                                    <xsl:text>, </xsl:text>
                                    <span class="date">
                                        <xsl:value-of select="substring(dim:field[@element='date' and @qualifier='issued']/node(),1,10)"/>
                                    </span>
                                    <xsl:text> - </xsl:text>
                                    <xsl:copy-of select="dim:field[@element='publisher']/node()"/>
                                </span>
                            </xsl:if>
                        </small></span>
                    </xsl:when>
                    <xsl:when test="dim:field[@element='date' and @qualifier='issued']">
                        <span class="publisher-date h4">  <small>
                        <xsl:text> - </xsl:text>
                            <span class="date">
                                <xsl:value-of select="substring(dim:field[@element='date' and @qualifier='issued']/node(),1,10)"/>
                            </span>
                        <xsl:if test="dim:field[@element='publisher']">
                                <xsl:text> - </xsl:text>
                            <span class="publisher">
                                    <xsl:for-each select="dim:field[@element='publisher']">
                                        <xsl:apply-templates select="."/>
                                        <xsl:if test="count(following-sibling::dim:field[@element='publisher']) != 0">
                                            <xsl:text>; </xsl:text>
                                        </xsl:if>
                                    </xsl:for-each>
                            </span>
                        </xsl:if>
                    </small></span>
                    </xsl:when>
                </xsl:choose>
            </div>
            <xsl:choose>
                <xsl:when test="dim:field[@element = 'description' and @qualifier='abstract']">
                    <xsl:variable name="abstract" select="dim:field[@element = 'description' and @qualifier='abstract']/node()"/>
                    <div class="artifact-abstract">
                        <xsl:value-of select="util:shortenString($abstract, 220, 10)" disable-output-escaping="yes"/>
                    </div>
                </xsl:when>
                <xsl:when test="dim:field[@element='description' and not(@qualifier)]">
                    <xsl:variable name="description" select="dim:field[@element='description' and not(@qualifier)]/node()"/>
                    <div class="artifact-abstract">
                        <xsl:value-of select="util:shortenString($description, 220, 10)" disable-output-escaping="yes"/>
                    </div>
                </xsl:when>
            </xsl:choose>
        </div>
    </xsl:template>

    <!-- This is to render html entities in abstract and description fields of search results -->
    <xsl:template name="itemSummaryList">
        <xsl:param name="handle"/>
        <xsl:param name="externalMetadataUrl"/>

        <xsl:variable name="metsDoc" select="document($externalMetadataUrl)"/>

        <div class="row ds-artifact-item ">

            <!--Generates thumbnails (if present)-->
            <div class="col-sm-3 hidden-xs">
                <xsl:apply-templates select="$metsDoc/mets:METS/mets:fileSec" mode="artifact-preview">
                    <xsl:with-param name="href" select="concat($context-path, '/handle/', $handle)"/>
                </xsl:apply-templates>
            </div>


            <div class="col-sm-9 artifact-description">
                <xsl:element name="a">
                    <xsl:attribute name="href">
                        <xsl:choose>
                            <xsl:when test="$metsDoc/mets:METS/mets:dmdSec/mets:mdWrap/mets:xmlData/dim:dim/@withdrawn">
                                <xsl:value-of select="$metsDoc/mets:METS/@OBJEDIT"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="concat($context-path, '/handle/', $handle)"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:attribute>
                    <h4>
                        <xsl:choose>
                            <xsl:when test="dri:list[@n=(concat($handle, ':dc.title'))]">
                                <xsl:apply-templates select="dri:list[@n=(concat($handle, ':dc.title'))]/dri:item"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <i18n:text>xmlui.dri2xhtml.METS-1.0.no-title</i18n:text>
                            </xsl:otherwise>
                        </xsl:choose>
                        <!-- Generate COinS with empty content per spec but force Cocoon to not create a minified tag  -->
                        <span class="Z3988">
                            <xsl:attribute name="title">
                                <xsl:for-each select="$metsDoc/mets:METS/mets:dmdSec/mets:mdWrap/mets:xmlData/dim:dim">
                                    <xsl:call-template name="renderCOinS"/>
                                </xsl:for-each>
                            </xsl:attribute>
                            <xsl:text>&#160;</xsl:text>
                            <!-- non-breaking space to force separating the end tag -->
                        </span>
                    </h4>
                </xsl:element>
                <div class="artifact-info">
                    <span class="author h4">    <small>
                        <xsl:choose>
                            <xsl:when test="dri:list[@n=(concat($handle, ':dc.contributor.author'))]">
                                <xsl:for-each select="dri:list[@n=(concat($handle, ':dc.contributor.author'))]/dri:item">
                                    <xsl:variable name="parseNames">
                                        <xsl:for-each select="str:tokenize(substring-after(., ', '),' -.')">
                                            <xsl:value-of select="substring(., 1, 1)" />
                                        </xsl:for-each>
                                        <xsl:text> </xsl:text>
                                        <xsl:value-of select="substring-before(., ', ')" />
                                    </xsl:variable>
                                    <span>
                                        <a>
                                            <xsl:if test="@authority">
                                                <xsl:attribute name="class"><xsl:text>ds-dc_contributor_author-authority</xsl:text></xsl:attribute>
                                            </xsl:if>
                                                <xsl:attribute name="href">
                                                    <xsl:text>/discover?filtertype=author&amp;filter_relational_operator=equals&amp;filter=</xsl:text>
                                                    <xsl:copy-of select="node()"/>
                                                </xsl:attribute>
                                            <xsl:choose>
                                                <xsl:when test="not(contains(.,','))">
                                                    <xsl:copy-of select="node()"/>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:value-of select="$parseNames"/>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </a>
                                    </span>
                                    <xsl:if test="count(following-sibling::dri:item) = 1">
                                        <xsl:text> &amp; </xsl:text>
                                    </xsl:if>
                                    <xsl:if test="count(following-sibling::dri:item) &gt; 1">
                                        <xsl:text>, </xsl:text>
                                    </xsl:if>
                                </xsl:for-each>
                            </xsl:when>
                            <xsl:when test="dri:list[@n=(concat($handle, ':dc.contributor.editor'))]">
                                <xsl:for-each select="dri:list[@n=(concat($handle, ':dc.contributor.editor'))]/dri:item">
                                    <xsl:variable name="parseNames">
                                        <xsl:for-each select="str:tokenize(substring-after(., ', '),' -.')">
                                            <xsl:value-of select="substring(., 1, 1)" />
                                        </xsl:for-each>
                                        <xsl:text> </xsl:text>
                                        <xsl:value-of select="substring-before(., ', ')" />
                                    </xsl:variable>
                                    <xsl:value-of select="$parseNames"/>
                                    <xsl:if test="count(following-sibling::dri:item) = 1">
                                        <xsl:text> &amp; </xsl:text>
                                    </xsl:if>
                                    <xsl:if test="count(following-sibling::dri:item) &gt; 1">
                                        <xsl:text>, </xsl:text>
                                    </xsl:if>
                                </xsl:for-each>
                            </xsl:when>
                            <xsl:when test="dri:list[@n=(concat($handle, ':dc.creator'))]">
                                <xsl:for-each select="dri:list[@n=(concat($handle, ':dc.creator'))]/dri:item">
                                    <xsl:apply-templates select="."/>
                                    <xsl:if test="count(following-sibling::dri:item) != 0">
                                        <xsl:text>; </xsl:text>
                                    </xsl:if>
                                </xsl:for-each>
                            </xsl:when>
                            <xsl:when test="dri:list[@n=(concat($handle, ':dc.contributor'))]">
                                <xsl:for-each select="dri:list[@n=(concat($handle, ':dc.contributor'))]/dri:item">
                                    <xsl:apply-templates select="."/>
                                    <xsl:if test="count(following-sibling::dri:item) != 0">
                                        <xsl:text>; </xsl:text>
                                    </xsl:if>
                                </xsl:for-each>
                            </xsl:when>
                            <xsl:otherwise>
                                <i18n:text>xmlui.dri2xhtml.METS-1.0.no-author</i18n:text>
                            </xsl:otherwise>
                        </xsl:choose>
                    </small></span>
                    <xsl:text> </xsl:text>
                    <xsl:choose>
                        <xsl:when test="dri:list[@n=(concat($handle, ':dc.citation.journalTitle'))]">
                        <span class="publisher-date h4">   <small>
                                <xsl:if test="dri:list[@n=(concat($handle, ':dc.publisher'))]">
                                    <span class="publisher">
                                        <xsl:text> - </xsl:text>
                                        <xsl:text disable-output-escaping="yes">&lt;i&gt;</xsl:text>
                                        <xsl:apply-templates select="dri:list[@n=(concat($handle, ':dc.citation.journalTitle'))]/dri:item"/>
                                        <xsl:text disable-output-escaping="yes">&lt;/i&gt;</xsl:text>
                                        <xsl:text>, </xsl:text>
                                        <span class="date">
                                            <xsl:value-of select="substring(dri:list[@n=(concat($handle, ':dc.date.issued'))]/dri:item,1,10)"/>
                                        </span>
                                        <xsl:text> - </xsl:text>
                                        <xsl:apply-templates select="dri:list[@n=(concat($handle, ':dc.publisher'))]/dri:item"/>
                                    </span>
                                </xsl:if>
                            </small></span>
                        </xsl:when>
                        <xsl:when test="dri:list[@n=(concat($handle, ':dc.citation.bookTitle'))]">
                            <span class="publisher-date h4">   <small>
                                <xsl:if test="dri:list[@n=(concat($handle, ':dc.publisher'))]">
                                    <span class="publisher">
                                        <xsl:text> - </xsl:text>
                                        <xsl:text disable-output-escaping="yes">&lt;i&gt;</xsl:text>
                                        <xsl:apply-templates select="dri:list[@n=(concat($handle, ':dc.citation.bookTitle'))]/dri:item"/>
                                        <xsl:text disable-output-escaping="yes">&lt;/i&gt;</xsl:text>
                                        <xsl:text>, </xsl:text>
                                        <span class="date">
                                            <xsl:value-of select="substring(dri:list[@n=(concat($handle, ':dc.date.issued'))]/dri:item,1,10)"/>
                                        </span>
                                        <xsl:text> - </xsl:text>
                                        <xsl:apply-templates select="dri:list[@n=(concat($handle, ':dc.publisher'))]/dri:item"/>
                                    </span>
                                </xsl:if>
                            </small></span>
                        </xsl:when>
                        <xsl:when test="dri:list[@n=(concat($handle, ':dc.date.issued'))]">
                            <span class="publisher-date h4">
                                <small>
                                    <xsl:text> - </xsl:text>
                                    <span class="date">
                                        <xsl:value-of
                                                select="substring(dri:list[@n=(concat($handle, ':dc.date.issued'))]/dri:item,1,10)"/>
                                    </span>
                                    <xsl:if test="dri:list[@n=(concat($handle, ':dc.publisher'))]">
                                        <span class="publisher">
                                            <xsl:text> - </xsl:text>
                                            <xsl:for-each
                                                    select="dri:list[@n=(concat($handle, ':dc.publisher'))]/dri:item">
                                                <xsl:apply-templates select="."/>
                                                <xsl:if test="count(following-sibling::dri:item) != 0">
                                                    <xsl:text>; </xsl:text>
                                                </xsl:if>
                                            </xsl:for-each>
                                        </span>
                                    </xsl:if>
                                </small>
                            </span>
                        </xsl:when>
                    </xsl:choose>
                    <xsl:choose>
                        <xsl:when test="dri:list[@n=(concat($handle, ':dc.description.abstract'))]/dri:item/dri:hi">
                            <div class="abstract">
                                <xsl:for-each select="dri:list[@n=(concat($handle, ':dc.description.abstract'))]/dri:item">
                                    <xsl:apply-templates select="." mode="literal"/>
                                    <xsl:text>...</xsl:text>
                                    <br/>
                                </xsl:for-each>

                            </div>
                        </xsl:when>
                        <xsl:when test="dri:list[@n=(concat($handle, ':fulltext'))]">
                            <div class="abstract">
                                <xsl:for-each select="dri:list[@n=(concat($handle, ':fulltext'))]/dri:item">
                                    <xsl:apply-templates select="."/>
                                    <xsl:text>...</xsl:text>
                                    <br/>
                                </xsl:for-each>
                            </div>
                        </xsl:when>
                        <xsl:when test="dri:list[@n=(concat($handle, ':dc.description.abstract'))]/dri:item">
                            <div class="abstract">
                                <xsl:value-of select="util:shortenString(dri:list[@n=(concat($handle, ':dc.description.abstract'))]/dri:item[1], 220, 10)" disable-output-escaping="yes"/>
                            </div>
                        </xsl:when>
                    </xsl:choose>
                </div>
            </div>
        </div>
    </xsl:template>

    <xsl:template name="itemSummaryView-DIM-DOI">
        <xsl:if test="dim:field[@element='identifier' and @qualifier='doi' and descendant::text()]">
            <div class="col-sm-6 col-print-4">
                <div class="simple-item-view-uri item-page-field-wrapper table word-break">
                    <h5>
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.item-doi</i18n:text>
                    </h5>
                <span>
                    <xsl:for-each select="dim:field[@element='identifier' and @qualifier='doi']">
                        <a>
                            <xsl:attribute name="href">
                                <xsl:text>http://dx.doi.org/</xsl:text>
                                <xsl:copy-of select="./node()"/>
                            </xsl:attribute>
                            <xsl:copy-of select="./node()"/>
                        </a>
                        <xsl:if test="count(following-sibling::dim:field[@element='identifier' and @qualifier='doi']) != 0">
                            <br/>
                        </xsl:if>
                    </xsl:for-each>
                </span>
            </div>
            </div>
        </xsl:if>
    </xsl:template>

    <xsl:template name="itemSummaryView-DIM-ISSN">
        <xsl:if test="dim:field[@element='identifier' and @qualifier='issn' and descendant::text()] or
         dim:field[@element='identifier' and @qualifier='essn' and descendant::text()]">
            <div class="col-sm-6 col-print-4">
            <div class="simple-item-view-issn item-page-field-wrapper table">
                <h5><i18n:text>xmlui.dri2xhtml.METS-1.0.item-issn</i18n:text></h5>
                <span>
                    <xsl:for-each select="dim:field[@element='identifier' and (@qualifier='issn' or @qualifier='essn') and descendant::text()]">
                        <xsl:copy-of select="./node()"/>
                        <xsl:if test="count(following-sibling::dim:field[@element='identifier' and (@qualifier='issn' or @qualifier='essn')]) != 0">
                            <xsl:text>; </xsl:text>
                        </xsl:if>
                    </xsl:for-each>
                </span>
            </div>
            </div>
        </xsl:if>
    </xsl:template>

    <xsl:template name="itemSummaryView-DIM-ISBN">
        <xsl:if test="dim:field[@element='identifier' and @qualifier='isbn' and descendant::text()]">
            <div class="col-sm-6 col-print-4">
            <div class="simple-item-view-isbn item-page-field-wrapper table">
                <h5><i18n:text>xmlui.dri2xhtml.METS-1.0.item-isbn</i18n:text></h5>
                <span>
                    <xsl:for-each select="dim:field[@element='identifier' and @qualifier='isbn']">
                        <xsl:copy-of select="./node()"/>
                        <xsl:if test="count(following-sibling::dim:field[@element='identifier' and @qualifier='isbn']) != 0">
                            <br/>
                        </xsl:if>
                    </xsl:for-each>
                </span>
            </div>
            </div>
        </xsl:if>
    </xsl:template>

    <xsl:template name="itemSummaryView-DIM-format">
        <xsl:if test="dim:field[@element='format' and @qualifier='extent' and descendant::text()]">
            <div class="col-sm-6 col-print-4">
                <div class="simple-item-view-format item-page-field-wrapper table">
                    <h5><i18n:text>xmlui.dri2xhtml.METS-1.0.item-format</i18n:text></h5>
                    <span>
                        <xsl:for-each select="dim:field[@element='format' and @qualifier='extent']">
                            <xsl:copy-of select="./node()"/>
                            <xsl:if test="count(following-sibling::dim:field[@element='format' and @qualifier='extent']) != 0">
                                <br/>
                            </xsl:if>
                        </xsl:for-each>
                    </span>
                </div>
            </div>
        </xsl:if>
    </xsl:template>

    <xsl:template name="itemSummaryView-DIM-subject">
        <xsl:if test="dim:field[@element='subject'][@qualifier='asfa'] or dim:field[@element='subject' and not(@qualifier)]">
            <div class="item-page-field-wrapper table">
                <h5><i18n:text>xmlui.dri2xhtml.METS-1.0.item-subject</i18n:text></h5>
                <div>
                    <xsl:for-each select="dim:field[@element='subject'][@qualifier='asfa']">
                        <xsl:choose>
                            <xsl:when test="node()">
                                <a>
                                    <xsl:attribute name="href">
                                        <xsl:value-of
                                                select="concat($context-path,'/discover?filtertype=')"/>
                                        <xsl:text>subject&amp;filter_relational_operator=equals&amp;filter=</xsl:text>
                                        <xsl:copy-of select="node()"/>
                                    </xsl:attribute>
                                    <xsl:value-of select="text()"/>
                                </a>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text>&#160;</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                        <xsl:if test="count(following-sibling::dim:field[@element='subject'][@qualifier='asfa']) != 0">
                            <xsl:text>; </xsl:text>
                        </xsl:if>
                    </xsl:for-each>
                    <xsl:if test="(dim:field[@element='subject' and not(@qualifier)])">
                        <xsl:if test="count(dim:field[@element='subject'][@qualifier='asfa']) != 0">
                            <xsl:text>; </xsl:text>
                        </xsl:if>
                        <xsl:for-each select="dim:field[@element='subject' and not(@qualifier)]">
                            <a>
                                <xsl:attribute name="href">
                                    <xsl:value-of
                                            select="concat($context-path,'/discover?filtertype=')"/>
                                    <xsl:text>subject&amp;filter_relational_operator=equals&amp;filter=</xsl:text>
                                    <xsl:copy-of select="."/>
                                </xsl:attribute>
                                <xsl:value-of select="text()"/>
                            </a>
                            <xsl:if test="count(following-sibling::dim:field[@element='subject' and not(@qualifier)]) != 0">
                                <xsl:text>; </xsl:text>
                            </xsl:if>
                        </xsl:for-each>
                    </xsl:if>
                </div>
            </div>
        </xsl:if>
    </xsl:template>

    <xsl:template name="itemSummaryView-DIM-authors-entry">
        <div>
            <xsl:if test="@authority">
                <xsl:attribute name="class"><xsl:text>ds-dc_contributor_author-authority</xsl:text></xsl:attribute>
            </xsl:if>
            <a>
                <xsl:attribute name="href">
                    <xsl:text>/discover?filtertype=author&amp;filter_relational_operator=equals&amp;filter=</xsl:text>
                    <xsl:copy-of select="node()"/>
                </xsl:attribute>
                <xsl:copy-of select="node()"/>
            </a>
        </div>
    </xsl:template>

    <xsl:template name="itemSummaryView-DIM-file-section">
        <xsl:choose>
            <xsl:when test="dim:field[@element='relation'][@qualifier='uri']">
                <xsl:call-template name="itemSummaryView-DIM-uri"/>
            </xsl:when>
            <xsl:when test="//mets:fileSec/mets:fileGrp[@USE='CONTENT' or @USE='ORIGINAL' or @USE='LICENSE']/mets:file">
                <div class="item-page-field-wrapper table">
                    <h5>
                        <xsl:text>Download </xsl:text>
                        <i aria-hidden="true">
                            <xsl:attribute name="class">
                                <xsl:text>fa fa-download</xsl:text>
                            </xsl:attribute>
                        </i>
                    </h5>

                    <xsl:variable name="label-1">
                        <xsl:choose>
                            <xsl:when test="confman:getProperty('mirage2.item-view.bitstream.href.label.1')">
                                <xsl:value-of select="confman:getProperty('mirage2.item-view.bitstream.href.label.1')"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text>label</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>

                    <xsl:variable name="label-2">
                        <xsl:choose>
                            <xsl:when test="confman:getProperty('mirage2.item-view.bitstream.href.label.2')">
                                <xsl:value-of select="confman:getProperty('mirage2.item-view.bitstream.href.label.2')"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text>title</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>

                    <xsl:for-each select="//mets:fileSec/mets:fileGrp[@USE='CONTENT' or @USE='ORIGINAL' or @USE='LICENSE']/mets:file">
                        <xsl:call-template name="itemSummaryView-DIM-file-section-entry">
                            <xsl:with-param name="href" select="mets:FLocat[@LOCTYPE='URL']/@xlink:href" />
                            <xsl:with-param name="mimetype" select="@MIMETYPE" />
                            <xsl:with-param name="label-1" select="$label-1" />
                            <xsl:with-param name="label-2" select="$label-2" />
                            <xsl:with-param name="title" select="mets:FLocat[@LOCTYPE='URL']/@xlink:title" />
                            <xsl:with-param name="label" select="mets:FLocat[@LOCTYPE='URL']/@xlink:label" />
                            <xsl:with-param name="size" select="@SIZE" />
                        </xsl:call-template>
                    </xsl:for-each>
                </div>
            </xsl:when>
            <!-- Special case for handling ORE resource maps stored as DSpace bitstreams -->
            <xsl:when test="//mets:fileSec/mets:fileGrp[@USE='ORE']">
                <xsl:apply-templates select="//mets:fileSec/mets:fileGrp[@USE='ORE']" mode="itemSummaryView-DIM" />
            </xsl:when>
            <xsl:otherwise>
                <div class="item-page-field-wrapper table">
                    <h5>Request copy</h5>
                    <div>
                        <xsl:call-template name="documentdelivery" />
                    </div>
                </div>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="itemSummaryView-DIM-file-section-entry">
        <xsl:param name="href" />
        <xsl:param name="mimetype" />
        <xsl:param name="label-1" />
        <xsl:param name="label-2" />
        <xsl:param name="title" />
        <xsl:param name="label" />
        <xsl:param name="size" />
        <div>
            <a>
                <xsl:attribute name="target">
                    <xsl:text>_blank</xsl:text>
                </xsl:attribute>
                <xsl:attribute name="href">
                    <xsl:value-of select="$href"/>
                </xsl:attribute>
                <xsl:call-template name="getFileIcon">
                    <xsl:with-param name="mimetype">
                        <xsl:value-of select="substring-before($mimetype,'/')"/>
                        <xsl:text>/</xsl:text>
                        <xsl:value-of select="substring-after($mimetype,'/')"/>
                    </xsl:with-param>
                </xsl:call-template>
                <xsl:choose>
                    <xsl:when test="contains($label-1, 'label') and string-length($label)!=0">
                        <xsl:value-of select="$label"/>
                    </xsl:when>
                    <xsl:when test="contains($label-1, 'title') and string-length($title)!=0">
                        <xsl:value-of select="$title"/>
                    </xsl:when>
                    <xsl:when test="contains($label-2, 'label') and string-length($label)!=0">
                        <xsl:value-of select="$label"/>
                    </xsl:when>
                    <xsl:when test="contains($label-2, 'title') and string-length($title)!=0">
                        <xsl:value-of select="$title"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="getFileTypeDesc">
                            <xsl:with-param name="mimetype">
                                <xsl:value-of select="substring-before($mimetype,'/')"/>
                                <xsl:text>/</xsl:text>
                                <xsl:choose>
                                    <xsl:when test="contains($mimetype,';')">
                                        <xsl:value-of select="substring-before(substring-after($mimetype,'/'),';')"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="substring-after($mimetype,'/')"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:text> (</xsl:text>
                <xsl:choose>
                    <xsl:when test="$size &lt; 1024">
                        <xsl:value-of select="$size"/>
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.size-bytes</i18n:text>
                    </xsl:when>
                    <xsl:when test="$size &lt; 1024 * 1024">
                        <xsl:value-of select="substring(string($size div 1024),1,5)"/>
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.size-kilobytes</i18n:text>
                    </xsl:when>
                    <xsl:when test="$size &lt; 1024 * 1024 * 1024">
                        <xsl:value-of select="substring(string($size div (1024 * 1024)),1,5)"/>
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.size-megabytes</i18n:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="substring(string($size div (1024 * 1024 * 1024)),1,5)"/>
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.size-gigabytes</i18n:text>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:text>)</xsl:text>
            </a>
        </div>
    </xsl:template>

    <xsl:template name="view-open">
        <a>
            <xsl:attribute name="target">
                <xsl:text>_blank</xsl:text>
            </xsl:attribute>
            <xsl:attribute name="href">
                <xsl:value-of select="mets:FLocat[@LOCTYPE='URL']/@xlink:href"/>
            </xsl:attribute>
            <i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-viewOpen</i18n:text>
        </a>
    </xsl:template>

    <xsl:template name="documentdelivery">
        <a>
            <xsl:attribute name="href">
                <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
                <xsl:value-of select="substring-before(/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request'][@qualifier='URI'],'handle/')"/>
                <xsl:text>/documentdelivery/</xsl:text>
                <xsl:value-of select="substring-after($request-uri,'handle/')"/>
            </xsl:attribute>
            <i aria-hidden="true">
                <xsl:attribute name="class">
                    <xsl:text>fa </xsl:text>
                    <xsl:text>fa-envelope-o</xsl:text>
                </xsl:attribute>
            </i>
            <xsl:text> </xsl:text>
            <xsl:text>Request this document</xsl:text>
        </a>
    </xsl:template>

    <xsl:template name="itemSummaryView-DIM-uri">
        <xsl:if test="dim:field[@element='relation'][@qualifier='uri']">
            <div class="item-page-field-wrapper table">
                <h5>
                    <i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-viewOpen</i18n:text>
                </h5>
                <xsl:for-each select="dim:field[@element='relation' and @qualifier='uri']">
                    <a>
                        <xsl:attribute name="data-toggle">
                            <xsl:text>tooltip</xsl:text>
                        </xsl:attribute>
                        <xsl:attribute name="href">
                            <xsl:copy-of select="./node()"/>
                        </xsl:attribute>
                        <xsl:attribute name="class">
                            <xsl:text>word-break</xsl:text>
                        </xsl:attribute>
                        <xsl:attribute name="id">
                            <xsl:text>external-link</xsl:text>
                        </xsl:attribute>
                        <xsl:choose>
                            <xsl:when test="contains(node(),'pdf')">
                                <img class="type" src="{$theme-path}/images/pdf.png"/>
                            </xsl:when>
                            <xsl:when test="contains(node(),'htm')">
                                <img class="type" src="{$theme-path}/images/page.png"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <i aria-hidden="true">
                                    <xsl:attribute name="class">
                                        <xsl:text>fa </xsl:text>
                                        <xsl:text>fa-link</xsl:text>
                                        <xsl:text> type</xsl:text>
                                    </xsl:attribute>
                                </i>
                            </xsl:otherwise>
                        </xsl:choose>
                        <xsl:text> from </xsl:text>
                        <xsl:variable name="url_ini">
                                <xsl:copy-of select="./node()"/>
                        </xsl:variable>
                        <xsl:variable name="url_minus_http">
                        <xsl:choose>
                                <xsl:when test="contains($url_ini,'www')">
                                    <xsl:value-of select="substring-after($url_ini,'www.')"/>
                            </xsl:when>
                                <xsl:when test="contains($url_ini,'ftp')">
                                    <xsl:value-of select="substring-after($url_ini,'ftp.')"/>
                                </xsl:when>
                            <xsl:otherwise>
                                    <xsl:value-of select="substring-after($url_ini,'://')"/>
                            </xsl:otherwise>
                        </xsl:choose>
                        </xsl:variable>
                        <xsl:value-of select="substring-before($url_minus_http,'/')"/>
                    <xsl:text> </xsl:text>
                    <i aria-hidden="true">
                        <xsl:attribute name="class">
                            <xsl:text>fa </xsl:text>
                            <xsl:text>fa-external-link</xsl:text>
                        </xsl:attribute>
                    </i>
                    </a>
                    <xsl:if test="count(following-sibling::dim:field[@element='relation' and @qualifier='uri']) != 0">
                        <br/>
                    </xsl:if>
                    <div class="modal fade" id="externalLinkModal">
                        <div class="modal-dialog">
                            <div class="modal-content">
                                <div class="modal-header">
                                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                                        <span aria-hidden="true">
                                            <xsl:text disable-output-escaping="yes">&amp;times;</xsl:text>
                                        </span>
                                    </button>
                                    <h4 class="modal-title">EXTERNAL LINKS DISCLAIMER</h4>
                                </div>
                                <div class="modal-body justify">
                                    <p>This link is being provided as a convenience and for informational purposes only.
                                        SEAFDEC/AQD bears no responsibility for the accuracy, legality or content of the
                                        external site or for that of subsequent links. Contact the external site for
                                        answers to questions regarding its content.
                                    </p>
                                    <p>If you come across any external links that don't work, we would be grateful if
                                        you could report them to the <a href="/contact">repository administrators</a>.
                                    </p>
                                </div>
                                <div class="modal-footer">
                                    <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                                </div>
                            </div><!-- /.modal-content -->
                        </div><!-- /.modal-dialog -->
                    </div><!-- /.modal -->
                </xsl:for-each>
            </div>
        </xsl:if>
    </xsl:template>

    <xsl:template name="itemSummaryView-DIM-publisher">
        <xsl:if test="dim:field[@element='publisher' and not(@qualifier)]">
                <div class="simple-item-view-uri item-page-field-wrapper table">
                    <h5>
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.item-publisher</i18n:text>
                    </h5>
                    <span>
                        <xsl:for-each select="dim:field[@element='publisher' and not(@qualifier)]">
                            <xsl:copy-of select="./node()"/>
                            <xsl:if test="count(following-sibling::dim:field[@element='publisher' and not(@qualifier)]) != 0">
                                <xsl:text>; </xsl:text>
                            </xsl:if>
                        </xsl:for-each>
                    </span>
                </div>
        </xsl:if>
    </xsl:template>

    <xsl:template name="itemSummaryView-DIM-journal">
        <xsl:if test="dim:field[@element='citation' and @qualifier='journalTitle']">
            <div class="simple-item-view-uri item-page-field-wrapper table">
                <h5>
                    <i18n:text>xmlui.dri2xhtml.METS-1.0.item-journalTitle</i18n:text>
                </h5>
                <span>
                    <xsl:text disable-output-escaping="yes">&lt;i&gt;</xsl:text>
                    <xsl:copy-of select="dim:field[@element='citation' and @qualifier='journalTitle']"/>
                    <xsl:text disable-output-escaping="yes">&lt;/i&gt;</xsl:text>
                    <xsl:text>, </xsl:text>
                    <xsl:if test="dim:field[@element='citation' and @qualifier='volume']">
                        <xsl:text disable-output-escaping="yes">&lt;i&gt;</xsl:text>
                        <xsl:value-of select="dim:field[@element='citation' and @qualifier='volume']"/>
                    <xsl:text disable-output-escaping="yes">&lt;/i&gt;</xsl:text>
                    </xsl:if>
                    <xsl:choose>
                        <xsl:when test="dim:field[@element='citation' and @qualifier='issue']">
                        <xsl:text>(</xsl:text>
                        <xsl:value-of select="dim:field[@element='citation' and @qualifier='issue']"/>
                        <xsl:text>), </xsl:text>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:text>, </xsl:text>
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:if test="dim:field[@element='citation' and @qualifier='spage']">
                        <xsl:value-of select="dim:field[@element='citation' and @qualifier='spage']"/>
                    </xsl:if>
                    <xsl:if test="dim:field[@element='citation' and @qualifier='epage']">
                        <xsl:text>-</xsl:text>
                        <xsl:value-of select="dim:field[@element='citation' and @qualifier='epage']"/>
                    </xsl:if>
                    <xsl:if test="count(following-sibling::dim:field[@element='citation' and @qualifier='journalTitle']) != 0">
                        <xsl:text>; </xsl:text>
                    </xsl:if>
                    </span>
                </div>
        </xsl:if>
    </xsl:template>

    <xsl:template name="itemSummaryView-DIM-type">
        <xsl:if test="dim:field[@element='type' and not(@qualifier)]">
            <div class="col-sm-6 col-print-4">
                <div class="simple-item-view-issn item-page-field-wrapper table">
                    <h5>
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.item-type</i18n:text>
                    </h5>
                    <span>
                        <xsl:for-each select="dim:field[@element='type' and not(@qualifier)]">
                            <xsl:copy-of select="./node()"/>
                            <xsl:if test="count(following-sibling::dim:field[@element='type' and not(@qualifier)]) != 0">
                                <xsl:text>; </xsl:text>
                            </xsl:if>
                        </xsl:for-each>
                    </span>
                </div>
            </div>
        </xsl:if>
    </xsl:template>

    <xsl:template name="itemSummaryView-DIM-share">
        <div class="item-page-field-wrapper table">
            <h5>
                <xsl:text>Share </xsl:text>
                <i aria-hidden="true">
                    <xsl:attribute name="class">
                        <xsl:text>fa fa-share-alt</xsl:text>
                    </xsl:attribute>
                </i>
            </h5>
            <ul class="social-links fa-ul">
                <li class="facebook">
                    <a>
                        <xsl:attribute name="href">
                            <xsl:text>http://www.facebook.com/sharer.php?u=</xsl:text>
                            <xsl:value-of select="$current-uri"/>
                            <xsl:text>&amp;title=</xsl:text>
                            <xsl:value-of select="dim:field[@element='title'][1]/node()"/>
                        </xsl:attribute>
                        <xsl:attribute name="title">
                            <xsl:text>Facebook</xsl:text>
                        </xsl:attribute>
                        <i aria-hidden="true">
                            <xsl:attribute name="class">
                                <xsl:text>fa fa-facebook-square fa-lg</xsl:text>
                            </xsl:attribute>
                        </i>
                        <xsl:text> </xsl:text>
                    </a>
                </li>
                <li class="twitter">
                    <a>
                        <xsl:attribute name="href">
                            <xsl:text>http://twitter.com/share?text=</xsl:text>
                            <xsl:value-of select="util:shortenString(concat(dim:field[@element='title'][1]/node(),'&amp;url=',$current-uri), 90, 50)"/>
                        </xsl:attribute>
                        <xsl:attribute name="title">
                            <xsl:text>Tweet</xsl:text>
                        </xsl:attribute>
                        <i aria-hidden="true">
                            <xsl:attribute name="class">
                                <xsl:text>fa fa-twitter-square fa-lg</xsl:text>
                            </xsl:attribute>
                        </i>
                        <xsl:text> </xsl:text>
                    </a>
                </li>
                <li class="google-plus">
                    <a>
                        <xsl:attribute name="href">
                            <xsl:text>https://plus.google.com/share?url=</xsl:text>
                            <xsl:value-of select="$current-uri"/>
                        </xsl:attribute>
                        <xsl:attribute name="title">
                            <xsl:text>Google+</xsl:text>
                        </xsl:attribute>
                        <i aria-hidden="true">
                            <xsl:attribute name="class">
                                <xsl:text>fa fa-google-plus-square fa-lg</xsl:text>
                            </xsl:attribute>
                        </i>
                        <xsl:text> </xsl:text>
                    </a>
                </li>
                <li class="linkedin">
                    <a>
                        <xsl:attribute name="href">
                            <xsl:text>http://www.linkedin.com/shareArticle?mini=true&amp;url=</xsl:text>
                            <xsl:value-of select="$current-uri"/>
                            <xsl:text disable-output-escaping="yes">&amp;title=</xsl:text>
                            <xsl:value-of select="dim:field[@element='title'][1]/node()"/>
                        </xsl:attribute>
                        <xsl:attribute name="title">
                            <xsl:text>LinkedIn</xsl:text>
                        </xsl:attribute>
                        <i aria-hidden="true">
                            <xsl:attribute name="class">
                                <xsl:text>fa fa-linkedin-square fa-lg</xsl:text>
                            </xsl:attribute>
                        </i>
                        <xsl:text> </xsl:text>
                    </a>
                </li>
                <li class="mendeley">
                    <a>
                        <xsl:attribute name="href">
                            <xsl:text>http://www.mendeley.com/import/?url=</xsl:text>
                            <xsl:value-of select="$current-uri"/>
                        </xsl:attribute>
                        <xsl:attribute name="title">
                            <xsl:text>Import to Mendeley</xsl:text>
                        </xsl:attribute>
                        <i aria-hidden="true">
                            <xsl:attribute name="class">
                                <xsl:text>ai ai-mendeley-square fa-lg</xsl:text>
                            </xsl:attribute>
                        </i>
                        <xsl:text> </xsl:text>
                    </a>
                </li>
                <li class="researchgate">
                    <a>
                        <xsl:attribute name="href">
                            <xsl:text>https://www.researchgate.net/go.Share.html?url=</xsl:text>
                            <xsl:value-of select="$current-uri"/>
                            <xsl:text>&amp;title=</xsl:text>
                            <xsl:value-of select="dim:field[@element='title'][1]/node()"/>
                        </xsl:attribute>
                        <xsl:attribute name="title">
                            <xsl:text>ResearchGate</xsl:text>
                        </xsl:attribute>
                        <i aria-hidden="true">
                            <xsl:attribute name="class">
                                <xsl:text>ai ai-researchgate-square fa-lg</xsl:text>
                            </xsl:attribute>
                        </i>
                        <xsl:text> </xsl:text>
                    </a>
                </li>
                <li class="zotero">
                    <a>
                        <xsl:attribute name="href">
                            <xsl:text>javascript:var%20d=document,s=d.createElement('script');s.src='https://www.zotero.org/bookmarklet/loader.js';(d.body?d.body:d.documentElement).appendChild(s);void(0);</xsl:text>
                        </xsl:attribute>
                        <xsl:attribute name="title">
                            <xsl:text>Import to Zotero</xsl:text>
                        </xsl:attribute>
                        <i aria-hidden="true">
                            <xsl:attribute name="class">
                                <xsl:text>ai ai-zotero-square fa-lg</xsl:text>
                            </xsl:attribute>
                        </i>
                        <xsl:text> </xsl:text>
                    </a>
                </li>
                <li class="citeulike">
                    <a>
                        <xsl:attribute name="href">
                            <xsl:text>http://www.citeulike.org/posturl2?url=</xsl:text>
                            <xsl:value-of select="$current-uri"/>
                            <xsl:text>&amp;title=</xsl:text>
                            <xsl:value-of select="dim:field[@element='title'][1]/node()"/>
                        </xsl:attribute>
                        <xsl:attribute name="title">
                            <xsl:text>Citeulike</xsl:text>
                        </xsl:attribute>
                        <i aria-hidden="true">
                            <xsl:attribute name="class">
                                <xsl:text>flaticon flaticon-citeulike2 fa-lg</xsl:text>
                            </xsl:attribute>
                        </i>
                        <xsl:text> </xsl:text>
                    </a>
                </li>
            </ul>
        </div>
    </xsl:template>

    <xsl:template name="eatAllSlashes">
        <xsl:param name="pText"/>

        <xsl:choose>
            <xsl:when test="not(contains($pText,'/'))">
                <xsl:value-of select="$pText"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="eatAllSlashes">
                    <xsl:with-param name="pText"
                                    select="substring-after($pText, '/')"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="getFileIcon">
        <xsl:param name="mimetype"/>
        <xsl:choose>
            <xsl:when test="contains(mets:FLocat[@LOCTYPE='URL']/@xlink:href,'isAllowed=n')">
                <i aria-hidden="true">
                    <xsl:attribute name="class">
                        <xsl:text>fa fa-lock</xsl:text>
                    </xsl:attribute>
                </i>
            </xsl:when>
            <xsl:when test="contains(mets:FLocat[@LOCTYPE='URL']/@xlink:href,'pdf')">
                <img src="{$theme-path}/images/pdf.png"/>
            </xsl:when>
            <xsl:otherwise>
                <i aria-hidden="true">
                    <xsl:attribute name="class">
                        <xsl:text>fa fa-file</xsl:text>
                    </xsl:attribute>
                </i>
            </xsl:otherwise>
        </xsl:choose>

        <xsl:text> </xsl:text>
    </xsl:template>

</xsl:stylesheet>
