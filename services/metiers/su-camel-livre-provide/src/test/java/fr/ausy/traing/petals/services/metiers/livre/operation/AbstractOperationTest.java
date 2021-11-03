package fr.ausy.traing.petals.services.metiers.livre.operation;

import fr.ausy.training.petals.modele.biblotheque._1.Livre;
import org.ow2.petals.camel.junit.PetalsCamelTestSupport;

import java.util.Random;

/**
 * Classe mére des classes de tests permettant de mutualiser les comportements.
 */
public abstract class AbstractOperationTest extends PetalsCamelTestSupport {
    protected final int LIVRE_ID = new Random().nextInt();

    public abstract AbstractOperation getService();

    protected Livre creerLivre() {
        return creerLivre(true);
    }

    protected Livre creerLivre(final boolean avecId) {
        final Livre livre = new Livre();
        if (avecId) {
            livre.setLivreId(LIVRE_ID);
        }
        livre.setTitre("Druss la légende");
        livre.setLangue("fr");
        livre.setIsbn("9782811202569");
        livre.setAuteur("David Gemmell");
        livre.setNbPage(507);
        livre.setAnneePublication(2009);
        livre.setResume("Son nom est Druss. Garçon violent et maladroit, il vit dans un petit village de paysans situé au pied des montagnes du pays drenaï. Bûcheron hargneux le jour, époux tendre le soir, il mène une existence paisible au milieu des bois. Jusqu'au jour où une troupe de mercenaires envahit le village pour tuer tous les hommes et capturer toutes les femmes. Druss, alors dans la forêt, arrive trop tard sur les lieux du massacre. Le village est détruit, son père gît dans une mare de sang. Et Rowena, sa femme, a disparu... S'armant de Snaga, une hache ayant appartenu à son grand-père, il part à la poursuite des ravisseurs. Déterminé à retrouver son épouse, rien ne devra se mettre en travers de son chemin. Mais la route sera longue pour ce jeune homme inexpérimenté. Car sa quête le mènera jusqu'au bout du monde. Il deviendra lutteur et mercenaire, il fera tomber des royaumes, il en élèvera d'autres, il combattra bêtes, hommes et démons. Car il est Druss... et voici sa légende...");
        return livre;
    }
}
