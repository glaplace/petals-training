package fr.ausy.traing.petals.services.techniques.openlibrary;

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
import static org.ow2.petals.binding.rest.junit.Assert.assertWsdlCompliance;

public class WsdlValidatorTest extends AbstractTest {

    /**
     * Validate syntax of the service contract
     */
    @Test
    public void validateWsdl() throws WSDL4ComplexWsdlException, URISyntaxException, IOException {

        final URL wsdlUrl = Thread.currentThread().getContextClassLoader().getResource("jbi/openlibrary.wsdl");
        assertNotNull(wsdlUrl);

        final WSDL4ComplexWsdlReader wsdlReader = WSDL4ComplexWsdlFactory.newInstance().newWSDLReader();
        final Description wsdlDescr = wsdlReader.read(wsdlUrl);
        assertNotNull(wsdlDescr);
        assertEquals(2, wsdlDescr.getBindings().get(0).getBindingOperations().size());
    }

    /**
     * Validate JBI descriptor against WSDL
     */
    @Test
    public void validateJbiAgainstWsdl() throws Exception {
        assertWsdlCompliance(COMPONENT_PLACEHOLDERS);
    }
}
