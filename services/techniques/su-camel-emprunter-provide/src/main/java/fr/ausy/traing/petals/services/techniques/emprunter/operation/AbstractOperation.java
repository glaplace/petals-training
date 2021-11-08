package fr.ausy.traing.petals.services.techniques.emprunter.operation;

import org.apache.camel.Message;
import org.ow2.petals.camel.helpers.MarshallingHelper;
import org.ow2.petals.camel.helpers.PetalsRouteBuilder;
import org.ow2.petals.components.sql.version_1.ColumnType;
import org.ow2.petals.components.sql.version_1.ObjectFactory;
import org.ow2.petals.components.sql.version_1.RowType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import java.util.List;

/**
 * Classe de base permettant l'implémentation d'une route camel réalisation une opération du service.
 */
abstract class AbstractOperation extends PetalsRouteBuilder {
    protected static final ObjectFactory SQL_OBJECT_FACTORY = new ObjectFactory();
    /**
     * Préfix Camel des rouutes gérées par petals
     */
    protected static final String PETALS_PREFIX = "petals:";

    /**
     * Nom de la propriété camel contenant la requête initiale.
     */
    protected static final String REQUETE = AbstractOperation.class.getName() + ".requete";

    private final MarshallingHelper marshalling;

    protected AbstractOperation() throws JAXBException {
        this.marshalling = new MarshallingHelper(createJaxbContext());
    }

    public MarshallingHelper getMarshalling() {
        return marshalling;
    }

    /**
     * UnMarshall XML -> JAVA.
     *
     * @param msg          message camel contenant le xml à transformer en java
     * @param declaredType le type d'objet que l'on souhaite obtenir à partir du xml
     * @param <T>          le type d'objet que l'on souhaite obtenir à partir du xml
     * @return Objet java, de type T, image du XML contenu dans le message
     * @throws JAXBException exeption levée en cas d'erreur de conversion XML -> java
     */
    public <T> T unmarshal(final Message msg, final Class<T> declaredType) throws JAXBException {
        synchronized (marshalling) {
            return marshalling.unmarshal(msg, declaredType);
        }
    }

    /**
     * Marshall java -> XML.
     *
     * @param msg message dans lequel "mettre" le XML image de {@code t}
     * @param t   objet à convertir en java
     * @throws JAXBException Exception levée en cas d'erreur de conversion java -> XML
     */
    public <T> void marshal(final Message msg, final T t) throws JAXBException {
        synchronized (marshalling) {
            marshalling.marshal(msg, t);
        }
    }

    /**
     * Création du contexte JAXB commun à toutes les opérations.
     *
     * @return {@code JAXBContext} à utiliser pour les opération JAXB
     * @throws JAXBException Levée en cas d'erreur à la création du {@code JAXBContext}
     */
    private static JAXBContext createJaxbContext() throws JAXBException {
        return JAXBContext.newInstance(
            org.ow2.petals.components.sql.version_1.Result.class,
            fr.ausy.training.petals.biblotheque.emprunter._1.ObjectFactory.class
        );
    }
}
