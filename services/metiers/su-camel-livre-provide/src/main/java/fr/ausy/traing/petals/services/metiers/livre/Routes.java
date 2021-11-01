package fr.ausy.traing.petals.services.metiers.livre;

public class Routes {
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
}
