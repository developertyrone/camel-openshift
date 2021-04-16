# Testing mllp to kafka
```
oc new-project demo-amq-stream
git clone https://github.com/developertyrone/camel-openshift
cd camel-openshift/mllp-kafka
```

## Deploy Kafka Operator
```
From operator hub
```

## Create a Kafka Cluster and Topic
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

## Expose Kafka externally
```
https://access.redhat.com/documentation/en-us/red_hat_amq/2020.q4/html/using_amq_streams_on_openshift/assembly-configuring-external-listeners-str
```

## Retrieve the cert for local and application deployment(Ignore if the kafka is in plain text mode)
Before deploying the example application on Kubernetes/OpenShift we have to run prepare-truststore.sh since our Strimzi cluster uses SSL/TLS for external access.
```
chmod +x prepare-truststore.sh && ./prepare-truststore.sh
```
## Using External Configuration
```
oc create configmap spring-app-config --from-file=application.properties
```

## Test from command line
```
wget https://mirror-hk.koddos.net/apache/kafka/2.7.0/kafka_2.13-2.7.0.tgz

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

## Deploy to Openshift
```
./mvnw clean fabric8:deploy -Popenshift
```

## Build image and push to openshift
```
./mnvw install
./mvnw package fabric8:build -Popenshift

oc create is mllp-kafka
docker pull <dockerhub>/<yourrepo>
docker tag <dockerhub>/<yourrepo> <internal-registry>/<your-project>/mllp-kafka
docker push <internal-registry>/<your-project>/mllp-kafka
```

## Create Deployment
```
kind: Deployment
apiVersion: apps/v1
metadata:
  name: mllp-kafka
  namespace: <namespace>
spec:
  replicas: 1
  selector:
    matchLabels:
      app: mllp-kafka
  template:
    metadata:
      creationTimestamp: null
      labels:
        app: mllp-kafka
    spec:
      volumes:
        - name: application-config
          configMap:
            name: spring-app-config
            items:
              - key: application.properties
                path: application.properties
            defaultMode: 420
      containers:
        - name: mllp-kafka
          image: <image>
          ports:
            - containerPort: 4650
              protocol: TCP
          volumeMounts:
            - name: application-config
              readOnly: true
              mountPath: /deployments/config
          resources:
            requests:
              cpu: "0.2"
              ### memory: 256Mi
            limits:
              cpu: "1.0"
              ### memory: 256Mi
```  


```

## Adding nodeport endpoint
```
oc -n demo-amq-stream expose dc demo --type=NodePort --name=mllp-demo --target-port=8888 --port=8888  --overrides '{ "apiVersion": "v1","spec":{"ports":[{"port":8888,"protocol":"TCP","targetPort":8888,"nodePort":30495}]}}'
```
## Test using mllp client
https://github.com/rkettelerij/mllp-client

example:

.\go_build_mllp_client_go.exe -file adt_a01.txt -host <node-ip> -port 30495