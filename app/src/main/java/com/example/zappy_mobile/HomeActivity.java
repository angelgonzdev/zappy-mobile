package com.example.zappy_mobile;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
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
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
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

    private float currentLux = 0f;
    private Handler luxHandler = new Handler();
    private Runnable luxRunnable;

    // Firebase
    private FirebaseFirestore db;

    // UI
    private RecyclerView rvComics;
    private ComicAdapter adapter;

    private LinearLayout btnHome, btnLibrary, btnCreateNav, btnProfile;
    private Button btnCreate;
    private ImageView btnSettings;

    private Handler handler = new Handler();
    private int notifCounter = 0;

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            irAlLogin();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            irAlLogin();
            return;
        }

        // --- NOTIFICACIÓN DE BIENVENIDA ---
        if (savedInstanceState == null) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String nombre = user != null && user.getDisplayName() != null ? user.getDisplayName() : "";
            String mensaje = nombre.isEmpty()
                    ? "Bienvenido a Zappy ⚡"
                    : "Bienvenido a Zappy, " + nombre + " ⚡";

            lanzarNotificacionSistema("¡Hola!", mensaje);
        }

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Inicializar sensores
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        brightnessSensorEnabled = lightSensor != null;

        // Inicializar UI
        rvComics = findViewById(R.id.rvComics);
        btnCreate = findViewById(R.id.btnCreateComic);
        btnSettings = findViewById(R.id.btnSettings);

        btnHome = findViewById(R.id.btnHome);
        btnLibrary = findViewById(R.id.btnLibrary);
        btnCreateNav = findViewById(R.id.btnCreate);
        btnProfile = findViewById(R.id.btnProfile);

        // RecyclerView
        rvComics.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ComicAdapter(this);
        adapter.setOnItemClickListener(this);
        rvComics.setAdapter(adapter);

        // Listeners navegación
        btnCreate.setOnClickListener(v -> openUpload());
        btnSettings.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, SettingsActivity.class)));
        btnHome.setOnClickListener(v -> Toast.makeText(this, "Ya estás en Inicio", Toast.LENGTH_SHORT).show());
        btnLibrary.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, LibraryActivity.class)));
        btnCreateNav.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, EditorActivity.class)));
        btnProfile.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));

        // Cargar cómics
        loadComics();

        // Iniciar notificaciones aleatorias
        startScheduledNotifications();

        // Runnable para luz
        luxRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentLux < 10) {
                    Toast.makeText(HomeActivity.this, "Ambiente oscuro 🌙", Toast.LENGTH_SHORT).show();
                } else if (currentLux > 1000) {
                    Toast.makeText(HomeActivity.this, "Ambiente muy iluminado ☀️", Toast.LENGTH_SHORT).show();
                }
                luxHandler.postDelayed(this, 120000);
            }
        };
        luxHandler.postDelayed(luxRunnable, 120000);

        createNotificationChannel();
    }

    // Listener de luz
    private final SensorEventListener lightListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            currentLux = event.values[0];
            float brightness = Math.min(1f, Math.max(0.1f, currentLux / 200f));

            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.screenBrightness = brightness;
            getWindow().setAttributes(lp);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };

    private void lanzarNotificacionSistema(String titulo, String contenido) {
        String channelId = "canal_bienvenida";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Notificaciones de Bienvenida",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(titulo)
                .setContentText(contenido)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            return;
        }

        NotificationManagerCompat.from(this).notify(1, builder.build());
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

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            loadComics();
        }
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

    // Shake
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            acelLast = acelVal;
            acelVal = (float) Math.sqrt(x * x + y * y + z * z);
            float delta = acelVal - acelLast;
            shake = shake * 0.9f + delta;

            if (shake > 12) showShakeNotification();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void showShakeNotification() {
        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        if (!prefs.getBoolean("notifications_enabled", true)) return;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Movimiento detectado")
                .setContentText("Sacudiste el dispositivo 📱✨")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat.from(this).notify(1, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Shake Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notificaciones al detectar movimiento");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void loadComics() {
        db.collection("comics").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Comic> list = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    try {
                        list.add(document.toObject(Comic.class));
                    } catch (Exception ignored) {}
                }
                adapter.setComics(list);
            } else {
                Toast.makeText(HomeActivity.this, "Error cargando datos", Toast.LENGTH_SHORT).show();
            }
        });
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
                if (prefs.getBoolean("notifications_enabled", true))
                    sendRandomZappyNotification();
                handler.postDelayed(this, 20000);
            }
        }, 20000);
    }

    private void sendRandomZappyNotification() {
        notifCounter++;
        NotificationCompat.Builder builder;

        switch (notifCounter % 4) {
            case 0:
                builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("✨ ¡Novedades Zappy!")
                        .setContentText("Nuevo cómic recomendado para ti.")
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
                        .setContentText("¡Mira esta ilustración increíble!")
                        .setStyle(new NotificationCompat.BigPictureStyle()
                                .bigPicture(BitmapFactory.decodeResource(
                                        getResources(), R.drawable.deedpool)))
                        .setPriority(NotificationCompat.PRIORITY_HIGH);
                break;

            default:
                builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("⚡ Zappy te acompaña")
                        .setContentText("¿Ya probaste el creador de cómics?")
                        .setPriority(NotificationCompat.PRIORITY_HIGH);
                break;
        }

        NotificationManagerCompat.from(this)
                .notify((int) System.currentTimeMillis(), builder.build());
    }

    private void irAlLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
