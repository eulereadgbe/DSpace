<!--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

-->
<xsl:stylesheet
        xmlns="http://di.tamu.edu/DRI/1.0/"
        xmlns:dri="http://di.tamu.edu/DRI/1.0/"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
        xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
        exclude-result-prefixes="xsl dri i18n">

    <xsl:output indent="yes"/>

    <!-- render frequency counts in sidebar facets as badges -->
    <xsl:template match="dri:list[@id='aspect.discovery.Navigation.list.discovery']/dri:list/dri:item/dri:xref" priority="9">
        <xref>
            <xsl:call-template name="copy-attributes"/>
            <xsl:choose>
                <xsl:when test="contains(text(), ' (') and contains(substring-after(text(), ' ('), ')')">
                    <xsl:variable name="title">
                        <xsl:call-template name="substring-before-last">
                            <xsl:with-param name="string" select="text()"/>
                            <xsl:with-param name="separator"><xsl:text> (</xsl:text></xsl:with-param>
                        </xsl:call-template>
                    </xsl:variable>
                    <xsl:variable name="count">
                        <xsl:call-template name="remove-parens">
                            <xsl:with-param name="string" select="normalize-space(substring-after(text(), $title))"/>
                        </xsl:call-template>
                    </xsl:variable>
                    <xsl:choose>
                        <xsl:when test="contains(.,'true')">
                            <i18n:text>xmlui.ArtifactBrowser.AdvancedSearch.value_has_content_in_original_bundle_true</i18n:text>
                        </xsl:when>
                        <xsl:when test="contains(.,'false')">
                            <i18n:text>xmlui.ArtifactBrowser.AdvancedSearch.value_has_content_in_original_bundle_false</i18n:text>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="$title"/>
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:text> </xsl:text>
                    <hi rend="badge">
                        <xsl:value-of select="format-number($count, '#,###')"/>
                    </hi>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates/>
                </xsl:otherwise>
            </xsl:choose>
        </xref>
    </xsl:template>

    <!-- better highlight active co-author/subject etc in facet list, incl show count as badge -->
    <xsl:template match="dri:list[@id='aspect.discovery.Navigation.list.discovery']/dri:list/dri:item[@rend='selected']">
        <item>
            <xsl:call-template name="copy-attributes"/>
            <xsl:attribute name="rend"><xsl:value-of select="@rend"/> disabled</xsl:attribute>
            <xsl:choose>
                <xsl:when test="contains(text(), ' (') and contains(substring-after(text(), ' ('), ')')">
                    <xsl:variable name="title">
                        <xsl:call-template name="substring-before-last">
                            <xsl:with-param name="string" select="text()"/>
                            <xsl:with-param name="separator"><xsl:text> (</xsl:text></xsl:with-param>
                        </xsl:call-template>
                    </xsl:variable>
                    <xsl:variable name="count">
                        <xsl:call-template name="remove-parens">
                            <xsl:with-param name="string" select="normalize-space(substring-after(text(), $title))"/>
                        </xsl:call-template>
                    </xsl:variable>
                    <xsl:choose>
                        <xsl:when test="contains(.,'true')">
                            <i18n:text>xmlui.ArtifactBrowser.AdvancedSearch.value_has_content_in_original_bundle_true</i18n:text>
                        </xsl:when>
                        <xsl:when test="contains(.,'false')">
                            <i18n:text>xmlui.ArtifactBrowser.AdvancedSearch.value_has_content_in_original_bundle_false</i18n:text>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="$title"/>
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:text> </xsl:text>
                    <hi rend="badge">
                        <xsl:value-of select="format-number($count, '#,###')"/>
                    </hi>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates/>
                </xsl:otherwise>
            </xsl:choose>
        </item>
    </xsl:template>

    <xsl:template name="substring-before-last">
        <xsl:param name="string" select="''" />
        <xsl:param name="separator" select="''" />

        <xsl:if test="$string != '' and $separator != ''">
            <xsl:variable name="head" select="substring-before($string, $separator)" />
            <xsl:variable name="tail" select="substring-after($string, $separator)" />
            <xsl:value-of select="$head" />
            <xsl:if test="contains($tail, $separator)">
                <xsl:value-of select="$separator" />
                <xsl:call-template name="substring-before-last">
                    <xsl:with-param name="string" select="$tail" />
                    <xsl:with-param name="separator" select="$separator" />
                </xsl:call-template>
            </xsl:if>
        </xsl:if>
    </xsl:template>

    <xsl:template name="remove-parens">
        <xsl:param name="string" select="''" />
        <xsl:if test="starts-with($string, '(') and substring($string, string-length($string))=')'">
            <xsl:value-of select="substring($string, 2, string-length($string) - 2)"/>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>