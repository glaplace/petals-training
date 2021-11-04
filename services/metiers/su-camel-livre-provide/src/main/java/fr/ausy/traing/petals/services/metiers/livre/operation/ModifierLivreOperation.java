package fr.ausy.traing.petals.services.metiers.livre.operation;

import fr.ausy.traing.petals.services.metiers.livre.Routes;
import fr.ausy.traing.petals.services.metiers.livre.sql.SQLUtils;
import fr.ausy.training.petals.modele.biblotheque._1.Livre;
import fr.ausy.training.petals.modele.biblotheque.livre._1.LivreInconnu;
import fr.ausy.training.petals.modele.biblotheque.livre._1.Modifier;
import fr.ausy.training.petals.modele.biblotheque.livre._1.Obtenir;
import fr.ausy.training.petals.modele.biblotheque.livre._1.ObtenirReponse;

import javax.jbi.messaging.MessagingException;
import javax.xml.bind.JAXBException;

import java.util.Objects;

/**
 * Définition de la route camel réalisant l'opération modifier livre.
 */
public class ModifierLivreOperation extends AbstractOperation {
    public ModifierLivreOperation() throws JAXBException {
        super();
    }

    @Override
    public void configure() throws Exception {
        fromPetals(Routes.MODIFIER_LIVRE)
            .process(exchange -> {
                final Modifier modifier = unmarshal(exchange.getIn(), Modifier.class);
                if (Objects.isNull(modifier) || Objects.isNull(modifier.getLivre()) || modifier.getLivre().getLivreId() < 1) {
                    throw new MessagingException(String.format("Le paramètre modifié est incorrecte %s", modifier));
                }
                exchange.setProperty(REQUETE, modifier.getLivre());

                final Obtenir obtenir = new Obtenir();
                obtenir.setIdentifiant(modifier.getLivre().getLivreId());
                marshal(exchange.getOut(), obtenir);
            }).to(PETALS_PREFIX + Routes.OBTENIR_LIVRE_INTERNE)
            .process(exchange -> {
                if (isJbiFailed(exchange)) {
                    if (isJbiFault(exchange.getIn())) {
                        final Object faultObject = unmarshal(exchange.getIn(), Object.class);
                        if (faultObject instanceof LivreInconnu) {
                            setJbiFault(getMarshalling(), exchange, faultObject);

                            return;
                        }
                        throw new MessagingException("Fault inconnue du service ObtenirLivre " + faultObject);
                    }
                    throw new MessagingException("Erreur inconnue du service Obtenir Livre", exchange.getException());
                }
                final ObtenirReponse obtenirReponse = unmarshal(exchange.getIn(), ObtenirReponse.class);
                final Livre livre = exchange.getProperty(REQUETE, Livre.class);
                if (Objects.isNull(obtenirReponse) || Objects.isNull(obtenirReponse.getLivre())) {
                    final LivreInconnu livreInconnu = new LivreInconnu();
                    livreInconnu.setIdentifiant(livreInconnu.getIdentifiant());
                    setJbiFault(getMarshalling(), exchange, livreInconnu);
                    return;
                }

                // Requête SQL
                final String sql = "update LIVRE set \n" +
                    "    TITRE=" + SQLUtils.getString(livre.getTitre()) + ",\n" +
                    "    RESUME=" + SQLUtils.getString(livre.getResume()) + ",\n" +
                    "    NB_PAGE=" + livre.getNbPage() + ",\n" +
                    "    ISBN=" + SQLUtils.getString(livre.getIsbn()) + ",\n" +
                    "    AUTEUR=" + SQLUtils.getString(livre.getAuteur()) + ",\n" +
                    "    LANGUE=" + SQLUtils.getString(livre.getLangue()) + ",\n" +
                    "    ANNEE_PUBLICATION=" + livre.getAnneePublication() +
                    "\nwhere LIVRE_ID=" + livre.getLivreId();
                marshal(exchange.getOut(), SQL_OBJECT_FACTORY.createSql(sql));
            })
            .to(PETALS_PREFIX + Routes.SQL_UPDATE)
            .process(exchange -> {
                if (isJbiFailed(exchange)) {
                    throw new MessagingException("Erreur SQL ", exchange.getException());
                }
            });
    }
}
