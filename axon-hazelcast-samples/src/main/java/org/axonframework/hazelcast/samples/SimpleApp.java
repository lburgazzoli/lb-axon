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
import org.axonframework.eventhandling.ClusteringEventBus;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventstore.EventStore;
import org.axonframework.hazelcast.DefaultHazelcastInstanceProxy;
import org.axonframework.hazelcast.eventhandling.HazelcastEventBusTerminal;
import org.axonframework.hazelcast.eventhandling.sub.DynamicSubscriber;
import org.axonframework.hazelcast.eventhandling.pub.PackageNamePublisher;
import org.axonframework.hazelcast.samples.helper.AxonService;
import org.axonframework.hazelcast.samples.helper.CommandCallbackTracer;
import org.axonframework.hazelcast.samples.helper.LocalHazelcastConfig;
import org.axonframework.hazelcast.samples.helper.MemoryEventStore;
import org.axonframework.hazelcast.samples.model.DataItem;
import org.axonframework.hazelcast.samples.model.DataItemCmd;
import org.axonframework.hazelcast.samples.model.DataItemEvt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.UUID.randomUUID;

/**
 *
 */
public class SimpleApp {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(SimpleApp.class);

    private static final CommandCallback<Object> CMDCBK =
        new CommandCallbackTracer<Object>();

    // *************************************************************************
    //
    // *************************************************************************

    private static AxonService newAxonService(DefaultHazelcastInstanceProxy proxy) {
        HazelcastEventBusTerminal evtBusTer = new HazelcastEventBusTerminal(proxy);
        evtBusTer.setPublisher(new PackageNamePublisher());
        evtBusTer.setSubscriber(new DynamicSubscriber(
            proxy.getDistributedObjectName("org.axonframework.hazelcast.samples.model.*"))
        );

        CommandBus     cmdBus   = new SimpleCommandBus();
        CommandGateway cmdGw    = new DefaultCommandGateway(cmdBus);
        EventStore     evtStore = new MemoryEventStore();
        EventBus       evtBus   = new ClusteringEventBus(evtBusTer);

        AxonService svc = new AxonService();
        svc.setCommandBus(cmdBus);
        svc.setCommandGateway(cmdGw);
        svc.setEventStore(evtStore);
        svc.setEventBus(evtBus);

        return svc;
    }

    // *************************************************************************
    //
    // *************************************************************************

    private static final class AxonServiceThread extends Thread {
        private final DefaultHazelcastInstanceProxy m_proxy;
        private final AtomicBoolean m_running;

        /**
         * @param threadName
         * @param proxy
         */
        public AxonServiceThread(String threadName,DefaultHazelcastInstanceProxy proxy) {
            super(threadName);
            m_proxy   = proxy;
            m_running = new AtomicBoolean(false);
        }

        @Override
        public void run() {
            m_running.set(true);

            AxonService svc = newAxonService(m_proxy);
            svc.init();
            svc.addEventHandler(this);

            while(m_running.get()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }

            svc.destroy();
        }

        @EventHandler
        public void handle(DataItemEvt.Create data) {
            LOGGER.debug("DataItemEvt <{}>",data);
        }

        @EventHandler
        public void handle(DataItemEvt.Update data) {
            LOGGER.debug("DataItemEvt <{}>",data);
            m_running.set(false);
        }

        /**
         *
         */
        public void shutdown() {
            m_running.set(false);
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static void main(String[] args) {
        DefaultHazelcastInstanceProxy hxPx = new DefaultHazelcastInstanceProxy(new LocalHazelcastConfig());
        hxPx.setDistributedObjectNamePrefix("axon");
        hxPx.init();

        AxonService svc = newAxonService(hxPx);
        svc.init();
        svc.addAggregateType(DataItem.class);

        AxonServiceThread st1 = new AxonServiceThread("axon-svc1-th",hxPx);
        AxonServiceThread st2 = new AxonServiceThread("axon-svc2-th",hxPx);

        st1.start();
        st2.start();

        svc.send(new DataItemCmd.Create("d01", randomUUID().toString()), CMDCBK);
        svc.send(new DataItemCmd.Update("d01", randomUUID().toString()), CMDCBK);

        try {
            st1.join();
            st2.join();
        } catch (InterruptedException e) {
        }

        svc.destroy();
        hxPx.destroy();
    }
}
