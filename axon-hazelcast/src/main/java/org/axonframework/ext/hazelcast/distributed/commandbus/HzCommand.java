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
package org.axonframework.ext.hazelcast.distributed.commandbus;

import org.axonframework.commandhandling.CommandMessage;

/**
 *
 */
public class HzCommand extends HzCommandCommon {
    private boolean m_callback;
    private final CommandMessage<?> m_message;

    /**
     * c-tor
     *
     * @param nodeName the node name
     */
    public HzCommand(String nodeName) {
        this(nodeName,null,false);
    }

    /**
     * c-tor
     *
     * @param nodeName the node name
     * @param message  the command message
     */
    public HzCommand(String nodeName, CommandMessage<?> message) {
        this(nodeName,message,false);
    }

    /**
     * c-tor
     *
     * @param nodeName  the node name
     * @param message   the comman message
     * @param callback  the callback
     */
    public HzCommand(String nodeName, CommandMessage<?> message, boolean callback) {
        super(nodeName);

        m_callback   = callback;
        m_message    = message;
    }

    /**
     *
     * @return the message
     */
    public CommandMessage<?> getMessage() {
        return m_message;
    }

    /**
     *
     * @return true if a callback is required
     */
    public boolean isCallbackRequired() {
        return m_callback;
    }
}
