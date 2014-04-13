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
import com.hazelcast.core.HazelcastInstance;
import org.axonframework.domain.DomainEventMessage;
import org.axonframework.domain.DomainEventStream;
import org.axonframework.ext.eventstore.AbstractEventStore;
import org.axonframework.ext.eventstore.CloseableDomainEventStore;
import org.axonframework.ext.eventstore.NullDomainEventStream;

import java.io.IOException;
import java.util.Map;

/**
 *
 */
public class HzEventStore<T> extends AbstractEventStore {
    private final HazelcastInstance m_hzInstance;
    private final Map<String,CloseableDomainEventStore<T>> m_domainEventStore;

    /**
     * c-tor
     *
     * @param hzInstance the Hazelcast instance
     */
    public HzEventStore(final HazelcastInstance hzInstance) {
        m_hzInstance = hzInstance;
        m_domainEventStore = Maps.newConcurrentMap();
    }


    // *************************************************************************
    // Closeable
    // *************************************************************************

    @Override
    public void close() throws IOException {
    }

    // *************************************************************************
    // EventStore
    // *************************************************************************

    @SuppressWarnings("unchecked")
    @Override
    public void appendEvents(String type, DomainEventStream events) {
        int size = 0;

        String storageId = null;
        CloseableDomainEventStore des = null;

        while(events.hasNext()) {
            DomainEventMessage<T> dem = events.next();
            if(size == 0) {
                storageId = HzEventStoreUtil.getStorageIdentifier(type, dem);
                des = m_domainEventStore.get(storageId);

                if(des == null) {
                    des = new HzDomainEventStore<T>(
                        storageId,type,dem.getAggregateIdentifier().toString(),m_hzInstance);

                    m_domainEventStore.put(des.getStorageId(),des);
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
    }

    @Override
    public DomainEventStream readEvents(String type, Object identifier) {
        String mapId = HzEventStoreUtil.getStorageIdentifier(type, identifier.toString());
        CloseableDomainEventStore<T> hdes = m_domainEventStore.get(mapId);

        if(hdes != null) {
            return hdes.getEventStream();
        }

        return NullDomainEventStream.INSTANCE;
    }
}
