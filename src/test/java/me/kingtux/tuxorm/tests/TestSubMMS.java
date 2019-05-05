package me.kingtux.tuxorm.tests;

import me.kingtux.tuxjsql.core.Column;
import me.kingtux.tuxjsql.core.CommonDataTypes;
import me.kingtux.tuxjsql.core.DataType;
import me.kingtux.tuxjsql.core.Table;
import me.kingtux.tuxjsql.core.result.DBResult;
import me.kingtux.tuxjsql.core.result.DBRow;
import me.kingtux.tuxorm.TOConnection;
import me.kingtux.tuxorm.serializers.SubMSSCompatible;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestSubMMS implements SubMSSCompatible<Item> {
    private TOConnection connection;

    public TestSubMMS(TOConnection connection) {
        this.connection = connection;
    }

    @Override
    public List<Column> getColumns() {
        return Arrays.asList(connection.getBuilder().createColumn("item", CommonDataTypes.BIGINT), connection.getBuilder().createColumn("hey", CommonDataTypes.TEXT));
    }

    @Override
    public Map<Column, Object> getValues(Item item, Table table) {
        Map<Column, Object> objectMap = new HashMap<>();
        objectMap.put(table.getColumnByName("item"), item.getI());
        objectMap.put(table.getColumnByName("hey"), item.getS());
        return objectMap;
    }

    @Override
    public Item minorBuild(DBRow dbRows) {
        return new Item(dbRows.getRowItem("hey").getAsString(), dbRows.getRowItem("item").getAsInt());
    }

    @Override
    public void insert(Object o, Table table, Object parentID, Field field) {

    }

    @Override
    public Object build(DBResult dbResult, Field field) {
        return null;
    }

    @Override
    public Table createTable(String name, Field field, DataType parentDataType) {
        return null;
    }

    @Override
    public TOConnection getConnection() {
        return null;
    }
}
