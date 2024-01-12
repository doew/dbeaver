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
package org.jkiss.dbeaver.ext.hana.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.generic.model.GenericTable;
import org.jkiss.dbeaver.ext.generic.model.GenericTableBase;
import org.jkiss.dbeaver.ext.generic.model.GenericTableColumn;
import org.jkiss.dbeaver.model.DBPNamedObject2;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.gis.DBGeometryDimension;
import org.jkiss.dbeaver.model.gis.GisAttribute;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;

import java.sql.SQLException;

public class HANATableColumn extends GenericTableColumn implements DBPNamedObject2, GisAttribute {

	private static final int FLAT_EARTH_SRID_START = 1000000000;

	private static final int FLAT_EARTH_SRID_END = 2000000000;

    private GeometryInfo geometryInfo;

    public HANATableColumn(GenericTable table) {
        super(table);
    }

    public HANATableColumn(GenericTableBase table, String columnName, String typeName, int valueType, int sourceType, int ordinalPosition, long columnSize, long charLength, Integer scale, Integer precision, int radix, boolean notNull, String remarks, String defaultValue, boolean autoIncrement, boolean autoGenerated) throws DBException {
        super(table, columnName, typeName, valueType, sourceType, ordinalPosition, columnSize, charLength, scale, precision, radix, notNull, remarks, defaultValue, autoIncrement, autoGenerated);
    }

    private static class GeometryInfo {
        private String type;
        private int srid = -1;
        private int dimension = -1;
    }

    @Override
    public int getAttributeGeometrySRID(DBRProgressMonitor monitor) throws DBCException {
        if (geometryInfo == null) {
            readGeometryInfo(monitor);
        }
        if (geometryInfo != null) {
            return geometryInfo.srid;
        } else {
            return -1;
        }
    }

    @NotNull
    @Override
    public DBGeometryDimension getAttributeGeometryDimension(DBRProgressMonitor monitor) throws DBCException {
        if (geometryInfo == null) {
            readGeometryInfo(monitor);
        }
        if (geometryInfo != null) {
            // TODO: This does not cover XYM dimension, need to find a better solution
            switch (geometryInfo.dimension) {
                case 3:
                    return DBGeometryDimension.XYZ;
                case 4:
                    return DBGeometryDimension.XYZM;
                default:
                    return DBGeometryDimension.XY;
            }
        } else {
            return DBGeometryDimension.XY;
        }

    }

    @Nullable
    @Override
    public String getAttributeGeometryType(DBRProgressMonitor monitor) throws DBCException {
        if (geometryInfo == null) {
            readGeometryInfo(monitor);
        }
        if (geometryInfo != null) {
            return geometryInfo.type;
        } else {
            return null;
        }
    }

    private void readGeometryInfo(DBRProgressMonitor monitor) throws DBCException {
        if (geometryInfo != null) {
            return;
        }

        GeometryInfo gi = new GeometryInfo();
        try (JDBCSession session = DBUtils.openMetaSession(monitor, this, "Load table inheritance info")) {
            try (JDBCPreparedStatement dbStat = session
                    .prepareStatement("SELECT SRS_ID, DATA_TYPE_NAME, COORD_DIMENSION FROM SYS.ST_GEOMETRY_COLUMNS "
                            + "WHERE SCHEMA_NAME=? AND TABLE_NAME=? AND COLUMN_NAME=?")) {
                dbStat.setString(1, getTable().getSchema().getName());
                dbStat.setString(2, getTable().getName());
                dbStat.setString(3, getName());
                try (JDBCResultSet dbResult = dbStat.executeQuery()) {
                    if (dbResult.next()) {
                        gi.srid = dbResult.getInt(1);
                        // HANA does not distinguish between geometry and geography type. Instead, HANA has additional
                        // SRS's for round-earth spatial reference systems with an offset of 1,000,000,000 that emulate
                        // a flat earth. If the SRID is in such a range, we have to substract the SRID to find the
                        // actual SRID that works with leaflet.
                        if ((FLAT_EARTH_SRID_START <= gi.srid) && (gi.srid < FLAT_EARTH_SRID_END)) {
                            gi.srid -= FLAT_EARTH_SRID_START;
                        }
                        gi.type = dbResult.getString(2);
                        gi.dimension = dbResult.getInt(3);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DBCException("Error reading geometry info", e);
        }

        geometryInfo = gi;
    }
}
