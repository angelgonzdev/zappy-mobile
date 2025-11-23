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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class ProfileActivity extends AppCompatActivity {

    private LinearLayout btnHome, btnLibrary, btnCreate, btnProfile;
    private MaterialButton btnEditProfile;
    private ImageView btnSettings;

    // Sensores
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private boolean brightnessSensorEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Vincular vistas
        btnHome = findViewById(R.id.btnHome);
        btnLibrary = findViewById(R.id.btnLibrary);
        btnCreate = findViewById(R.id.btnEdit); // antes llamado btnEdit
        btnProfile = findViewById(R.id.btnProfile);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnSettings = findViewById(R.id.btnSettings);

        // Navegación footer
        btnHome.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, HomeActivity.class))
        );

        btnLibrary.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, LibraryActivity.class))
        );

        btnCreate.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, EditorActivity.class))
        );

        btnProfile.setOnClickListener(v ->
                Toast.makeText(this, "Ya estás en Perfil", Toast.LENGTH_SHORT).show()
        );

        // Botón editar perfil
        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class))
        );

        // Botón de Configuración -> Abrir SettingsActivity
        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, SettingsActivity.class))
        );

        // ===== Sensores =====
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
