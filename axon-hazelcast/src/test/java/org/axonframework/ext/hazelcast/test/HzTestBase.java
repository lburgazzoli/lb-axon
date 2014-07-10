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
package org.axonframework.ext.hazelcast.test;

import com.github.lburgazzoli.hazelcast.HzConf;
import com.hazelcast.core.HazelcastInstance;

public class HzTestBase {

    protected HazelcastInstance createInstance() {
        HzConf cfg = new HzConf("axon");
        cfg.setProperty("hazelcast.logging.type", "slf4j");
        cfg.setInterfacesEnabled(false);
        cfg.setAwsEnabled(false);
        cfg.setMulticastEnabled(false);
        cfg.setTcpIpEnabled(false);
        cfg.getNetworkConfig().setPortAutoIncrement(false);

        return cfg.newHazelcastInstance();
    }
}
