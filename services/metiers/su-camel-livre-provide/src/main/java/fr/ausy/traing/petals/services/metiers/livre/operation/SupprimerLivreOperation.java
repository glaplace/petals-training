package fr.ausy.traing.petals.services.metiers.livre.operation;

import fr.ausy.traing.petals.services.metiers.livre.Routes;
import fr.ausy.training.petals.modele.biblotheque.livre._1.Supprimer;

import javax.jbi.messaging.MessagingException;
import javax.xml.bind.JAXBException;

/**
 * Définition de la route camel réalisant l'opération « supprimer ».
 */
public class SupprimerLivreOperation extends AbstractOperation {
    public SupprimerLivreOperation() throws JAXBException {
        super();
    }

    @Override
    public void configure() throws Exception {
        fromPetals(Routes.SUPPRIMER_LIVRE)
            .process(exchange -> {
                final Supprimer supprimer = unmarshal(exchange.getIn(), Supprimer.class);
                final String sql = "delete from livre where livre_id=" + supprimer.getIdentifiant();
                marshal(exchange.getOut(), SQL_OBJECT_FACTORY.createSql(sql));
            }).to(PETALS_PREFIX + Routes.SQL_DELETE)
            .process(exchange -> {
                if (isJbiFailed(exchange)) {
                    throw new MessagingException("Erreur SQL ", exchange.getException());
                }
            });
    }
}
