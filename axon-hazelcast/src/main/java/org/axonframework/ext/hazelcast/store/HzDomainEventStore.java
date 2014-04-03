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
package org.axonframework.ext.hazelcast.store;

import org.axonframework.domain.DomainEventMessage;
import org.axonframework.domain.DomainEventStream;
import org.axonframework.ext.eventstore.AbstractDomainEventStore;
import org.axonframework.ext.hazelcast.IHzProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 *
 */
public class HzDomainEventStore extends AbstractDomainEventStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(HzDomainEventStore.class);

    private final IHzProxy m_hazelcastManager;
    private final Map<HzDomainEventKey,HzDomainEventMessage> m_storage;

    /**
     * c-tor
     *
     * @param storageId
     * @param aggregateType
     * @param aggregateId
     * @param hazelcastManager
     */
    public HzDomainEventStore(String storageId, String aggregateType, String aggregateId, IHzProxy hazelcastManager) {
        super(storageId,aggregateType,aggregateId);
        m_hazelcastManager = hazelcastManager;
        m_storage = m_hazelcastManager.getMap(storageId);
    }


    @Override
    public void close() throws IOException {
    }

    @Override
    public void clear() {
        m_storage.clear();
    }

    @Override
    public long getStorageSize() {
        return m_storage.size();
    }


    @Override
    @SuppressWarnings("unchecked")
    public void add(DomainEventMessage message) {
        m_storage.put(new HzDomainEventKey(message),new HzDomainEventMessage(message));
    }

    @Override
    public DomainEventStream getEventStream() {
        return new HzDomainEventStream(m_storage);
    }
}
