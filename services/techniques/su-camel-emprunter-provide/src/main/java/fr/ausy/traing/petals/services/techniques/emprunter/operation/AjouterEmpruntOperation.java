package fr.ausy.traing.petals.services.techniques.emprunter.operation;

import fr.ausy.traing.petals.services.techniques.emprunter.Routes;
import fr.ausy.training.petals.bibliotheque.emprunter.technique._1.AjouterEmprunt;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.ow2.petals.camel.helpers.Step;

import javax.jbi.messaging.MessagingException;
import javax.xml.bind.JAXBException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Définition de la route camel réalisant l'opération « Ajouter emprunt ».
 */
public class AjouterEmpruntOperation extends AbstractOperation {
    public static final String DUREE_EMPRUNT_PLACEHOLER = "ausy.training.emprunt.duree";

    public AjouterEmpruntOperation() throws JAXBException {
        super();
    }

    @Override
    public void configure() throws Exception {
        fromPetals(Routes.AJOUTER_EMPRUNT)
            .process(exchange -> {
                final AjouterEmprunt requete = unmarshal(exchange.getIn(), AjouterEmprunt.class);
                final int duree;
                if (StringUtils.isNumeric(getPlaceHolder(DUREE_EMPRUNT_PLACEHOLER))) {
                    duree = Integer.parseInt(getPlaceHolder(DUREE_EMPRUNT_PLACEHOLER));
                } else {
                    duree = 3;
                }

                final String sql = String.format(
                    "insert into pret (UTILISATEUR_ID, LIVRE_ID, DATE_PRET, DUREE_PRET, RENDU, NB_RELANCE) values (%d, %d, %s, %d, %b, %d)",
                    requete.getLivre(),
                    requete.getUtilisateur(),
                    "'" + LocalDate.now().format(DateTimeFormatter.ISO_DATE) + "'",
                    duree,
                    false,
                    0
                );

                marshal(exchange.getOut(), SQL_OBJECT_FACTORY.createSql(sql));
            }).to(PETALS_PREFIX + Routes.SQL_INSERT)
            .process(new Step("Humpha humpa") {
                @Override
                public void process(final Exchange exchange) throws Exception {
                    // Vérifier qu'une ligne est affectée (donc ajoutée dans notre cas)..
                    final Integer resultAffected = unmarshal(exchange.getIn(), Integer.class);
                    if (resultAffected != 1) {
                        throw new MessagingException("Erreur à l'insertion mauvais nombre de ligne affectée [" + resultAffected + "]");
                    }
                }
            });
    }
}
