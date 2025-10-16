package com.example.zappy_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsuario, etClave;
    private CheckBox cbRecordarme;
    private Button btnIniciarSesion, btnIrRegistro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Vincular vistas
        etUsuario = findViewById(R.id.etUsuario);
        etClave = findViewById(R.id.etClave);
        cbRecordarme = findViewById(R.id.cbRecordarme);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        btnIrRegistro = findViewById(R.id.btnIrRegistro);

        // Botón Iniciar Sesión
        btnIniciarSesion.setOnClickListener(v -> {
            String usuario = etUsuario.getText().toString().trim();
            String clave = etClave.getText().toString().trim();

            // Validación básica
            if (usuario.isEmpty() || clave.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Ir a HomeActivity (no MainActivity)
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });

        // Botón Registrarse
        btnIrRegistro.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistroActivity.class);
            startActivity(intent);
        });
    }
}