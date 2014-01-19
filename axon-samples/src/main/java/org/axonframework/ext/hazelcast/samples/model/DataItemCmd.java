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
package org.axonframework.ext.hazelcast.samples.model;

import org.axonframework.commandhandling.annotation.TargetAggregateIdentifier;
import org.axonframework.serializer.Revision;

import java.io.Serializable;

/**
 *
 */
public class DataItemCmd {

    // *************************************************************************
    //
    // *************************************************************************

    /**
     *
     */
    @Revision("1")
    public static final class Create implements Serializable {

        @TargetAggregateIdentifier
        private final String m_id;
        private final String m_text;

        /**
         * c-tor
         *
         * @param id
         * @param text
         */
        public Create(String id, String text) {
            m_id = id;
            m_text = text;
        }

        /**
         *
         * @return
         */
        public String getId() {
            return m_id;
        }

        /**
         *
         * @return
         */
        public String getText() {
            return m_text;
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     *
     */
    @Revision("1")
    public static final class Update implements Serializable {

        @TargetAggregateIdentifier
        private final String m_id;
        private final String m_text;

        /**
         * c-tor
         *
         * @param id
         * @param text
         */
        public Update(String id, String text) {
            m_id = id;
            m_text = text;
        }

        /**
         *
         * @return
         */
        public String getId() {
            return m_id;
        }

        /**
         *
         * @return
         */
        public String getText() {
            return m_text;
        }
    }
}
