#!/bin/bash

if [ "$(uname)" = "Darwin" ]
then
	echo "osx"
elif [ "$(uname)" = "Linux" ]
then
	echo "linux"
else
	echo "astyle incompatible OS $(uname)"
	exit 1;
fi