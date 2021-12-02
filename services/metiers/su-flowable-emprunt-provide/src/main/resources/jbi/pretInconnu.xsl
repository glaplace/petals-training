<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:bpmn-fault="http://petals.ow2.org/se/flowable/faults/1.0">

    <xsl:output method="xml" encoding="UTF-8" omit-xml-declaration="no"/>

    <xsl:param name="bpmn-fault:processInstanceId"/>
    <xsl:param name="bpmn-fault:messageName"/>

    <xsl:template match="/">
        <xsl:element name="pretInconnu"
                     namespace="http://ausy.fr/training/petals/bibliotheque/emprunter/metier/processus/1.0">
            <xsl:element name="process-instance-id-callback">
                <xsl:value-of select="$bpmn-fault:processInstanceId"/>
            </xsl:element>
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>
