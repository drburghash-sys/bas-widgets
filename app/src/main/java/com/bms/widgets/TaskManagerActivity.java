package com.bms.widgets;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

public class TaskManagerActivity extends Activity {

    private EditText titleInput;
    private Button timeButton;
    private LinearLayout listContainer;
    private String selectedTime = "12:00";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(0xFF101318);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(28), dp(22), dp(30));
        scroll.addView(root);

        TextView title = text("المهام اليومية", 26, 0xFFFFFFFF);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title, fullWrap(0, 20));

        titleInput = new EditText(this);
        titleInput.setHint("اسم المهمة");
        titleInput.setHintTextColor(0xFF7D8793);
        titleInput.setTextColor(0xFFFFFFFF);
        titleInput.setSingleLine(true);
        titleInput.setBackgroundColor(0xFF1B212A);
        titleInput.setPadding(dp(12), 0, dp(12), 0);
        root.addView(titleInput, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(52)));

        timeButton = button("الوقت: 12:00");
        timeButton.setOnClickListener(v -> chooseTime());
        root.addView(timeButton, fullButton(10));

        Button add = button("إضافة المهمة");
        add.setOnClickListener(v -> addTask());
        root.addView(add, fullButton(8));

        TextView hint = text(
                "كل مهمة تتكرر يوميًا. عند الضغط على ✓ في الويدجت تُعتبر منجزة لهذا اليوم فقط.",
                13, 0xFF9DA6B2);
        hint.setGravity(Gravity.CENTER);
        root.addView(hint, fullWrap(14, 18));

        listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(listContainer, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        setContentView(scroll);
        renderTasks();
    }

    private void chooseTime() {
        LocalTime current;
        try { current = LocalTime.parse(selectedTime); }
        catch (Exception e) { current = LocalTime.NOON; }

        new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedTime = String.format(Locale.US, "%02d:%02d", hourOfDay, minute);
                    timeButton.setText("الوقت: " + selectedTime);
                },
                current.getHour(),
                current.getMinute(),
                true
        ).show();
    }

    private void addTask() {
        String title = titleInput.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "اكتب اسم المهمة أولًا", Toast.LENGTH_SHORT).show();
            return;
        }
        TaskStore.add(this, selectedTime, title);
        titleInput.setText("");
        renderTasks();
        refreshWidget();
    }

    private void renderTasks() {
        listContainer.removeAllViews();
        List<TaskStore.Task> tasks = TaskStore.getAll(this);

        if (tasks.isEmpty()) {
            TextView empty = text("لا توجد مهام بعد", 16, 0xFF9DA6B2);
            empty.setGravity(Gravity.CENTER);
            listContainer.addView(empty, fullWrap(18, 0));
            return;
        }

        for (TaskStore.Task task : tasks) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(12), dp(8), dp(12), dp(8));
            row.setBackgroundColor(0xFF1B212A);

            TextView value = text(task.time + "   " + task.title, 16,
                    task.isDoneToday() ? 0xFF6F7984 : 0xFFFFFFFF);
            value.setGravity(Gravity.RIGHT);
            value.setLayoutParams(new LinearLayout.LayoutParams(
                    0, dp(48), 1f));

            Button done = button(task.isDoneToday() ? "↺" : "✓");
            done.setTextSize(18);
            done.setOnClickListener(v -> {
                if (task.isDoneToday()) TaskStore.undoToday(this, task.id);
                else TaskStore.completeToday(this, task.id);
                renderTasks();
                refreshWidget();
            });

            Button delete = button("حذف");
            delete.setTextSize(13);
            delete.setOnClickListener(v -> {
                TaskStore.delete(this, task.id);
                renderTasks();
                refreshWidget();
            });

            row.addView(value);
            row.addView(done, new LinearLayout.LayoutParams(dp(54), dp(44)));
            row.addView(delete, new LinearLayout.LayoutParams(dp(66), dp(44)));

            LinearLayout.LayoutParams rp = fullWrap(0, 8);
            listContainer.addView(row, rp);
        }
    }

    private void refreshWidget() {
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        ComponentName provider = new ComponentName(this, TaskWidgetProvider.class);
        for (int id : manager.getAppWidgetIds(provider)) {
            TaskWidgetProvider.updateWidget(this, manager, id);
        }
    }

    private Button button(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        return b;
    }

    private TextView text(String value, int sizeSp, int color) {
        TextView v = new TextView(this);
        v.setText(value);
        v.setTextSize(sizeSp);
        v.setTextColor(color);
        return v;
    }

    private LinearLayout.LayoutParams fullButton(int top) {
        LinearLayout.LayoutParams p = fullWrap(top, 0);
        p.height = dp(52);
        return p;
    }

    private LinearLayout.LayoutParams fullWrap(int top, int bottom) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, dp(top), 0, dp(bottom));
        return p;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
