package com.example.stockprediction.activites;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stockprediction.R;
import com.example.stockprediction.objects.User;
import com.example.stockprediction.apis.firebase.MyFireBaseServices;
import com.example.stockprediction.utils.MySignal;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.gson.Gson;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private enum LoginState {
        SIGN_IN,
        REGISTER
    }

    private enum SuccessStatus {
        NONE,
        SUCCESS,
        FAILURE
    }


    private TextInputLayout login_EDT_name;
    private TextInputLayout login_EDT_email;
    private TextInputLayout login_EDT_password;
    private ProgressBar login_PB;
    private MaterialButton login_BTN_login;
    private TextView login_register;
    private TextView login_TV_notes;
    private LoginState loginState = LoginState.SIGN_IN;
    private SuccessStatus successStatus = SuccessStatus.NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findViews();
        initViews();

    }
    private void findViews() {
        login_EDT_name = findViewById(R.id.login_EDT_name);
        login_EDT_email = findViewById(R.id.login_EDT_email);
        login_EDT_password = findViewById(R.id.login_EDT_password);
        login_PB = findViewById(R.id.login_PB);
        login_BTN_login = findViewById(R.id.login_BTN_login);
        login_register = findViewById(R.id.login_register);
        login_TV_notes = findViewById(R.id.login_TV_notes);
    }

    private void initViews() {
        login_BTN_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isValidPassword = checkAndHandleEmptyPassword();
                boolean isEmptyEmail = checkAndHandleEmptyEmail();

                if(loginState == LoginState.SIGN_IN) {
                    // invoke sign-in operation.
                    if (!isValidPassword && !isEmptyEmail) {
                        login(v);
                    }
                } else if (loginState == LoginState.REGISTER) {
                    // invoke register operation.
                    boolean isEmptyName = checkAndHandleEmptyName();
                    if(!isValidPassword && !isEmptyEmail && !isEmptyName) {
                        createUser(v);
                    }
                }

            }
        });
        login_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginState = LoginState.REGISTER;
                successStatus = SuccessStatus.NONE;
                updateUI();
            }
        });
    }

    private void login(View view) {
        /**
         *  Method handle Firebase login event.
         */

        login_PB.setVisibility(View.VISIBLE);
        hideKeyBoard(view);
        signInWithEmailAndPassword(getEmailInput(),getPasswordInput());
    }

    private void createUser(View view) {
        login_PB.setVisibility(View.VISIBLE);
        hideKeyBoard(view);
        handleCreateUserRequest(getEmailInput(),getPasswordInput());
    }

    private void signInWithEmailAndPassword(String email, String password) {
        MyFireBaseServices.getInstance().getFirebaseAuth().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this,  new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("LoginActivity", "signInWithEmailAndPassword: ignInWithEmail:success");
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d("LoginActivity", "signInWithEmailAndPassword: signInWithEmail:failure", task.getException());
                            MySignal.getInstance().toast("Authentication failed.");
                            login_PB.setVisibility(View.INVISIBLE);
                            checkAndHandleEmptyEmail();
                            updateUI();
                        }
                    }
                });
    }

    private boolean checkAndHandleEmptyName() {
        String name = getNameInput();
        boolean isEmpty = true;
        if (name.equalsIgnoreCase("")) {
            login_EDT_name.setErrorEnabled(true);
            setAndEnabledErrorMsgTextFiled(getString(R.string.name_constraint), login_EDT_name);
        } else {
            login_EDT_name.setErrorEnabled(false);
            isEmpty = false;
        }
        return isEmpty;
    }

    private boolean checkAndHandleEmptyPassword() {
        String password = getPasswordInput();
        boolean isEmptyOrBelowSixCharacters = true;
        if (password.equalsIgnoreCase("") || password.length() < 6) {
            login_EDT_password.setErrorEnabled(true);
            setAndEnabledErrorMsgTextFiled(getString(R.string.password_constraint), login_EDT_password);
        } else {
            login_EDT_password.setErrorEnabled(false);
            isEmptyOrBelowSixCharacters = false;
        }
        return isEmptyOrBelowSixCharacters;
    }

    private boolean checkAndHandleEmptyEmail() {
        String email = getEmailInput();
        boolean isEmptyEmail = true;
        if (email.equalsIgnoreCase("") || !validateEmailRegex(email)) {
            login_EDT_email.setErrorEnabled(true);
            setAndEnabledErrorMsgTextFiled(getString(R.string.invalid_email), login_EDT_email);
        } else {
            login_EDT_email.setErrorEnabled(false);
            isEmptyEmail = false;
        }
        return isEmptyEmail;
    }

    private boolean validateEmailRegex(String emailStr) {
        Pattern emailRegex= Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = emailRegex.matcher(emailStr);
        return matcher.find();
    }

    private void handleCreateUserRequest(String email, String password) {
        /**
         * Method checks if is there already exist user using the inserted email, if not creating a new user.
         */
        MyFireBaseServices.getInstance().getFirebaseAuth().fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                if (task.isSuccessful()) { // Email not exists - allow creating user.
                    login_EDT_email.setErrorEnabled(false);
                    login_PB.setVisibility(View.VISIBLE);
                    createUserWithEmailAndPassword(email, password);
                } else {    // There is already account with this email address.
                    setAndEnabledErrorMsgTextFiled(getString(R.string.already_account),login_EDT_email);
                    login_PB.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void createUserWithEmailAndPassword(String email, String password) {
        MyFireBaseServices myFireBaseServices = MyFireBaseServices.getInstance();
        myFireBaseServices.getFirebaseAuth().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("LoginActivity", "createUserWithEmailAndPassword: createUserWithEmail:success");
                            myFireBaseServices.setFirebaseUser(task.getResult().getUser());
                            User user = new User()
                                    .setUid(myFireBaseServices.getFirebaseUser().getUid())
                                    .setEmail(email)
                                    .setName(getNameInput());
                            Log.d("LoginActivity", "onComplete: user ="+user);
                            myFireBaseServices.saveUserToFireBase(user);
                            Bundle bundle = new Bundle();
                            Gson gson = new Gson();
                            String jsonUser = gson.toJson(user,User.class);
                            bundle.putString(MainActivity.USER, jsonUser);
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtras(bundle);
                            startActivity(intent);
                            finish();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d("LoginActivity", "createUserWithEmailAndPassword: createUserWithEmail:failure", task.getException());
                            MySignal.getInstance().toast("Authentication failed.");
                            login_PB.setVisibility(View.INVISIBLE);
                        }
                    }
                });
    }


    private void setAndEnabledErrorMsgTextFiled(String msg, TextInputLayout textInputLayout) {
        textInputLayout.setErrorEnabled(true);
        textInputLayout.setError(msg);
    }

    private void hideKeyBoard(View view) {
        InputMethodManager imm = (InputMethodManager) getBaseContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private String getEmailInput() {
        return login_EDT_email.getEditText().getText().toString();
    }

    private String getPasswordInput() {
        return login_EDT_password.getEditText().getText().toString();
    }

    private String getNameInput() {
        return login_EDT_name.getEditText().getText().toString();
    }


    private void updateUI() {
        login_EDT_name.setErrorEnabled(false);
        login_EDT_email.setErrorEnabled(false);
        login_EDT_password.setErrorEnabled(false);
        switch(loginState) {
            case SIGN_IN:
                login_EDT_name.setVisibility(View.INVISIBLE);
                login_BTN_login.setText("Login");
                login_register.setVisibility(View.VISIBLE);
                login_register.setClickable(true);
                break;
            case REGISTER:
                login_EDT_name.setVisibility(View.VISIBLE);
                login_BTN_login.setText("Register");
                login_register.setVisibility(View.INVISIBLE);
                login_register.setClickable(false);
                break;
        }
        switch(successStatus) {
            case FAILURE:
                login_TV_notes.setVisibility(View.VISIBLE);
                break;
            case NONE:
            case SUCCESS:
                login_TV_notes.setVisibility(View.INVISIBLE);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (loginState == LoginState.REGISTER) {
            loginState = LoginState.SIGN_IN;
            successStatus = SuccessStatus.NONE;
            updateUI();
        } else {
            super.onBackPressed();
        }
    }
}