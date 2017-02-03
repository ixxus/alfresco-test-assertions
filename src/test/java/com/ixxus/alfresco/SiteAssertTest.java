/**
 * All rights reserved. Copyright (c) Ixxus Ltd 2017
 */
package com.ixxus.alfresco;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.UUID;

import static com.ixxus.alfresco.SiteAssert.assertThat;

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class SiteAssertTest {

    private static final String TEST_SITE_PRESET = "test site preset";

    @Autowired
    @Qualifier("SiteService")
    private SiteService siteService;

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;

    @Autowired
    private Repository repository;

    private SiteInfo testSiteInfo;

    @Before
    public void setUp() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        testSiteInfo = createTestSite();
        siteService.createContainer(testSiteInfo.getShortName(), SiteService.DOCUMENT_LIBRARY, ContentModel.TYPE_FOLDER, Collections.emptyMap());
    }

    @Test
    public void test_is_site_preset() {
        assertThat(testSiteInfo).isSitePreset(TEST_SITE_PRESET);
    }

    private SiteInfo createTestSite() {
        final String siteId = UUID.randomUUID().toString();
        final String siteTitle = siteId;
        final String siteDescription = siteId;
        final SiteInfo siteInfo = siteService.createSite(TEST_SITE_PRESET, siteId, siteTitle, siteDescription, SiteVisibility.PUBLIC);
        siteService.createContainer(siteInfo.getShortName(), SiteService.DOCUMENT_LIBRARY, ContentModel.TYPE_FOLDER, Collections.emptyMap());
        return siteInfo;
    }

    @Test
    public void test_is_node_in_site() {
        final NodeRef doclib = getDoclib(testSiteInfo);
        final NodeRef nodeRef = createNode(doclib);
        assertThat(testSiteInfo).isNodeInSite(nodeRef);
    }

    private NodeRef createNode(final NodeRef parentNodeRef) {
        return nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_CONTENT)
                .getChildRef();
    }

    private NodeRef getDoclib(final SiteInfo siteInfo) {
        return siteService.getContainer(siteInfo.getShortName(), SiteService.DOCUMENT_LIBRARY);
    }

    @Test
    public void test_repository_node_is_node_not_in_site() {
        assertThat(testSiteInfo).isNodeNotInSite(repository.getCompanyHome());
    }

    @Test
    public void test_node_is_not_in_the_same_site() {
        final SiteInfo siteInfo = createTestSite();
        final NodeRef doclib = getDoclib(siteInfo);
        final NodeRef node = createNode(doclib);

        assertThat(testSiteInfo).isNodeNotInSite(node);
    }

    @Test
    public void test_is_site_name() {
        assertThat(testSiteInfo).isSiteName(testSiteInfo.getShortName());
    }

    @Test
    public void test_has_site() {
        assertThat(testSiteInfo).hasSite(testSiteInfo.getShortName());
    }

    @Test
    public void test_has_container() {
        assertThat(testSiteInfo).hasContainer(SiteService.DOCUMENT_LIBRARY);
    }

    @Test
    public void test_does_not_have_container() {
        assertThat(testSiteInfo).doesNotHaveContainer("forums");
    }

}