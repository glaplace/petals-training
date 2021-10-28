<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="xml" encoding="UTF-8" indent="yes" omit-xml-declaration="no"/>
    <xsl:variable name="ol-ns">http://cd35.fr/controle-legalite/1.0</xsl:variable>
    <xsl:variable name="modele">http://ausy.fr/training/petals/modele/biblotheque/1.0</xsl:variable>

    <xsl:template match="/">
        <xsl:element name="obtenir-par-isbn-reponse" namespace="${ol-ns}">
            <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="response">
        <xsl:element name="livre" namespace="{$modele}">
            <xsl:element name="titre" namespace="{$modele}">
                <xsl:value-of select="title"/>
            </xsl:element>
            <xsl:element name="resume" namespace="{$modele}">
                <xsl:value-of select="description/value"/>
            </xsl:element>
            <xsl:element name="nb-page" namespace="{$modele}">
                <xsl:value-of select="identifiant-technique-dossier"/>
            </xsl:element>
            <xsl:element name="isbn" namespace="{$modele}">
                <xsl:value-of select="isbn_13"/>
            </xsl:element>
            <!-- référence vers le détails auteur -->
            <xsl:element name="auteur" namespace="{$modele}">
                <xsl:value-of select="authors[0]/key"/>
            </xsl:element>
            <xsl:element name="langue" namespace="{$modele}">
                <xsl:value-of select="publish_country"/>
            </xsl:element>
            <xsl:element name="annee-publication" namespace="{$modele}">
                <xsl:value-of select="publish_date"/>
            </xsl:element>
        </xsl:element>
    </xsl:template>
</xsl:stylesheet>
