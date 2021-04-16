package com.example.demo;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MLLPRoute extends RouteBuilder {
    private final static Logger LOGGER = LoggerFactory.getLogger(MLLPRoute.class.getName());

    String tmpDirsLocation = System.getProperty("java.io.tmpdir");

    @Override
    public void configure() throws Exception {

        from("mllp:{{mllp.server}}:{{mllp.port}}{{mllp.query_params}}")
                .process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        String payload = exchange.getIn().getBody(String.class);
                        // do something with the payload and/or exchange here
                        log.debug("Incoming Message:"+payload);
                    }})
                .to("direct:tokafka")
        ;

        from("direct:tokafka")
                .log(LoggingLevel.DEBUG, tmpDirsLocation)
                .log("Send to Kafka")
                .to("kafka:{{kafka.producer.topic}}");
        ;

    }
}