package fr.ausy.traing.petals.services.metiers.livre.operation;

import fr.ausy.traing.petals.services.metiers.livre.Routes;
import fr.ausy.traing.petals.services.metiers.livre.sql.SQLUtils;
import fr.ausy.training.petals.modele.biblotheque._1.Livre;
import fr.ausy.training.petals.modele.biblotheque.livre._1.Creer;
import fr.ausy.training.petals.modele.biblotheque.livre._1.CreerReponse;
import fr.ausy.training.petals.modele.biblotheque.livre._1.Rechercher;
import fr.ausy.training.petals.modele.biblotheque.livre._1.RechercherReponse;
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
 * Tests de la route camel réalisant l'opération Créer.
 */
public class CreerLivreOperationTest extends AbstractOperationTest {
    private CreerLivreOperation service;

    @Test
    public void creerLivreExistantTest() throws InterruptedException, JAXBException {
        creerMockRechercherLivre(1);
        creerMockSqlInsert(0);

        final Exchange exchange = template().send(getFrom(Routes.CREER_LIVRE), ExchangePattern.InOut, new Step("Test creer livre") {
            @Override
            public void process(final Exchange exchange) throws Exception {
                final Creer creer = new Creer();
                creer.setLivre(creerLivre(false));
                service.marshal(exchange.getIn(), creer);
            }
        });

        assertMockEndpointsSatisfied();
        assertFalse(PetalsRouteBuilder.isJbiFailed(exchange));
        assertNull(exchange.getException());
        assertFalse(PetalsRouteBuilder.isJbiFault(exchange.getOut()));

        final CreerReponse reponse = service.unmarshal(exchange.getOut(), CreerReponse.class);
        final CreerReponse reponseAttendue = new CreerReponse();
        reponseAttendue.setLivre(creerLivre());

        org.assertj.core.api.Assertions.assertThat(reponse)
            .isNotNull()
            .usingRecursiveComparison()
            .isEqualTo(reponseAttendue);
    }

    @Test
    public void livreAcreerTest() throws InterruptedException, JAXBException {
        creerMockRechercherLivre(2);
        creerMockSqlInsert(1);

        final Exchange exchange = template().send(getFrom(Routes.CREER_LIVRE), ExchangePattern.InOut, new Step("Test creer livre") {
            @Override
            public void process(final Exchange exchange) throws Exception {
                final Creer creer = new Creer();
                creer.setLivre(creerLivre(false));
                service.marshal(exchange.getIn(), creer);
            }
        });

        assertMockEndpointsSatisfied();
        assertFalse(PetalsRouteBuilder.isJbiFailed(exchange));
        assertNull(exchange.getException());
        assertFalse(PetalsRouteBuilder.isJbiFault(exchange.getOut()));

        final CreerReponse reponse = service.unmarshal(exchange.getOut(), CreerReponse.class);
        final CreerReponse reponseAttendue = new CreerReponse();
        reponseAttendue.setLivre(creerLivre());

        org.assertj.core.api.Assertions.assertThat(reponse)
            .isNotNull()
            .usingRecursiveComparison()
            .isEqualTo(reponseAttendue);
    }

    @Test
    public void rechercherLivreFaultTest() throws InterruptedException, JAXBException {
        final MockEndpoint mock = getTo(Routes.RECHERCHER_LIVRE_INTERNE);
        mock.expectedMessageCount(1);
        mock.whenAnyExchangeReceived(exchange -> {
            assertionRechercherLivre(exchange);
            // Objet quelconque en fault, juste avec un @XmlRootElement
            setJbiFault(service.getMarshalling(), exchange, new Rechercher());
        });
        creerMockSqlInsert(0);

        final Exchange exchange = template().send(getFrom(Routes.CREER_LIVRE), ExchangePattern.InOut, new Step("Test creer livre") {
            @Override
            public void process(final Exchange exchange) throws Exception {
                final Creer creer = new Creer();
                creer.setLivre(creerLivre(false));
                service.marshal(exchange.getIn(), creer);
            }
        });

        assertMockEndpointsSatisfied();
        assertTrue(PetalsRouteBuilder.isJbiFailed(exchange));
        assertNotNull(exchange.getException());
        assertFalse(PetalsRouteBuilder.isJbiFault(exchange.getOut()));

        org.assertj.core.api.Assertions.assertThat(exchange.getException())
            .isNotNull()
            .isInstanceOf(MessagingException.class)
            .hasMessageStartingWith("Fault inattendue du service Rechercher livre : ");
    }

    @Test
    public void rechercherLivreSecondAppelFaultTest() throws InterruptedException, JAXBException {
        final MockEndpoint mock = getTo(Routes.RECHERCHER_LIVRE_INTERNE);
        mock.expectedMessageCount(2);
        mock.whenExchangeReceived(1, exchange -> {
            assertionRechercherLivre(exchange);
            service.marshal(exchange.getOut(), new RechercherReponse());
        });
        mock.whenExchangeReceived(2, exchange -> {
            assertionRechercherLivre(exchange);
            // Objet quelconque en fault, juste avec un @XmlRootElement
            setJbiFault(service.getMarshalling(), exchange, new Rechercher());
        });
        creerMockSqlInsert(1);

        final Exchange exchange = template().send(getFrom(Routes.CREER_LIVRE), ExchangePattern.InOut, new Step("Test creer livre") {
            @Override
            public void process(final Exchange exchange) throws Exception {
                final Creer creer = new Creer();
                creer.setLivre(creerLivre(false));
                service.marshal(exchange.getIn(), creer);
            }
        });

        assertMockEndpointsSatisfied();
        assertTrue(PetalsRouteBuilder.isJbiFailed(exchange));
        assertNotNull(exchange.getException());
        assertFalse(PetalsRouteBuilder.isJbiFault(exchange.getOut()));

        org.assertj.core.api.Assertions.assertThat(exchange.getException())
            .isNotNull()
            .isInstanceOf(MessagingException.class)
            .hasMessageStartingWith("Fault inattendue, impossible de rechercher le livre nouvellement créé : ");
    }

