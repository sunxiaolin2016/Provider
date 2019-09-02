package com.ad.carlib;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

public class CarContentObserver extends ContentObserver {
    private static final String TAG = "CarContentObserver";

    public static final int MSG_ID_1 = 1;
    public static final int MSG_ID_2 = 2;

    private Handler mHandler = null;
    private String mName = null;
    private CarSettings.OnDataChangeListener mListener = null;
    private Uri mUri = null;

    public CarContentObserver(Handler handler, String name, CarSettings.OnDataChangeListener listener) {
        super(handler);
        // TODO Auto-generated constructor stub
        mHandler = handler;
        mName = name;
        mListener = listener;
    }

    public CarContentObserver(Handler handler, Uri uri, CarSettings.OnDataChangeListener listener) {
        super(handler);
        mHandler = handler;
        mUri = uri;
        mListener = listener;
    }

    public void onDestroy() {
        this.mHandler = null;
        this.mName = null;
        this.mListener = null;
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);

        Log.i("shine","CarContentObserver,onChange,selfChange = " + selfChange);
        Log.i("shine","CarContentObserver,onChange,uri = " + uri);

        if (null != mHandler) {

            if(null != mUri){
                Log.i("shine","CarContentObserver,MSG_ID_2");
                mName = uri.getLastPathSegment();
                mHandler.obtainMessage(MSG_ID_1, this).sendToTarget();
            }

//            if (null != mName) {
//                Log.i("shine","CarContentObserver,MSG_ID_1");
//                mHandler.obtainMessage(MSG_ID_1, this).sendToTarget();
//            }else if(null != mUri){
//                Log.i("shine","CarContentObserver,MSG_ID_2");
//                mName = uri.getLastPathSegment();
//                mHandler.obtainMessage(MSG_ID_1, this).sendToTarget();
//            }
        }
    }

    public String getName(){
        return mName;
    }

    public CarSettings.OnDataChangeListener getListener() {
        return mListener;
    }
}