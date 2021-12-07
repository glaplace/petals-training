package fr.ausy.traing.petals.services.techniques.ajouter.operation;

import fr.ausy.traing.petals.services.techniques.ajouter.Routes;
import fr.ausy.training.petals.bibliotheque.ajouter.technique._1.LivreExiste;
import fr.ausy.training.petals.bibliotheque.ajouter.technique._1.LivreExisteReponse;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;
import org.ow2.petals.camel.helpers.PetalsRouteBuilder;
import org.ow2.petals.camel.helpers.Step;
import org.ow2.petals.components.sql.version_1.ColumnType;
import org.ow2.petals.components.sql.version_1.Result;
import org.ow2.petals.components.sql.version_1.RowType;

import javax.jbi.messaging.MessagingException;
import javax.xml.bind.JAXBException;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * Test de la route Camel Réalisation l'opération « livre existe ».
 */
public class LivreExisteOperationTest extends AbstractOperationTest {
    private LivreExisteOperation service;

    /**
     * Test du cas d'appelavce un format isbn incorrecte.
     *
     * @throws InterruptedException Exception levée en cas d'erreurlors de l'éxécution de la route camel.
     * @throws JAXBException        Exception levée en cas d'erreur de conversion java - xml.
     */
    @Test
    public void isbnKoTest() throws InterruptedException, JAXBException {
        final String isbn = UUID.randomUUID().toString();

        final Exchange exchange = template().send(
            getFrom(Routes.LIVRE_EXISTE),
            ExchangePattern.InOut,
            new Step("Test livre existe") {
                @Override
                public void process(final Exchange exchange) throws JAXBException {
                    final LivreExiste requete = new LivreExiste();
                    requete.setIsbn(isbn);
                    service.marshal(exchange.getIn(), requete);
                }
            });

        assertMockEndpointsSatisfied();
        assertTrue(PetalsRouteBuilder.isJbiFailed(exchange));
        assertNotNull(exchange.getException());
        assertFalse(PetalsRouteBuilder.isJbiFault(exchange.getOut()));

        org.assertj.core.api.Assertions.assertThat(exchange.getException())
            .isNotNull()
            .isInstanceOf(MessagingException.class)
            .hasMessage("L'isbn ne peut être null et doit contenir 13 chiffres [" + isbn + "] est invalide");
    }

    /**
     * Test du cas d'appel avec un  isbn null.
     *
     * @throws InterruptedException Exception levée en cas d'erreurlors de l'éxécution de la route camel.
     * @throws JAXBException        Exception levée en cas d'erreur de conversion java - xml.
     */
    @Test
    public void isbnNullTest() throws InterruptedException, JAXBException {
        final String isbn = UUID.randomUUID().toString();

        final Exchange exchange = template().send(
            getFrom(Routes.LIVRE_EXISTE),
            ExchangePattern.InOut,
            new Step("Test livre existe") {
                @Override
                public void process(final Exchange exchange) throws JAXBException {
                    service.marshal(exchange.getIn(), new LivreExiste());
                }
            });

        assertMockEndpointsSatisfied();
        assertTrue(PetalsRouteBuilder.isJbiFailed(exchange));
        assertNotNull(exchange.getException());
        assertFalse(PetalsRouteBuilder.isJbiFault(exchange.getOut()));

        org.assertj.core.api.Assertions.assertThat(exchange.getException())
            .isNotNull()
            .isInstanceOf(MessagingException.class)
            .hasMessage("L'isbn ne peut être null et doit contenir 13 chiffres [null] est invalide");
    }

    /**
     * Test du cas : livre présent en base.
     *
     * @throws InterruptedException Exception levée en cas d'erreurlors de l'éxécution de la route camel.
     * @throws JAXBException        Exception levée en cas d'erreur de conversion java - xml.
     */
    @Test
    public void livreExisteTest() throws InterruptedException, JAXBException {
        testService(true);
    }

    /**
     * Test du cas : livre inexestant en base.
     *
     * @throws InterruptedException Exception levée en cas d'erreurlors de l'éxécution de la route camel.
     * @throws JAXBException        Exception levée en cas d'erreur de conversion java - xml.
     */
    @Test
    public void livreExistePasTest() throws InterruptedException, JAXBException {
        testService(false);
    }


    /**
     * Test de la route camel en fonction du résultat sçouhaité sur l'existance du livre.
     *
     * @param exist {@code true} si le livre existe, {@code false} sinon
     * @throws InterruptedException Exception levée en cas d'erreurlors de l'éxécution de la route camel.
     * @throws JAXBException        Exception levée en cas d'erreur de conversion java - xml.
     */
    private void testService(final boolean exist) throws InterruptedException, JAXBException {
        creerMockSqlSelect(exist);
        final Exchange exchange = template().send(
            getFrom(Routes.LIVRE_EXISTE),
            ExchangePattern.InOut,
            new Step("Test livre existe") {
                @Override
                public void process(final Exchange exchange) throws JAXBException {
                    final LivreExiste requete = new LivreExiste();
                    requete.setIsbn(ISBN);
                    service.marshal(exchange.getIn(), requete);
                }
            });

        assertMockEndpointsSatisfied();
        assertFalse(PetalsRouteBuilder.isJbiFailed(exchange));
        assertNull(exchange.getException());
        assertFalse(PetalsRouteBuilder.isJbiFault(exchange.getOut()));

        final LivreExisteReponse reponse = service.unmarshal(exchange.getOut(), LivreExisteReponse.class);
        final LivreExisteReponse reponseAttendue = new LivreExisteReponse();
        reponseAttendue.setExiste(exist);
        org.assertj.core.api.Assertions.assertThat(reponse)
            .isNotNull()
            .usingRecursiveComparison()
            .isEqualTo(reponseAttendue);
    }

    /**
     * Création du mock du service SQL Select.
     *
     * @param exist indique si la réponse indique un livre existant ({@code true} ou non {@code false}
     */
    private void creerMockSqlSelect(final boolean exist) {
        final MockEndpoint mock = getTo(Routes.SQL_SELECT);
        mock.expectedMessageCount(1);
        mock.whenAnyExchangeReceived(exchange -> {
            final String sql = service.unmarshal(exchange.getIn(), String.class);
            final String select = "select count(1) from livre where isbn='" + ISBN + "'";

            org.assertj.core.api.Assertions.assertThat(sql)
                .isNotNull()
                .isEqualTo(select);

            final Result result = new Result();
            final RowType row = new RowType();
            result.getRow().add(row);
            final ColumnType col = new ColumnType();
            if (exist) {
                col.setValue("1");
            } else {
                col.setValue("0");
            }
            row.getColumn().add(col);

            service.marshal(exchange.getOut(), result);
        });
    }

    @Override
    protected Collection<String> routesToMock() {
        return Collections.singletonList(Routes.LIVRE_EXISTE);
    }

    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        service = new LivreExisteOperation();
        return service;
    }

    @Override
    public AbstractOperation getService() {
        return service;
    }
}
