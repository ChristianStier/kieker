#!/usr/bin/env bash

./x-variables.sh

sudo docker run -v ${KIEKER_DIR}:/opt/kieker kieker/kieker-build:openjdk6 /bin/bash -c "cd /opt/kieker; ./gradlew -S test"

exit $?
