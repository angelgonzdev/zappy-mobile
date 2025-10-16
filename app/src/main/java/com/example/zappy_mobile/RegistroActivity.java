package com.example.zappy_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegistroActivity extends AppCompatActivity {

    private EditText etNombre, etEmail, etPassword, etConfirmPassword;
    private Button btnRegistrar, btnVolverLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Vincular vistas (ajusta los IDs según tu XML)
        etNombre = findViewById(R.id.etNombre);
        etEmail = findViewById(R.id.etCorreo);
        etPassword = findViewById(R.id.etClave);
        etConfirmPassword = findViewById(R.id.etConfirmarClave);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        btnVolverLogin = findViewById(R.id.btnVolverLogin);

        // Botón Registrar
        btnRegistrar.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPass = etConfirmPassword.getText().toString().trim();

            // Validaciones básicas
            if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPass)) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }

            // Aquí guardarías en tu base de datos
            // dbHelper.insertarUsuario(nombre, email, password);

            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
            finish(); // Vuelve al login
        });

        // Botón Volver al Login
        btnVolverLogin.setOnClickListener(v -> finish());
    }
}