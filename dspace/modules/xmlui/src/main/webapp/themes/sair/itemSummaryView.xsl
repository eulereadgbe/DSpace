<!--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

-->
<!--
    Rendering specific to the item display page.

    Author: art.lowel at atmire.com
    Author: lieven.droogmans at atmire.com
    Author: ben at atmire.com
    Author: Alexey Maslov

-->

<xsl:stylesheet
        xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
        xmlns:dri="http://di.tamu.edu/DRI/1.0/"
        xmlns:mets="http://www.loc.gov/METS/"
        xmlns:dim="http://www.dspace.org/xmlns/dspace/dim"
        xmlns:xlink="http://www.w3.org/TR/xlink/"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
        xmlns="http://www.w3.org/1999/xhtml"
        xmlns:xalan="http://xml.apache.org/xalan"
        xmlns:encoder="xalan://java.net.URLEncoder"
        xmlns:util="org.dspace.app.xmlui.utils.XSLUtils"
        xmlns:jstring="java.lang.String"
        xmlns:rights="http://cosimo.stanford.edu/sdr/metsrights/"
        xmlns:str="http://exslt.org/strings"
        exclude-result-prefixes="xalan encoder i18n dri mets dim xlink xsl util jstring rights">

    <xsl:output indent="yes"/>

    <xsl:template name="itemSummaryView-DIM">
        <!-- Generate the info about the item from the metadata section -->
        <xsl:apply-templates select="./mets:dmdSec/mets:mdWrap[@OTHERMDTYPE='DIM']/mets:xmlData/dim:dim"
                             mode="itemSummaryView-DIM"/>

        <xsl:copy-of select="$SFXLink"/>
        <!-- Generate the bitstream information from the file section -->
        <xsl:choose>
            <xsl:when test="./mets:fileSec/mets:fileGrp[@USE='CONTENT' or @USE='ORIGINAL']/mets:file">
                <xsl:apply-templates select="./mets:fileSec/mets:fileGrp[@USE='CONTENT' or @USE='ORIGINAL']">
                    <xsl:with-param name="context" select="."/>
                    <xsl:with-param name="primaryBitstream"
                                    select="./mets:structMap[@TYPE='LOGICAL']/mets:div[@TYPE='DSpace Item']/mets:fptr/@FILEID"/>
                </xsl:apply-templates>
            </xsl:when>
            <!-- Special case for handling ORE resource maps stored as DSpace bitstreams -->
            <xsl:when test="./mets:fileSec/mets:fileGrp[@USE='ORE']">
                <xsl:apply-templates select="./mets:fileSec/mets:fileGrp[@USE='ORE']"/>
            </xsl:when>
            <xsl:otherwise>
                <h2>
                    <i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-head</i18n:text>
                </h2>
                <table class="ds-table file-list">
                    <tr class="ds-table-header-row">
                        <th>
                            <i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-file</i18n:text>
                        </th>
                        <th>
                            <i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-size</i18n:text>
                        </th>
                        <th>
                            <i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-format</i18n:text>
                        </th>
                        <th>
                            <i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-view</i18n:text>
                        </th>
                    </tr>
                    <tr>
                        <td colspan="4">
                            <p>
                                <i18n:text>xmlui.dri2xhtml.METS-1.0.item-no-files</i18n:text>
                            </p>
                        </td>
                    </tr>
                </table>
            </xsl:otherwise>
        </xsl:choose>

        <!-- Generate the Creative Commons license information from the file section (DSpace deposit license hidden by default)-->
        <xsl:apply-templates select="./mets:fileSec/mets:fileGrp[@USE='CC-LICENSE']"/>

    </xsl:template>


    <xsl:template match="dim:dim" mode="itemSummaryView-DIM">
        <div class="item-summary-view-metadata">
            <!-- Test the item if it's still under submission -->
            <xsl:call-template name="itemSummaryView-DIM-fields"/>
            <xsl:if test="dim:field[@element='identifier' and @qualifier='uri']">
            <!-- Add QR code in every item -->
            <xsl:element name="img">
                <xsl:attribute name="src">
                    <xsl:text>http://chart.apis.google.com/chart?cht=qr&amp;chs=100x100&amp;chl=</xsl:text>
                    <xsl:value-of select="dim:field[@element='identifier' and @qualifier='uri']"/>
                    <xsl:text>&amp;chld=H|0</xsl:text>
                </xsl:attribute>
                <xsl:attribute name="alt">QRCode</xsl:attribute>
            </xsl:element>
            </xsl:if>
        </div>
    </xsl:template>

    <xsl:template name="itemSummaryView-DIM-fields">
        <xsl:param name="clause" select="'1'"/>
        <xsl:param name="phase" select="'even'"/>
        <xsl:variable name="otherPhase">
            <xsl:choose>
                <xsl:when test="$phase = 'even'">
                    <xsl:text>odd</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>even</xsl:text>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:choose>
            <!-- Title row -->
            <xsl:when test="$clause = 1">

                <xsl:choose>
                    <xsl:when test="count(dim:field[@element='title'][not(@qualifier)]) &gt; 1">
                        <!-- display first title as h1 -->
                        <h1>
                            <xsl:value-of select="dim:field[@element='title'][not(@qualifier)][1]/node()"/>
                        </h1>
                        <div class="simple-item-view-other">
                            <span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-title</i18n:text>:
                            </span>
                            <span>
                                <xsl:for-each select="dim:field[@element='title'][not(@qualifier)]">
                                    <xsl:value-of select="./node()"/>
                                    <xsl:if test="count(following-sibling::dim:field[@element='title'][not(@qualifier)]) != 0">
                                        <xsl:text>; </xsl:text>
                                        <br/>
                                    </xsl:if>
                                </xsl:for-each>
                            </span>
                        </div>
                    </xsl:when>
                    <xsl:when test="count(dim:field[@element='title'][not(@qualifier)]) = 1">
                        <h1>
                            <xsl:value-of select="dim:field[@element='title'][not(@qualifier)][1]/node()"/>
                        </h1>
                    </xsl:when>
                    <xsl:otherwise>
                        <h1>
                            <i18n:text>xmlui.dri2xhtml.METS-1.0.no-title</i18n:text>
                        </h1>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:call-template name="itemSummaryView-DIM-fields">
                    <xsl:with-param name="clause" select="($clause + 1)"/>
                    <xsl:with-param name="phase" select="$otherPhase"/>
                </xsl:call-template>
            </xsl:when>

            <!-- Author(s) row -->
            <xsl:when
                    test="$clause = 2 and (dim:field[@element='contributor'][@qualifier='author'] or dim:field[@element='creator'] or dim:field[@element='contributor'])">
                <div class="simple-item-view-authors">
                    <xsl:choose>
                        <xsl:when test="dim:field[@element='contributor'][@qualifier='author']">
                            <xsl:for-each select="dim:field[@element='contributor'][@qualifier='author']">
                                <span>
                                    <xsl:if test="@authority">
                                        <xsl:attribute name="class">
                                            <xsl:text>ds-dc_contributor_author-authority</xsl:text>
                                        </xsl:attribute>
                                    </xsl:if>
                                    <a>
                                        <xsl:attribute name="href">
                                            <xsl:text>/discover?filtertype=author&amp;filter_relational_operator=contains&amp;filter=</xsl:text>
                                            <xsl:value-of select="substring-before(node(),',')"/>
                                        </xsl:attribute>
                                        <xsl:copy-of select="node()"/>
                                    </a>
                                <xsl:if test="count(following-sibling::dim:field[@element='contributor'][@qualifier='author']) != 0">
                                    <xsl:text>; </xsl:text>
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
                            <i18n:text>xmlui.dri2xhtml.METS-1.0.no-author</i18n:text>
                        </xsl:otherwise>
                    </xsl:choose>
                </div>
                <xsl:call-template name="itemSummaryView-DIM-fields">
                    <xsl:with-param name="clause" select="($clause + 1)"/>
                    <xsl:with-param name="phase" select="$otherPhase"/>
                </xsl:call-template>
            </xsl:when>

            <!-- identifier.uri row -->
            <xsl:when test="$clause = 3 and (dim:field[@element='identifier' and @qualifier='uri'])">
                <div class="simple-item-view-other">
                    <span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-uri</i18n:text>:
                    </span>
                    <span>
                        <xsl:for-each select="dim:field[@element='identifier' and @qualifier='uri']">
                            <a>
                                <xsl:attribute name="href">
                                    <xsl:copy-of select="./node()"/>
                                </xsl:attribute>
                                <xsl:copy-of select="./node()"/>
                            </a>
                            <xsl:if test="count(following-sibling::dim:field[@element='identifier' and @qualifier='uri']) != 0">
                                <br/>
                            </xsl:if>
                        </xsl:for-each>
                    </span>
                </div>
                <xsl:call-template name="itemSummaryView-DIM-fields">
                    <xsl:with-param name="clause" select="($clause + 1)"/>
                    <xsl:with-param name="phase" select="$otherPhase"/>
                </xsl:call-template>
            </xsl:when>

            <!-- identifier.doi row -->
            <xsl:when test="$clause = 4 and (dim:field[@element='identifier' and @qualifier='doi'])">
                <div class="simple-item-view-other">
                    <span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-doi</i18n:text>:
                    </span>
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
                <xsl:call-template name="itemSummaryView-DIM-fields">
                    <xsl:with-param name="clause" select="($clause + 1)"/>
                    <xsl:with-param name="phase" select="$otherPhase"/>
                </xsl:call-template>
            </xsl:when>

            <xsl:when test="$clause = 5 and (dim:field[@element='relation' and @qualifier='uri'])">
                <div class="simple-item-view-other">
                    <span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-url</i18n:text>:
                    </span>
                    <span>
                        <xsl:for-each select="dim:field[@element='relation' and @qualifier='uri']">
                            <a>
                                <xsl:attribute name="href">
                                    <xsl:copy-of select="./node()"/>
                                </xsl:attribute>
                                <xsl:copy-of select="./node()"/>
                            </a>
                            <xsl:if test="count(following-sibling::dim:field[@element='relation' and @qualifier='uri']) != 0">
                                <br/>
                            </xsl:if>
                        </xsl:for-each>
                    </span>
                </div>
                <xsl:call-template name="itemSummaryView-DIM-fields">
                    <xsl:with-param name="clause" select="($clause + 1)"/>
                    <xsl:with-param name="phase" select="$otherPhase"/>
                </xsl:call-template>
            </xsl:when>

            <!-- date.issued row -->
            <xsl:when test="$clause = 6 and (dim:field[@element='date' and @qualifier='issued'])">
                <div class="simple-item-view-other">
                    <span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-date</i18n:text>:
                    </span>
                    <span>
                        <xsl:for-each select="dim:field[@element='date' and @qualifier='issued']">
                            <xsl:copy-of select="substring(./node(),1,10)"/>
                            <xsl:if test="count(following-sibling::dim:field[@element='date' and @qualifier='issued']) != 0">
                                <br/>
                            </xsl:if>
                        </xsl:for-each>
                    </span>
                </div>
                <xsl:call-template name="itemSummaryView-DIM-fields">
                    <xsl:with-param name="clause" select="($clause + 1)"/>
                    <xsl:with-param name="phase" select="$otherPhase"/>
                </xsl:call-template>
            </xsl:when>

            <!-- Citation row -->
            <xsl:when
                    test="$clause = 7 and (dim:field[@element='identifier' and @qualifier='citation' and descendant::text()])">
                <div class="simple-item-view-description">
                    <h3><i18n:text>xmlui.dri2xhtml.METS-1.0.item-citation</i18n:text>:
                    </h3>
                    <div>
                        <xsl:for-each select="dim:field[@element='identifier' and @qualifier='citation']">
                            <xsl:choose>
                                <xsl:when test="node()">
                                    <xsl:value-of select="node()"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:text>&#160;</xsl:text>
                                </xsl:otherwise>
                            </xsl:choose>
                            <xsl:if test="count(following-sibling::dim:field[@element='identifier' and @qualifier='citation']) != 0">
                                <div class="spacer">&#160;</div>
                            </xsl:if>
                        </xsl:for-each>
                    </div>
                </div>
                <xsl:call-template name="itemSummaryView-DIM-fields">
                    <xsl:with-param name="clause" select="($clause + 1)"/>
                    <xsl:with-param name="phase" select="$otherPhase"/>
                </xsl:call-template>
            </xsl:when>

            <!-- Abstract row -->
            <xsl:when
                    test="$clause = 8 and (dim:field[@element='description' and @qualifier='abstract' and descendant::text()])">
                <div class="simple-item-view-description">
                    <h3><i18n:text>xmlui.dri2xhtml.METS-1.0.item-abstract</i18n:text>:
                    </h3>
                    <div>
                        <xsl:if test="count(dim:field[@element='description' and @qualifier='abstract']) &gt; 1">
                            <div class="spacer">&#160;</div>
                        </xsl:if>
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
                <xsl:call-template name="itemSummaryView-DIM-fields">
                    <xsl:with-param name="clause" select="($clause + 1)"/>
                    <xsl:with-param name="phase" select="$otherPhase"/>
                </xsl:call-template>
            </xsl:when>

            <!-- Description row -->
            <xsl:when test="$clause = 9 and (dim:field[@element='description' and not(@qualifier)])">
                <div class="simple-item-view-description">
                    <h3 class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-description</i18n:text>:
                    </h3>
                    <div>
                        <xsl:if test="count(dim:field[@element='description' and not(@qualifier)]) &gt; 1 and not(count(dim:field[@element='description' and @qualifier='abstract']) &gt; 1)">
                            <div class="spacer">&#160;</div>
                        </xsl:if>
                        <xsl:for-each select="dim:field[@element='description' and not(@qualifier)]">
                            <xsl:call-template name="break">
                                <xsl:with-param name="text" select="./node()"/>
                            </xsl:call-template>
                            <xsl:if test="count(following-sibling::dim:field[@element='description' and not(@qualifier)]) != 0">
                                <div class="spacer">&#160;</div>
                            </xsl:if>
                        </xsl:for-each>
                        <xsl:if test="count(dim:field[@element='description' and not(@qualifier)]) &gt; 1">
                            <div class="spacer">&#160;</div>
                        </xsl:if>
                    </div>
                </div>
                <xsl:call-template name="itemSummaryView-DIM-fields">
                    <xsl:with-param name="clause" select="($clause + 1)"/>
                    <xsl:with-param name="phase" select="$otherPhase"/>
                </xsl:call-template>
            </xsl:when>

            <!-- Sponsorship row -->
            <xsl:when
                    test="$clause = 10 and (dim:field[@element='description' and @qualifier='sponsorship' and descendant::text()])">
                <div class="simple-item-view-description">
                    <h3><i18n:text>xmlui.dri2xhtml.METS-1.0.item-sponsorship</i18n:text>:
                    </h3>
                    <div>
                        <xsl:if test="count(dim:field[@element='description' and @qualifier='sponsorship']) &gt; 1">
                            <div class="spacer">&#160;</div>
                        </xsl:if>
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
                <xsl:call-template name="itemSummaryView-DIM-fields">
                    <xsl:with-param name="clause" select="($clause + 1)"/>
                    <xsl:with-param name="phase" select="$otherPhase"/>
                </xsl:call-template>
            </xsl:when>

            <!-- TOC row -->
            <xsl:when
                    test="$clause = 11 and (dim:field[@element='description' and @qualifier='tableofcontents' and descendant::text()])">
                <div class="simple-item-view-description">
                    <h3><i18n:text>xmlui.dri2xhtml.METS-1.0.item-toc</i18n:text>:
                    </h3>
                    <div>
                        <xsl:if test="count(dim:field[@element='description' and @qualifier='tableofcontents']) &gt; 1">
                            <div class="spacer">&#160;</div>
                        </xsl:if>
                        <xsl:for-each select="dim:field[@element='description' and @qualifier='tableofcontents']">
                            <xsl:call-template name="break">
                                <xsl:with-param name="text" select="./node()"/>
                            </xsl:call-template>
                            <xsl:if test="count(following-sibling::dim:field[@element='description' and @qualifier='tableofcontents']) != 0">
                                <hr class="metadata-seperator"/>
                            </xsl:if>
                        </xsl:for-each>
                        <xsl:if test="count(dim:field[@element='description' and @qualifier='tableofcontents']) &gt; 1">
                            <div class="spacer">&#160;</div>
                        </xsl:if>
                    </div>
                </div>
                <xsl:call-template name="itemSummaryView-DIM-fields">
                    <xsl:with-param name="clause" select="($clause + 1)"/>
                    <xsl:with-param name="phase" select="$otherPhase"/>
                </xsl:call-template>
            </xsl:when>

            <!-- Keywords row -->
            <xsl:when test="$clause = 12 and (dim:field[@element='subject'][@qualifier='asfa'] or dim:field[@element='subject' and not(@qualifier)])">
                <div class="simple-item-view-subject">
                    <h3 class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-keyword</i18n:text>:</h3>
                    <div>
                        <xsl:if test="(dim:field[@element='subject' and @qualifier='asfa'])">
                            <xsl:for-each select="dim:field[@element='subject' and @qualifier='asfa']">
                                <a>
                                    <xsl:attribute name="href">
                                        <xsl:value-of
                                                select="concat($context-path,'/discover?filtertype=')"/>
                                        <xsl:text>subject&amp;filter_relational_operator=contains&amp;filter=</xsl:text>
                                        <xsl:copy-of select="."/>
                                    </xsl:attribute>
                                    <xsl:value-of select="text()"/>
                                </a>
                                <xsl:if test="count(following-sibling::dim:field[@element='subject' and @qualifier='asfa']) != 0">
                                    <xsl:text>; </xsl:text>
                                </xsl:if>
                            </xsl:for-each>
                            <xsl:if test="count(dim:field[@element='subject' and not(@qualifier)]) != 0">
                                <xsl:text>; </xsl:text>
                            </xsl:if>
                        </xsl:if>
                        <xsl:if test="(dim:field[@element='subject' and not(@qualifier)])">
                            <xsl:for-each select="dim:field[@element='subject' and not(@qualifier)]">
                                <a>
                                    <xsl:attribute name="href">
                                        <xsl:value-of
                                                select="concat($context-path,'/discover?filtertype=')"/>
                                        <xsl:text>subject&amp;filter_relational_operator=contains&amp;filter=</xsl:text>
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
                <xsl:call-template name="itemSummaryView-DIM-fields">
                    <xsl:with-param name="clause" select="($clause + 1)"/>
                    <xsl:with-param name="phase" select="$otherPhase"/>
                </xsl:call-template>
            </xsl:when>

            <!-- Source row -->
            <xsl:when test="$clause = 13 and (dim:field[@element='relation' and @qualifier='ispartof'])">
                <xsl:if test="count(dim:field[@element='identifier' and @qualifier='citation']) = 0">
                    <div class="simple-item-view-description">
                        <h3 class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-ispartof</i18n:text>:
                        </h3>
                        <div>
                            <xsl:for-each select="dim:field[@element='relation' and @qualifier='ispartof']">
                                <xsl:copy-of select="./node()"/>
                                <xsl:if test="count(following-sibling::dim:field[@element='relation' and @qualifier='ispartof']) != 0">
                                    <br/>
                                </xsl:if>
                            </xsl:for-each>

                        </div>
                    </div>
                </xsl:if>

                <xsl:call-template name="itemSummaryView-DIM-fields">
                    <xsl:with-param name="clause" select="($clause + 1)"/>
                    <xsl:with-param name="phase" select="$otherPhase"/>
                </xsl:call-template>
            </xsl:when>

            <xsl:when test="$clause = 14 and $ds_item_view_toggle_url != ''">
                <p class="ds-paragraph item-view-toggle item-view-toggle-bottom">
                    <a>
                        <xsl:attribute name="href">
                            <xsl:value-of select="$ds_item_view_toggle_url"/>
                        </xsl:attribute>
                        <i18n:text>xmlui.ArtifactBrowser.ItemViewer.show_full</i18n:text>
                    </a>
                </p>
            </xsl:when>

            <!-- recurse without changing phase if we didn't output anything -->
            <xsl:otherwise>
                <!-- IMPORTANT: This test should be updated if clauses are added! -->
                <xsl:if test="$clause &lt; 14">
                    <xsl:call-template name="itemSummaryView-DIM-fields">
                        <xsl:with-param name="clause" select="($clause + 1)"/>
                        <xsl:with-param name="phase" select="$phase"/>
                    </xsl:call-template>
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>

        <!-- Generate the Creative Commons license information from the file section (DSpace deposit license hidden by default) -->
        <xsl:apply-templates select="mets:fileSec/mets:fileGrp[@USE='CC-LICENSE']"/>
    </xsl:template>

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