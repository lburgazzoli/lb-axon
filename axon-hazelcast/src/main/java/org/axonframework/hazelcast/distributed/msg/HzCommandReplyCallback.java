package org.axonframework.hazelcast.distributed.msg;

import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.hazelcast.IHzProxy;
import org.axonframework.hazelcast.distributed.HzCommandBusAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class HzCommandReplyCallback<T> implements CommandCallback<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HzCommandReplyCallback.class);

    private final IHzProxy m_proxy;
    private final HzCommandBusAgent m_agent;
    private final HzCommand m_command;

    /**
     * c-tor
     *
     * @param proxy
     * @param agent
     * @param command
     */
    public HzCommandReplyCallback(IHzProxy proxy,HzCommandBusAgent agent,HzCommand command) {
        m_proxy   = proxy;
        m_agent   = agent;
        m_command = command;
    }

    @Override
    public void onSuccess(Object result) {
        try {
            m_proxy.getQueue(m_command.getSourceNodeId()).put(new HzCommandReply(
                m_agent.getNodeName(),
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
            m_proxy.getQueue(m_command.getSourceNodeId()).put(new HzCommandReply(
                m_agent.getNodeName(),
                m_command.getMessage().getIdentifier(),
                cause)
            );
        } catch(Exception e) {
            LOGGER.warn("Exception",e);
        }
    }
}
