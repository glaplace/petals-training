<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:rest-xsl="http://petals.ow2.org/bc/rest/xsl/param/output/1.0"
>

    <xsl:output method="xml" encoding="UTF-8" indent="yes" omit-xml-declaration="no"/>
    <xsl:variable name="openlibrary-ns">http://openlibrary.org/api/1.0</xsl:variable>
    <xsl:param name="rest-xsl:http-status-code"/>
    <xsl:param name="isbn" />

    <xsl:template match="/">
        <xsl:element name="openlibrary:isbn-inconnu" namespace="{$openlibrary-ns}">
            <xsl:element name="openlibrary:isbn" namespace="{$openlibrary-ns}">
                <xsl:value-of select="$isbn"/>
            </xsl:element>
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>
