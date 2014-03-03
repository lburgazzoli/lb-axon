/*
 * Copyright (c) 2010-2014. Axon Framework
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
package org.axonframework.ext.hazelcast.distributed.commandbus.executor;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.ext.hazelcast.distributed.IHzAxonEngine;
import org.axonframework.ext.hazelcast.distributed.commandbus.HzCommand;
import org.axonframework.ext.hazelcast.distributed.commandbus.HzCommandReply;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;

/**
 *
 */
public class HzCommandTask extends HzCommand implements HazelcastInstanceAware, Callable<HzCommandReply> {
    private HazelcastInstance m_instance;
    private HzCommand m_command;

    /**
     * c-tor
     */
    public HzCommandTask() {
        this(null);
    }

    /**
     *
     * @param command
     */
    public HzCommandTask(HzCommand command) {
        m_instance = null;
        m_command = command;
    }

    // *************************************************************************
    // HazelcastInstanceAware
    // *************************************************************************

    @Override
    public void setHazelcastInstance(HazelcastInstance instance) {
        m_instance = instance;
    }

    // *************************************************************************
    // Callable<HzCommandReply>
    // *************************************************************************

    @Override
    public HzCommandReply call() throws Exception {
        Map<String, Object> ctx = m_instance.getUserContext();
        IHzAxonEngine engine = (IHzAxonEngine)ctx.get(HzCommandConstants.USER_CONTEXT_NAME);

        if(m_command.isCallbackRequired()) {
        } else {
        }

        return new HzCommandReply(
            m_command.getMessage().getIdentifier(),
            "<empty>");
    }
}
