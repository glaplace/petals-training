package fr.ausy.traing.petals.services.metiers.livre.operation;

import fr.ausy.traing.petals.services.metiers.livre.Routes;
import fr.ausy.training.petals.modele.biblotheque.livre._1.Rechercher;
import fr.ausy.training.petals.modele.biblotheque.livre._1.RechercherReponse;
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

import javax.xml.bind.JAXBException;

import java.util.Collection;
import java.util.Collections;

/**
 * Test de la route camel réalisant l'opération « rechercher livre ».
 */
public class RechercherLivreOperationTest extends AbstractOperationTest {
    private RechercherLivreOperation service;

    /**
     * Test de la méthode permettant de récréer la requete SQL à partir du message entrant.
     */
    @Test
    public void creerRequeteSqlTest() {
        final Rechercher rechercher = new Rechercher();

        String sqlAttendu = "select livre_id, titre, resume, nb_page, isbn, auteur, langue, annee_publication from livre";
        // Test du cas où il n'y a pas de paramètre fournit (toutes la table est retournée).
        assertEquals(sqlAttendu, RechercherLivreOperation.creerRequeteSql(rechercher));

        rechercher.setTitre("Druss");
        sqlAttendu += " where lower(titre) like '%druss%'";
        assertEquals(sqlAttendu, RechercherLivreOperation.creerRequeteSql(rechercher));

        rechercher.setResume("Druss est un bucheron avec une hache'");
        sqlAttendu += " and lower(resume) like '%druss est un bucheron avec une hache''%'";
        assertEquals(sqlAttendu, RechercherLivreOperation.creerRequeteSql(rechercher));

        rechercher.setNbPage(507);
        sqlAttendu += " and nb_page=507";
        assertEquals(sqlAttendu, RechercherLivreOperation.creerRequeteSql(rechercher));

        rechercher.setIsbn("9782811202569");
        sqlAttendu += " and lower(isbn) like '%9782811202569%'";
        assertEquals(sqlAttendu, RechercherLivreOperation.creerRequeteSql(rechercher));

        rechercher.setAuteur("Gemmell");
        sqlAttendu += " and lower(auteur) like '%gemmell%'";
        assertEquals(sqlAttendu, RechercherLivreOperation.creerRequeteSql(rechercher));

        rechercher.setLangue("FR");
        sqlAttendu += " and lower(langue) like '%fr%'";
        assertEquals(sqlAttendu, RechercherLivreOperation.creerRequeteSql(rechercher));

        rechercher.setAnneePublication(2009);
        sqlAttendu += " and annee_publication=2009";
        assertEquals(sqlAttendu, RechercherLivreOperation.creerRequeteSql(rechercher));

        // Test du cas où un seul paramètre est fournit et c'est le dernier
        sqlAttendu = "select livre_id, titre, resume, nb_page, isbn, auteur, langue, annee_publication from livre where annee_publication=2009";
        final Rechercher rechercher2 = new Rechercher();
        rechercher2.setAnneePublication(2009);
        assertEquals(sqlAttendu, RechercherLivreOperation.creerRequeteSql(rechercher2));

        // Test d'un seul paramètre au milieu
        sqlAttendu = "select livre_id, titre, resume, nb_page, isbn, auteur, langue, annee_publication from livre where lower(resume) like '%druss est un bucheron avec une hache''%'";
        final Rechercher rechercher3 = new Rechercher();
        rechercher3.setResume("Druss est un bucheron avec une hache'");
        assertEquals(sqlAttendu, RechercherLivreOperation.creerRequeteSql(rechercher3));

    }

    @Test
    public void rechercherLivreTest() throws InterruptedException, JAXBException {
        creerMockSql();
        final Exchange exchange = template().send(getFrom(Routes.RECHERCHER_LIVRE), ExchangePattern.InOut, new Step("Test obtenir livre") {
            @Override
            public void process(final Exchange exchange) throws Exception {
                final Rechercher rechercher = new Rechercher();
                rechercher.setTitre("Druss");
                rechercher.setResume("Druss est un bucheron avec une hache'");
                rechercher.setNbPage(507);
                rechercher.setIsbn("9782811202569");
                rechercher.setAuteur("Gemmell");
                rechercher.setLangue("FR");
                rechercher.setAnneePublication(2009);
                service.marshal(exchange.getIn(), rechercher);
            }
        });

        assertMockEndpointsSatisfied();
        assertFalse(PetalsRouteBuilder.isJbiFailed(exchange));
        assertNull(exchange.getException());
        assertFalse(PetalsRouteBuilder.isJbiFault(exchange.getOut()));

        final RechercherReponse response = service.unmarshal(exchange.getOut(), RechercherReponse.class);
        final RechercherReponse expected = new RechercherReponse();
        expected.getLivre().add(creerLivre());
        expected.getLivre().add(creerLivre(1337));
        expected.getLivre().add(creerLivre(42));
        expected.getLivre().add(creerLivre(131619));
        expected.getLivre().add(creerLivre(112909));

        org.assertj.core.api.Assertions.assertThat(response)
            .isNotNull()
            .usingRecursiveComparison()
            .isEqualTo(expected);
    }

    private void creerMockSql() {
        final MockEndpoint mock = getTo(Routes.SQL_SELECT);
        mock.expectedMessageCount(1);
        mock.whenAnyExchangeReceived(new Step("Mock requête SQL de sélection d'un livre") {
            @Override
            public void process(final Exchange exchange) throws Exception {
                final String sql = service.unmarshal(exchange.getIn(), String.class);
                final String select = "select livre_id, titre, resume, nb_page, isbn, auteur, langue, annee_publication from livre"
                    + " where lower(titre) like '%druss%'"
                    + " and lower(resume) like '%druss est un bucheron avec une hache''%'"
                    + " and nb_page=507"
                    + " and lower(isbn) like '%9782811202569%'"
                    + " and lower(auteur) like '%gemmell%'"
                    + " and lower(langue) like '%fr%'"
                    + " and annee_publication=2009";

                org.assertj.core.api.Assertions.assertThat(sql)
                    .isNotNull()
                    .isEqualTo(select);

                final Result result = new Result();
                result.getRow().add(creerLigneResultatSql(LIVRE_ID));
                result.getRow().add(creerLigneResultatSql(1337));
                result.getRow().add(creerLigneResultatSql(42));
                result.getRow().add(creerLigneResultatSql(131619));
                result.getRow().add(creerLigneResultatSql(112909));

                service.marshal(exchange.getOut(), result);
            }
        });
    }

    private RowType creerLigneResultatSql(final int id) {
        final RowType row = new RowType();
        final ColumnType colId = new ColumnType();
        colId.setValue("" + id);
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
        return row;
    }

    @Override
    protected Collection<String> routesToMock() {
        return Collections.singletonList(Routes.RECHERCHER_LIVRE);
    }

    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        service = new RechercherLivreOperation();
        return service;
    }

    @Override
    public AbstractOperation getService() {
        return service;
    }
}
