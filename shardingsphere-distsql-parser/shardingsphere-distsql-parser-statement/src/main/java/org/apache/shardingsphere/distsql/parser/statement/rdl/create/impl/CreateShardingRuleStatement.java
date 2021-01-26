/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.parser.segment.FunctionSegment;
import org.apache.shardingsphere.distsql.parser.segment.TableRuleSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.CreateRDLStatement;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Create sharding rule statement.
 */
@RequiredArgsConstructor
@Getter
public final class CreateShardingRuleStatement extends CreateRDLStatement {
    
    private final Collection<TableRuleSegment> tables = new LinkedList<>();
    
    private final Collection<String> bindingTables = new LinkedList<>();
    
    private final Collection<String> broadcastTables = new LinkedList<>();
    
    private final String defaultTableStrategyColumn;
    
    private final FunctionSegment defaultTableStrategy;
}