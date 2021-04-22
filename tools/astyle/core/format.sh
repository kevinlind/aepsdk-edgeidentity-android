#/bin/bash

cd $(dirname $0)

OS=$(../get_os.sh)

../astyle_$OS --options=astyle.options --recursive ../../../code/*.java
