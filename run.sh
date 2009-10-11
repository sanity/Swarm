#!/bin/bash

java -cp target:$SCALA_HOME/lib/scala-library.jar:$SCALA_C_DIR/build/pack/selectivecps-library.jar swarm.demos.$1 $2 $3
