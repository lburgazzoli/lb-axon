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
package org.axonframework.ext.hazelcast.samples;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.disruptor.DisruptorCommandBus;
import org.axonframework.commandhandling.distributed.DistributedCommandBus;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.axonframework.eventhandling.ClusteringEventBus;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventstore.EventStore;
import org.axonframework.ext.hazelcast.HzProxy;
import org.axonframework.ext.hazelcast.distributed.commandbus.queue.HzCommandBusConnector;
import org.axonframework.ext.hazelcast.eventhandling.HzEventBusTerminal;
import org.axonframework.ext.hazelcast.eventhandling.pub.PackageNamePublisher;
import org.axonframework.ext.hazelcast.eventhandling.sub.DynamicSubscriber;
import org.axonframework.ext.hazelcast.samples.helper.AxonService;
import org.axonframework.ext.hazelcast.samples.helper.CommandCallbackTracer;
import org.axonframework.ext.hazelcast.samples.helper.LocalHazelcastConfig;
import org.axonframework.ext.hazelcast.samples.helper.MemoryEventStore;
import org.axonframework.ext.hazelcast.samples.model.DataItem;
import org.axonframework.ext.hazelcast.samples.model.DataItemCmd;
import org.axonframework.ext.hazelcast.samples.model.DataItemEvt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class SimpleApp {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(SimpleApp.class);

    private static final CommandCallback<Object> CMDCBK =
        new CommandCallbackTracer<Object>(LOGGER);

    // *************************************************************************
    //
    // *************************************************************************

    private static AxonService newAxonService(HzProxy proxy,boolean remote,String nodeName) {
        HzEventBusTerminal evtBusTer = new HzEventBusTerminal(proxy);
        evtBusTer.setPublisher(new PackageNamePublisher());
        evtBusTer.setSubscriber(new DynamicSubscriber(
            proxy.getDistributedObjectName("org.axonframework.ext.hazelcast.samples.model.*"))
        );

        CommandBus cmdBus    = null;
        EventStore evtStore  = new MemoryEventStore();
        EventBus   evtBus    = new ClusteringEventBus(evtBusTer);

        if(remote) {
            HzCommandBusConnector cmdBusCnx =
                new HzCommandBusConnector(proxy,new SimpleCommandBus(),"axon",nodeName);

            cmdBusCnx.connect();

            cmdBus = new DistributedCommandBus(cmdBusCnx);
        } else {
            cmdBus = new SimpleCommandBus();
        }

        AxonService svc = new AxonService();
        svc.setCommandBus(cmdBus);
        svc.setCommandGateway(new DefaultCommandGateway(cmdBus));
        svc.setEventStore(evtStore);
        svc.setEventBus(evtBus);

        return svc;
    }

    // *************************************************************************
    //
    // *************************************************************************

    private static final class AxonServiceThread extends Thread {
        private final HzProxy m_proxy;
        private final AtomicBoolean m_running;
        private final Logger m_logger;

        /**
         * @param threadName
         * @param proxy
         */
        public AxonServiceThread(String threadName,HzProxy proxy) {
            super(threadName);
            m_proxy   = proxy;
            m_running = new AtomicBoolean(false);
            m_logger  = LoggerFactory.getLogger(threadName);
        }

        @Override
        public void run() {
            m_running.set(true);

            AxonService svc = newAxonService(m_proxy,false,null);
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
            m_logger.debug("DataItemEvt <{}>",data);
        }

        @EventHandler
        public void handle(DataItemEvt.Update data) {
            m_logger.debug("DataItemEvt <{}>",data);
            m_running.set(false);
        }

        /**
         *
         */
        public void shutdown() {
            m_running.set(false);
        }
    }


    private static final class AxonRemoteServiceThread extends Thread {
        private final HzProxy m_proxy;
        private final AtomicBoolean m_running;
        private final String m_nodeName;
        private final Logger m_logger;

        /**
         * @param nodeName
         * @param threadName
         * @param proxy
         */
        public AxonRemoteServiceThread(String nodeName,String threadName,HzProxy proxy) {
            super(threadName);
            m_proxy    = proxy;
            m_running  = new AtomicBoolean(false);
            m_nodeName = nodeName;
            m_logger   = LoggerFactory.getLogger(threadName);
        }

        @Override
        public void run() {
            m_running.set(true);

            AxonService svc = newAxonService(m_proxy,true,m_nodeName);
            svc.init();
            svc.addAggregateType(DataItem.class);

            while(m_running.get()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }

            svc.destroy();
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
        HzProxy hxPx = new HzProxy(new LocalHazelcastConfig());
        hxPx.setDistributedObjectNamePrefix("axon");
        hxPx.init();

        AxonService svc = newAxonService(hxPx,true,"main");
        svc.init();
        svc.addAggregateType(DataItem.class);

        try {
            Thread.sleep(1000 * 5);
        } catch(Exception e) {
        }

        ExecutorService pool = Executors.newFixedThreadPool(15);

        for(int i=0;i<10;i++) {
            AxonRemoteServiceThread st =
                new AxonRemoteServiceThread(
                    "axon-svc-r-" + i,
                    "axon-svc-r-" + i + "-th",
                    hxPx);

            pool.execute(st);
        }

        for(int i=0;i<5;i++) {
            AxonServiceThread st =
                new AxonServiceThread(
                    "axon-svc-r-" + i + "-th",
                    hxPx);

            pool.execute(st);
        }

        try {
            Thread.sleep(1000 * 5);
        } catch(Exception e) {
        }

        for(int i=0;i<10;i++) {
            svc.send(new DataItemCmd.Create(
                String.format("d_%02d",i),
                String.format("t_%02d",i)),
                CMDCBK
            );
        }

        try {
            pool.awaitTermination(5, TimeUnit.MINUTES);
        } catch(Exception e) {
        }

        svc.destroy();
        hxPx.destroy();
    }
}
