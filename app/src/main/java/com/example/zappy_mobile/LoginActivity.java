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
    private Button btnIniciarSesion, btnIrRegistro;
    private CheckBox cbRecordarme;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // tu layout de login

        etUsuario = findViewById(R.id.etUsuario);
        etClave = findViewById(R.id.etClave);
        cbRecordarme = findViewById(R.id.cbRecordarme);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        btnIrRegistro = findViewById(R.id.btnIrRegistro);

        dbHelper = new DatabaseHelper(this);

        // Botón iniciar sesión
        btnIniciarSesion.setOnClickListener(v -> {
            String username = etUsuario.getText().toString().trim();
            String password = etClave.getText().toString().trim();

            if(username.isEmpty() || password.isEmpty()){
                Toast.makeText(this, "Ingrese usuario y contraseña", Toast.LENGTH_SHORT).show();
            } else {
                boolean loginOk = dbHelper.checkUser(username, password);
                if(loginOk){
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Botón ir a registro
        btnIrRegistro.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegistroActivity.class)));
    }
}
