package fr.ausy.traing.petals.services.metiers.livre.operation;

import com.ebmwebsourcing.easycommons.xml.SourceHelper;
import fr.ausy.traing.petals.services.metiers.livre.Routes;
import fr.ausy.traing.petals.services.metiers.livre.sql.SQLUtils;
import fr.ausy.training.petals.modele.biblotheque._1.Livre;
import fr.ausy.training.petals.modele.biblotheque.livre._1.*;

import javax.jbi.messaging.MessagingException;
import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;

/**
 * Définition de la route camel réalisant l'opération creer.
 * <p>
 * Idempotence : tester si le livre existe, si c'est le cas faire obtenir et retourner celui qui existe.
 * définir les critères d'égalité (case sensitive ou pas ...)
 */
public class CreerLivreOperation extends AbstractOperation {
    private static final String LIVRE_EXISTANT = CreerLivreOperation.class.getName() + ".livre-existant";
    private static final String LIVRE_A_RETOURNER = CreerLivreOperation.class.getName() + ".livre-a-retourner";
    protected static final String DIRECT_CREER = "direct:creer-livre-interne";

    public CreerLivreOperation() throws JAXBException {
        super();
    }

    @Override
    public void configure() {
        routeInterneCreer();
        routePetalsCreer();
    }

    public void routePetalsCreer() {
        fromPetals(Routes.CREER_LIVRE)
            .process(exchange -> {
                final Creer creer = unmarshal(exchange.getIn(), Creer.class);
                final Livre livre = creer.getLivre();
                exchange.setProperty(REQUETE, livre);

                final Rechercher rechercher = creerRechercheDepuisLivre(livre);
                marshal(exchange.getOut(), rechercher);
            }).to(PETALS_PREFIX + Routes.RECHERCHER_LIVRE_INTERNE)
            .process(exchange -> {
                if (isJbiFailed(exchange)) {
                    if (isJbiFault(exchange.getIn())) {
                        throw new MessagingException("Fault inattendue du service Rechercher livre : " + SourceHelper.toString(exchange.getIn().getBody(Source.class)));
                    }
                    throw new MessagingException("Erreur inattendue du service Rechercher livre", exchange.getException());
                }

                final RechercherReponse rechercherReponse = unmarshal(exchange.getIn(), RechercherReponse.class);
                exchange.setProperty(LIVRE_EXISTANT, !rechercherReponse.getLivre().isEmpty());
                if (!rechercherReponse.getLivre().isEmpty()) {
                    exchange.setProperty(LIVRE_A_RETOURNER, rechercherReponse.getLivre().get(0));
                }
            })
            .choice()
            .when(exchange -> exchange.getProperty(LIVRE_EXISTANT, Boolean.class))
            .process(exchange -> {
                final CreerReponse reponse = new CreerReponse();
                reponse.setLivre(exchange.getProperty(LIVRE_A_RETOURNER, Livre.class));
                marshal(exchange.getOut(), reponse);
            })
            .otherwise()
            .to(DIRECT_CREER)
            .end();
    }

    /**
     * Cas de la création du livre.
     * Le livre est créé, puis recherché et retourné.
     */
    public void routeInterneCreer() {
        from(DIRECT_CREER)
            .process(exchange -> {
                final Livre livre = exchange.getProperty(REQUETE, Livre.class);
                final String sql = "insert into livre (TITRE, RESUME, NB_PAGE, ISBN, AUTEUR, LANGUE, ANNEE_PUBLICATION) values ("
                    + SQLUtils.getString(livre.getTitre()) + ", "
                    + SQLUtils.getString(livre.getResume()) + ", "
                    + livre.getNbPage() + ", "
                    + SQLUtils.getString(livre.getIsbn()) + ", "
                    + SQLUtils.getString(livre.getAuteur()) + ", "
                    + SQLUtils.getString(livre.getLangue()) + ", "
                    + livre.getAnneePublication()
                    + ")";

                marshal(exchange.getOut(), SQL_OBJECT_FACTORY.createSql(sql));
            }).to(PETALS_PREFIX + Routes.SQL_INSERT)
            .process(exchange -> {
                if (isJbiFailed(exchange)) {
                    if (isJbiFault(exchange.getIn())) {
                        throw new MessagingException("Fault inattendue du service SQL insert : " + SourceHelper.toString(exchange.getIn().getBody(Source.class)));
                    }
                    throw new MessagingException("Erreur inattendue du service SQL insert : ", exchange.getException());
                }
                final Rechercher rechercher = creerRechercheDepuisLivre(exchange.getProperty(REQUETE, Livre.class));
                marshal(exchange.getOut(), rechercher);
            }).to(PETALS_PREFIX + Routes.RECHERCHER_LIVRE_INTERNE)
            .process(exchange -> {
                if (isJbiFailed(exchange)) {
                    if (isJbiFault(exchange.getIn())) {
                        throw new MessagingException("Fault inattendue, impossible de rechercher le livre nouvellement créé : " + SourceHelper.toString(exchange.getIn().getBody(Source.class)));
                    }
                    throw new MessagingException("Erreur inattendue du service Rechercher livre", exchange.getException());
                }

                final RechercherReponse rechercherReponse = unmarshal(exchange.getIn(), RechercherReponse.class);
                if (rechercherReponse.getLivre().size() != 1) {
                    throw new MessagingException("Impossible de retrouver le livre nouvellement créé. " + rechercherReponse.getLivre().size() + " élements retourné. ");
                }
                final CreerReponse reponse = new CreerReponse();
                reponse.setLivre(rechercherReponse.getLivre().get(0));
                marshal(exchange.getOut(), reponse);
            });
    }

    /**
     * Création de l'objet de recherche depuis l'objet {@link Livre}.
     *
     * @param livre livre à utiliser pour la recherche
     * @return Objet permettant la recherche d'un livre, image du paramètre {@ode livre}.
     */
    private Rechercher creerRechercheDepuisLivre(final Livre livre) {
        final Rechercher rechercher = new Rechercher();
        rechercher.setAnneePublication(livre.getAnneePublication());
        rechercher.setAuteur(livre.getAuteur());
        rechercher.setIsbn(livre.getIsbn());
        rechercher.setLangue(livre.getLangue());
        rechercher.setNbPage(livre.getNbPage());
        rechercher.setResume(livre.getResume());
        rechercher.setTitre(livre.getTitre());
        return rechercher;
    }
}
