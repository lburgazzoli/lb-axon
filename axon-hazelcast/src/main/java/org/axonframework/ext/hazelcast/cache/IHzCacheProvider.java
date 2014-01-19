package org.axonframework.ext.hazelcast.cache;

import net.sf.jsr107cache.Cache;
import org.axonframework.eventsourcing.EventSourcedAggregateRoot;

/**
 *
 */
public interface IHzCacheProvider {
    /**
     *
     * @param aggregateType
     * @return
     */
    public Cache getCache(Class<? extends EventSourcedAggregateRoot> aggregateType);
}
