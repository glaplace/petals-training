package fr.ausy.traing.petals.services.techniques.emprunter.operation;

import fr.ausy.traing.petals.services.techniques.emprunter.Routes;
import fr.ausy.training.petals.bibliotheque.emprunter.technique._1.AjouterEmprunt;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;
import org.ow2.petals.camel.helpers.PetalsRouteBuilder;
import org.ow2.petals.camel.helpers.Step;
import org.ow2.petals.components.sql.version_1.ObjectFactory;

import javax.jbi.messaging.MessagingException;
import javax.xml.bind.JAXBElement;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.Random;

/**
 * Test de la route camel réalisant l'opération « Ajouter emprunt».
 */
public class AjouterEmpruntOperationTest extends AbstractOperationTest {
    private static final int UTILISATEUR_ID = Math.abs(new Random().nextInt());
    private AjouterEmpruntOperation service;

    @Test
    public void ajouterEmpruntTest() throws InterruptedException {
        creerMockSqlInsert(true);
        final Exchange exchange = template().send(getFrom(Routes.AJOUTER_EMPRUNT), ExchangePattern.InOnly, new Step("Test ajouter emprunt livre") {
            @Override
            public void process(final Exchange exchange) throws Exception {
                final AjouterEmprunt requete = new AjouterEmprunt();
                requete.setLivre(LIVRE_ID);
                requete.setUtilisateur(UTILISATEUR_ID);
                service.marshal(exchange.getIn(), requete);
            }
        });

        assertMockEndpointsSatisfied();
        assertFalse(PetalsRouteBuilder.isJbiFailed(exchange));
        assertNull(exchange.getException());
        assertFalse(PetalsRouteBuilder.isJbiFault(exchange.getOut()));

    }

    @Test
    public void ajouterEmpruntKoTest() throws InterruptedException {
        creerMockSqlInsert(false);
        final Exchange exchange = template().send(getFrom(Routes.AJOUTER_EMPRUNT), ExchangePattern.InOnly, new Step("Test ajouter emprunt livre") {
            @Override
            public void process(final Exchange exchange) throws Exception {
                final AjouterEmprunt requete = new AjouterEmprunt();
                requete.setLivre(LIVRE_ID);
                requete.setUtilisateur(UTILISATEUR_ID);
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
            .hasMessage("Erreur à l'insertion mauvais nombre de ligne affectée [1337]");
    }

    /**
     * Mock du service SQL d'insertion.
     */
    private void creerMockSqlInsert(final boolean resultOk) {
        final MockEndpoint mock = getTo(Routes.SQL_INSERT);
        mock.expectedMessageCount(1);

        mock.whenAnyExchangeReceived(exchange -> {
            final String sql = service.unmarshal(exchange.getIn(), String.class);
            final String select = "insert into pret (UTILISATEUR_ID, LIVRE_ID, DATE_PRET, DUREE_PRET, RENDU, NB_RELANCE) values (" + LIVRE_ID
                + ", " + UTILISATEUR_ID
                + ", '" + LocalDate.now().format(DateTimeFormatter.ISO_DATE)
                + "', " + 42
                + ", false, 0)";

            org.assertj.core.api.Assertions.assertThat(sql)
                .isNotNull()
                .isEqualTo(select);

            final ObjectFactory of = new ObjectFactory();
            final JAXBElement<String> reponse;
            if (resultOk) {
                reponse = of.createUpdated("1");
            } else {
                reponse = of.createUpdated("1337");
            }
            service.marshal(exchange.getOut(), reponse);
        });
    }

    @Override
    protected Collection<String> routesToMock() {
        return Collections.singletonList(Routes.AJOUTER_EMPRUNT);
    }

    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        service = new AjouterEmpruntOperation();
        final Properties properties = new Properties();
        properties.setProperty(AjouterEmpruntOperation.DUREE_EMPRUNT_PLACEHOLER, "42");
        service.onPlaceHolderValuesReloaded(properties);
        return service;
    }

    @Override
    public AbstractOperation getService() {
        return service;
    }
}
