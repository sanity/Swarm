Swarm
=====

You may find the Swarm source code here, however for the project's homepage
please visit GoogleCode_.

.. _GoogleCode: http://code.google.com/p/swarm-dpl

Notes
-----

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

Run the Twitter simulator
-------------------------

From the Swarm directory, in one console window:

``> sbt "project swarm-twitter-node1" jetty-run``

Then, in another console window:

``> sbt "project swarm-twitter-node2" jetty-run``

In a Web browser, navigate to http://localhost:8080/ and http://localhost:8081/ to interact with each node.
