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
package org.axonframework.ext.hazelcast.distributed.commandbus.queue;

import org.axonframework.ext.hazelcast.HzConstants;

/**
 *
 */
public class HzCommandConstants extends HzConstants {

    public static final String ATTR_SRC_NODE_ID      = "attr.node.id.source";
    public static final String REG_CMD_NODES         = "reg.queue.cmd.nodes";
    public static final String REG_CMD_DESTINATIONS  = "reg.queue.cmd.destinations";
    public static final String REG_CMD_HANDLERS      = "reg.queue.cmd.handlers";
}
