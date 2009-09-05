#!/bin/bash

java -cp bin:$HOME/local/share/scala/lib/scala-library.jar:$HOME/local/share/scala/plugin-build/continuations/build/pack/selectivecps-library.jar swarm.demos.ExplicitMoveTo1 $@
