package me.kingtux.tuxorm.daos;

import me.kingtux.tuxjsql.core.Table;
import me.kingtux.tuxjsql.core.result.DBResult;
import me.kingtux.tuxjsql.core.result.DBRow;
import me.kingtux.tuxjsql.core.statements.WhereStatement;
import me.kingtux.tuxorm.*;
import me.kingtux.tuxorm.serializers.MultiSecondarySerializer;
import me.kingtux.tuxorm.serializers.MultipleValueSerializer;
import me.kingtux.tuxorm.serializers.SecondarySerializer;
import me.kingtux.tuxorm.serializers.SingleSecondarySerializer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultSerializerDao<T, I> implements Dao<T, I> {
    private TOObject toObject;
    private DefaultSerializer defaultSerializer;
    private TOConnection connection;

    public DefaultSerializerDao(TOObject toObject, DefaultSerializer defaultSerializer, TOConnection connection) {
        this.toObject = toObject;
        this.defaultSerializer = defaultSerializer;
        this.connection = connection;
    }

    @Override
    public T findByID(I id) {
        return fetchFirst(toObject.getTable().getPrimaryColumn().getName(), id);
    }

    @Override
    public void update(T t) {
        if (t == null) {
            throw new NullPointerException("You cant update null!");
        }
        defaultSerializer.update(t, toObject);
    }

    @Override
    public T create(T t) {
        if(t==null){
            throw new NullPointerException("You can't insert null into db");
        }
        I id = (I) defaultSerializer.create(t, toObject);

        if (TOConnection.logger.isDebugEnabled())
            connection.getLogger().debug(id.toString());

        return findByID(id);
    }

    @Override
    public List<T> fetchAll() {
        return fetch(null);
    }

    @Override
    public List<T> fetch(String columnName, Object value) {
        String column = columnName;
        if (columnName == null || value == null) {
            throw new NullPointerException("Unable to fetch with null values");
        }
        Object v = null;
        if (TOUtils.isAnyTypeBasic(toObject.getFieldForColumnName(columnName).getType())) {
            v = value;
        } else if (toObject.getFieldForColumnName(columnName) != null &&
                connection.getSecondarySerializer(toObject.getFieldForColumnName(columnName).getType()) != null) {
            SecondarySerializer secondarySerializer = connection.getSecondarySerializer(toObject.getFieldForColumnName(columnName).getType());
            if (secondarySerializer instanceof SingleSecondarySerializer) {
                v = ((SingleSecondarySerializer) secondarySerializer).getSimplifiedValue(value);
            } else if (secondarySerializer instanceof MultiSecondarySerializer) {
                if (secondarySerializer instanceof MultipleValueSerializer) {
                    column = toObject.getTable().getPrimaryColumn().getName();
                    v = ((MultipleValueSerializer) secondarySerializer).contains(value, toObject.getOtherObjects().get(toObject.getFieldForColumnName(columnName)));
                } else {
                    //TODO look by that object
                }
            }
        } else {
            v = connection.getPrimaryValue(value);
        }

        if (v instanceof List) {
            List<T> values = new ArrayList<>();
            for (Object object : ((List) v)) {
                TOConnection.logger.debug(column + " " + object);
                values.addAll(fetch(connection.getBuilder().createWhere().start(column, TOUtils.simplifyObject(object))));
            }
            return values;
        } else {
            return fetch(connection.getBuilder().createWhere().start(column, TOUtils.simplifyObject(v)));
        }
    }

    public List<T> fetch(WhereStatement statement) {
        DBResult dbRows = toObject.getTable().select(statement);
        List<TOResult> results = new ArrayList<>();
        for (DBRow row : dbRows) {
            TableResult tr = new TableResult(row, toObject.getTable());
            Map<Field, TableResult> map = new HashMap<>();
            for (Map.Entry<Field, Table> entry : toObject.getOtherObjects().entrySet()) {
                Object object = TOUtils.simplifyObject(tr.getRow().getRowItem(toObject.getTable().getPrimaryColumn().getName()).getAsObject());
                DBResult result = entry.getValue().select(connection.getBuilder().createWhere().start(TOUtils.PARENT_ID_NAME
                        , object));
                TableResult subResult =
                        new TableResult(entry.getValue(), result);
                map.put(entry.getKey(), subResult);
            }
            results.add(new TOResult(toObject.getType(), tr, map));
        }
        List<T> list = new ArrayList<>();
        for (TOResult toResult : results) {
            T build = defaultSerializer.build(toObject.getType(), toResult, toObject);
            list.add(build);
        }
        return list;
    }

    @Override
    public void delete(T t) {
        if(t==null){
            throw new NullPointerException("You cant delete null");
        }
        defaultSerializer.delete(t, toObject);
    }

    @Override
    public void deleteById(I t) {
        delete(findByID(t));
    }

    @Override
    public String getTableName() {
        return toObject.getTable().getName();
    }

    @Override
    public TOConnection getConnection() {
        return connection;
    }

    @Override
    public TOObject getTOObject() {
        return toObject;
    }

    @Override
    public T refresh(T t) {
        return findByID((I) defaultSerializer.getPrimaryKey(t));
    }
}
