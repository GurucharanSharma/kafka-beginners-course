package com.programming.kafka.tutorial2;

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class TwitterProducer {
    private static final String CONSUMER_KEY = "bx73UHKFNBgHXDduxFMgohWUT";
    private static final String CONSUMER_SECRET = "vKxPCs3FtWybljJszEz4gpFeincmLR7ubLWGzfmEbYUSYwqYlN";
    private static final String TOKEN = "359983482-XowqNO0b9IGiuVSHV70diLYecNyTPtnOWJWZniQM";
    private static final String SECRET = "vshQhs7R71qhU1egCmtDGGujElbtW12L2OzhX9xbBHGjU";

    public static void main(String[] args) {
        new TwitterProducer().run();
    }

    public void run() {
        // Set up your blocking queues: Be sure to size these properly based on expected TPS of your stream
        BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(100000);

        // create a twitter client
        Client client = createTwitterClient(msgQueue);

        // attempts to establish connection
        client.reconnect();

        // create a kafka producer

        // loop to send tweets to kafka
        // on a different thread, or multiple different threads....
        while (!client.isDone()) {
            String msg = null;
            try {
                msg = msgQueue.poll(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                client.stop();
            }

            if (msg != null) {
                log.info("Message: {}", msg);
            }

            log.info("End of application");
        }
    }

    public Client createTwitterClient(BlockingQueue<String> msgQueue) {
        // Declare the host you want to connect to, the endpoint, and authentication (basic auth or oauth)
        Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
        StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();

        // Optional: set up some track terms
        List<String> terms = Lists.newArrayList("bitcoin");
        hosebirdEndpoint.trackTerms(terms);

        // These secrets should be read from a config file
        Authentication hosebirdAuth = new OAuth1(CONSUMER_KEY, CONSUMER_SECRET, TOKEN, SECRET);

        ClientBuilder builder = new ClientBuilder()
                .name("Hosebird-Client-01")                              // optional: mainly for the logs
                .hosts(hosebirdHosts)
                .authentication(hosebirdAuth)
                .endpoint(hosebirdEndpoint)
                .processor(new StringDelimitedProcessor(msgQueue));

        return builder.build();
    }
}
