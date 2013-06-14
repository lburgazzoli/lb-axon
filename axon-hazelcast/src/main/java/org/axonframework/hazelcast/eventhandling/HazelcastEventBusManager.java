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
package org.axonframework.hazelcast.eventhandling;

import com.hazelcast.core.ITopic;
import com.hazelcast.core.MessageListener;
import org.apache.commons.lang3.StringUtils;
import org.axonframework.domain.EventMessage;
import org.axonframework.hazelcast.IHazelcastManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class HazelcastEventBusManager {
    private final static Logger LOGGER = LoggerFactory.getLogger(HazelcastEventBusManager.class);

    private final IHazelcastManager m_hazelcastManager;

    /**
     * c-tor
     *
     * @param hazelcastManager
     */
    public HazelcastEventBusManager(IHazelcastManager hazelcastManager) {
        m_hazelcastManager = hazelcastManager;
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     *
     * @return
     */
    public IHazelcastManager getManager() {
        return m_hazelcastManager;
    }

    /**
     *
     * @param event
     * @return
     */
    public void publish(String topicName,EventMessage event) {
        String tn = event.getPayloadType().getName();
        if(StringUtils.isNotBlank(topicName)) {
            tn = topicName + ":" + tn;
        }

        LOGGER.debug("TopicName: <{}>",tn);
        getTopic(tn).publish(event);
    }

    /**
     *
     * @param topicName
     * @param listener
     */
    public void subscribe(String topicName,MessageListener<EventMessage> listener) {
        getTopic(topicName).addMessageListener(listener);
    }

    /**
     *
     * @param topicName
     * @param listener
     */
    public void unsubscribe(String topicName,MessageListener<EventMessage> listener) {
        getTopic(topicName).removeMessageListener(listener);
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     *
     * @param topicName
     * @return
     */
    private ITopic<EventMessage> getTopic(String topicName) {
        return m_hazelcastManager.getTopic(topicName);
    }
}
