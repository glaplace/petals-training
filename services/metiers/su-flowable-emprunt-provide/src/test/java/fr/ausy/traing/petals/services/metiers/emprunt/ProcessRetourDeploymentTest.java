package fr.ausy.traing.petals.services.metiers.emprunt;

import fr.ausy.traing.petals.services.metiers.emprunt.mock.ServiceNotifierMock;
import fr.ausy.traing.petals.services.metiers.emprunt.mock.ServieMetierMock;
import fr.ausy.traing.petals.services.metiers.emprunt.mock.SeviceTechniqueMock;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.test.Deployment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.ow2.petals.flowable.junit.PetalsFlowableRule;

import javax.xml.namespace.QName;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test du processus flowable « Emprunter »
 */
public class ProcessRetourDeploymentTest {
    private static final int TIME_OUT_END_PROCESS = 120;
    private static final int UTILISATEUR_ID = Math.abs(new Random().nextInt());
    private static final int LIVRE_ID = Math.abs(new Random().nextInt());
    private static final int PROCESS_ID_A_NOTIFIER = Math.abs(new Random().nextInt());
    private static final String PROCESS_ID_RETOUR = "processusRetour";

    // -------------------------
    // Prepare Flowable engine
    // -------------------------
    @Rule
    public final PetalsFlowableRule flowableRule = new PetalsFlowableRule();
    @Rule
    public final ServiceNotifierMock serviceNotifierMock = new ServiceNotifierMock();
    @Rule
    public final ServieMetierMock serviceMetierMock = new ServieMetierMock();
    @Rule
    public final SeviceTechniqueMock serviceTechniqueMock = new SeviceTechniqueMock();

    @Before
    public void setUp() throws Exception {
        final ConcurrentMap<QName, URL> endpoints = new ConcurrentHashMap<>();
        serviceNotifierMock.registerEndPoint(endpoints);
        serviceMetierMock.registerEndPoint(endpoints);
        serviceTechniqueMock.registerEndPoint(endpoints);
        serviceNotifierMock.reset();
        serviceMetierMock.reset();
        serviceTechniqueMock.reset();

        serviceTechniqueMock.setUtilisateur(UTILISATEUR_ID);
        serviceTechniqueMock.setLivre(LIVRE_ID);
        serviceNotifierMock.setLivre(LIVRE_ID);

        ((ProcessEngineConfigurationImpl) this.flowableRule.getProcessEngine().getProcessEngineConfiguration()).setWsOverridenEndpointAddresses(endpoints);
    }

    @Test
    @Deployment(resources = {"jbi/rendre_livre.bpmn20.xml"})
    public void retourLivreTest() throws InterruptedException {
        serviceTechniqueMock.setEstDejaEmprunte(false);
        // Démarerr le processus
        final ProcessInstance processInstance = startProcess();

         // S'assurer que le processus soit terminé
        flowableRule.waitEndOfProcessInstance(processInstance.getProcessInstanceId(), TIME_OUT_END_PROCESS);

        // Assertions finales

        // Service non appelés
        assertFalse(serviceTechniqueMock.isLivreEstDejaEmprunteAppelee());
        assertFalse(serviceTechniqueMock.isAjouterEmpruntAppelee());
        assertFalse(serviceNotifierMock.isNotifierAppelee());

        // le service appelé
        assertTrue(serviceMetierMock.isNotifierParentAppelee());
        assertTrue(serviceTechniqueMock.isRetournerEmpruntAppelee());
    }

    @Test
    @Deployment(resources = {"jbi/rendre_livre.bpmn20.xml"})
    public void demarrageTest() throws InterruptedException {
        serviceTechniqueMock.setEstDejaEmprunte(true);
        final ProcessInstance processInstance = startProcess();
        assertNotNull(processInstance);
    }

    /**
     * Démarre un processus d'emprunt
     */
    private ProcessInstance startProcess() throws InterruptedException {
        this.assertAllProcessStufDeployed();

        // Les variables à passer en paramètres au démarrage du processus
        final Map<String, Object> variables = new HashMap<>();
        variables.put("livre", LIVRE_ID);
        variables.put("utilisateur", UTILISATEUR_ID);
        variables.put("processInstanceIdCallback", PROCESS_ID_A_NOTIFIER);

//        Démarrage du processus flowable
        final ProcessInstance processInstance = flowableRule.getRuntimeService().startProcessInstanceByKey(PROCESS_ID_RETOUR, variables);
        // Le processus est démarré et l'on a l'id
        assertNotNull(processInstance);
        assertEquals(PROCESS_ID_RETOUR, processInstance.getProcessDefinitionKey());

        return processInstance;
    }

    /**
     * Vérifier que le processus soit bien démarrer dans me moteur flowable.
     */
    private void assertAllProcessStufDeployed() {

        final ProcessDefinition process = flowableRule.getRepositoryService()
            .createProcessDefinitionQuery()
            .processDefinitionKey(PROCESS_ID_RETOUR)
            .singleResult();

        assertNotNull("Processus 'Emprunter' non déployé", process);
    }
}
