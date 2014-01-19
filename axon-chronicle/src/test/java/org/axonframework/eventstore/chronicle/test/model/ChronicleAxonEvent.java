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
package org.axonframework.eventstore.chronicle.test.model;

import com.google.common.base.Objects;

import java.io.Serializable;

/**
 *
 */
public class ChronicleAxonEvent implements Serializable {

    private final Object m_data;

    /**
     * c-tor
     */
    public ChronicleAxonEvent() {
        this(null);
    }

    /**
     * c-tor
     *
     * @param data
     */
    public ChronicleAxonEvent(Object data) {
        m_data = data;
    }

    /**
     *
     * @return
     */
    public Object getData() {
        return m_data;
    }


    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("data" , m_data)
            .toString();
    }
}
