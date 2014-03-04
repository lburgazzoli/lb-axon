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
package org.axonframework.ext.hazelcast.distributed;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.annotation.AggregateAnnotationCommandHandler;
import org.axonframework.commandhandling.distributed.DistributedCommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.axonframework.domain.AggregateRoot;
import org.axonframework.eventhandling.ClusteringEventBus;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.EventListener;
import org.axonframework.eventhandling.SimpleEventBus;
import org.axonframework.eventhandling.annotation.AnnotationEventListenerAdapter;
import org.axonframework.eventsourcing.EventSourcedAggregateRoot;
import org.axonframework.ext.hazelcast.HzConstants;
import org.axonframework.ext.hazelcast.HzProxy;
import org.axonframework.ext.hazelcast.IHzProxy;
import org.axonframework.ext.hazelcast.distributed.commandbus.HzCommand;
import org.axonframework.ext.hazelcast.distributed.commandbus.HzCommandReply;
import org.axonframework.ext.hazelcast.distributed.commandbus.executor.HzCommandBusConnector;
import org.axonframework.ext.hazelcast.eventhandling.HzEventBusTerminal;
import org.axonframework.ext.hazelcast.eventhandling.IHzTopicPublisher;
import org.axonframework.ext.hazelcast.eventhandling.IHzTopicSubscriber;
import org.axonframework.ext.hazelcast.store.HzEventStore;
import org.axonframework.ext.repository.EventSourcingRepositoryFactory;
import org.axonframework.ext.repository.IRepositoryFactory;
import org.axonframework.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class HzAxonEngine implements IHzAxonEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(HzAxonEngine.class);

    private final String m_nodeName;
    private final IHzProxy m_hz;
    private HzEventBusTerminal m_evtBusTer;
    private HzCommandBusConnector m_connector;
    private IRepositoryFactory m_repositoryFactory;
    private HzEventStore m_evtStore;
    private EventBus m_evtBus;
    private CommandBus m_cmdBus;
    private CommandGateway m_cmdGw;

    private final Set<EventListener> m_eventListeners;
    private final Map<Object,EventListener> m_eventHandlers;
    private final Map<Class<? extends AggregateRoot>,AggregateSubscription> m_aggregates;

    /**
     * c-tor
     *
     * @param nodeName
     * @param config
     */
    public HzAxonEngine(final String nodeName, final Config config) {
        this(nodeName,Hazelcast.newHazelcastInstance(config));
    }

    /**
     * c-tor
     *
     * @param nodeName
     * @param hz
     */
    public HzAxonEngine(final String nodeName, final HazelcastInstance hz) {
        m_hz           = new HzProxy(hz);
        m_nodeName     = nodeName;
        m_evtBusTer    = null;
        m_connector    = null;
        m_cmdBus       = null;
        m_cmdGw        = null;
        m_evtStore     = null;
        m_evtBus       = null;


        m_eventListeners = Sets.newHashSet();
        m_eventHandlers  = Maps.newConcurrentMap();
        m_aggregates     = Maps.newConcurrentMap();
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     *
     */
    public void init() {
        createEventBus();
        cerateEventStore();
        createCommandBus();
        createCommandGateway();

        m_hz.getInstance().getUserContext().put(HzConstants.USER_CONTEXT_NAME,this);
    }

    /**
     *
     */
    public void destroy() {
        LOGGER.debug("Cleanup - EventListeners ({})",m_eventListeners.size());
        for(EventListener listener : m_eventListeners) {
            m_evtBus.unsubscribe(listener);
        }

        m_eventListeners.clear();

        LOGGER.debug("Cleanup - EventHandlers ({})",m_eventHandlers.size());
        for(EventListener listener : m_eventHandlers.values()) {
            m_evtBus.unsubscribe(listener);
        }

        m_eventHandlers.clear();

        LOGGER.debug("Cleanup - AggregateSubscription ({})",m_aggregates.size());
        for(AggregateSubscription subscription : m_aggregates.values()) {
            for (String supportedCommand : subscription.handler.supportedCommands()) {
                m_cmdBus.unsubscribe(supportedCommand, subscription.handler);
            }
        }

        m_aggregates.clear();

        LOGGER.debug("Cleanup - CommandBusConnector");
        if(m_connector != null) {
            m_connector.close();
        }

        LOGGER.debug("Cleanup - EventStore");
        if(m_evtStore != null) {
            try {
                m_evtStore.close();
            } catch(IOException e) {
                LOGGER.warn("EventStore - IOException",e);
            }
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public void send(Object command) {
        m_cmdGw.send(command);
    }

    @Override
    public <R> void send(Object command, CommandCallback<R> callback) {
        m_cmdGw.send(command,callback);
    }

    @Override
    public <R> R sendAndWait(Object command) {
        return m_cmdGw.sendAndWait(command);
    }

    @Override
    public <R> R sendAndWait(Object command, long timeout, TimeUnit unit) {
        return m_cmdGw.sendAndWait(command,timeout,unit);
    }

    @Override
    public Future<HzCommandReply> dispatch(final HzCommand command) {
        return m_connector.dispatch(command);
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     *
     * @param eventHandler
     */
    public void addEventHandler(Object eventHandler) {
        if(!m_eventHandlers.containsKey(eventHandler)) {
            EventListener eventListener = new AnnotationEventListenerAdapter(eventHandler);
            m_evtBus.subscribe(eventListener);

            m_eventHandlers.put(eventHandler,eventListener);
        }
    }

    /**
     *
     * @param eventHandler
     */
    public void removeEventHandler(Object eventHandler) {
        if(m_eventHandlers.containsKey(eventHandler)) {
            m_evtBus.unsubscribe(m_eventHandlers.get(eventHandler));
            m_eventHandlers.remove(eventHandler);
        }
    }

    /**
     *
     * @param eventListener
     */
    public void addEventListener(EventListener eventListener) {
        if(m_eventListeners.add(eventListener)) {
            m_evtBus.subscribe(eventListener);
        }
    }

    /**
     *
     * @param eventListener
     */
    public void removeEventListener(EventListener eventListener) {
        if(eventListener != null) {
            m_evtBus.unsubscribe(eventListener);
        }
    }

    /**
     *
     * @param aggregateType
     */
    @SuppressWarnings("unchecked")
    public <T extends EventSourcedAggregateRoot> void addAggregateType(Class<T> aggregateType) {
        removeAggregateType(aggregateType);

        Repository<T> repo = m_repositoryFactory.createRepository(aggregateType);

        m_aggregates.put(aggregateType,new AggregateSubscription(
            repo,
            AggregateAnnotationCommandHandler.subscribe(
                aggregateType,
                repo,
                m_cmdBus))
        );
    }

    /**
     *
     * @param aggregateType
     */
    public void removeAggregateType(Class<? extends EventSourcedAggregateRoot> aggregateType) {
        if(m_aggregates.containsKey(aggregateType)) {
            AggregateSubscription subscription = m_aggregates.get(aggregateType);
            for (String supportedCommand : subscription.handler.supportedCommands()) {
                m_cmdBus.subscribe(supportedCommand, subscription.handler);
            }

            m_aggregates.remove(aggregateType);
        }
    }

    // *************************************************************************
    // Getters/Setters
    // *************************************************************************

    /**
     *
     * @param publisher
     */
    public void setPublisher(IHzTopicPublisher publisher) {
        createEventBusTerminal().setPublisher(publisher);
    }

    /**
     *
     * @param subscriber
     */
    public void setSubscriber(IHzTopicSubscriber subscriber) {
        createEventBusTerminal().setSubscriber(subscriber);
    }

    // *************************************************************************
    // Helpers
    // *************************************************************************

    /**
     *
     */
    private HzEventBusTerminal createEventBusTerminal() {
        if(m_evtBusTer == null) {
            m_evtBusTer = new HzEventBusTerminal(m_hz);
        }

        return m_evtBusTer;
    }

    /**
     *
     * @return
     */
    private CommandBus createCommandBus() {
        if(m_cmdBus == null && m_evtStore != null && m_evtBus != null) {
            SimpleCommandBus scb = new SimpleCommandBus();

            m_repositoryFactory = new EventSourcingRepositoryFactory(m_evtStore,m_evtBus);

            m_connector = new HzCommandBusConnector(m_hz,scb,"","");
            m_connector.open();

            m_cmdBus = new DistributedCommandBus(m_connector);
        }

        return m_cmdBus;
    }

    /**
     *
     * @return
     */
    private HzEventStore cerateEventStore() {
        if(m_evtStore == null) {
            m_evtStore = new HzEventStore(m_hz);
        }

        return m_evtStore;
    }

    /**
     *
     * @return
     */
    private EventBus createEventBus() {
        if(m_evtBus == null) {
            m_evtBus = (m_evtBusTer != null)
                ? new ClusteringEventBus(m_evtBusTer)
                : new SimpleEventBus();
        }

        return m_evtBus;
    }

    /**
     *
     * @return
     */
    private CommandGateway createCommandGateway() {
        if(m_cmdGw == null && m_cmdBus != null) {
            m_cmdGw = new DefaultCommandGateway(m_cmdBus);
        }

        return m_cmdGw;
    }

    // *************************************************************************
    //
    // *************************************************************************

    private final class AggregateSubscription {

        public final Repository<?> repository;
        public final AggregateAnnotationCommandHandler<?> handler;

        /**
         * c-tor
         *
         * @param repository
         * @param handler
         */
        public AggregateSubscription(final Repository<?> repository,final AggregateAnnotationCommandHandler<?> handler) {
            this.repository = repository;
            this.handler    = handler;
        }
    }
}
