package org.axonframework.hazelcast.eventhandling.impl;

import com.hazelcast.core.ITopic;
import org.axonframework.domain.EventMessage;
import org.axonframework.hazelcast.IHazelcastInstanceProxy;
import org.axonframework.hazelcast.eventhandling.IHazelcastTopicPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public abstract class AbstractPublisher implements IHazelcastTopicPublisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPublisher.class);
    private final IHazelcastInstanceProxy m_proxy;

    /**
     * c-tor
     */
    public AbstractPublisher(IHazelcastInstanceProxy proxy) {
        m_proxy = proxy;
    }

    /**
     *
     * @param event
     */
    public void publish(EventMessage event) {
        if(m_proxy != null) {
            String               topicName = resolve(event);
            ITopic<EventMessage> topic     = m_proxy.getTopic(topicName);

            LOGGER.debug("Publish event <{}> to {}",event.getIdentifier(),topic.getName());
            topic.publish(event);
        }
    }

    protected abstract String resolve(EventMessage event);
}
