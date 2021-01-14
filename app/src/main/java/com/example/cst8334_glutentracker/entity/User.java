package com.example.cst8334_glutentracker.entity;

/**
 * This class represents a user instance.
 */
public class User {

    /**
     * User's user name.
     */
    private String userName;

    /**
     * User's login name.
     */
    private String loginName;

    /**
     * User's email.
     */
    private String email;

    /**
     * User's password.
     */
    private String password;

    /**
     * User's sign in type.
     */
    private String signInType;

    /**
     * This class's only instance.
     */
    private static final User INSTANCE = new User();

    /**
     * Private constructor.
     */
    private User(){
    }

    /**
     * Get this class's only instance.
     *
     * @return this class's only instance.
     */
    public static User getInstance(){
        return INSTANCE;
    }

    /**
     * Getter of user name.
     * @return user's user name.
     */
    public String getUserName(){
        return userName;
    }

    /**
     * Setter of user name.
     * @param userName user's user name.
     * @return the current instance.
     */
    public User setUserName(String userName){
        this.userName = userName;
        return this;
    }

    /**
     * Getter of login name.
     * @return user's login name.
     */
    public String getLoginName(){
        return loginName;
    }

    /**
     * Setter of login name.
     * @param loginName user's login name.
     * @return the current instance.
     */
    public User setLoginName(String loginName){
        this.loginName = loginName;
        return this;
    }

    /**
     * Getter of email.
     * @return user's email.
     */
    public String getEmail(){
        return this.email;
    }

    /**
     * Setter of email.
     * @param email user's email.
     * @return the current instance.
     */
    public User setEmail(String email) {
        this.email = email;
        return this;
    }

    /**
     * Getter of password.
     * @return user's password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Setter of password.
     * @param password user's password.
     * @return the current instance.
     */
    public User setPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     * Getter of sign in type.
     * @return user's user sign in type.
     */
    public String getSignInType() {
        return signInType;
    }

    /**
     * Setter of sign in type.
     * @param isSignInType user's sign in type.
     * @return the current instance.
     */
    public User setSignInType(String isSignInType) {
        this.signInType = isSignInType;
        return this;
    }
}
