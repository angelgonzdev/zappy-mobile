package com.example.zappy_mobile;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

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
    private float acelVal;
    private float acelLast;
    private float shake;

    // Base de datos
    private DBHelper dbHelper;

    // RecyclerView
    private RecyclerView rvComics;
    private ComicAdapter adapter;

    // Botones
    private LinearLayout btnHome, btnLibrary, btnCreateNav, btnProfile;
    private Button btnCreate;
    private ImageView btnSettings;

    // Handler para notificaciones programadas
    private android.os.Handler handler = new android.os.Handler();
    private int notifCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // ================= PERMISOS DE NOTIFICACIÓN =================
        askNotificationPermission();

        // ================= CREAR CANAL =================
        createNotificationChannel();

        // ================= SENSORES =================
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL
        );

        acelVal = SensorManager.GRAVITY_EARTH;
        acelLast = SensorManager.GRAVITY_EARTH;
        shake = 0.00f;

        // ============= BASE DE DATOS ============
        dbHelper = new DBHelper(this);

        // ============= VISTAS =============
        rvComics = findViewById(R.id.rvComics);
        btnCreate = findViewById(R.id.btnCreateComic);
        btnSettings = findViewById(R.id.btnSettings);

        btnHome = findViewById(R.id.btnHome);
        btnLibrary = findViewById(R.id.btnLibrary);
        btnCreateNav = findViewById(R.id.btnCreate);
        btnProfile = findViewById(R.id.btnProfile);

        // ============= CONFIG RV ============
        rvComics.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ComicAdapter(this);
        adapter.setOnItemClickListener(this);
        rvComics.setAdapter(adapter);

        // ============= ACCIONES ============
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
    }

    // =================== PERMISO DE NOTIFICACIONES ===================
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
        loadComics();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    // ================== SENSOR SHAKE ==================
    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        acelLast = acelVal;
        acelVal = (float) Math.sqrt((double) (x * x + y * y + z * z));

        float delta = acelVal - acelLast;
        shake = shake * 0.9f + delta;

        if (shake > 12) {  // sensibilidad
            showShakeNotification();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    // ================== NOTIFICACIÓN SHAKE ==================
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

    // ================== CANAL DE NOTIFICACIONES ==================
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Shake Channel";
            String description = "Notificaciones al detectar movimiento";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel =
                    new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // ================== MÉTODOS COMICS ==================
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

    // =================== NOTIFICACIONES PROGRAMADAS ===================
    private void startScheduledNotifications() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
                boolean notifEnabled = prefs.getBoolean("notifications_enabled", true);
                if (notifEnabled) {
                    sendRandomZappyNotification();
                }
                handler.postDelayed(this, 20000); // cada 20 segundos
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

