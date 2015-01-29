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
    xmlns:confman="org.dspace.core.ConfigurationManager"
    exclude-result-prefixes="i18n dri mets xlink xsl dim xhtml mods dc confman">

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


    <xsl:template name="itemSummaryView-DIM-file-section">
        <xsl:choose>
            <xsl:when test="dim:field[@element='relation'][@qualifier='uri']">
                <xsl:call-template name="itemSummaryView-DIM-uri"/>
            </xsl:when>
            <xsl:when test="//mets:fileSec/mets:fileGrp[@USE='CONTENT' or @USE='ORIGINAL' or @USE='LICENSE']/mets:file">
                <div class="item-page-field-wrapper table">
                    <h5>
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-viewOpen</i18n:text>
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

    <xsl:template name="documentdelivery">
        <a>
            <xsl:attribute name="href">
                <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
                <xsl:value-of select="substring-before(/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request'][@qualifier='URI'],'handle/')"/>
                <xsl:text>/documentdelivery/</xsl:text>
                <xsl:value-of select="substring-after($request-uri,'handle/')"/>
            </xsl:attribute>
            <xsl:text>Request document delivery</xsl:text>
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
                        <xsl:attribute name="href">
                            <xsl:copy-of select="./node()"/>
                        </xsl:attribute>
                        <xsl:attribute name="target">
                            <xsl:text>_blank</xsl:text>
                        </xsl:attribute>
                        <xsl:attribute name="class">
                            <xsl:text>word-break</xsl:text>
                        </xsl:attribute>
                                <xsl:variable name="output">
                                    <xsl:call-template name="eatAllSlashes">
                                        <xsl:with-param name="pText" select="."/>
                                    </xsl:call-template>
                                </xsl:variable>
                                <xsl:choose>
                                    <xsl:when test="string-length($output) > 0">
                                        <xsl:value-of select="$output"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="text()"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                    </a>
                    <xsl:text> </xsl:text>
                    <i aria-hidden="true">
                        <xsl:attribute name="class">
                            <xsl:text>glyphicon </xsl:text>
                            <xsl:text>glyphicon-exclamation-sign</xsl:text>
                        </xsl:attribute>
                        <xsl:attribute name="data-original-title">
                        <xsl:text>
                            &lt;b&gt;DISCLAIMER&lt;/b&gt;&lt;br/&gt;
                            This link is being provided as a convenience and for informational purposes only.
                            SEAFDEC/AQD bears no responsibility for the accuracy, legality or content of the
                            external site or for that of subsequent links. Contact the external site for answers to
                            questions regarding its content.
                        </xsl:text>
                        </xsl:attribute>
                        <xsl:attribute name="data-placement">
                            <xsl:text>top</xsl:text>
                        </xsl:attribute>
                        <xsl:attribute name="data-html">
                            <xsl:text>true</xsl:text>
                        </xsl:attribute>
                        <xsl:attribute name="data-toggle">
                            <xsl:text>tooltip</xsl:text>
                        </xsl:attribute>
                    </i>
                    <xsl:if test="count(following-sibling::dim:field[@element='relation' and @qualifier='uri']) != 0">
                        <br/>
                    </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
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

</xsl:stylesheet>
