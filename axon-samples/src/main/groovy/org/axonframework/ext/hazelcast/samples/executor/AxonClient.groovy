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
package org.axonframework.ext.hazelcast.samples.executor

import org.axonframework.ext.CommandCallbackTracer
import org.axonframework.ext.hazelcast.samples.model.DataItemCmd
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.support.ClassPathXmlApplicationContext


class AxonClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(AxonClient.class)

    // *************************************************************************
    //
    // *************************************************************************

    public static void main(String[] args) {
        System.setProperty('hazelcast.logging.type','slf4j')
        System.setProperty('hazelcast.jmx','false')

        def context = null;
        def engine = null;

        try {
            context = new ClassPathXmlApplicationContext("axon-client.xml")
            engine  = context.getBean("axon-service",AxonService.class)

            for(n in 0..10) {
                for(i in 0..10) {
                    engine.send(
                        new DataItemCmd.Create(
                            String.format("k_%03d",i),
                            String.format("d_%03d",i)
                        ),
                        new CommandCallbackTracer<>(LOGGER)
                    )
                }
            }

            try {
                LOGGER.debug("sleep...")
                Thread.sleep(5000)
            } catch (InterruptedException e) {
            }

        } catch(Exception e) {
            LOGGER.warn("Exception",e)
        } finally {
            context.close()
        }
    }
}
