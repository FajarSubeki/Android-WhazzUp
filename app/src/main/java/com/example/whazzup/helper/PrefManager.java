package com.example.whazzup.helper;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {

    SharedPreferences sharedPreferences;
    Context mContext;
    // shared pref mode
    int PRIVATE_MODE = 0;
    // Shared preferences file name
    private static final String PREF_NAME = "sessionPref";
    SharedPreferences.Editor editor;

    public PrefManager(Context mContext) {
        this.mContext = mContext;
        sharedPreferences = mContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = sharedPreferences.edit();
    }

    public void saveIsLoggedIn(Context context, Boolean isLoggedIn){
        mContext = context;
        sharedPreferences = mContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean ("IS_LOGGED_IN", isLoggedIn);
        editor.commit();

    }

    public boolean getISLogged_IN() {
        //mContext = context;
        sharedPreferences = mContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        return sharedPreferences.getBoolean("IS_LOGGED_IN", false);
    }

    public void saveUser(Context context, String name){
        mContext = context;
        sharedPreferences = mContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("USER", name);
        editor.commit();
    }

    public String getUser(){
        sharedPreferences = mContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        return sharedPreferences.getString("USER", null);
    }

    public void savePassword(Context context, String name){
        mContext = context;
        sharedPreferences = mContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("PASSWORD", name);
        editor.commit();
    }

    public String getPassword(){
        sharedPreferences = mContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        return sharedPreferences.getString("PASSWORD", null);
    }

    public void clear(){
        editor.clear();
        editor.apply();
    }

}
