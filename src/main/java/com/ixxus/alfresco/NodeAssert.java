/**
 * Copyright 2017 Ixxus Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.ixxus.alfresco;

import java.io.Serializable;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;

/**
 * Custom assertion specifically for various operations around an Alfresco node
 *
 * @author Bert Blommers
 * @author Simon Hutchinson
 * @author Alex Lu
 */
public class NodeAssert extends AbstractAssert<NodeAssert, NodeRef> {
    private static NodeService nodeService;

    private static ContentService contentService;

    /**
     * Only constructor - use the assertThat-method
     *
     * @param nodeRef an instance of a nodeRef
     */
    private NodeAssert(final NodeRef nodeRef) {
        super(nodeRef, NodeAssert.class);
    }

    /**
     * Setter for Alfresco NodeService
     *
     * @param nodeService an instance of NodeService
     */
    public static void setNodeService(final NodeService nodeService) {
        NodeAssert.nodeService = nodeService;
    }

    /**
     * Setter for Alfresco ContentService
     * 
     * @param contentService an instance of ContentService
     */
    public static void setContentService(final ContentService contentService) {
        NodeAssert.contentService = contentService;
    }

    /**
     * @param nodeRef instance of a nodeRef
     * @return The created node assertion object
     */
    public static NodeAssert assertThat(final NodeRef nodeRef) {
        return new NodeAssert(nodeRef);
    }

    /**
     * Check if a node's type matches a given expected type
     *
     * @param expectedType expected node type
     * @return The created node assertion object
     */
    public NodeAssert isType(final QName expectedType) {
        final QName actualType = nodeService.getType(this.actual);
        if (!actualType.isMatch(expectedType)) {
            failWithMessage("Node's type <%s> does not match expected type of <%s> ", actualType, expectedType);
        }
        return this;
    }

    /**
     * Check if a node exists. Fail if node does not exist.
     *
     * @return The created node assertion object
     */
    public NodeAssert exists() {
        final boolean exist = existsForNode();
        if (!exist) {
            failWithMessage("Node <%s> does not exist", this.actual);
        }
        return this;
    }

    private boolean existsForNode() {
        isNotNull();
        return nodeService.exists(this.actual);
    }

    /**
     * Check if a node does not exist. Fail if node does not exist;
     *
     * @return The created node assertion object
     */
    public NodeAssert doesNotExist() {
        final boolean exist = existsForNode();
        if (exist) {
            failWithMessage("Node <%s> exists", this.actual);
        }
        return this;
    }

    /**
     * Check if a node has a given aspect. Fail if node does not have the given
     * aspect
     *
     * @param aspect expected aspect for a node
     * @return The created node assertion object
     */
    public NodeAssert hasAspect(final QName aspect) {
        final boolean hasAspect = hasAspectforNode(aspect);
        if (!hasAspect) {
            failWithMessage("Node <%s> does not have aspect <%s>", super.actual, aspect);
        }
        return this;
    }

    private boolean hasAspectforNode(final QName aspect) {
        exists();
        return nodeService.hasAspect(this.actual, aspect);
    }

    /**
     * Check if a node does not have a given aspect. Fail if node has the given
     * aspect
     *
     * @param aspect expected aspect
     * @return The created node assertion object
     */
    public NodeAssert doesNotHaveAspect(final QName aspect) {
        final boolean hasAspect = hasAspectforNode(aspect);
        if (hasAspect) {
            failWithMessage("Node <%s> should not have aspect <%s>", this.actual, aspect);
        }
        return this;
    }

    /**
     * Check if a node has a property that matches an expected value. Fail if
     * node does not exist and does not have matched value
     *
     * @param property name of a property
     * @param expectedValue value of a property
     * @return The created node assertion object
     */
    public NodeAssert hasPropertyValue(final QName property, final Serializable expectedValue) {
        exists();
        final Serializable actualValue = nodeService.getProperty(this.actual, property);
        Assertions.assertThat(actualValue).isEqualTo(expectedValue);
        return this;
    }

    /**
     * Check if a node has a property that does not match an expected value.
     * Fail if matches.
     *
     * @param property name of a property
     * @param expectedValue value of a property
     * @return The created node assertion object
     */
    public NodeAssert doesNotHavePropertyValue(final QName property, final Serializable expectedValue) {
        exists();
        final Serializable actualValue = nodeService.getProperty(this.actual, property);
        Assertions.assertThat(actualValue).isNotEqualTo(expectedValue);
        return this;
    }

    public NodeAssert propertyValue(QName property, Condition<Serializable> expectedCondition) {
        exists();
        final Serializable actualValue = nodeService.getProperty(this.actual, property);
        Assertions.assertThat(expectedCondition.matches(actualValue)).as(expectedCondition.description()).isTrue();
        return this;
    }

