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
import org.axonframework.ext.hazelcast.HzConstants;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 *
 */
public abstract class HzTask<T> implements HazelcastInstanceAware, Serializable, Callable<T> {
    private transient HazelcastInstance m_instance;

    /**
     * c-tor
     */
    public HzTask() {
        m_instance = null;
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

    /**
     *
     * @return the Hazelcast instance
     */
    protected HazelcastInstance instance() {
        return m_instance;
    }

    /**
     *
     * @return the task dispatcher
     */
    protected HzTaskDispatcher dispatcher() {
        Map<String, Object> ctx = m_instance.getUserContext();
        return (HzTaskDispatcher)ctx.get(HzConstants.USER_CONTEXT_NAME);
    }
}
