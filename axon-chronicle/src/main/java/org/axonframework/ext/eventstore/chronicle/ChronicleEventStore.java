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


import com.google.common.collect.Maps;
import net.openhft.chronicle.tools.ChronicleTools;
import org.axonframework.domain.DomainEventMessage;
import org.axonframework.domain.DomainEventStream;
import org.axonframework.ext.eventstore.AbstractEventStore;
import org.axonframework.ext.eventstore.CloseableDomainEventStore;
import org.axonframework.ext.eventstore.NullDomainEventStream;
import org.axonframework.serializer.Serializer;
import org.axonframework.serializer.xml.XStreamSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 *
 */
public class ChronicleEventStore extends AbstractEventStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChronicleEventStore.class);

    private final Serializer m_serializer;
    private final String m_basePath;
    private final Map<String,CloseableDomainEventStore> m_domainEventStore;

    // *************************************************************************
    //
    // *************************************************************************

    /**
     * c-tor
     *
     * @param basePath
     */
    public ChronicleEventStore(String basePath) {
        this(basePath,new XStreamSerializer());
    }

    /**
     * c-tor
     *
     * @param basePath
     * @param serializer
     */
    public ChronicleEventStore(String basePath,Serializer serializer) {
        m_basePath = basePath;
        m_serializer = serializer;
        m_domainEventStore = Maps.newConcurrentMap();

        ChronicleTools.warmup();
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public void close() throws IOException {
        for(CloseableDomainEventStore des : m_domainEventStore.values()) {
            des.close();
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public void appendEvents(String type, DomainEventStream events) {
        int size = 0;

        String storageId = null;
        CloseableDomainEventStore des = null;

        while(events.hasNext()) {
            DomainEventMessage dem = events.next();
            if(size == 0) {
                storageId = ChronicleEventStoreUtil.getStorageIdentifier(type, dem);
                des  = m_domainEventStore.get(storageId);

                if(des == null) {
                    // create a new DomainEventStore
                    des = new ChronicleDomainEventStore(
                        m_serializer,
                        m_basePath,
                        storageId,
                        type,
                        dem.getAggregateIdentifier().toString()
                    );

                    m_domainEventStore.put(storageId,des);
                }

                if(dem.getSequenceNumber() == 0) {
                    des.clear();
                }
            }

            if(des != null) {
                des.add(dem);
                size++;
            }
        }

        LOGGER.debug("appendEvents: type={}, nbStoredEvents={}, eventStoreSize={}",
            type,size,(des != null) ? des.getStorageSize() : 0);
    }

    @Override
    public DomainEventStream readEvents(String type, Object identifier) {
        String mapId = ChronicleEventStoreUtil.getStorageIdentifier(type, identifier.toString());
        CloseableDomainEventStore hdes = m_domainEventStore.get(mapId);

        if(hdes != null) {
            return hdes.getEventStream();
        }

        return NullDomainEventStream.INSTANCE;
    }
}
