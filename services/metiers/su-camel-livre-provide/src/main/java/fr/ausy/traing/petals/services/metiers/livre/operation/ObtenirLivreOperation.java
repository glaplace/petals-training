package fr.ausy.traing.petals.services.metiers.livre.operation;

import fr.ausy.traing.petals.services.metiers.livre.Routes;
import fr.ausy.training.petals.modele.biblotheque._1.Livre;
import fr.ausy.training.petals.modele.biblotheque.livre._1.LivreInconnu;
import fr.ausy.training.petals.modele.biblotheque.livre._1.Obtenir;
import fr.ausy.training.petals.modele.biblotheque.livre._1.ObtenirReponse;
import org.ow2.petals.components.sql.version_1.Result;

import javax.jbi.messaging.MessagingException;
import javax.xml.bind.JAXBException;

import java.util.Objects;

/**
 * Configuration de la route camel réalisant l'opération « Obtenir ».
 */
public class ObtenirLivreOperation extends AbstractOperation {
    public ObtenirLivreOperation() throws JAXBException {
        super();
    }

    @Override
    public void configure() throws Exception {
        fromPetals(Routes.OBTENIR_LIVRE)
            .process(exchange -> {
                final Obtenir requete = unmarshal(exchange.getIn(), Obtenir.class);
                exchange.setProperty(ID_LIVRE, requete.getIdentifiant());
                final String sql = "select livre_id, titre, resume, nb_page, isbn, auteur, langue, annee_publication from livre where livre_id=" + requete.getIdentifiant();
                marshal(exchange.getOut(), SQL_OBJECT_FACTORY.createSql(sql));
            }).to(PETALS_PREFIX + Routes.SQL_SELECT)
            .process(exchange -> {
                if (isJbiFailed(exchange)) {
                    throw new MessagingException("Erreur SQL ", exchange.getException());
                }
                final Result result = unmarshal(exchange.getIn(), Result.class);
                // oui pas de livre ou plus d'un livre (pour le même id) c'est livre inconnu.
                if (Objects.isNull(result) || result.getRow().size() != 1) {
                    final LivreInconnu livreInconnu = new LivreInconnu();
                    livreInconnu.setIdentifiant(exchange.getProperty(ID_LIVRE, Long.class));
                    setJbiFault(getMarshalling(), exchange, livreInconnu);
                    return;
                }
                final ObtenirReponse reponse = transformerResultEnreponse(result);
                marshal(exchange.getOut(), reponse);
            });
    }

    /**
     * Permet de passer d'un objet {@link Result} en un objet {@link ObtenirReponse}.
     *
     * @param result Réponse SQL à transoformer
     * @return réponse du service image de la réponse SQL
     */
    private static ObtenirReponse transformerResultEnreponse(final Result result) {
        final ObtenirReponse reponse = new ObtenirReponse();
        final Livre livre = transformerRowEnLivre(result.getRow().get(0));
        reponse.setLivre(livre);
        return reponse;
    }


}
