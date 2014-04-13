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

import org.axonframework.ext.hazelcast.distributed.commandbus.HzCommand;
import org.axonframework.ext.hazelcast.distributed.commandbus.HzCommandReply;

/**
 *
 */
public class HzCommandTask extends HzTask<HzCommandReply> {
    private HzCommand m_command;

    /**
     * c-tor
     */
    public HzCommandTask() {
        this(null);
    }

    /**
     * c-tor
     *
     * @param command the command
     */
    public HzCommandTask(HzCommand command) {
        m_command = command;
    }

    // *************************************************************************
    // Callable<HzCommandReply>
    // *************************************************************************

    @Override
    public HzCommandReply call() throws Exception {
        return dispatcher().dispatch(m_command).get();
    }
}
