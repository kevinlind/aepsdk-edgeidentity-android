#/bin/bash

cd $(dirname $0)

OS=$(../get_os.sh)

if ../astyle_$OS --dry-run --options=astyle.options --recursive ../../../code/*.java --exclude=build -i	| grep -q 'Formatted'; then
	echo "Style check failed, please goto root folder and run ./tools/astyle/format.sh";
	exit 1;
fi
