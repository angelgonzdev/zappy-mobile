package com.example.zappy_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText etCorreo, etClave;
    private Button btnIniciarSesion, btnIrRegistro;
    private FirebaseAuth mAuth;

    @Override
    protected void onStart() {
        super.onStart();
        // Si el usuario ya está logueado, saltar al Home directamente
        if (mAuth.getCurrentUser() != null) {
            irAHome();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        etCorreo = findViewById(R.id.etCorreo);
        etClave = findViewById(R.id.etClave);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        btnIrRegistro = findViewById(R.id.btnIrRegistro);

        // Configuración del botón ir a Registro
        btnIrRegistro.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegistroActivity.class));
        });

        // Configuración del botón Iniciar Sesión
        btnIniciarSesion.setOnClickListener(v -> {
            String email = etCorreo.getText().toString().trim();
            String password = etClave.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Completa los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            loginUsuario(email, password);
        });
    } // <--- Aquí termina onCreate correctamente

    private void loginUsuario(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login exitoso -> ir a Home
                        irAHome();
                    } else {
                        // Manejo de errores
                        String mensajeError = "";
                        try {
                            throw task.getException();
                        } catch (com.google.firebase.auth.FirebaseAuthInvalidUserException e) {
                            mensajeError = "Este usuario no existe o ha sido inhabilitado.";
                        } catch (com.google.firebase.auth.FirebaseAuthInvalidCredentialsException e) {
                            mensajeError = "La contraseña es incorrecta.";
                        } catch (com.google.firebase.FirebaseNetworkException e) {
                            mensajeError = "Error de conexión. Verifica tu internet.";
                        } catch (Exception e) {
                            mensajeError = "Error al iniciar sesión: " + e.getMessage();
                        }
                        Toast.makeText(LoginActivity.this, mensajeError, Toast.LENGTH_LONG).show();
                    }
                });
    }

    // AGREGADO: Este es el método que faltaba para cambiar de pantalla
    private void irAHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
