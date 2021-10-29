package fr.ausy.traing.petals.services.techniques.openlibrary;

import org.apache.http.client.methods.HttpUriRequest;
import org.ow2.easywsdl.wsdl.api.abstractItf.AbsItfOperation;
import org.ow2.petals.binding.rest.junit.rule.JUnitRestJBIExchange;
import org.ow2.petals.binding.rest.junit.rule.ServiceOperationUnderTest;
import org.ow2.petals.component.framework.util.XMLUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.logging.LogManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

abstract class AbstractTest {

    protected static final Properties COMPONENT_PLACEHOLDERS = new Properties();
    protected static final String NAMESPACE = "http://openlibrary.org/api/1.0";

    static {
        final URL logConfig = AbstractTest.class.getResource("/logging.properties");
        assertNotNull("Logging configuration file not found", logConfig);
        try {
            LogManager.getLogManager().readConfiguration(logConfig.openStream());
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }

        COMPONENT_PLACEHOLDERS.clear();
        COMPONENT_PLACEHOLDERS.setProperty("ausy.training.openlibrary.url", "https://openlibrary.org");
    }

    /**
     * Assertion about an expected JBI error generated from the given REST response.
     * Ne prend pas en compte les données de la requête.
     *
     * @param opUnderTest              The operation under test
     * @param mep                      The JBI exchange pattern
     * @param inXMLPayloadResourceName Name of the resource containing the JBI IN message payload from which XML response can be generated
     * @param uriParameterValues       Values of URI parameters. Can not be {@code null}.
     * @param expectedUri
     * @throws Exception
     */
    public static void assertTemplateUri(final ServiceOperationUnderTest opUnderTest,
                                         final AbsItfOperation.MEPPatternConstants mep,
                                         final String inXMLPayloadResourceName,
                                         final Map<String, String> uriParameterValues,
                                         final String expectedUri) throws Exception {
        assertNotNull("Operation under test is null", opUnderTest);
        assertNotNull("Mep is null", mep);
        assertNotNull("inXMLPayloadResourceName is null", inXMLPayloadResourceName);
        assertNotNull("uriParameterValues is null", uriParameterValues);
        assertNotNull("expectedUri does not be null", expectedUri);
        assertFalse("expectedUri does not be empty", expectedUri.trim().isEmpty());

        try (final InputStream inXMLPayloadResourceStream = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream(inXMLPayloadResourceName)) {
            assertNotNull("XML resource file '" + inXMLPayloadResourceName
                + "' containing the XML input message is not found", inXMLPayloadResourceStream);

            final JUnitRestJBIExchange jbiExchange = new JUnitRestJBIExchange(XMLUtil.loadDocument(inXMLPayloadResourceStream),
                mep.value(),
                uriParameterValues);

            final HttpUriRequest request = opUnderTest.transformRESTRequest(jbiExchange);
            assertEquals("URI does not match", expectedUri, request.getURI().toString());
        }
    }
}
