/**
 * All rights reserved. Copyright (c) Ixxus Ltd 2017
 */
package com.ixxus.alfresco;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import java.util.Objects;

/**
 * Custom assertion specifically for various operations around an Alfresco site
 *
 * @author Alex Lu
 */
public class SiteAssert extends AbstractAssert<SiteAssert, SiteInfo> {
    private static SiteService siteService;

    /**
     * Only constructor - use the assertSiteThat-method
     *
     * @param siteInfo an instance of a siteInfo
     * @return The created site assertion object
     */
    private SiteAssert(final SiteInfo siteInfo) {
        super(siteInfo, SiteAssert.class);
        Objects.requireNonNull(siteInfo);
    }

    public static void setSiteService(final SiteService siteService) {
        SiteAssert.siteService = siteService;
    }

    /**
     * Instantiate a custom site assertion with an instance of SiteInfo.
     *
     * @param siteInfo instance of a SiteInfo
     * @return The created site assertion object
     */
    public static SiteAssert assertThat(final SiteInfo siteInfo) {
        Objects.requireNonNull(siteInfo, "siteInfo is required");
        return new SiteAssert(siteInfo);
    }

    /**
     * Instantiate a custom site assertion with name of site.
     *
     * @param siteShortname shortname of a site
     * @return The created site assertion object
     */
    public static SiteAssert assertThat(final String siteShortname) {
        Objects.requireNonNull(siteShortname, "siteShortname is required.");
        final SiteInfo siteInfo = siteService.getSite(siteShortname);
        return new SiteAssert(siteInfo);
    }

    /**
     * Check if the site preset of the actual site matches expected value.
     *
     * @param expectedSitePreset expected site preset
     * @return The created site assertion object
     */
    public SiteAssert isSitePreset(final String expectedSitePreset) {
        final String actualSitePreset = actual.getSitePreset();
        Assertions.assertThat(expectedSitePreset).isEqualTo(actualSitePreset);
        return this;
    }

    /**
     * Check if an expected node resides within the actual site.
     *
     * @param expectedNode expected node in a site
     * @return The created site assertion object
     */
    public SiteAssert isNodeInSite(final NodeRef expectedNode) {
        final SiteInfo expectedSite = siteService.getSite(expectedNode);
        if (expectedSite == null) {
            failWithMessage("<%s> does not reside in any site", expectedNode);
        }

        Assertions.assertThat(expectedSite).isEqualTo(actual);
        return this;
    }

    /**
     * Check if an expected node resides within the actual site.
     *
     * @param expectedNode expected node NOT in a site
     * @return The created site assertion object
     */
    public SiteAssert isNodeNotInSite(final NodeRef expectedNode) {
        final SiteInfo expectedSite = siteService.getSite(expectedNode);
        if (expectedSite != null) {
            Assertions.assertThat(expectedSite.getShortName()).isNotEqualTo(actual.getShortName());
        }

        return this;
    }

    /**
     * Check if the name of the actual site matches expected name.
     *
     * @param expectedSiteName expected site name
     * @return The created site assertion object
     */
    public SiteAssert isSiteName(final String expectedSiteName) {
        Assertions.assertThat(expectedSiteName).isEqualTo(actual.getShortName());
        return this;
    }

    /**
     * Check if a site exists for an expected site name.
     * @return The created site assertion object
     *
     * @param expectedSiteName expected site name
     */
    public SiteAssert hasSite(final String expectedSiteName) {
        Assertions.assertThat(siteService.hasSite(expectedSiteName)).isTrue();
        return this;
    }

    /**
     * Check if the actual site has an expected member.
     *
     * @param expectedAuthority expected member in a site
     * @return The created site assertion object
     */
    public SiteAssert isMember(final String expectedAuthority) {
        Assertions.assertThat(siteService.isMember(actual.getShortName(), expectedAuthority)).isTrue();
        return this;
    }

    /**
     * Check if a site has an expected container.
     *
     * @param expectedContainer expected container in a site
     * @return The created site assertion object
     */
    public SiteAssert hasContainer(final String expectedContainer) {
        Assertions.assertThat(containerExists(expectedContainer)).isTrue();
        return this;
    }

    private boolean containerExists(final String expectedContainer) {
        return siteService.hasContainer(actual.getShortName(), expectedContainer);
    }

    /**
     * Check if a site does NOT have an expected container
     *
     * @param expectedContainer expected container NOT in a site
     * @return The created site assertion object
     */
    public SiteAssert doesNotHaveContainer(final String expectedContainer) {
        Assertions.assertThat(containerExists(expectedContainer)).isFalse();
        return this;
    }

}