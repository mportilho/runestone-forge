# runestone-forge

Java development toolkit

## Tests

Unit tests must be run with the VM parameters `-XX:+EnableDynamicAgentLoading -Dnet.bytebuddy.experimental=true` on JDK
21, to enable Mockito and ByteBuddy

On pom.xml file, add the following lines to the build/plugins section:

```xml

<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.0.0-M5</version>
    <configuration>
        <argLine>-XX:+EnableDynamicAgentLoading -Dnet.bytebuddy.experimental=true</argLine>
    </configuration>
</plugin>
```