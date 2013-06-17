package org.axonframework.hazelcast.eventhandling.impl;

import org.axonframework.domain.EventMessage;
import org.axonframework.hazelcast.IHazelcastInstanceProxy;

/**
 *
 */
public class StaticPublisher extends AbstractPublisher {
    private final String m_topicName;

    /**
     *
     * @param topicName
     */
    public StaticPublisher(IHazelcastInstanceProxy proxy,String topicName) {
        super(proxy);
        m_topicName = topicName;
    }

    @Override
    protected String resolve(EventMessage event) {
        return m_topicName;
    }
}
