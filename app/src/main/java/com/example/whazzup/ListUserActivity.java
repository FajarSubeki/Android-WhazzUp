package com.example.whazzup;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.whazzup.adapter.ListUserAdapter;
import com.example.whazzup.base.BaseActivity;
import com.example.whazzup.common.Common;
import com.example.whazzup.config.AppConfig;
import com.example.whazzup.holder.QBUsersHolder;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.request.QBDialogRequestBuilder;
import com.quickblox.chat.utils.DialogUtils;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class ListUserActivity extends BaseActivity {

    ListView listView;
    Button btnCreate;

    String mode = "";
    QBChatDialog qbChatDialog;
    List<QBUser> userAdd = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_user);

        mode = getIntent().getStringExtra(AppConfig.UPDATE_MODE);
        qbChatDialog=(QBChatDialog)getIntent().getSerializableExtra(AppConfig.UPDATE_DIALOG_EXTRA);
        super.setUpActionBar("Kontak Anda");

        init();

    }

    private void init()
    {
        listView = findViewById(R.id.recycler_userr);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        btnCreate = findViewById(R.id.btn_create_chat);

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mode == null) {

                    int countChoice = listView.getCount();

                    if (listView.getCheckedItemPositions().size() == 1)
                        createPrivateChat(listView.getCheckedItemPositions());
                    else if (listView.getCheckedItemPositions().size() > 1)
                        createGroupChat(listView.getCheckedItemPositions());
                    else
                        Toast.makeText(ListUserActivity.this, "Pilih dulu teman untuk memulai chat", Toast.LENGTH_SHORT).show();

                } else if (mode.equals(AppConfig.UPDATE_ADD_MODE) && qbChatDialog != null) {
                    if (userAdd.size() > 0) {
                        QBDialogRequestBuilder requestBuilder = new QBDialogRequestBuilder();
                        int cntChoice = listView.getCount();
                        SparseBooleanArray checkItemPositions = listView.getCheckedItemPositions();
                        for (int i = 0; i < cntChoice; i++) {
                            if (checkItemPositions.get(i)) {
                                QBUser user = (QBUser) listView.getItemAtPosition(i);
                                requestBuilder.addUsers(user);
                            }
                        }

                        //Call services
                        QBRestChatService.updateGroupChatDialog(qbChatDialog, requestBuilder)
                                .performAsync(new QBEntityCallback<QBChatDialog>() {
                                    @Override
                                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                        Toast.makeText(ListUserActivity.this, "Anggota Baru berhasil dibuat", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }

                                    @Override
                                    public void onError(QBResponseException e) {

                                    }
                                });
                    }
                } else if (mode.equals(AppConfig.UPDATE_REMOVE_MODE) && qbChatDialog != null) {
                    if (userAdd.size() > 0) {
                        QBDialogRequestBuilder requestBuilder = new QBDialogRequestBuilder();
                        int cntChoice = listView.getCount();
                        SparseBooleanArray checkItemPositions = listView.getCheckedItemPositions();
                        for (int i = 0; i < cntChoice; i++) {
                            if (checkItemPositions.get(i)) {
                                QBUser user = (QBUser) listView.getItemAtPosition(i);
                                requestBuilder.removeUsers(user);
                            }
                        }

                        //Call services
                        QBRestChatService.updateGroupChatDialog(qbChatDialog, requestBuilder)
                                .performAsync(new QBEntityCallback<QBChatDialog>() {
                                    @Override
                                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                        Toast.makeText(ListUserActivity.this, "Anggota berhasil dihapus", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }

                                    @Override
                                    public void onError(QBResponseException e) {

                                    }
                                });
                    }
                }
            }
        });

        if (mode == null && qbChatDialog == null)
            retrieveAllUser();
        else{
            if (mode.equals(AppConfig.UPDATE_ADD_MODE))
                loadListAvailabeUser();
            else if(mode.equals(AppConfig.UPDATE_REMOVE_MODE))
                loadListUserInGroup();
        }
    }

    private void loadListUserInGroup() {
        btnCreate.setText("Keluarkan Anggota");
        QBRestChatService.getChatDialogById(qbChatDialog.getDialogId())
                .performAsync(new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                        List<Integer> occupantsId = qbChatDialog.getOccupants();
                        List<QBUser> listUserAlreadyInGroup = QBUsersHolder.getInstance().getUsersByIds(occupantsId);
                        ArrayList<QBUser> users = new ArrayList<>();
                        users.addAll(listUserAlreadyInGroup);

                        ListUserAdapter adapter = new ListUserAdapter(getBaseContext(), users);
                        listView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        userAdd = users;
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(ListUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadListAvailabeUser() {
        btnCreate.setText("Tambah Anggota");
        QBRestChatService.getChatDialogById(qbChatDialog.getDialogId())
                .performAsync(new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                        ArrayList<QBUser> listUsers = QBUsersHolder.getInstance().getAllUser();
                        List<Integer> occupantsId = qbChatDialog.getOccupants();
                        List<QBUser> listUserAlreadyInChatGroup = QBUsersHolder.getInstance().getUsersByIds(occupantsId);

                        //Remove all user already join in chat group
                        for(QBUser user:listUserAlreadyInChatGroup)
                            listUsers.remove(user);
                        if(listUsers.size() > 0)
                        {
                            ListUserAdapter adapter = new ListUserAdapter(getBaseContext(), listUsers);
                            listView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                            userAdd = listUsers;
                        }
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(ListUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createGroupChat(SparseBooleanArray checkedItemPositions) {
        super.showLoading();

        int coutChoice = listView.getCount();
        ArrayList<Integer> occupantIdsList = new ArrayList<>();
        for(int i=0; i<coutChoice; i++){
            if(checkedItemPositions.get(i))
            {
                QBUser user = (QBUser)listView.getItemAtPosition(i);
                occupantIdsList.add(user.getId());
            }
        }

        //Create Chat Dialog
        QBChatDialog dialog = new QBChatDialog();
        dialog.setName(Common.craateChatDialogName(occupantIdsList));
        dialog.setType(QBDialogType.GROUP);
        dialog.setOccupantsIds(occupantIdsList);

        QBRestChatService.createChatDialog(dialog).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                Toast.makeText(ListUserActivity.this, "Dialog chat sukses dibuat", Toast.LENGTH_SHORT).show();

                //Send system message to recipient id user
                QBSystemMessagesManager qbSystemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();
                QBChatMessage qbChatMessage = new QBChatMessage();
                qbChatMessage.setBody(qbChatDialog.getDialogId());
                for(int i=0; i<qbChatDialog.getOccupants().size(); i++)
                {
                    qbChatMessage.setRecipientId(qbChatDialog.getOccupants().get(i));
                    try {
                        qbSystemMessagesManager.sendSystemMessage(qbChatMessage);
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }
                }


                finish();
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("ERROR", e.getMessage());
            }
        });
    }


    private void createPrivateChat(SparseBooleanArray checkedItemPositions) {

        super.showLoading();

        int countChoice = listView.getCount();
        for(int i = 0; i<countChoice; i++)
        {
            if(checkedItemPositions.get(i))
            {
                final QBUser user = (QBUser)listView.getItemAtPosition(i);
                QBChatDialog dialog = DialogUtils.buildPrivateDialog(user.getId());

                QBRestChatService.createChatDialog(dialog).performAsync(new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                        ListUserActivity.super.dismissLoading();
                        Toast.makeText(ListUserActivity.this, "Dialog chat sukses dibuat", Toast.LENGTH_SHORT).show();

                        //Send system message to recipient id user
                        QBSystemMessagesManager qbSystemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();
                        QBChatMessage qbChatMessage = new QBChatMessage();
                        qbChatMessage.setRecipientId(user.getId());
                        qbChatMessage.setBody(qbChatDialog.getDialogId());
                        try {
                            qbSystemMessagesManager.sendSystemMessage(qbChatMessage);
                        } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                        }

                        finish();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.e("ERROR", e.getMessage());
                    }
                });
            }
        }

    }

    private void retrieveAllUser()  {

        QBUsers.getUsers(null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {

                //Add to chache
                QBUsersHolder.getInstance().putUsers(qbUsers);

                ArrayList<QBUser> qbUserWithoutCurrent = new ArrayList<QBUser>();
                for(QBUser user : qbUsers)
                {
                    if (!user.getLogin().equals(QBChatService.getInstance().getUser().getLogin()))
                        qbUserWithoutCurrent.add(user);
                }

                ListUserAdapter adapter = new ListUserAdapter(getBaseContext(), qbUserWithoutCurrent);
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("ERROR", e.getMessage());
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
