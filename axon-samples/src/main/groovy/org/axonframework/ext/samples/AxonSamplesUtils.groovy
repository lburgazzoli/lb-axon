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
package org.axonframework.ext.samples

import com.github.lburgazzoli.hazelcast.HzConf
import com.hazelcast.config.Config
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.axonframework.ext.eventstore.chronicle.ChronicleEventStore

class AxonSamplesUtils {
    static def Config newHazelcastLocalConfig() {
        HzConf config = new HzConf()
        config.networkConfig.portAutoIncrement = false
        config.interfacesEnabled = false
        config.awsEnabled = false
        config.multicastEnabled = false
        config.tcpIpEnabled = false

        return config;
    }

    static def ChronicleEventStore defaultChronicleEventStore() {
        String basePath = FilenameUtils.concat(
            FileUtils.tempDirectoryPath,
            "chronicle/axon-evt-store");

        return new ChronicleEventStore(basePath);
    }

    public static void sleep(long ms) {
        try {
            Thread.sleep(ms)
        } catch (InterruptedException e) {
        }
    }
}
