package fr.ausy.traing.petals.services.metiers.livre.operation;

import fr.ausy.traing.petals.services.metiers.livre.Routes;
import fr.ausy.traing.petals.services.metiers.livre.sql.SQLUtils;
import fr.ausy.training.petals.modele.biblotheque.livre._1.Rechercher;
import fr.ausy.training.petals.modele.biblotheque.livre._1.RechercherReponse;
import org.apache.commons.lang3.StringUtils;
import org.ow2.petals.components.sql.version_1.Result;

import javax.jbi.messaging.MessagingException;
import javax.xml.bind.JAXBException;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Définition de la route camel réalisant l'opération « rechercher livre ».
 */
public class RechercherLivreOperation extends AbstractOperation {
    public RechercherLivreOperation() throws JAXBException {
        super();
    }

    @Override
    public void configure() throws Exception {
        fromPetals(Routes.RECHERCHER_LIVRE)
            .process(exchange -> {
                final Rechercher rechercher = unmarshal(exchange.getIn(), Rechercher.class);

                final String sql = creerRequeteSql(rechercher);

                marshal(exchange.getOut(), SQL_OBJECT_FACTORY.createSql(sql));
            }).to(PETALS_PREFIX + Routes.SQL_SELECT)
            .process(exchange -> {
                if (isJbiFailed(exchange)) {
                    throw new MessagingException("Erreur SQL ", exchange.getException());
                }
                final Result result = unmarshal(exchange.getIn(), Result.class);
                final RechercherReponse reponse = new RechercherReponse();
                reponse.getLivre().addAll(
                    result.getRow().stream()
                        .filter(Objects::nonNull)
                        .map(AbstractOperation::transformerRowEnLivre)
                        .collect(Collectors.toList())
                );
                marshal(exchange.getOut(), reponse);
            });
    }

    /**
     * Création de la requete SQL de recherche à partir du message entrant.
     *
     * @param rechercher message entrant contenant les paramètres de recherches.
     * @return requete SQL de recherche.
     */
    protected static String creerRequeteSql(final Rechercher rechercher) {
        final StringBuilder sql = new StringBuilder("select livre_id, titre, resume, nb_page, isbn, auteur, langue, annee_publication from livre where ");

        boolean dejaUnPredicat = false;

        if (StringUtils.isNotBlank(rechercher.getTitre())) {
            sql.append("lower(titre) like '%")
                .append(SQLUtils.escapeString(rechercher.getTitre().toLowerCase()))
                .append("%'");
            dejaUnPredicat = true;
        }

        if (StringUtils.isNotBlank(rechercher.getResume())) {
            ajouterEtSqlSiBesoin(sql, dejaUnPredicat);
            sql.append("lower(resume) like '%")
                .append(SQLUtils.escapeString(rechercher.getResume().toLowerCase()))
                .append("%'");
            dejaUnPredicat = true;
        }

        if (Objects.nonNull(rechercher.getNbPage())) {
            ajouterEtSqlSiBesoin(sql, dejaUnPredicat);
            sql.append("nb_page=")
                .append(rechercher.getNbPage());
            dejaUnPredicat = true;
        }

        if (StringUtils.isNotBlank(rechercher.getIsbn())) {
            ajouterEtSqlSiBesoin(sql, dejaUnPredicat);
            sql.append("lower(isbn) like '%")
                .append(SQLUtils.escapeString(rechercher.getIsbn().toLowerCase()))
                .append("%'");
            dejaUnPredicat = true;
        }

        if (StringUtils.isNotBlank(rechercher.getAuteur())) {
            ajouterEtSqlSiBesoin(sql, dejaUnPredicat);
            sql.append("lower(auteur) like '%")
                .append(SQLUtils.escapeString(rechercher.getAuteur().toLowerCase()))
                .append("%'");
            dejaUnPredicat = true;
        }

        if (StringUtils.isNotBlank(rechercher.getLangue())) {
            ajouterEtSqlSiBesoin(sql, dejaUnPredicat);
            sql.append("lower(langue) like '%")
                .append(SQLUtils.escapeString(rechercher.getLangue().toLowerCase()))
                .append("%'");
            dejaUnPredicat = true;
        }

        if (Objects.nonNull(rechercher.getAnneePublication())) {
            ajouterEtSqlSiBesoin(sql, dejaUnPredicat);
            sql.append("annee_publication=")
                .append(rechercher.getAnneePublication());
            dejaUnPredicat = true;
        }

        // suppression du « where » si il  n'y a pas de paramètre
        if (Boolean.FALSE.equals(dejaUnPredicat)) {
            sql.replace(sql.length() - 7, sql.length(), "");
        }
        return sql.toString();
    }

    /**
     * Permet d'ajouter le « AND » SQL si il y a déjà un paramètre avant.
     *
     * @param sql            requête SQL à laquelle ajouter « AND »
     * @param dejaUnPredicat ajouter « AND » si {@code true}
     */
    private static void ajouterEtSqlSiBesoin(final StringBuilder sql, final boolean dejaUnPredicat) {
        if (dejaUnPredicat) {
            sql.append(" and ");
        }
    }
}
