![Ixxus](ixxus_logo.png)
# Alfresco Test Assertions
[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

Fluent assertions for writing integration (remote unit) tests for Alfresco.

# Syntax
Using the [com.ixxus.alfresco.NodeAssert](src/main/java/com/ixxus/alfresco/NodeAssert.java), assertions on nodes can be written like this:
```
assertThat(nodeRef).exists().hasAspect(ContentModel.ASPECT_EMAILED).hasPropertyValue(ContentModel.PROP_NAME, "This is a node's name");
```

For more examples, check out [com.ixxus.alfresco.NodeAssertTest](src/test/java/com/ixxus/alfresco/NodeAssertTest.java).

There's also a custom assertion for testing Alfresco sites. See [com.ixxus.alfresco.SiteAssert](src/main/java/com/ixxus/alfresco/SiteAssert.java) and [how to use it](src/test/java/com/ixxus/alfresco/SiteAssertTest.java)

# Change log
## [1] - 2017-06-05
### Added
- Node assertion
- Site assertion