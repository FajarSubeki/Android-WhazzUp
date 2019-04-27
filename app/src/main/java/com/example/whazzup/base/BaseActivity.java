package com.example.whazzup.base;

import android.app.ProgressDialog;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

import com.example.whazzup.R;

public class BaseActivity extends AppCompatActivity {

    ProgressDialog progressDialog;

    public void setUpActionBar(String title)
    {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(title);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
    }

    public void setUpActionBarNoIcon(String title)
    {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(title);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
    }

    public void setHideActionBar()
    {
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    public void showLoading()
    {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Tunggu Sebentar...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void changeStatusBar() {
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        }
    }

    public void dismissLoading()
    {
        progressDialog.dismiss();
    }

}
