package fr.ausy.traing.petals.services.techniques.emprunter;

public final class Routes {
    private Routes() {
        // CLasse utilitaire ne doit pas etre instanciéé.
    }

    public static final String LIVRE_EST_DEJA_EMPRUNTE = "livreEstDejaEmprunte";
    public static final String AJOUTER_EMPRUNT = "ajouterEmprunt";
    public static final String RETOURNER_EMPRUNT = "retournerEmprunt";

    // Opérations autre services (consommé par ce service)
    public static final String SQL_SELECT = "sql-select";
    public static final String SQL_INSERT = "sql-insert";
    public static final String SQL_UPDATE = "sql-update";
    public static final String SQL_DELETE = "sql-delete";
}
