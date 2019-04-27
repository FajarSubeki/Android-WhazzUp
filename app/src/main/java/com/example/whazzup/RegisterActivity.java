package com.example.whazzup;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whazzup.base.BaseActivity;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.session.QBSession;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.rengwuxian.materialedittext.MaterialEditText;

public class RegisterActivity extends BaseActivity {

    TextView cancel;
    Button sign_up;
    MaterialEditText username, password, fullname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        super.setHideActionBar();
        init();
        event();
        registerSession();
    }

    private void registerSession() {
        QBAuth.createSession().performAsync(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {

            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("ERROR" , e.getMessage());
            }
        });
    }

    private void init()
    {
        cancel = findViewById(R.id.tv_cancel);
        sign_up = findViewById(R.id.btn_register);
        username = findViewById(R.id.et_username_register);
        password = findViewById(R.id.et_password_register);
        fullname = findViewById(R.id.et_full_name_rgister);
    }

    private void event()
    {
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });

        sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = username.getText().toString();
                String pass = password.getText().toString();
                String full_name = fullname.getText().toString();

                QBUser qbUser = new QBUser(user, pass);
                qbUser.setFullName(full_name);

                QBUsers.signUp(qbUser).performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        Toast.makeText(RegisterActivity.this, "Register Berhasil", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(RegisterActivity.this, "Register Gagal" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
