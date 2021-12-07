package fr.ausy.traing.petals.services.techniques.ajouter.operation;

import fr.ausy.traing.petals.services.techniques.ajouter.Routes;
import fr.ausy.training.petals.bibliotheque.ajouter.technique._1.SauvegarderLivre;
import fr.ausy.training.petals.modele.bibliotheque._1.Livre;
import org.apache.commons.lang3.StringUtils;
import org.ow2.petals.components.sql.version_1.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;

/**
 * Définition de la route camel Réalisant l'opération « sauvegarder livre ».
 */
public class SauvegarderLivreOperation extends AbstractOperation {
    private static Logger LOGGER = LoggerFactory.getLogger(SauvegarderLivreOperation.class);

    public SauvegarderLivreOperation() throws JAXBException {
        super();
    }

    @Override
    public void configure() throws Exception {
        fromPetals(Routes.SAUVEGARDER_LIVRE)
            .process(exchange -> {
                final SauvegarderLivre requete = unmarshal(exchange.getIn(), SauvegarderLivre.class);
                LOGGER.debug("Livre à insérer en base {}", requete.getLivre());
                marshal(exchange.getOut(), SQL_OBJECT_FACTORY.createSql(creerRequêteSql(requete.getLivre())));
            }).to(PETALS_PREFIX + Routes.SQL_SELECT)
            .process(exchange -> {
                if (!isJbiFailed(exchange)) {
                    final Result result = unmarshal(exchange.getIn(), Result.class);
                    LOGGER.error("Nouvel ID inséré [{}]", result.getRow().get(0).getColumn().get(0).getValue());
                }
            });
    }

    /**
     * Création de la requête SQL d'insertion en base.
     *
     * @param livre Le livre à insérer en base
     * @return requête SQL créée à part de {@code livre}
     */
    protected String creerRequêteSql(final Livre livre) {
        return String.format(
            "insert into livre (TITRE, RESUME, NB_PAGE, ISBN, AUTEUR, LANGUE, ANNEE_PUBLICATION) "
            + "values ('%s', '%s', %d, '%s', '%s', '%s', %d) returning livre_id",
            escapeString(livre.getTitre()),
            escapeString(livre.getResume()),
            livre.getNbPage(),
            escapeString(livre.getIsbn()),
            StringUtils.isNotBlank(livre.getAuteur()) ? escapeString(livre.getAuteur()) : "null",
            escapeString(livre.getLangue()),
            livre.getAnneePublication()
        );
    }
}
