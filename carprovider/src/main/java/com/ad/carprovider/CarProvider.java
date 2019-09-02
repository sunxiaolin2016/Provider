package com.ad.carprovider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.UserHandle;
import android.util.Log;
import com.ad.carlib.common.UriDefine;
import java.util.HashMap;
import java.util.List;

public class CarProvider extends ContentProvider {
    private static final String TAG = "CarProvider";

    private static volatile Context mContext = null;
    private static volatile ContentResolver mResolver = null;

    private static final int SETTINGS_TABLE = 0;
    private static final int AC_STATUS_TABLE = 1;

    public static final HashMap<String,Integer> mTables;
    static {
        mTables = new HashMap<String,Integer>();
        mTables.put(UriDefine.SETTINGS_TABLE,SETTINGS_TABLE);
    }

    public static Context getProviderContext(){
        return mContext;
    }

    @Override
    public boolean onCreate() {
        mContext = getContext();
        mResolver = mContext.getContentResolver();
        synchronized (SettingsData.SETTINGS_LOCK){
            SettingsData.init(mContext);
        }
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.i(TAG,"query uri = " + uri);
        Cursor c = getCursorByUri(uri,projection);

        if( null != c && null != mResolver ){
            c.setNotificationUri(mResolver,uri);
        }

        return c;
    }

    @Override
    public String getType(Uri uri) {
        Log.i(TAG,"getType uri = " + uri);
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.i(TAG,"insert uri = " + uri);
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.i(TAG,"delete uri = " + uri);
        return 0;
    }

    @Override
    public int update(Uri uri,  ContentValues values, String selection, String[] selectionArgs) {
        Log.i(TAG,"update uri = " + uri);

        return setDataByUri(uri, values);
    }

    private Cursor getCursorByUri(Uri uri, String[] projection) {
        List<String> list = uri.getPathSegments();
        if( null == list || list.isEmpty() ){
            return null;
        }

        int size = list.size();
        Integer table  = mTables.get(list.get(0));
        if( null == table ){
            return null;
        }

        switch (table){
            case SETTINGS_TABLE:
                synchronized (SettingsData.SETTINGS_LOCK) {     // 一次只能有一个线程进入
                    if (size == 2) {
                        return SettingsData.getItemData(list.get(1));
                    } else if (size == 1) {
                        if(projection == null) {
                            return SettingsData.getAllData();
                        } else {
                            return SettingsData.getData(projection);
                        }
                    }
                }
                break;
            case AC_STATUS_TABLE:
                break;
            default:
                break;
        }

        return null;
    }

    private int setDataByUri(Uri uri, ContentValues values) {
        int count = 0;
        List<String> updateList = null;
        List<String> list = uri.getPathSegments();
        if(null == list || list.isEmpty())
            return 0;

        int size = list.size();

        Integer table = mTables.get(list.get(0));
        if(null == table)
            return 0;

        switch(table) {
            case SETTINGS_TABLE:
                synchronized (SettingsData.SETTINGS_LOCK) {// 一次只能有一个线程进入
                    if(size == 2) {
                        count = SettingsData.setItemData(list.get(1), values);
                    } else if(size == 1) {
                        updateList = SettingsData.setData(values);
                    }
                }
                break;
            case AC_STATUS_TABLE:
                break;
        }

        if(mResolver != null) {
            if (null != updateList) {
                //int userHandle = UserHandle.myUserId();
                for (String name : updateList) {
                    if (name != null) {
                        Uri subUri = Uri.withAppendedPath(uri, name);
                        //mResolver.notifyChange(subUri, null, false, userHandle);
                        mResolver.notifyChange(subUri, null, false);
                    }
                }
                return updateList.size();
            } else if (count > 0) {
                //mResolver.notifyChange(uri, null, false, UserHandle.myUserId());
                mResolver.notifyChange(uri, null, false);
            }
        }

        return count;
    }
}