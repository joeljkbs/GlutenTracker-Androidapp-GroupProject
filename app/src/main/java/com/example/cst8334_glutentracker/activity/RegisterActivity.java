package com.example.cst8334_glutentracker.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.cst8334_glutentracker.R;
import com.example.cst8334_glutentracker.entity.User;
import com.example.cst8334_glutentracker.functionality.ValidateMessageFragment;
import com.example.cst8334_glutentracker.database.FirebaseOnlineDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This activity is where is user is able to register for a new account.
 */
public class RegisterActivity extends AppCompatActivity {

    /**
     * Edit text object where the user inputs login name.
     */
    private EditText loginName;

    /**
     * Edit text object where the user inputs password.
     */
    private EditText password;

    /**
     * Edit text object where the user inputs re-password.
     */
    private EditText rePassword;

    /**
     * Edit text object where the user inputs user name.
     */
    private EditText userName;

    /**
     * Edit text object where the user inputs email.
     */
    private EditText email;

    /**
     * Value of login name.
     */
    private static String loginNameValue;

    /**
     * Value of password.
     */
    private static String passwordValue;

    /**
     * Value of re-password.
     */
    private static String rePasswordValue;

    /**
     * Value of user name.
     */
    private static String userNameValue;

    /**
     * Value of email.
     */
    private static String emailValue;

    /**
     * Message fragment of login name.
     */
    private ValidateMessageFragment errorMessageLoginName = new ValidateMessageFragment();

    /**
     * Message fragment of password.
     */
    private ValidateMessageFragment errorMessagePassword = new ValidateMessageFragment();

    /**
     * Message fragment of re-password.
     */
    private ValidateMessageFragment errorMessageRePassword = new ValidateMessageFragment();

    /**
     * Message fragment of user name.
     */
    private ValidateMessageFragment errorMessageUserName = new ValidateMessageFragment();

    /**
     * Message fragment of email.
     */
    private ValidateMessageFragment errorMessageEmail = new ValidateMessageFragment();

    /**
     * Frame ID map.
     */
    private static Map<String, Integer> frameMap = new HashMap<>();

    /**
     * Fragment map.
     */
    private static Map<String, ValidateMessageFragment> fragmentMap = new HashMap<>();

    /**
     * Login name key.
     */
    public static final String KEY_LOGIN_NAME = "Login name";

    /**
     * Password key.
     */
    public static final String KEY_PASSWORD = "Password";

    /**
     * Re-password key.
     */
    public static final String KEY_RE_PASSWORD = "Re-Password";

    /**
     * User name key.
     */
    public static final String KEY_USER_NAME = "User name";

    /**
     * Email key.
     */
    public static final String KEY_EMAIL = "Email";

    /**
     * Error message key.
     */
    public static final String KEY_ERROR_MESSAGE = "Error message";

    /**
     * Pattern for login name's range.
     */
    private static final String PATTERN_LOGIN_NAME_RANGE_CHECK = ".{1,20}";

    /**
     * Pattern for login name's validation.
     */
    private static final String PATTERN_LOGIN_NAME_INVALID = "[a-zA-Z0-9_\\-]*";

    /**
     * Pattern for password.
     */
    private static final String PATTERN_PASSWORD_INVALID = "((?=.*[a-z])(?=.*\\d)(?=.*[A-Z])(.{8,}))";

    /**
     * Pattern for user name.
     */
    private static final String PATTERN_USER_NAME_INVALID = "([^\\\\/:?*\"'<>|{}()\\[\\]]*)";

    /**
     * Pattern for email.
     */
    private static final String PATTERN_EMAIL_INVALID = "[a-zA-Z0-9._-]+@[a-z]+\\.[a-z]+";

    /**
     * Error map.
     */
    private static Map<String, List<String>> errorMap;

    /**
     * Firebase online database object.
     */
    private static FirebaseOnlineDatabase db;

    /**
     * Is login name not null?
     */
    private static boolean isLoginNameNotNull;

    /**
     * Is email not null?
     */
    private static boolean isEmailNotNull;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        loginName = findViewById(R.id.sign_up_login_name);
        password = findViewById(R.id.sign_up_password);
        rePassword = findViewById(R.id.sign_up_re_password);
        userName = findViewById(R.id.sign_up_user_name);
        email = findViewById(R.id.sign_up_email);
        Button submit = findViewById(R.id.submit_btn);
        Button cancel = findViewById(R.id.cancel_btn);
        errorMap = new HashMap<>();

        frameMap.put(KEY_LOGIN_NAME, R.id.login_name_error);
        frameMap.put(KEY_PASSWORD, R.id.password_error);
        frameMap.put(KEY_RE_PASSWORD, R.id.re_password_error);
        frameMap.put(KEY_USER_NAME, R.id.user_name_error);
        frameMap.put(KEY_EMAIL, R.id.email_error);

        fragmentMap.put(KEY_LOGIN_NAME, errorMessageLoginName);
        fragmentMap.put(KEY_PASSWORD, errorMessagePassword);
        fragmentMap.put(KEY_RE_PASSWORD, errorMessageRePassword);
        fragmentMap.put(KEY_USER_NAME, errorMessageUserName);
        fragmentMap.put(KEY_EMAIL, errorMessageEmail);

        submit.setOnClickListener((View v) ->{
            getInput();
            userValidator();
            isLoginNameNotNull = Objects.requireNonNull(errorMap.get(KEY_LOGIN_NAME)).isEmpty();
            isEmailNotNull = Objects.requireNonNull(errorMap.get(KEY_EMAIL)).isEmpty();
            if(isLoginNameNotNull || isEmailNotNull){
                submitLoginName();
            }else updateSignUpForm();
        });

