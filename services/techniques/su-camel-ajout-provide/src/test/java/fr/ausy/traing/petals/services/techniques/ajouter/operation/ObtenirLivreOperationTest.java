package fr.ausy.traing.petals.services.techniques.ajouter.operation;

import fr.ausy.traing.petals.services.techniques.ajouter.Routes;
import fr.ausy.training.petals.bibliotheque.ajouter.technique._1.LivreInconnu;
import fr.ausy.training.petals.bibliotheque.ajouter.technique._1.ObtenirLivre;
import fr.ausy.training.petals.bibliotheque.ajouter.technique._1.ObtenirLivreReponse;
import fr.ausy.training.petals.modele.bibliotheque._1.Livre;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;
import org.openlibrary.api._1.AuteurInconnu;
import org.openlibrary.api._1.IsbnInconnu;
import org.openlibrary.api._1.ObtenirAuteur;
import org.openlibrary.api._1.ObtenirAuteurReponse;
import org.openlibrary.api._1.ObtenirParIsbn;
import org.openlibrary.api._1.ObtenirParIsbnReponse;
import org.ow2.petals.camel.helpers.PetalsRouteBuilder;
import org.ow2.petals.camel.helpers.Step;

import javax.jbi.messaging.MessagingException;
import javax.xml.bind.JAXBException;

import java.util.Collection;
import java.util.Collections;

/**
 * Test de la route camel réalisant l'opération « Obtenir livre ».
 */
public class ObtenirLivreOperationTest extends AbstractOperationTest {

    private ObtenirLivreOperation service;

    /**
     * Test du cas passant, le livre et l'auteur sont bien retrouvés.
     *
     * @throws InterruptedException Exception levée en cas d'erreur lors de l'éxécution de la route camel.
     * @throws JAXBException        Exception levée en cas d'erreur de conversion java - xml.
     */
    @Test
    public void obtenirLivreExistantTest() throws InterruptedException, JAXBException {
        creerObtenirLivreMock(true);
        creerObtenirAuteurMock();

        final Exchange exchange = template().send(
            getFrom(Routes.OBTENIR_LIVRE),
            ExchangePattern.InOut,
            new Step("Test Obtenir livre") {
                @Override
                public void process(final Exchange exchange) throws JAXBException {
                    final ObtenirLivre requete = new ObtenirLivre();
                    requete.setIsbn(ISBN);
                    service.marshal(exchange.getIn(), requete);
                }
            });

        assertMockEndpointsSatisfied();
        assertFalse(PetalsRouteBuilder.isJbiFailed(exchange));
        assertNull(exchange.getException());
        assertFalse(PetalsRouteBuilder.isJbiFault(exchange.getOut()));

        final ObtenirLivreReponse reponse = service.unmarshal(exchange.getOut(), ObtenirLivreReponse.class);
        final ObtenirLivreReponse reponseAttendue = new ObtenirLivreReponse();
        reponseAttendue.setLivre(creerLivre(true));
        // corriger l'auteur qui est celui attendu avec Obtenir auteur.
        reponseAttendue.getLivre().setAuteur("David Gemmell");

        org.assertj.core.api.Assertions.assertThat(reponse)
            .isNotNull()
            .usingRecursiveComparison()
            .isEqualTo(reponseAttendue);
    }

    /**
     * Test du cas où l'isbn n'existe sur openlibrary.
     *
     * @throws InterruptedException Exception levée en cas d'erreur lors de l'éxécution de la route camel.
     * @throws JAXBException        Exception levée en cas d'erreur de conversion java - xml.
     */
    @Test
    public void obtenirLivreInconnuTest() throws InterruptedException, JAXBException {
        final MockEndpoint mock = getTo(Routes.OBTENIR_AUTEUR_OL);
        mock.expectedMessageCount(0);

        final MockEndpoint mockLivre = getTo(Routes.OBTENIR_LIVRE_OL);
        mockLivre.expectedMessageCount(1);
        mockLivre.whenAnyExchangeReceived(exchange -> {
            final ObtenirParIsbn requete = service.unmarshal(exchange.getIn(), ObtenirParIsbn.class);
            final ObtenirParIsbn requeteAttendue = new ObtenirParIsbn();
            requeteAttendue.setIsbn(Long.parseLong(ISBN));

            org.assertj.core.api.Assertions.assertThat(requete)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(requeteAttendue);

            final IsbnInconnu fault = new IsbnInconnu();
            fault.setIsbn(Long.parseLong(ISBN));
            setJbiFault(service.getMarshalling(), exchange, fault);
        });

        final Exchange exchange = template().send(
            getFrom(Routes.OBTENIR_LIVRE),
            ExchangePattern.InOut,
            new Step("Test Obtenir livre") {
                @Override
                public void process(final Exchange exchange) throws JAXBException {
                    final ObtenirLivre requete = new ObtenirLivre();
                    requete.setIsbn(ISBN);
                    service.marshal(exchange.getIn(), requete);
                }
            });

        assertMockEndpointsSatisfied();
        assertTrue(PetalsRouteBuilder.isJbiFailed(exchange));
        assertNull(exchange.getException());
        assertTrue(PetalsRouteBuilder.isJbiFault(exchange.getOut()));

        final LivreInconnu fault = service.unmarshal(exchange.getOut(), LivreInconnu.class);
        final LivreInconnu faultAttendue = new LivreInconnu();
        faultAttendue.setIsbn(ISBN);

        org.assertj.core.api.Assertions.assertThat(fault)
            .isNotNull()
            .usingRecursiveComparison()
            .isEqualTo(faultAttendue);
    }

