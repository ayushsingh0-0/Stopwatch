package com.ayush.stopwatch;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private TextView timeDisplay;
    private TextView lapTimeDisplay;
    private TableLayout lapTable;
    private FloatingActionButton startButton;
    private FloatingActionButton lapButton;
    private FloatingActionButton resetButton;

    private boolean isRunning = false;
    private long startTime = 0L;
    private long elapsedTime = 0L;
    private long lastLapTime = 0L;
    private Timer timer;
    private Handler handler = new Handler();
    private ArrayList<String[]> lapTimes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timeDisplay = findViewById(R.id.timeDisplay);
        lapTimeDisplay = findViewById(R.id.lapTimeDisplay);
        lapTable = findViewById(R.id.lapTable);
        startButton = findViewById(R.id.startButton);
        lapButton = findViewById(R.id.lapButton);
        resetButton = findViewById(R.id.resetButton);

        startButton.setOnClickListener(v -> {
            if (isRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        lapButton.setOnClickListener(v -> recordLap());
        resetButton.setOnClickListener(v -> resetTimer());

        lapButton.setEnabled(false);
        resetButton.setEnabled(false);
    }

    private void startTimer() {
        if (!isRunning) {
            startTime = SystemClock.elapsedRealtime() - elapsedTime;
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    elapsedTime = SystemClock.elapsedRealtime() - startTime;
                    updateDisplay(elapsedTime);
                }
            }, 0, 10);
            isRunning = true;
            startButton.setImageResource(R.drawable.ic_pause);
            lapButton.setEnabled(true);
            resetButton.setEnabled(true);
        }
    }

    private void pauseTimer() {
        if (timer != null) {
            timer.cancel();
        }
        isRunning = false;
        startButton.setImageResource(R.drawable.ic_play);
        lapButton.setEnabled(false);
    }

    private void resetTimer() {
        if (timer != null) {
            timer.cancel();
        }
        elapsedTime = 0L;
        lastLapTime = 0L;
        updateDisplay(0);
        isRunning = false;
        startButton.setImageResource(R.drawable.ic_play);
        lapButton.setEnabled(false);
        resetButton.setEnabled(false);
        lapTimes.clear();
        updateLapTable();
    }

    private void recordLap() {
        if (isRunning) {
            long lapTime = elapsedTime - lastLapTime;
            lastLapTime = elapsedTime;
            String[] lapData = {
                    String.format("%02d", lapTimes.size() + 1),
                    formatTime(lapTime),
                    formatTime(elapsedTime)
            };
            lapTimes.add(0, lapData);
            updateLapTable();
        }
    }

    private void updateDisplay(final long time) {
        handler.post(() -> {
            timeDisplay.setText(formatTime(time));
            lapTimeDisplay.setText(formatTime(time - lastLapTime));
        });
    }

    private void updateLapTable() {
        handler.post(() -> {
            lapTable.removeViews(1, lapTable.getChildCount() - 1);
            for (String[] lap : lapTimes) {
                TableRow row = new TableRow(this);
                for (String data : lap) {
                    TextView textView = new TextView(this);
                    textView.setText(data);
                    textView.setPadding(8, 8, 8, 8);
                    row.addView(textView);
                }
                lapTable.addView(row);
            }
        });
    }

    private String formatTime(long time) {
        int minutes = (int) (time / 60000);
        int seconds = (int) ((time / 1000) % 60);
        int hundredths = (int) ((time / 10) % 100);
        return String.format("%02d:%02d.%02d", minutes, seconds, hundredths);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
}