package org.axonframework.hazelcast.test;

import com.hazelcast.config.Config;
import org.axonframework.hazelcast.HzProxy;

/**
 *
 */
public class HzTestBase {

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
