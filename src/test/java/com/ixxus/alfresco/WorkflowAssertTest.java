package com.ixxus.alfresco;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.ImmutableSet;
import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;

/**
 * @author Bert Blommers
 */
@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class WorkflowAssertTest extends AbstractServiceTest {

    private static final String WORKFLOW_DEF_NAME = "activiti$activitiReview";
    private final static String DESCRIPTION = "DESCRIPTION";

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;

    @Autowired
    private Repository repository;

    @Autowired
    @Qualifier("WorkflowService")
    private WorkflowService workflowService;

    @Autowired
    private PersonService personService;

    private NodeRef unknownNode, attachment1, attachment2;
    private WorkflowInstance workflowInstance;

    @Before
    public void setup() throws Exception {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        NodeRef companyHome = repository.getCompanyHome();
        unknownNode = nodeService.createNode(companyHome, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_CONTENT).getChildRef();
        attachment1 = nodeService.createNode(companyHome, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_CONTENT).getChildRef();
        attachment2 = nodeService.createNode(companyHome, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_CONTENT).getChildRef();
        //
        workflowInstance = createWorkflow();
    }

    @Test
    public void test_workflow_conditions() {
        WorkflowAssert.assertThat(workflowInstance)
                        .isInitiator(AuthenticationUtil.getAdminUserName())
                        .hasDescription(DESCRIPTION)
                        .hasNumberOfPackageItems(2)
                        .hasPackageItemAttached(attachment1)
                        .hasPackageItemsAttached(ImmutableSet.of(attachment1, attachment2));
    }

    @Test
    public void test_failure_when_workflow_does_not_have_item_attached() {
        exception.expect(AssertionError.class);

        WorkflowAssert.assertThat(workflowInstance)
                        .hasPackageItemAttached(unknownNode);
    }

    @Test
    public void test_failure_when_workflow_has_incorrect_nr_of_items_attached() {
        exception.expect(AssertionError.class);

        WorkflowAssert.assertThat(workflowInstance)
                        .hasNumberOfPackageItems(1);
    }

    @Test
    public void test_failure_when_workflow_has_different_initiator() {
        exception.expect(AssertionError.class);

        WorkflowAssert.assertThat(workflowInstance).isInitiator("unknown User");
    }

    @Test
    public void test_failure_when_workflow_has_different_description() {
        exception.expect(AssertionError.class);

        WorkflowAssert.assertThat(workflowInstance).hasDescription("Unknown Description");
    }

    @Test
    public void test_attachment_properties_can_be_tested() {
        String attachment1Name = (String) nodeService.getProperty(attachment1, ContentModel.PROP_NAME);

        WorkflowAssert.assertThat(workflowInstance)
                        .hasPackageItemAttached(attachment1)
                        .attachment(attachment1)
                        .hasPropertyValue(ContentModel.PROP_NAME, attachment1Name);
    }

    private WorkflowInstance createWorkflow() {
        String workflowDefinitionId = getWorkflowDefIdByName(WORKFLOW_DEF_NAME);
        NodeRef assignee = personService.getPerson(AuthenticationUtil.getAdminUserName());
        return createWorkflow(workflowDefinitionId, assignee, DESCRIPTION, attachment1, attachment2);
    }

    /**
     * @param workflowDefName
     * @return
     */
    private String getWorkflowDefIdByName(String workflowDefName) {
        return workflowService.getAllDefinitions().stream()
                        .filter(workflowDefinition -> workflowDefinition.getName().equals(workflowDefName))
                        .findFirst().get().getId();
    }

    /**
     * Creates a workflow and attaches it to all of the node refs
     *
     * @param workflowParams - any extra parameters in a map
     * @param nodeRefs The node refs to attach the workflow to
     * @return the ID of the workflow that was created
     */
    private WorkflowInstance createWorkflow(String workflowDefId, final NodeRef assignee, String description, final NodeRef... nodeRefs) {
        final NodeRef wfPackage = createWorkflowPackage(Arrays.asList(nodeRefs));

        final Map<QName, Serializable> parameters = new HashMap<>();
        parameters.put(WorkflowModel.ASSOC_ASSIGNEE, assignee);
        parameters.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);
        parameters.put(WorkflowModel.PROP_CONTEXT, wfPackage);
        parameters.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, description);
        parameters.put(WorkflowModel.PROP_SEND_EMAIL_NOTIFICATIONS, false);

        final WorkflowPath wfPath = workflowService.startWorkflow(workflowDefId, parameters);
        final String workflowId = wfPath.getInstance().getId();
        final WorkflowTask startTask = workflowService.getStartTask(workflowId);
        workflowService.endTask(startTask.getId(), null);
        return wfPath.getInstance();
    }

    /**
     * Creates and returns a workflow package. This package will contain the
     * given {@link NodeRef} <code>items</code>. The item name is used to create
     * a valid local name for the association between the package and the item.
     * The workflow package will contain only the existing items.
     *
     * @param items
     * @return {@link NodeRef} of the newly created workflow package
     */
    private NodeRef createWorkflowPackage(final List<NodeRef> items) {
        ParameterCheck.mandatoryCollection("items", items);
        final NodeRef wfPackage = workflowService.createPackage(null);

        for (final NodeRef item : items) {
            if ((item != null) && nodeService.exists(item)) {
                final String itemName = (String) nodeService.getProperty(item, ContentModel.PROP_NAME);
                nodeService.addChild(wfPackage, item, WorkflowModel.ASSOC_PACKAGE_CONTAINS,
                                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(itemName)));
            }
        }

        return wfPackage;
    }
}
