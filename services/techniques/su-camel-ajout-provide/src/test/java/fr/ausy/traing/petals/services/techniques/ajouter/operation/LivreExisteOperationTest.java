package fr.ausy.traing.petals.services.techniques.ajouter.operation;

import fr.ausy.traing.petals.services.techniques.ajouter.Routes;
import org.apache.camel.RoutesBuilder;

import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.*;

public class LivreExisteOperationTest extends AbstractOperationTest {
    private LivreExisteOperation service;

    @Override
    protected Collection<String> routesToMock() {
        return Collections.singletonList(Routes.LIVRE_EXISTE);
    }

    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        service = new LivreExisteOperation();
        return service;
    }

    @Override
    public AbstractOperation getService() {
        return service;
    }
}
