/*
 * Copyright (c) 2010-2013. Axon Framework
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
package org.axonframework.hazelcast.samples;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.SimpleEventBus;
import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventstore.EventStore;
import org.axonframework.hazelcast.samples.helper.AxonApplication;
import org.axonframework.hazelcast.samples.helper.DefaultCommandCallback;
import org.axonframework.hazelcast.samples.helper.MemoryEventStore;
import org.axonframework.hazelcast.samples.model.DataItem;
import org.axonframework.hazelcast.samples.model.DataItemCmd;
import org.axonframework.hazelcast.samples.model.DataItemEvt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.UUID.randomUUID;

/**
 *
 */
public class SimpleApp {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(SimpleApp.class);

    private static final CommandCallback<Object> CMDCBK =
        new DefaultCommandCallback<Object>();

    // *************************************************************************
    //
    // *************************************************************************

    private static final class DataItemEvtHandler {
        @EventHandler
        public void handle(DataItemEvt.Create data) {
            LOGGER.debug("DataItemEvt.Create <{}> :\n\tid   = {}\n\ttext = {}",
                data.getClass().getName(),
                data.getId(),
                data.getText());
        }

        @EventHandler
        public void handle(DataItemEvt.Update data) {
            LOGGER.debug("DataItemEvt.Update <{}> :\n\tid   = {}\n\ttext = {}",
                data.getClass().getName(),
                data.getId(),
                data.getText());
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static void main(String[] args) {
        CommandBus        cmdBus     = new SimpleCommandBus();
        CommandGateway    cmdGw      = new DefaultCommandGateway(cmdBus);
        EventStore        evtStore   = new MemoryEventStore();
        EventBus          evtBus     = new SimpleEventBus();

        AxonApplication app = new AxonApplication();
        //app.setCacheProvider(new HazelcastCacheProvider(new DefaultHazelcastManager()));
        app.setCommandBus(cmdBus);
        app.setCommandGateway(cmdGw);
        app.setEventStore(evtStore);
        app.setEventBus(evtBus);

        app.init();
        app.addEventHandler(new DataItemEvtHandler());
        app.addAggregateType(DataItem.class);

        app.send(new DataItemCmd.Create("d01",randomUUID().toString()),CMDCBK);
        app.send(new DataItemCmd.Update("d01",randomUUID().toString()),CMDCBK);

        app.destroy();
    }
}
