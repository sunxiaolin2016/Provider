package com.ad.carprovider.common;

import android.net.Uri;

public class UriDefine {

    public static final String AUTHORITY = "com.ad.carprovider";
    public static final String URI_HEAD = "content://" + AUTHORITY + "/";

    //table
    public static final String SETTINGS_TABLE = "settings";

    //uri
    public static final String SETTINGS_URI_STRING = URI_HEAD + SETTINGS_TABLE;
    public static final Uri SETTINGS_URI = Uri.parse(SETTINGS_URI_STRING);
}
