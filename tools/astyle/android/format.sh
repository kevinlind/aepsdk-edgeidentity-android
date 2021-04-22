#/bin/bash

cd $(dirname $0)

OS=$(../get_os.sh)

echo `pwd`


../astyle_$OS --options=astyle.options --recursive ../../../code/*.java --exclude=build -i
