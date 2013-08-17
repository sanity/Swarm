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

### Install Scala 2.9.2

[Installation Instructions](http://www.scala-lang.org/downloads)

### Download Swarm

`git clone git@github.com:sanity/Swarm.git`

### Run the demo

From the Swarm directory, in one console window:

`$ sbt/sbt "demos/run-main swarm.demos.Listen"`

Then, in another console window:

`$ sbt/sbt "demos/run-main swarm.demos.ExplicitMoveTo1"`

In this demo keep an eye on both consoles, you will be asked for input in one console, then the other.

### Run the Twitter simulator

From the command line, launch the Twitter demo:

`$ sbt/sbt "demos/run-main swarm.demos.SwarmTwitter"`

In a Web browser, navigate to `http://localhost:8080/` and `http://localhost:8081/` to interact with each node.
