#!/bin/bash
if [ ! -d target ]
then
  mkdir target
fi
scalac -cp $SCALA_C_DIR/build/pack/selectivecps-library.jar \
	-unchecked \
       -Xpluginsdir $SCALA_C_DIR/build/pack \
       -sourcepath src/main/scala -d target/classes \
       `find src/main/scala -name '*.scala'`
