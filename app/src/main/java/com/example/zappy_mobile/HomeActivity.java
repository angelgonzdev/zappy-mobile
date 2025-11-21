package com.example.zappy_mobile;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.List;

public class HomeActivity extends AppCompatActivity
        implements ComicAdapter.OnItemClickListener, SensorEventListener {

    private static final String CHANNEL_ID = "shake_channel";
    private static final int NOTIFICATION_PERMISSION_CODE = 101;

    // Sensores
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private boolean brightnessSensorEnabled = false;

    private float acelVal;
    private float acelLast;
    private float shake;

    // Último valor de lux y handler para actualizar cada 2 minutos
    private float currentLux = 0f;
    private Handler luxHandler = new Handler();
    private Runnable luxRunnable;

    // Base de datos
    private DBHelper dbHelper;

    // RecyclerView
    private RecyclerView rvComics;
    private ComicAdapter adapter;

    // Botones
    private LinearLayout btnHome, btnLibrary, btnCreateNav, btnProfile;
    private Button btnCreate;
    private ImageView btnSettings;

    private Handler handler = new Handler();
    private int notifCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Permisos de notificación
        askNotificationPermission();

        // Canal de notificaciones
        createNotificationChannel();

        // Sensores
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL
        );

        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        acelVal = SensorManager.GRAVITY_EARTH;
        acelLast = SensorManager.GRAVITY_EARTH;
        shake = 0.00f;

        // Preferencias
        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        brightnessSensorEnabled = prefs.getBoolean("brightness_sensor_enabled", false);

        if (brightnessSensorEnabled && lightSensor != null) {
            sensorManager.registerListener(lightListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        // Base de datos
        dbHelper = new DBHelper(this);

        // Vistas
        rvComics = findViewById(R.id.rvComics);
        btnCreate = findViewById(R.id.btnCreateComic);
        btnSettings = findViewById(R.id.btnSettings);

        btnHome = findViewById(R.id.btnHome);
        btnLibrary = findViewById(R.id.btnLibrary);
        btnCreateNav = findViewById(R.id.btnCreate);
        btnProfile = findViewById(R.id.btnProfile);

        // Config RecyclerView
        rvComics.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ComicAdapter(this);
        adapter.setOnItemClickListener(this);
        rvComics.setAdapter(adapter);

        // Acciones
        btnCreate.setOnClickListener(v -> openUpload());
        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, SettingsActivity.class)));
        btnHome.setOnClickListener(v ->
                Toast.makeText(this, "Ya estás en Inicio", Toast.LENGTH_SHORT).show());
        btnLibrary.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, LibraryActivity.class)));
        btnCreateNav.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, EditorActivity.class)));
        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));

        // Cargar comics
        loadComics();

        // Iniciar notificaciones programadas
        startScheduledNotifications();

        // Runnable para mostrar mensaje de luz cada 2 minutos
        luxRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentLux < 10) {
                    Toast.makeText(HomeActivity.this, "Ambiente oscuro 🌙", Toast.LENGTH_SHORT).show();
                } else if (currentLux > 1000) {
                    Toast.makeText(HomeActivity.this, "Ambiente muy iluminado ☀️", Toast.LENGTH_SHORT).show();
                }
                luxHandler.postDelayed(this, 120000); // 2 minutos
            }
        };
        luxHandler.postDelayed(luxRunnable, 120000); // iniciar primera vez
    }

    // Listener de luz
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

    // Permiso de notificación
    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso de notificación concedido ✔", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Debes permitirlo para ver notificaciones", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL
        );
        if (brightnessSensorEnabled && lightSensor != null) {
            sensorManager.registerListener(lightListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        luxHandler.postDelayed(luxRunnable, 120000);
        loadComics();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        if (lightSensor != null) sensorManager.unregisterListener(lightListener);
        luxHandler.removeCallbacks(luxRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        luxHandler.removeCallbacks(luxRunnable);
        if (lightSensor != null) sensorManager.unregisterListener(lightListener);
    }

    // Sensor shake
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            acelLast = acelVal;
            acelVal = (float) Math.sqrt((double) (x * x + y * y + z * z));
            float delta = acelVal - acelLast;
            shake = shake * 0.9f + delta;

            if (shake > 12) {
                showShakeNotification();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    // Notificación shake
    private void showShakeNotification() {
        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        boolean notifEnabled = prefs.getBoolean("notifications_enabled", true);
        if (!notifEnabled) return;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Movimiento detectado")
                .setContentText("Sacudiste el dispositivo 📱✨")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify(1, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Shake Channel";
            String description = "Notificaciones al detectar movimiento";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void loadComics() {
        List<Comic> list = dbHelper.getAllComics();
        adapter.setComics(list);
    }

    private void openUpload() {
        startActivity(new Intent(this, UploadActivity.class));
    }

    @Override
    public void onItemClick(Comic comic) {
        Intent intent = new Intent(this, PdfViewerActivity.class);
        intent.putExtra("path", comic.getFilePath());
        intent.putExtra("title", comic.getTitle());
        startActivity(intent);
    }

    private void startScheduledNotifications() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
                boolean notifEnabled = prefs.getBoolean("notifications_enabled", true);
                if (notifEnabled) sendRandomZappyNotification();
                handler.postDelayed(this, 20000);
            }
        }, 20000);
    }

    private void sendRandomZappyNotification() {
        notifCounter++;
        NotificationCompat.Builder builder;
        NotificationManagerCompat manager = NotificationManagerCompat.from(this);

        switch (notifCounter % 4) {
            case 0:
                builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("✨ ¡Novedades Zappy!")
                        .setContentText("Nuevo cómic recomendado especialmente para ti.")
                        .setPriority(NotificationCompat.PRIORITY_HIGH);
                break;
            case 1:
                builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("📚 Tu biblioteca te espera")
                        .setContentText("Continúa leyendo donde lo dejaste 🦸‍♂️.")
                        .setPriority(NotificationCompat.PRIORITY_HIGH);
                break;
            case 2:
                builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("🔥 Zappy: Destacado del día")
                        .setContentText("Mira esta ilustración increíble!")
                        .setStyle(new NotificationCompat.BigPictureStyle()
                                .bigPicture(BitmapFactory.decodeResource(getResources(), R.drawable.deedpool)))
                        .setPriority(NotificationCompat.PRIORITY_HIGH);
                break;
            default:
                builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("⚡ Zappy te acompaña")
                        .setContentText("¿Probaste ya el creador de cómics?")
                        .setPriority(NotificationCompat.PRIORITY_HIGH);
                break;
        }
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
