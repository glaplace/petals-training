package fr.ausy.traing.petals.services.techniques.emprunter.operation;

import fr.ausy.traing.petals.services.techniques.emprunter.Routes;
import fr.ausy.training.petals.biblotheque.emprunter._1.LivreEstDejaEmprunte;
import fr.ausy.training.petals.biblotheque.emprunter._1.LivreEstDejaEmprunteReponse;
import org.apache.camel.Exchange;
import org.ow2.petals.camel.helpers.Step;
import org.ow2.petals.components.sql.version_1.Result;

import javax.jbi.messaging.MessagingException;
import javax.xml.bind.JAXBException;

import java.util.Objects;

/**
 * Déclaration de la route camel réalisant l'opération permettant de savoir un livre est déjà emprunter.
 * <p>
 * un livre est déjà emprunter si
 * <ul>
 *     <li>il existe une ligne en base avec livre_id </li>
 *     <li>Le livre n'est pas rendu : rendu == false</li>
 * </ul>
 */
public class LivreDejaEmprunteOperation extends AbstractOperation {
    public LivreDejaEmprunteOperation() throws JAXBException {
        super();
    }

    @Override
    public void configure() throws Exception {
        fromPetals(Routes.LIVRE_EST_DEJA_EMPRUNTE)
            .process(exchange -> {
                final LivreEstDejaEmprunte requete = unmarshal(exchange.getIn(), LivreEstDejaEmprunte.class);

                final String sql = String.format(
                    "select count(1) from pret where livre_id=%d and rendu=false",
                    requete.getIdentifiant());
                marshal(exchange.getOut(), SQL_OBJECT_FACTORY.createSql(sql));
            }).to(PETALS_PREFIX + Routes.SQL_SELECT)
            .process(new Step("Traitement réponse SQL") {
                @Override
                public void process(final Exchange exchange) throws Exception {

                    final Result result = unmarshal(exchange.getIn(), Result.class);
                    if (Objects.nonNull(result) && !result.getRow().isEmpty()) {
                        final String strval = result.getRow().get(0).getColumn().get(0).getValue();

                        try {
                            final int nb = Integer.parseInt(strval);
                            final LivreEstDejaEmprunteReponse reponse = new LivreEstDejaEmprunteReponse();
                            reponse.setDejaEmprunter(nb > 1);
                            marshal(exchange.getOut(), reponse);
                        } catch (final NumberFormatException e) {
                            throw new MessagingException("Erreur de conversion : la requete ne retourne pas un chiffre " + strval, e);
                        }
                    } else {
                        // TODO : faire mieux
                        throw new MessagingException("Erreur SQL");
                    }
                }
            })
        ;
    }
}
