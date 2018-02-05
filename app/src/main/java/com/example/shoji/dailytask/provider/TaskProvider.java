package com.example.shoji.dailytask.provider;

import android.net.Uri;

import com.example.shoji.dailytask.BuildConfig;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

@ContentProvider(
        authority = TaskProvider.AUTHORITY,
        database = TaskDatabase.class)
public class TaskProvider {
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider.tasks";

    @TableEndpoint(table = TaskDatabase.TABLE_NAME)
    public static class Tasks {
        public static final String PATH = TaskDatabase.TABLE_NAME;
        @ContentUri(
                path = PATH,
                type = "vnd.android.cursor.dir/" + PATH,
                defaultSort = TaskContract._ID + " ASC")
        public static final Uri CONTENT_URI = Uri.parse("content://" + "com.example.shoji.dailytask.provider.tasks" + "/" + PATH);

    }
}
