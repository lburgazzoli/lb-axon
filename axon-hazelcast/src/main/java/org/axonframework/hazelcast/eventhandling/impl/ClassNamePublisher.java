package org.axonframework.hazelcast.eventhandling.impl;

import org.axonframework.domain.EventMessage;
import org.axonframework.hazelcast.IHazelcastInstanceProxy;

/**
 *
 */
public class ClassNamePublisher extends AbstractPublisher {
    /**
     *
     * @param proxy
     */
    public ClassNamePublisher(IHazelcastInstanceProxy proxy) {
        super(proxy);
    }

    @Override
    protected String resolve(EventMessage event) {
        return event.getPayloadType().getName();
    }
}
