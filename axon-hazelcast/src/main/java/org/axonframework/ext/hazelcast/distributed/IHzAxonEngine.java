package org.axonframework.ext.hazelcast.distributed;

import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.eventhandling.EventListener;
import org.axonframework.eventsourcing.EventSourcedAggregateRoot;
import org.axonframework.ext.hazelcast.distributed.commandbus.HzCommand;
import org.axonframework.ext.hazelcast.distributed.commandbus.HzCommandReply;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author lburgazzoli
 */
public interface IHzAxonEngine {
    /**
     *
     */
    public void init();

    /**
     *
     */
    public void destroy();

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

    /**
     *
     * @param command
     * @return
     */
    public Future<HzCommandReply> dispatch(final HzCommand command);

    /**
     *
     * @param eventHandler
     */
    public void addEventHandler(Object eventHandler);

    /**
     *
     * @param eventHandler
     */
    public void removeEventHandler(Object eventHandler);

    /**
     *
     * @param eventListener
     */
    public void addEventListener(EventListener eventListener);

    /**
     *
     * @param eventListener
     */
    public void removeEventListener(EventListener eventListener) ;

    /**
     *
     * @param aggregateType
     */
    public <T extends EventSourcedAggregateRoot> void addAggregateType(Class<T> aggregateType);

    /**
     *
     * @param aggregateType
     */
    public void removeAggregateType(Class<? extends EventSourcedAggregateRoot> aggregateType);
}
