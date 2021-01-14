package com.example.cst8334_glutentracker.database;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;

import com.example.cst8334_glutentracker.activity.LoginActivity;
import com.example.cst8334_glutentracker.activity.MainMenuActivity;
import com.example.cst8334_glutentracker.activity.RegisterActivity;
import com.example.cst8334_glutentracker.entity.User;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import android.content.Intent;

/**
 * This class helps the application to interact with the online Firebase database.
 */
public class FirebaseOnlineDatabase extends AsyncTask<String, String, List<User>> {

    /**
     * Firebase's Firestore object.
     */
    private FirebaseFirestore db;

    /**
     * Firebase auth object.
     */
    private FirebaseAuth firebaseAuth;

    /**
     * Users's table name.
     */
    private final static String TABLE_NAME = "users";

    /**
     * User name column.
     */
    private final static String USER_NAME = "userName";

    /**
     * Login name column.
     */
    private final static String LOGIN_NAME = "loginName";

    /**
     * Password column.
     */
    private final static String PASSWORD = "password";

    /**
     * email column.
     */
    private final static String EMAIL = "email";

    /**
     * First argument of doInBackGround(String...);
     */
    private static String firstArgument;

    /**
     * Third argument of doInBackGround(String...);
     */
    private static String thirdArgument;

    /**
     * Are the progresses successful?
     */
    private static boolean isSuccess;

    /**
     * Parent activity.
     */
    @SuppressLint("StaticFieldLeak")
    private Activity fromActivity;

    public FirebaseOnlineDatabase(Activity fromActivity){
        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        this.fromActivity = fromActivity;
    }

    @Override
    protected List<User> doInBackground(String... userInfo) {
        firstArgument = userInfo[0];
        /**
         * Second argument of doInBackGround(String...);
         */
        String secondArgument;
        switch(firstArgument){
            case LoginActivity.LOGIN:{
                thirdArgument = userInfo[2];
                secondArgument = userInfo[1];
                return get(LOGIN_NAME, secondArgument);
            }
            case LoginActivity.CHECK_LOGIN_NAME:{
                secondArgument = userInfo[1];
                return get(LOGIN_NAME, secondArgument);
            }
            case LoginActivity.CHECK_EMAIL:{
                secondArgument = userInfo[1];
                return get(EMAIL, secondArgument);
            }
            case LoginActivity.REGISTER:{
                if(fromActivity!=null){
                    RegisterActivity activity = (RegisterActivity) fromActivity;
                    if(activity.isNoError() && registerNewUser(User.getInstance())){
                        List<User> result = new ArrayList<>();
                        result.add(User.getInstance());
                        return result;
                    }
                }
                return null;
            }
            default: return null;
        }
    }

