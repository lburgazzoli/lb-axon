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

import com.google.common.collect.Maps;
import org.axonframework.domain.DomainEventMessage;
import org.axonframework.domain.DomainEventStream;
import org.axonframework.eventstore.EventStore;
import org.axonframework.ext.eventstore.NullDomainEventStream;
import org.axonframework.ext.hazelcast.IHzProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 *
 */
public class HzEventStore implements EventStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(HzEventStore.class);

    private final IHzProxy m_hazelcastManager;
    private final Map<String,HzDomainEventStore> m_domainEventStore;

    /**
     * c-tor
     *
     * @param hazelcastManager
     */
    public HzEventStore(IHzProxy hazelcastManager) {
        m_hazelcastManager = hazelcastManager;
        m_domainEventStore = Maps.newHashMap();
    }

    // *************************************************************************
    // EventStore
    // *************************************************************************

    @Override
    public void appendEvents(String type, DomainEventStream events) {
        int size = 0;

        String mapId = null;
        HzDomainEventStore hdes = null;

        while(events.hasNext()) {
            DomainEventMessage dem = events.next();
            if(size == 0) {
                mapId = HzEventStoreUtil.getStorageIdentifier(type, dem);
                hdes  = m_domainEventStore.get(mapId);

                if(hdes == null) {
                    hdes = new HzDomainEventStore(
                        mapId,type,dem.getAggregateIdentifier().toString(),m_hazelcastManager);

                    m_domainEventStore.put(hdes.getStorageId(),hdes);
                }

                if(dem.getSequenceNumber() == 0) {
                    hdes.clear();
                }
            }

            if(hdes != null) {
                LOGGER.debug("Add : <{}>",dem.getPayload().toString());
                hdes.add(dem);
                size++;
            }
        }

        LOGGER.debug("appendEvents: type={}, nbStoredEvents={}, eventStoreSize={}",
            type,size,(hdes != null) ? hdes.getStorageSize() : 0);
    }

    @Override
    public DomainEventStream readEvents(String type, Object identifier) {
        String mapId = HzEventStoreUtil.getStorageIdentifier(type, identifier.toString());
        HzDomainEventStore hdes = m_domainEventStore.get(mapId);

        if(hdes != null) {
            return hdes.getEventStream();
        }

        return  NullDomainEventStream.INSTANCE;
    }
}
