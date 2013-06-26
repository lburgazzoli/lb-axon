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

import com.google.common.collect.Lists;
import com.hazelcast.core.IList;
import org.axonframework.domain.DomainEventMessage;
import org.axonframework.domain.DomainEventStream;
import org.axonframework.domain.SimpleDomainEventStream;
import org.axonframework.hazelcast.IHzProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 *
 */
public class HzDomainEventStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(HzDomainEventStore.class);

    private final String m_aggregateType;
    private final String m_aggregateId;
    private final String m_storageId;
    private final IHzProxy m_hazelcastManager;
    private final IList<HzDomainEventMessage> m_storage;

    /**
     *
     * @param aggregateType
     * @param aggregateId
     * @param hazelcastManager
     */
    public HzDomainEventStore(String aggregateType, String aggregateId, IHzProxy hazelcastManager) {
        m_aggregateType = aggregateType;
        m_aggregateId = aggregateId;
        m_storageId = HzStorageUtils.getStorageIdentifier(m_aggregateType, m_aggregateId);
        m_hazelcastManager = hazelcastManager;
        m_storage = m_hazelcastManager.getList(m_storageId);
    }

    /**
     *
     */
    public void clear() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClassLoader());
            m_storage.clear();
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    /**
     *
     * @return
     */
    public ClassLoader getClassLoader() {
        return m_hazelcastManager.getClassloader();
    }

    /**
     *
     * @return
     */
    public String getAggregateType() {
        return m_aggregateType;
    }

    /**
     *
     * @return
     */
    public String getAggregateId() {
        return m_aggregateId;
    }

    /**
     *
     * @return
     */
    public String getStorageId() {
        return m_storageId;
    }

    /**
     *
     * @return
     */
    public IList<HzDomainEventMessage> getStorage() {
        return m_storage;
    }

    /**
     *
     * @return
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
        m_storage.add(new HzDomainEventMessage(message));
    }

    /**
     *
     * @return
     */
    public DomainEventStream getEventStream() {
        DomainEventStream des = null;
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        try {
            Thread.currentThread().setContextClassLoader(getClassLoader());

            List<DomainEventMessage> messages =
                Lists.newArrayListWithCapacity(m_storage.size());

            des = new SimpleDomainEventStream(m_storage);
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }

        return des;
    }
}
