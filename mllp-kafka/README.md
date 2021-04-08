#Testing mllp to kafka
```
git clone https://github.com/developertyrone/camel-openshift
cd camel-openshift/mllp-kafka
```

##Deploy Kafka Operator
```
From operator hub
```

##Create a Kafka Cluster and Topic
```
//Generate the kafka cluster with default settings "my-cluster"
 
cat << EOF | oc create -f -
apiVersion: kafka.strimzi.io/v1beta1
kind: KafkaTopic
metadata:
  name: demo-topic
  labels:
    strimzi.io/cluster: "my-cluster"
  namespace: demo-amq-stream
spec:
  partitions: 1
  replicas: 1
EOF
```

##Expose Kafka externally
https://access.redhat.com/documentation/en-us/red_hat_amq/2020.q4/html/using_amq_streams_on_openshift/assembly-configuring-external-listeners-str

##Retrieve the cert for local and application deployment
Before deploying the example application on Kubernetes/OpenShift we have to run prepare-truststore.sh since our Strimzi cluster uses SSL/TLS for external access.
```
chmod +x prepare-truststore.sh && ./prepare-truststore.sh
```

##Test from Local IDE
```

oc -n demo-amq-stream get service my-cluster-kafka-bootstrap-nodeport -o=jsonpath='{.spec.ports[0].nodePort}{"\n"}'
30495

oc get node dev-99mhk-worker-4xkjp -o=jsonpath='{range .status.addresses[*]}{.type}{"\t"}{.address}{"\n"}'
10.0.0.9

## test on either one zoo keeper pod terminal 
bin/kafka-console-producer.sh --broker-list 10.0.0.9:30495  --topic demo-topic
bin/kafka-console-producer.sh --broker-list my-cluster-kafka-bootstrap-demo-amq-stream.apps.dev.ocp.local:443 --producer-property security.protocol=SSL --producer-property ssl.truststore.password=123456 --producer-property ssl.truststore.location=./truststore.jks --topic demo-topic


bin/kafka-console-consumer.sh --bootstrap-server 10.0.0.9:30495 --topic demo-topic --from-beginning
bin/kafka-console-consumer.sh --bootstrap-server my-cluster-kafka-bootstrap-demo-amq-stream.apps.dev.ocp.local:443 --consumer-property security.protocol=SSL --consumer-property ssl.truststore.password=123456 --consumer-property ssl.truststore.location=./truststore.jks --topic demo-topic --from-beginning
```

##deploy to Openshift
mvn fabric8:deploy -Pkubernetes
