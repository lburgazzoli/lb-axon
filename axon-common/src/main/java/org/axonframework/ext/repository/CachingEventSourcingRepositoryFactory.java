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
package org.axonframework.ext.repository;

import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventsourcing.*;
import org.axonframework.eventstore.EventStore;
import org.axonframework.repository.Repository;

import javax.cache.Cache;

/**
 *
 */
public class CachingEventSourcingRepositoryFactory implements IRepositoryFactory {

    private final EventBus m_evtBus;
    private final EventStore m_evtStore;
    private final Cache m_cache;

    /**
     * c-tor
     *
     * @param evtStore
     * @param evtBus
     */
    public CachingEventSourcingRepositoryFactory(final Cache cache, final EventStore evtStore, final EventBus evtBus) {
        m_evtBus = evtBus;
        m_evtStore = evtStore;
        m_cache = cache;
    }

    @Override
    public <T extends EventSourcedAggregateRoot> Repository<T> createRepository(Class<T> aggregateType) {
        AggregateFactory<T> af = new GenericAggregateFactory<>(aggregateType);

        CachingEventSourcingRepository<T> repo = new CachingEventSourcingRepository<>(af,m_evtStore);
        repo.setEventBus(m_evtBus);
        repo.setCache(m_cache);

        return repo;
    }
}
