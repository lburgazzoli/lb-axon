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

/**
 *
 */
public class HzCommandReply extends HzCommandCommon {

    private String m_commandId;
    private Object m_returnValue;
    private boolean m_success;

    /**
     * c-tor
     *
     * @oaram nodeName
     */
    public HzCommandReply(String nodeName) {
        this(nodeName,null,null,false);
    }

    /**
     * c-tor
     *
     * @oaram nodeName
     * @param commandId
     * @param returnValue
     */
    public HzCommandReply(String nodeName, String commandId, Object returnValue) {
        this(
            nodeName,
            commandId,
            returnValue,
            returnValue != null
                ? !(returnValue instanceof Throwable)
                : true);
    }

    /**
     * c-tor
     *
     * @param nodeName
     * @param commandId
     * @param returnValue
     * @param success
     */
    public HzCommandReply(String nodeName, String commandId, Object returnValue, boolean success) {
        super(nodeName);

        m_commandId = commandId;
        m_returnValue = returnValue;
        m_success = success;
    }

    // *************************************************************************
    //
    // *************************************************************************


    /**
     *
     * @return
     */
    public String getCommandId() {
        return m_commandId;
    }

    /**
     *
     * @return
     */
    public boolean isSuccess() {
        return m_success;
    }

    /**
     *
     * @return
     */
    public Object getReturnValue() {
        return m_success ? m_returnValue : null;
    }

    /**
     *
     * @return
     */
    public Throwable getError() {
        return m_success ? null : (Throwable)m_returnValue;
    }
}
