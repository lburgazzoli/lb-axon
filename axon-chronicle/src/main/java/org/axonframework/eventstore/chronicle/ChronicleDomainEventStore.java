/*
 * Copyright (c) 2010-2014. Axon Framework
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
package org.axonframework.eventstore.chronicle;


import org.axonframework.domain.DomainEventMessage;
import org.axonframework.domain.DomainEventStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ChronicleDomainEventStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChronicleDomainEventStore.class);

    private final String m_storageId;
    private final String m_aggregateType;
    private final String m_aggregateId;

    /**
     * c-tor
     *
     * @param storageId
     * @param aggregateType
     * @param aggregateId
     */
    public ChronicleDomainEventStore(String storageId, String aggregateType, String aggregateId) {
        m_storageId = storageId;
        m_aggregateType = aggregateType;
        m_aggregateId = aggregateId;
    }

    /**
     * clean the stored events
     */
    public void clear() {
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
        return 0;
    }

    /**
     *
     * @param message
     */
    @SuppressWarnings("unchecked")
    public void add(DomainEventMessage message) {
        //m_storage.put(message.getSequenceNumber(),new HzDomainEventMessage(message));
    }

    /**
     *
     * @return the event stream
     */
    public DomainEventStream getEventStream() {
        return new ChronicleDomainEventStream();
    }
}
