package com.hugdev.webapper;

import static android.app.usage.UsageEvents.Event.NONE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.material.color.DynamicColors;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;

public class tuto_3 extends AppCompatActivity {
    private int selectedTheme;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int savedTheme = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getInt("selected_theme", NONE);
        if (savedTheme != NONE) {
            selectedTheme = savedTheme;
        }
        setTheme(selectedTheme);
        DynamicColors.applyToActivitiesIfAvailable(this.getApplication());
        setContentView(R.layout.tuto_3);

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
        Button button = findViewById(R.id.button4);
        button.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstStart", false);
            editor.apply();
            Intent intent = new Intent(tuto_3.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

    }

}