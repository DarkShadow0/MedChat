package com.example.abhinay.medchat.core.login;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.abhinay.medchat.utils.Constants;
import com.example.abhinay.medchat.utils.SharedPrefUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import static android.content.ContentValues.TAG;

/**
 * Author: Kartik Sharma
 * Created on: 8/28/2016 , 11:10 AM
 * Project: FirebaseChat
 */

public class LoginInteractor implements LoginContract.Interactor {
    private LoginContract.OnLoginListener mOnLoginListener;

    public LoginInteractor(LoginContract.OnLoginListener onLoginListener) {
        this.mOnLoginListener = onLoginListener;
    }

    @Override
    public void performFirebaseLogin(final Activity activity, final String email, String password) {
        FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "performFirebaseLogin:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (task.isSuccessful()) {
                            mOnLoginListener.onSuccess(task.getResult().toString());
                            updateFirebaseToken(task.getResult().getUser().getUid(),
                                    new SharedPrefUtil(activity.getApplicationContext()).getString(Constants.ARG_FIREBASE_TOKEN, null));
                        } else {
                            mOnLoginListener.onFailure(task.getException().getMessage());
                        }
                    }
                });
    }

    private void updateFirebaseToken(String uid, String token) {
        FirebaseDatabase.getInstance()
                .getReference()
                .child(Constants.ARG_USERS)
                .child(uid)
                .child(Constants.ARG_FIREBASE_TOKEN)
                .setValue(token);
    }

    /**
     * Author: Kartik Sharma
     * Created on: 8/28/2016 , 11:10 AM
     * Project: FirebaseChat
     */

    public static class LoginPresenter implements LoginContract.Presenter, LoginContract.OnLoginListener {
        private LoginContract.View mLoginView;
        private LoginInteractor mLoginInteractor;

        public LoginPresenter(LoginContract.View loginView) {
            this.mLoginView = loginView;
            mLoginInteractor = new LoginInteractor(this);
        }

        @Override
        public void login(Activity activity, String email, String password) {
            mLoginInteractor.performFirebaseLogin(activity, email, password);
        }

        @Override
        public void onSuccess(String message) {
            mLoginView.onLoginSuccess(message);
        }

        @Override
        public void onFailure(String message) {
            mLoginView.onLoginFailure(message);
        }
    }
}
