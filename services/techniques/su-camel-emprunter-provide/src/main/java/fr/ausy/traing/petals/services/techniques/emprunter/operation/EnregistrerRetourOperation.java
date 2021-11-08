package fr.ausy.traing.petals.services.techniques.emprunter.operation;

import fr.ausy.traing.petals.services.techniques.emprunter.Routes;
import fr.ausy.training.petals.biblotheque.emprunter._1.RetournerEmprunt;
import org.apache.commons.lang3.StringUtils;

import javax.jbi.messaging.MessagingException;
import javax.xml.bind.JAXBException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static fr.ausy.traing.petals.services.techniques.emprunter.operation.AjouterEmpruntOperation.DUREE_EMPRUNT_PLACEHOLER;

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
                    "update pret (RENDU) values (true) where livre_id = %d and utilisateur_id = %d",
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
