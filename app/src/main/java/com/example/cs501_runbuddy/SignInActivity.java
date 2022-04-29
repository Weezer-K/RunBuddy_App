package com.example.cs501_runbuddy;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cs501_runbuddy.models.User;
import com.fitbit.authentication.AuthenticationHandler;
import com.fitbit.authentication.AuthenticationManager;
import com.fitbit.authentication.AuthenticationResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class SignInActivity extends AppCompatActivity implements AuthenticationHandler {

    private GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN = 9999; // request code for implicit intent of google sign in
    private boolean loginWithoutFitbit; // flag to determine if fitbit auth should be executed or not

    private Button signInAccount;
    private Button signInWithoutFitbit;
    private ImageView image;

    AudioManager audio;
    MediaPlayer signInSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // retrieve views from layout
        signInAccount = (Button) findViewById(R.id.signInAccount);
        signInWithoutFitbit = (Button) findViewById(R.id.signInWithoutFitbit);
        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        signInAccount.setVisibility(View.INVISIBLE);
        signInWithoutFitbit.setVisibility(View.INVISIBLE);
        signInSound = MediaPlayer.create(getApplicationContext(), R.raw.signinsoundeffect);
        image = (ImageView) findViewById(R.id.logoSignInImage);
        image.setImageResource(R.drawable.run_buddy);

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, RunBuddyApplication.getGoogleSignInClient());

        // default is to request fitbit auth
        loginWithoutFitbit = false;

        signInAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // sign in to both google and fitbit accounts
                signInUser();
            }
        });

        signInWithoutFitbit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // set flag to skip fitbit auth step, then sign in just with google
                loginWithoutFitbit = true;
                signInUser();
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);

        // if user signed in, go straight to dashboard
        if (acct != null) {
            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(intent);

        }
        // else, set sign in buttons to visible and wait for user action
        else {
            signInAccount.setVisibility(View.VISIBLE);
            signInWithoutFitbit.setVisibility(View.VISIBLE);
        }
    }

    //Used to sign in via google authentication
    private void signInUser() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    //Will check if fitbit sign up is finished and go to main activity
    @Override
    public void onAuthFinished(AuthenticationResult result) {
        if(result.isSuccessful()){
            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            if(audio.getRingerMode() != AudioManager.RINGER_MODE_VIBRATE && audio.getRingerMode() != AudioManager.RINGER_MODE_SILENT){
                signInSound.start();
            }
            startActivity(intent);
        }
    }

    //Authentication handler functions
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        } else {
            AuthenticationManager.onActivityResult(requestCode, resultCode, data, (AuthenticationHandler) this);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            // get signed in google account
            GoogleSignInAccount acct = completedTask.getResult(ApiException.class);

            // create user object from google account and write it to the database
            User user = new User(acct.getId(), acct.getGivenName(), acct.getFamilyName(), acct.getEmail());
            user.writeToDatabase();

            // if access token for fitbit valid or user does not want to sign in with fitbit,
            // go to dashboard activity
            if(AuthenticationManager.isLoggedIn() || loginWithoutFitbit){
                // execute sign in sound
                if(audio.getRingerMode() != AudioManager.RINGER_MODE_VIBRATE && audio.getRingerMode() != AudioManager.RINGER_MODE_SILENT){
                    signInSound.start();
                }
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);
            }
            // else, request user to sign into fitbit
            //and store access token
            else{
                AuthenticationManager.login(SignInActivity.this);
            }
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed(); // do not call super during a race
    }
}