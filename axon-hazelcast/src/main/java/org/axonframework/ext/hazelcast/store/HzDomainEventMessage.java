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

import com.hazelcast.core.PartitionAware;
import org.axonframework.domain.DomainEventMessage;
import org.axonframework.domain.MetaData;
import org.joda.time.DateTime;

import java.util.Map;

/**
 *
 */
public class HzDomainEventMessage<T> implements DomainEventMessage<T>, PartitionAware {

    private long m_sequenceNumber;
    private Object m_aggregateIdentifier;
    private String m_identifier;
    private MetaData m_metaData;
    private T m_payload;
    private Class m_payloadType;
    private DateTime m_timestamp;

    /**
     *
     */
    public HzDomainEventMessage() {
        m_sequenceNumber = 0;
        m_aggregateIdentifier = null;
        m_identifier = null;
        m_metaData = null;
        m_payload = null;
        m_payloadType = null;
        m_timestamp = null;
    }

    /**
     *
     * @param domainEventMessage
     */
    public HzDomainEventMessage(DomainEventMessage<T> domainEventMessage) {
        m_sequenceNumber = domainEventMessage.getSequenceNumber();
        m_aggregateIdentifier = domainEventMessage.getAggregateIdentifier();
        m_identifier = domainEventMessage.getIdentifier();
        m_metaData = domainEventMessage.getMetaData();
        m_payload = domainEventMessage.getPayload();
        m_payloadType = domainEventMessage.getPayloadType();
        m_timestamp = domainEventMessage.getTimestamp();
    }

    // *************************************************************************
    //
    // *************************************************************************

    public void setTimestamp(DateTime timestamp) {
        m_timestamp = timestamp;
    }

    public void setSequenceNumber(long sequenceNumber) {
        m_sequenceNumber = sequenceNumber;
    }

    public void setAggregateIdentifier(Object aggregateIdentifier) {
        m_aggregateIdentifier = aggregateIdentifier;
    }

    public void setIdentifier(String identifier) {
        m_identifier = identifier;
    }

    public void setMetaData(MetaData metaData) {
        m_metaData = metaData;
    }

    public void setPayload(T payload) {
        m_payload = payload;
    }

    public void setPayloadType(Class payloadType) {
        m_payloadType = payloadType;
    }


    @Override
    public long getSequenceNumber() {
        return m_sequenceNumber;
    }

    @Override
    public Object getAggregateIdentifier() {
        return m_aggregateIdentifier;
    }

    @Override
    public String getIdentifier() {
        return m_identifier;
    }

    @Override
    public MetaData getMetaData() {
        return m_metaData;
    }

    @Override
    public T getPayload() {
        return m_payload;
    }

    @Override
    public Class getPayloadType() {
        return m_payloadType;
    }

    @Override
    public DateTime getTimestamp() {
        return m_timestamp;
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    @SuppressWarnings("unchecked")
    public DomainEventMessage<T> withMetaData(Map<String, ?> metaData) {
        HzDomainEventMessage dem = new HzDomainEventMessage(this);
        dem.setMetaData(new MetaData(metaData));

        return dem;
    }

    @Override
    @SuppressWarnings("unchecked")
    public DomainEventMessage<T> andMetaData(Map<String, ?> metaData) {
        HzDomainEventMessage dem = new HzDomainEventMessage(this);
        dem.getMetaData().mergedWith(metaData);

        return dem;
    }

    // *************************************************************************
    // PartitionAware
    // *************************************************************************

    @Override
    public Object getPartitionKey() {
        return m_aggregateIdentifier;
    }
}
