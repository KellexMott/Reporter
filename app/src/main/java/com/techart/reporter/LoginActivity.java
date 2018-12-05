package com.techart.reporter;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {
    // UI references.
    private ProgressBar pbProgress;
    private TextView mLogin;
    // Firebase references.
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mAuth = FirebaseAuth.getInstance();
        mLogin = findViewById(R.id.bt_login);
        pbProgress = findViewById(R.id.progressBar);

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (haveNetworkConnection()){
                    validUserCredentials();
                }else{
                    noIntenet();
                }
            }
        });
    }

    private void validUserCredentials() {
        // [START signin_anonymously]
        pbProgress.setVisibility(View.VISIBLE);
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "signInAnonymously:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                      //  hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END signin_anonymously]
    }

    private void updateUI(FirebaseUser user) {
       // hideProgressDialog();
        boolean isSignedIn = (user != null);
        // Status text
        if (isSignedIn) {
            pbProgress.setVisibility(View.VISIBLE);
            Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
            startActivity(mainIntent);
            //idView.setText(getString(R.string.id_fmt, user.getUid()));
        } else {
            pbProgress.setVisibility(View.VISIBLE);
        }
    }

    private boolean haveNetworkConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo netWorkInfo = cm.getActiveNetworkInfo();
            return netWorkInfo != null && netWorkInfo.getState() == NetworkInfo.State.CONNECTED;
        }
        return false;
    }


    @Override
    public void onBackPressed() {
        //disable going back to the Main activity
        moveTaskToBack(true);
    }

    private void noIntenet(){
        Toast.makeText(LoginActivity.this,"No internet...! Turn on Data or Wifi.", Toast.LENGTH_LONG).show();
    }
}