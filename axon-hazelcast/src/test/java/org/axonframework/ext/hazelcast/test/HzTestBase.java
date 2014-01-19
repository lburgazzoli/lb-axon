package org.axonframework.ext.hazelcast.test;

import com.hazelcast.config.Config;
import org.axonframework.ext.hazelcast.HzProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class HzTestBase {
    protected static final Logger LOGGER = LoggerFactory.getLogger("hz-axon-org.axonframework.ext.eventstore.chronicle.test");

    /**
     *
     */
    protected HzProxy createHzProxy() {

        Config cfg = new Config();
        cfg.setProperty("hazelcast.logging.type", "slf4j");
        cfg.getNetworkConfig().setPortAutoIncrement(false);
        cfg.getNetworkConfig().getInterfaces().setEnabled(false);
        cfg.getNetworkConfig().getJoin().getAwsConfig().setEnabled(false);
        cfg.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        cfg.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(false);

        HzProxy hxPx = new HzProxy(cfg);
        hxPx.setDistributedObjectNamePrefix("axon");

        return hxPx;
    }
}
