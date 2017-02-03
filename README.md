![Ixxus](ixxus_logo.png)
# Alfresco Test Assertions
Fluent assertions for writing integration (remote unit) tests for Alfresco.

# Include in your project
```
<dependency>
    <groupId>com.ixxus.alfresco</groupId>
    <artifactId>alfresco-test-assertions</artifactId>
    <version>1</version>
</dependency>
```

# Syntax
Using the [com.ixxus.alfresco.NodeAssert](src/main/java/com/ixxus/alfresco/NodeAssert.java), assertions on nodes can be written like this:
```
assertThat(nodeRef).exists().hasAspect(ContentModel.ASPECT_EMAILED).hasPropertyValue(ContentModel.PROP_NAME, "This is a node's name");
```

For more examples, check out [com.ixxus.alfresco.NodeAssertTest](src/test/java/com/ixxus/alfresco/NodeAssertTest.java).