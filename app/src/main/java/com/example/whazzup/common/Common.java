package com.example.whazzup.common;

import com.example.whazzup.holder.QBUsersHolder;
import com.quickblox.users.model.QBUser;

import java.util.List;

public class Common {

    public static String craateChatDialogName(List<Integer> qbUsers)
    {
        List<QBUser> qbUsers1 = QBUsersHolder.getInstance().getUsersByIds(qbUsers);
        StringBuilder name = new StringBuilder();
        for(QBUser user:qbUsers1)
            name.append(user.getFullName()).append(" ");
        if (name.length() > 30)
            name = name.replace(30, name.length()-1, "...");
        return name.toString();
    }

}
