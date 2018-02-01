package com.example.shoji.dailytask.provider;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

@Database(version = TaskDatabase.VERSION)
public class TaskDatabase {
    public static final int VERSION = 1;

    @Table(TaskContract.class)
    public static final String TABLE_NAME = "tasks";
}
