package fr.ausy.traing.petals.services.techniques.ajouter.operation;

import fr.ausy.traing.petals.services.techniques.ajouter.Routes;
import org.apache.camel.RoutesBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import static org.junit.Assert.*;

public class SauvegarderLivreOperationTest extends AbstractOperationTest {
    private SauvegarderLivreOperation service;

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
