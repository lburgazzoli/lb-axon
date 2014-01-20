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
import org.axonframework.eventstore.EventStore;
import org.axonframework.ext.eventstore.NullDomainEventStream;
import org.axonframework.serializer.Serializer;
import org.axonframework.serializer.xml.XStreamSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

/**
 *
 */
public class ChronicleEventStore implements EventStore  {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChronicleEventStore.class);

    private final Serializer m_serializer;
    private final String m_basePath;
    private final Map<String,ChronicleDomainEventStore> m_domainEventStore;

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
    public void appendEvents(String type, DomainEventStream events) {
        int size = 0;

        String mapId = null;
        ChronicleDomainEventStore hdes = null;

        while(events.hasNext()) {
            DomainEventMessage dem = events.next();
            if(size == 0) {
                mapId = ChronicleEventStoreUtil.getStorageIdentifier(type, dem);
                hdes  = m_domainEventStore.get(mapId);

                if(hdes == null) {
                    // create a new DomainEventStore
                    hdes = new ChronicleDomainEventStore(
                        m_serializer,
                        m_basePath,
                        mapId,
                        type,
                        dem.getAggregateIdentifier().toString()
                    );

                    m_domainEventStore.put(mapId,hdes);
                }

                if(dem.getSequenceNumber() == 0) {
                    hdes.clear();
                }
            }

            if(hdes != null) {
                hdes.add(dem);
                size++;
            }
        }

        LOGGER.debug("appendEvents: type={}, nbStoredEvents={}, eventStoreSize={}",
            type,size,(hdes != null) ? hdes.getStorageSize() : 0);
    }

    @Override
    public DomainEventStream readEvents(String type, Object identifier) {
        String mapId = ChronicleEventStoreUtil.getStorageIdentifier(type, identifier.toString());
        ChronicleDomainEventStore hdes = m_domainEventStore.get(mapId);

        if(hdes != null) {
            return hdes.getEventStream();
        }

        return NullDomainEventStream.INSTANCE;
    }
}