    /**
     * Check if a node has a multiple value property with the provided value as
     * a member.
     *
     * @param property The multi-value property
     * @param expectedValue The value to test the existence of
     * @return The created node assertion object
     */
    public <T> NodeAssert hasMultiplePropertyMember(final QName property, final T expectedValue) {
        exists();
        final List<T> values = (List<T>) nodeService.getProperty(this.actual, property);
        if (!values.contains(expectedValue)) {
            failWithMessage("Node <%s> should have value <%s> as a member of <%s>", this.actual, expectedValue,
                            property);
        }

        return this;
    }

    /**
     * Check if a node has a multiple value property with the provided value as
     * a member. Fail if matches.
     *
     * @param property name of a property
     * @param expectedValue value of a property
     * @return The created node assertion object
     */
    public <T> NodeAssert doesNotHaveMultiplePropertyMember(final QName property, final T expectedValue) {
        exists();
        final List<T> values = (List<T>) nodeService.getProperty(this.actual, property);
        if ((values != null) && values.contains(expectedValue)) {
            failWithMessage("Node <%s> should not have value <%s> as a member of <%s>", this.actual, expectedValue,
                            property);
        }

        return this;
    }

    /**
     * Check if a node has a target association to the provided target. Fail if
     * matches.
     *
     * @param qnamePattern the association qname pattern to match against
     * @param target The {@link NodeRef} of the target
     * @return The created node assertion object
     */
    public NodeAssert doesNotHaveTargetAssociationTo(final QName qnamePattern, final NodeRef target) {
        exists();
        for (final AssociationRef assocRef : nodeService.getTargetAssocs(this.actual, qnamePattern)) {
            if (assocRef.getTargetRef().equals(target)) {
                failWithMessage("Node <%s> should not be related via target association <%s>", this.actual, assocRef);
            }
        }
        return this;
    }

    /**
     * Check if a node has any target association with the given qname. Fail if
     * &gt; 0.
     *
     * @param qnamePattern the association qname pattern to match against
     * @return The created node assertion object
     */
    public NodeAssert doesNotHaveTargetAssociation(final QName qnamePattern) {
        exists();
        final List<AssociationRef> targetAssocs = nodeService.getTargetAssocs(this.actual, qnamePattern);
        if (!targetAssocs.isEmpty()) {
            failWithMessage("Node <%s> should not have any targets for association <%s>", this.actual, qnamePattern);
        }
        return this;
    }

    /**
     * Check that {@link ContentModel#PROP_CONTENT} contains (non-empty)
     * content, i.e. content with size &gt; 0
     *
     * @return The created node assertion object
     */
    public NodeAssert hasContent() {
        return hasContent(ContentModel.PROP_CONTENT);
    }

    /**
     * Check that a property contains (non-empty) content, i.e. content with
     * size &gt; 0
     *
     * @param contentQName Which property to check
     * @return The created node assertion object
     */
    public NodeAssert hasContent(final QName contentQName) {
        isNotNull();
        exists();
        final Serializable prop = nodeService.getProperty(actual, contentQName);
        if (prop != null) {
            final ContentData content = (ContentData) prop;
            if (content.getSize() == 0) {
                failWithMessage("Node <%s> should have content with size > 0 ", actual);
            }
        } else {
            failWithMessage("Node <%s> should have content ", actual);
        }
        return this;
    }

    /**
     * Check that {@link ContentModel#PROP_CONTENT} has specific content
     *
     * @param expected String-representation of the expected content
     * @return The created node assertion object
     */
    public NodeAssert hasContent(final String expected) {
        return hasContent(ContentModel.PROP_CONTENT, expected);
    }

    /**
     * Check that a property has specific content
     *
     * @param contentQName Which property to check
     * @param expected String-representation of the expected content
     * @return The created node assertion object
     */
    public NodeAssert hasContent(final QName contentQName, final String expected) {
        isNotNull();
        exists();
        final ContentReader reader = contentService.getReader(actual, contentQName);
        if (reader != null) {
            Assertions.assertThat(reader.getContentString()).as("Content should be equal").isEqualTo(expected);
        } else {
            failWithMessage("Node <%s> should have content ", actual);
        }
        return this;
    }

    /**
     * Check that {@link ContentModel#PROP_CONTENT} contains specific content
     *
     * @param expected String-representation of the expected content
     * @return The created node assertion object
     */
    public NodeAssert containsContent(final String expected) {
        return containsContent(ContentModel.PROP_CONTENT, expected);
    }

    /**
     * Check that a property contains specific content
     *
     * @param contentQName Which property to check
     * @param expected String-representation of the expected content
     * @return The created node assertion object
     */
    public NodeAssert containsContent(final QName contentQName, final String expected) {
        isNotNull();
        exists();
        final ContentReader reader = contentService.getReader(actual, contentQName);
        if (reader != null) {
            Assertions.assertThat(reader.getContentString()).as("Content should contain our expected string")
                            .contains(expected);
        } else {
            failWithMessage("Node <%s> should have our content ", actual);
        }
        return this;
    }
}
