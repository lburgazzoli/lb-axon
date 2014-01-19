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

import org.axonframework.domain.DomainEventMessage;
import org.axonframework.domain.DomainEventStream;

import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeSet;

/**
 *
 */
public class HzDomainEventStream implements DomainEventStream {

    private int m_nextIndex;
    private final Long[] m_keys;
    private final Map<Long,HzDomainEventMessage> m_data;

    /**
     * c-tor
     *
     * @param data
     */
    public HzDomainEventStream(Map<Long,HzDomainEventMessage> data) {
        m_data      = data;
        m_keys      = new TreeSet<Long>(m_data.keySet()).toArray(new Long[]{});
        m_nextIndex = 0;

        Arrays.sort(m_keys);
    }

    @Override
    public boolean hasNext() {
        return m_keys.length > m_nextIndex;
    }

    @Override
    public DomainEventMessage next() {
        if (!hasNext()) {
            throw new NoSuchElementException("Trying to peek beyond the limits of this stream.");
        }

        return m_data.get(m_keys[m_nextIndex++]);
    }

    @Override
    public DomainEventMessage peek() {
        if (!hasNext()) {
            throw new NoSuchElementException("Trying to peek beyond the limits of this stream.");
        }

        return m_data.get(m_keys[m_nextIndex]);
    }
}
