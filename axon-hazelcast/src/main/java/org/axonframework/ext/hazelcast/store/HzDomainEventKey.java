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
package org.axonframework.ext.hazelcast.store;

import com.hazelcast.core.PartitionAware;
import org.axonframework.domain.DomainEventMessage;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 */
public class HzDomainEventKey<T> implements Serializable, PartitionAware, Comparable<HzDomainEventKey<T>> {
    private final long m_sequence;
    private final Object m_key;

    /**
     * c-tor
     *
     * @param message
     */
    public HzDomainEventKey(DomainEventMessage<T> message) {
        m_sequence = message.getSequenceNumber();
        m_key = message.getAggregateIdentifier();
    }

    // *************************************************************************
    //
    // *************************************************************************

    public long getSequence() {
        return m_sequence;
    }

    @Override
    public Object getPartitionKey() {
        return m_key;
    }

    @Override
    public int compareTo(HzDomainEventKey<T> o) {
        assert(Objects.equals(m_key,o.m_key));

        return m_sequence < o.m_sequence
             ? -1
             : m_sequence > o.m_sequence
                 ? +1
                 : 0;
    }
}
