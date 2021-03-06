package me.kingtux.tuxorm.daos;

import me.kingtux.tuxjsql.core.TuxJSQL;
import me.kingtux.tuxjsql.core.response.DBColumnItem;
import me.kingtux.tuxjsql.core.response.DBRow;
import me.kingtux.tuxjsql.core.response.DBSelect;
import me.kingtux.tuxjsql.core.sql.SQLTable;
import me.kingtux.tuxjsql.core.sql.where.WhereStatement;
import me.kingtux.tuxorm.*;
import me.kingtux.tuxorm.exceptions.MissingValueException;
import me.kingtux.tuxorm.toobjects.TOObject;
import me.kingtux.tuxorm.serializers.MultiSecondarySerializer;
import me.kingtux.tuxorm.serializers.MultipleValueSerializer;
import me.kingtux.tuxorm.serializers.SecondarySerializer;
import me.kingtux.tuxorm.serializers.SingleSecondarySerializer;

import java.lang.reflect.Field;
import java.util.*;

public class DaoUtils {
    public static List<WhereStatement> createWhere(TuxJSQL tuxJSQL, TOConnection connection, TOObject toObject, String columnName, Object value) {
        if (columnName == null || value == null) {
            throw new NullPointerException("Unable to fetch with null values");
        }
        String column = columnName.toLowerCase();
        Field field = toObject.getFieldForColumnName(column);
        if (field == null) {
            throw new NullPointerException(String.format("Unable to find column %s", column));
        }
        Object v = null;
        if (TOUtils.isAnyTypeBasic(field.getType())) {
            v = value;
        } else if (connection.getSecondarySerializer(field.getType()) != null) {
            SecondarySerializer secondarySerializer = connection.getSecondarySerializer(field.getType());
            if (secondarySerializer instanceof SingleSecondarySerializer) {
                v = ((SingleSecondarySerializer) secondarySerializer).getSimplifiedValue(value);
            } else if (secondarySerializer instanceof MultiSecondarySerializer) {
                if (secondarySerializer instanceof MultipleValueSerializer) {
                    column = toObject.getTable().getPrimaryColumn().getName();
                    SQLTable table = toObject.getOtherObjects().get(field);
                    v = ((MultipleValueSerializer) secondarySerializer).contains(value, table);
                } else {

                    WhereStatement whereStatement = ((MultiSecondarySerializer) secondarySerializer).where(value, toObject.getOtherObjects().get(field));
                    DBSelect result;
                    try {
                        result = toObject.getOtherObjects().get(field).select().where(whereStatement).execute().complete();
                    } catch (InterruptedException e) {
                        TOConnection.logger.error("Unable to get value", e);
                        Thread.currentThread().interrupt();
                        return Collections.emptyList();
                    }
                    v = TOUtils.ids(result, value);
                    column = toObject.getTable().getPrimaryColumn().getName();
                }
            }
        } else {
            v = connection.getPrimaryValue(value);
        }

        if (v instanceof List) {
            List<WhereStatement> values = new ArrayList<>();
            for (Object object : ((List) v)) {
                values.add(connection.getBuilder().createWhere().start(column, TOUtils.simplifyObject(object)));
            }
            return values;
        } else {
            return Collections.singletonList(connection.getBuilder().createWhere().start(column, TOUtils.simplifyObject(v)));
        }
    }

    public static List<TOResult> fetch(WhereStatement statement, TOObject toObject) {
        DBSelect dbRows;
        List<TOResult> results = new ArrayList<>();

        try {
            dbRows = toObject.getTable().select().where(statement).execute().complete();
            for (DBRow row : dbRows) {
                TableResult tr = new TableResult(row, toObject.getTable());
                Map<Field, TableResult> map = new HashMap<>();
                for (Map.Entry<Field, SQLTable> entry : toObject.getOtherObjects().entrySet()) {
                    Optional<DBColumnItem> columnItem = tr.getRow().getColumn(toObject.getTable().getPrimaryColumn().getName());
                    if (!columnItem.isPresent()) {
                        throw new MissingValueException(toObject.getTable().getPrimaryColumn().getName() + " is missing from the table");
                    }
                    Object object = TOUtils.simplifyObject(columnItem.get().getAsObject());
                    DBSelect result;
                    result = entry.getValue().select().where().start(TOUtils.PARENT_ID_NAME
                            , object).and().execute().complete();

                    TableResult subResult =
                            new TableResult(entry.getValue(), result);
                    map.put(entry.getKey(), subResult);
                }
                results.add(new TOResult(toObject.getType(), tr, map));
            }
        } catch (InterruptedException e) {
            TOConnection.logger.error("Unable to get value", e);
            Thread.currentThread().interrupt();
        }
        return results;
    }

}
