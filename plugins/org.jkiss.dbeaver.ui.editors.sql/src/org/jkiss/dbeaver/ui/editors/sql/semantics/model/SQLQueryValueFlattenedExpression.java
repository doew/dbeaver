/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2024 DBeaver Corp and others
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
package org.jkiss.dbeaver.ui.editors.sql.semantics.model;

import org.antlr.v4.runtime.misc.Interval;
import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.ui.editors.sql.semantics.SQLQueryRecognitionContext;
import org.jkiss.dbeaver.ui.editors.sql.semantics.context.SQLQueryDataContext;

import java.util.List;

public class SQLQueryValueFlattenedExpression extends SQLQueryValueExpression {
    private final List<SQLQueryValueExpression> operands;
    private final String content;

    public SQLQueryValueFlattenedExpression(
        @NotNull Interval range,
        @NotNull String content,
        @NotNull List<SQLQueryValueExpression> operands
    ) {
        super(range);
        this.content = content;
        this.operands = operands;
    }

    public String getContent() {
        return this.content;
    }

    public List<SQLQueryValueExpression> getOperands() {
        return operands;
    }

    @Override
    void propagateContext(@NotNull SQLQueryDataContext context, @NotNull SQLQueryRecognitionContext statistics) {
        this.operands.forEach(opnd -> opnd.propagateContext(context, statistics));
    }

    @Override
    protected <R, T> R applyImpl(@NotNull SQLQueryNodeModelVisitor<T, R> visitor, @NotNull T node) {
        return visitor.visitValueFlatExpr(this, node);
    }
}