#!/bin/bash

if [ "$#" == "0" ]
then
  echo "Pass version of gcp4zio libraries to download for generating README.md"
  echo "For e.g. 1.1.0"
  exit 1
fi

gcs="com.github.tharwaninitin:gcp4zio-gcs_2.12:$1"
bq="com.github.tharwaninitin:gcp4zio-bq_2.12:$1"
dp="com.github.tharwaninitin:gcp4zio-dp_2.12:$1"
pubsub="com.github.tharwaninitin:gcp4zio-pubsub_2.12:$1"

echo "Downloading $gcs"
echo "Downloading $bq"
echo "Downloading $dp"
echo "Downloading $pubsub"

cp=$(coursier fetch -p "$gcs" "$bq" "$dp" "$pubsub")

echo "Done downloading all above libraries"

coursier launch org.scalameta:mdoc_2.12:2.3.6 -- \
--site.VERSION "$1" \
--in readme.template.md \
--out README.md \
--classpath "$cp"