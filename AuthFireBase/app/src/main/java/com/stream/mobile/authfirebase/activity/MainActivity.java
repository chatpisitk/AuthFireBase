package com.stream.mobile.authfirebase.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;
import com.stream.mobile.authfirebase.R;

import java.io.InputStream;
import java.net.URL;

public class MainActivity extends BaseActivity {

    Button btnSignIn;
    Button btnFacebookLogin;
    Button btnTwitterLogin;
    Button btnGoogleSignin;
    String user;
    String pass;
    TextView mStatusTextView;
    TextView mDetailTextView;
    EditText mEmailField;
    EditText mPasswordField;
    Button btnCreateAccount;
    Button sign_out_button;

    ImageView mImageView;
    private FirebaseAnalytics mFirebaseAnalytics;
    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSignIn = (Button) findViewById(R.id.btnSignIn);
        btnCreateAccount = (Button) findViewById(R.id.btnCreateAccount);
        btnFacebookLogin = (Button) findViewById(R.id.btnFacebookLogin);
        btnTwitterLogin = (Button) findViewById(R.id.btnTwitterLogin);
        btnGoogleSignin = (Button) findViewById(R.id.btnGoogleSignin);
        mEmailField = (EditText) findViewById(R.id.txtEmail);
        mPasswordField = (EditText) findViewById(R.id.txtPass);
        sign_out_button = (Button) findViewById(R.id.sign_out_button);
        mImageView = (ImageView) findViewById(R.id.logo);
        user = mEmailField.getText().toString();
        pass = mPasswordField.getText().toString();
        mStatusTextView = (TextView) findViewById(R.id.profile);

        FirebaseCrash.report(new Exception("My first Android non-fatal error"));
        FirebaseCrash.log("Activity created");

        // Initialize
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Set user property
        mFirebaseAnalytics.setUserProperty("use_activity", "user_signin");

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }

            }
        };

        ButtonClick();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void createAccount(String email, String password) {

        if (!validateForm()) {
            return;
        }
        showProgressDialog();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            mStatusTextView.setTextColor(Color.RED);
                            mStatusTextView.setText(task.getException().getMessage());
                        } else {
                            mStatusTextView.setTextColor(Color.DKGRAY);
                        }
                        hideProgressDialog();
                    }
                });
    }

    private void signIn(String email, String password) {
        if (!validateForm()) {
            return;
        }

        showProgressDialog();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            mStatusTextView.setTextColor(Color.RED);
                            mStatusTextView.setText(task.getException().getMessage());
                        } else {
                            mStatusTextView.setTextColor(Color.DKGRAY);

                            mStatusTextView.setText("Login Sucess!!!!");

                            Intent itn = new Intent(MainActivity.this, DashBoardActivity.class);
                            startActivity(itn);
                        }
                        hideProgressDialog();
                    }
                });
    }

    private void signOut() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(R.string.logout);
        alert.setCancelable(false);
        alert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mAuth.signOut();
                updateUI(null);
            }
        });
        alert.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alert.show();
    }

    private boolean validateForm() {
        if (TextUtils.isEmpty(mEmailField.getText().toString())) {
            mEmailField.setError("Required.");
            return false;
        } else if (TextUtils.isEmpty(mPasswordField.getText().toString())) {
            mPasswordField.setError("Required.");
            return false;
        } else {
            mEmailField.setError(null);
            return true;
        }
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();

        if (user != null) {
            if (user.getPhotoUrl() != null) {
                new DownloadImageTask().execute(user.getPhotoUrl().toString());
            }
            mStatusTextView.setText("DisplayName: " + user.getDisplayName());
            mStatusTextView.append("\n\n");
            mStatusTextView.append("Email: " + user.getEmail());
            mStatusTextView.append("\n\n");
            mStatusTextView.append("Firebase ID: " + user.getUid());

            findViewById(R.id.email_password_buttons).setVisibility(View.GONE);
            findViewById(R.id.email_password_fields).setVisibility(View.GONE);
            findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
        } else {
//            mStatusTextView.setText(R.string.signed_out);
            mDetailTextView.setText(null);

            findViewById(R.id.email_password_buttons).setVisibility(View.VISIBLE);
            findViewById(R.id.email_password_fields).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_button).setVisibility(View.GONE);
        }
    }

//    // android:onClick="sendPredefineEvent" in Button
//    public void sendPredefineEvent(View view) {
//        Bundle bundle = new Bundle();
//        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "12345");
//        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Nougat");
//        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Image");
//        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
//    }
//
//    // android:onClick="sendCustomEvent" in Button
//    public void sendCustomEvent(View view) {
//        Bundle params = new Bundle();
//        params.putString("image_name", "android.png");
//        params.putString("full_text", "Android 7.0 Nougat");
//        mFirebaseAnalytics.logEvent("share_image", params);
//    }


    public void ButtonClick() {
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
//                Toast.makeText(getApplicationContext(), "Onclick !!!!", Toast.LENGTH_SHORT).show();

                Log.d("Log : mEmailField", mEmailField.getText().toString());
                Log.d("Log : mPasswordField", mPasswordField.getText().toString());
            }
        });
        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
            }
        });
        sign_out_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
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

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            Bitmap mIcon = null;
            try {
                InputStream in = new URL(urls[0]).openStream();
                mIcon = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mIcon;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                mImageView.getLayoutParams().width = (getResources().getDisplayMetrics().widthPixels / 100) * 24;
                mImageView.setImageBitmap(result);
            }
        }
    }

}
