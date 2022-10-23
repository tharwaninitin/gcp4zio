Run PubSub Emulator in one terminal
```shell
gcloud beta emulators pubsub start --project=testproject --host-port=0.0.0.0:8085
```

Run below commands in other terminal
```shell
export GCS_PROJECT=testproject
export TOPIC=testtopic
export SUBSCRIPTION=testsub

$(gcloud beta emulators pubsub env-init)

sbt "project examples; runMain PSProduceConsume"
```

