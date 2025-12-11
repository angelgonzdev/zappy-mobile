package com.example.zappy_mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class CreateComicActivity extends AppCompatActivity {

    // Footer buttons
    private LinearLayout btnHome, btnLibrary, btnProfile;
    private ImageView btnSettings;

    // Cuadro para añadir contenido
    private LinearLayout addContentCard;

    // Sensores
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private boolean brightnessSensorEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_comic);

        // ====== Footer ======
        btnHome = findViewById(R.id.btnHome);
        btnLibrary = findViewById(R.id.btnLibrary);
        btnProfile = findViewById(R.id.btnProfile);
        btnSettings = findViewById(R.id.btnSettings);

        // ====== Cuadro principal clickeable ======
        addContentCard = findViewById(R.id.addContentCard);
        addContentCard.setOnClickListener(v -> {
            Intent intent = new Intent(CreateComicActivity.this, EditorActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // ====== Footer navigation ======
        btnHome.setOnClickListener(v ->
                startActivity(new Intent(CreateComicActivity.this, HomeActivity.class))
        );

        btnLibrary.setOnClickListener(v ->
                startActivity(new Intent(CreateComicActivity.this, LibraryActivity.class))
        );

        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(CreateComicActivity.this, ProfileActivity.class))
        );

        // Configuración
        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(CreateComicActivity.this, SettingsActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // ====== Sensores ======
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        brightnessSensorEnabled = prefs.getBoolean("brightness_sensor_enabled", false);

        if (brightnessSensorEnabled && lightSensor != null) {
            sensorManager.registerListener(lightListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    // Listener de luz
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

    @Override
    protected void onResume() {
        super.onResume();
        if (brightnessSensorEnabled && lightSensor != null) {
            sensorManager.registerListener(lightListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (lightSensor != null) sensorManager.unregisterListener(lightListener);
    }
}
