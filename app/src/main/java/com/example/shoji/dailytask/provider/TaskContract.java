package com.example.shoji.dailytask.provider;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.ConflictResolutionType;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

public class TaskContract {
    /* internal database id */
    @DataType(DataType.Type.INTEGER)
    @PrimaryKey(onConflict = ConflictResolutionType.REPLACE)
    @AutoIncrement
    public static final String _ID = "_id";

    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String COLUMN_TITLE = "title";

    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String COLUMN_DESCRIPTION = "description";

    @DataType(DataType.Type.INTEGER)
    @NotNull
    public static final String COLUMN_PRIORITY = "priority";

    @DataType(DataType.Type.INTEGER)
    @NotNull
    public static final String COLUMN_IS_CONCLUDED = "is_concluded";

    @DataType(DataType.Type.INTEGER)
    @NotNull
    public static final String COLUMN_CONCLUDED_DATE = "concluded_date";




    public static int NOT_CONCLUDED = 0;
}
