package com.example.meetup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.parse.ParseException;
import com.parse.ParseUser;

import com.parse.SignUpCallback;

public class SignUpActivity extends AppCompatActivity {

    private ImageView back;
    private Button signUp;
    private TextInputEditText username;
    private TextInputEditText password;
    private TextInputEditText passwordAgain;
    private ProgressDialog progressDialog;
    private GoogleSignInAccount account;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        progressDialog = new ProgressDialog(SignUpActivity.this);

        back = findViewById(R.id.back);
        signUp = findViewById(R.id.signup);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        passwordAgain = findViewById(R.id.passwordagain);

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (intent != null) {
                account = intent.getParcelableExtra("account");
                if (account != null)
                    username.setText(account.getDisplayName());
            }
        }

        signUp.setOnClickListener(v -> {
            if (password.getText().toString().equals(passwordAgain.getText().toString()) && !TextUtils.isEmpty(username.getText().toString()))
                signUp(username.getText().toString(), password.getText().toString());
            else
                Toast.makeText(this, "Make sure that the values you entered are correct.", Toast.LENGTH_SHORT).show();
        });

        back.setOnClickListener(v -> finish());

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        savedInstanceState.putAll(savedInstanceState);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void signUp(String username, String password) {
        progressDialog.show();
        ParseUser user = new ParseUser();
        // Set the user's username and password, which can be obtained by a forms
        user.setUsername(username);
        user.setPassword(password);
        user.signUpInBackground(e -> {
            progressDialog.dismiss();
            if (e == null) {
                Utils.showAlert("Successful Sign Up ! You logged in...\n", "Welcome " + username + " !", SignUpActivity.this, LogoutActivity.class);
            } else {
                ParseUser.logOut();
                Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}