package fr.ausy.traing.petals.services.metiers.livre.operation;

import fr.ausy.traing.petals.services.metiers.livre.Routes;
import fr.ausy.traing.petals.services.metiers.livre.sql.SQLUtils;
import fr.ausy.training.petals.modele.bibliotheque._1.Livre;
import fr.ausy.training.petals.modele.bibliotheque.livre._1.LivreInconnu;
import fr.ausy.training.petals.modele.bibliotheque.livre._1.Modifier;
import fr.ausy.training.petals.modele.bibliotheque.livre._1.Obtenir;
import fr.ausy.training.petals.modele.bibliotheque.livre._1.ObtenirReponse;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;
import org.ow2.petals.camel.helpers.PetalsRouteBuilder;
import org.ow2.petals.camel.helpers.Step;

import javax.jbi.messaging.MessagingException;
import javax.xml.bind.JAXBException;

import java.util.Collection;
import java.util.Collections;

/**
 * Test de la route camel réalisant l'opération « modifier ».
 */
public class ModifierLivreOperationTest extends AbstractOperationTest {
    private ModifierLivreOperation service;

    @Test
    public void miseAjourLivreTest() throws InterruptedException, JAXBException {
        creerMockSqlUpdate();
        creerMockObtenirLivre();
        final Exchange exchange = template().send(getFrom(Routes.MODIFIER_LIVRE), ExchangePattern.RobustInOnly, new Step("Test modifier livre") {
            @Override
            public void process(final Exchange exchange) throws Exception {
                final Modifier modifier = new Modifier();
                modifier.setLivre(creerLivre());
                service.marshal(exchange.getIn(), modifier);
            }
        });

        assertMockEndpointsSatisfied();
        assertFalse(PetalsRouteBuilder.isJbiFailed(exchange));
        assertNull(exchange.getException());
        assertFalse(PetalsRouteBuilder.isJbiFault(exchange.getOut()));
    }

    @Test
    public void miseAjourLivreInconnuTest() throws InterruptedException, JAXBException {
        creerMockSqlUpdate(0);
        creerMockObtenirLivre(true);
        final Exchange exchange = template().send(getFrom(Routes.MODIFIER_LIVRE), ExchangePattern.RobustInOnly, new Step("Test modifier livre") {
            @Override
            public void process(final Exchange exchange) throws Exception {
                final Modifier modifier = new Modifier();
                modifier.setLivre(creerLivre());
                service.marshal(exchange.getIn(), modifier);
            }
        });

        assertMockEndpointsSatisfied();
        assertTrue(PetalsRouteBuilder.isJbiFailed(exchange));
        assertTrue(PetalsRouteBuilder.isJbiFault(exchange.getOut()));
        assertNull(exchange.getException());

        final LivreInconnu livreInconnu = service.unmarshal(exchange.getOut(), LivreInconnu.class);
        final LivreInconnu attendu = new LivreInconnu();
        attendu.setIdentifiant(LIVRE_ID);
    }

    @Test
    public void obtenirLivreNeRetournePasDeLivreTest() throws InterruptedException, JAXBException {
        creerMockSqlUpdate(0);
        creerMockObtenirLivre(false, false);
        final Exchange exchange = template().send(getFrom(Routes.MODIFIER_LIVRE), ExchangePattern.RobustInOnly, new Step("Test modifier livre") {
            @Override
            public void process(final Exchange exchange) throws Exception {
                final Modifier modifier = new Modifier();
                modifier.setLivre(creerLivre());
                service.marshal(exchange.getIn(), modifier);
            }
        });

        assertMockEndpointsSatisfied();
        assertTrue(PetalsRouteBuilder.isJbiFailed(exchange));
        assertTrue(PetalsRouteBuilder.isJbiFault(exchange.getOut()));
        assertNull(exchange.getException());

        final LivreInconnu livreInconnu = service.unmarshal(exchange.getOut(), LivreInconnu.class);
        final LivreInconnu attendu = new LivreInconnu();
        attendu.setIdentifiant(LIVRE_ID);
    }

