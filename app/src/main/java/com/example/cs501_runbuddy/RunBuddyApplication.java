package com.example.cs501_runbuddy;

import android.app.Application;

import com.fitbit.authentication.AuthenticationConfiguration;
import com.fitbit.authentication.AuthenticationConfigurationBuilder;
import com.fitbit.authentication.AuthenticationManager;
import com.fitbit.authentication.ClientCredentials;
import com.fitbit.authentication.Scope;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by jboggess on 9/28/16.
 */

public class RunBuddyApplication extends Application {

    private static FirebaseDatabase database;
    private static GoogleSignInOptions gso;

    /**
     * These client credentials come from creating an app on https://dev.fitbit.com.
     * <p>
     * To use with your own app, register as a developer at https://dev.fitbit.com, create an application,
     * set the "OAuth 2.0 Application Type" to "Client", enter a word for the redirect url as a url
     * (like `https://finished` or `https://done` or `https://completed`, etc.), and save.
     * <p>
     */

    //!! THIS SHOULD BE IN AN ANDROID KEYSTORE!! See https://developer.android.com/training/articles/keystore.html
    private static final String CLIENT_SECRET = "6609345666d27219fab79ca9c32a222f";

    /**
     * This key was generated using the SecureKeyGenerator [java] class. Run as a Java application (not Android)
     * This key is used to encrypt the authentication token in Android user preferences. If someone decompiles
     * your application they'll have access to this key, and access to your user's authentication token
     */
    //!! THIS SHOULD BE IN AN ANDROID KEYSTORE!! See https://developer.android.com/training/articles/keystore.html
    private static final String SECURE_KEY = "+dLs9LQ+fwZUGqVQZA/WPQY0VcSyG+Buh8lFNXWH1oI=";

    /**
     * This method sets up the authentication config needed for
     * successfully connecting to the Fitbit API. Here we include our client credentials,
     * requested scopes, and  where to return after login
     */


    /**
     * 1. When the application starts, load our keys and configure the AuthenticationManager
     */
    @Override
    public void onCreate() {
        super.onCreate();
        // set up fitbit authentication for the run buddy app passing in necessary keys
        ClientCredentials CLIENT_CREDENTIALS = new ClientCredentials("23876X", CLIENT_SECRET, "https://finished");
        AuthenticationConfiguration config = new AuthenticationConfigurationBuilder()
                .setClientCredentials(CLIENT_CREDENTIALS)
                .setEncryptionKey(SECURE_KEY)
                // these are requested scopes for fitbit when access is approved
                .addRequiredScopes(Scope.profile, Scope.settings, Scope.heartrate, Scope.activity)
                .build();
        AuthenticationManager.configure(this, config);
    }

    // instantiate singleton instance of the firebase database
    public static FirebaseDatabase getDatabase() {
        if (database == null)
            database = FirebaseDatabase.getInstance();
        return database;
    }

    // instantiate singleton instance of the google sign in client
    public static GoogleSignInOptions getGoogleSignInClient() {
        if (gso == null)
            // Configure sign-in to request the user's ID, email address, and basic
            // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
            gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();

        return gso;
    }
}
