package com.programming.kafka.tutorial1;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

@Slf4j
public class ProducerDemoKeys {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("Hello World");

        // Bootstrap servers for our Kafka
        String bootstrapServers = "127.0.0.1:9092";

        // create producer properties
        Properties properties = new Properties();
        /*properties.setProperty("bootstrap.servers", "127.0.0.1:9092");
        properties.setProperty("key.serializer", StringSerializer.class.getName());
        properties.setProperty("value.serializer", StringSerializer.class.getName());*/

        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        for (int i = 0; i < 10; i++) {
            String topicName = "first_topic";
            String key = "id _ " + i;
            String value = "hello world [" + i + "]";

            // create a producer record
            ProducerRecord<String, String> record = new ProducerRecord<>(topicName, key, value);

            // send data
            // Since this is an async call. The program exits the producer never sends the data.
            // We have to wait until the message is delivered.
            producer.send(record, (recordMetadata, exception) -> {
                // Executes every time the message is sent or there is an exception
                if (exception == null) {
                    log.info("Produced the meessage with the following details => \n" +
                            "Topic Name: " + recordMetadata.topic() + "\n" +
                            "Partition: " + recordMetadata.partition() + "\n" +
                            "Offset: " + recordMetadata.offset() + "\n" +
                            "Timestamp: " + recordMetadata.timestamp());
                } else {
                    log.error("Exception while producing the message: " + exception);
                }
            }).get(); // block the .send() to make it synchronous - don't do this in production
        }

        // flush data
        // This waits until the message is sent by the producer.
        producer.flush();

        // flush and close producer
        producer.close();
    }
}
