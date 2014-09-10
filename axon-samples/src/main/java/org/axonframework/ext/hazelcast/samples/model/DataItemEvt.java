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

import com.google.common.base.MoreObjects;
import org.axonframework.serializer.Revision;

import java.io.Serializable;

public class DataItemEvt {

    // *************************************************************************
    //
    // *************************************************************************

    private abstract static class AbstractEvent implements Serializable {
        private String m_id;
        private String m_text;

        public AbstractEvent() {
            m_id   = null;
            m_text = null;
        }

        public AbstractEvent(String id,String text) {
            m_id   = id;
            m_text = text;
        }

        public void setId(String id) {
            m_id = id;
        }

        public String getId() {
            return m_id;
        }

        public void setText(String text) {
            m_text = text;
        }

        public String getText() {
            return m_text;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                .add("id"  , getId())
                .add("test", getText())
                .toString();
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Revision("1")
    public static final class Create extends AbstractEvent implements Serializable {

        public Create() {
            super();
        }

        public Create(String id,String text) {
            super(id,text);
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Revision("1")
    public static final class Update extends AbstractEvent implements Serializable {

        public Update() {
            super();
        }

        public Update(String id,String text) {
            super(id,text);
        }
    }
}

