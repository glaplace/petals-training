package fr.ausy.traing.petals.services.metiers.livre.operation;

import org.junit.Test;
import org.ow2.easywsdl.extensions.wsdl4complexwsdl.WSDL4ComplexWsdlFactory;
import org.ow2.easywsdl.extensions.wsdl4complexwsdl.api.WSDL4ComplexWsdlException;
import org.ow2.easywsdl.extensions.wsdl4complexwsdl.api.WSDL4ComplexWsdlReader;
import org.ow2.easywsdl.wsdl.api.Description;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.ow2.petals.camel.junit.Assert.assertWsdlCompliance;

/**
 * Validation WSDL / jbi.xml .
 */
public class WsdlComplianceTest {
    /**
     * Validation cohérence wsdl / jbi.xml.
     *
     * @throws Exception Exception levée en cas d'erreur
     */
    @Test
    public void validate() throws Exception {
        assertWsdlCompliance();
    }

    /**
     * Validatio uniquement du WSDL, bien formé et assertion sur le nombre d'opération.
     *
     * @throws WSDL4ComplexWsdlException Exception levée si l'outil WSDL ne « démarre pas ». ou si le wsdl ne pas être lu
     * @throws URISyntaxException        Exception levé si l'emplacement du WSDL n'est pas indiqué correctement.
     * @throws IOException               exception levée si le WSDL ne peux être lu.
     */
    @Test
    public void validateWsdl() throws WSDL4ComplexWsdlException, URISyntaxException, IOException {

        final URL wsdlUrl = Thread.currentThread().getContextClassLoader().getResource("jbi/referentiel-livre.wsdl");
        assertNotNull(wsdlUrl);

        final WSDL4ComplexWsdlReader wsdlReader = WSDL4ComplexWsdlFactory.newInstance().newWSDLReader();
        final Description wsdlDescr = wsdlReader.read(wsdlUrl);
        assertNotNull(wsdlDescr);
        assertEquals(5, wsdlDescr.getBindings().get(0).getBindingOperations().size());
    }
}
