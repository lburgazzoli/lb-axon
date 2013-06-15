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
package org.axonframework.hazelcast.cache;

import net.sf.jsr107cache.Cache;
import org.axonframework.eventsourcing.EventSourcedAggregateRoot;
import org.axonframework.hazelcast.IHazelcastInstanceProxy;

/**
 *
 */
public class HazelcastCacheProvider implements ICacheProvider {
    private final IHazelcastInstanceProxy m_hazelcastManager;

    /**
     *
     * @param hazelcastManager
     */
    public HazelcastCacheProvider(IHazelcastInstanceProxy hazelcastManager) {
        m_hazelcastManager = hazelcastManager;
    }

    @Override
    public Cache getCache(Class<? extends EventSourcedAggregateRoot> aggregateType) {
        return new HazelcastCache(
            m_hazelcastManager,
            "axon-cache-<" + aggregateType.getName() + ">");
    }
}
