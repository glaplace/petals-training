package fr.ausy.traing.petals.services.techniques.ajouter.operation;

import fr.ausy.traing.petals.services.techniques.ajouter.Routes;
import fr.ausy.training.petals.bibliotheque.ajouter.technique._1.LivreInconnu;
import fr.ausy.training.petals.bibliotheque.ajouter.technique._1.ObtenirLivre;
import fr.ausy.training.petals.bibliotheque.ajouter.technique._1.ObtenirLivreReponse;
import fr.ausy.training.petals.modele.bibliotheque._1.Livre;
import org.apache.commons.lang3.StringUtils;
import org.openlibrary.api._1.AuteurInconnu;
import org.openlibrary.api._1.IsbnInconnu;
import org.openlibrary.api._1.ObtenirAuteur;
import org.openlibrary.api._1.ObtenirAuteurReponse;
import org.openlibrary.api._1.ObtenirParIsbn;
import org.openlibrary.api._1.ObtenirParIsbnReponse;

import javax.jbi.messaging.MessagingException;
import javax.xml.bind.JAXBException;

/**
 * Définition de la route camel rélisant l'opération « Obtenir livre ».
 */
public class ObtenirLivreOperation extends AbstractOperation {
    private static final String LIVRE = ObtenirLivreOperation.class.getName() + ".livre";

    public ObtenirLivreOperation() throws JAXBException {
        super();
    }

    @Override
    public void configure() throws Exception {
        fromPetals(Routes.OBTENIR_LIVRE)
            .process(exchange -> {
                final ObtenirLivre requete = unmarshal(exchange.getIn(), ObtenirLivre.class);

                final ObtenirParIsbn obtenirParIsbn = new ObtenirParIsbn();
                obtenirParIsbn.setIsbn(Long.parseLong(requete.getIsbn()));
                marshal(exchange.getOut(), obtenirParIsbn);
            }).to(PETALS_PREFIX + Routes.OBTENIR_LIVRE_OL)
            .process(exchange -> {
                if (!isJbiFault(exchange.getIn())) {
                    final ObtenirParIsbnReponse reponse = unmarshal(exchange.getIn(), ObtenirParIsbnReponse.class);
                    exchange.setProperty(LIVRE, reponse.getLivre());
                } else {
                    final Object fault = unmarshal(exchange.getIn(), Object.class);
                    if (fault instanceof IsbnInconnu) {
                        final IsbnInconnu isbnInconnu = (IsbnInconnu) fault;
                        final LivreInconnu livreInconnu = new LivreInconnu();
                        livreInconnu.setIsbn(String.valueOf(isbnInconnu.getIsbn()));
                        setJbiFault(getMarshalling(), exchange, livreInconnu);
                    }
                }
            }).choice()
            // Récupérer le l'auteur que si il est indiqué dans la réponse.
            .when(exchange -> {
                final Livre livre = exchange.getProperty(LIVRE, Livre.class);
                return StringUtils.isNotBlank(livre.getAuteur());
            })
            .process(exchange -> {
                final ObtenirAuteur requete = new ObtenirAuteur();
                final Livre livre = exchange.getProperty(LIVRE, Livre.class);
                requete.setId(livre.getAuteur());
                marshal(exchange.getOut(), requete);
            }).to(PETALS_PREFIX + Routes.OBTENIR_AUTEUR_OL)
            .process(exchange -> {
                final Livre livre = exchange.getProperty(LIVRE, Livre.class);
                if (!isJbiFault(exchange.getIn())) {
                    final ObtenirAuteurReponse reponseAuteur = unmarshal(exchange.getIn(), ObtenirAuteurReponse.class);
                    livre.setAuteur(reponseAuteur.getNom());
                } else {
                    final Object fault = unmarshal(exchange.getIn(), Object.class);
                    if (fault instanceof AuteurInconnu) {
                        final AuteurInconnu auteurInconnu = (AuteurInconnu) fault;
                        throw new MessagingException(String.format("Erreur Technique auteur inconnu [%s]", livre.getAuteur()));
                    }
                }
            }).end()
            .process(exchange -> {
                final Livre livre = exchange.getProperty(LIVRE, Livre.class);
                final ObtenirLivreReponse reponse = new ObtenirLivreReponse();
                reponse.setLivre(livre);
                marshal(exchange.getOut(), reponse);
            });
    }
}
