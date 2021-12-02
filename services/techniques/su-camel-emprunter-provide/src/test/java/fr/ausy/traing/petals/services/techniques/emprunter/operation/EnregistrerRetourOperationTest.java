package fr.ausy.traing.petals.services.techniques.emprunter.operation;

import fr.ausy.traing.petals.services.techniques.emprunter.Routes;
import fr.ausy.training.petals.bibliotheque.emprunter.technique._1.RetournerEmprunt;
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

import java.util.Collection;
import java.util.Collections;
import java.util.Random;

/**
 * Classe de test de la route camel réalisant l'opération de retour d'un livre.
 */
public class EnregistrerRetourOperationTest extends AbstractOperationTest {
    private static final int UTILISATEUR_ID = Math.abs(new Random().nextInt());
    private EnregistrerRetourOperation service;

    @Test
    public void retourTest() throws InterruptedException {
        creerMockSqlUpdate(true);
        final Exchange exchange = template().send(getFrom(Routes.RETOURNER_EMPRUNT), ExchangePattern.InOnly, new Step("Test retourner livre") {
            @Override
            public void process(final Exchange exchange) throws Exception {
                final RetournerEmprunt requete = new RetournerEmprunt();
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
    public void retourKoTest() throws InterruptedException {
        creerMockSqlUpdate(false);
        final Exchange exchange = template().send(getFrom(Routes.RETOURNER_EMPRUNT), ExchangePattern.InOnly, new Step("Test retourner livre") {
            @Override
            public void process(final Exchange exchange) throws Exception {
                final RetournerEmprunt requete = new RetournerEmprunt();
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
            .hasMessage("Erreur à la mise à jour, mauvais nombre de ligne affectée [1337]");
    }

    /**
     * Mock du service SQL d'insertion.
     */
    private void creerMockSqlUpdate(final boolean resultOk) {
        final MockEndpoint mock = getTo(Routes.SQL_UPDATE);
        mock.expectedMessageCount(1);

        mock.whenAnyExchangeReceived(exchange -> {
            final String sql = service.unmarshal(exchange.getIn(), String.class);
            final String select = "update pret set RENDU=true where livre_id = " + LIVRE_ID + " and utilisateur_id = " + UTILISATEUR_ID;

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
        return Collections.singletonList(Routes.RETOURNER_EMPRUNT);
    }

    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        service = new EnregistrerRetourOperation();
        return service;
    }

    @Override
    public AbstractOperation getService() {
        return service;
    }
}
