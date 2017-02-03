/**
 * All rights reserved. Copyright (c) Ixxus Ltd 2017
 */
package com.ixxus.alfresco;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.ixxus.alfresco.NodeAssert.assertThat;

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class NodeAssertTest extends AbstractServiceTest {

    private NodeRef nodeRef;

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;

    @Autowired
    private Repository repository;

    @Autowired
    @Qualifier("FileFolderService")
    private FileFolderService fileFolderService;

    @Autowired
    private ContentService contentService;

    @Before
    public void setUp() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        String nodeName = "NodeAssertTest-" + UUID.randomUUID();
        PropertyMap propertyMap = new PropertyMap();
        propertyMap.put(ContentModel.PROP_NAME, nodeName);
        nodeRef = nodeService.createNode(repository.getCompanyHome(), ContentModel.ASSOC_CONTAINS, QName.createQName(ContentModel.USER_MODEL_URI, nodeName), ContentModel.TYPE_CONTENT, propertyMap).getChildRef();
    }

    @After
    public void tearDown() {
        if (nodeRef != null && nodeService.exists(nodeRef)) {
            nodeService.deleteNode(nodeRef);
        }
    }

    @Test
    public void test_is_type() {
        assertThat(nodeRef).isType(nodeService.getType(nodeRef));
    }

    @Test
    public void test_is_custom_type() {
        nodeService.setType(nodeRef, ContentModel.TYPE_CATEGORY);
        assertThat(nodeRef).isType(ContentModel.TYPE_CATEGORY);
    }

    @Test
    public void test_exists() {
        assertThat(nodeRef).exists();
    }

    @Test
    public void test_does_not_exist() {
        final NodeRef nonExistingNodeRef = new NodeRef("workspace://SpacesStore/whatevs");
        assertThat(nonExistingNodeRef).doesNotExist();
    }

    @Test(expected = AssertionError.class)
    public void test_failed_exist_test() {
        final NodeRef nonExistingNodeRef = new NodeRef("workspace://SpacesStore/whatevs");
        assertThat(nonExistingNodeRef).exists();
    }

    @Test
    public void test_has_aspect() {
        nodeService.addAspect(nodeRef, ContentModel.ASPECT_CHECKED_OUT, null);
        assertThat(nodeRef).hasAspect(ContentModel.ASPECT_CHECKED_OUT);
    }

    @Test
    public void test_does_not_have_aspect() {
        assertThat(nodeRef).doesNotHaveAspect(ContentModel.ASPECT_EMAILED);
    }

    @Test(expected = AssertionError.class)
    public void test_failed_aspect_test() {
        assertThat(nodeRef).hasAspect(ContentModel.ASPECT_EMAILED);
    }

    @Test
    public void test_has_property_value() {
        nodeService.setProperty(nodeRef, ContentModel.PROP_COMPANYEMAIL, "my@email.com");
        assertThat(nodeRef).hasPropertyValue(ContentModel.PROP_COMPANYEMAIL, "my@email.com");
    }

    @Test
    public void test_does_not_have_property_value() {
        nodeService.setProperty(nodeRef, ContentModel.PROP_COMPANYEMAIL, "my@email.com");
        assertThat(nodeRef).doesNotHavePropertyValue(ContentModel.PROP_COMPANYEMAIL, "my@othermail.com");
    }

    @Test(expected = AssertionError.class)
    public void test_failed_property_value_test() {
        nodeService.setProperty(nodeRef, ContentModel.PROP_COMPANYEMAIL, "my@email.com");
        assertThat(nodeRef).doesNotHavePropertyValue(ContentModel.PROP_COMPANYEMAIL, "my@email.com");
    }

    @Test
    public void a_value_in_a_multi_valued_property_can_be_asserted() {
        final QName property = ContentModel.PROP_ADDRESSEES;
        final String expectedValue = "foo";
        final List<String> tags = Arrays.asList(expectedValue);
        nodeService.setProperty(nodeRef, property, (Serializable) tags);
        assertThat(nodeRef).hasMultiplePropertyMember(property, expectedValue);
    }

    @Test(expected = AssertionError.class)
    public void a_value_missing_a_multi_valued_property_is_an_assertion_error() {
        final QName property = ContentModel.PROP_ADDRESSEES;
        final String missingValue = "bar";
        final List<String> tags = Arrays.asList("foo");
        nodeService.setProperty(nodeRef, property, (Serializable) tags);
        assertThat(nodeRef).hasMultiplePropertyMember(property, missingValue);
    }

    @Test
    public void a_value_not_in_a_multi_valued_property_can_be_asserted() {
        final QName property = ContentModel.PROP_ADDRESSEES;
        final String missingValue = "bar";
        final List<String> tags = Arrays.asList("foo");
        nodeService.setProperty(nodeRef, property, (Serializable) tags);
        assertThat(nodeRef).doesNotHaveMultiplePropertyMember(property, missingValue);
    }

    @Test(expected = AssertionError.class)
    public void a_value_present_when_testing_for_not_in_a_multi_valued_property_is_an_assertion_error() {
        final QName property = ContentModel.PROP_ADDRESSEES;
        final String value = "bar";
        final List<String> tags = Arrays.asList(value);
        nodeService.setProperty(nodeRef, property, (Serializable) tags);
        assertThat(nodeRef).doesNotHaveMultiplePropertyMember(property, value);
    }

    @Test
    public void a_non_exisiting_association_can_be_asserted() {
        assertThat(nodeRef).doesNotHaveTargetAssociation(ContentModel.ASSOC_CONTAINS);
    }

    @Test(expected = AssertionError.class)
    public void an_association_present_when_testing_for_non_exisiting_association_is_an_assertion_error() {
        final QName assoc = ContentModel.ASSOC_CONTAINS;
        nodeService.createAssociation(nodeRef, nodeRef, assoc);
        assertThat(nodeRef).doesNotHaveTargetAssociation(assoc);
    }

    @Test
    public void a_non_exisiting_target_for_an_association_can_be_asserted() {
        final NodeRef nonExistingTarget = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "1");
        final QName assoc = ContentModel.ASSOC_CONTAINS;
        nodeService.createAssociation(nodeRef, nodeRef, assoc);
        assertThat(nodeRef).doesNotHaveTargetAssociationTo(assoc, nonExistingTarget);
    }

    @Test(expected = AssertionError.class)
    public void an_association_taget_present_when_testing_for_non_exisiting_association_target_is_an_assertion_error() {
        final QName assoc = ContentModel.ASSOC_CONTAINS;
        nodeService.createAssociation(nodeRef, nodeRef, assoc);
        assertThat(nodeRef).doesNotHaveTargetAssociationTo(assoc, nodeRef);
    }

    @Test(expected = AssertionError.class)
    public void ensure_node_without_content_throws_exception() {
        assertThat(nodeRef).hasContent(ContentModel.PROP_CONTENT);
    }

    @Test(expected = AssertionError.class)
    public void ensure_node_with_empty_content_throws_exception() {
        ContentWriter w = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        w.putContent("");
        assertThat(nodeRef).hasContent(ContentModel.PROP_CONTENT);
    }

    @Test
    public void ensure_node_with_content_does_not_throw_exception() {
        ContentWriter w = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        w.putContent("content");
        assertThat(nodeRef).hasContent(ContentModel.PROP_CONTENT);
    }

    @Test
    public void ensure_node_has_content() {
        ContentWriter w = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        w.putContent("content");
        assertThat(nodeRef).hasContent("content");
    }

    @Test(expected = AssertionError.class)
    public void ensure_node_with_different_content_throws_exception() {
        ContentWriter w = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        w.putContent("content");
        assertThat(nodeRef).hasContent("different content");
    }
}