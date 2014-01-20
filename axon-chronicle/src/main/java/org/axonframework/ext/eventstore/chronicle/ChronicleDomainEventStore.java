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
package org.axonframework.ext.eventstore.chronicle;


import net.openhft.chronicle.IndexedChronicle;
import org.axonframework.domain.DomainEventMessage;
import org.axonframework.domain.DomainEventStream;
import org.axonframework.ext.eventstore.AbstractDomainEventStore;
import org.axonframework.ext.eventstore.CloseableDomainEventStore;
import org.axonframework.serializer.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/**
 *
 */
public class ChronicleDomainEventStore extends AbstractDomainEventStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChronicleDomainEventStore.class);

    private final String m_basePath;
    private final Serializer m_serializer;

    private IndexedChronicle m_chronicle;
    private ChronicleDomainEventWriter m_writer;

    /**
     * c-tor
     *
     * @param basePath
     * @param storageId
     * @param aggregateType
     * @param aggregateId
     */
    public ChronicleDomainEventStore(Serializer serializer,String basePath,String storageId, String aggregateType, String aggregateId) {
        super(storageId,aggregateType,aggregateId);
        m_serializer = serializer;
        m_basePath = basePath;

        try {
            String dataPath = new File(m_basePath,storageId).getAbsolutePath();
            LOGGER.debug("IndexedChronicle => BasePath: {}, DataPath: {}",basePath,dataPath);

            m_chronicle = new IndexedChronicle(dataPath);
            if(m_chronicle != null) {
                m_writer = new ChronicleDomainEventWriter(m_chronicle,m_serializer);
            }
        } catch(Exception e) {
            //TODO: check what to do
            m_chronicle = null;
            LOGGER.warn("Exception",e);
        }
    }

    @Override
    public void clear() {
        //TODO: implements
    }

    @Override
    public void close() throws IOException {
        if(m_chronicle != null) {
            m_chronicle.close();
        }
    }

    @Override
    public long getStorageSize() {
        return m_chronicle != null ? m_chronicle.size() : 0;
    }

    @Override
    public void add(DomainEventMessage message) {
        if(m_writer != null) {
            m_writer.write(message);
        }
    }

    @Override
    public DomainEventStream getEventStream() {
        return m_chronicle != null
             ? new ChronicleDomainEventReader(m_chronicle,m_serializer)
             : null;
    }
}
