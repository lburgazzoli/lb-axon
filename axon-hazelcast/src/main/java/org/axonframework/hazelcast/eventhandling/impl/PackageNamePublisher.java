package org.axonframework.hazelcast.eventhandling.impl;

import org.axonframework.domain.EventMessage;
import org.axonframework.hazelcast.IHazelcastInstanceProxy;

/**
 *
 */
public class PackageNamePublisher extends AbstractPublisher {
    /**
     *
     * @param proxy
     */
    public PackageNamePublisher(IHazelcastInstanceProxy proxy) {
        super(proxy);
    }

    @Override
    protected String resolve(EventMessage event) {
        return event.getPayloadType().getPackage().getName();
    }
}
