package fr.ausy.training.petals.services.metiers.ajout.mock;

import fr.ausy.training.petals.bibliotheque.ajouter.technique._1_0.AjouterLivreTechnique;
import fr.ausy.training.petals.modele.bibliotheque._1.Livre;
import org.apache.commons.lang3.StringUtils;
import org.junit.rules.ExternalResource;

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.WebServiceException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

/**
 * Mock du service technique du processus.
 */
public class ServiceTechniqueMock extends ExternalResource implements AjouterLivreTechnique {
    private static final String MOCK_URL = "http://localhost:12345/ServiceTechnique";
    private Endpoint mockServiceEdp;

    private Boolean expectedLivreExiste;
    private String expectedIsbn;
    private Livre expectedLivre;

    private boolean livreExistCalled;
    private boolean saveCalled;
    private boolean getCalled;

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
        final QName qName = new QName("http://ausy.fr/training/petals/bibliotheque/ajouter/technique/1.0",
                                      "autogenerate");
        endpoints.put(qName, new URL(MOCK_URL));
    }

    @Override
    public boolean livreExiste(final String isbn) {
        livreExistCalled = true;
        if (!expectedIsbn.equals(isbn)) {
            throw new WebServiceException("ISBN incorrect : " + isbn);
        }
        return expectedLivreExiste;
    }

    @Override
    public void sauvegarderLivre(final Livre livre) {
        saveCalled = true;
        if (Objects.isNull(livre)) {
            throw new WebServiceException("Le livre ne peut être null");
        }
        if (!expectedLivre.getAnneePublication().equals(livre.getAnneePublication())) {
            throw new WebServiceException("AnneePublication incorrecte : " + livre.getAnneePublication());
        }
        if (StringUtils.isBlank(livre.getAuteur()) || !expectedLivre.getAuteur().equals(livre.getAuteur())) {
            throw new WebServiceException("Auteur incorrecte : " + livre.getAuteur());
        }
        if (StringUtils.isBlank(livre.getIsbn()) || !expectedLivre.getIsbn().equals(livre.getIsbn())) {
            throw new WebServiceException("Isbn incorrecte : " + livre.getIsbn());
        }
        if (StringUtils.isBlank(livre.getLangue()) || !expectedLivre.getLangue().equals(livre.getLangue())) {
            throw new WebServiceException("Langue incorrecte : " + livre.getLangue());
        }
        if (Objects.nonNull(livre.getLivreId())) {
            throw new WebServiceException("LivreId doit être null");
        }
        if (livre.getNbPage() != expectedLivre.getNbPage()) {
            throw new WebServiceException("NbPage incorrecte : " + livre.getNbPage());
        }
        if (StringUtils.isBlank(livre.getResume()) || !expectedLivre.getResume().equals(livre.getResume())) {
            throw new WebServiceException("Resume incorrecte : " + livre.getResume());
        }
        if (StringUtils.isBlank(livre.getTitre()) || !expectedLivre.getTitre().equals(livre.getTitre())) {
            throw new WebServiceException("Titre incorrecte : " + livre.getTitre());
        }
    }

    @Override
    public Livre obtenirLivre(final String isbn) {
        getCalled = true;
        if (!expectedIsbn.equals(isbn)) {
            throw new WebServiceException("ISBN incorrect : " + isbn);
        }
        return expectedLivre;
    }

    /**
     * Les données de références sont réinitialisée.
     */
    public void reset() {
        expectedLivre = null;
        expectedIsbn = null;
        expectedLivreExiste = null;
        livreExistCalled = false;
        saveCalled = false;
        getCalled = false;

    }

    public void setExpectedLivreExiste(final Boolean expectedLivreExiste) {
        this.expectedLivreExiste = expectedLivreExiste;
    }

    public void setExpectedIsbn(final String expectedIsbn) {
        this.expectedIsbn = expectedIsbn;
    }

    public void setExpectedLivre(final Livre expectedLivre) {
        this.expectedLivre = expectedLivre;
    }

    public boolean isLivreExistCalled() {
        return livreExistCalled;
    }

    public boolean isSaveCalled() {
        return saveCalled;
    }

    public boolean isGetCalled() {
        return getCalled;
    }
}
