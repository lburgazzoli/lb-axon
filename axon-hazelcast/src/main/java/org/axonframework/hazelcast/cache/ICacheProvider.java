package org.axonframework.hazelcast.cache;

import net.sf.jsr107cache.Cache;
import org.axonframework.eventsourcing.EventSourcedAggregateRoot;

/**
 *
 */
public interface ICacheProvider {
    /**
     *
     * @param aggregateType
     * @return
     */
    public Cache getCache(Class<? extends EventSourcedAggregateRoot> aggregateType);
}
