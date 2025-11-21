package com.example.zappy_mobile;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;// Importar AlertDialog
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException; // Importante para detectar correo duplicado
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistroActivity extends AppCompatActivity {

    private EditText etNombre, etCorreo, etClave, etConfirmarClave;
    private Button btnRegistrar, btnVolverLogin;

    // Variables de Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 1. Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 2. Vincular vistas
        etNombre = findViewById(R.id.etNombre);
        etCorreo = findViewById(R.id.etCorreo);
        etClave = findViewById(R.id.etClave);
        etConfirmarClave = findViewById(R.id.etConfirmarClave);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        btnVolverLogin = findViewById(R.id.btnVolverLogin);

        // 3. Botón Registrar
        btnRegistrar.setOnClickListener(v -> registrarUsuario());

        // 4. Botón Volver
        btnVolverLogin.setOnClickListener(v -> {
            irALogin();
        });
    }

    private void registrarUsuario() {
        String nombre = etNombre.getText().toString().trim();
        String email = etCorreo.getText().toString().trim();
        String password = etClave.getText().toString().trim();
        String confirmPassword = etConfirmarClave.getText().toString().trim();

        // Validaciones básicas
        if (TextUtils.isEmpty(nombre)) { etNombre.setError("Nombre requerido"); return; }
        if (TextUtils.isEmpty(email)) { etCorreo.setError("Correo requerido"); return; }
        if (TextUtils.isEmpty(password)) { etClave.setError("Contraseña requerida"); return; }
        if (password.length() < 6) { etClave.setError("Mínimo 6 caracteres"); return; }
        if (!password.equals(confirmPassword)) { etConfirmarClave.setError("No coinciden"); return; }

        // --- CREAR USUARIO EN FIREBASE AUTH ---
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // El registro en Auth fue exitoso
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            // Actualizar nombre visual
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(nombre).build();
                            user.updateProfile(profileUpdates);

                            // GUARDAR EN FIRESTORE
                            guardarDatosEnFirestore(user.getUid(), nombre, email);
                        }

                    } else {
                        // --- MANEJO DE ERRORES ---
                        String mensajeError;

                        // Verificamos si el error es porque el correo ya existe
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            mensajeError = "Este correo ya está en uso.";
                        } else {
                            // Otro error (ej: sin internet, contraseña muy debil rechazada por firebase, etc)
                            mensajeError = "Error al registrar: " + task.getException().getMessage();
                        }

                        mostrarAlerta("Error de Registro", mensajeError, false);
                    }
                });
    }

    private void guardarDatosEnFirestore(String uid, String nombre, String email) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", uid);
        userData.put("username", nombre);
        userData.put("email", email);
        userData.put("role", "user");

        db.collection("users")
                .document(uid)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    // ¡ÉXITO TOTAL!
                    // Mostramos la alerta y cuando le de OK, lo mandamos al Login
                    mostrarAlerta("¡Registro Exitoso!", "Se ha registrado correctamente.", true);

                    // Opcional: Cerrar sesión automáticamente para obligarlo a loguearse
                    mAuth.signOut();
                })
                .addOnFailureListener(e -> {
                    mostrarAlerta("Error", "Usuario creado pero falló al guardar datos: " + e.getMessage(), false);
                });
    }

    // --- MÉTODO PARA MOSTRAR ALERTAS ---
    private void mostrarAlerta(String titulo, String mensaje, boolean irAlLoginAlCerrar) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titulo);
        builder.setMessage(mensaje);
        builder.setCancelable(false); // Evita que se cierre tocando fuera

        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (irAlLoginAlCerrar) {
                    irALogin();
                }
                // Si es false, simplemente se cierra el diálogo y se queda en el registro para corregir
            }
        });

        builder.show();
    }

    private void irALogin() {
        startActivity(new Intent(RegistroActivity.this, LoginActivity.class));
        finish();
    }
}
