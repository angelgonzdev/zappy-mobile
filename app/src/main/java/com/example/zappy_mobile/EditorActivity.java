package com.example.zappy_mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class EditorActivity extends AppCompatActivity {

    private LinearLayout btnHome, btnLibrary, btnEdit, btnProfile;
    private ImageView btnSettings;

    private LinearLayout addContentCard;

    // Sensores
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private boolean brightnessSensorEnabled = false;

    private float currentLux = 0f;
    private Handler luxHandler = new Handler();
    private Runnable luxRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_comic);

        btnHome = findViewById(R.id.btnHome);
        btnLibrary = findViewById(R.id.btnLibrary);
        btnEdit = findViewById(R.id.btnEdit);
        btnProfile = findViewById(R.id.btnProfile);
        btnSettings = findViewById(R.id.btnSettings);

        addContentCard = findViewById(R.id.addContentCard);

        // CÓDIGO MODIFICADO PARA ABRIR CREATECOMICACTIVITY
        addContentCard.setOnClickListener(v -> {
            Intent intent = new Intent(EditorActivity.this, CreateComicActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // Footer navigation
        btnHome.setOnClickListener(v ->
                startActivity(new Intent(EditorActivity.this, HomeActivity.class))
        );

        btnLibrary.setOnClickListener(v ->
                startActivity(new Intent(EditorActivity.this, LibraryActivity.class))
        );

        btnEdit.setOnClickListener(v ->
                Toast.makeText(this, "Ya estás en el editor ✏️", Toast.LENGTH_SHORT).show()
        );

        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(EditorActivity.this, ProfileActivity.class))
        );

        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(EditorActivity.this, SettingsActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // ====== Sensor de luz ======
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        brightnessSensorEnabled = prefs.getBoolean("brightness_sensor_enabled", false);

        if (brightnessSensorEnabled && lightSensor != null) {
            sensorManager.registerListener(lightListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        // Runnable para mostrar mensaje de luz cada 2 minutos
        luxRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentLux < 10) {
                    Toast.makeText(EditorActivity.this, "Ambiente oscuro 🌙", Toast.LENGTH_SHORT).show();
                } else if (currentLux > 1000) {
                    Toast.makeText(EditorActivity.this, "Ambiente muy iluminado ☀️", Toast.LENGTH_SHORT).show();
                }
                luxHandler.postDelayed(this, 120000); // 2 minutos
            }
        };
        luxHandler.postDelayed(luxRunnable, 120000);
    }

    private final SensorEventListener lightListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            currentLux = event.values[0];
            float brightness = Math.min(1f, Math.max(0.1f, currentLux / 200f));

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
    protected void onResume() {
        super.onResume();
        if (brightnessSensorEnabled && lightSensor != null) {
            sensorManager.registerListener(lightListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        luxHandler.postDelayed(luxRunnable, 120000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (lightSensor != null) sensorManager.unregisterListener(lightListener);
        luxHandler.removeCallbacks(luxRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        luxHandler.removeCallbacks(luxRunnable);
        if (lightSensor != null) sensorManager.unregisterListener(lightListener);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
