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
package org.axonframework.hazelcast.distributed.cmd;

import com.google.common.base.Objects;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;
import java.util.Date;

/**
 *
 */
public class HzCommandBusNode implements DataSerializable {
    private String m_name;
    private String m_queueName;
    private Long m_lastHeartBeat;

    /**
     * c-tor
     */
    public HzCommandBusNode() {
        m_name          = null;
        m_queueName     = null;
        m_lastHeartBeat = null;
    }

    /**
     * c-tor
     *
     * @param name
     * @param queueName
     */
    public HzCommandBusNode(String name, String queueName) {
        m_name          = name;
        m_queueName     = queueName;
        m_lastHeartBeat = System.currentTimeMillis();
    }

    /**
     *
     * @return
     */
    public String getName() {
        return m_name;
    }

    /**
     *
     * @return
     */
    public String getQueueName() {
        return m_queueName;
    }

    @Override
    public void writeData(ObjectDataOutput dataOutput) throws IOException {
        dataOutput.writeUTF(m_name);
        dataOutput.writeUTF(m_queueName);
        dataOutput.writeLong(m_lastHeartBeat);
    }

    @Override
    public void readData(ObjectDataInput dataInput) throws IOException {
        m_name          = dataInput.readUTF();
        m_queueName     = dataInput.readUTF();
        m_lastHeartBeat = dataInput.readLong();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("name"         , m_name)
            .add("inbox"        , m_queueName)
            .add("lastHeartBeat", new Date(m_lastHeartBeat))
            .toString();
    }
}
