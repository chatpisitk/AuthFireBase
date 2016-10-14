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
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;
import com.google.firebase.crash.FirebaseCrash;
import com.stream.mobile.authfirebase.R;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends BaseActivity implements
        GoogleApiClient.OnConnectionFailedListener {

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
    Button btnForgetPassword;
    TwitterLoginButton mTwitterLoginButton;
    LoginButton FacebookLoginBtn;

    ImageView mImageView;
    private CallbackManager mCallbackManager;
    private FirebaseAnalytics mFirebaseAnalytics;
    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    private ArrayList<String> permissions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Config Facebook SDK
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        // Configure Twitter SDK
        TwitterAuthConfig authConfig = new TwitterAuthConfig(
                getString(R.string.twitter_consumer_key),
                getString(R.string.twitter_consumer_secret));
        Fabric.with(this, new Twitter(authConfig));

        setContentView(R.layout.activity_main);

        btnSignIn = (Button) findViewById(R.id.btnSignIn);
        btnCreateAccount = (Button) findViewById(R.id.btnCreateAccount);
        btnFacebookLogin = (Button) findViewById(R.id.btnFacebookLoginImg);
        btnFacebookLogin.setBackgroundResource(R.drawable.face_icon);
        FacebookLoginBtn = (LoginButton) findViewById(R.id.btnFacebookLogin);
        btnTwitterLogin = (Button) findViewById(R.id.btnTwitterLoginImg);
        btnTwitterLogin.setBackgroundResource(R.drawable.twitter_icon);
        mTwitterLoginButton = (TwitterLoginButton) findViewById(R.id.btnTwitterLogin);
        btnGoogleSignin = (Button) findViewById(R.id.btnGoogleSignin);
        btnForgetPassword = (Button) findViewById(R.id.btnForgetPassword);
        mEmailField = (EditText) findViewById(R.id.txtEmail);
        mPasswordField = (EditText) findViewById(R.id.txtPass);
        sign_out_button = (Button) findViewById(R.id.sign_out_button);
        mImageView = (ImageView) findViewById(R.id.logo);
        user = mEmailField.getText().toString();
        pass = mPasswordField.getText().toString();
        mStatusTextView = (TextView) findViewById(R.id.profile);

        FirebaseCrash.report(new Exception("Android non-fatal error"));
        FirebaseCrash.log("Activity created");

        // Initialize
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

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

        FacebookOnclick();
        TwitterOnclick();
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


    //Auth Result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Facebook Auth
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        //Twitter Auth
        mTwitterLoginButton.onActivityResult(requestCode, resultCode, data);

        //Google Auth
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);

                Intent itn = new Intent(MainActivity.this, DashBoardActivity.class);
                startActivity(itn);
            } else {
                // Google Sign In failed, update UI appropriately
                updateUI(null);
            }
        }
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        showProgressDialog();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                        hideProgressDialog();
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    //Google End Auth

    /***********************************************************************************************/

    //Facebook Auth
    public void FacebookOnclick() {
        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();

        FacebookLoginBtn.setReadPermissions("email", "public_profile");
        FacebookLoginBtn.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
                Intent itn = new Intent(MainActivity.this, DashBoardActivity.class);
                startActivity(itn);
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                updateUI(null);
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                updateUI(null);
            }
        });

        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // login successful
                        Log.d(TAG, "facebook:onSuccess:" + loginResult);
                        handleFacebookAccessToken(loginResult.getAccessToken());
                        Intent itn = new Intent(MainActivity.this, DashBoardActivity.class);
                        startActivity(itn);
                    }

                    @Override
                    public void onCancel() {
                        // login cancelled
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // login error
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        showProgressDialog();

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
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
    //Facebook End Auth

    /***********************************************************************************************/

    //Twitter Auth
    public void TwitterOnclick() {
        // initialize_twitter_login

        mTwitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Log.d(TAG, "twitterLogin:success" + result);
                handleTwitterSession(result.data);
                Intent itn = new Intent(MainActivity.this, DashBoardActivity.class);
                startActivity(itn);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.w(TAG, "twitterLogin:failure", exception);
                updateUI(null);
            }
        });
    }

    private void handleTwitterSession(TwitterSession session) {
        Log.d(TAG, "handleTwitterSession:" + session);
        showProgressDialog();

        AuthCredential credential = TwitterAuthProvider.getCredential(
                session.getAuthToken().token,
                session.getAuthToken().secret);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Login Success.", Toast.LENGTH_SHORT).show();
                        }
                        hideProgressDialog();
                    }
                });
    }
    //Twitter End Auth

    /***********************************************************************************************/

    //Create Account
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
            mStatusTextView.setText(R.string.signed_out);
            mDetailTextView.setText(null);

            findViewById(R.id.email_password_buttons).setVisibility(View.VISIBLE);
            findViewById(R.id.email_password_fields).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_button).setVisibility(View.GONE);
        }
    }

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
        btnFacebookLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                FacebookOnclick();
                Toast.makeText(getApplicationContext(), "Onclick Facebook Button", Toast.LENGTH_SHORT).show();
            }
        });
        btnTwitterLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                TwitterOnclick();
                Toast.makeText(getApplicationContext(), "Onclick Twitter Button", Toast.LENGTH_SHORT).show();
            }
        });

        sign_out_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
        btnGoogleSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        btnForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                String emailAddress = mEmailField.getText().toString();

                if (TextUtils.isEmpty(mEmailField.getText().toString())) {
                    mEmailField.setError("Required.");
//                    return false;
                } else {
                    auth.sendPasswordResetEmail(emailAddress)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(), "Email Sent Reset Password !!!!", Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, "Email sent.");
                                    }
                                }
                            });
                }
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


