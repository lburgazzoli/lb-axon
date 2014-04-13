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

import com.hazelcast.core.HazelcastInstance;
import org.axonframework.domain.DomainEventMessage;
import org.axonframework.domain.DomainEventStream;
import org.axonframework.ext.eventstore.AbstractDomainEventStore;

import java.io.IOException;
import java.util.Map;

/**
 *
 */
public class HzDomainEventStore<T> extends AbstractDomainEventStore<T> {
    private final HazelcastInstance m_hzInstance;
    private final Map<HzDomainEventKey<T>,HzDomainEventMessage<T>> m_storage;

    /**
     * c-tor
     *
     * @param storageId      the storage id
     * @param aggregateType  the aggregate type
     * @param aggregateId    the aggregate id
     * @param hzInstance     the Hazelcast instance
     */
    public HzDomainEventStore(String storageId, String aggregateType, String aggregateId, HazelcastInstance hzInstance) {
        super(storageId,aggregateType,aggregateId);
        m_hzInstance = hzInstance;
        m_storage = m_hzInstance.getMap(storageId);
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
    public void add(DomainEventMessage<T> message) {
        m_storage.put(new HzDomainEventKey<>(message),new HzDomainEventMessage<>(message));
    }

    @Override
    public DomainEventStream getEventStream() {
        return new HzDomainEventStream<>(m_storage);
    }
}
