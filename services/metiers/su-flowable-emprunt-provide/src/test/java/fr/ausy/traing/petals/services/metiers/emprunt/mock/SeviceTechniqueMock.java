package fr.ausy.traing.petals.services.metiers.emprunt.mock;

import fr.ausy.training.petals.bibliotheque.emprunter.technique._1_0.EmprunterLivreTechnique;
import org.apache.commons.lang3.AnnotationUtils;
import org.junit.rules.ExternalResource;

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.WebServiceException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ConcurrentMap;

/**
 * Mock du service technique.
 */
public class SeviceTechniqueMock extends ExternalResource implements EmprunterLivreTechnique {
    private static final String MOCK_URL = "http://localhost:12345/TechniqueService";
    private Endpoint mockServiceEdp;

    private boolean livreEstDejaEmprunteAppelee;
    private boolean ajouterEmpruntAppelee;
    private boolean retournerEmpruntAppelee;

    private int livre;
    private int utilisateur;
    private Boolean estDejaEmprunte;

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
        final QName qName = new QName("http://ausy.fr/training/petals/bibliotheque/emprunter/technique/1.0",
            "autogenerate");
        endpoints.put(qName, new URL(MOCK_URL));
    }

    @Override
    public boolean livreEstDejaEmprunte(final long identifiant) {
        livreEstDejaEmprunteAppelee = true;
        if (identifiant != livre) {
            throw new WebServiceException("L'opération livreEstDejaEmprunte n'est pas appelée avec le bon identifiant (" + identifiant + ")");
        }
        return estDejaEmprunte;
    }

    @Override
    public void ajouterEmprunt(final long livre, final long utilisateur) {
        assertionLivreEtUtilisateur(livre, utilisateur, "ajouterEmprunt");
        ajouterEmpruntAppelee = true;
    }

    @Override
    public void retournerEmprunt(final long livre, final long utilisateur) {
        assertionLivreEtUtilisateur(livre, utilisateur, "retournerEmprunt");
        retournerEmpruntAppelee = true;
    }

    private void assertionLivreEtUtilisateur(final long livre, final long utilisateur, final String operation) {
        if (this.livre != livre) {
            throw new WebServiceException("L'opération " + operation + " n'est pas appelée avec le bon livre (" + livre + ")");
        }
        if (this.utilisateur != utilisateur) {
            throw new WebServiceException("L'opération " + operation + " n'est pas appelée avec le bon utilisateur (" + utilisateur + ")");
        }
    }

    public void setLivre(final int livre) {
        this.livre = livre;
    }

    public void setUtilisateur(final int utilisateur) {
        this.utilisateur = utilisateur;
    }

    public void setEstDejaEmprunte(final Boolean estDejaEmprunte) {
        this.estDejaEmprunte = estDejaEmprunte;
    }

    public boolean isLivreEstDejaEmprunteAppelee() {
        return livreEstDejaEmprunteAppelee;
    }

    public boolean isAjouterEmpruntAppelee() {
        return ajouterEmpruntAppelee;
    }

    public boolean isRetournerEmpruntAppelee() {
        return retournerEmpruntAppelee;
    }

    /**
     * Remise à zéro des données attendue par les différentes opérations du mock.
     */
    public void reset() {
        livreEstDejaEmprunteAppelee = false;
        ajouterEmpruntAppelee = false;
        retournerEmpruntAppelee = false;
        livre = -42;
        utilisateur = -42;
        estDejaEmprunte = null;
    }
}
