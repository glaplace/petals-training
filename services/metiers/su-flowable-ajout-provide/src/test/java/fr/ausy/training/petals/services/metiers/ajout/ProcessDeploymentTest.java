package fr.ausy.training.petals.services.metiers.ajout;

import fr.ausy.training.petals.modele.bibliotheque._1.Livre;
import fr.ausy.training.petals.services.metiers.ajout.mock.ServiceNotifierMock;
import fr.ausy.training.petals.services.metiers.ajout.mock.ServiceTechniqueMock;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.test.Deployment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.ow2.petals.flowable.junit.PetalsFlowableRule;

import javax.xml.namespace.QName;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Classe de test du procesus d'ajout d'un livre dans le système.
 */
public class ProcessDeploymentTest {
    private static final int TIME_OUT_END_PROCESS = 120;
    private static final String PROCESS_ID = "processAjout";
    private final String ISBN = "9782811202569";

    // -------------------------
    // Prepare Flowable engine
    // -------------------------
    @Rule
    public final PetalsFlowableRule flowableRule = new PetalsFlowableRule();
    @Rule
    public final ServiceNotifierMock serviceNotifierMock = new ServiceNotifierMock();
    @Rule
    public final ServiceTechniqueMock serviceTechniqueMock = new ServiceTechniqueMock();

    @Before
    public void setUp() throws MalformedURLException {
        final ConcurrentMap<QName, URL> endpoints = new ConcurrentHashMap<>();
        serviceNotifierMock.registerEndPoint(endpoints);
        serviceTechniqueMock.registerEndPoint(endpoints);

        serviceNotifierMock.reset();
        serviceNotifierMock.setIsbn(ISBN);
        serviceTechniqueMock.reset();

        serviceTechniqueMock.setExpectedIsbn(ISBN);
        serviceTechniqueMock.setExpectedLivre(creerLivreattendu());

        ((ProcessEngineConfigurationImpl) this.flowableRule.getProcessEngine()
            .getProcessEngineConfiguration())
            .setWsOverridenEndpointAddresses(endpoints);
    }

    @Test
    @Deployment(resources = {"jbi/ajouter-livre.bpmn20.xml"})
    public void nominalTest() throws InterruptedException {
        // Le livre existe
        serviceTechniqueMock.setExpectedLivreExiste(Boolean.FALSE);
        // Démarerr le processus
        final ProcessInstance processInstance = startProcess();
        // S'assurer que le processus soit terminé
        flowableRule.waitEndOfProcessInstance(processInstance.getProcessInstanceId(), TIME_OUT_END_PROCESS);

        assertTrue(serviceTechniqueMock.isGetCalled());
        assertTrue(serviceTechniqueMock.isLivreExistCalled());
        assertTrue(serviceTechniqueMock.isSaveCalled());
        
        assertFalse(serviceNotifierMock.isNotifierAppelee());
    }

    @Test
    @Deployment(resources = {"jbi/ajouter-livre.bpmn20.xml"})
    public void livreExisteDejaTest() throws InterruptedException {
        // Le livre existe
        serviceTechniqueMock.setExpectedLivreExiste(Boolean.TRUE);
        // Démarerr le processus
        final ProcessInstance processInstance = startProcess();
        // S'assurer que le processus soit terminé
        flowableRule.waitEndOfProcessInstance(processInstance.getProcessInstanceId(), TIME_OUT_END_PROCESS);

        assertTrue(serviceTechniqueMock.isLivreExistCalled());
        assertTrue(serviceNotifierMock.isNotifierAppelee());
        assertFalse(serviceTechniqueMock.isGetCalled());
        assertFalse(serviceTechniqueMock.isSaveCalled());

    }

    /**
     * Démarre un processus d'emprunt
     */
    private ProcessInstance startProcess() throws InterruptedException {
        this.assertAllProcessStufDeployed();

        // Les variables à passer en paramètres au démarrage du processus
        final Map<String, Object> variables = new HashMap<>();
        variables.put("isbn", ISBN);

//        Démarrage du processus flowable
        final ProcessInstance processInstance = flowableRule.getRuntimeService().startProcessInstanceByKey(PROCESS_ID, variables);
        // Le processus est démarré et l'on a l'id
        assertNotNull(processInstance);
        assertEquals(PROCESS_ID, processInstance.getProcessDefinitionKey());

        return processInstance;
    }

    /**
     * Vérifier que le processus soit bien démarrer dans me moteur flowable.
     */
    private void assertAllProcessStufDeployed() {

        final ProcessDefinition process = flowableRule.getRepositoryService()
            .createProcessDefinitionQuery()
            .processDefinitionKey(PROCESS_ID)
            .singleResult();

        assertNotNull("Processus 'Ajouter' non déployé", process);
    }

    /**
     * Livre de référence.
     *
     * @return Livre
     */
    private Livre creerLivreattendu() {
        final Livre livre = new Livre();
        livre.setAuteur("David Gemmell");
        livre.setIsbn(ISBN);
        livre.setTitre("Druss la légende");
        livre.setResume("Son nom est Druss. Garçon violent et maladroit, il vit dans un petit village de paysans situé au pied des montagnes du pays drenaï. Bûcheron hargneux le jour, époux tendre le soir, il mène une existence paisible au milieu des bois. Jusqu'au jour où une troupe de mercenaires envahit le village pour tuer tous les hommes et capturer toutes les femmes. Druss, alors dans la forêt, arrive trop tard sur les lieux du massacre. Le village est détruit, son père gît dans une mare de sang. Et Rowena, sa femme, a disparu... S'armant de Snaga, une hache ayant appartenu à son grand-père, il part à la poursuite des ravisseurs. Déterminé à retrouver son épouse, rien ne devra se mettre en travers de son chemin. Mais la route sera longue pour ce jeune homme inexpérimenté. Car sa quête le mènera jusqu'au bout du monde. Il deviendra lutteur et mercenaire, il fera tomber des royaumes, il en élèvera d'autres, il combattra bêtes, hommes et démons. Car il est Druss... et voici sa légende...");
        livre.setLangue("fr");
        livre.setNbPage(507);
        livre.setAnneePublication(2009);
        return livre;
    }
}
