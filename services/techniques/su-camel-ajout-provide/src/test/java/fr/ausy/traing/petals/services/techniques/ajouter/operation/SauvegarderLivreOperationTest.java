package fr.ausy.traing.petals.services.techniques.ajouter.operation;

import fr.ausy.traing.petals.services.techniques.ajouter.Routes;
import fr.ausy.training.petals.bibliotheque.ajouter.technique._1.SauvegarderLivre;
import fr.ausy.training.petals.modele.bibliotheque._1.Livre;
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

public class SauvegarderLivreOperationTest extends AbstractOperationTest {
    private SauvegarderLivreOperation service;

    @Test
    public void casNominalTest() throws InterruptedException {
        creerMockSql(true);
        final Exchange exchange = template().send(
            getFrom(Routes.SAUVEGARDER_LIVRE),
            ExchangePattern.InOnly,
            new Step("Test livre existe") {
                @Override
                public void process(final Exchange exchange) throws JAXBException {
                    final SauvegarderLivre requete = new SauvegarderLivre();
                    requete.setLivre(creerLivre(true));
                    service.marshal(exchange.getIn(), requete);
                }
            });

        assertMockEndpointsSatisfied();
        assertFalse(PetalsRouteBuilder.isJbiFailed(exchange));
        assertNull(exchange.getException());
        assertFalse(PetalsRouteBuilder.isJbiFault(exchange.getOut()));
    }

    /**
     * Mock SLQ insertion.
     *
     * @param estOk indique si le résultat doit être ok ({@code true}).
     */
    private void creerMockSql(final boolean estOk) {
        final MockEndpoint mock = getTo(Routes.SQL_SELECT);
        mock.expectedMessageCount(1);
        mock.whenAnyExchangeReceived(exchange -> {
            final String sql = service.unmarshal(exchange.getIn(), String.class);
            final Livre livre = creerLivre(true);
            final String select = String.format(
                "insert into livre (TITRE, RESUME, NB_PAGE, ISBN, AUTEUR, LANGUE, ANNEE_PUBLICATION) "
                + "values ('%s', '%s', %d, '%s', '%s', '%s', %d) returning livre_id",
                livre.getTitre(),
                // Échappement des ' par ''
                livre.getResume().replace("'","''"),
                livre.getNbPage(),
                livre.getIsbn(),
                livre.getAuteur(),
                livre.getLangue(),
                livre.getAnneePublication()
            );

            org.assertj.core.api.Assertions.assertThat(sql)
                .isNotNull()
                .isEqualTo(select);

            final Result result = new Result();
            final RowType row = new RowType();
            result.getRow().add(row);
            final ColumnType col = new ColumnType();
            col.setValue("42");
            row.getColumn().add(col);

            service.marshal(exchange.getOut(), result);
        });
    }

    @Override
    protected Collection<String> routesToMock() {
        return Collections.singletonList(Routes.SAUVEGARDER_LIVRE);
    }

    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        service = new SauvegarderLivreOperation();
        return service;
    }

    @Override
    public AbstractOperation getService() {
        return service;
    }
}
