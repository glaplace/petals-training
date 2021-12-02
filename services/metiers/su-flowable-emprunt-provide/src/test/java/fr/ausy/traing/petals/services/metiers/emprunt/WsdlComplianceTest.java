package fr.ausy.traing.petals.services.metiers.emprunt;

import org.junit.Test;
import org.ow2.easywsdl.extensions.wsdl4complexwsdl.WSDL4ComplexWsdlFactory;
import org.ow2.easywsdl.extensions.wsdl4complexwsdl.api.WSDL4ComplexWsdlException;
import org.ow2.easywsdl.extensions.wsdl4complexwsdl.api.WSDL4ComplexWsdlReader;
import org.ow2.easywsdl.wsdl.api.Description;

import javax.xml.namespace.QName;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.LogManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.ow2.petals.flowable.junit.Assert.assertWsdlCompliance;

/**
 * Validation du WSDL.
 */
public class WsdlComplianceTest {

    private static final String NAMESPACE = "http://ausy.fr/training/petals/bibliotheque/emprunter/metier/1.0";

    static {
        final URL logConfig = WsdlComplianceTest.class.getResource("/logging.properties");
        assertNotNull("Logging configuration file not found", logConfig);
        try {
            LogManager.getLogManager().readConfiguration(logConfig.openStream());
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void validate() throws Exception {
        assertWsdlCompliance(new QName[]{
            new QName(NAMESPACE, "emprunter"),
            new QName(NAMESPACE, "retourner"),
            new QName(NAMESPACE, "notifierRetourParent")
        });
    }

    /**
     * Ce test permet de valider le wsdl syntaxiquement
     */
    @Test
    public void validateWsdl() throws WSDL4ComplexWsdlException, URISyntaxException, IOException {

        final URL wsdlUrl = Thread.currentThread().getContextClassLoader().getResource("jbi/processus-emprunt.wsdl");
        assertNotNull(wsdlUrl);

        final WSDL4ComplexWsdlReader wsdlReader = WSDL4ComplexWsdlFactory.newInstance().newWSDLReader();
        final Description wsdlDescr = wsdlReader.read(wsdlUrl);
        assertNotNull(wsdlDescr);

        assertEquals(3, wsdlDescr.getBindings().get(0).getBindingOperations().size());
    }
}
