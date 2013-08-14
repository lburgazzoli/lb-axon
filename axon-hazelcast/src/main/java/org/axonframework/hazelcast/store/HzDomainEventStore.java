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
package org.axonframework.hazelcast.store;

import org.axonframework.domain.DomainEventMessage;
import org.axonframework.domain.DomainEventStream;
import org.axonframework.hazelcast.IHzProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 *
 */
public class HzDomainEventStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(HzDomainEventStore.class);

    private final String m_aggregateType;
    private final String m_aggregateId;
    private final String m_storageId;
    private final IHzProxy m_hazelcastManager;
    private final Map<Long,HzDomainEventMessage> m_storage;

    /**
     * c-tor
     *
     * @param storageId
     * @param aggregateType
     * @param aggregateId
     * @param hazelcastManager
     */
    public HzDomainEventStore(String storageId, String aggregateType, String aggregateId, IHzProxy hazelcastManager) {
        m_aggregateType = aggregateType;
        m_aggregateId = aggregateId;
        m_storageId = storageId;
        m_hazelcastManager = hazelcastManager;
        m_storage = m_hazelcastManager.getMap(m_storageId);
    }

    /**
     * clean the stored events
     */
    public void clear() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        ClassLoader newCl = m_hazelcastManager.getClassloader();

        try {
            Thread.currentThread().setContextClassLoader(newCl);
            m_storage.clear();
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    /**
     *
     * @return the aggregate type
     */
    public String getAggregateType() {
        return m_aggregateType;
    }

    /**
     *
     * @return the aggregate id
     */
    public String getAggregateId() {
        return m_aggregateId;
    }

    /**
     *
     * @return the storage id
     */
    public String getStorageId() {
        return m_storageId;
    }

    /**
     *
     * @return the number of items stored
     */
    public int getStorageSize() {
        return m_storage.size();
    }

    /**
     *
     * @param message
     */
    @SuppressWarnings("unchecked")
    public void add(DomainEventMessage message) {
        m_storage.put(message.getSequenceNumber(),new HzDomainEventMessage(message));
    }

    /**
     *
     * @return the event stream
     */
    public DomainEventStream getEventStream() {
        DomainEventStream des = null;

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        ClassLoader newCl = m_hazelcastManager.getClassloader();

        try {
            Thread.currentThread().setContextClassLoader(newCl);
            des = new HzDomainEventStream(m_storage);
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }

        return des;
    }
}
