package fr.ausy.traing.petals.services.techniques.ajouter.operation;

import fr.ausy.traing.petals.services.techniques.ajouter.Routes;

import javax.xml.bind.JAXBException;

/**
 * Définition de la route camel rélisant l'opération « Obtenir livre ».
 */
public class ObtenirLivreOperation extends AbstractOperation {
    public ObtenirLivreOperation() throws JAXBException {
        super();
    }

    @Override
    public void configure() throws Exception {
        fromPetals(Routes.OBTENIR_LIVRE);
    }
}
