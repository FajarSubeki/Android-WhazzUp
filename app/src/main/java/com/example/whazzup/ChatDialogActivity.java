package com.example.whazzup;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.whazzup.adapter.ChatDialogAdapter;
import com.example.whazzup.base.BaseActivity;
import com.example.whazzup.common.Common;
import com.example.whazzup.config.AppConfig;
import com.example.whazzup.helper.PrefManager;
import com.example.whazzup.holder.QBChatDialogHolder;
import com.example.whazzup.holder.QBUnreadMessageHolder;
import com.example.whazzup.holder.QBUsersHolder;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.session.BaseService;
import com.quickblox.auth.session.QBSession;
import com.quickblox.auth.session.QBSettings;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.listeners.QBSystemMessageListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ChatDialogActivity extends BaseActivity implements QBSystemMessageListener, QBChatDialogMessageListener {

    FloatingActionButton floatingActionButton;
    ListView lstChatDialog;
    PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_dialog);
        super.setUpActionBar("Dialog Chat");

        initializeFramework();
        init();
        event();
        createSessionForChat();

        registerForContextMenu(lstChatDialog);

        lstChatDialog = findViewById(R.id.list_chat_dialog);
        lstChatDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                QBChatDialog qbChatDialog = (QBChatDialog)lstChatDialog.getAdapter().getItem(position);
                Intent intent = new Intent(ChatDialogActivity.this, ChatMessageActivity.class);
                intent.putExtra(AppConfig.DIALOG_EXTRA, qbChatDialog);
                startActivity(intent);
            }
        });

        loadChatDialogs();

    }

    private void initializeFramework() {
        QBSettings.getInstance().init(getApplicationContext(), AppConfig.APP_ID, AppConfig.AUTH_KEY, AppConfig.AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(AppConfig.ACCOUNT_KEY);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.chat_dialog_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

        switch (item.getItemId())
        {
            case R.id.context_delete_dialog:
                deleteDialog(info.position);
                break;
        }

        return true;
    }

    private void deleteDialog(int index) {
        final QBChatDialog chatDialog = (QBChatDialog)lstChatDialog.getAdapter().getItem(index);
        QBRestChatService.deleteDialog(chatDialog.getDialogId(), false)
                .performAsync(new QBEntityCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid, Bundle bundle) {
                        QBChatDialogHolder.getInstance().removeDialog(chatDialog.getDialogId());
                        ChatDialogAdapter adapter = new ChatDialogAdapter(getBaseContext(), QBChatDialogHolder.getInstance().getAllChatDialogs());
                        lstChatDialog.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(QBResponseException e) {

                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChatDialogs();
    }

    private void init(){
        floatingActionButton = findViewById(R.id.chatdialog_adduser);
        lstChatDialog = findViewById(R.id.list_chat_dialog);
    }

    private void event(){
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ListUserActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadChatDialogs() {
        QBRequestGetBuilder requestGetBuilder = new QBRequestGetBuilder();
        requestGetBuilder.setLimit(100);

        QBRestChatService.getChatDialogs(null, requestGetBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBChatDialog> qbChatDialogs, Bundle bundle) {

                //Put all dialog to cache
                QBChatDialogHolder.getInstance().putDialogs(qbChatDialogs);

                //Unread setting
                Set<String> setIds = new HashSet<>();
                for(QBChatDialog chatDialog:qbChatDialogs)
                    setIds.add(chatDialog.getDialogId());

                //Get message unread
                QBRestChatService.getTotalUnreadMessagesCount(setIds, QBUnreadMessageHolder.getInstance().getBundle())
                        .performAsync(new QBEntityCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer integer, Bundle bundle) {
                                //Save to cache
                                QBUnreadMessageHolder.getInstance().setBundle(bundle);

                                //Refresh List Dialogs
                                ChatDialogAdapter adapter = new ChatDialogAdapter(getBaseContext(), QBChatDialogHolder.getInstance().getAllChatDialogs());
                                lstChatDialog.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onError(QBResponseException e) {

                            }
                        });
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("ERROR", e.getMessage());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_dialog_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.chat_dialog_menu_user:
                showUserProfile();
                break;
            default:
                break;
        }
        return true;
    }

    private void showUserProfile() {
        Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
        startActivity(intent);
    }

    private void createSessionForChat(){
        super.showLoading();

        prefManager = new PrefManager(getBaseContext());

        String user, password;
//        user = getIntent().getStringExtra("user");
//        password = getIntent().getStringExtra("password");
        user = prefManager.getUser();
        password = prefManager.getPassword();
        Log.e("User", user);
        Log.e("Pas", password);

        //Load all user and save to cache
        QBUsers.getUsers(null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                QBUsersHolder.getInstance().putUsers(qbUsers);
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });

        final QBUser qbUser  = new QBUser(user, password);
        QBAuth.createSession(qbUser).performAsync(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                qbUser.setId(qbSession.getUserId());
                try {
                    qbUser.setPassword(BaseService.getBaseService().getToken());
                } catch (BaseServiceException e) {
                    e.printStackTrace();
                }

                QBChatService.getInstance().login(qbUser, new QBEntityCallback() {
                    @Override
                    public void onSuccess(Object o, Bundle bundle) {
                        ChatDialogActivity.super.dismissLoading();

                        QBSystemMessagesManager qbSystemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();
                        qbSystemMessagesManager.addSystemMessageListener(ChatDialogActivity.this);

                        QBIncomingMessagesManager qbIncomingMessagesManager = QBChatService.getInstance().getIncomingMessagesManager();
                        qbIncomingMessagesManager.addDialogMessageListener(ChatDialogActivity.this);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.e("ERROR ", "" + e.getMessage());
                    }
                });
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }

    @Override
    public void processMessage(QBChatMessage qbChatMessage) {
        //Put dialog to cache
        //Because we send system message with content is DialogId
        //So we can get dialog by Dialog Id
        QBRestChatService.getChatDialogById(qbChatMessage.getBody()).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                //Put to cache
                QBChatDialogHolder.getInstance().putDialog(qbChatDialog);
                ArrayList<QBChatDialog> adapterSource = QBChatDialogHolder.getInstance().getAllChatDialogs();
                ChatDialogAdapter adapters = new ChatDialogAdapter(getBaseContext(), adapterSource);
                lstChatDialog.setAdapter(adapters);
                adapters.notifyDataSetChanged();
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }

    @Override
    public void processError(QBChatException e, QBChatMessage qbChatMessage) {
        Log.e("ERROR", "" + e.getMessage());
    }

    @Override
    public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {
        loadChatDialogs();
    }

    @Override
    public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {

    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory( Intent.CATEGORY_HOME );
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, AppConfig.THREE_DELAY);
    }
}
