package com.example.cst8334_glutentracker.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.cst8334_glutentracker.R;
import com.example.cst8334_glutentracker.database.FirebaseOnlineDatabase;
import com.example.cst8334_glutentracker.entity.User;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Collections;

/**
 *This activity is where the user can sign in or sign up an account.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Edit text object where the user inputs login name.
     */
    private EditText loginName;

    /**
     * Edit text object where the user inputs password.
     */
    private EditText password;

    /**
     * Firebase online database object.
     */
    private FirebaseOnlineDatabase db;

    /**
     * Firebase Auth object;
     */
    private FirebaseAuth firebaseAuth;

    /**
     * Call back manager object;
     */
    private CallbackManager callbackManager;

    public final static String LOGIN = "Login";
    public final static String LOGIN_BY_FACEBOOK = "Login by Facebook";
    public final static String LOGIN_BY_GMAIL = "Login by Gmail";
    public final static String CHECK_LOGIN_NAME = "Check login name";
    public final static String CHECK_EMAIL = "Check email";
    public final static String REGISTER = "register";
    private static final String EMAIL = "email";
    private static final String ACTIVITY = "Login activity";

    public final static int REQUEST_CODE_REGISTER = 1;
    public final static int RESULT_CODE_REGISTER = 1;
    public final static int REQUEST_CODE_GOOGLE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FacebookSdk.setApplicationId(getString(R.string.facebook_app_id));
        callbackManager = CallbackManager.Factory.create();

        Button login = findViewById(R.id.sign_in_button);
        Button loginViaGoogle = findViewById(R.id.google_sign_in_btn);
        Button loginViaFacebook = findViewById(R.id.facebook_sign_in_btn);
        loginName = findViewById(R.id.login_name);
        password = findViewById(R.id.password);
        TextView signUp = findViewById(R.id.sign_up_link);

        firebaseAuth = FirebaseAuth.getInstance();
        callbackManager = CallbackManager.Factory.create();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("980537074160-bcnhvb9fhptsdi4ekvt034p5ntsdu488.apps.googleusercontent.com")
                .requestEmail().build();

        SharedPreferences pre = getSharedPreferences(ACTIVITY, Context.MODE_PRIVATE);
        loginName.setText(pre.getString(RegisterActivity.KEY_LOGIN_NAME, ""));
        password.setText(pre.getString(RegisterActivity.KEY_PASSWORD, ""));

        login.setOnClickListener((View v) -> {
            db = new FirebaseOnlineDatabase(LoginActivity.this);
            db.execute(LOGIN, loginName.getText().toString(), password.getText().toString());
        });

        signUp.setOnClickListener((View ) -> startActivityForResult(
                new Intent(LoginActivity.this,
                RegisterActivity.class),
                REQUEST_CODE_REGISTER));

        loginViaGoogle.setOnClickListener((View v) -> startActivityForResult(
                GoogleSignIn.getClient(this, gso).getSignInIntent(),
                REQUEST_CODE_GOOGLE));

        loginViaFacebook.setOnClickListener((View v) ->{
            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        AuthCredential credential = FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());
                        firebaseAuth.signInWithCredential(credential)
                                .addOnCompleteListener(taskFirebase -> {
                                    User.getInstance().setUserName(firebaseAuth.getCurrentUser().getDisplayName())
                                            .setEmail(firebaseAuth.getCurrentUser().getEmail())
                                            .setSignInType(LOGIN_BY_FACEBOOK);
                                    startActivity(new Intent(LoginActivity.this, MainMenuActivity.class));
                                });
                    }

                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onError(FacebookException error) {
                    }
                });
            LoginManager.getInstance().logIn(this, Collections.singletonList(EMAIL));
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_REGISTER && resultCode == RESULT_CODE_REGISTER){
            User currentUser = User.getInstance()
                    .setUserName(data.getStringExtra(RegisterActivity.KEY_USER_NAME))
                    .setLoginName(data.getStringExtra(RegisterActivity.KEY_LOGIN_NAME))
                    .setEmail(data.getStringExtra(RegisterActivity.KEY_EMAIL))
                    .setPassword(data.getStringExtra(RegisterActivity.KEY_PASSWORD));
            db = new FirebaseOnlineDatabase(LoginActivity.this);
            db.execute(LOGIN, currentUser.getLoginName(), currentUser.getPassword());
        }
        if(requestCode == REQUEST_CODE_GOOGLE){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try{
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuth.signInWithCredential(
                        GoogleAuthProvider.getCredential(account.getIdToken(), null)
                ).addOnCompleteListener(taskFirebase -> {
                    User.getInstance().setUserName(account.getDisplayName())
                            .setEmail(account.getEmail())
                            .setSignInType(LOGIN_BY_GMAIL);
                    startActivity(new Intent(LoginActivity.this, MainMenuActivity.class));
                });
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences pre = getSharedPreferences(ACTIVITY, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = pre.edit();

        edit.putString(RegisterActivity.KEY_LOGIN_NAME, loginName.getText().toString());
        edit.putString(RegisterActivity.KEY_PASSWORD, password.getText().toString());
        edit.apply();
    }
}