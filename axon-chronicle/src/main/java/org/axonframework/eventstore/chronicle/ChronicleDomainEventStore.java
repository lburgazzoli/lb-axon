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


import net.openhft.chronicle.IndexedChronicle;
import org.axonframework.domain.DomainEventMessage;
import org.axonframework.domain.DomainEventStream;
import org.axonframework.serializer.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ChronicleDomainEventStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChronicleDomainEventStore.class);

    private final String m_basePath;
    private final boolean m_deleteOnExit;
    private final String m_storageId;
    private final String m_aggregateType;
    private final String m_aggregateId;
    private final Serializer m_serializer;

    private IndexedChronicle m_chronicle;
    private ChronicleDomainEventWriter m_writer;

    /**
     * c-tor
     *
     * @param basePath
     * @param deleteOnExit
     * @param storageId
     * @param aggregateType
     * @param aggregateId
     */
    public ChronicleDomainEventStore(Serializer serializer,String basePath,boolean deleteOnExit,String storageId, String aggregateType, String aggregateId) {
        m_serializer = serializer;
        m_basePath = basePath;
        m_deleteOnExit = deleteOnExit;
        m_storageId = storageId;
        m_aggregateType = aggregateType;
        m_aggregateId = aggregateId;

        try {
            m_chronicle = new IndexedChronicle(basePath);
            if(m_chronicle != null) {
                m_writer = new ChronicleDomainEventWriter(m_chronicle,m_serializer);
            }
        } catch(Exception e) {
            //TODO: check what to do
            m_chronicle = null;
            LOGGER.warn("Exception",e);
        }
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
    public long getStorageSize() {
        return m_chronicle != null ? m_chronicle.size() : 0;
    }

    /**
     *
     * @param message
     */
    public void add(DomainEventMessage message) {
        if(m_writer != null) {
            m_writer.write(message);
        }
    }

    /**
     *
     * @return the event stream
     */
    public DomainEventStream getEventStream() {
        return m_chronicle != null
             ? new ChronicleDomainEventReader(m_chronicle,m_serializer)
             : null;
    }
}
