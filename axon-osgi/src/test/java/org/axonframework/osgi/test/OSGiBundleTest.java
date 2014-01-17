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
package org.axonframework.osgi.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.cleanCaches;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemPackage;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;

/**
 * @author lburgazzoli
 */
@RunWith(PaxExam.class)
public class OSGiBundleTest {
    private static final List<String> BUNDLE_NAMES = Arrays.asList("lb-axon-hazelcast");

    @Inject
    BundleContext context;

    @Configuration
    public Option[] config() {
        String axonHazelcastJar = new StringBuilder()
            .append("build")
            .append("/")
            .append("libs")
            .append("/")
            .append("lb-axon-hazelcast-")
            .append(System.getProperty("projectVersion"))
            .append(".jar")
            .toString();

        return options(
            systemProperty("org.osgi.framework.storage.clean").value("true"),
            systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("WARN"),
            mavenBundle("org.slf4j", "slf4j-api", System.getProperty("slf4jVersion")),
            mavenBundle("org.slf4j", "slf4j-simple", System.getProperty("slf4jVersion")).noStart(),
            mavenBundle("org.apache.geronimo.specs","geronimo-servlet_3.0_spec","1.0"),
            wrappedBundle(mavenBundle("net.sf.jsr107cache", "jsr107cache", "1.1")),
            mavenBundle("joda-time", "joda-time", "2.1"),
            mavenBundle("org.apache.commons","commons-lang3","3.1"),
            mavenBundle("commons-collections","commons-collections","3.2.1"),
            mavenBundle("com.hazelcast","hazelcast",System.getProperty("hazelcastVersion")),
            mavenBundle("org.axonframework","axon-core",System.getProperty("axonVersion")),
            mavenBundle("org.axonframework","axon-distributed-commandbus",System.getProperty("axonVersion")),
            mavenBundle("com.google.guava","guava",System.getProperty("guavaVersion")),
            new File("axon-hazelcast/" + axonHazelcastJar).exists()
                ? bundle("reference:file:axon-hazelcast/" + axonHazelcastJar)
                : bundle("reference:file:../axon-hazelcast/" + axonHazelcastJar),
            junitBundles(),
            systemPackage("com.sun.script.javascript"),
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
                if(BUNDLE_NAMES.contains(bundle.getSymbolicName())) {
                    axonBundles.put(bundle.getSymbolicName(),bundle);
                }
            }
        }

        for(String bundleName : BUNDLE_NAMES) {
            assertTrue(axonBundles.containsKey(bundleName));
            assertTrue(axonBundles.get(bundleName).getState() == Bundle.ACTIVE);
        }
    }
}