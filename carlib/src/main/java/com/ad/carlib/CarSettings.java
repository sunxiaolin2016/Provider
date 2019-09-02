package com.ad.carlib;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.ad.carlib.common.UriDefine;
import java.util.HashMap;

public class CarSettings {
    private static final String TAG = "CarSettings";
    private ContentResolver mResolver = null;

    public static final String ChangeFactoryFlag = "ChangeFactoryFlag";// 修改工厂设置数据时需要加上此标志才能改变工厂设置数据
    public static final String Mute = "Mute";
    public static final String KeyBeep = "KeyBeep";
    public static final String EQValue = "EQValue";
    public static final String EQBand1 = "EQBand1";
    public static final String EQBand2 = "EQBand2";
    public static final String EQBand3 = "EQBand3";
    public static final String EQBand4 = "EQBand4";
    public static final String EQBand5 = "EQBand5";
    public static final String EQBand6 = "EQBand6";
    public static final String EQBand7 = "EQBand7";
    public static final String EQBand8 = "EQBand8";
    public static final String EQBand9 = "EQBand9";
    public static final String Fader = "Fader";
    public static final String Balance = "Balance";
    public static final String SubWoof = "SubWoof";
    public static final String BackLight = "BackLight";
    public static final String MediaVolume = "MediaVolume";
    public static final String DialVolume = "DialVolume";
    public static final String NaviVolume = "NaviVolume";
    public static final String Bass = "Bass";
    public static final String Midd = "Midd";
    public static final String Treble = "Treble";
    public static final String Loudness = "Loudness";
    public static final String NaviMix = "NaviMix";

    //Air
    public static final String AcOnOff = "AcOnOff";
    public static final String AcAuto = "AcAuto";
    public static final String AcRecycle = "AcRecycle";
    public static final String AcEco = "AcEco";
    public static final String AcDual = "AcDual";
    public static final String AcWindValue = "AcWindValue";
    public static final String AcWindMode = "AcWindMode";
    public static final String AcLeftTemp = "AcLeftTemp";
    public static final String AcRightTemp = "AcRightTemp";
    public static final String AcFrontDefroster = "AcFrontDefroster";
    public static final String AcRearDefroster = "AcRearDefroster";

    public CarSettings(ContentResolver resolver){
        mResolver = resolver;
    }

    public Uri getUri(){
        return UriDefine.SETTINGS_URI;
    }

    public String getUriStr(){
        return UriDefine.SETTINGS_URI_STRING;
    }

    public boolean set(String name,int value){
        return setData(mResolver,getUriStr(),name,value);
    }

    public int get(String name,int defaultValue){
        return getData(mResolver,getUriStr(),name,defaultValue);
    }

    public static boolean setData(ContentResolver resolver, String uriStr, String name, int value) {

        if (null == resolver || null == uriStr || uriStr.isEmpty() || null == name || name.isEmpty()) {
            return false;
        }
        int count = 0;
        final Uri uri = Uri.parse(uriStr + "/" + name);
        ContentValues values = new ContentValues();
        values.put(name, value);

        try {
            count = resolver.update(uri, values, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return count == 1;
    }

    public static int getData(ContentResolver resolver, String uriStr, String name, int defaultValue) {

        if(null == resolver || null == uriStr || uriStr.isEmpty() || null == name || name.isEmpty()) {
            return defaultValue;
        }

        int value = defaultValue;
        final Uri uri = Uri.parse(uriStr + "/" + name);

        try {
            Cursor c = resolver.query(uri, null, null, null, null);

            if(c != null){
                if(c.moveToNext()) {
                    value = c.getInt(c.getColumnIndex("_value"));
                }
                c.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return value;
    }

    public interface OnDataChangeListener{
        public void onChange(String itemName);
    }

    //保存各个应用程序的监听对象
    protected HashMap<String, CarContentObserver> mListeners = new HashMap<String, CarContentObserver>();
    private final Object LISTENER_LOCK = new Object();
    private Handler mMainHandler = null;

    //Observer
    //各个应用程序设置自己的监听
    public void setOnDataChangeListener(OnDataChangeListener listener) {
        if (null == mResolver || null == listener)
            return;

        String name = "";
        synchronized(LISTENER_LOCK) {//一次只能有一个线程进入
            if (null == mMainHandler) {
                createMainHandler();
            }
            if (null != mMainHandler) {
                removeOnDataChangeListener(name);

                final Uri uri = getUri();
                CarContentObserver observer = new CarContentObserver(mMainHandler, uri, listener);
                mResolver.registerContentObserver(uri, true, observer);
                mListeners.put(name, observer);
            }
        }
    }

    private synchronized void createMainHandler() {
        Looper mainLooper = Looper.getMainLooper();
        if (mainLooper == null) {
            mainLooper = Looper.myLooper();
        }
        if (mainLooper != null) {
            mMainHandler = new Handler(mainLooper) {

                @Override
                public void handleMessage(Message msg) {
                    Log.d(TAG, "shine,what = " + msg.what);
                    switch (msg.what) {
                        case CarContentObserver.MSG_ID_1:
                            CarContentObserver observer = (CarContentObserver) msg.obj;
                            if (null != observer) {
                                OnDataChangeListener listener = observer.getListener();
                                if (null != listener) {
                                    Log.i(TAG,"shine,observer.getName()=" + observer.getName() );
                                    listener.onChange(observer.getName());
                                }
                            }
                            break;
                        default:
                            break;
                    }
                }
            };
        }
    }

    public void removeOnDataChangeListener(String name) {
        if(null == name)
            return;

        synchronized(LISTENER_LOCK) {//一次只能有一个线程进入
            if(mListeners.containsKey(name)) {
                CarContentObserver observer = mListeners.get(name);
                if (null != observer && null != mResolver) {
                    observer.onDestroy();
                    mResolver.unregisterContentObserver(observer);
                    if (null != mMainHandler) {//防止remove后还会有消息（比如小挂件更新时间就可能会有很多消息在mMainHandler消息循环中）
                        mMainHandler.removeMessages(CarContentObserver.MSG_ID_1, observer);
                    }
                }
            }
        }
    }
}