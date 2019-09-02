package com.ad.carlib.source;

import android.os.Binder;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import com.ad.carlib.source.aidl.ICarClient;
import com.ad.carlib.source.aidl.ICarService;
import java.util.HashMap;

public class CarService extends ICarService.Stub {
    private static final String TAG = "CarService";

    private final Object LOCK = new Object();
    private static CarService mInstance = null;
    private int mCurrBindID = 1;
    public final HashMap<Integer,ClientInfo> mSourceList = new HashMap<Integer,ClientInfo>();

    public static synchronized CarService getInstance(){
        if( null == mInstance )
            mInstance = new CarService();

        return mInstance;
    }

    @Override
    public int createSource(int source, ICarClient client) throws RemoteException {
        int bindID = 0;
        int pid = Binder.getCallingPid();
        Log.i(TAG,"createSource,pid=" + pid);
        synchronized (LOCK){
            if( mSourceList.containsKey(source) ){
                ClientInfo clientInfo = mSourceList.get(source);
                if( null != clientInfo ){
                    clientInfo.setPid(pid);
                    clientInfo.setCarClient(client);
                    if( clientInfo.getBindID() == 0 ){
                        bindID = mCurrBindID++;
                        clientInfo.setBindID(bindID);
                    }else{
                        bindID = clientInfo.getBindID();
                    }
                }
            }else {
                bindID = mCurrBindID++;
                ClientInfo clientInfo = new ClientInfo(pid,bindID,ClientInfo.CAR_CLIENT_IDLE,client);
                mSourceList.put(source,clientInfo);
            }
        }

        if( null != mOnClientListener ){
            mOnClientListener.onClientCreate(source,pid);
        }

        return bindID;
    }

    @Override
    public void destroySource(int source, int bindID) throws RemoteException {
        if( null != mOnClientListener ){
            mOnClientListener.onClientDestroy(source);
        }

        synchronized (LOCK){
            if( mSourceList.containsKey(source))
                mSourceList.remove(source);
        }
    }

    @Override
    public Bundle sourceAction(boolean bDirectCall, int source, int action, Bundle b) throws RemoteException {
        Log.i(TAG,"sourceAction,source = " + source);
        Log.i(TAG,"sourceAction,action = " + action);

        if( null != mOnClientListener ){
            mOnClientListener.OnClientAction(bDirectCall,source,action,b);
        }
        return null;
    }

    public ICarClient getCarClient(int source){
        ICarClient client = null;
        synchronized (LOCK){
            if( mSourceList.containsKey(source) ){
                ClientInfo info = mSourceList.get(source);
                if( null != info )
                    client = info.getCarClient();
            }
        }

        return client;
    }

    public void setOnClientListener( OnClientListener l ){
        mOnClientListener = l;
    }

    private OnClientListener mOnClientListener;

    public interface OnClientListener{
        public void onClientCreate(int source, int pId);
        public void onClientDestroy(int source);
        public Bundle OnClientAction(boolean bDirectCall,int source,int action,Bundle bundle);
    }
}
