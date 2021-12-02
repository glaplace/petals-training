package fr.ausy.traing.petals.services.techniques.emprunter.operation;

import fr.ausy.traing.petals.services.techniques.emprunter.Routes;
import fr.ausy.training.petals.bibliotheque.emprunter.technique._1.RetournerEmprunt;

import javax.jbi.messaging.MessagingException;
import javax.xml.bind.JAXBException;

/**
 * Définition de la route camel réalisant l'enregistrement du retour d'un pret.
 */
public class EnregistrerRetourOperation extends AbstractOperation {
    public EnregistrerRetourOperation() throws JAXBException {
        super();
    }

    @Override
    public void configure() throws Exception {
        fromPetals(Routes.RETOURNER_EMPRUNT)
            .process(exchange -> {
                final RetournerEmprunt requete = unmarshal(exchange.getIn(), RetournerEmprunt.class);

                final String sql = String.format(
                    "update pret set RENDU=true where livre_id = %d and utilisateur_id = %d",
                    requete.getLivre(),
                    requete.getUtilisateur()
                );

                marshal(exchange.getOut(), SQL_OBJECT_FACTORY.createSql(sql));
            }).to(PETALS_PREFIX + Routes.SQL_UPDATE)
            .process(exchange -> {
                // Vérifier qu'une ligne est affectée (donc modifiée dans notre cas)..
                final Integer resultAffected = unmarshal(exchange.getIn(), Integer.class);
                if (resultAffected != 1) {
                    throw new MessagingException("Erreur à la mise à jour, mauvais nombre de ligne affectée [" + resultAffected + "]");
                }
            });
    }
}
