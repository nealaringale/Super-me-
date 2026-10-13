package com.nealaringale.superme;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Super Me is an original, offline-first habit, activity-time and fasting tracker.
 * The UI and data model are implemented here rather than copied from either input APK.
 */
public class MainActivity extends Activity {
    private static final int BG = Color.rgb(12, 19, 24);
    private static final int SURFACE = Color.rgb(22, 32, 39);
    private static final int SURFACE_2 = Color.rgb(29, 43, 50);
    private static final int TEXT = Color.rgb(241, 247, 246);
    private static final int MUTED = Color.rgb(155, 174, 177);
    private static final int GREEN = Color.rgb(112, 235, 177);
    private static final int CYAN = Color.rgb(126, 220, 232);
    private static final int ORANGE = Color.rgb(255, 190, 112);
    private static final String PREFS = "super_me_local_v1";
    private static final String KEY_HABITS = "habits_json";
    private static final String KEY_DAY = "day_key";
    private static final String KEY_FOCUS_SECONDS = "focus_seconds_today";
    private static final String KEY_FOCUS_START = "focus_start_ms";
    private static final String KEY_FOCUS_LABEL = "focus_label";
    private static final String KEY_FAST_START = "fast_start_ms";
    private static final String KEY_FAST_HOURS = "fast_hours";
    private static final long HOUR_MS = 60L * 60L * 1000L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<Habit> habits = new ArrayList<>();

    private ScrollView scrollView;
    private TextView habitSummary;
    private TextView progressPercent;
    private TextView progressCaption;
    private TextView focusStatus;
    private TextView focusElapsed;
    private TextView fastingStatus;
    private TextView fastingSubstatus;
    private Button focusStartButton;
    private Button focusStopButton;
    private Button fastToggleButton;
    private LinearLayout habitsContainer;
    private LinearLayout protocolRow;

    private String currentDayKey;
    private long focusSecondsToday;
    private long focusStartMs;
    private String focusLabel = "Focus session";
    private long fastStartMs;
    private int fastHours = 16;

    private final Runnable ticker = new Runnable() {
        @Override public void run() {
            boolean dayChanged = checkDayRollover();
            if (dayChanged) {
                renderScreen();
            } else {
                updateDynamicLabels();
            }
            handler.postDelayed(this, 1000L);
        }
    };

    private static final class Habit {
        final String id;
        String name;
        boolean done;

        Habit(String id, String name, boolean done) {
            this.id = id;
            this.name = name;
            this.done = done;
        }
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setStatusBarColor(BG);
        window.setNavigationBarColor(BG);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.getDecorView().setSystemUiVisibility(0);
        loadState();
        renderScreen();
        handler.post(ticker);
    }

    @Override protected void onDestroy() {
        handler.removeCallbacks(ticker);
        super.onDestroy();
    }

    private void loadState() {
        android.content.SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String today = dayKey(System.currentTimeMillis());
        currentDayKey = prefs.getString(KEY_DAY, today);
        focusSecondsToday = prefs.getLong(KEY_FOCUS_SECONDS, 0L);
        focusStartMs = prefs.getLong(KEY_FOCUS_START, 0L);
        focusLabel = prefs.getString(KEY_FOCUS_LABEL, "Focus session");
        fastStartMs = prefs.getLong(KEY_FAST_START, 0L);
        fastHours = prefs.getInt(KEY_FAST_HOURS, 16);

        if (prefs.contains(KEY_HABITS)) {
            try {
                JSONArray array = new JSONArray(prefs.getString(KEY_HABITS, "[]"));
                for (int i = 0; i < array.length(); i++) {
                    JSONObject item = array.getJSONObject(i);
                    String name = item.optString("name", "").trim();
                    if (!name.isEmpty()) {
                        habits.add(new Habit(item.optString("id", Integer.toString(i)), name,
                                item.optBoolean("done", false)));
                    }
                }
            } catch (JSONException ignored) {
                habits.clear();
            }
        }
        if (habits.isEmpty() && !prefs.contains(KEY_HABITS)) {
            habits.add(new Habit("h1", "Morning walk", false));
            habits.add(new Habit("h2", "Study / deep work", false));
            habits.add(new Habit("h3", "Read 10 pages", false));
            habits.add(new Habit("h4", "Stretch or mobility", false));
        }
        if (!today.equals(currentDayKey)) {
            currentDayKey = today;
            focusSecondsToday = 0L;
            for (Habit habit : habits) habit.done = false;
            persistState();
            persistFocus();
        }
        if (fastHours != 12 && fastHours != 14 && fastHours != 16 && fastHours != 18 && fastHours != 20) {
            fastHours = 16;
        }
        persistState();
    }

