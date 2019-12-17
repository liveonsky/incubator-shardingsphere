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

package org.apache.shardingsphere.core.rewrite.feature.shadow;

import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.core.rule.ShadowRule;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.SimpleExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.SubqueryPredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateBetweenRightValue;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateCompareRightValue;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateInRightValue;
import org.apache.shardingsphere.sql.parser.sql.statement.generic.WhereSegmentAvailable;

import java.util.Collection;
import java.util.HashSet;

/**
 * Shadow condition engine.
 *
 * @author zhyee
 */
@RequiredArgsConstructor
public final class ShadowConditionEngine {

    private final ShadowRule shadowRule;

    /**
     * Create shadow conditions.
     *
     * @param sqlStatementContext SQL statement context
     * @return shadow condition
     */
    public Optional<ShadowCondition> createShadowCondition(final SQLStatementContext sqlStatementContext) {
        if (!(sqlStatementContext.getSqlStatement() instanceof WhereSegmentAvailable)) {
            return Optional.absent();
        }
        Optional<WhereSegment> whereSegment = ((WhereSegmentAvailable) sqlStatementContext.getSqlStatement()).getWhere();
        if (!whereSegment.isPresent()) {
            return Optional.absent();
        }
        for (AndPredicate each : whereSegment.get().getAndPredicates()) {
            Optional<ShadowCondition> condition = createShadowCondition(each);
            if (condition.isPresent()) {
                return condition;
            }
        }
        for (SubqueryPredicateSegment each : sqlStatementContext.getSqlStatement().findSQLSegments(SubqueryPredicateSegment.class)) {
            for (AndPredicate andPredicate : each.getAndPredicates()) {
                Optional<ShadowCondition> condition = createShadowCondition(andPredicate);
                if (condition.isPresent()) {
                    return condition;
                }
            }
        }
        return Optional.absent();
    }

    private Optional<ShadowCondition> createShadowCondition(final AndPredicate andPredicate) {
        for (PredicateSegment predicate : andPredicate.getPredicates()) {
            Collection<Integer> stopIndexes = new HashSet<>();
            if (stopIndexes.add(predicate.getStopIndex())) {
                Optional<ShadowCondition> condition = shadowRule.getColumn().equals(predicate.getColumn().getName()) ? createShadowCondition(predicate) : Optional.<ShadowCondition>absent();
                if (condition.isPresent()) {
                    return condition;
                }
            }
        }
        return Optional.absent();
    }

    private Optional<ShadowCondition> createShadowCondition(final PredicateSegment predicateSegment) {
        if (predicateSegment.getRightValue() instanceof PredicateCompareRightValue) {
            PredicateCompareRightValue compareRightValue = (PredicateCompareRightValue) predicateSegment.getRightValue();
            return isSupportedOperator(compareRightValue.getOperator()) ? createCompareShadowCondition(predicateSegment, compareRightValue) : Optional.<ShadowCondition>absent();
        }
        if (predicateSegment.getRightValue() instanceof PredicateInRightValue) {
            throw new ShardingException("The SQL clause 'IN...' is unsupported in shadow rule.");
        }
        if (predicateSegment.getRightValue() instanceof PredicateBetweenRightValue) {
            throw new ShardingException("The SQL clause 'BETWEEN...AND...' is unsupported in shadow rule.");
        }
        return Optional.absent();
    }

    private static Optional<ShadowCondition> createCompareShadowCondition(final PredicateSegment predicateSegment, final PredicateCompareRightValue compareRightValue) {
        return compareRightValue.getExpression() instanceof SimpleExpressionSegment
                ? Optional.of(new ShadowCondition(predicateSegment.getColumn().getName(), compareRightValue.getExpression().getStartIndex(),
                predicateSegment.getStopIndex(), compareRightValue.getExpression()))
                : Optional.<ShadowCondition>absent();
    }

    private boolean isSupportedOperator(final String operator) {
        return "=".equals(operator);
    }
}
