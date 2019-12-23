#!/bin/bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

for x in bungee paper; do
	cd "$SCRIPT_DIR/$x"
	./upload-play.sh
	ret=$?
	if [[ $ret -ne 0 ]]; then
		echo "Upload Failed!" >&2
		exit $ret
	fi
done
