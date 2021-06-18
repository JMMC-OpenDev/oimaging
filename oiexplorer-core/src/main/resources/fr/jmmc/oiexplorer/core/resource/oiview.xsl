<?xml version="1.0" encoding="UTF-8" ?>
<!--
*******************************************************************************
* JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
******************************************************************************
-->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:exslt="http://exslt.org/common"
                xmlns:math="http://exslt.org/math"
                xmlns:date="http://exslt.org/dates-and-times"
                xmlns:func="http://exslt.org/functions"
                xmlns:set="http://exslt.org/sets"
                xmlns:str="http://exslt.org/strings"
                xmlns:dyn="http://exslt.org/dynamic"
                extension-element-prefixes="exslt math date func set str dyn" >

    <xsl:output method="html" encoding="UTF-8"/>




    <!-- root document correspond to the result branch of main document -->
    <xsl:template match="/">
        <html>
            <body>
                <xsl:apply-templates select="//oifits/*"/>
            </body>
        </html>
    </xsl:template>




    <xsl:template match="filename">
        <h2>
            File :
        </h2>

        <xsl:value-of select="text()"/>
    </xsl:template>




    <xsl:template match="OI_ARRAY|OI_WAVELENGTH|OI_TARGET|OI_VIS|OI_VIS2|OI_T3|OI_SPECTRUM|OI_FLUX">
        <h2>
            <xsl:value-of select="name()"/>
        </h2>

        <xsl:apply-templates select="keywords"/>

        <xsl:apply-templates select="table"/>
    </xsl:template>




    <xsl:template match="keywords">
        <h3>Keywords</h3>

        <table border="1">
            <tr>
                <th>name</th>
                <th>value</th>
                <th>description</th>
            </tr>
            <xsl:for-each select="keyword">
                <tr>
                    <td>
                        <xsl:value-of select="name"/>
                    </td>
                    <td>
                        <xsl:value-of select="value"/>
                    </td>
                    <td>
                        <xsl:value-of select="description"/>
                    </td>
                </tr>
            </xsl:for-each>
        </table>
    </xsl:template>




    <xsl:template match="table">
        <h3>Columns</h3>

        <table border="1">
            <xsl:copy-of select="tr"/>
        </table>
    </xsl:template>

</xsl:stylesheet>
