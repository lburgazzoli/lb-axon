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
