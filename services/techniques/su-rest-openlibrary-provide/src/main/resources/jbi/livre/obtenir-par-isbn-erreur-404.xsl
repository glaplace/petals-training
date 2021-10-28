<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:rest-xsl="http://petals.ow2.org/bc/rest/xsl/param/output/1.0"
>

    <xsl:output method="xml" encoding="UTF-8" indent="yes" omit-xml-declaration="no"/>
    <xsl:variable name="ol-ns">http://openlibrary.org/api/1.0</xsl:variable>
    <xsl:param name="rest-xsl:http-status-code"/>
    <xsl:param name="isbn" />

    <xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="response">
        <xsl:element name="ol:isbn-inconnu" namespace="{$ol-ns}">
            <xsl:element name="isbn" namespace="{$ol-ns}">
                <xsl:value-of select="$isbn"/>
            </xsl:element>
        </xsl:element>
    </xsl:template>
</xsl:stylesheet>
