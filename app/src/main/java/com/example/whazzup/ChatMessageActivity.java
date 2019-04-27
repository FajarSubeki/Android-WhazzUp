package com.example.whazzup;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.bhargavms.dotloader.DotLoader;
import com.example.whazzup.adapter.ChatMessageAdapter;
import com.example.whazzup.base.BaseActivity;
import com.example.whazzup.config.AppConfig;
import com.example.whazzup.holder.QBChatMessageHolder;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.listeners.QBChatDialogParticipantListener;
import com.quickblox.chat.listeners.QBChatDialogTypingListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.model.QBPresence;
import com.quickblox.chat.request.QBDialogRequestBuilder;
import com.quickblox.chat.request.QBMessageGetBuilder;
import com.quickblox.chat.request.QBMessageUpdateBuilder;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestUpdateBuilder;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class ChatMessageActivity extends BaseActivity implements QBChatDialogMessageListener{

    QBChatDialog qbChatDialog;
    ListView lstChatMessages;
    ImageButton submitButton, emojibutton;
    EmojiconEditText edtContent;

    ChatMessageAdapter adapter;

    //Variable for edit/delete message
    int contextMenuIndexClicked = -1;
    boolean isEditMode = false;
    QBChatMessage editMessage;

    DotLoader dotLoader;
    Bitmap FixBitmap;

    Toolbar toolbar;
    View rootview;
    EmojIconActions emojIcon;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        qbChatDialog.removeMessageListrener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        qbChatDialog.removeMessageListrener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_message);

        init();
        initChatDialog();
        retrieveMessage();


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!edtContent.getText().toString().isEmpty()){
                if (!isEditMode) {
                    QBChatMessage chatMessage = new QBChatMessage();
                    chatMessage.setBody(edtContent.getText().toString());
                    chatMessage.setSenderId(QBChatService.getInstance().getUser().getId());
                    chatMessage.setSaveToHistory(true);

                    try {
                        qbChatDialog.sendMessage(chatMessage);
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }

                    //Fix private chat don't show message
                    if (qbChatDialog.getType() == QBDialogType.PRIVATE) {
                        //Cache Message
                        QBChatMessageHolder.getInstance().putMessage(qbChatDialog.getDialogId(), chatMessage);
                        ArrayList<QBChatMessage> messages = QBChatMessageHolder.getInstance().getChatMessageByDialogId(chatMessage.getDialogId());
                        adapter = new ChatMessageAdapter(getBaseContext(), messages);
                        lstChatMessages.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }

                    //Remove text from edittext
                    edtContent.setText("");
                    edtContent.setFocusable(true);
                }else{
                    ChatMessageActivity.super.showLoading();

                    QBMessageUpdateBuilder messageUpdateBuilder = new QBMessageUpdateBuilder();
                    messageUpdateBuilder.updateText(edtContent.getText().toString()).markDelivered().markRead();

                    QBRestChatService.updateMessage(editMessage.getId(), qbChatDialog.getDialogId(), messageUpdateBuilder)
                            .performAsync(new QBEntityCallback<Void>() {
                                @Override
                                public void onSuccess(Void aVoid, Bundle bundle) {
                                    //Refresh data
                                    retrieveMessage();
                                    isEditMode = false; // reset variable
                                    ChatMessageActivity.super.dismissLoading();

                                    //Reset edittext
                                    edtContent.setText("");
                                    edtContent.setFocusable(true);
                                }

                                @Override
                                public void onError(QBResponseException e) {
                                    Toast.makeText(getBaseContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                }
            }
        });

    }

    private void retrieveMessage() {
        QBMessageGetBuilder messageGetBuilder = new QBMessageGetBuilder();
        messageGetBuilder.setLimit(500); // get limit 500 messages

        if (qbChatDialog != null)
        {
            QBRestChatService.getDialogMessages(qbChatDialog, messageGetBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatMessage>>() {
                @Override
                public void onSuccess(ArrayList<QBChatMessage> qbChatMessages, Bundle bundle) {
                    //Put message to cache
                    QBChatMessageHolder.getInstance().putMessages(qbChatDialog.getDialogId(), qbChatMessages);
                    adapter = new ChatMessageAdapter(getBaseContext(), qbChatMessages);
                    lstChatMessages.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onError(QBResponseException e) {

                }
            });
        }
    }

    private void initChatDialog() {
        qbChatDialog = (QBChatDialog)getIntent().getSerializableExtra(AppConfig.DIALOG_EXTRA);

        qbChatDialog.initForChat(QBChatService.getInstance());

        //Register listener incoming message
        QBIncomingMessagesManager incomingMessage = QBChatService.getInstance().getIncomingMessagesManager();
        incomingMessage.addDialogMessageListener(new QBChatDialogMessageListener() {
            @Override
            public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {

            }

            @Override
            public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {

            }
        });

        //Add typing listener
        registerTypingForChatDialog(qbChatDialog);

        //Add join group to enable group chat
        if(qbChatDialog.getType() == QBDialogType.PUBLIC_GROUP || qbChatDialog.getType() == QBDialogType.GROUP){
            DiscussionHistory discussionHistory = new DiscussionHistory();
            discussionHistory.setMaxStanzas(0);

            qbChatDialog.join(discussionHistory, new QBEntityCallback() {
                @Override
                public void onSuccess(Object o, Bundle bundle) {

                }

                @Override
                public void onError(QBResponseException e) {
                    Log.e("ERROR", "" + e.getMessage());
                }
            });
        }

        QBChatDialogParticipantListener participantListener = new QBChatDialogParticipantListener() {
            @Override
            public void processPresence(String dialogId, QBPresence qbPresence) {
                if (dialogId == qbChatDialog.getDialogId())
                {
                    QBRestChatService.getChatDialogById(dialogId)
                            .performAsync(new QBEntityCallback<QBChatDialog>() {
                                @Override
                                public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                    //Get online user
                                    try {
                                        Collection<Integer> onlineList = qbChatDialog.getOnlineUsers();
                                        toolbar.setSubtitle(String.format("%d/%d online", onlineList.size(), qbChatDialog.getOccupants().size()));
                                    } catch (XMPPException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onError(QBResponseException e) {

                                }
                            });

                }
            }
        };

        qbChatDialog.addParticipantListener(participantListener);

        qbChatDialog.addMessageListener(this);
        toolbar.setTitle(qbChatDialog.getName());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    private void registerTypingForChatDialog(QBChatDialog qbChatDialog) {
        QBChatDialogTypingListener typingListener = new QBChatDialogTypingListener() {
            @Override
            public void processUserIsTyping(String dialogId, Integer integer) {
                if (dotLoader.getVisibility() != View.VISIBLE)
                    dotLoader.setVisibility(View.VISIBLE);
            }

            @Override
            public void processUserStopTyping(String dialogId, Integer integer) {
                if (dotLoader.getVisibility() != View.INVISIBLE)
                    dotLoader.setVisibility(View.INVISIBLE);
            }
        };

        qbChatDialog.addIsTypingListener(typingListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (qbChatDialog.getType() == QBDialogType.GROUP || qbChatDialog.getType() == QBDialogType.PUBLIC_GROUP)
            getMenuInflater().inflate(R.menu.chat_message_group_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.chat_group_edit_name:
                editNameGroup();
                break;
            case R.id.chat_group_add_user:
                addUser();
                break;
            case R.id.chat_group_remove_user:
                removeUser();
                break;
        }
        return true;
    }

    private void removeUser() {
        Intent intent = new Intent(this, ListUserActivity.class);
        intent.putExtra(AppConfig.UPDATE_DIALOG_EXTRA, qbChatDialog);
        intent.putExtra(AppConfig.UPDATE_MODE, AppConfig.UPDATE_REMOVE_MODE);
        startActivity(intent);
    }

    private void addUser() {
        Intent intent = new Intent(this, ListUserActivity.class);
        intent.putExtra(AppConfig.UPDATE_DIALOG_EXTRA, qbChatDialog);
        intent.putExtra(AppConfig.UPDATE_MODE, AppConfig.UPDATE_ADD_MODE);
        startActivity(intent);
    }

    private void editNameGroup() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_edit_group_layout, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(view);
        final MaterialEditText name = view.findViewById(R.id.et_new_group_name);

        //Set dialog message
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        qbChatDialog.setName(name.getText().toString());

                        QBDialogRequestBuilder requestBuilder = new QBDialogRequestBuilder();
                        QBRestChatService.updateGroupChatDialog(qbChatDialog, requestBuilder)
                                .performAsync(new QBEntityCallback<QBChatDialog>() {
                                    @Override
                                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                        Toast.makeText(ChatMessageActivity.this, "Nama grup berhasil diubah", Toast.LENGTH_SHORT).show();
                                        ChatMessageActivity.super.setUpActionBar(qbChatDialog.getName());
                                    }

                                    @Override
                                    public void onError(QBResponseException e) {
                                        Toast.makeText(getBaseContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        //Create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.chat_message_content_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        //Get index context menu click
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        contextMenuIndexClicked = info.position;

        switch (item.getItemId())
        {
            case R.id.chat_message_update_message:
                updateMessage();
                break;
            case R.id.chat_message_delete_message:
                deleteMessage();
                break;
        }
        return true;
    }

    private void deleteMessage() {

        super.showLoading();

        editMessage = QBChatMessageHolder.getInstance().getChatMessageByDialogId(qbChatDialog.getDialogId())
                .get(contextMenuIndexClicked);

        QBRestChatService.deleteMessage(editMessage.getId(), false).performAsync(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                retrieveMessage();
                ChatMessageActivity.super.dismissLoading();
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });

    }

    private void updateMessage() {
        //Set message for edittext
        editMessage = QBChatMessageHolder.getInstance().getChatMessageByDialogId(qbChatDialog.getDialogId())
                .get(contextMenuIndexClicked);
        edtContent.setText(editMessage.getBody());
        isEditMode = true; // set edit mode to true
    }

    private void init() {
        dotLoader = findViewById(R.id.dot_loader);
        lstChatMessages = findViewById(R.id.list_of_message);
        submitButton = findViewById(R.id.send_button);
        edtContent = findViewById(R.id.edt_content);
        emojibutton = findViewById(R.id.emoji_button);
        rootview = findViewById(R.id.root_view);
        emojIcon = new EmojIconActions(this, rootview, edtContent, emojibutton);
        emojIcon.ShowEmojIcon();
        emojIcon.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {
                Log.e("Keyboard", "open");
            }

            @Override
            public void onKeyboardClose() {
                Log.e("Keyboard", "close");
            }
        });

        edtContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    qbChatDialog.sendIsTypingNotification();
                } catch (XMPPException e) {
                    e.printStackTrace();
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    qbChatDialog.sendStopTypingNotification();
                } catch (XMPPException e) {
                    e.printStackTrace();
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
            }
        });

        toolbar = findViewById(R.id.chat_message_toolbar);

        //Add context menu
        registerForContextMenu(lstChatMessages);
    }

    @Override
    public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {
        //Cache Message
        QBChatMessageHolder.getInstance().putMessage(qbChatMessage.getDialogId(), qbChatMessage);
        ArrayList<QBChatMessage> messages = QBChatMessageHolder.getInstance().getChatMessageByDialogId(qbChatMessage.getDialogId());
        adapter = new ChatMessageAdapter(getBaseContext(), messages);
        lstChatMessages.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {
        Log.e("ERROR", "" + e.getMessage());
    }

}
