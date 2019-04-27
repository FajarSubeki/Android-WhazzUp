package com.example.whazzup;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whazzup.base.BaseActivity;
import com.example.whazzup.common.Common;
import com.example.whazzup.config.AppConfig;
import com.example.whazzup.helper.PrefManager;
import com.example.whazzup.holder.QBUsersHolder;
import com.quickblox.chat.QBChat;
import com.quickblox.chat.QBChatService;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class UserProfileActivity extends BaseActivity {

    MaterialEditText full_name, email, no_hp, old_password, new_password;
    Button btnUpdate, btnCancel;
    ImageView user_avatar;
    Bitmap FixBitmap;
    TextView ganti;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        super.setUpActionBar("Profile");
        init();
        event();
        loadUserProfile();
    }

    private void loadUserProfile() {

        //Load avatar
        QBUsers.getUser(QBChatService.getInstance().getUser().getId())
                .performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        //save to cache
                        QBUsersHolder.getInstance().putUser(qbUser);
                        if (qbUser.getFileId() != null){
                            int profilePictureId = qbUser.getFileId();
                            QBContent.getFile(profilePictureId)
                                    .performAsync(new QBEntityCallback<QBFile>() {
                                        @Override
                                        public void onSuccess(QBFile qbFile, Bundle bundle) {
                                            String fileUrl = qbFile.getPublicUrl();
                                            Picasso.with(getBaseContext())
                                                    .load(fileUrl)
                                                    .into(user_avatar);
                                        }

                                        @Override
                                        public void onError(QBResponseException e) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onError(QBResponseException e) {

                    }
                });

        QBUser currentUser = QBChatService.getInstance().getUser();
        String full_namee = currentUser.getFullName();
        String emaile = currentUser.getEmail();
        String phonee = currentUser.getPhone();

        full_name.setText(full_namee);
        email.setText(emaile);
        no_hp.setText(phonee);
    }

    private void init()
    {
        full_name = findViewById(R.id.et_update_fullname);
        email = findViewById(R.id.et_update_email);
        no_hp = findViewById(R.id.et_update_phonenumber);
        old_password = findViewById(R.id.et_update_oldpasword);
        new_password = findViewById(R.id.et_update_newpasword);
        btnUpdate = findViewById(R.id.btn_update);
        btnCancel = findViewById(R.id.btn_cancel);
        user_avatar = findViewById(R.id.user_avatar);
        ganti = findViewById(R.id.tv_ganti_foto);
    }

    private void event()
    {
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ganti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Pilih Foto"), AppConfig.SELECT_PICTURE);
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = new_password.getText().toString();
                String oldpassword = old_password.getText().toString();
                String fullname = full_name.getText().toString();
                String emaill = email.getText().toString();
                String nohp = no_hp.getText().toString();

                QBUser user = new QBUser();
                user.setId(QBChatService.getInstance().getUser().getId());
                if (!AppConfig.isNullOrEmptyString(oldpassword));
                    user.setOldPassword(oldpassword);
                if (!AppConfig.isNullOrEmptyString(password));
                    user.setPassword(password);
                if (!AppConfig.isNullOrEmptyString(fullname));
                    user.setFullName(fullname);
                if (!AppConfig.isNullOrEmptyString(emaill));
                    user.setEmail(emaill);
                if (!AppConfig.isNullOrEmptyString(nohp));
                    user.setPhone(nohp);

                UserProfileActivity.super.showLoading();
                QBUsers.updateUser(user).performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        Toast.makeText(getApplicationContext(), "Berhasil Update : " + qbUser.getLogin(), Toast.LENGTH_SHORT).show();
                        UserProfileActivity.super.dismissLoading();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(UserProfileActivity.this, "Error : " + e.getMessage() , Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED){
            return;
        }
        if (requestCode == AppConfig.SELECT_PICTURE)
        {
            if (data != null){
                Uri selectedImageUri = data.getData();
                super.showLoading();
                try {
                    FixBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    int nh = (int)(FixBitmap.getHeight() * (512.0 / FixBitmap.getWidth()));
                    InputStream in = getContentResolver().openInputStream(selectedImageUri);
                    final Bitmap bitmap = BitmapFactory.decodeStream(in);
                    bitmap.createScaledBitmap(FixBitmap, 1000, nh, true);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
                    File file = new File(Environment.getExternalStorageDirectory()+"/myimage.png");
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(bos.toByteArray());
                    fos.flush();
                    fos.close();

                    //Get file size
                    final int imageSizeKb = (int)file.length() / 1024;
                    if (imageSizeKb >= (1024*100)){
                        Toast.makeText(this, "Ukuran gambar error", Toast.LENGTH_SHORT).show();
                    }

                    //Upload file to server
                    QBContent.uploadFileTask(file, true, null)
                            .performAsync(new QBEntityCallback<QBFile>() {
                                @Override
                                public void onSuccess(QBFile qbFile, Bundle bundle) {
                                    //Set avatar for user
                                    QBUser user = new QBUser();
                                    user.setId(QBChatService.getInstance().getUser().getId());
                                    user.setFileId(Integer.parseInt(qbFile.getId().toString()));

                                    //Update user
                                    QBUsers.updateUser(user)
                                            .performAsync(new QBEntityCallback<QBUser>() {
                                                @Override
                                                public void onSuccess(QBUser qbUser, Bundle bundle) {
                                                    UserProfileActivity.super.dismissLoading();
                                                    user_avatar.setImageBitmap(bitmap);
                                                }

                                                @Override
                                                public void onError(QBResponseException e) {

                                                }
                                            });
                                }

                                @Override
                                public void onError(QBResponseException e) {

                                }
                            });
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_update_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.user_update_logout:
                logOut();
                break;
            default:
                break;
        }
        return true;
    }

    private void logOut() {
        new PrefManager(getApplicationContext()).clear();
        QBUsers.signOut().performAsync(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                QBChatService.getInstance().logout(new QBEntityCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid, Bundle bundle) {
                        Toast.makeText(UserProfileActivity.this, "Berhasil Logout", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(UserProfileActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // remove all previous activiy
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(QBResponseException e) {

                    }
                });
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }
}
