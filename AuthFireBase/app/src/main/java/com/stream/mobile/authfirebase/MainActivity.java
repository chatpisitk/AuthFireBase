package com.stream.mobile.authfirebase;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Button btnSignIn;
    Button btnCreateAccount;
    Button btnFacebookLogin;
    Button btnTwitterLogin;
    Button btnGoogleSignin;
    TextView txtEmail;
    TextView txtPass;
    TextView profile;
    String user;
    String pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSignIn = (Button) findViewById(R.id.btnSignIn);
        btnCreateAccount = (Button) findViewById(R.id.btnCreateAccount);
        btnFacebookLogin = (Button) findViewById(R.id.btnFacebookLogin);
        btnTwitterLogin = (Button) findViewById(R.id.btnTwitterLogin);
        btnGoogleSignin = (Button) findViewById(R.id.btnGoogleSignin);
        txtEmail = (TextView) findViewById(R.id.txtEmail);
        txtPass = (TextView) findViewById(R.id.txtPass);
        profile = (TextView) findViewById(R.id.profile);
        user = txtEmail.getText().toString();
        pass = txtPass.getText().toString();

        ButtonClick();
    }


    public void ButtonClick() {
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent itn = new Intent(MainActivity.this, EmailPasswordActivity.class);
//                itn.putExtra("user", user);
//                itn.putExtra("pass", pass);
                startActivity(itn);
                EmailPasswordActivity();

            }
        });
        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        btnFacebookLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        btnTwitterLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        btnGoogleSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

}
