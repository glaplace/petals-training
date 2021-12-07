package fr.ausy.traing.petals.services.techniques.emprunter.operation;

import fr.ausy.traing.petals.services.techniques.emprunter.Routes;
import fr.ausy.training.petals.bibliotheque.emprunter.technique._1.LivreEstDejaEmprunte;
import fr.ausy.training.petals.bibliotheque.emprunter.technique._1.LivreEstDejaEmprunteReponse;
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
 * Test de la route camel réalisant l'opération « livre déjà emprunté ».
 */
public class LivreDejaEmprunteOperationTest extends AbstractOperationTest {
    private LivreDejaEmprunteOperation service;

    @Test
    public void livreEmprunte() throws InterruptedException, JAXBException {
        testService(false);
    }

    @Test
    public void livreDisponible() throws InterruptedException, JAXBException {
        testService(true);
    }

    private void testService(final boolean estDisponible) throws InterruptedException, JAXBException {
        creerMockSql(estDisponible);

        final Exchange exchange = template().send(getFrom(Routes.LIVRE_EST_DEJA_EMPRUNTE), ExchangePattern.InOut, new Step("Test le livre est-il emprunté ?") {
            @Override
            public void process(final Exchange exchange) throws Exception {
                final LivreEstDejaEmprunte requete = new LivreEstDejaEmprunte();
                requete.setIdentifiant(LIVRE_ID);
                service.marshal(exchange.getIn(), requete);
            }
        });

        assertMockEndpointsSatisfied();
        assertFalse(PetalsRouteBuilder.isJbiFailed(exchange));
        assertNull(exchange.getException());
        assertFalse(PetalsRouteBuilder.isJbiFault(exchange.getOut()));

        final LivreEstDejaEmprunteReponse reponse = service.unmarshal(exchange.getOut(), LivreEstDejaEmprunteReponse.class);
        final LivreEstDejaEmprunteReponse reponseAttendue = new LivreEstDejaEmprunteReponse();
        reponseAttendue.setDejaEmprunter(!estDisponible);

        org.assertj.core.api.Assertions.assertThat(reponse)
            .isNotNull()
            .usingRecursiveComparison()
            .isEqualTo(reponseAttendue);
    }

    /**
     * Mock du service SQL select permettant de compter le nombre d'emprunt actif.
     *
     * @param estDisponible est-ce que le livre est disponible ? {@code true} disponible, {@code false} déjà emprunté.
     */
    private void creerMockSql(final boolean estDisponible) {
        final MockEndpoint mock = getTo(Routes.SQL_SELECT);
        mock.expectedMessageCount(1);
        mock.whenAnyExchangeReceived(exchange -> {
            final String sql = service.unmarshal(exchange.getIn(), String.class);
            final String select = "select count(1) from pret where livre_id=" + LIVRE_ID + " and rendu=false";

            org.assertj.core.api.Assertions.assertThat(sql)
                .isNotNull()
                .isEqualTo(select);

            final Result result = new Result();
            final RowType row = new RowType();
            result.getRow().add(row);
            final ColumnType col = new ColumnType();
            if (estDisponible) {
                col.setValue("0");
            } else {
                col.setValue("1337");
            }
            row.getColumn().add(col);

            service.marshal(exchange.getOut(), result);
        });
    }

    @Override
    protected Collection<String> routesToMock() {
        return Collections.singletonList(Routes.LIVRE_EST_DEJA_EMPRUNTE);
    }

    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        service = new LivreDejaEmprunteOperation();
        return service;
    }

    @Override
    public AbstractOperation getService() {
        return service;
    }
}