    @Test
    public void secondeRechercheNeRetournePasDeLivreTest() throws InterruptedException, JAXBException {
        final MockEndpoint mock = getTo(Routes.RECHERCHER_LIVRE_INTERNE);
        mock.expectedMessageCount(2);
        mock.whenExchangeReceived(1, exchange -> {
            assertionRechercherLivre(exchange);
            service.marshal(exchange.getOut(), new RechercherReponse());
        });
        mock.whenExchangeReceived(2, exchange -> {
            assertionRechercherLivre(exchange);
            // pas de livre trouvé.
            service.marshal(exchange.getOut(), new RechercherReponse());
        });
        creerMockSqlInsert(1);

        final Exchange exchange = template().send(getFrom(Routes.CREER_LIVRE), ExchangePattern.InOut, new Step("Test creer livre") {
            @Override
            public void process(final Exchange exchange) throws Exception {
                final Creer creer = new Creer();
                creer.setLivre(creerLivre(false));
                service.marshal(exchange.getIn(), creer);
            }
        });

        assertMockEndpointsSatisfied();
        assertTrue(PetalsRouteBuilder.isJbiFailed(exchange));
        assertNotNull(exchange.getException());
        assertFalse(PetalsRouteBuilder.isJbiFault(exchange.getOut()));

        org.assertj.core.api.Assertions.assertThat(exchange.getException())
            .isNotNull()
            .isInstanceOf(MessagingException.class)
            .hasMessageStartingWith("Impossible de retrouver le livre nouvellement créé. ");
    }

    /**
     * Mock du service de recherche de livre.
     * Ce mock est appelé une seule fois dans le cas du livre existant.
     * deux dans le cas de la création (pour palier le fait que l'on a pas l'id au retour de l'insert).
     *
     * @param nbAppel nombre de fois ou le mock doit être appeleé,
     */
    private void creerMockRechercherLivre(final int nbAppel) {
        final MockEndpoint mock = getTo(Routes.RECHERCHER_LIVRE_INTERNE);
        mock.expectedMessageCount(nbAppel);
        mock.whenExchangeReceived(1, exchange -> {
            assertionRechercherLivre(exchange);
            final RechercherReponse rechercherReponse = new RechercherReponse();
            // retourne un livre avec un id seulement il y a un seul appel
            // c'est le cas de test du livre existant.
            if (nbAppel == 1) {
                rechercherReponse.getLivre().add(creerLivre(true));
            }
            service.marshal(exchange.getOut(), rechercherReponse);
        });
        if (nbAppel > 1) {
            mock.whenExchangeReceived(2, exchange -> {
                assertionRechercherLivre(exchange);
                final RechercherReponse rechercherReponse = new RechercherReponse();
                rechercherReponse.getLivre().add(creerLivre());
                service.marshal(exchange.getOut(), rechercherReponse);
            });
        }
    }

    /**
     * Réalise les assertions sur le message entrant du service de recherche.
     *
     * @param exchange Échange camel contenant le message.
     * @throws JAXBException Exception levée en cas d'erreur de convertion java - xml.
     */
    private void assertionRechercherLivre(final Exchange exchange) throws JAXBException {
        final Rechercher rechercher = service.unmarshal(exchange.getIn(), Rechercher.class);
        final Rechercher rechercherAttendu = new Rechercher();
        final Livre livre = creerLivre();
        rechercherAttendu.setAnneePublication(livre.getAnneePublication());
        rechercherAttendu.setAuteur(livre.getAuteur());
        rechercherAttendu.setIsbn(livre.getIsbn());
        rechercherAttendu.setLangue(livre.getLangue());
        rechercherAttendu.setNbPage(livre.getNbPage());
        rechercherAttendu.setResume(livre.getResume());
        rechercherAttendu.setTitre(livre.getTitre());

        org.assertj.core.api.Assertions.assertThat(rechercher)
            .isNotNull()
            .usingRecursiveComparison()
            .isEqualTo(rechercherAttendu);
    }

    /**
     * Mock du service SQL Insert.
     *
     * @param nbAppel nombre de fois ou le mock doit être appeleé,
     */
    private void creerMockSqlInsert(final int nbAppel) {
        final MockEndpoint mock = getTo(Routes.SQL_INSERT);
        mock.expectedMessageCount(nbAppel);
        mock.whenAnyExchangeReceived(exchange -> {
            final String sql = service.unmarshal(exchange.getIn(), String.class);
            final Livre l = creerLivre(true);
            final String sqlAttendu = "insert into livre (TITRE, RESUME, NB_PAGE, ISBN, AUTEUR, LANGUE, ANNEE_PUBLICATION) values ("
                + SQLUtils.getString(l.getTitre()) + ", "
                + SQLUtils.getString(l.getResume()) + ", "
                + l.getNbPage() + ", "
                + SQLUtils.getString(l.getIsbn()) + ", "
                + SQLUtils.getString(l.getAuteur()) + ", "
                + SQLUtils.getString(l.getLangue()) + ", "
                + l.getAnneePublication()
                + ")";
            org.assertj.core.api.Assertions.assertThat(sql)
                .isNotNull()
                .isEqualTo(sqlAttendu);

            // Pas de réponse inOnly
        });
    }

    @Override
    protected Collection<String> routesToMock() {
        return Collections.singletonList(Routes.CREER_LIVRE);
    }

    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        service = new CreerLivreOperation();
        return service;
    }

    @Override
    public AbstractOperation getService() {
        return service;
    }
}
