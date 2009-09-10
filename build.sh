#!/bin/bash
if [ ! -f bin ]
then
  mkdir bin
fi
scalac -cp $SCALA_C_DIR/build/pack/selectivecps-library.jar \
	-unchecked \
       -Xpluginsdir $SCALA_C_DIR/build/pack \
       -sourcepath src -d bin/ \
       `find src -name '*.scala'`
