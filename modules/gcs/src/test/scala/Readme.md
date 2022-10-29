Run Fake GCS 
```shell
cd gcp4zio/modules/gcs/src/test

docker run -it --rm --name gcs \
-v ${PWD}/resources/data:/data \
-p 8080:8080 fsouza/fake-gcs-server \
-scheme http -port 8080
```

Run below commands in other terminal
```shell
# Set environment variables
export GCS_BUCKET=testbucket
export VALID_TOPIC_NAME=projects/testproject/topics/testtopic
export INVALID_TOPIC_NAME=invalidtopic
export VALID_NOTIFICATION_ID=testnot
export INVALID_NOTIFICATION_ID=invalidnot

# To make sure everything is working as expected, execute below command
curl http://0.0.0.0:8080/storage/v1/b/testbucket/o

# Execute Tests
sbt "project gcs; test"
```

