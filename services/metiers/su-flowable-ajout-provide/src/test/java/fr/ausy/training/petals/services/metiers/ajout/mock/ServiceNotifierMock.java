package fr.ausy.training.petals.services.metiers.ajout.mock;

import fr.ausy.training.petals.bibliotheque.technique.notifier._1_0.NotifierMail;
import org.junit.rules.ExternalResource;

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.WebServiceException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ConcurrentMap;

/**
 * Mock du service de notification par mail
 */
public class ServiceNotifierMock extends ExternalResource implements NotifierMail {
    private static final String MOCK_URL = "http://localhost:12345/NotifierService";
    private Endpoint mockServiceEdp;
    private String isbn;
    private boolean notifierAppelee;

    @Override
    protected void before() throws Throwable {
        super.before();
        this.mockServiceEdp = Endpoint.publish(MOCK_URL, this);
    }

    @Override
    protected void after() {
        this.mockServiceEdp.stop();
        super.after();
    }

    public void registerEndPoint(final ConcurrentMap<QName, URL> endpoints) throws MalformedURLException {
        final QName qName = new QName("http://ausy.fr/training/petals/bibliotheque/technique/notifier/1.0",
                                      "autogenerate");
        endpoints.put(qName, new URL(MOCK_URL));
    }

    @Override
    public void notifier(final String sujet, final String message, final String destinataire) {
        notifierAppelee = true;
        if (!"Livre existe déjà".equals(sujet)) {
            throw new WebServiceException("Le sujet [" + sujet + "] n'est pas correcte");
        }
        final String messageAttendu = "Le livre " + isbn + " est existe déjà";
        if (!messageAttendu.equals(message)) {
            throw new WebServiceException("Le sujet [" + message + "] n'est pas correcte");
        }
        if (!"user@ausy.bzh".equals(destinataire)) {
            throw new WebServiceException("Le destinataire [" + destinataire + "] n'est pas correcte");
        }
    }

    public void reset() {
        isbn = null;
        notifierAppelee = false;
    }

    public boolean isNotifierAppelee() {
        return notifierAppelee;
    }

    public void setIsbn(final String isbn) {
        this.isbn = isbn;
    }
}
