# Consul Registrator Agent

[![Build Status](https://travis-ci.org/voostindie/consul-registrator.svg?branch=master)](https://travis-ci.org/voostindie/consul-registrator)
[![Code Quality](https://api.codacy.com/project/badge/grade/960587eea7c24e77b6b297c0f2cba56a)](https://www.codacy.com/app/voostindie/consul-registrator)
[![Code Coverage](https://api.codacy.com/project/badge/coverage/960587eea7c24e77b6b297c0f2cba56a)](https://www.codacy.com/app/voostindie/consul-registrator)

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

Where the Docker Registrator inspects the Docker container, this is not possible in the case of a runnable JAR. That's why this agent requires its own manifest file, to be packaged within the application. If missing, the agent will not allow the application to be started.

The manifest file is an XML file called `consul-catalog.xml`. It must be in the META-INF directory of of the JAR and it must look as follows:

### Minimal example

```xml
<catalog>
    <service name="myFirstService" port="8080">
        <http-check url="http://localhost:8080/health"/>
    </service>
</catalog>
```

A catalog must contain at least one service, otherwise it's invalid. With this minimal example:

* The agent will wait 3 seconds before registering services with Consul, giving the application time to start up properly.
* The service is assigned a unique ID in Consul that looks like `<serviceName>:<UUID>`.
* The health check will be called by the Consul agent with an interval of 5 seconds.

### Extended example

```xml
<catalog delay="0s"> <!-- No dawdling! -->
    <service id="service1" name="myFirstService" address="192.168.1.1" port="8100">
        <http-check url="http://localhost:8180/health" interval="10s"/>
        <tag name="master"/>
        <tag name="v1"/>
    </service>
</catalog>
```

### Logging

The agent logs some messages to standard out. The logging can be configured by passing an argument to the agent at startup:

    java -javaagent:consul-registrator.jar=logger=<level> -jar <my-runnable-app.jar>

Possible levels are:

* `debug`: generates more diagnostic information
* `info`: the default
* `silent`: disables logging

Error messages cannot be disabled. Any error will fail application startup, so it's probably good to have all available information.

## Design decisions

While investigating the technical approach for this agent I came to the following design decisions:

* No dependencies outside the JRE.
* All code in one package.
* Exactly one public class: the agent itself.

The reason for these decisions is that the agent code becomes part of the classpath, and I don't want to pollute it (too much). An alternative approach is to perform some classpath wizardry, but I'm not a magician. This is easier.

Thanks to these decisions the JAR is around 25 kB currently. That should be acceptable!