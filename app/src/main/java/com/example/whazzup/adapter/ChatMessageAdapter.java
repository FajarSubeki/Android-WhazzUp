package com.example.whazzup.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.whazzup.R;
import com.example.whazzup.holder.QBUsersHolder;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBChatMessage;

import java.util.ArrayList;

public class ChatMessageAdapter extends BaseAdapter {


    private Context context;
    private ArrayList<QBChatMessage> qbChatMessages;

    public ChatMessageAdapter(Context context, ArrayList<QBChatMessage> qbChatMessages) {
        this.context = context;
        this.qbChatMessages = qbChatMessages;
    }

    @Override
    public int getCount() {
        return qbChatMessages.size();
    }

    @Override
    public Object getItem(int position) {
        return qbChatMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (qbChatMessages.get(position).getSenderId().equals(QBChatService.getInstance().getUser().getId())){
                view = inflater.inflate(R.layout.list_send_message, null);
                TextView textView = view.findViewById(R.id.message_content);
                textView.setText(qbChatMessages.get(position).getBody());
            }else{
                view = inflater.inflate(R.layout.list_receive_message, null);
                TextView textView = view.findViewById(R.id.message_content);
                textView.setText(qbChatMessages.get(position).getBody());

                TextView txtName = view.findViewById(R.id.message_user);
                txtName.setText(QBUsersHolder.getInstance().getUserById(qbChatMessages.get(position).getSenderId()).getFullName());

            }
        }
        return view;
    }
}
