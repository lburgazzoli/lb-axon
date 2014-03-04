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
package org.axonframework.ext.osgi.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.options.WrappedUrlProvisionOption;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
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
public class OSGiBundleTest extends OSGiTestCommon {
    private static final List<String> BUNDLE_NAMES = Arrays.asList(
        "com.github.lburgazzoli.axon-hazelcast"
    );

    @Inject
    BundleContext context;

    @Configuration
    public Option[] config() {

        return options(
            systemProperty("org.osgi.framework.storage.clean").value("true"),
            systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("WARN"),
            mavenBundleEnv("org.slf4j", "slf4j-api", "version.slf4j"),
            mavenBundleEnv("org.slf4j", "slf4j-simple", "version.slf4j").noStart(),
            mavenBundleEnv("org.apache.commons","commons-lang3","version.commonsLang"),
            mavenBundleEnv("commons-collections","commons-collections","version.commonsCollections"),
            mavenBundleEnv("com.hazelcast","hazelcast","version.hazelcast"),
            mavenBundleEnv("org.axonframework","axon-core","version.axon"),
            mavenBundleEnv("org.axonframework","axon-distributed-commandbus","version.axon"),
            mavenBundleEnv("com.google.guava","guava","version.guava"),
            mavenBundleWrap("javax.cache","cache-api","version.javaxCache"),
            //mavenBundleWrap("net.sf.jsr107cache", "jsr107cache", "1.1"),
            mavenBundle("org.apache.geronimo.specs", "geronimo-servlet_3.0_spec", "1.0"),
            mavenBundle("joda-time", "joda-time", "2.3"),
            axonBundle("axon-common"),
            axonBundle("axon-hazelcast"),
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
            if(BUNDLE_NAMES.contains(bundle.getSymbolicName())) {
                axonBundles.put(bundle.getSymbolicName(),bundle);
            }
        }

        for(String bundleName : BUNDLE_NAMES) {
            assertTrue(axonBundles.containsKey(bundleName));
            assertTrue(axonBundles.get(bundleName).getState() == Bundle.ACTIVE);
        }
    }
}
