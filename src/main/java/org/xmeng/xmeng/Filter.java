package org.xmeng.xmeng;

import java.io.IOException;
import java.util.Properties;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;

class Filter {
    static void filter() {
        Properties config = new Properties();

        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "streama");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Integer().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        ObjectMapper mapper = new ObjectMapper();

        final String topic = "raw";
        KStreamBuilder builder = new KStreamBuilder();
        KStream<Integer, String> raw = builder.stream(topic);

        KStream<Integer, String> filtered = raw.filter((Integer k, String v) ->
                {
                    try {
                        Data data = mapper.readValue(v, Data.class);
                        if (data.valid()) {
                            System.out.println(v);
                            return true;
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    return false;
                }
        );
        filtered.to("filtered");

        KafkaStreams streams = new KafkaStreams(builder, config);
        streams.start();
    }
}
