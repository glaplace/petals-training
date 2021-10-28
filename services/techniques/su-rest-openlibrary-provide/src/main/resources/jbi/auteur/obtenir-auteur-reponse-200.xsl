<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="xml" encoding="UTF-8" indent="yes" omit-xml-declaration="no"/>
    <xsl:variable name="ol-ns">http://cd35.fr/controle-legalite/1.0</xsl:variable>

    <xsl:template match="/">
        <xsl:element name="obtenir-auteur-reponse" namespace="${ol-ns}">
            <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="response">
        <xsl:element name="nom" namespace="{$ol-ns}">
            <xsl:value-of select="name"/>
        </xsl:element>
    </xsl:template>
</xsl:stylesheet>
