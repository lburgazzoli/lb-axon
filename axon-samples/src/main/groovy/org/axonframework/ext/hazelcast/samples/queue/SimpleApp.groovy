/*
 * Copyright (c) 2010-2013. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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
package org.axonframework.ext.hazelcast.samples.queue

import com.github.lburgazzoli.hazelcast.HzPrefixedInstance
import org.axonframework.commandhandling.CommandBus
import org.axonframework.commandhandling.CommandCallback
import org.axonframework.commandhandling.SimpleCommandBus
import org.axonframework.commandhandling.disruptor.DisruptorCommandBus
import org.axonframework.commandhandling.distributed.DistributedCommandBus
import org.axonframework.commandhandling.gateway.DefaultCommandGateway
import org.axonframework.eventhandling.ClusteringEventBus
import org.axonframework.eventhandling.EventBus
import org.axonframework.eventhandling.annotation.EventHandler
import org.axonframework.eventstore.EventStore
import org.axonframework.ext.CommandCallbackTracer
import org.axonframework.ext.eventstore.MemoryEventStore
import org.axonframework.ext.hazelcast.distributed.commandbus.queue.HzCommandBusConnector
import org.axonframework.ext.hazelcast.eventhandling.HzEventBusTerminal
import org.axonframework.ext.hazelcast.eventhandling.pub.PackageNamePublisher
import org.axonframework.ext.hazelcast.eventhandling.sub.DynamicSubscriber
import org.axonframework.ext.hazelcast.samples.model.DataItem
import org.axonframework.ext.hazelcast.samples.model.DataItemCmd
import org.axonframework.ext.hazelcast.samples.model.DataItemEvt
import org.axonframework.ext.repository.DisruptorRepositoryFactory
import org.axonframework.ext.repository.EventSourcingRepositoryFactory
import org.axonframework.ext.repository.IRepositoryFactory
import org.axonframework.ext.samples.AxonSamplesUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

public class SimpleApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleApp.class)
    private static final CommandCallback<Object> CMDCBK = new CommandCallbackTracer<>(LOGGER)

    // *************************************************************************
    //
    // *************************************************************************

    private static AxonService newAxonService(final HzPrefixedInstance instance,boolean remote,String nodeName) {
        def cmdBus      = null
        def repoFactory = null

        def evtStore = new MemoryEventStore()
        def evtBus = new ClusteringEventBus(new HzEventBusTerminal(
            instance,
            new PackageNamePublisher(),
            new DynamicSubscriber(
                instance.getPrefix("org.axonframework.ext.hazelcast.samples.model.*")
            )
        ))

        if(remote) {
            def cmdBusLoc = new DisruptorCommandBus(evtStore,evtBus)
            def cmdBusCnx = new HzCommandBusConnector(instance,cmdBusLoc,"axon",nodeName)

            cmdBusCnx.open()

            cmdBus = new DistributedCommandBus(cmdBusCnx)
            repoFactory = new DisruptorRepositoryFactory(cmdBusLoc)
        } else {
            cmdBus = new SimpleCommandBus()
            repoFactory = new EventSourcingRepositoryFactory(evtStore,evtBus)
        }

        def svc = new AxonService()
        svc.repositoryFactory = repoFactory
        svc.commandBus = cmdBus
        svc.commandGateway = new DefaultCommandGateway(cmdBus)
        svc.eventStore = evtStore
        svc.eventBus = evtBus

        return svc
    }

    // *************************************************************************
    //
    // *************************************************************************

    private static final class AxonServiceThread implements Runnable {
        private final HzPrefixedInstance m_instance
        private final AtomicBoolean m_running
        private final Logger m_logger

        public AxonServiceThread(final String threadName,final HzPrefixedInstance instance) {
            m_instance = instance
            m_running  = new AtomicBoolean(false)
            m_logger   = LoggerFactory.getLogger(threadName)
        }

        @Override
        public void run() {
            m_running.set(true)

            def svc = newAxonService(m_instance,false,null)
            svc.init()
            svc.addEventHandler(this)

            while(m_running.get()) {
                try {
                    Thread.sleep(1000)
                } catch (InterruptedException e) {
                }
            }

            svc.destroy()
        }

        @EventHandler
        public void handle(DataItemEvt.Create data) {
            m_logger.debug("DataItemEvt <{}>",data)
        }

        @EventHandler
        public void handle(DataItemEvt.Update data) {
            m_logger.debug("DataItemEvt <{}>",data)
            m_running.set(false)
        }

        public void shutdown() {
            m_running.set(false)
        }
    }

    private static final class AxonRemoteServiceThread implements Runnable {
        private final HzPrefixedInstance m_instance
        private final AtomicBoolean m_running
        private final String m_nodeName
        private final Logger m_logger

        public AxonRemoteServiceThread(String nodeName,String threadName,HzPrefixedInstance instance) {
            m_instance = instance
            m_running  = new AtomicBoolean(false)
            m_nodeName = nodeName
            m_logger   = LoggerFactory.getLogger(threadName)
        }

        @Override
        public void run() {
            m_running.set(true)

            def svc = newAxonService(m_instance,true,m_nodeName)
            svc.init()
            svc.addAggregateType(DataItem.class)

            while(m_running.get()) {
                AxonSamplesUtils.sleep(1000)
            }

            svc.destroy()
        }

        public void shutdown() {
            m_running.set(false)
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static void main(String[] args) throws Exception {
        def hx  = null
        def svc = null

        try {
            hx = new HzPrefixedInstance(AxonSamplesUtils.newHazelcastLocalConfig(), "axon")

            svc = newAxonService(hx, true, "main")
            svc.init()
            svc.addAggregateType(DataItem.class)

            AxonSamplesUtils.sleep(5000)

            ExecutorService pool = Executors.newFixedThreadPool(15)

            for (i in 0..10) {
                pool.execute(
                    new AxonRemoteServiceThread(
                        "axon-svc-r-" + i,
                        "axon-svc-r-" + i + "-th",
                        hx)
                )
            }

            for (i in 0..5) {
                pool.execute(new AxonServiceThread("axon-svc-r-" + i + "-th",hx))
            }

            AxonSamplesUtils.sleep(5000)

            for (i in 0..10) {
                svc.send(
                    new DataItemCmd.Create(
                        String.format("d_%09d", i),
                        String.format("t_%09d", i)),
                    CMDCBK
                )
            }

            pool.shutdown();
            pool.awaitTermination(5, TimeUnit.MINUTES)
        } catch (Exception e) {
            LOGGER.warn("Exception",e)
        } finally {
            if(svc != null) {
                svc.destroy()
            }

            if(hx != null) {
                hx.shutdown()
            }
        }
    }
}
