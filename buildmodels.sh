#!/usr/bin/env sh
# ./buildmodels.sh <training corpus dir> <training edit1s file>

java -Xmx2048m -cp classes edu.stanford.cs276.BuildModels $@