    /**
     * Cas, peu probable, d'une incohérence sur l'api qui ne possège pas l'auteur du livre.
     *
     * @throws InterruptedException Exception levée en cas d'erreur lors de l'éxécution de la route camel.
     */
    @Test
    public void auteurInconnuTest() throws InterruptedException {
        creerObtenirLivreMock(true);
        final MockEndpoint mock = getTo(Routes.OBTENIR_AUTEUR_OL);
        mock.expectedMessageCount(1);
        mock.whenAnyExchangeReceived(exchange -> {
            final ObtenirAuteur requete = service.unmarshal(exchange.getIn(), ObtenirAuteur.class);
            final ObtenirAuteur requeteAttendue = new ObtenirAuteur();
            requeteAttendue.setId(AUTEUR_REF);

            org.assertj.core.api.Assertions.assertThat(requete)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(requeteAttendue);

            final AuteurInconnu auteurInconnu = new AuteurInconnu();
            auteurInconnu.setId(AUTEUR_REF);
            setJbiFault(service.getMarshalling(), exchange, auteurInconnu);
        });

        final Exchange exchange = template().send(
            getFrom(Routes.OBTENIR_LIVRE),
            ExchangePattern.InOut,
            new Step("Test Obtenir livre") {
                @Override
                public void process(final Exchange exchange) throws JAXBException {
                    final ObtenirLivre requete = new ObtenirLivre();
                    requete.setIsbn(ISBN);
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
            .hasMessage("Erreur Technique auteur inconnu [" + AUTEUR_REF + "]");
    }

    /**
     * Test du cas où le livre n'a pas d'auteur indiqué.
     * Dans ce cas pas d'appel de « obtenir auteur »
     *
     * @throws InterruptedException Exception levée en cas d'erreur lors de l'éxécution de la route camel.
     * @throws JAXBException        Exception levée en cas d'erreur de conversion java - xml.
     */
    @Test
    public void livreSansAuteurTest() throws InterruptedException, JAXBException {
        creerObtenirLivreMock(false);
        final MockEndpoint mock = getTo(Routes.OBTENIR_AUTEUR_OL);
        mock.expectedMessageCount(0);

        final Exchange exchange = template().send(
            getFrom(Routes.OBTENIR_LIVRE),
            ExchangePattern.InOut,
            new Step("Test Obtenir livre") {
                @Override
                public void process(final Exchange exchange) throws JAXBException {
                    final ObtenirLivre requete = new ObtenirLivre();
                    requete.setIsbn(ISBN);
                    service.marshal(exchange.getIn(), requete);
                }
            });

        assertMockEndpointsSatisfied();
        assertFalse(PetalsRouteBuilder.isJbiFailed(exchange));
        assertNull(exchange.getException());
        assertFalse(PetalsRouteBuilder.isJbiFault(exchange.getOut()));

        final ObtenirLivreReponse reponse = service.unmarshal(exchange.getOut(), ObtenirLivreReponse.class);
        final ObtenirLivreReponse reponseAttendue = new ObtenirLivreReponse();
        reponseAttendue.setLivre(creerLivre(false));

        org.assertj.core.api.Assertions.assertThat(reponse)
            .isNotNull()
            .usingRecursiveComparison()
            .isEqualTo(reponseAttendue);
    }

    /**
     * Mock du service obtenir livre
     */
    private void creerObtenirAuteurMock() {
        final MockEndpoint mock = getTo(Routes.OBTENIR_AUTEUR_OL);
        mock.expectedMessageCount(1);
        mock.whenAnyExchangeReceived(exchange -> {

            final ObtenirAuteur requete = service.unmarshal(exchange.getIn(), ObtenirAuteur.class);
            final ObtenirAuteur requeteAttendue = new ObtenirAuteur();
            requeteAttendue.setId(AUTEUR_REF);

            org.assertj.core.api.Assertions.assertThat(requete)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(requeteAttendue);

            final ObtenirAuteurReponse reponse = new ObtenirAuteurReponse();
            reponse.setNom("David Gemmell");
            service.marshal(exchange.getOut(), reponse);
        });
    }

    private void creerObtenirLivreMock() {
        creerObtenirLivreMock(true);
    }

    /**
     * Mock du service obtenir auteur.
     *
     * @param avecAuteur
     */
    private void creerObtenirLivreMock(final boolean avecAuteur) {
        final MockEndpoint mock = getTo(Routes.OBTENIR_LIVRE_OL);
        mock.expectedMessageCount(1);
        mock.whenAnyExchangeReceived(exchange -> {
            final ObtenirParIsbn requete = service.unmarshal(exchange.getIn(), ObtenirParIsbn.class);
            final ObtenirParIsbn requeteAttendue = new ObtenirParIsbn();
            requeteAttendue.setIsbn(Long.parseLong(ISBN));

            org.assertj.core.api.Assertions.assertThat(requete)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(requeteAttendue);

            final ObtenirParIsbnReponse reponse = new ObtenirParIsbnReponse();
            reponse.setLivre(creerLivre(avecAuteur));
            service.marshal(exchange.getOut(), reponse);
        });
    }


    @Override
    protected Collection<String> routesToMock() {
        return Collections.singletonList(Routes.OBTENIR_LIVRE);
    }

    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        service = new ObtenirLivreOperation();
        return service;
    }

    @Override
    public AbstractOperation getService() {
        return service;
    }
}
