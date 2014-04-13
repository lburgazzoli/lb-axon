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
package org.axonframework.ext.eventstore;

import java.io.IOException;

/**
 *
 */
public abstract class AbstractDomainEventStore<T> implements CloseableDomainEventStore<T> {
    private final String m_aggregateType;
    private final String m_aggregateId;
    private final String m_storageId;

    /**
     * c-tor
     *
     * @param storageId      the storage id
     * @param aggregateType  the aggregate type
     * @param aggregateId    the aggregate id
     */
    public AbstractDomainEventStore(String storageId, String aggregateType, String aggregateId) {
        m_aggregateType = aggregateType;
        m_aggregateId = aggregateId;
        m_storageId = storageId;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public void clear() {
    }

    @Override
    public String getAggregateType() {
        return m_aggregateType;
    }

    @Override
    public String getAggregateId() {
        return m_aggregateId;
    }

    @Override
    public String getStorageId() {
        return m_storageId;
    }
}
