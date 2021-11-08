package fr.ausy.traing.petals.services.techniques.emprunter.operation;

import org.ow2.petals.camel.junit.PetalsCamelTestSupport;

import java.util.Random;

public abstract class AbstractOperationTest extends PetalsCamelTestSupport {
    protected final int LIVRE_ID = Math.abs(new Random().nextInt());

    public abstract AbstractOperation getService();
}
