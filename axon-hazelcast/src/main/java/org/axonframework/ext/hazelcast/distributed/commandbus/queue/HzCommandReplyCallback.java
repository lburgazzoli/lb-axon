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
package org.axonframework.ext.hazelcast.distributed.commandbus.queue;

import com.hazelcast.core.HazelcastInstance;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.ext.hazelcast.distributed.commandbus.HzCommand;
import org.axonframework.ext.hazelcast.distributed.commandbus.HzCommandReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class HzCommandReplyCallback<T> implements CommandCallback<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HzCommandReplyCallback.class);

    private final HazelcastInstance m_hzInstance;
    private final HzCommandBusAgent m_agent;
    private final HzCommand m_command;

    /**
     * c-tor
     *
     * @param hzInstance  the Hazelcast instance
     * @param agent       the Agent
     * @param command     the command
     */
    public HzCommandReplyCallback(HazelcastInstance hzInstance,HzCommandBusAgent agent,HzCommand command) {
        m_hzInstance = hzInstance;
        m_agent      = agent;
        m_command    = command;
    }

    @Override
    public void onSuccess(Object result) {
        try {
            m_hzInstance.getQueue(getSourceNodeId()).put(
                newReply(
                    m_command.getMessage().getIdentifier(),
                    result)
            );
        } catch(Exception e) {
            LOGGER.warn("Exception",e);
        }
    }
    @Override
    public void onFailure(Throwable cause) {
        try {
            m_hzInstance.getQueue(getSourceNodeId()).put(
                newReply(
                    m_command.getMessage().getIdentifier(),
                    cause)
            );
        } catch(Exception e) {
            LOGGER.warn("Exception",e);
        }
    }

    // *************************************************************************
    // Helpers
    // *************************************************************************

    /**
     *
     * @return
     */
    private String getSourceNodeId() {
        return m_command.getNodeName();
    }

    /**
     *
     * @param id
     * @param data
     * @return
     */
    private HzCommandReply newReply(String id, Object data) {
        return new HzCommandReply(m_agent.getNodeName(),id,data);
    }
}
