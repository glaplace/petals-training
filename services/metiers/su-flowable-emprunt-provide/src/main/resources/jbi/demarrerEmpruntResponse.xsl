<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:se-out-special-params="http://petals.ow2.org/se/flowable/output-params/1.0/special"
                xmlns:se-out-process-params="http://petals.ow2.org/se/flowable/output-params/1.0/process-instance">

    <xsl:output method="xml" encoding="UTF-8" indent="no" omit-xml-declaration="no"/>

    <!-- The process instance id retrieve from the BPMN engine -->
    <xsl:param name="se-out-special-params:processInstanceId"/>

    <xsl:template match="/">
        <xsl:element name="emprunterReponse"
                     namespace="http://ausy.fr/training/petals/bibliotheque/emprunter/metier/processus/1.0">
            <xsl:element name="processInstanceId">
                <xsl:value-of select="$se-out-special-params:processInstanceId"/>
            </xsl:element>
        </xsl:element>
    </xsl:template>
</xsl:stylesheet>
