package me.kingtux.tuxorm.serializers.builtin;

import me.kingtux.tuxjsql.core.Column;
import me.kingtux.tuxjsql.core.CommonDataTypes;
import me.kingtux.tuxorm.TOConnection;
import me.kingtux.tuxorm.serializers.SingleSecondarySerializer;

import java.io.File;

public class FileSerializer implements SingleSecondarySerializer<File, String> {
    private TOConnection connection;

    public FileSerializer(TOConnection toConnection) {
        this.connection = toConnection;
    }

    @Override
    public String getSimplifiedValue(File o) {
        return o.getPath();
    }

    @Override
    public File buildFromSimplifiedValue(String value) {
        return new File(value);
    }

    @Override
    public Column createColumn(String name) {
        return connection.getBuilder().createColumn().name(name).type(CommonDataTypes.TEXT).build();
    }
}