package fr.ausy.traing.petals.services.techniques.ajouter.operation;

import fr.ausy.traing.petals.services.techniques.ajouter.Routes;
import org.apache.camel.RoutesBuilder;

import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.*;

public class ObtenirLivreOperationTest extends AbstractOperationTest {
    private ObtenirLivreOperation service;

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
