package com.calltracker.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private TextView tvDate, tvDayLabel, tvCountIn, tvCountOut, tvCountMiss, tvTotal;
    private RecyclerView rvHistory;
    private TextView tvEmptyState;
    private LinearLayout layoutPermission;
    private TextView btnGrantPermission;
    private CallLogAdapter adapter;
    private List<CallEntry> callEntries = new ArrayList<>();
    private Calendar currentDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currentDay = Calendar.getInstance();
        normalizeDay(currentDay);
        initViews();
        setupListeners();
        checkPermissionAndLoad();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasPermission()) loadCallLogs();
    }

    private void initViews() {
        tvDate = findViewById(R.id.tv_date);
        tvDayLabel = findViewById(R.id.tv_day_label);
        tvCountIn = findViewById(R.id.tv_count_in);
        tvCountOut = findViewById(R.id.tv_count_out);
        tvCountMiss = findViewById(R.id.tv_count_miss);
        tvTotal = findViewById(R.id.tv_total);
        findViewById(R.id.btn_prev_day).setOnClickListener(v -> { currentDay.add(Calendar.DAY_OF_YEAR, -1); loadCallLogs(); });
        findViewById(R.id.btn_next_day).setOnClickListener(v -> {
            Calendar today = Calendar.getInstance(); normalizeDay(today);
            if (currentDay.before(today)) { currentDay.add(Calendar.DAY_OF_YEAR, 1); loadCallLogs(); }
        });
        findViewById(R.id.btn_today).setOnClickListener(v -> { currentDay = Calendar.getInstance(); normalizeDay(currentDay); loadCallLogs(); });
        rvHistory = findViewById(R.id.rv_history);
        tvEmptyState = findViewById(R.id.tv_empty_state);
        layoutPermission = findViewById(R.id.layout_permission);
        btnGrantPermission = findViewById(R.id.btn_grant_permission);
        adapter = new CallLogAdapter(callEntries);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(adapter);
    }

    private void setupListeners() {
        btnGrantPermission.setOnClickListener(v -> requestPermission());
    }

    private boolean hasPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED;
    }

    private void checkPermissionAndLoad() {
        if (hasPermission()) { layoutPermission.setVisibility(View.GONE); loadCallLogs(); }
        else { layoutPermission.setVisibility(View.VISIBLE); requestPermission(); }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_PHONE_STATE }, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            layoutPermission.setVisibility(View.GONE);
            loadCallLogs();
        }
    }

    private void normalizeDay(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);
    }

    private void loadCallLogs() {
        callEntries.clear();
        Calendar startCal = (Calendar) currentDay.clone();
        Calendar endCal = (Calendar) currentDay.clone();
        endCal.set(Calendar.HOUR_OF_DAY, 23); endCal.set(Calendar.MINUTE, 59); endCal.set(Calendar.SECOND, 59);
        int countIn = 0, countOut = 0, countMiss = 0;
        try {
            String[] projection = { CallLog.Calls.NUMBER, CallLog.Calls.TYPE, CallLog.Calls.DATE, CallLog.Calls.DURATION, CallLog.Calls.CACHED_NAME };
            Cursor cursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, projection,
                CallLog.Calls.DATE + " >= ? AND " + CallLog.Calls.DATE + " <= ?",
                new String[]{ String.valueOf(startCal.getTimeInMillis()), String.valueOf(endCal.getTimeInMillis()) },
                CallLog.Calls.DATE + " DESC");
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String number = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
                    int type = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE));
                    long date = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE));
                    int duration = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME));
                    String callType;
                    switch (type) {
                        case CallLog.Calls.INCOMING_TYPE: callType = "in"; countIn++; break;
                        case CallLog.Calls.OUTGOING_TYPE: callType = "out"; countOut++; break;
                        case CallLog.Calls.MISSED_TYPE: callType = "miss"; countMiss++; break;
                        default: callType = "other"; break;
                    }
                    callEntries.add(new CallEntry((name != null && !name.isEmpty()) ? name : number, callType, date, duration));
                }
                cursor.close();
            }
        } catch (Exception e) { Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show(); }
        updateUI(countIn, countOut, countMiss);
    }

    private void updateUI(int countIn, int countOut, int countMiss) {
        Calendar today = Calendar.getInstance(); normalizeDay(today);
        Calendar yesterday = Calendar.getInstance(); yesterday.add(Calendar.DAY_OF_YEAR, -1); normalizeDay(yesterday);
        SimpleDateFormat dayFmt = new SimpleDateFormat("EEEE dd MMMM yyyy", Locale.FRENCH);
        SimpleDateFormat shortFmt = new SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH);
        if (currentDay.equals(today)) tvDate.setText("Aujourd'hui");
        else if (currentDay.equals(yesterday)) tvDate.setText("Hier");
        else tvDate.setText(shortFmt.format(currentDay.getTime()));
        tvDayLabel.setText(dayFmt.format(currentDay.getTime()));
        tvCountIn.setText(String.valueOf(countIn));
        tvCountOut.setText(String.valueOf(countOut));
        tvCountMiss.setText(String.valueOf(countMiss));
        int total = countIn + countOut + countMiss;
        tvTotal.setText(total + " appel" + (total != 1 ? "s" : ""));
        adapter.notifyDataSetChanged();
        tvEmptyState.setVisibility(callEntries.isEmpty() ? View.VISIBLE : View.GONE);
        rvHistory.setVisibility(callEntries.isEmpty() ? View.GONE : View.VISIBLE);
    }
}
