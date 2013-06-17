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
package org.axonframework.hazelcast.samples.helper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.axonframework.domain.DomainEventMessage;
import org.axonframework.domain.DomainEventStream;
import org.axonframework.domain.SimpleDomainEventStream;
import org.axonframework.eventstore.EventStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class MemoryEventStore implements EventStore{

    private Map<String,Map<Object,List<DomainEventMessage>>> m_storage;

    /**
     *
     */
    public MemoryEventStore() {
        m_storage = Maps.newHashMap();
    }

    @Override
    public void appendEvents(String type, DomainEventStream events) {
        Map<Object,List<DomainEventMessage>> typeStorage = m_storage.get(type);
        if(typeStorage == null) {
            typeStorage = Maps.newHashMap();
            m_storage.put(type,typeStorage);
        }

        if(events.hasNext()) {
            DomainEventMessage       message     = events.next();
            Object                   aggregateId = message.getAggregateIdentifier();
            List<DomainEventMessage> messages    = typeStorage.get(aggregateId);

            if(message.getSequenceNumber() == 0 || messages == null) {
                messages = Lists.newLinkedList();
                typeStorage.put(aggregateId,messages);
            }

            messages.add(message);
            while(events.hasNext()) {
                messages.add(events.next());
            }
        }
    }

    @Override
    public DomainEventStream readEvents(String type, Object aggregateId) {
        DomainEventStream eventStream = null;
        Map<Object,List<DomainEventMessage>> typeStorage = m_storage.get(type);

        if(typeStorage != null) {
            List<DomainEventMessage> messages = typeStorage.get(aggregateId);
            if(messages != null) {
                eventStream = new SimpleDomainEventStream(messages);
            }
        }

        return eventStream != null
             ? eventStream
             : new SimpleDomainEventStream(new ArrayList<DomainEventMessage>(0));
    }
}
