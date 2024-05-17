#!/bin/bash

set -e

if [ "$#" == "0" ]
then
  echo "Pass version of gcp4zio libraries to download for generating README.md"
  echo "For e.g. 1.5.1"
  exit 1
fi

gcs="com.github.tharwaninitin:gcp4zio-gcs_2.13:$1"
bq="com.github.tharwaninitin:gcp4zio-bq_2.13:$1"
dp="com.github.tharwaninitin:gcp4zio-dp_2.13:$1"
pubsub="com.github.tharwaninitin:gcp4zio-pubsub_2.13:$1"
batch="com.github.tharwaninitin:gcp4zio-batch_2.13:$1"
monitoring="com.github.tharwaninitin:gcp4zio-monitoring_2.13:$1"

echo "Downloading $gcs"
echo "Downloading $bq"
echo "Downloading $dp"
echo "Downloading $pubsub"
echo "Downloading $batch"
echo "Downloading $monitoring"

cp=$(cs fetch -p "$gcs" "$bq" "$dp" "$pubsub" "$batch" "$monitoring")

echo "Done downloading all above libraries"

cs launch org.scalameta:mdoc_2.13:2.5.2 -- \
--site.VERSION "$1" \
--site.SCALA213 "2.13.12" \
--site.SCALA3 "3.3.1" \
--site.JAVA "17, 21" \
--in docs/readme.template.md \
--out README.md \
--classpath "$cp"