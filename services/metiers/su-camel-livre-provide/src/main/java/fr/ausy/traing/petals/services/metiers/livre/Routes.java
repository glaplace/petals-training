package fr.ausy.traing.petals.services.metiers.livre;

/**
 * Contient les noms des routes utilisées par ce service dans petals.
 */
public final class Routes {
    private Routes() {
        // Classe utilitaire ne doit pas être instanciée
    }

    // opérations du service
    public static final String OBTENIR_LIVRE = "obtenir-livre";
    public static final String CREER_LIVRE = "creer-livre";
    public static final String MODIFIER_LIVRE = "modifier-livre";
    public static final String SUPPRIMER_LIVRE = "supprimer-livre";
    public static final String RECHERCHER_LIVRE = "rechercher-livre";

    // Opérations autre services (consommé par ce service)
    public static final String SQL_SELECT = "sql-select";
    public static final String SQL_INSERT = "sql-insert";
    public static final String SQL_UPDATE = "sql-update";
    public static final String SQL_DELETE = "sql-delete";


    public static final String OBTENIR_LIVRE_INTERNE = "obtenir-livre-interne";
    public static final String RECHERCHER_LIVRE_INTERNE = "rechercher-livre-interne";
}
