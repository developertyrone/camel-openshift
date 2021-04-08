#!/usr/bin/env bash

rm truststore.jks

kubectl get secret/my-cluster-cluster-ca-cert -o 'go-template={{index .data "ca.crt"}}' -n demo-amq-stream | base64 -d > ca.crt

echo "yes" | keytool -import -trustcacerts -file ca.crt -keystore truststore.jks -storepass 123456

#cp truststore.jks <your local truststorepath>/truststore.jks

kubectl create configmap truststore-config --from-file=truststore.jks=truststore.jks -n demo-amq-stream

rm -f ca.crt