package com.bms.widgets;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;

import java.nio.charset.StandardCharsets;

public class TaskSyncActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handle(getIntent() == null ? null : getIntent().getData());
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        handle(intent == null ? null : intent.getData());
        finish();
        overridePendingTransition(0, 0);
    }

    private void handle(Uri uri) {
        if (uri == null
                || !"baswidgets".equals(uri.getScheme())
                || !"tasks".equals(uri.getHost())) return;

        String encoded = uri.getQueryParameter("data");
        if (encoded == null || encoded.isEmpty()) return;

        try {
            byte[] bytes = Base64.decode(encoded, Base64.DEFAULT);
            String json = new String(bytes, StandardCharsets.UTF_8);
            MofakkiratiStore.saveSnapshot(this, json);
            refreshTaskWidgets();
        } catch (Exception ignored) {}
    }

    private void refreshTaskWidgets() {
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        ComponentName provider = new ComponentName(this, TaskWidgetProvider.class);
        for (int id : manager.getAppWidgetIds(provider)) {
            TaskWidgetProvider.updateWidget(this, manager, id);
        }
    }
}
