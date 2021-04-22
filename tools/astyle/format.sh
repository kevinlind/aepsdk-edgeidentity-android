#!/bin/bash

repo=$(basename `git rev-parse --show-toplevel`)
if [[ $repo == *"android"* ]]; then
  tools/astyle/android/format.sh
else
  tools/astyle/core/format.sh
fi

