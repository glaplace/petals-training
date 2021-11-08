package fr.ausy.traing.petals.services.metiers.livre.operation;

import fr.ausy.traing.petals.services.metiers.livre.Routes;
import fr.ausy.training.petals.modele.biblotheque._1.Livre;
import fr.ausy.training.petals.modele.biblotheque.livre._1.Obtenir;
import fr.ausy.training.petals.modele.biblotheque.livre._1.ObtenirReponse;
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

/**
 *
 */
public class ObtenirLivreOperationTest extends AbstractOperationTest {
    private ObtenirLivreOperation service;

    @Test
    public void obtenirLivre() throws InterruptedException, JAXBException {
        creerMockSql();
        final Exchange exchange = template().send(getFrom(Routes.OBTENIR_LIVRE), ExchangePattern.InOut, new Step("Test obtenir livre") {
            @Override
            public void process(final Exchange exchange) throws Exception {
                final Obtenir obtenir = new Obtenir();
                obtenir.setIdentifiant(LIVRE_ID);
                service.marshal(exchange.getIn(), obtenir);
            }
        });

        assertMockEndpointsSatisfied();
        assertFalse(PetalsRouteBuilder.isJbiFailed(exchange));
        assertNull(exchange.getException());
        assertFalse(PetalsRouteBuilder.isJbiFault(exchange.getOut()));

        final ObtenirReponse response = service.unmarshal(exchange.getOut(), ObtenirReponse.class);
        final ObtenirReponse expected = new ObtenirReponse();
        final Livre livre = creerLivre();
        expected.setLivre(livre);

        org.assertj.core.api.Assertions.assertThat(response)
            .isNotNull()
            .usingRecursiveComparison()
            .isEqualTo(expected);
    }

    @Test
    public void obtenirLivreErreurSQL() throws InterruptedException, JAXBException {
        final String messageException = "My Error Message";

        final MockEndpoint mock = getTo(Routes.SQL_SELECT);
        mock.expectedMessageCount(1);
        mock.whenAnyExchangeReceived(exchange -> {
            throw new MessagingException(messageException);
        });
        final Exchange exchange = template().send(getFrom(Routes.OBTENIR_LIVRE), ExchangePattern.InOut, new Step("Test obtenir livre") {
            @Override
            public void process(final Exchange exchange) throws Exception {
                final Obtenir obtenir = new Obtenir();
                obtenir.setIdentifiant(LIVRE_ID);
                service.marshal(exchange.getIn(), obtenir);
            }
        });

        assertMockEndpointsSatisfied();
        assertTrue(PetalsRouteBuilder.isJbiFailed(exchange));
        assertNotNull(exchange.getException());
        assertFalse(PetalsRouteBuilder.isJbiFault(exchange.getOut()));

        org.assertj.core.api.Assertions.assertThat(exchange.getException())
            .isNotNull()
            .isInstanceOf(MessagingException.class)
            .hasMessage(messageException);
    }

    /**
     * Mock du service SQL select.
     */
    private void creerMockSql() {
        final MockEndpoint mock = getTo(Routes.SQL_SELECT);
        mock.expectedMessageCount(1);
        mock.whenAnyExchangeReceived(new Step("Mock requête SQL de sélection d'un livre") {
            @Override
            public void process(final Exchange exchange) throws Exception {
                final String sql = service.unmarshal(exchange.getIn(), String.class);
                final String select = "select livre_id, titre, resume, nb_page, isbn, auteur, langue, annee_publication from livre where livre_id=" + LIVRE_ID;

                org.assertj.core.api.Assertions.assertThat(sql)
                    .isNotNull()
                    .isEqualTo(select);

                final Result result = new Result();
                final RowType row = new RowType();
                result.getRow().add(row);
                final ColumnType colId = new ColumnType();
                colId.setValue("" + LIVRE_ID);
                row.getColumn().add(colId);

                final ColumnType colTitre = new ColumnType();
                colTitre.setValue("Druss la légende");
                row.getColumn().add(colTitre);
                final ColumnType colResume = new ColumnType();
                colResume.setValue("Son nom est Druss. Garçon violent et maladroit, il vit dans un petit village de paysans situé au pied des montagnes du pays drenaï. Bûcheron hargneux le jour, époux tendre le soir, il mène une existence paisible au milieu des bois. Jusqu'au jour où une troupe de mercenaires envahit le village pour tuer tous les hommes et capturer toutes les femmes. Druss, alors dans la forêt, arrive trop tard sur les lieux du massacre. Le village est détruit, son père gît dans une mare de sang. Et Rowena, sa femme, a disparu... S'armant de Snaga, une hache ayant appartenu à son grand-père, il part à la poursuite des ravisseurs. Déterminé à retrouver son épouse, rien ne devra se mettre en travers de son chemin. Mais la route sera longue pour ce jeune homme inexpérimenté. Car sa quête le mènera jusqu'au bout du monde. Il deviendra lutteur et mercenaire, il fera tomber des royaumes, il en élèvera d'autres, il combattra bêtes, hommes et démons. Car il est Druss... et voici sa légende...");
                row.getColumn().add(colResume);
                final ColumnType colNbPage = new ColumnType();
                colNbPage.setValue("507");
                row.getColumn().add(colNbPage);
                final ColumnType colIsbn = new ColumnType();
                colIsbn.setValue("9782811202569");
                row.getColumn().add(colIsbn);
                final ColumnType colAuteur = new ColumnType();
                colAuteur.setValue("David Gemmell");
                row.getColumn().add(colAuteur);
                final ColumnType colLangue = new ColumnType();
                colLangue.setValue("fr");
                row.getColumn().add(colLangue);
                final ColumnType colAnne = new ColumnType();
                colAnne.setValue("2009");
                row.getColumn().add(colAnne);
                service.marshal(exchange.getOut(), result);
            }
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
