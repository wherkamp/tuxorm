package me.kingtux.tuxorm.serializers;

import me.kingtux.tuxjsql.core.builders.ColumnBuilder;
import me.kingtux.tuxjsql.core.response.DBColumnItem;
import me.kingtux.tuxjsql.core.response.DBRow;
import me.kingtux.tuxjsql.core.sql.SQLColumn;
import me.kingtux.tuxjsql.core.sql.SQLTable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * If it has multiple values like lists and maps use this.
 * Warning this defaults the SUbMMS methods
 *
 * @param <T> Type to Serialize
 */
public interface MultipleValueSerializer<T> extends MultiSecondarySerializer<T> {

    List<Object> contains(Object o, SQLTable table);


    //The defaults
    default List<ColumnBuilder> getColumns(String after) {
        return Collections.emptyList();
    }


    default Map<SQLColumn, Object> getValues(T t, SQLTable table, String s) {
        return Collections.emptyMap();
    }


    default T minorBuild(DBRow dbRows, String after) {
        return null;
    }


}
