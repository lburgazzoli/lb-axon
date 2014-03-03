package org.axonframework.ext.hazelcast.distributed;

import org.axonframework.commandhandling.CommandCallback;

import java.util.concurrent.TimeUnit;

/**
 * @author lburgazzoli
 */
public interface IHzAxonEngine {
    /**
     *
     * @param command
     */
    public void send(Object command);

    /**
     *
     * @param command
     * @param callback
     * @param <R>
     */
    public <R> void send(Object command, CommandCallback<R> callback);

    /**
     *
     * @param command
     * @param <R>
     * @return
     */
    public <R> R sendAndWait(Object command);

    /**
     *
     * @param command
     * @param timeout
     * @param unit
     * @param <R>
     * @return
     */
    public <R> R sendAndWait(Object command, long timeout, TimeUnit unit);
}
