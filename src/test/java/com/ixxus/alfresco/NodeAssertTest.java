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

import static com.ixxus.alfresco.NodeAssert.assertThat;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;
import org.assertj.core.api.Condition;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;

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

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        String nodeName = "NodeAssertTest-" + UUID.randomUUID();
        PropertyMap propertyMap = new PropertyMap();
        propertyMap.put(ContentModel.PROP_NAME, nodeName);
        nodeRef = nodeService.createNode(repository.getCompanyHome(), ContentModel.ASSOC_CONTAINS, QName.createQName(ContentModel.USER_MODEL_URI, nodeName),
                        ContentModel.TYPE_CONTENT, propertyMap).getChildRef();
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
    public void test_property_starts_with() {
        nodeService.setProperty(nodeRef, ContentModel.PROP_COMPANYEMAIL, "myname@email.com");
        Condition<Serializable> startsWithMe = new Condition<>(value -> ((String) value).startsWith("myname"), "Should start with 'myname'");
        assertThat(nodeRef).propertyValue(ContentModel.PROP_COMPANYEMAIL, startsWithMe);
    }

    @Test
    public void test_property_not_starts_with() {
        String errorMsg = "Should start with 'myname'";
        exception.expect(AssertionError.class);
        exception.expectMessage(errorMsg);
        //
        nodeService.setProperty(nodeRef, ContentModel.PROP_COMPANYEMAIL, "someoneelse@email.com");
        Condition<Serializable> startsWithMe = new Condition<>(value -> ((String) value).startsWith("myname"), errorMsg);
        //
        assertThat(nodeRef).propertyValue(ContentModel.PROP_COMPANYEMAIL, startsWithMe);
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

    @Test
    public void ensure_node_content_contains_our_string() {
        ContentWriter w = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        w.putContent("my custom content");
        assertThat(nodeRef).containsContent("custom");
    }

    @Test(expected = AssertionError.class)
    public void ensure_node_content_that_does_not_contain_our_string_throws_exception() {
        ContentWriter w = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        w.putContent("content");
        assertThat(nodeRef).containsContent("different content");
    }
}