    @Override
    protected void onPostExecute(List<User> u) {
        switch (firstArgument){
            case LoginActivity.LOGIN: {
                if (!u.isEmpty()) {
                    if (u.size() == 1 &&
                            u.get(0).getPassword().equals(thirdArgument) &&
                            fromActivity != null &&
                            !u.get(0).getLoginName().equals("Admin")) {
                        firebaseAuth.signInWithEmailAndPassword(u.get(0).getEmail(), u.get(0).getLoginName())
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        if (firebaseAuth.getCurrentUser().isEmailVerified()) {
                                            User.getInstance().setSignInType(LoginActivity.LOGIN);
                                            fromActivity.startActivity(new Intent(
                                                    fromActivity.getBaseContext(),
                                                    MainMenuActivity.class));
                                        } else {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(fromActivity);
                                            builder.setTitle("Sign in denied!")
                                                    .setMessage("Please verify your email to sign in!");
                                            builder.create().show();
                                            firebaseAuth.signOut();
                                        }
                                    } else {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(fromActivity);
                                        builder.setTitle("Sign in denied!")
                                                .setMessage("You login name or password is not correct. Please try again!");
                                        builder.create().show();
                                        firebaseAuth.signOut();
                                    }
                                });
                        break;
                    }
                    if (u.get(0).getLoginName().equals("Admin") &&
                            u.get(0).getPassword().equals(thirdArgument)) {
                        fromActivity.startActivity(new Intent(
                                fromActivity.getBaseContext(),
                                MainMenuActivity.class));
                        break;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(fromActivity);
                    builder.setTitle("Sign in denied!")
                            .setMessage("You login name or password is not correct. Please try again!");
                    builder.create().show();
                    break;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(fromActivity);
                builder.setTitle("Sign in denied!")
                        .setMessage("Please enter your account and password!");
                builder.create().show();
                break;
            }
            case LoginActivity.CHECK_LOGIN_NAME:{
                if(fromActivity != null) {
                    RegisterActivity activity = (RegisterActivity) fromActivity;
                    activity.checkLoginAccount(u.size()==1);
                    activity.updateSignUpForm();
                    activity.submitEmail();
                }
                break;
            }
            case LoginActivity.CHECK_EMAIL:{
                if(fromActivity != null) {
                    RegisterActivity activity = (RegisterActivity) fromActivity;
                    activity.checkEmail(u.size()==1);
                    activity.updateSignUpForm();
                    activity.register();
                }
                break;
            }
            case LoginActivity.REGISTER:{
                if(fromActivity != null && u!=null) {
                    RegisterActivity activity = (RegisterActivity) fromActivity;
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle("Sign up successfully!")
                            .setMessage("You have successfully signed up your account. Account information:\n\n" +
                                    "Login name: " + u.get(0).getLoginName() +
                                    "\nUser name: " + u.get(0).getUserName() +
                                    "\nEmail: " + u.get(0).getEmail() +
                                    "\n\nAn verification mail is sent to your email address. " +
                                    "Please verify your email address to sign in your account!")
                            .setPositiveButton("OK", (DialogInterface dialog, int which) -> {
                                Intent user = new Intent();
                                user.putExtra(RegisterActivity.KEY_LOGIN_NAME, u.get(0).getLoginName());
                                user.putExtra(RegisterActivity.KEY_PASSWORD, u.get(0).getPassword());
                                user.putExtra(RegisterActivity.KEY_USER_NAME, u.get(0).getUserName());
                                user.putExtra(RegisterActivity.KEY_EMAIL, u.get(0).getEmail());
                                activity.setResult(LoginActivity.RESULT_CODE_REGISTER, user);
                                activity.finish();
                            });
                    builder.create().show();
                }
            }
        }
    }

    /**
     * Register a new user account.
     *
     * @param user User object.
     * @return is register progress is successful or not.
     */
    private boolean registerNewUser(User user){
        Map<String, String> userMap = new HashMap<>();
        userMap.put(USER_NAME, user.getUserName());
        userMap.put(LOGIN_NAME, user.getLoginName());
        userMap.put(PASSWORD, user.getPassword());
        userMap.put(EMAIL, user.getEmail());
        firebaseAuth.createUserWithEmailAndPassword(user.getEmail(), user.getLoginName())
                .addOnCompleteListener(taskCreate -> {
                    if(taskCreate.isSuccessful()) {
                        db.collection(TABLE_NAME).add(userMap);
                        firebaseAuth.getCurrentUser().sendEmailVerification()
                                .addOnCompleteListener(taskVerifyEmail ->{
                                    if(taskVerifyEmail.isSuccessful()){
                                        firebaseAuth.signOut();
                                        isSuccess = true;
                                    }else {
                                        isSuccess = false;
                                    }
                                });
                    }else isSuccess = false;
                });
        return isSuccess;
    }

    /**
     * Find a user account.
     *
     * @param clause condition's clause.
     * @param value condition's value.
     * @return A list of user accounts.
     */
    private List<User> get(String clause, String value){
        List<User> users = new ArrayList<>();
        try {
            Tasks.await(db.collection(TABLE_NAME).whereEqualTo(clause, value).get()
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful() && task.getResult() != null){

                            for(QueryDocumentSnapshot document: task.getResult()){
                                users.add(User.getInstance()
                                        .setUserName(document.getString(USER_NAME))
                                        .setLoginName(document.getString(LOGIN_NAME))
                                        .setEmail(document.getString(EMAIL))
                                        .setPassword(document.getString(PASSWORD)));
                            }
                        }
                    }));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return users;
    }
}
