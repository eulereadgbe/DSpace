<!--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

-->

<!--
    Rendering of a list of items (e.g. in a search or
    browse results page)

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
    xmlns:atom="http://www.w3.org/2005/Atom"
    xmlns:ore="http://www.openarchives.org/ore/terms/"
    xmlns:oreatom="http://www.openarchives.org/ore/atom/"
    xmlns="http://www.w3.org/1999/xhtml"
    xmlns:xalan="http://xml.apache.org/xalan"
    xmlns:encoder="xalan://java.net.URLEncoder"
    xmlns:util="org.dspace.app.xmlui.utils.XSLUtils"
    xmlns:confman="org.dspace.core.ConfigurationManager"
    exclude-result-prefixes="xalan encoder i18n dri mets dim xlink xsl util confman">

    <xsl:output indent="yes"/>

    <!--these templates are modfied to support the 2 different item list views that
    can be configured with the property 'xmlui.theme.mirage.item-list.emphasis' in dspace.cfg-->

    <xsl:template name="itemSummaryList-DIM">
        <xsl:variable name="itemWithdrawn" select="./mets:dmdSec/mets:mdWrap[@OTHERMDTYPE='DIM']/mets:xmlData/dim:dim/@withdrawn" />

        <xsl:variable name="href">
            <xsl:choose>
                <xsl:when test="$itemWithdrawn">
                    <xsl:value-of select="@OBJEDIT"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="@OBJID"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="emphasis" select="confman:getProperty('xmlui.theme.mirage.item-list.emphasis')"/>
        <xsl:choose>
            <xsl:when test="'file' = $emphasis">


                <div class="item-wrapper row">
                    <div class="col-sm-3 hidden-xs">
                        <xsl:apply-templates select="./mets:fileSec" mode="artifact-preview">
                            <xsl:with-param name="href" select="$href"/>
                        </xsl:apply-templates>
                    </div>

                    <div class="col-sm-9">
                        <xsl:apply-templates select="./mets:dmdSec/mets:mdWrap[@OTHERMDTYPE='DIM']/mets:xmlData/dim:dim"
                                             mode="itemSummaryList-DIM-metadata">
                            <xsl:with-param name="href" select="$href"/>
                        </xsl:apply-templates>
                    </div>

                </div>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates select="./mets:dmdSec/mets:mdWrap[@OTHERMDTYPE='DIM']/mets:xmlData/dim:dim"
                                     mode="itemSummaryList-DIM-metadata"><xsl:with-param name="href" select="$href"/></xsl:apply-templates>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!--handles the rendering of a single item in a list in file mode-->
    <!--handles the rendering of a single item in a list in metadata mode-->
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
                                <span>
                                  <xsl:if test="@authority">
                                    <xsl:attribute name="class"><xsl:text>ds-dc_contributor_author-authority</xsl:text></xsl:attribute>
                                  </xsl:if>
                                  <xsl:copy-of select="node()"/>
                                </span>
                                <xsl:if test="count(following-sibling::dim:field[@element='contributor'][@qualifier='author']) != 0">
                                    <xsl:text>; </xsl:text>
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
                        <xsl:otherwise/>
                    </xsl:choose>
                    </small>
                </span>
                <xsl:text> </xsl:text>
                <xsl:if test="dim:field[@element='date' and @qualifier='issued']">
                    <xsl:choose>
                        <xsl:when test="dim:field[@element='type']='newspaperArticle'">
                            <xsl:text>(</xsl:text>
                            <i><xsl:value-of select="dim:field[@element='citation' and @qualifier='journalTitle']/node()"/></i>
                            <xsl:text>, </xsl:text>
                            <xsl:call-template name="transform-date"/>
                            <xsl:text>, on page </xsl:text>
                            <xsl:value-of select="dim:field[@element='citation' and @qualifier='spage']"/>
                            <xsl:if test="dim:field[@element='citation' and @qualifier='epage']">
                                <xsl:text>-</xsl:text>
                                <xsl:value-of select="dim:field[@element='citation' and @qualifier='epage']"/>
                            </xsl:if>
                            <xsl:text>)</xsl:text>
                        </xsl:when>
                        <xsl:when test="dim:field[@element='type']='Article' or dim:field[@element='type']='magazineArticle'">
                            <i><xsl:value-of select="dim:field[@element='citation' and @qualifier='journalTitle']/node()"/></i>
                            <xsl:text> </xsl:text>
                            <xsl:if test="dim:field[@element='citation' and @qualifier='volume']/node()">
                                <xsl:value-of select="dim:field[@element='citation' and @qualifier='volume']/node()"/>
                            </xsl:if>
                            <xsl:if test="dim:field[@element='citation' and @qualifier='issue']/node()">
                                <xsl:text>, no. </xsl:text>
                                <xsl:value-of select="dim:field[@element='citation' and @qualifier='issue']/node()"/>
                            </xsl:if>
                            <xsl:text>(</xsl:text>
                            <xsl:value-of select="substring(dim:field[@element='date' and @qualifier='issued']/node(),1,10)"/>
                            <xsl:text>)</xsl:text>
                            <xsl:if test="dim:field[@element='citation' and @qualifier='spage']/node()">
                                <xsl:text>: </xsl:text>
                                <xsl:value-of select="dim:field[@element='citation' and @qualifier='spage']/node()"/>
                            </xsl:if>
                            <xsl:if test="dim:field[@element='citation' and @qualifier='epage']/node()">
                                <xsl:text>-</xsl:text>
                                <xsl:value-of select="dim:field[@element='citation' and @qualifier='epage']/node()"/>
                            </xsl:if>
                            <xsl:text>.</xsl:text>
                        </xsl:when>
                        <xsl:otherwise>
                            <span class="publisher-date h4">  <small>
                                <xsl:text>(</xsl:text>
                                <xsl:if test="dim:field[@element='publisher']">
                                    <span class="publisher">
                                        <xsl:copy-of select="dim:field[@element='publisher']/node()"/>
                                    </span>
                                    <xsl:text>, </xsl:text>
                                </xsl:if>
                                    <xsl:call-template name="transform-date"/>
                                <xsl:text>)</xsl:text>
                            </small></span>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:if>
                <xsl:if test="dim:field[@element='relation' and @qualifier='ispartofseries']">
                    <br/>
                    <small>
                        <xsl:text>(</xsl:text>
                        <i>
                            <xsl:copy-of select="dim:field[@element='relation' and @qualifier='ispartofseries']/node()"/>
                        </i>
                        <xsl:text>)</xsl:text>
                    </small>
                </xsl:if>
            </div>
            <xsl:if test="dim:field[@element = 'seafdecaqd' and @qualifier='extract']">
                <xsl:variable name="extract" select="dim:field[@element = 'seafdecaqd' and @qualifier='extract']/node()"/>
                <div class="artifact-abstract">
                    <xsl:value-of select="util:shortenString($extract, 220, 10)"/>
                </div>
            </xsl:if>
        </div>
    </xsl:template>

    <xsl:template name="itemDetailList-DIM">
        <xsl:call-template name="itemSummaryList-DIM"/>
    </xsl:template>


    <xsl:template match="mets:fileSec" mode="artifact-preview">
        <xsl:param name="href"/>
        <div class="thumbnail artifact-preview">
            <a class="image-link" href="{$href}">
                <xsl:choose>
                    <xsl:when test="mets:fileGrp[@USE='THUMBNAIL']">
                        <!-- Checking if Thumbnail is restricted and if so, show a restricted image --> 
                        <xsl:variable name="src">
                            <xsl:value-of select="mets:fileGrp[@USE='THUMBNAIL']/mets:file/mets:FLocat[@LOCTYPE='URL']/@xlink:href"/>
                        </xsl:variable>
                        <xsl:choose>
                            <xsl:when test="contains($src,'isAllowed=n')">
                                <div style="width: 100%; text-align: center">
                                    <i aria-hidden="true" class="glyphicon  glyphicon-lock"></i>
                                </div>
                            </xsl:when>
                            <xsl:otherwise>
                                <img class="img-responsive img-thumbnail" alt="xmlui.mirage2.item-list.thumbnail" i18n:attr="alt">
                                    <xsl:attribute name="src">
                                        <xsl:value-of select="$src"/>
                                    </xsl:attribute>
                                </img>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise>
                        <img class="img-thumbnail" alt="xmlui.mirage2.item-list.thumbnail" i18n:attr="alt">
                            <xsl:attribute name="data-src">
                                <xsl:text>holder.js/100%x</xsl:text>
                                <xsl:value-of select="$thumbnail.maxheight"/>
                                <!-- <xsl:text>/text:No Thumbnail</xsl:text> -->
                                <xsl:text>/text:</xsl:text>
                                <xsl:value-of select="../mets:dmdSec/mets:mdWrap[@OTHERMDTYPE='DIM']/mets:xmlData/dim:dim/dim:field[@qualifier='journalTitle']/text()"/>
                                <xsl:text> \n </xsl:text>
                                <xsl:copy>
                                    <xsl:call-template name="format_date">
                                        <xsl:with-param name="date" select="substring(../mets:dmdSec/mets:mdWrap[@OTHERMDTYPE='DIM']/mets:xmlData/dim:dim/dim:field[@qualifier='issued' and @element='date']/node(),1,10)"/>
                                    </xsl:call-template>
                                </xsl:copy>
                            </xsl:attribute>
                        </img>
                    </xsl:otherwise>
                </xsl:choose>
            </a>
        </div>
    </xsl:template>




    <!--
        Rendering of a list of items (e.g. in a search or
        browse results page)

        Author: art.lowel at atmire.com
        Author: lieven.droogmans at atmire.com
        Author: ben at atmire.com
        Author: Alexey Maslov

    -->



        <!-- Generate the info about the item from the metadata section -->
        <xsl:template match="dim:dim" mode="itemSummaryList-DIM">
            <xsl:variable name="itemWithdrawn" select="@withdrawn" />
            <div class="artifact-description">
                <div class="artifact-title">
                    <xsl:element name="a">
                        <xsl:attribute name="href">
                            <xsl:choose>
                                <xsl:when test="$itemWithdrawn">
                                    <xsl:value-of select="ancestor::mets:METS/@OBJEDIT" />
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="ancestor::mets:METS/@OBJID" />
                                </xsl:otherwise>
                            </xsl:choose>
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
                </div>
                <span class="Z3988">
                    <xsl:attribute name="title">
                        <xsl:call-template name="renderCOinS"/>
                    </xsl:attribute>
                    &#xFEFF; <!-- non-breaking space to force separating the end tag -->
                </span>
                <div class="artifact-info">
                    <span class="author">
                        <xsl:choose>
                            <xsl:when test="dim:field[@element='contributor'][@qualifier='author']">
                                <xsl:for-each select="dim:field[@element='contributor'][@qualifier='author']">
                                    <span>
                                        <xsl:if test="@authority">
                                            <xsl:attribute name="class"><xsl:text>ds-dc_contributor_author-authority</xsl:text></xsl:attribute>
                                        </xsl:if>
                                        <xsl:copy-of select="node()"/>
                                    </span>
                                    <xsl:if test="count(following-sibling::dim:field[@element='contributor'][@qualifier='author']) != 0">
                                        <xsl:text>; </xsl:text>
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
                    </span>
                    <xsl:text> </xsl:text>
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
                </div>
            </div>
        </xsl:template>

    <xsl:template name="transform-date">
        <xsl:copy>
            <xsl:call-template name="format_date">
                <xsl:with-param name="date" select="substring(dim:field[@element='date' and @qualifier='issued']/node(),1,10)"/>
            </xsl:call-template>
        </xsl:copy>

    </xsl:template>

    <xsl:template name="format_date">
        <xsl:param name="date"/>

        <xsl:variable name="month" select="substring($date, 6, 2)"/>

        <xsl:choose>
            <xsl:when test="$month = '01'">January</xsl:when>
            <xsl:when test="$month = '02'">February</xsl:when>
            <xsl:when test="$month = '03'">March</xsl:when>
            <xsl:when test="$month = '04'">April</xsl:when>
            <xsl:when test="$month = '05'">May</xsl:when>
            <xsl:when test="$month = '06'">June</xsl:when>
            <xsl:when test="$month = '07'">July</xsl:when>
            <xsl:when test="$month = '08'">August</xsl:when>
            <xsl:when test="$month = '09'">September</xsl:when>
            <xsl:when test="$month = '10'">October</xsl:when>
            <xsl:when test="$month = '11'">November</xsl:when>
            <xsl:when test="$month = '12'">December</xsl:when>
        </xsl:choose>
         <xsl:value-of select="number(substring($date, 9, 2))"/><xsl:text>, </xsl:text>
        <xsl:value-of select="substring($date, 1, 4)"/>
    </xsl:template>

</xsl:stylesheet>
