package com.ad.carlib.source;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.ad.carlib.source.aidl.ICarClient;
import com.ad.carlib.source.aidl.ICarService;

public class CarClient extends ICarClient.Stub {
    private static final String TAG = "CarClient";

    private Context mContext = null;
    private ICarService mCarService = null;
    private int mSource = SourceID.SOURCE_NONE.ordinal();
    private int mBindID = 0;
    private boolean mBindService = false;

    public static final int CMD_SOURCE_START = 1;
    public static final int CMD_SOURCE_STOP = 2;

    public static final String CAR_CONTROL_PACKAGE_NAME = "com.ad.carcontrol";
    public static final String CAR_CONTROL_SERVICE_NAME = "com.ad.carcontrol.CarControlService";

    private OnServiceToClientListener mSourceListener = null;

    public CarClient(Context context,int source,OnServiceToClientListener listener) {
        mContext = context;
        mSource = source;
        mSourceListener = listener;
        if( !mBindService && null != mContext && null != mConnection ){
            Intent intent = new Intent("com.ad.carcontrol.CarControlService");
            ComponentName cn = new ComponentName(CAR_CONTROL_PACKAGE_NAME,CAR_CONTROL_SERVICE_NAME);
            intent.setComponent(cn);
            mBindService = mContext.bindService(intent,mConnection,Context.BIND_AUTO_CREATE);
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mCarService = ICarService.Stub.asInterface(iBinder);
            if( null != mCarService )
                createSource();

            Log.i(TAG,"onServiceConnected.");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mCarService = null;
            Log.i(TAG,"onServiceDisconnected.");
        }
    };

    public void createSource(){
        if( null == mCarService )
            return;

        try {
            mBindID = mCarService.createSource(mSource,this);

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void destroySource(){
        Log.i(TAG,"shine:destroySource");
        if( null != mCarService ) {
            try {
                mCarService.destroySource(mSource,mBindID);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            if( mBindService && null != mContext && null != mConnection ){
                mContext.unbindService(mConnection);
                mBindService = false;
                mCarService = null;
            }
        }
    }

    public boolean isBindServce() {
        return mCarService != null ? true : false;
    }

    @Override
    public void onSourceAction(int cmd) throws RemoteException {
        if( null == mSourceListener )
            return;

        switch (cmd){
            case CMD_SOURCE_START:
                break;
            case CMD_SOURCE_STOP:
                mSourceListener.onStop();
                break;
            default:
                break;
        }
    }

    public Bundle requestAction(boolean bDirectCall,int action,Bundle bundle){
        if( null == mCarService ){
            Log.i(TAG,"shine,requestAction,mCarService == null");
            return null;
        }

        Bundle ret = new Bundle();
        try {
            Log.i(TAG,"shine,mCarService.sourceAction");
            ret = mCarService.sourceAction(bDirectCall,mSource,action,bundle);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return ret;
    }
}