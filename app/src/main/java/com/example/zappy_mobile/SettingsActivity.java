package com.example.zappy_mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private Button btnLogout, btnBack, btnToggleBrightnessSensor;
    private Switch switchNotifications;

    private SensorManager sensorManager;
    private Sensor lightSensor;
    private boolean brightnessSensorEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        btnLogout = findViewById(R.id.btnLogout);
        btnBack = findViewById(R.id.btnBack);
        switchNotifications = findViewById(R.id.switchNotifications);
        btnToggleBrightnessSensor = findViewById(R.id.btnToggleBrightnessSensor);

        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        boolean notifEnabled = prefs.getBoolean("notifications_enabled", true);
        switchNotifications.setChecked(notifEnabled);

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("notifications_enabled", isChecked);
            editor.apply();
        });

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        btnBack.setOnClickListener(v -> onBackPressed());

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        brightnessSensorEnabled = prefs.getBoolean("brightness_sensor_enabled", false);

        btnToggleBrightnessSensor.setText(
                brightnessSensorEnabled ? "Desactivar sensor de brillo" : "Activar sensor de brillo"
        );

        btnToggleBrightnessSensor.setOnClickListener(v -> {
            brightnessSensorEnabled = !brightnessSensorEnabled;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("brightness_sensor_enabled", brightnessSensorEnabled);
            editor.apply();

            btnToggleBrightnessSensor.setText(
                    brightnessSensorEnabled ? "Desactivar sensor de brillo" : "Activar sensor de brillo"
            );

            if (brightnessSensorEnabled) {
                sensorManager.registerListener(lightListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
                Toast.makeText(this, "Sensor de brillo activado", Toast.LENGTH_SHORT).show();
            } else {
                sensorManager.unregisterListener(lightListener);
                resetAppBrightness();
                Toast.makeText(this, "Sensor de brillo desactivado", Toast.LENGTH_SHORT).show();
            }
        });

        if (brightnessSensorEnabled) {
            sensorManager.registerListener(lightListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private final SensorEventListener lightListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float lux = event.values[0];
            float brightness = Math.min(1f, Math.max(0.1f, lux / 200f));

            WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
            layoutParams.screenBrightness = brightness;
            getWindow().setAttributes(layoutParams);


        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };

    private void resetAppBrightness() {
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        getWindow().setAttributes(layoutParams);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(lightListener);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