        cancel.setOnClickListener((View v) -> finish());
    }

    /**
     * Get input values from the user.
     */
    private void getInput(){
        loginNameValue = loginName.getText().toString();
        passwordValue = password.getText().toString();
        rePasswordValue = rePassword.getText().toString();
        userNameValue = userName.getText().toString();
        emailValue = email.getText().toString();
    }

    /**
     * Validate sign up form.
     */
    private void userValidator(){
        List<String> errors = new ArrayList<>();
        Pattern pattern;
        Matcher matcher;

        if(loginNameValue.isEmpty()) {
            errors.add(getString(R.string.error_login_name_empty));
        }else {
            pattern = Pattern.compile(PATTERN_LOGIN_NAME_RANGE_CHECK);
            matcher = pattern.matcher(loginNameValue);
            if(!matcher.matches()) errors.add(getString(R.string.error_login_name_range));
            pattern = Pattern.compile(PATTERN_LOGIN_NAME_INVALID);
            matcher = pattern.matcher(loginNameValue);
            if(!matcher.matches()) errors.add(getString(R.string.error_login_name_contains_invalid_value));
        }
        errorMap.put(KEY_LOGIN_NAME, errors);
        errors = new ArrayList<>();

        if(passwordValue.isEmpty()) {
            errors.add(getString(R.string.error_password_empty));
        }else {
            pattern = Pattern.compile(PATTERN_PASSWORD_INVALID);
            matcher = pattern.matcher(passwordValue);
            if(!matcher.matches()) errors.add(getString(R.string.error_invalid_password));
        }
        errorMap.put(KEY_PASSWORD, errors);
        errors = new ArrayList<>();

        if(rePasswordValue.isEmpty()) {
            errors.add(getString(R.string.error_password_empty));
        }else {
            if (!rePasswordValue.equals(passwordValue)) errors.add(getString(R.string.error_password_does_not_match));
        }
        errorMap.put(KEY_RE_PASSWORD, errors);
        errors = new ArrayList<>();

        if(userNameValue.isEmpty()){
            errors.add(getString(R.string.error_user_name_empty));
        }else {
            pattern = Pattern.compile(PATTERN_USER_NAME_INVALID);
            matcher = pattern.matcher(userNameValue);
            if(!matcher.matches()) errors.add(getString(R.string.error_user_name_contains_invalid_value));
        }
        errorMap.put(KEY_USER_NAME, errors);
        errors = new ArrayList<>();

        if(emailValue.isEmpty()){
            errors.add(getString(R.string.error_email_empty));
        }else {
            pattern = Pattern.compile(PATTERN_EMAIL_INVALID);
            matcher = pattern.matcher(emailValue);
            if(!matcher.matches()) errors.add(getString(R.string.error_invalid_email));
        }
        errorMap.put(KEY_EMAIL, errors);
    }

    /**
     * Check if login name already exists or not.
     *
     * @param isLoginNameAlreadyExist is login name already exists?
     */
    public void checkLoginAccount(boolean isLoginNameAlreadyExist){
        List<String> errors = new ArrayList<>();
        if(isLoginNameAlreadyExist) errors.add(loginNameValue + getString(R.string.error_login_name_already_exits));
        errorMap.put(KEY_LOGIN_NAME, errors);
    }

    /**
     * Check if email already exists or not.
     *
     * @param isEmailAlreadyExist is email already exists?
     */
    public void checkEmail(boolean isEmailAlreadyExist){
        List<String> errors = new ArrayList<>();
        if(isEmailAlreadyExist) errors.add(emailValue + getString(R.string.error_email_already_exists));
        errorMap.put(KEY_EMAIL, errors);
    }

    /**
     * Update sign up form.
     */
    public void updateSignUpForm(){
        List<String> errors;
        StringBuilder updatedText;
        Bundle errorMessages;

        for(String key: errorMap.keySet()){

            errorMessages = new Bundle();
            errors = errorMap.get(key);
            updatedText = new StringBuilder();

            if(errors!=null && !errors.isEmpty()) {
                for (String error : errors) {
                    updatedText.append(error).append("\n");
                }
                fragmentMap.put(key, new ValidateMessageFragment());
                errorMessages.putString(KEY_ERROR_MESSAGE, updatedText.toString());
                fragmentMap.get(key).setArguments(errorMessages);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(frameMap.get(key), fragmentMap.get(key))
                        .commit();
            }else {
                getSupportFragmentManager().beginTransaction().remove(fragmentMap.get(key)).commit();
            }
        }
    }

    /**
     * Check if there is any error.
     *
     * @return if there is any error.
     */
    public boolean isNoError(){
        boolean isNoError = true;
        for (List<String> v: errorMap.values()){
            if(!v.isEmpty()){
                isNoError = false;
                break;
            }
        }
        return isNoError;
    }

    /**
     * Run checking login name progress.
     */
    public void submitLoginName(){
        if(isLoginNameNotNull){
            db = new FirebaseOnlineDatabase(RegisterActivity.this);
            db.execute(LoginActivity.CHECK_LOGIN_NAME, loginNameValue);
        }
    }

    /**
     * Run checking email progress.
     */
    public void submitEmail(){
        if(isEmailNotNull){
            db = new FirebaseOnlineDatabase(RegisterActivity.this);
            db.execute(LoginActivity.CHECK_EMAIL, emailValue);
        }
    }

    /**
     * Run register progress.
     */
    public void register(){
        if(isNoError()){
            User.getInstance()
                    .setLoginName(loginNameValue)
                    .setPassword(passwordValue)
                    .setUserName(userNameValue)
                    .setEmail(emailValue);
            db = new FirebaseOnlineDatabase(RegisterActivity.this);
            db.execute(LoginActivity.REGISTER);
        }
    }
}