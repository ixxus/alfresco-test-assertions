package com.ixxus.alfresco;

import java.util.List;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.ParameterCheck;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

/**
 * Custom assertions for various operations around Alfresco workflows
 *
 * @author Xurxo Pantin
 */
public class WorkflowAssert extends AbstractAssert<WorkflowAssert, WorkflowInstance> {

    private static NodeService nodeService;

    /**
     * Only constructor - use the assertThat-method
     *
     * @param workflowInstance an instance of a workflowInstance
     */
    private WorkflowAssert(final WorkflowInstance workflowInstance) {
        super(workflowInstance, WorkflowAssert.class);
    }

    /**
     * Setter method for nodeService
     *
     * @param nodeService
     */
    public static void setNodeService(final NodeService nodeService) {
        WorkflowAssert.nodeService = nodeService;
    }

    /**
     *
     * @param workflowInstance instance of a workflowInstance
     * @return
     */
    public static WorkflowAssert assertThat(final WorkflowInstance workflowInstance) {
        return new WorkflowAssert(workflowInstance);
    }

    /**
     * Checks that the item is attached to the workflow
     *
     * @param expectedItem the expected item attached to the workflow
     * @return
     */
    public WorkflowAssert hasPackageItemAttached(final NodeRef expectedItem) {
        final List<NodeRef> wfItems = getPackageItems(this.actual.getWorkflowPackage());
        Assertions.assertThat(wfItems)
                        .as(String.format("The item '%s' should be an attachment on the workflow", expectedItem.toString()))
                        .contains(expectedItem);
        return this;
    }

    /**
     * Checks that the items are the only ones attached to the workflow
     *
     * @param expectedItems the expectedItems attached to the workflow
     * @return
     */
    public WorkflowAssert hasPackageItemsAttached(final NodeRef... expectedItems) {
        final List<NodeRef> wfItems = getPackageItems(this.actual.getWorkflowPackage());
        Assertions.assertThat(wfItems).as("The number of attached items on the workflow has to match")
                        .hasSize(expectedItems.length);
        for (final NodeRef item : expectedItems) {
            Assertions.assertThat(wfItems)
                            .as(String.format("The item '%s' should be an attachment on the workflow", item.toString()))
                            .contains(item);
        }
        return this;
    }

    /**
     * Checks that the workflow only has x nr of items attached
     * 
     * @param nrOfItems
     * @return
     */
    public WorkflowAssert hasNumberOfPackageItems(final int nrOfItems) {
        final List<NodeRef> wfItems = getPackageItems(this.actual.getWorkflowPackage());
        Assertions.assertThat(wfItems).as("The number of attached items on the workflow has to match")
                        .hasSize(nrOfItems);
        return this;
    }

    /**
     * Checks that the username is the initiator of the workflow
     *
     * @param userName the userName parameter to be matched
     * @return
     */
    public WorkflowAssert hasInitiator(final String userName) {
        final String initiatorName = (String) nodeService.getProperty(this.actual.getInitiator(),
                        ContentModel.PROP_USERNAME);
        Assertions.assertThat(initiatorName).as("The initiator should match the user name").isEqualTo(userName);
        return this;
    }

    /**
     * Checks that the description passed matches the one of the workflow
     *
     * @param description the description parameter to be matches
     * @return
     */
    public WorkflowAssert hasDescription(final String description) {
        Assertions.assertThat(this.actual.getDescription())
                        .as("The workflow description should match the expected description").isEqualTo(description);
        return this;
    }

    /**
     * Verify properties of an attachment using {@link NodeAssert}
     * 
     * @param attachment
     * @return
     */
    public NodeAssert where(NodeRef attachment) {
        return NodeAssert.assertThat(attachment);
    }

    /**
     * Returns a list of {@link NodeRef} using the given
     * <code>packageNodeRef</code> as container
     *
     * @param packageNodeRef {@link NodeRef}
     * @return list of {@link NodeRef}
     */
    private List<NodeRef> getPackageItems(final NodeRef packageNodeRef) {
        ParameterCheck.mandatory("packageNodeRef", packageNodeRef);

        final List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(packageNodeRef, WorkflowModel.ASSOC_PACKAGE_CONTAINS,
                        RegexQNamePattern.MATCH_ALL);
        return childAssocs.stream().map(childAssoc -> childAssoc.getChildRef()).collect(Collectors.toList());
    }
}
