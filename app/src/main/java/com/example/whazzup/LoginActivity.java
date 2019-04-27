package com.example.whazzup;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whazzup.base.BaseActivity;
import com.example.whazzup.config.AppConfig;
import com.example.whazzup.helper.PrefManager;
import com.quickblox.auth.session.QBSettings;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.shashank.sony.fancytoastlib.FancyToast;

public class LoginActivity extends BaseActivity {

    TextView register;
    MaterialEditText username, password;
    Button login;
    PrefManager sharedPrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        requestRuntimePermission();

        init();
        event();
        initializeFramework();
        super.setHideActionBar();
    }

    private void requestRuntimePermission() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, AppConfig.REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case AppConfig.REQUEST_CODE:
            {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
            break;
        }
    }

    private void initializeFramework() {
        QBSettings.getInstance().init(getApplicationContext(), AppConfig.APP_ID, AppConfig.AUTH_KEY, AppConfig.AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(AppConfig.ACCOUNT_KEY);
    }

    private void init()
    {
        register = findViewById(R.id.tv_register);
        username = findViewById(R.id.et_username);
        password = findViewById(R.id.et_password);
        login = findViewById(R.id.btn_login);

    }

    private void event() {
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String user = username.getText().toString();
                final String pass = password.getText().toString();

                if (user.isEmpty() && pass.isEmpty()){
                    Toast.makeText(LoginActivity.this, "Lengkapi Field", Toast.LENGTH_SHORT).show();
                }else{
                    QBUser qbUser = new QBUser(user, pass);

                    QBUsers.signIn(qbUser).performAsync(new QBEntityCallback<QBUser>() {
                        @Override
                        public void onSuccess(QBUser qbUser, Bundle bundle) {
                            Toast.makeText(LoginActivity.this, "Login Berhasil", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), ChatDialogActivity.class);
//                            intent.putExtra("user", user);
//                            intent.putExtra("password", pass);
                            sharedPrefManager = new PrefManager(getBaseContext());
                            sharedPrefManager.saveIsLoggedIn(getBaseContext(), true);
                            sharedPrefManager.saveUser(getBaseContext(), user);
                            sharedPrefManager.savePassword(getBaseContext(), pass);
                            startActivity(intent);
                            finish(); // close login activity after logged
                        }

                        @Override
                        public void onError(QBResponseException e) {
                            Toast.makeText(LoginActivity.this, "Login Gagal " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}
