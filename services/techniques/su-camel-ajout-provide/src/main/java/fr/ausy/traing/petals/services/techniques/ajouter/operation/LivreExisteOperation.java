package fr.ausy.traing.petals.services.techniques.ajouter.operation;

import fr.ausy.traing.petals.services.techniques.ajouter.Routes;
import fr.ausy.training.petals.bibliotheque.ajouter.technique._1.LivreExiste;
import fr.ausy.training.petals.bibliotheque.ajouter.technique._1.LivreExisteReponse;
import org.apache.commons.lang3.StringUtils;
import org.ow2.petals.components.sql.version_1.Result;

import javax.jbi.messaging.MessagingException;
import javax.xml.bind.JAXBException;

/**
 * Définition de la route camel réalisant l'opération « Livre existe ».
 */
public class LivreExisteOperation extends AbstractOperation {
    public LivreExisteOperation() throws JAXBException {
        super();
    }

    @Override
    public void configure() throws Exception {
        fromPetals(Routes.LIVRE_EXISTE)
            .process(exchange -> {
                final LivreExiste requete = unmarshal(exchange.getIn(), LivreExiste.class);
                System.out.println("requete = " + requete);
                if (StringUtils.isBlank(requete.getIsbn()) || requete.getIsbn().length() != 13) {
                    throw new MessagingException(
                        String.format(
                            "L'isbn ne peut être null et doit contenir 13 chiffres [%s] est invalide",
                            requete.getIsbn()
                        )
                    );
                }

                final String sql = "select count(1) from livre where isbn='" + escapeString(requete.getIsbn()) + "'";
                marshal(exchange.getOut(), SQL_OBJECT_FACTORY.createSql(sql));
            }).to(PETALS_PREFIX + Routes.SQL_SELECT)
            .process(exchange -> {
                if (!isJbiFault(exchange.getIn())) {
                    final Result result = unmarshal(exchange.getIn(), Result.class);
                    // On assume qu'il y a forément une ligne et une colonne de résultat.
                    final String sqlValue = result.getRow().get(0).getColumn().get(0).getValue();
                    System.out.println("sqlValue = " + sqlValue);
                    final LivreExisteReponse reponse = new LivreExisteReponse();
                    reponse.setExiste(true);
                    if (Integer.parseInt(sqlValue) == 0) {
                        reponse.setExiste(false);
                    }
                    marshal(exchange.getOut(), reponse);
                }
            });
    }
}