    /** @return true if a new local calendar day began while the activity was open. */
    private boolean checkDayRollover() {
        String today = dayKey(System.currentTimeMillis());
        if (today.equals(currentDayKey)) return false;
        currentDayKey = today;
        focusSecondsToday = 0L;
        for (Habit habit : habits) habit.done = false;
        persistState();
        persistFocus();
        return true;
    }

    private String dayKey(long time) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date(time));
    }

    private String todayLabel() {
        return new SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(new Date());
    }

    private void persistState() {
        JSONArray array = new JSONArray();
        try {
            for (Habit habit : habits) {
                JSONObject item = new JSONObject();
                item.put("id", habit.id);
                item.put("name", habit.name);
                item.put("done", habit.done);
                array.put(item);
            }
        } catch (JSONException ignored) {
            // The fields above are supported JSON types; this is a defensive fallback.
        }
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putString(KEY_HABITS, array.toString())
                .putString(KEY_DAY, currentDayKey)
                .putInt(KEY_FAST_HOURS, fastHours)
                .apply();
    }

    private void persistFocus() {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putLong(KEY_FOCUS_SECONDS, focusSecondsToday)
                .putLong(KEY_FOCUS_START, focusStartMs)
                .putString(KEY_FOCUS_LABEL, focusLabel)
                .apply();
    }

    private void persistFast() {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putLong(KEY_FAST_START, fastStartMs)
                .putInt(KEY_FAST_HOURS, fastHours)
                .apply();
    }

    private void renderScreen() {
        int scrollY = scrollView == null ? 0 : scrollView.getScrollY();
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(20), dp(16), dp(20), dp(28));
        page.setBackgroundColor(BG);

        LinearLayout brandRow = new LinearLayout(this);
        brandRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView logo = text("SM", 16, GREEN, true);
        logo.setGravity(Gravity.CENTER);
        logo.setBackground(roundRect(SURFACE_2, dp(14)));
        brandRow.addView(logo, new LinearLayout.LayoutParams(dp(48), dp(48)));
        LinearLayout brandText = new LinearLayout(this);
        brandText.setOrientation(LinearLayout.VERTICAL);
        brandText.setPadding(dp(12), 0, 0, 0);
        brandText.addView(text("SUPER ME", 17, TEXT, true));
        brandText.addView(text("BUILD YOUR EVERYDAY", 10, MUTED, true));
        brandRow.addView(brandText, new LinearLayout.LayoutParams(0, -2, 1));
        page.addView(brandRow, margin(0, 0, 0, 24));
        page.addView(text(todayLabel().toUpperCase(Locale.getDefault()), 11, MUTED, true), margin(0, 0, 0, 12));

        LinearLayout hero = card();
        hero.setBackground(roundRect(0xFF172B2B, dp(24)));
        hero.addView(text("Small wins.\nStronger you.", 30, TEXT, true), margin(0, 0, 0, 10));
        hero.addView(text("Your habits, tracked time and fasting window — all in one calm dashboard.", 14, MUTED, false), margin(0, 0, 0, 20));
        progressPercent = text("0%", 25, GREEN, true);
        hero.addView(progressPercent, margin(0, 0, 0, 2));
        progressCaption = text("Daily habit progress", 12, MUTED, false);
        hero.addView(progressCaption);
        page.addView(hero, margin(0, 0, 0, 14));

        LinearLayout summary = new LinearLayout(this);
        summary.setOrientation(LinearLayout.HORIZONTAL);
        summary.addView(summaryCard("HABITS", "0 / 0", "completed today", GREEN), weightedMargin(1, 0, 8, 0, 0));
        summary.addView(summaryCard("TIME TRACKED", "0m", "activity today", CYAN), weightedMargin(1, 8, 0, 0, 0));
        page.addView(summary, margin(0, 0, 0, 18));

        LinearLayout habitsCard = card();
        LinearLayout head = new LinearLayout(this);
        head.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout headText = new LinearLayout(this);
        headText.setOrientation(LinearLayout.VERTICAL);
        headText.addView(text("Daily habits", 19, TEXT, true));
        habitSummary = text("Make today count", 12, MUTED, false);
        headText.addView(habitSummary, margin(0, 3, 0, 0));
        head.addView(headText, new LinearLayout.LayoutParams(0, -2, 1));
        Button addButton = button("+ Add", SURFACE_2, GREEN, 12);
        addButton.setOnClickListener(v -> showAddHabitDialog());
        head.addView(addButton, new LinearLayout.LayoutParams(dp(76), dp(40)));
        habitsCard.addView(head, margin(0, 0, 0, 14));
        habitsContainer = new LinearLayout(this);
        habitsContainer.setOrientation(LinearLayout.VERTICAL);
        habitsCard.addView(habitsContainer);
        page.addView(habitsCard, margin(0, 0, 0, 14));

        LinearLayout focusCard = card();
        focusCard.addView(sectionKicker("TRACK IT", CYAN), margin(0, 0, 0, 7));
        focusCard.addView(text("Time tracking", 19, TEXT, true), margin(0, 0, 0, 4));
        focusCard.addView(text("Keep a record of time spent on the things that matter.", 13, MUTED, false), margin(0, 0, 0, 14));
        focusStatus = text("Ready when you are", 15, TEXT, true);
        focusCard.addView(focusStatus, margin(0, 0, 0, 5));
        focusElapsed = text("0m tracked today", 12, MUTED, false);
        focusCard.addView(focusElapsed, margin(0, 0, 0, 14));
        LinearLayout focusButtons = new LinearLayout(this);
        focusButtons.setOrientation(LinearLayout.HORIZONTAL);
        focusStartButton = button("Start session", GREEN, BG, 13);
        focusStartButton.setOnClickListener(v -> showStartFocusDialog());
        focusStopButton = button("Finish session", SURFACE_2, TEXT, 13);
        focusStopButton.setOnClickListener(v -> finishFocusSession());
        focusButtons.addView(focusStartButton, weightedMargin(1, 0, 0, 6, 0));
        focusButtons.addView(focusStopButton, weightedMargin(1, 6, 0, 0, 0));
        focusCard.addView(focusButtons);
        page.addView(focusCard, margin(0, 0, 0, 14));

        LinearLayout fastingCard = card();
        fastingCard.addView(sectionKicker("FASTING TRACKER", ORANGE), margin(0, 0, 0, 7));
        fastingCard.addView(text("Fasting window", 19, TEXT, true), margin(0, 0, 0, 4));
        fastingCard.addView(text("Choose a target duration and track elapsed time locally.", 13, MUTED, false), margin(0, 0, 0, 15));
        fastingCard.addView(text("YOUR TARGET", 10, MUTED, true), margin(0, 0, 0, 8));
        protocolRow = new LinearLayout(this);
        protocolRow.setOrientation(LinearLayout.HORIZONTAL);
        int[] protocols = {12, 14, 16, 18, 20};
        for (int protocol : protocols) {
            Button chip = button(protocol + "h", fastHours == protocol ? GREEN : SURFACE_2,
                    fastHours == protocol ? BG : TEXT, 11);
            chip.setOnClickListener(v -> {
                if (fastStartMs != 0L) {
                    toast("Finish the current fast before changing its target.");
                    return;
                }
                fastHours = protocol;
                persistFast();
                renderScreen();
            });
            LinearLayout.LayoutParams chipParams = new LinearLayout.LayoutParams(0, dp(42), 1f);
            if (protocol != 12) chipParams.leftMargin = dp(5);
            protocolRow.addView(chip, chipParams);
        }
        fastingCard.addView(protocolRow, margin(0, 0, 0, 16));
        fastingStatus = text("No active fast", 17, TEXT, true);
        fastingCard.addView(fastingStatus, margin(0, 0, 0, 5));
        fastingSubstatus = text("A timer only — choose a routine that is appropriate for you.", 12, MUTED, false);
        fastingCard.addView(fastingSubstatus, margin(0, 0, 0, 14));
        fastToggleButton = button(fastStartMs == 0L ? "Start fast" : "End fast", fastStartMs == 0L ? ORANGE : SURFACE_2, fastStartMs == 0L ? BG : TEXT, 13);
        fastToggleButton.setOnClickListener(v -> toggleFast());
        fastingCard.addView(fastToggleButton, new LinearLayout.LayoutParams(-1, dp(46)));
        page.addView(fastingCard, margin(0, 0, 0, 14));

        TextView privacy = text("PRIVATE BY DEFAULT  ·  YOUR DATA STAYS ON THIS DEVICE", 10, MUTED, true);
        privacy.setGravity(Gravity.CENTER);
        privacy.setPadding(dp(4), dp(10), dp(4), dp(8));
        page.addView(privacy);

        scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setClipToPadding(false);
        scrollView.addView(page);
        setContentView(scrollView);
        if (scrollY > 0) scrollView.post(() -> scrollView.scrollTo(0, scrollY));

        refreshHabitList();
        updateDynamicLabels();
    }

    private LinearLayout summaryCard(String title, String value, String caption, int accent) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(15), dp(15), dp(12), dp(15));
        box.setBackground(roundRect(SURFACE, dp(18)));
        box.addView(text(title, 10, MUTED, true), margin(0, 0, 0, 10));
        TextView val = text(value, 22, accent, true);
        box.addView(val, margin(0, 0, 0, 3));
        box.addView(text(caption, 11, MUTED, false));
        box.setTag(title);
        return box;
    }

    private void refreshHabitList() {
        if (habitsContainer == null) return;
        habitsContainer.removeAllViews();
        for (Habit habit : habits) {
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(dp(11), dp(8), dp(7), dp(8));
            row.setBackground(roundRect(habit.done ? 0xFF18352E : SURFACE_2, dp(14)));
            Button check = button(habit.done ? "✓" : "○", habit.done ? GREEN : SURFACE, habit.done ? BG : MUTED, 15);
            check.setContentDescription(habit.done ? "Mark habit incomplete" : "Mark habit complete");
            check.setOnClickListener(v -> {
                habit.done = !habit.done;
                persistState();
                refreshHabitList();
                updateDynamicLabels();
            });
            row.addView(check, new LinearLayout.LayoutParams(dp(42), dp(42)));
            TextView name = text(habit.name, 14, habit.done ? GREEN : TEXT, true);
            name.setPadding(dp(11), 0, dp(4), 0);
            row.addView(name, new LinearLayout.LayoutParams(0, -2, 1));
            Button remove = button("×", SURFACE, MUTED, 18);
            remove.setOnClickListener(v -> showRemoveHabitDialog(habit));
            row.addView(remove, new LinearLayout.LayoutParams(dp(38), dp(38)));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
            params.bottomMargin = dp(7);
            habitsContainer.addView(row, params);
        }
        if (habits.isEmpty()) {
            habitsContainer.addView(text("No habits yet. Add one small win to get started.", 13, MUTED, false));
        }
    }

    private void updateDynamicLabels() {
        int completed = 0;
        for (Habit habit : habits) if (habit.done) completed++;
        int total = habits.size();
        int percent = total == 0 ? 0 : Math.round((completed * 100f) / total);
        if (progressPercent != null) progressPercent.setText(percent + "%");
        if (progressCaption != null) progressCaption.setText("Daily habit progress  ·  " + completed + " of " + total + " complete");
        if (habitSummary != null) habitSummary.setText(completed + " of " + total + " completed today");

        if (focusStartButton != null) {
            focusStartButton.setEnabled(focusStartMs == 0L);
            focusStartButton.setAlpha(focusStartMs == 0L ? 1f : 0.48f);
        }
        if (focusStopButton != null) {
            focusStopButton.setEnabled(focusStartMs > 0L);
            focusStopButton.setAlpha(focusStartMs > 0L ? 1f : 0.48f);
        }
        if (fastToggleButton != null) {
            fastToggleButton.setText(fastStartMs == 0L ? "Start fast" : "End fast");
            fastToggleButton.setBackground(roundRect(fastStartMs == 0L ? ORANGE : SURFACE_2, dp(13)));
            fastToggleButton.setTextColor(fastStartMs == 0L ? BG : TEXT);
        }
        if (focusStatus != null) {
            if (focusStartMs > 0L) {
                focusStatus.setText("IN SESSION  ·  " + focusLabel);
                focusStatus.setTextColor(CYAN);
            } else {
                focusStatus.setText("Ready when you are");
                focusStatus.setTextColor(TEXT);
            }
        }
        if (focusElapsed != null) {
            long tracked = focusSecondsToday;
            if (focusStartMs > 0L) {
                long dayStart = startOfTodayMs();
                long start = Math.max(focusStartMs, dayStart);
                tracked += Math.max(0L, (System.currentTimeMillis() - start) / 1000L);
            }
            focusElapsed.setText(formatDuration(tracked) + " tracked today");
        }
        if (fastingStatus != null) {
            if (fastStartMs == 0L) {
                fastingStatus.setText("No active fast");
                fastingStatus.setTextColor(TEXT);
                fastingSubstatus.setText(fastHours + "-hour target ready whenever you are.");
            } else {
                long end = fastStartMs + fastHours * HOUR_MS;
                long now = System.currentTimeMillis();
                if (now >= end) {
                    long elapsedHours = Math.max(0L, (now - fastStartMs) / HOUR_MS);
                    fastingStatus.setText("Target reached  ·  " + fastHours + "h");
                    fastingStatus.setTextColor(GREEN);
                    fastingSubstatus.setText("Elapsed " + elapsedHours + "h. End the timer when you're ready.");
                } else {
                    long mins = Math.max(0L, (end - now + 59999L) / 60000L);
                    long h = mins / 60L;
                    long m = mins % 60L;
                    fastingStatus.setText(String.format(Locale.getDefault(), "%02dh %02dm remaining", h, m));
                    fastingStatus.setTextColor(ORANGE);
                    fastingSubstatus.setText("Started " + new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date(fastStartMs)) + " · " + fastHours + "h goal");
                }
            }
        }
        updateSummaryCardValues();
    }

    private void updateSummaryCardValues() {
        if (scrollView == null) return;
        updateSummaryValue("HABITS", countDone() + " / " + habits.size());
        long tracked = focusSecondsToday;
        if (focusStartMs > 0L) tracked += Math.max(0L, (System.currentTimeMillis() - Math.max(focusStartMs, startOfTodayMs())) / 1000L);
        updateSummaryValue("TIME TRACKED", formatDuration(tracked));
    }

    private int countDone() {
        int count = 0;
        for (Habit habit : habits) if (habit.done) count++;
        return count;
    }

    private void updateSummaryValue(String title, String value) {
        if (scrollView == null) return;
        // Find the matching summary card by its private tag; this avoids replacing the screen every second.
        View pageView = scrollView.getChildAt(0);
        if (!(pageView instanceof LinearLayout)) return;
        LinearLayout page = (LinearLayout) pageView;
        for (int i = 0; i < page.getChildCount(); i++) {
            View child = page.getChildAt(i);
            if (!(child instanceof LinearLayout)) continue;
            LinearLayout maybeRow = (LinearLayout) child;
            for (int j = 0; j < maybeRow.getChildCount(); j++) {
                View card = maybeRow.getChildAt(j);
                if (card instanceof LinearLayout && title.equals(card.getTag())) {
                    LinearLayout cardLayout = (LinearLayout) card;
                    if (cardLayout.getChildCount() > 1 && cardLayout.getChildAt(1) instanceof TextView) {
                        ((TextView) cardLayout.getChildAt(1)).setText(value);
                    }
                    return;
                }
            }
        }
    }

    private void showAddHabitDialog() {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint("e.g. 20 minute walk");
        input.setTextColor(TEXT);
        input.setHintTextColor(MUTED);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        int pad = dp(20);
        input.setPadding(pad, dp(12), pad, dp(12));
        new AlertDialog.Builder(this)
                .setTitle("Add a habit")
                .setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Add habit", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        toast("Enter a habit name first.");
                        return;
                    }
                    habits.add(new Habit("h" + System.currentTimeMillis(), name, false));
                    persistState();
                    renderScreen();
                })
                .show();
    }

    private void showRemoveHabitDialog(Habit habit) {
        new AlertDialog.Builder(this)
                .setTitle("Remove habit?")
                .setMessage("Remove “" + habit.name + "” from your list?")
                .setNegativeButton("Keep it", null)
                .setPositiveButton("Remove", (dialog, which) -> {
                    habits.remove(habit);
                    persistState();
                    renderScreen();
                })
                .show();
    }

    private void showStartFocusDialog() {
        if (focusStartMs > 0L) {
            toast("A session is already running.");
            return;
        }
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setText(focusLabel.equals("Focus session") ? "Focus session" : focusLabel);
        input.setHint("What are you working on?");
        input.setTextColor(TEXT);
        input.setHintTextColor(MUTED);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setPadding(dp(20), dp(12), dp(20), dp(12));
        new AlertDialog.Builder(this)
                .setTitle("Start tracking time")
                .setMessage("Give this session a name.")
                .setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Start", (dialog, which) -> {
                    String label = input.getText().toString().trim();
                    focusLabel = label.isEmpty() ? "Focus session" : label;
                    focusStartMs = System.currentTimeMillis();
                    persistFocus();
                    updateDynamicLabels();
                })
                .show();
    }

    private void finishFocusSession() {
        if (focusStartMs == 0L) {
            toast("No activity session is running.");
            return;
        }
        long now = System.currentTimeMillis();
        long effectiveStart = Math.max(focusStartMs, startOfTodayMs());
        focusSecondsToday += Math.max(0L, (now - effectiveStart) / 1000L);
        focusStartMs = 0L;
        persistFocus();
        updateDynamicLabels();
        toast("Session saved to today's time total.");
    }

    private long startOfTodayMs() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private void toggleFast() {
        if (fastStartMs == 0L) {
            fastStartMs = System.currentTimeMillis();
            persistFast();
            updateDynamicLabels();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("End this fast?")
                .setMessage("End the timer and reset the current fasting window?")
                .setNegativeButton("Keep running", null)
                .setPositiveButton("End fast", (dialog, which) -> {
                    fastStartMs = 0L;
                    persistFast();
                    updateDynamicLabels();
                    toast("Fasting timer ended.");
                })
                .show();
    }

    private TextView sectionKicker(String label, int color) {
        return text(label, 10, color, true);
    }

    private TextView text(String value, int sizeSp, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sizeSp);
        view.setTextColor(color);
        view.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
        view.setIncludeFontPadding(true);
        return view;
    }

    private Button button(String label, int background, int foreground, int sizeSp) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(sizeSp);
        button.setTextColor(foreground);
        button.setAllCaps(false);
        button.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        button.setMinHeight(0);
        button.setMinimumHeight(0);
        button.setPadding(dp(8), dp(5), dp(8), dp(5));
        button.setBackground(roundRect(background, dp(13)));
        button.setStateListAnimator(null);
        return button;
    }

    private LinearLayout card() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(17), dp(18), dp(17), dp(17));
        box.setBackground(roundRect(SURFACE, dp(22)));
        return box;
    }

    private GradientDrawable roundRect(int color, int radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(radius);
        return shape;
    }

    private LinearLayout.LayoutParams margin(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.leftMargin = dp(left);
        params.topMargin = dp(top);
        params.rightMargin = dp(right);
        params.bottomMargin = dp(bottom);
        return params;
    }

    private LinearLayout.LayoutParams weightedMargin(float weight, int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -1, weight);
        params.leftMargin = dp(left);
        params.topMargin = dp(top);
        params.rightMargin = dp(right);
        params.bottomMargin = dp(bottom);
        return params;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private String formatDuration(long seconds) {
        seconds = Math.max(0L, seconds);
        long hours = seconds / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        if (hours > 0) return String.format(Locale.getDefault(), "%dh %02dm", hours, minutes);
        return String.format(Locale.getDefault(), "%dm", minutes);
    }

    private void toast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }
}
