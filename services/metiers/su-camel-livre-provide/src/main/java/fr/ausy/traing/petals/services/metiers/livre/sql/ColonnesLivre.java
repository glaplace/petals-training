package fr.ausy.traing.petals.services.metiers.livre.sql;

/**
 * Énumeration contenant la position d'une colonne "livre" dans les réponse SQL select d'un livre.
 * Cette énumération permet de réemployer facilement ces positions.
 */
public enum ColonnesLivre {

    LIVRE_ID(0),
    TITRE(1),
    RESUME(2),
    NB_PAGE(3),
    ISBN(4),
    AUTEUR(5),
    LANGUE(6),
    ANNEE_PUBLICATION(7);

    /**
     * Position de la colonne dans la réponse sqL
     */
    private final int position;

    ColonnesLivre(final int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
