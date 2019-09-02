package com.ad.carlib.source;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public abstract class BaseService extends Service implements OnServiceToClientListener{
    private static final String TAG = "BaseService";

    private CarClient mCarClient = null;

    abstract public SourceID getSource();

    @Override
    public IBinder onBind(Intent intent) {
        return mCarClient;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if( null == mCarClient || !mCarClient.isBindServce() ){
            mCarClient = new CarClient(this,getSource().ordinal(),this);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"shine:onStartCommand");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"shine:onDestroy mCarClient=" + mCarClient);
        if( null != mCarClient ){
            mCarClient.destroySource();
            mCarClient = null;
        }
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onStop(){
    }

    public Bundle requestAction(boolean bDirectCall,int action,Bundle bundle){
        if( null != mCarClient ){
            Log.i(TAG,"shine,requestAction,action = " + action);
            return mCarClient.requestAction(bDirectCall,action,bundle);
        }

        Log.i(TAG,"shine,requestAction null");
        return null;
    }
}