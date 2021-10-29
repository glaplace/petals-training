package fr.ausy.traing.petals.services.techniques.openlibrary;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.ow2.petals.binding.rest.junit.rule.ServiceOperationUnderTest;
import org.ow2.petals.component.framework.api.Message;

import javax.ws.rs.core.MediaType;
import javax.xml.namespace.QName;

import java.util.HashMap;
import java.util.Map;

import static org.ow2.petals.binding.rest.junit.Assert.assertJBIFault;
import static org.ow2.petals.binding.rest.junit.Assert.assertJBIOutResponse;

public class ObtenirLivreOperationTest extends AbstractTest {

    private static final QName OPERATION = new QName(NAMESPACE, "obtenir-par-isbn");

    private static final Map<String, String> URI_PARAMETERS;
    private static final String RESOURCES_HOME = "livre";
    private static final String JBI_REQUEST_XML = "jbi-request.xml";

    static {
        URI_PARAMETERS = new HashMap<>();
        URI_PARAMETERS.put("isbn", "9782811202569");
    }

    @ClassRule
    public static final ServiceOperationUnderTest OPERATION_UNDER_TEST = new ServiceOperationUnderTest(OPERATION, COMPONENT_PLACEHOLDERS);

    @Test
    public void nominal() throws Exception {
        assertJBIOutResponse(RESOURCES_HOME + "/expected-response.xml",
            OPERATION_UNDER_TEST,
            HttpStatus.OK_200,
            HttpStatus.getCode(HttpStatus.OK_200).getMessage(),
            RESOURCES_HOME + "/api-response.json",
            MediaType.APPLICATION_JSON_TYPE,
            URI_PARAMETERS);
    }

    @Test
    public void validerUri() throws Exception {
        assertTemplateUri(
            OPERATION_UNDER_TEST,
            Message.MEPConstants.IN_OUT_PATTERN,
            RESOURCES_HOME + "/jbi-request.xml",
            URI_PARAMETERS,
            "https://openlibrary.org/isbn/9782811202569.json"
        );
    }

    @Test
    public void erreur404Test() throws Exception {
        final int httpCode = 404;
        assertJBIFault(
            RESOURCES_HOME + "/jbi-response-erreur-404.xml",
            OPERATION_UNDER_TEST,
            Message.MEPConstants.IN_OUT_PATTERN,
            httpCode,
            HttpStatus.getCode(httpCode).getMessage(),
            RESOURCES_HOME + "/api-response.json",
            MediaType.APPLICATION_JSON_TYPE,
            RESOURCES_HOME + "/jbi-request.xml",
            URI_PARAMETERS);
    }

}
