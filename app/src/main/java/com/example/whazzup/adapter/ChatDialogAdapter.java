package com.example.whazzup.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.example.whazzup.R;
import com.example.whazzup.holder.QBUnreadMessageHolder;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatDialogAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<QBChatDialog> qbChatDialogs;

    public ChatDialogAdapter(Context context, ArrayList<QBChatDialog> qbChatDialogs) {
        this.context = context;
        this.qbChatDialogs = qbChatDialogs;
    }

    @Override
    public int getCount() {
        return qbChatDialogs.size();
    }

    @Override
    public Object getItem(int position) {
        return qbChatDialogs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_chat_dialog, null);

            TextView txtTitle, txtMessage;
            final ImageView image_unread;
            final CircleImageView imageView;

            txtMessage = view.findViewById(R.id.list_chat_dialog_message);
            txtTitle = view.findViewById(R.id.list_chat_dialog_title);
            imageView = view.findViewById(R.id.iv_chatdialog);
            image_unread = view.findViewById(R.id.iv_undread);

            txtMessage.setText(qbChatDialogs.get(position).getLastMessage());
            txtTitle.setText(qbChatDialogs.get(position).getName());

            if (qbChatDialogs.get(position).getPhoto().equals("null")) {

            } else {
                //Donwload bitmap from server and set for dialog
                QBContent.getFile(Integer.parseInt(qbChatDialogs.get(position).getPhoto()))
                        .performAsync(new QBEntityCallback<QBFile>() {
                            @Override
                            public void onSuccess(QBFile qbFile, Bundle bundle) {
                                String fileUrl = qbFile.getPublicUrl();
                                Picasso.with(context)
                                        .load(fileUrl)
                                        .resize(50, 50)
                                        .centerCrop()
                                        .into(imageView);
                            }

                            @Override
                            public void onError(QBResponseException e) {
                                Log.e("ERROR IMAGE", "" + e.getMessage());
                            }
                        });
                //Set message unread count
                TextDrawable.IBuilder unreadBuilder = TextDrawable.builder().beginConfig()
                        .withBorder(4)
                        .endConfig()
                        .round();
                int unread_count = QBUnreadMessageHolder.getInstance().getBundle().getInt(qbChatDialogs.get(position).getDialogId());
                if (unread_count > 0) {
                    TextDrawable unread_drawable = unreadBuilder.build("" + unread_count, Color.RED);
                    image_unread.setImageDrawable(unread_drawable);
                }
            }
        }
        return view;
    }
}
