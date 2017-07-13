![Ixxus](ixxus_logo.png)
# Alfresco Test Assertions
[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

Fluent assertions for writing integration (remote unit) tests for Alfresco.

# Syntax
Using the [com.ixxus.alfresco.NodeAssert](src/main/java/com/ixxus/alfresco/NodeAssert.java), assertions on nodes can be written like this:
```bash
assertThat(nodeRef).exists().hasAspect(ContentModel.ASPECT_EMAILED).hasPropertyValue(ContentModel.PROP_NAME, "This is a node's name");
```
```bash
Condition<Serializable> startsWithTitle = new Condition<>(value -> ((String) value).startsWith("title"), "Should start with 'title'");
assertThat(nodeRef).exists().propertyValue(ContentModel.PROP_TITLE, startsWithTitle);
```

For more examples, check out [com.ixxus.alfresco.NodeAssertTest](src/test/java/com/ixxus/alfresco/NodeAssertTest.java).

There's also a custom assertion for testing:
 - Alfresco Sites. See [com.ixxus.alfresco.SiteAssert](src/main/java/com/ixxus/alfresco/SiteAssert.java) and [how to use it](src/test/java/com/ixxus/alfresco/SiteAssertTest.java)
 - Alfresco Workflows. See [com.ixxus.alfresco.WorkflowAssert](src/main/java/com/ixxus/alfresco/WorkflowAssert.java) and [how to use it](src/test/java/com/ixxus/alfresco/WorkflowAssertTest.java)

# How to include the library
We are still working on deploying to maven central. At the mean time, we suggest to clone the repository and build locally.

```
mvn clean install
```

Then include as a maven dependency
```
<dependency>
    <groupId>com.ixxus.alfresco</groupId>
    <artifactId>alfresco-test-assertions</artifactId>
    <version><PLEASE SEE POM.XML FOR VERSION></version>
    <scope>test</scope>
</dependency>
```

# Change log
## Unreleased
### Added
 - AssertJ Conditions for NodeAssert
 - WorkflowAssert in the same style as NodeAssert/SiteAssert

## [1] - 2017-06-05
### Added
- Node assertion
- Site assertion
