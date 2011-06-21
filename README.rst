Swarm
=====

What is it?
===========
Swarm is a framework allowing the creation of applications which can scale transparently through a novel portable continuation-based approach. Like Map-Reduce, Swarm follows the maxim "move the computation, not the data". However Swarm takes the concept much further, allowing it to be applied to almost any computation, not just those that can be broken down into map and reduce operations.

In effect, Swarm will be the ultimate "Platform as a Service", going much further than systems like Google App Engine in relieving the programmer from the difficulties of cloud computing.

You may find the Swarm source code here, however for the project's homepage and lots more information, please visit GoogleCode_.

.. _GoogleCode: http://code.google.com/p/swarm-dpl

Quick Start Guide
=================

*Note:* Swarm is still very-much a work in progress.  These instructions will let you play with some proof-of-concept demos, but Swarm remains a long way from being of practical use.

These instructions assume you are using a Linux or Mac-like system. If you are using Windows, we recommending using Cygwin which should allow you to follow these instructions more-or-less verbatim.

Install Scala 2.9.0
-------------------

http://www.scala-lang.org/downloads


Install sbt 0.7.7
-----------------
http://code.google.com/p/simple-build-tool/wiki/Setup

Download Swarm
--------------

git clone git@github.com:sanity/Swarm.git

Resolve dependencies
--------------------

``> sbt update``

Run the demo
------------

From the Swarm directory, in one console window:

``> sbt "project swarm-demos" run``

When prompted, select the class ``swarm.demos.Listen``

Then, in another console window:

``> sbt "project swarm-demos" run``

When prompted, select the class ``swarm.demos.ExplicitMoveTo1``

In this demo keep an eye on both consoles, you will be asked for input in one console, then the other.

Run the Twitter simulator
-------------------------

From the sbt console, run the ``twitter-demo`` task:

``$ sbt``

``> twitter-demo``

In a Web browser, navigate to http://localhost:8080/ and http://localhost:8081/ to interact with each node.
