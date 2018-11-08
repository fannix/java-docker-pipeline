package org.xmeng.xmeng;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

class Reader {
    static void read() {
        ZooKeeper zk = null;
        Logger logger = LoggerFactory.getLogger(Reader.class);
        try {
            CountDownLatch latch = new CountDownLatch(1);
            zk = new ZooKeeper("localhost:2181", 30, (WatchedEvent event) -> {
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    latch.countDown();
                }

            });

            latch.await();

            // Use zookeeper to set up a lock to only allow one instance to run
            final String lock = "/pipeline";
            if (zk.exists(lock, false) != null) {
                logger.error("Another instance is already running. Exiting");
                System.exit(-1);
            }
            zk.create(lock, "1".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

            final String fileName = System.getenv("FILE_PATH");

            CsvMapper mapper = new CsvMapper();
            CsvSchema schema = mapper.schemaFor(Data.class);

            ObjectMapper oMapper = new ObjectMapper();

            final String progressCounter = "/progress";
            String checkpoint = "0";
            if (zk.exists(progressCounter, false) == null) {
                zk.create(progressCounter, "1".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            } else {
                checkpoint = new String(zk.getData(progressCounter, null, null));
            }
            Long lastRead = Long.parseLong(checkpoint);
            logger.info("last read position {}", lastRead);
            Long lineCount = 0L;

            Properties props = new Properties();
            props.put("bootstrap.servers", System.getenv("KAFKA_BROKER"));
            KafkaProducer<Integer, String> producer = new KafkaProducer<>(props, new IntegerSerializer(), new StringSerializer());

            try(BufferedReader fileReader = Files.newBufferedReader(Paths.get(fileName),StandardCharsets.UTF_8)) {
                // skip the header;
                String line = fileReader.readLine();

                List<Future<RecordMetadata>> futureList = new ArrayList<>();

                while ((line = fileReader.readLine()) != null) {

                    lineCount += 1;

                    // skip rows already read
                    if (lineCount <= lastRead) {
                        continue;
                    }

                    MappingIterator<Data> it = mapper.readerFor(Data.class).with(schema).readValues(line);

                    while (it.hasNextValue()) {
                        Data data = it.next();
                        String json = oMapper.writeValueAsString(data);

                        // If we don't wait until the future to finish, the program will exit before some message are acked by Kafka brokers
                        Future<RecordMetadata> future = producer.send(new ProducerRecord<>(System.getenv("TOPIC"), json.hashCode(), json));
                        futureList.add(future);
                    }

                    if (lineCount % 100 == 0) {
                        zk.setData(progressCounter, lineCount.toString().getBytes(),-1);
                        String position = new String(zk.getData(progressCounter, null, null));
                        logger.info("current position {}", position);

                        waitAck(logger, futureList);
                    }
                }

                waitAck(logger, futureList);
                // delete the counter if the data is successfully processed.
                zk.delete(progressCounter, -1);
            }
        } catch (IOException | KeeperException | InterruptedException e) {
            e.printStackTrace();
            logger.error("The data might not be processed completely. Please rerun the program");
            System.exit(-1);
        }
    }

    private static void waitAck(Logger logger, List<Future<RecordMetadata>> futureList) throws InterruptedException {
        try {
            for (Future<RecordMetadata> future : futureList) {
                future.get();
            }
        } catch (ExecutionException e) {
            logger.warn("error when waiting for future");
            e.printStackTrace();
        } finally {
            futureList.clear();
        }
    }
}
