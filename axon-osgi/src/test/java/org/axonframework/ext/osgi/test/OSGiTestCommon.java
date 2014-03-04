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

import org.ops4j.pax.exam.options.MavenArtifactProvisionOption;
import org.ops4j.pax.exam.options.UrlProvisionOption;

import java.io.File;

import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

/**
 *
 */
public class OSGiTestCommon {
    /**
     * @param axonBundleName
     * @return
     */
    protected UrlProvisionOption axonBundle(final String axonBundleName) {
        String jarName = new StringBuilder()
            .append("build")
            .append(File.separator)
            .append("libs")
            .append(File.separator)
            .append(axonBundleName)
            .append("-")
            .append(System.getProperty("version.project"))
            .append(".jar")
            .toString();

        return new File(axonBundleName + File.separator + jarName).exists()
            ? bundle("reference:file:" + axonBundleName + File.separator + jarName)
            : bundle("reference:file:.." + File.separator + axonBundleName + File.separator + jarName);
    }

    /**
     *
     * @param groupId
     * @param artifactId
     * @param systemProperyKey
     * @return
     */
    protected MavenArtifactProvisionOption mavenBundleEnv(final String groupId,final String artifactId,final String systemProperyKey) {
        return mavenBundle(groupId, artifactId, System.getProperty(systemProperyKey));
    }
}
