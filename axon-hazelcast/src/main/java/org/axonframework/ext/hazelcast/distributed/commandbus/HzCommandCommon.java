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
package org.axonframework.ext.hazelcast.distributed.commandbus;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 *
 */
public class HzCommandCommon extends HzMessage {
    private final Map<String,String> m_attributes;

    /**
     * c-tor
     */
    public HzCommandCommon() {
        m_attributes = Maps.newHashMap();
    }

    /**
     *
     * @return
     */
    public Map<String,String> getAttributes() {
        return m_attributes;
    }
}
