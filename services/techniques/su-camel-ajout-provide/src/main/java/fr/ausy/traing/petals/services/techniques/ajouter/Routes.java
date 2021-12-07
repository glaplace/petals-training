package fr.ausy.traing.petals.services.techniques.ajouter;

public final class Routes {
    private Routes() {
        // classe utilitaire ne doit pas être instanciée
    }

    public static final String LIVRE_EXISTE = "livreExiste";
    public static final String OBTENIR_LIVRE = "obtenirLivre";
    public static final String SAUVEGARDER_LIVRE = "sauvegarderLivre";

    public static final String SQL_SELECT = "sql-select";
    public static final String SQL_INSERT = "sql-insert";

    public static final String OBTENIR_LIVRE_OL = "obtenir-livre-ol";
    public static final String OBTENIR_AUTEUR_OL = "obtenir-auteur-ol";
}