    @Test
    public void obtenirLivreFaultInconnueTest() throws InterruptedException {
        creerMockSqlUpdate(0);
        final MockEndpoint mock = getTo(Routes.OBTENIR_LIVRE_INTERNE);
        mock.expectedMessageCount(1);
        mock.whenAnyExchangeReceived(exchange -> {
            assertObtenirLivreRequest(exchange);
            // Utilisation d'un objet qui n'est pas LivreInconnu
            setJbiFault(service.getMarshalling(), exchange, new Obtenir());
        });

        final Exchange exchange = template().send(getFrom(Routes.MODIFIER_LIVRE), ExchangePattern.RobustInOnly, new Step("Test modifier livre") {
            @Override
            public void process(final Exchange exchange) throws Exception {
                final Modifier modifier = new Modifier();
                modifier.setLivre(creerLivre());
                service.marshal(exchange.getIn(), modifier);
            }
        });

        assertMockEndpointsSatisfied();
        assertTrue(PetalsRouteBuilder.isJbiFailed(exchange));
        assertFalse(PetalsRouteBuilder.isJbiFault(exchange.getOut()));
        assertNotNull(exchange.getException());

        org.assertj.core.api.Assertions.assertThat(exchange.getException())
            .isNotNull()
            .isInstanceOf(MessagingException.class)
            .hasMessageStartingWith("Fault inconnue du service ObtenirLivre ")
            .hasMessageEndingWith("[identifiant=0]");

    }

    private void creerMockSqlUpdate() {
        creerMockSqlUpdate(1);
    }

    private void creerMockSqlUpdate(final int nbAppel) {
        final MockEndpoint mock = getTo(Routes.SQL_UPDATE);
        mock.expectedMessageCount(nbAppel);
        mock.whenAnyExchangeReceived(exchange -> {
            final String sql = service.unmarshal(exchange.getIn(), String.class);
            final Livre livre = creerLivre();
            final String attendue = "update LIVRE set \n" +
                "    TITRE=" + SQLUtils.getString(livre.getTitre()) + ",\n" +
                "    RESUME=" + SQLUtils.getString(livre.getResume()) + ",\n" +
                "    NB_PAGE=" + livre.getNbPage() + ",\n" +
                "    ISBN=" + SQLUtils.getString(livre.getIsbn()) + ",\n" +
                "    AUTEUR=" + SQLUtils.getString(livre.getAuteur()) + ",\n" +
                "    LANGUE=" + SQLUtils.getString(livre.getLangue()) + ",\n" +
                "    ANNEE_PUBLICATION=" + livre.getAnneePublication() +
                "\nwhere LIVRE_ID=" + livre.getLivreId();

            org.assertj.core.api.Assertions.assertThat(sql)
                .isNotNull()
                .isEqualTo(attendue);
            // INOnly pas de réponse
        });
    }

    private void creerMockObtenirLivre() {
        creerMockObtenirLivre(false, true);
    }

    private void creerMockObtenirLivre(final boolean returnFault) {
        creerMockObtenirLivre(returnFault, true);
    }

    private void creerMockObtenirLivre(final boolean returnFault, final boolean returnBook) {
        final MockEndpoint mock = getTo(Routes.OBTENIR_LIVRE_INTERNE);
        mock.expectedMessageCount(1);
        mock.whenAnyExchangeReceived(exchange -> {
            assertObtenirLivreRequest(exchange);

            if (returnFault) {
                final LivreInconnu livreInconnu = new LivreInconnu();
                livreInconnu.setIdentifiant(LIVRE_ID);
                setJbiFault(service.getMarshalling(), exchange, livreInconnu);
            } else {
                final ObtenirReponse obtenirReponse = new ObtenirReponse();
                if (returnBook) {
                    obtenirReponse.setLivre(creerLivre());
                }
                service.marshal(exchange.getOut(), obtenirReponse);
            }
        });
    }

    /**
     * Assertion validant la requête sur le service « Obtenir Livre ».
     *
     * @param exchange echange contenant la requête
     * @throws JAXBException exception levée en cas d'erreur de convertion java - xml
     */
    private void assertObtenirLivreRequest(final Exchange exchange) throws JAXBException {
        final Obtenir obtenir = service.unmarshal(exchange.getIn(), Obtenir.class);
        final Obtenir attendu = new Obtenir();
        attendu.setIdentifiant(LIVRE_ID);

        org.assertj.core.api.Assertions.assertThat(obtenir)
            .isNotNull()
            .usingRecursiveComparison()
            .isEqualTo(attendu);
    }

    @Override
    protected Collection<String> routesToMock() {
        return Collections.singletonList(Routes.MODIFIER_LIVRE);
    }

    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        service = new ModifierLivreOperation();
        return service;
    }

    @Override
    public AbstractOperation getService() {
        return service;
    }
}
