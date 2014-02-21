/*
 * Copyright (c) 2010-2014. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.axonframework.ext.samples;

import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.commandhandling.annotation.AggregateAnnotationCommandHandler;
import org.axonframework.commandhandling.callbacks.VoidCallback;
import org.axonframework.commandhandling.disruptor.DisruptorCommandBus;
import org.axonframework.commandhandling.distributed.DistributedCommandBus;
import org.axonframework.commandhandling.distributed.RoutingStrategy;
import org.axonframework.commandhandling.distributed.jgroups.JGroupsConnector;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.SimpleEventBus;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.axonframework.eventstore.EventStore;
import org.axonframework.ext.hazelcast.samples.helper.MemoryEventStore;
import org.axonframework.ext.hazelcast.samples.model.DataItem;
import org.axonframework.ext.hazelcast.samples.model.DataItemCmd;
import org.axonframework.serializer.xml.XStreamSerializer;
import org.jgroups.JChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.Scanner;
import java.util.UUID;

/**
 * @author lburgazzoli
 */
public class CommandBusSample {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandBusSample.class);

    private static DistributedCommandBus dcb;
    private static final int MESSAGE_COUNT = 2;
    private static JGroupsConnector connector;

    // *************************************************************************
    //
    // *************************************************************************

    public static void main(String[] args) throws Exception {
        System.setProperty("java.net.preferIPv4Stack", "true");

        JChannel   channel  = new JChannel("jgroups.xml");
        EventBus   evtBus   = new SimpleEventBus();
        EventStore evtStore = new MemoryEventStore();

        connector = new JGroupsConnector(
            channel,
            "testing",
            new DisruptorCommandBus(evtStore,evtBus),
            new XStreamSerializer());


        dcb = new DistributedCommandBus(connector);

        EventSourcingRepository<DataItem> repo = new EventSourcingRepository<>(DataItem.class,evtStore);
        repo.setEventBus(evtBus);

        AggregateAnnotationCommandHandler.subscribe(
            DataItem.class,
            repo,
            dcb);

        LOGGER.debug("Subscribed to group. Ready to join.");
        Scanner scanner = new Scanner(System.in);
        Integer loadFactor = new Random().nextInt(10);

        connector.connect(loadFactor);
        LOGGER.debug("Waiting for Joining to complete");
        connector.awaitJoined();
        LOGGER.debug("Runner is ready, write burst to start");

        String line = "";
        while (!line.startsWith("quit")) {
            line = scanner.nextLine();
            if ("burst".equalsIgnoreCase(line)) {
                readAndSendMessages();
            } else if (line.startsWith("loadfactor ")) {
                String newLoadFactor = line.substring(11);
                try {
                    connector.connect(Integer.parseInt(newLoadFactor));
                } catch (NumberFormatException e) {
                    LOGGER.debug(newLoadFactor + " is not a number");
                }
            } else if ("members".equals(line)) {
                LOGGER.debug(connector.getConsistentHash().toString());
            } else if (line.matches("join [0-9]+")) {
                int factor = Integer.parseInt(line.split(" ")[1]);
                connector.connect(factor);
            } else if (!"quit".equals(line)) {
                dcb.dispatch(new GenericCommandMessage<String>(line));
            }
        }

        channel.close();
    }

    // *************************************************************************
    //
    // *************************************************************************

    private static void readAndSendMessages() throws Exception {
        String messageBase = UUID.randomUUID().toString();
        for (int t = 0; t < MESSAGE_COUNT; t++) {
            CommandMessage<DataItemCmd.Create> cmd =
                new GenericCommandMessage<DataItemCmd.Create>(
                    new DataItemCmd.Create(messageBase + " #" + t,"" + t)
            );

            dcb.dispatch(cmd,new VoidCallback() {
                @Override
                protected void onSuccess() {
                    LOGGER.debug("Successfully receive response");
                }

                @Override
                public void onFailure(Throwable cause) {
                    LOGGER.warn("onFailure", cause);
                }
            });
        }
    }
}
