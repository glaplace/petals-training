package fr.ausy.traing.petals.services.techniques.openlibrary;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.ClassRule;
import org.junit.Test;
import org.ow2.petals.binding.rest.junit.rule.ServiceOperationUnderTest;
import org.ow2.petals.component.framework.api.Message;

import javax.ws.rs.core.MediaType;
import javax.xml.namespace.QName;

import java.util.HashMap;
import java.util.Map;

import static org.ow2.petals.binding.rest.junit.Assert.assertJBIFault;
import static org.ow2.petals.binding.rest.junit.Assert.assertJBIOutResponse;

/**
 * Classe de test de la configuration JBI de l'opération « obtenir-auteur ».
 */
public class ObtenirAuteurOperationTest extends AbstractTest {

    private static final QName OPERATION = new QName(NAMESPACE, "obtenir-auteur");

    private static final Map<String, String> URI_PARAMETERS;
    private static final String RESOURCES_HOME = "auteur";
    private static final String JBI_REQUEST_XML = "jbi-request.xml";

    static {
        URI_PARAMETERS = new HashMap<>();
        URI_PARAMETERS.put("id", "OL8027105A");
    }

    @ClassRule
    public static final ServiceOperationUnderTest OPERATION_UNDER_TEST = new ServiceOperationUnderTest(OPERATION, COMPONENT_PLACEHOLDERS);

    /**
     * Test du cas passant.
     *
     * @throws Exception levée en cas d'erreur
     */
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

    /**
     * Validation de l'uri générée.
     *
     * @throws Exception levée en cas d'erreur
     */
    @Test
    public void validerUri() throws Exception {
        assertTemplateUri(
            OPERATION_UNDER_TEST,
            Message.MEPConstants.IN_OUT_PATTERN,
            RESOURCES_HOME + "/jbi-request.xml",
            URI_PARAMETERS,
            "https://openlibrary.org/authors/OL8027105A.json"
        );
    }

    /**
     * Test de la configuration en cas d'erreur 404.
     *
     * @throws Exception levée en cas d'erreur
     */
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
