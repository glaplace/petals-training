package fr.ausy.traing.petals.services.metiers.livre.operation;

import fr.ausy.traing.petals.services.metiers.livre.Routes;
import fr.ausy.traing.petals.services.metiers.livre.sql.SQLUtils;
import fr.ausy.training.petals.modele.biblotheque._1.Livre;
import fr.ausy.training.petals.modele.biblotheque.livre._1.*;

import javax.jbi.messaging.MessagingException;
import javax.xml.bind.JAXBException;

/**
 * Définition de la route camel réalisant l'opération creer.
 * <p>
 * Idempotence : tester si le livre existe, si c'est le cas faire obtenir et retourner celui qui existe.
 * définir les critères d'égalité (case sensitive ou pas ...)
 */
public class CreerLivreOperation extends AbstractOperation {
    private static final String LIVRE_EXISTANT = CreerLivreOperation.class.getName() + ".livre-existant";
    protected static final String DIRECT_CREER = "direct:creer-livre-interne";
    protected static final String DIRECT_LIVRE_EXISTANT = "direct:livre-existant";

    public CreerLivreOperation() throws JAXBException {
        super();
    }

    @Override
    public void configure() {
        routeInterneCreer();
        routeInterneLivreExistant();
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
                        //TODO
                        return;
                    }
                    throw new MessagingException("Erreur innatendue", exchange.getException());
                }

                final RechercherReponse rechercherReponse = unmarshal(exchange.getIn(), RechercherReponse.class);
                exchange.setProperty(LIVRE_EXISTANT, rechercherReponse.getLivre().isEmpty());
                exchange.setProperty(ID_LIVRE, rechercherReponse.getLivre().get(0).getLivreId());
            })
            .choice()
            .when(exchange -> exchange.getProperty(LIVRE_EXISTANT, Boolean.class))
            .to(DIRECT_LIVRE_EXISTANT)
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
                    + SQLUtils.escapeString(livre.getTitre())
                    + SQLUtils.escapeString(livre.getResume())
                    + livre.getNbPage()
                    + SQLUtils.escapeString(livre.getIsbn())
                    + SQLUtils.escapeString(livre.getAuteur())
                    + SQLUtils.escapeString(livre.getLangue())
                    + SQLUtils.escapeString("" + livre.getAnneePublication())
                    + ")";

                marshal(exchange.getOut(), SQL_OBJECT_FACTORY.createSql(sql));
            }).to(PETALS_PREFIX + Routes.CREER_LIVRE)
            .process(exchange -> {
                if (isJbiFailed(exchange)) {
                    if (isJbiFault(exchange.getIn())) {
                        return;
                    }
                }
                final Rechercher rechercher = creerRechercheDepuisLivre(exchange.getProperty(REQUETE, Livre.class));
                marshal(exchange.getOut(), rechercher);
            }).to(PETALS_PREFIX + Routes.RECHERCHER_LIVRE_INTERNE)
            .process(exchange -> {
                if (isJbiFailed(exchange)) {
                    if (isJbiFault(exchange.getIn())) {
                        //TODO
                        return;
                    }
                    throw new MessagingException("Erreur innatendue", exchange.getException());
                }

                final RechercherReponse rechercherReponse = unmarshal(exchange.getIn(), RechercherReponse.class);
                if (rechercherReponse.getLivre().size() != 1) {
                    throw new MessagingException("Impossible de retrouver le livre nouvellement créé. " + rechercherReponse.getLivre().size() + " élements retourné. ");
                }
                final CreerReponse reponse = new CreerReponse();
                reponse.setLivre(rechercherReponse.getLivre().get(0));
            });
    }

    /**
     * Cas du livre déjà créé, on retourne simplement le livre trouvé en base.
     */
    public void routeInterneLivreExistant() {
        from(DIRECT_LIVRE_EXISTANT)
            .process(exchange -> {
                final Obtenir obtenir = new Obtenir();
                obtenir.setIdentifiant(exchange.getProperty(ID_LIVRE, Long.class));
                marshal(exchange.getOut(), obtenir);
            }).to(PETALS_PREFIX + Routes.OBTENIR_LIVRE_INTERNE)
            .process(exchange -> {
                if (isJbiFailed(exchange)) {
                    if (isJbiFault(exchange.getIn())) {
//TODO :
                    }
                    return;
                }
                final ObtenirReponse obtenirReponse = unmarshal(exchange.getIn(), ObtenirReponse.class);

                final CreerReponse reponse = new CreerReponse();
                reponse.setLivre(obtenirReponse.getLivre());
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
