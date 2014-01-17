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
package org.axonframework.hazelcast.test.osgi;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import javax.inject.Inject;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.*;

/**
 * @author lburgazzoli
 */
@RunWith(PaxExam.class)
public class OSGiBundleTest {
    private static final String BUNDLE_GROUP = "com.github.lburgazzoli";
    private static final String[] UNDLE_ARTIFACTS = new String[] {
        "axon-hazelcast"
    };

    @Inject
    BundleContext context;

    @Configuration
    public Option[] config() {
        return options(
            systemProperty("org.osgi.framework.storage.clean").value("true"),
            systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("WARN"),
            mavenBundle("org.slf4j", "slf4j-api", System.getProperty("slf4jVersion")),
            mavenBundle("org.slf4j", "slf4j-ext", System.getProperty("slf4jVersion")).noStart(),
            mavenBundle("org.slf4j", "jul-to-slf4j", System.getProperty("slf4jVersion")).noStart(),
            mavenBundle("org.slf4j", "jcl-over-slf4j", System.getProperty("slf4jVersion")).noStart(),
            mavenBundle("org.slf4j", "slf4j-log4j12", System.getProperty("slf4jVersion")).noStart(),
            mavenBundle("log4j","log4j",System.getProperty("log4jVersion")),
            mavenBundle("com.hazelcast","hazelcast-all",System.getProperty("hazelcastVersion")),
            mavenBundle("org.axonframework","axon-core",System.getProperty("axonVersion")),
            mavenBundle("org.axonframework","axon-distributed-commandbus",System.getProperty("axonVersion")),
            new File("axon-hazelcast/target/classes").exists()
                ? bundle("reference:file:axon-hazelcast/target/classes")
                : bundle("reference:file:../axon-hazelcast/target/classes"),
            junitBundles(),
            systemPackage("com.sun.tools.attach"),
            cleanCaches()
        );
    }

    @Test
    public void checkInject() {
        assertNotNull(context);
    }

    @Test
    public void checkBundle() {
        Map<String,Bundle> axonBundles = new HashMap<String,Bundle>();

        for(Bundle bundle : context.getBundles()) {
            if(bundle != null) {
                if(bundle.getSymbolicName().startsWith(BUNDLE_GROUP)) {
                    axonBundles.put(bundle.getSymbolicName(),bundle);
                }
            }
        }

        for(String artifact : UNDLE_ARTIFACTS) {
            assertTrue(axonBundles.containsKey(BUNDLE_GROUP + "." + artifact));
            assertTrue(axonBundles.get(BUNDLE_GROUP + "." + artifact).getState() == Bundle.ACTIVE);
        }
    }
}
