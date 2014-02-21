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

import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.commandhandling.annotation.AggregateAnnotationCommandHandler;
import org.axonframework.commandhandling.disruptor.DisruptorCommandBus;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.SimpleEventBus;
import org.axonframework.eventhandling.annotation.AnnotationEventListenerAdapter;
import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventsourcing.AggregateFactory;
import org.axonframework.eventsourcing.GenericAggregateFactory;
import org.axonframework.eventstore.EventStore;
import org.axonframework.ext.hazelcast.samples.helper.MemoryEventStore;
import org.axonframework.ext.hazelcast.samples.model.DataItem;
import org.axonframework.ext.hazelcast.samples.model.DataItemCmd;
import org.axonframework.ext.hazelcast.samples.model.DataItemEvt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lburgazzoli
 */
public class DisruptorSample {

    private static final Logger LOGGER = LoggerFactory.getLogger(DisruptorSample.class);



    // *************************************************************************
    //
    // *************************************************************************

    private final static class DisruptorEventHandler {
        @EventHandler
        public void handle(DataItemEvt.Create data) {
            LOGGER.debug("DataItemEvt <{}>",data);
        }

        @EventHandler
        public void handle(DataItemEvt.Update data) {
            LOGGER.debug("DataItemEvt <{}>", data);
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static void main(String[] args) {
        try {
            EventStore                 evtStore = new MemoryEventStore();
            EventBus                   evtBus   = new SimpleEventBus();
            DisruptorCommandBus        cmdBus   = new DisruptorCommandBus(evtStore,evtBus);
            AggregateFactory<DataItem> agf      = new GenericAggregateFactory<>(DataItem.class);

            evtBus.subscribe(
                new AnnotationEventListenerAdapter(new DisruptorEventHandler()));

            AggregateAnnotationCommandHandler.subscribe(
                DataItem.class,
                cmdBus.createRepository(agf),
                cmdBus);

            LOGGER.info("dispatch");
            cmdBus.dispatch(new GenericCommandMessage<>(
                new DataItemCmd.Create("id1","description_1"))
            );

            LOGGER.info("sleep");
            Thread.sleep(1000 * 5);

            LOGGER.info("stop");
            cmdBus.stop();

        } catch(Exception e) {
            LOGGER.warn("Exception",e);
        }
    }
}
