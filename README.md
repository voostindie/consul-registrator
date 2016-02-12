# Consul Registrator Agent

A Java agent that pulls metadata out of applications (runnable JARs) and (de)registers services with Consul on application startup and shutdown.

## Use case

Docker comes with its [Registrator](http://gliderlabs.com/registrator/latest/), which is a great way to (de)register services within Docker containers with Consul (and others). But what if you don't use Docker and instead run JVM applications (*runnable JARs*)?

For those of us who are not willing or able to use Docker (yet) and are running on the JVM, this agent might come in handy.

By putting this code in an agent you can keep the applications themselves completely decoupled from Consul. Updates to the agent can be rolled out across all applications without having to rebuild them.

## How to use

Where you normally say

    java -jar <my-runnable-app.jar>

Now you say:

    java -javaagent:consul-registrator.jar -jar <my-runnable-app.jar>

## Manifest

Where the Docker Registrator inspects the Docker container, this is not possible in the case of a runnable JAR. That's why this agent requires its own manifest file, to be packaged within the applicaion. If missing, the agent will not allow the application to be started.

The manifest file is an XML file called `consul-services.xml`. It must be in the META-INF directory of of the JAR and it must conform to the following format:

```xml
<consul-services>
    <!-- Minimal example -->
    <service name="myFirstService" port="8080">
        <http-check url="/health"/>
    </service>

    <!-- Extensive example -->
    <service id="service2" name="mySecondService" port="8100">
        <http-check url="http://localhost:8180/health" interval="10s"/>
        <tag name="master"/>
        <tag name="v1"/>
    </service>
</consul-services>
```

## Design decisions

While investigating the technical approach for this agent I came to the following design decisions:

* No dependencies outside the JRE.
* All code in one package.
* Exactly one public class: the agent itself.

The reason for these decisions is that the agent code becomes part of the classpath, and I don't want to pollute it (too much). An alternative approach is to perform some classpath wizardry, but I'm not a magician. This is easier.
