package fr.ausy.traing.petals.services.metiers.emprunt.mock;

import fr.ausy.training.petals.bibliotheque.emprunter.metier._1_0.Emprunter;
import fr.ausy.training.petals.bibliotheque.emprunter.metier._1_0.PretInconnu;
import org.junit.rules.ExternalResource;

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.WebServiceException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

/**
 * Mock du service metier, utilisé pour l'appel de notification.
 */
public class ServieMetierMock extends ExternalResource implements Emprunter {
    private static final String MOCK_URL = "http://localhost:12345/MetierService";
    private Endpoint mockServiceEdp;
    private boolean notifierParentAppelee;
    private int processInstanceIdParent;

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
        final QName qName = new QName("http://ausy.fr/training/petals/bibliotheque/emprunter/metier/1.0",
            "autogenerate");
        endpoints.put(qName, new URL(MOCK_URL));
    }

    @Override
    public int retourner(final int livre, final int utilisateur, final int processInstanceIdCallback) {
        throw new WebServiceException("L'opération retourner ne doit pas être appelée");
    }

    @Override
    public int emprunter(final int livre, final int utilisateur) {
        throw new WebServiceException("L'opération emprunter ne doit pas être appelée");
    }

    @Override
    public void notifierRetourParent(final long processInstanceIdParent) throws PretInconnu {
        notifierParentAppelee = true;
        if (this.processInstanceIdParent == processInstanceIdParent) {
            throw new WebServiceException(
                String.format(
                    "L'opération n'est pas appelée avec le bon id %d != %d",
                    this.processInstanceIdParent,
                    processInstanceIdParent
                )
            );
        }
    }

    public void reset() {
        processInstanceIdParent = -42;
        notifierParentAppelee = false;
    }

    public boolean isNotifierParentAppelee() {
        return notifierParentAppelee;
    }

    public void setProcessInstanceIdParent(final int processInstanceIdParent) {
        this.processInstanceIdParent = processInstanceIdParent;
    }
}
