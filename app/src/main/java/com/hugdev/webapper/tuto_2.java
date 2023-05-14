package com.hugdev.webapper;

import static android.app.usage.UsageEvents.Event.NONE;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.material.color.DynamicColors;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;

public class tuto_2 extends AppCompatActivity {
    private int selectedTheme;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int savedTheme = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getInt("selected_theme", NONE);
        if (savedTheme != NONE) {
            selectedTheme = savedTheme;
        }
        setTheme(selectedTheme);
        DynamicColors.applyIfAvailable(this);
        setContentView(R.layout.tuto_2);
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(this);

// Returns an intent object that you use to check for an update.
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

// Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE);
            }// This example applies an immediate update. To apply a flexible update
// instead, pass in AppUpdateType.FLEXIBLE
// Request the update.
        });
        Button btn = findViewById(R.id.button_example_show);
        Button btn1 = findViewById(R.id.button4);
        btn.setOnClickListener(view -> {
            if (btn1.getVisibility() == View.VISIBLE) {
                btn1.setVisibility(View.INVISIBLE);
            } else {
                btn1.setVisibility(View.VISIBLE);
            }
        });
        btn1.setOnClickListener(v -> {
            Intent intent = new Intent(tuto_2.this, tuto_3.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }
    }


}