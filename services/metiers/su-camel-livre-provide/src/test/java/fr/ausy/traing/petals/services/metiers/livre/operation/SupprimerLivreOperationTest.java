package fr.ausy.traing.petals.services.metiers.livre.operation;

import fr.ausy.traing.petals.services.metiers.livre.Routes;
import fr.ausy.training.petals.modele.biblotheque._1.Livre;
import fr.ausy.training.petals.modele.biblotheque.livre._1.Obtenir;
import fr.ausy.training.petals.modele.biblotheque.livre._1.ObtenirReponse;
import fr.ausy.training.petals.modele.biblotheque.livre._1.Supprimer;
import junit.framework.TestCase;
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
 * Classe de test de la route camel réalisant l'opération « supprime ».
 */
public class SupprimerLivreOperationTest extends AbstractOperationTest {
    private SupprimerLivreOperation service;

    @Test
    public void supprimerTest() throws InterruptedException {
        creerMockSql();
        final Exchange exchange = template().send(getFrom(Routes.SUPPRIMER_LIVRE), ExchangePattern.InOnly, new Step("Test supprimer livre") {
            @Override
            public void process(final Exchange exchange) throws Exception {
                final Supprimer supprimer = new Supprimer();
                supprimer.setIdentifiant(LIVRE_ID);
                service.marshal(exchange.getIn(), supprimer);
            }
        });

        assertMockEndpointsSatisfied();
        assertFalse(PetalsRouteBuilder.isJbiFailed(exchange));
        assertNull(exchange.getException());
        assertFalse(PetalsRouteBuilder.isJbiFault(exchange.getOut()));
    }

    private void creerMockSql() {
        final MockEndpoint mock = getTo(Routes.SQL_DELETE);
        mock.expectedMessageCount(1);
        mock.whenAnyExchangeReceived(new Step("Mock requête SQL de suppression d'un livre") {
            @Override
            public void process(final Exchange exchange) throws Exception {
                final String sql = service.unmarshal(exchange.getIn(), String.class);
                final String delete = "delete from livre where livre_id=" + LIVRE_ID;

                org.assertj.core.api.Assertions.assertThat(sql)
                    .isNotNull()
                    .isEqualTo(delete);
                // pas de réponse c'est du in only
            }
        });
    }

    @Override
    protected Collection<String> routesToMock() {
        return Collections.singletonList(Routes.SUPPRIMER_LIVRE);
    }

    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        service = new SupprimerLivreOperation();
        return service;
    }

    @Override
    public AbstractOperation getService() {
        return service;
    }
}
