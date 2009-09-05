#!/bin/bash
# rm -rf bin && \
# mkdir bin && \
scalac -cp $HOME/local/share/scala/plugin-build/continuations/build/pack/selectivecps-library.jar \
       -Xpluginsdir $HOME/local/share/scala/plugin-build/continuations/build/pack \
       -sourcepath src -d bin/ \
       `find src -name '*.scala'`
