package org.axonframework.hazelcast.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.axonframework.hazelcast.test.model.HzAxonAggregate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class HzCacheTest extends HzTestBase {
    //private HzProxy m_proxy = null;
    private ObjectMapper m_mapper = null;

    // *************************************************************************
    //
    // *************************************************************************

    @Before
    public void setUp() throws Exception {
        //m_proxy = createHzProxy();
        //m_proxy.init();

        m_mapper = new ObjectMapper();
        m_mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS,false);
        m_mapper.configure(SerializationFeature.INDENT_OUTPUT,true);
    }

    @After
    public void tearDown() throws Exception {
        //m_proxy.destroy();
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Test
    public void testSaveReadBack() {
        //HzCache cache = new HzCache(m_proxy,"test");

        HzAxonAggregate agPut = new HzAxonAggregate("id1","data1");
        HzAxonAggregate agGet = new HzAxonAggregate("id1","data1");

        try {
            LOGGER.debug("json --> {}", m_mapper.writeValueAsString(agPut));
        } catch (JsonProcessingException e) {
            LOGGER.warn("JsonProcessingException",e);
        }

        assertEquals(agPut.getIdentifier(), agGet.getIdentifier());
    }
}
