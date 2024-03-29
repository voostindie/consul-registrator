# Consul Registrator Agent

[![Build Status](https://travis-ci.com/voostindie/consul-registrator.svg?branch=master)](https://travis-ci.com/voostindie/consul-registrator)
[![Code Coverage](https://codecov.io/gh/voostindie/consul-registrator/branch/master/graph/badge.svg)](https://codecov.io/gh/voostindie/consul-registrator)

A Java agent that pulls metadata out of applications (runnable JARs) and (de)registers services with Consul on application startup and shutdown.

**Update September 2021**: as I'm not using this tool anymore, I'm archiving it. Right now it's working well with modern JDKs, so it's still usable. But I'm not planning on maintaining it any longer. Who knows though?

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

## Environment variable substitution

The last example is pretty useless in practice, since it contains references to infrastructure: addresses and ports. In practice these are assigned at application startup, through the environment. This is why you can use environment variables anywhere. For example:

```xml
<catalog>
    <service id="foo:${MESOS_TASK_ID}" name="foo" port="${PORT0}">
        <http-check url="http://localhost:${PORT1}/health"/>
    </service>
</catalog>
```

If an environment variable is missing, the application won't start. In case you have a sensible default you can specify one like this:

    ${<variable>:<default>}

So a reference like `${PORT0:8080}` will use the value of the environment variable `PORT0`, falling back on the value `8080` if the variable is not defined.

## Key/Value storage

Consul offers a generic K/V store. You can tap into it through the manifest, like so (extending the last example):

```xml
<catalog>
    <service .../>
    <key
        name="metrics/foo:${MESOS_TASK_ID}"
        value="http://localhost:${PORT1}/metrics"/>
</catalog>
```

Note that every key/value pair defined in the catalog will be automatically removed from Consul on application shutdown! In other words: use this facility to store values specific to the application *instances*.

## Logging

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
