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
package org.jkiss.dbeaver.ext.wmi.model;

import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.*;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.struct.DBSEntityAttribute;
import org.jkiss.utils.CommonUtils;
import org.jkiss.wmi.service.WMIConstants;
import org.jkiss.wmi.service.WMIException;
import org.jkiss.wmi.service.WMIObjectAttribute;

/**
 * Class property
 */
public class WMIClassAttribute extends WMIClassElement<WMIObjectAttribute> implements DBSEntityAttribute, DBPImageProvider
{
    private static final Log log = Log.getLog(WMIClassAttribute.class);

    protected WMIClassAttribute(WMIClass wmiClass, WMIObjectAttribute attribute)
    {
        super(wmiClass, attribute);
    }

    @Override
    @Property(viewable = true, order = 10)
    public String getTypeName()
    {
        return element.getTypeName();
    }

    @Override
    public String getFullTypeName() {
        return DBUtils.getFullTypeName(this);
    }

    @Override
    public int getTypeID()
    {
        return element.getType();
    }

    @Override
    public DBPDataKind getDataKind()
    {
        return getDataKindById(element.getType());
    }

    @Override
    public Integer getScale()
    {
        return 0;
    }

    @Override
    public Integer getPrecision()
    {
        return 0;
    }

    @Override
    public long getMaxLength()
    {
        try {
            Object maxLengthQ = getQualifiedObject().getQualifier(WMIConstants.Q_MaxLen);
            if (maxLengthQ instanceof Number) {
                return ((Number) maxLengthQ).longValue();
            }
        } catch (WMIException e) {
            log.warn(e);
        }
        return 0;
    }

    @Override
    public long getTypeModifiers() {
        return 0;
    }

    @Override
    public boolean isRequired()
    {
        return false;
    }

    @Override
    public boolean isAutoGenerated()
    {
        return false;
    }

    @Override
    public int getOrdinalPosition()
    {
        return 0;
    }

    public boolean isKey() throws DBException
    {
        return getFlagQualifier(WMIConstants.Q_Key) || getFlagQualifier(WMIConstants.Q_CIM_Key);
    }

    @Override
    @Property(viewable = true, order = 20)
    public String getDefaultValue()
    {
        return CommonUtils.toString(element.getValue());
    }

    @Nullable
    @Override
    public DBPImage getObjectImage()
    {
        return getPropertyImage(element.getType());
    }

    public static DBPImage getPropertyImage(int type)
    {
        switch (type) {
            case WMIConstants.CIM_SINT8:
            case WMIConstants.CIM_UINT8:
            case WMIConstants.CIM_SINT16:
            case WMIConstants.CIM_UINT16:
            case WMIConstants.CIM_SINT32:
            case WMIConstants.CIM_UINT32:
            case WMIConstants.CIM_SINT64:
            case WMIConstants.CIM_UINT64:
            case WMIConstants.CIM_REAL32:
            case WMIConstants.CIM_REAL64:
                return DBIcon.TYPE_NUMBER;
            case WMIConstants.CIM_BOOLEAN:
                return DBIcon.TYPE_BOOLEAN;
            case WMIConstants.CIM_STRING:
            case WMIConstants.CIM_CHAR16:
                return DBIcon.TYPE_STRING;
            case WMIConstants.CIM_DATETIME:
                return DBIcon.TYPE_DATETIME;
            default:
                return DBIcon.TYPE_UNKNOWN;
        }
    }

    public static DBPDataKind getDataKindById(int type)
    {
        switch (type) {
            case WMIConstants.CIM_SINT8:
            case WMIConstants.CIM_UINT8:
            case WMIConstants.CIM_SINT16:
            case WMIConstants.CIM_UINT16:
            case WMIConstants.CIM_SINT32:
            case WMIConstants.CIM_UINT32:
            case WMIConstants.CIM_SINT64:
            case WMIConstants.CIM_UINT64:
            case WMIConstants.CIM_REAL32:
            case WMIConstants.CIM_REAL64:
                return DBPDataKind.NUMERIC;
            case WMIConstants.CIM_BOOLEAN:
                return DBPDataKind.BOOLEAN;
            case WMIConstants.CIM_STRING:
            case WMIConstants.CIM_CHAR16:
                return DBPDataKind.STRING;
            case WMIConstants.CIM_DATETIME:
                return DBPDataKind.DATETIME;
            default:
                return DBPDataKind.OBJECT;
        }
    }

}
