package fr.ausy.traing.petals.services.techniques.ajouter.operation;

import fr.ausy.traing.petals.services.techniques.ajouter.Routes;

import javax.xml.bind.JAXBException;

/**
 * Définition de la route camel Réalisant l'opération « sauvegarder livre ».
 */
public class SauvegarderLivreOperation extends AbstractOperation {
    public SauvegarderLivreOperation() throws JAXBException {
        super();
    }

    @Override
    public void configure() throws Exception {
        fromPetals(Routes.SAUVEGARDER_LIVRE);
    }
}
