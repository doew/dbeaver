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
package org.jkiss.dbeaver.ext.clickhouse.model;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.clickhouse.ClickhouseTypeParser;
import org.jkiss.dbeaver.ext.generic.model.GenericTableBase;
import org.jkiss.dbeaver.ext.generic.model.GenericTableColumn;

import java.util.Map;

public class ClickhouseTableColumn extends GenericTableColumn {
    private final Map<String, Integer> enumEntries;
    private String fullTypeName;

    public ClickhouseTableColumn(GenericTableBase table, String columnName, String typeName, int valueType, int sourceType, int ordinalPosition, long columnSize, long charLength, Integer scale, Integer precision, int radix, boolean notNull, String remarks, String defaultValue, boolean autoIncrement, boolean autoGenerated) {
        super(table, columnName, typeName, valueType, sourceType, ordinalPosition, columnSize, charLength, scale, precision, radix, notNull, remarks, defaultValue, autoIncrement, autoGenerated);
        this.typeName = ClickhouseTypeParser.getTypeNameWithoutModifiers(typeName);
        this.fullTypeName = typeName;
        this.enumEntries = ClickhouseTypeParser.tryParseEnumEntries(typeName);
    }

    @Override
    public String getFullTypeName() {
        return fullTypeName;
    }

    @Override
    public void setFullTypeName(String fullTypeName) throws DBException {
        this.fullTypeName = fullTypeName;
        if (fullTypeName.toUpperCase().contains("ARRAY")) {
            setTypeName(fullTypeName);
        } else {
            super.setFullTypeName(fullTypeName);
        }
    }

    @NotNull
    public Map<String, Integer> getEnumEntries() {
        return enumEntries;
    }
}
