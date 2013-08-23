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
        exclude-result-prefixes="xalan encoder i18n dri mets dim xlink xsl util jstring rights">

    <xsl:output indent="yes"/>

    <xsl:template match="dim:dim" mode="itemDetailView-DIM">
        <div class="responsive">
            <table class="ds-includeSet-table detailtable">
                <xsl:apply-templates mode="itemDetailView-DIM"/>
            </table>
        </div>
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
        <span class="Z3988">
            <xsl:attribute name="title">
                <xsl:call-template name="renderCOinS"/>
            </xsl:attribute>
            &#xFEFF; <!-- non-breaking space to force separating the end tag -->
        </span>
        <xsl:copy-of select="$SFXLink"/>
    </xsl:template>

    <xsl:template match="dim:field" mode="itemDetailView-DIM">
        <tr>
            <xsl:attribute name="class">
                <xsl:text>ds-table-row </xsl:text>
                <xsl:if test="(position() div 2 mod 2 = 0)">even</xsl:if>
                <xsl:if test="(position() div 2 mod 2 = 1)">odd</xsl:if>
            </xsl:attribute>
            <td class="label-cell">
                <xsl:choose>
                    <xsl:when test="@element='contributor' and @qualifier='author'">
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.item-author</i18n:text>
                    </xsl:when>
                    <xsl:when test="@element='contributor' and @qualifier='editor'">
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.item-editor</i18n:text>
                    </xsl:when>
                    <xsl:when test="@element='contributor'">
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.item-contributor</i18n:text>
                    </xsl:when>
                    <xsl:when test="@element='title'">
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.item-title</i18n:text>
                    </xsl:when>
                    <xsl:when test="@element='description' and @qualifier='abstract'">
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.item-abstract</i18n:text>
                    </xsl:when>
                    <xsl:when test="@element='description' and @qualifier='provenance'">
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.item-provenance</i18n:text>
                    </xsl:when>
                    <xsl:when test="@element='description' and @qualifier='sponsorship'">
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.item-sponsorship</i18n:text>
                    </xsl:when>
                    <xsl:when test="@element='description'">
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.item-description</i18n:text>
                    </xsl:when>
                    <xsl:when test="@element='date' and @qualifier='accessioned'">
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.item-dateaccessioned</i18n:text>
                    </xsl:when>
                    <xsl:when test="@element='date' and @qualifier='available'">
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.item-dateavailable</i18n:text>
                    </xsl:when>
                    <xsl:when test="@element='date' and @qualifier='issued'">
                        <xsl:text>Date issued</xsl:text>
                    </xsl:when>
                    <xsl:when test="@element='identifier' and @qualifier='citation'">
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.item-citation</i18n:text>
                    </xsl:when>
                    <xsl:when test="@element='identifier' and @qualifier='issn'">
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.item-issn</i18n:text>
                    </xsl:when>
                    <xsl:when test="@element='identifier' and @qualifier='isbn'">
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.item-isbn</i18n:text>
                    </xsl:when>
                    <xsl:when test="@element='identifier' and @qualifier='uri'">
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.item-uri</i18n:text>
                    </xsl:when>
                    <xsl:when test="@element='relation' and @qualifier='uri'">
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.item-url</i18n:text>
                    </xsl:when>
                    <xsl:when test="@element='relation' and @qualifier='ispartofseries'">
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.item-ispartofseries</i18n:text>
                    </xsl:when>
                    <xsl:when test="@element='relation' and @qualifier='ispartof'">
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.item-ispartof</i18n:text>
                    </xsl:when>
                    <xsl:when test="@element='identifier' and @qualifier='doi'">
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.item-doi</i18n:text>
                    </xsl:when>
                    <xsl:when test="@element='citation' and @qualifier='bookTitle'">
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.item-bookTitle</i18n:text>
                    </xsl:when>
                    <xsl:when test="@element='citation' and @qualifier='journalTitle'">
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.item-journalTitle</i18n:text>
                    </xsl:when>
                    <xsl:when test="@element='citation' and @qualifier='conferenceTitle'">
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.item-conferenceTitle</i18n:text>
                    </xsl:when>
                    <xsl:when test="@element='citation' and @qualifier='volume'">
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.item-volume</i18n:text>
                    </xsl:when>
                    <xsl:when test="@element='citation' and @qualifier='issue'">
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.item-issue</i18n:text>
                    </xsl:when>
                    <xsl:when test="@element='citation' and @qualifier='spage'">
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.item-spage</i18n:text>
                    </xsl:when>
                    <xsl:when test="@element='citation' and @qualifier='epage'">
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.item-epage</i18n:text>
                    </xsl:when>
                    <xsl:when test="@element='language' and @qualifier='iso'">
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.item-language</i18n:text>
                    </xsl:when>
                    <xsl:when test="@element='publisher'">
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.item-publisher</i18n:text>
                    </xsl:when>
                    <xsl:when test="@element='subject'">
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.item-subject</i18n:text>
                    </xsl:when>
                    <xsl:when test="@element='type'">
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.item-type</i18n:text>
                    </xsl:when>
                    <xsl:when test="@element='databank' and @qualifier='controlnumber'">
                        <xsl:text>Control No.</xsl:text>
                    </xsl:when>
                    <xsl:when test="@element='library' and @qualifier='callnumber'">
                        <xsl:text>Call No.</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="./@mdschema"/>
                        <xsl:text>.</xsl:text>
                        <xsl:value-of select="./@element"/>
                        <xsl:if test="./@qualifier">
                            <xsl:text>.</xsl:text>
                            <xsl:value-of select="./@qualifier"/>
                        </xsl:if>
                    </xsl:otherwise>
                </xsl:choose>
            </td>
            <td class="value-cell">
                <xsl:choose>
                    <xsl:when test="./@element='description'">
                        <xsl:value-of select="./node()" disable-output-escaping="yes"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="./node()"/>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:if test="./@authority and ./@confidence">
                    <xsl:call-template name="authorityConfidenceIcon">
                        <xsl:with-param name="confidence" select="./@confidence"/>
                    </xsl:call-template>
                </xsl:if>
            </td>
            <td class="lang">
                <xsl:value-of select="./@language"/>
            </td>
        </tr>
    </xsl:template>

</xsl:stylesheet>