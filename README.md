# Swarm

## What is it?

Swarm is a framework allowing the creation of applications which can scale transparently through a novel portable continuation-based approach. Like Map-Reduce, Swarm follows the maxim "move the computation, not the data". However Swarm takes the concept much further, allowing it to be applied to almost any computation, not just those that can be broken down into map and reduce operations.

In effect, Swarm will be the ultimate "Platform as a Service", going much further than systems like Google App Engine in relieving the programmer from the difficulties of cloud computing.

## Community

IRC: `irc.freenode.net #swarmproject`

[Mailing List](http://groups.google.com/group/swarm-discuss)


## Quick Start Guide

*Note:* Swarm is still very-much a work in progress.  These instructions will let you play with some proof-of-concept demos, but Swarm remains a long way from being of practical use.

These instructions assume you are using a Linux or Mac-like system. If you are using Windows, we recommending using Cygwin which should allow you to follow these instructions more-or-less verbatim.

### Install Scala 2.10.x

[Installation Instructions](http://www.scala-lang.org/downloads)

### Download Swarm

`git clone git@github.com:sanity/Swarm.git`

### Run the demo

From the Swarm directory, in one console window:

`$ sbt/sbt "demos/run-main org.swarmframework.demos.Listen"`

Then, in another console window:

`$ sbt/sbt "demos/run-main org.swarmframework.demos.ExplicitMoveTo1"`

In this demo keep an eye on both consoles, you will be asked for input in one console, then the other.  The
surprising thing is that the [code](https://github.com/sanity/Swarm/blob/master/swarm-demos/src/main/scala/swarm/demos/ExplicitMoveTo1.scala)
jumps between consoles with a single command!

```scala
    val name = readLine("What is your name? ")
    moveTo(InetLocation(InetAddress.getLocalHost, 9997))
    val age = Integer.parseInt(readLine(s"Hello $name, what age are you? "))
    moveTo(InetLocation(InetAddress.getLocalHost, 9998))
    println(s"Wow $name you're half way to ${age * 2} years old!")
```

### Run the Twitter simulator

From the command line, launch the Twitter demo:

`$ sbt/sbt "demos/run-main org.swarmframework.demos.SwarmTwitter"`

In a Web browser, navigate to `http://localhost:8080/` and `http://localhost:8081/` to interact with each node.

### Developing Swarm with IntelliJ IDEA

If you haven't already, install the 
[Scala](http://confluence.jetbrains.com/display/SCA/Scala+Plugin+Nightly+Builds+for+Leda) and 
[SBT](http://confluence.jetbrains.com/display/SCA/SBT+Plugin+Nightly+Builds+for+Leda) plugins (we recommend 
the nightly versions, see the green box on the linked pages for auto-update).

Generate an IDEA project file:

`$ sbt/sbt gen-idea`

Now open the project in IDEA.  
