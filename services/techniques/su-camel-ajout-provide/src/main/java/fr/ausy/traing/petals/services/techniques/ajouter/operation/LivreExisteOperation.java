package fr.ausy.traing.petals.services.techniques.ajouter.operation;

import fr.ausy.traing.petals.services.techniques.ajouter.Routes;

import javax.xml.bind.JAXBException;

/**
 * Définition de la route camel réalisant l'opération « Livre existe ».
 */
public class LivreExisteOperation extends AbstractOperation {
    public LivreExisteOperation() throws JAXBException {
        super();
    }

    @Override
    public void configure() throws Exception {
        fromPetals(Routes.LIVRE_EXISTE);
    }
}
