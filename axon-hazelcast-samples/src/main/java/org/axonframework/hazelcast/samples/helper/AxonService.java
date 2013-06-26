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
package org.axonframework.hazelcast.samples.helper;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.annotation.AggregateAnnotationCommandHandler;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.common.Subscribable;
import org.axonframework.domain.AggregateRoot;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.EventListener;
import org.axonframework.eventhandling.annotation.AnnotationEventListenerAdapter;
import org.axonframework.eventsourcing.CachingEventSourcingRepository;
import org.axonframework.eventsourcing.EventSourcedAggregateRoot;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.axonframework.eventsourcing.GenericAggregateFactory;
import org.axonframework.eventstore.EventStore;
import org.axonframework.hazelcast.cache.IHzCacheProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class AxonService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AxonService.class);

    private CommandBus m_commandBus;
    private CommandGateway m_commandGateway;
    private EventStore m_eventStore;
    private EventBus m_eventBus;
    private IHzCacheProvider m_cacheProvider;

    private final Set<EventListener> m_eventListeners;
    private final Map<Object,Subscribable> m_eventHandlers;
    private final Map<Class<? extends AggregateRoot>,AggregateSubscription> m_aggregates;

    /**
     * c-tor
     */
    public AxonService() {
        m_commandBus = null;
        m_commandGateway = null;
        m_eventStore = null;
        m_eventBus = null;
        m_cacheProvider = null;
        m_eventListeners = Sets.newHashSet();
        m_eventHandlers = Maps.newConcurrentMap();
        m_aggregates = Maps.newConcurrentMap();
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     *
     */
    public void init() {
        LOGGER.debug("CommandBus     : {}",m_commandBus);
        LOGGER.debug("CommandGateway : {}",m_commandGateway);
        LOGGER.debug("EventStore     : {}",m_eventStore);
        LOGGER.debug("EventBus       : {}",m_eventBus);
        LOGGER.debug("IHzCacheProvider : {}",m_cacheProvider);
    }

    /**
     *
     */
    public void destroy() {
        LOGGER.debug("Cleanup - EventListeners ({})",m_eventListeners.size());
        for(EventListener listener : m_eventListeners) {
            m_eventBus.unsubscribe(listener);
        }

        m_eventListeners.clear();

        LOGGER.debug("Cleanup - EventHandlers ({})",m_eventHandlers.size());
        for(Subscribable subscription : m_eventHandlers.values()) {
            subscription.unsubscribe();
        }

        m_eventHandlers.clear();

        LOGGER.debug("Cleanup - AggregateSubscription ({})",m_aggregates.size());
        for(AggregateSubscription subscription : m_aggregates.values()) {
            subscription.handler.unsubscribe();
        }

        m_aggregates.clear();
    }

    // *************************************************************************
    //
    // *************************************************************************

    public CommandBus getCommandBus() {
        return m_commandBus;
    }

    public void setCommandBus(CommandBus commandBus) {
        m_commandBus = commandBus;
    }

    public CommandGateway getCommandGateway() {
        return m_commandGateway;
    }

    public void setCommandGateway(CommandGateway commandGateway) {
        m_commandGateway = commandGateway;
    }

    public EventStore getEventStore() {
        return m_eventStore;
    }

    public void setEventStore(EventStore eventStore) {
        m_eventStore = eventStore;
    }

    public EventBus getEventBus() {
        return m_eventBus;
    }

    public void setEventBus(EventBus eventBus) {
        m_eventBus = eventBus;
    }

    public IHzCacheProvider getCacheProvider() {
        return m_cacheProvider;
    }

    public void setCacheProvider(IHzCacheProvider cacheProvider) {
        m_cacheProvider = cacheProvider;
    }

    // *************************************************************************
    //
    // *************************************************************************

    public void send(Object command) {
        m_commandGateway.send(command);
    }

    public <R> void send(Object command, CommandCallback<R> callback) {
        m_commandGateway.send(command,callback);
    }

    public <R> R sendAndWait(Object command) {
        return m_commandGateway.sendAndWait(command);
    }

    public <R> R sendAndWait(Object command, long timeout, TimeUnit unit) {
        return m_commandGateway.sendAndWait(command,timeout,unit);
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
            m_eventHandlers.put(
                eventHandler,
                AnnotationEventListenerAdapter.subscribe(eventHandler, m_eventBus));
        }

    }

    /**
     *
     * @param eventHandler
     */
    public void removeEventHandler(Object eventHandler) {
        if(m_eventHandlers.containsKey(eventHandler)) {
            m_eventHandlers.get(eventHandler).unsubscribe();
            m_eventHandlers.remove(eventHandler);
        }
    }

    /**
     *
     * @param eventListener
     */
    public void addEventListener(EventListener eventListener) {
        if(m_eventListeners.add(eventListener)) {
            m_eventBus.subscribe(eventListener);
        }
    }

    /**
     *
     * @param eventListener
     */
    public void removeEventListener(EventListener eventListener) {
        if(eventListener != null) {
            m_eventBus.unsubscribe(eventListener);
        }
    }

    /**
     *
     * @param aggregateType
     */
    @SuppressWarnings("unchecked")
    public void addAggregateType(Class<? extends EventSourcedAggregateRoot> aggregateType) {
        removeAggregateType(aggregateType);

        EventSourcingRepository eventRepository = null;
        if(m_cacheProvider == null) {
            EventSourcingRepository repo = new EventSourcingRepository(aggregateType,m_eventStore);
            repo.setEventBus(m_eventBus);

            eventRepository = repo;
        } else {
            CachingEventSourcingRepository repo =
                new CachingEventSourcingRepository(new GenericAggregateFactory(aggregateType),m_eventStore);

            repo.setEventBus(m_eventBus);
            repo.setCache(m_cacheProvider.getCache(aggregateType));

            eventRepository = repo;
        }

        m_aggregates.put(aggregateType,new AggregateSubscription(
            eventRepository,
            AggregateAnnotationCommandHandler.subscribe(
                aggregateType,
                eventRepository,
                m_commandBus)));
    }

    /**
     *
     * @param aggregateType
     */
    public void removeAggregateType(Class<? extends EventSourcedAggregateRoot> aggregateType) {
        if(m_aggregates.containsKey(aggregateType)) {
            m_aggregates.get(aggregateType).handler.unsubscribe();
            m_aggregates.remove(aggregateType);
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    private final class AggregateSubscription {

        public final EventSourcingRepository repository;
        public final Subscribable handler;

        /**
         * c-tor
         *
         * @param repository
         * @param handler
         */
        public AggregateSubscription(EventSourcingRepository repository,Subscribable handler) {
            this.repository = repository;
            this.handler    = handler;
        }
    }
}
