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
                        irAHome();
                    } else {
                        Toast.makeText(this, "Error: Credenciales inválidas", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void irAHome() {
        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        finish();
    }
}
