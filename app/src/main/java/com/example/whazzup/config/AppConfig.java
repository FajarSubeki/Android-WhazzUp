package com.example.whazzup.config;

public class AppConfig {

    public final static int THREE_DELAY  = 3000;
    public final static String APP_ID = "76521";
    public final static String AUTH_KEY = "sgs99ahqgxzuGRg";
    public final static String AUTH_SECRET = "U6QccsycPDTgtj6";
    public final static String ACCOUNT_KEY = "UFwxoUAGphQddJCQUhrD";
    public final static String DIALOG_EXTRA = "Dialogs";
    public final static String UPDATE_DIALOG_EXTRA = "ChatDialogs";
    public final static String UPDATE_MODE = "Mode";
    public final static String UPDATE_ADD_MODE= "add";
    public final static String UPDATE_REMOVE_MODE = "remove";
    public final static int REQUEST_CODE = 1000;
    public final static int SELECT_PICTURE = 7171;

    public static boolean isNullOrEmptyString(String content)
    {
        return(content != null && !content.trim().isEmpty()?false:true);
    }

}
