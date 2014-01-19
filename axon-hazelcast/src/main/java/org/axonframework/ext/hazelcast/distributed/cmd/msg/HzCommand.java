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
package org.axonframework.ext.hazelcast.distributed.cmd.msg;

import org.axonframework.commandhandling.CommandMessage;

/**
 *
 */
public class HzCommand extends HzMessage {
    private String m_sourceNodeId;
    private boolean m_callback;
    private CommandMessage<?> m_message;

    /**
     * c-tor
     */
    public HzCommand() {
        this(null,null,false);
    }

    /**
     * c-tor
     *
     * @param nodeId
     * @param message
     */
    public HzCommand(String nodeId,CommandMessage<?> message) {
        this(nodeId,message,false);
    }

    /**
     * c-tor
     *
     * @param sourceNodeId
     * @param message
     * @param callback
     */
    public HzCommand(String sourceNodeId,CommandMessage<?> message, boolean callback) {
        m_sourceNodeId = sourceNodeId;
        m_callback = callback;
        m_message  = message;
    }

    /**
     *
     * @return
     */
    public CommandMessage<?> getMessage() {
        return m_message;
    }

    /**
     *
     * @return
     */
    public boolean isCallbackRequired() {
        return m_callback;
    }

    /**
     *
     * @return
     */
    public String getSourceNodeId() {
        return m_sourceNodeId;
    }
}
