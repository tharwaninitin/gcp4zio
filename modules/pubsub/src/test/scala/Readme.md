Run PubSub Emulator in one terminal
```shell
gcloud beta emulators pubsub start --project=testproject --host-port=0.0.0.0:8085
```

Run below commands in other terminal
```shell
export GCS_PROJECT=testproject
export TOPIC=testtopic
export NON_EXISTING_TOPIC=notopic
export SUBSCRIPTION_1=testsub1
export SUBSCRIPTION_2=testsub2
$(gcloud beta emulators pubsub env-init)
sbt "project pubsub; test"
```

