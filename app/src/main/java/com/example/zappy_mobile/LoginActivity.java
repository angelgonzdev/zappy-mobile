package com.example.zappy_mobile;import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
        setContentView(R.layout.activity_login); // Asegúrate que tu XML se llame así

        mAuth = FirebaseAuth.getInstance();

        // Ajusta estos IDs si son diferentes en tu activity_login.xml
        etCorreo = findViewById(R.id.etCorreo); // O etUsuario, revisa tu XML
        etClave = findViewById(R.id.etClave);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        btnIrRegistro = findViewById(R.id.btnIrRegistro);

        btnIrRegistro.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegistroActivity.class));
        });

        btnIniciarSesion.setOnClickListener(v -> {
            String email = etCorreo.getText().toString().trim();
            String password = etClave.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Completa los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            loginUsuario(email, password);
        });
    }

    private void loginUsuario(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login exitoso -> ir a Home
                        irAHome();
                    } else {
                        // --- INICIO DEL MANEJO DE ERRORES DETALLADO ---
                        String mensajeError = "";

                        try {
                            // Lanzamos la excepción para poder capturarla por tipos
                            throw task.getException();
                        } catch (com.google.firebase.auth.FirebaseAuthInvalidUserException e) {
                            // El correo no está registrado o la cuenta fue borrada/inhabilitada
                            mensajeError = "Este usuario no existe o ha sido inhabilitado.";
                        } catch (com.google.firebase.auth.FirebaseAuthInvalidCredentialsException e) {
                            // La contraseña es incorrecta o el correo tiene formato mal (ej: faltan letras)
                            mensajeError = "La contraseña es incorrecta.";
                        } catch (com.google.firebase.FirebaseNetworkException e) {
                            // Error de conexión a internet
                            mensajeError = "Error de conexión. Verifica tu internet.";
                        } catch (Exception e) {
                            // Cualquier otro error
                            mensajeError = "Error al iniciar sesión: " + e.getMessage();
                        }

                        // Mostrar el mensaje específico al usuario
                        Toast.makeText(LoginActivity.this, mensajeError, Toast.LENGTH_LONG).show();
                        // --- FIN DEL MANEJO DE ERRORES ---
                    }
                });
    }


    private void irAHome() {
        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        finish();
    }
}
