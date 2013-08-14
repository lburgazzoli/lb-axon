package org.axonframework.hazelcast.test.model;

import org.axonframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;
import org.axonframework.eventsourcing.annotation.AggregateIdentifier;

import java.io.Serializable;

/**
 *
 */
public class HzAxonAggregate extends AbstractAnnotatedAggregateRoot implements Serializable {


    @AggregateIdentifier
    private Object m_id;
    private Object m_data;

    /**
     * c-tor
     */
    public HzAxonAggregate() {
        this(null,null);
    }

    /**
     * c-tor
     *
     * @param id
     */
    public HzAxonAggregate(Object id) {
        this(id,null);
    }

    /**
     * c-tor
     *
     * @param id
     * @param data
     */
    public HzAxonAggregate(Object id, String data) {
        m_id   = id;
        m_data = data;
    }

    /**
     *
     * @return the data
     */
    public Object getData() {
        return m_data;
    }
}
