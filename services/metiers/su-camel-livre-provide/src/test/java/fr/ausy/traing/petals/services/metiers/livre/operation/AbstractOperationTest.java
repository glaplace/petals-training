package fr.ausy.traing.petals.services.metiers.livre.operation;

import org.ow2.petals.camel.junit.PetalsCamelTestSupport;

/**
 * Classe mére des classes de tests permettant de mutualiser les comportements.
 */
public abstract class AbstractOperationTest extends PetalsCamelTestSupport {
    protected final int LIVRE_ID = 666;

    public abstract AbstractOperation getService();
}
