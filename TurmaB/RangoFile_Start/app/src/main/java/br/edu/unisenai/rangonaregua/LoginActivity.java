package br.edu.unisenai.rangonaregua;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    EditText edtEmail, edtSenha;
    Button btnEntrar, btnCriarConta, btnRecuperar;
    SignInButton btnGoogle;


    private FirebaseAuth autenticar;

    private ActivityResultLauncher<Intent> signInLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        task.addOnSuccessListener(googleAccount -> {
                            AuthCredential credential = GoogleAuthProvider.getCredential(googleAccount.getIdToken(), null);
                            autenticar.signInWithCredential(credential);

                            Intent rota = new Intent(this, MainActivity.class);
                            startActivity(rota);
                            finish();
                        });
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);
        btnEntrar = findViewById(R.id.btnEntrar);
        btnCriarConta = findViewById(R.id.btnCriarConta);
        btnRecuperar = findViewById(R.id.btnRecuperar);
        btnGoogle = findViewById(R.id.btnGoogle);


        // Abrir conexão com o serviço de Autenticação
        autenticar = FirebaseAuth.getInstance();

        // Verificar se já existe usuário logado
        if (autenticar.getCurrentUser() != null) {
            Intent rota = new Intent(this, MainActivity.class);
            startActivity(rota);
            finish();
        }

        // -- fim

        btnCriarConta.setOnClickListener(v -> criarConta());
        btnEntrar.setOnClickListener(v -> entrar());
        btnRecuperar.setOnClickListener(v-> recuperar());
        btnGoogle.setOnClickListener(v-> google());
    }

    private void google() {
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        signInLauncher.launch(signInIntent);
    }

    private void recuperar() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String emailAddress = edtEmail.getText().toString();

        auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Email enviado", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void entrar() {
        if (edtEmail.getText().toString().isEmpty()) {
            edtEmail.setError("Obrigatório");
        } else if (edtSenha.getText().toString().isEmpty()) {
            edtSenha.setError("Obrigatório");
        } else {
            // entrar
            autenticar.signInWithEmailAndPassword(edtEmail.getText().toString()
                                                  ,edtSenha.getText().toString())
                    .addOnFailureListener(e -> {
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(this, "Email ou senha inválidos", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (e instanceof FirebaseAuthEmailException){
                            Toast.makeText(this, "Email não cadastrado ou Inválido", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnSuccessListener(authResult -> {
                        Intent rota = new Intent(this, MainActivity.class);
                        startActivity(rota);
                        finish();
                    });

        }
    }

    private void criarConta() {
        if (edtEmail.getText().toString().isEmpty()) {
            edtEmail.setError("Obrigatório");
        } else if (edtSenha.getText().toString().isEmpty()) {
            edtSenha.setError("Obrigatório");
        } else {
            // adicinar conta
            autenticar.createUserWithEmailAndPassword(edtEmail.getText().toString()
                                                      ,edtSenha.getText().toString())
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnSuccessListener(authResult -> {
                        Intent rota = new Intent(this, MainActivity.class);
                        startActivity(rota);
                        finish();
                    });
        }
    }
}