package com.ad.carprovider;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.MatrixCursor;
import android.util.Log;

import com.ad.carlib.CarSettings;
import com.ad.carprovider.util.SettingsItem;
import com.ad.carprovider.util.SettingsXmlTool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SettingsData {
    private static final String TAG = "SettingsData";

    public static final String[] COLUMNS = new String[]{"_name","_value","_flag"};

    public static final Object SETTINGS_LOCK = new Object();

    public static final String mFactoryXmlFilePath = "cardata/static/settings/Settings.xml";

    public static final HashMap<String, SettingsItem> mSettings;
    public static HashMap<String, SettingsItem> mResetSettings = null;  //用于复位(上点第一次开机复位、恢复出厂设置)
    private static volatile boolean mFirstPowerOnReseted = false;//是否已经开机复位

    static {
        mSettings = new HashMap<String,SettingsItem>();
        mSettings.put(CarSettings.Mute,new SettingsItem(0, 4));
        mSettings.put(CarSettings.KeyBeep,new SettingsItem(0, 4));
        mSettings.put(CarSettings.EQValue,new SettingsItem(0, 4));
        mSettings.put(CarSettings.EQBand1,new SettingsItem(12, 4));
        mSettings.put(CarSettings.EQBand2,new SettingsItem(12, 4));
        mSettings.put(CarSettings.EQBand3,new SettingsItem(12, 4));
        mSettings.put(CarSettings.EQBand4,new SettingsItem(12, 4));
        mSettings.put(CarSettings.EQBand5,new SettingsItem(12, 4));
        mSettings.put(CarSettings.EQBand6,new SettingsItem(12, 4));
        mSettings.put(CarSettings.EQBand7,new SettingsItem(12, 4));
        mSettings.put(CarSettings.EQBand8,new SettingsItem(12, 4));
        mSettings.put(CarSettings.EQBand9,new SettingsItem(12, 4));

        mSettings.put(CarSettings.Fader,new SettingsItem(0, 4));
        mSettings.put(CarSettings.Balance,new SettingsItem(0, 4));
        mSettings.put(CarSettings.SubWoof,new SettingsItem(0, 4));
        mSettings.put(CarSettings.BackLight,new SettingsItem(255, 4));
        mSettings.put(CarSettings.MediaVolume,new SettingsItem(8, 4));
        mSettings.put(CarSettings.DialVolume,new SettingsItem(8, 4));
        mSettings.put(CarSettings.NaviVolume,new SettingsItem(8, 4));
        mSettings.put(CarSettings.Bass,new SettingsItem(0, 4));
        mSettings.put(CarSettings.Midd,new SettingsItem(0, 4));
        mSettings.put(CarSettings.Treble,new SettingsItem(0, 4));
        mSettings.put(CarSettings.Loudness,new SettingsItem(0, 4));
        mSettings.put(CarSettings.NaviMix,new SettingsItem(0, 4));

        //Air
        mSettings.put(CarSettings.AcOnOff,new SettingsItem(0, 4));
        mSettings.put(CarSettings.AcAuto,new SettingsItem(0, 4));
        mSettings.put(CarSettings.AcRecycle,new SettingsItem(0, 4));
        mSettings.put(CarSettings.AcEco,new SettingsItem(0, 4));
        mSettings.put(CarSettings.AcDual,new SettingsItem(0, 4));
        mSettings.put(CarSettings.AcWindValue,new SettingsItem(0, 4));
        mSettings.put(CarSettings.AcWindMode,new SettingsItem(0, 4));
        mSettings.put(CarSettings.AcLeftTemp,new SettingsItem(0, 4));
        mSettings.put(CarSettings.AcRightTemp,new SettingsItem(0, 4));
    }

    private static volatile boolean mSettingsLoad = false;

    public static void init(Context context){
        if( null == context ){
            return;
        }

        loadSettingsData();
    }

    private static synchronized boolean loadSettingsData() {
        if (mSettingsLoad)
            return true;

        if(readXmlData()) {
            backupXml();
        }else {
            if(!readBackupXmlData()) {
                Log.e(TAG, "@@##@@read BackupXml Data error....");
                //return false;
            }
        }

        mResetSettings = new HashMap<String, SettingsItem>();
        Iterator<Map.Entry<String, SettingsItem>> iterator = mSettings.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, SettingsItem> entry = iterator.next();
            SettingsItem item = entry.getValue();
            if (null != item) {
                mResetSettings.put(entry.getKey(), new SettingsItem(item.value, item.flag));
            }
        }

        readPreferencesData();
        mSettingsLoad = true;
        return true;
    }

    public static List<String> setData(ContentValues values) {
        if (!mSettingsLoad) {
            loadSettingsData();
        }

        List<String> retList = new ArrayList<String>();
        if (null == values)
            return null;
        Set<String> keySet = values.keySet();
        if (null == keySet)
            return null;

        if(keySet.contains(CarSettings.ChangeFactoryFlag)) {
            for (String key : keySet) {
                SettingsItem item = mSettings.get(key);
                Integer value = values.getAsInteger(key);
                if (null != item && null != value && value != item.value) {
                    item.value = value;
                    retList.add(key);
                }
            }

            setFactoryData(retList);
        } else {
            for (String key : keySet) {
                SettingsItem item = mSettings.get(key);
                Integer value = values.getAsInteger(key);
                if (null != item && null != value && value != item.value && !SettingsItem.isFactoryData(item.flag)) {// 工厂设置数据只能通过专门的方式改变
                    item.value = value;
                    retList.add(key);
                }
            }
        }

        if(!retList.isEmpty()) {
            //aquestDelaySave();
            saveSettingsData();
        }
        return retList;
    }

    public static int setItemData(String name, ContentValues values) {
        if (!mSettingsLoad) {
            loadSettingsData();
        }
        if (null == name || null == values)
            return 0;

        SettingsItem item = mSettings.get(name);
        Integer value = values.getAsInteger(name);
        if(null != item && null != value && value != item.value && !SettingsItem.isFactoryData(item.flag)) {//工厂设置数据只能通过专门的方式改变
            item.value = value;
            //aquestDelaySave();
            saveSettingsData();
            return 1;
        }

        return 0;
    }

    public static MatrixCursor getItemData(String name) {
        if (!mSettingsLoad) {
            loadSettingsData();
        }
        MatrixCursor cursor = new MatrixCursor(COLUMNS);
        if (null != name) {
            SettingsItem item = mSettings.get(name);
            if (null != item) {
                Object[] objs = new Object[] { name, item.value, item.flag };
                cursor.addRow(objs);
            }
        }
        return cursor;
    }

    public static MatrixCursor getData(String[] projection) {
        if (!mSettingsLoad) {
            loadSettingsData();
        }

        MatrixCursor cursor = new MatrixCursor(COLUMNS);
        if (null != projection) {
            for(String name : projection) {
                if(name != null) {
                    SettingsItem item = mSettings.get(name);
                    if (null != item) {
                        Object[] objs = new Object[] { name, item.value, item.flag };
                        cursor.addRow(objs);
                    }
                }
            }
        }
        return cursor;
    }

    public static MatrixCursor getAllData() {
        if (!mSettingsLoad) {
            loadSettingsData();
        }
        MatrixCursor cursor = new MatrixCursor(COLUMNS);
        Iterator<Map.Entry<String, SettingsItem>> iterator = mSettings.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, SettingsItem> entry = iterator.next();
            SettingsItem item = entry.getValue();
            if (null != item) {
                Object[] objs = new Object[] { entry.getKey(), item.value, item.flag };
                cursor.addRow(objs);
            }
        }
        return cursor;
    }

    private static synchronized boolean saveSettingsData() {
        Context context = CarProvider.getProviderContext();
        if (null == context)
            return false;

        SharedPreferences sharedata = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        if (null != sharedata) {
            SharedPreferences.Editor editor = sharedata.edit();
            if (null != editor) {
                Iterator<Map.Entry<String, SettingsItem>> iterator = mSettings.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, SettingsItem> entry = iterator.next();
                    SettingsItem item = entry.getValue();
                    if(null != item) {
                        editor.putInt(entry.getKey(), item.value);
                    }
                }
                editor.commit();
            }
        }

        return true;
    }

    private static boolean readXmlData() {
        Log.v(TAG, "ReadFactoryData....");
        HashMap<String, SettingsItem> srcList = SettingsXmlTool.readFile(mFactoryXmlFilePath);
        if(null == srcList || srcList.isEmpty()) {
            Log.e(TAG, "@@##@@read xml file error....");
            return false;
        }

        Iterator<Map.Entry<String, SettingsItem>> iterator = srcList.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, SettingsItem> entry = iterator.next();
            SettingsItem item = entry.getValue();
            if(null != item) {
                mSettings.put(entry.getKey(), item);
            }
        }

        return true;
    }

    public static boolean backupXml() {
        Context context = CarProvider.getProviderContext();
        if (null == context)
            return false;

        SharedPreferences sharedata = context.getSharedPreferences("settings_bacup_flag", Context.MODE_PRIVATE);
        if (null == sharedata)
            return false;

        boolean isBackup = sharedata.getBoolean("isBackup", false);
        if(isBackup) {
            return true;
        }

        SharedPreferences sharedata2 = context.getSharedPreferences("settings_bacup", Context.MODE_PRIVATE);
        if (null != sharedata2) {
            SharedPreferences.Editor editor2 = sharedata2.edit();
            if (null != editor2) {
                Iterator<Map.Entry<String, SettingsItem>> iterator = mSettings.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, SettingsItem> entry = iterator.next();
                    SettingsItem item = entry.getValue();
                    if(null != item) {
                        editor2.putString(entry.getKey(), String.valueOf(item.value) + ',' + item.flag);
                    }
                }
                editor2.commit();

                //标志已备份
                SharedPreferences.Editor editor = sharedata.edit();
                if (null != editor) {
                    editor.putBoolean("isBackup", true);
                    editor.commit();
                }
                return true;
            }
        }

        return false;
    }

    public static boolean readBackupXmlData() {
        Map<String, String> srcList = readBackupXml();
        if (null == srcList || srcList.isEmpty())
            return false;

        Iterator<Map.Entry<String, SettingsItem>> iterator = mSettings.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, SettingsItem> entry = iterator.next();
            SettingsItem item = entry.getValue();
            String str = srcList.get(entry.getKey());
            if(null != item && null != str) {
                String[] strs = str.split(",");
                if(null != strs && strs.length == 2
                        && strs[0] != null && strs[0] != null) {
                    item.value = Integer.parseInt(strs[0]);
                    item.flag = Integer.parseInt(strs[1]);
                }
            }
        }

        return true;
    }

    public static Map<String, String> readBackupXml() {
        Context context = CarProvider.getProviderContext();
        if (null == context)
            return null;

        SharedPreferences sharedata = context.getSharedPreferences("settings_bacup_flag", Context.MODE_PRIVATE);
        if (null == sharedata)
            return null;

        boolean isBackup = sharedata.getBoolean("isBackup", false);
        if(!isBackup) {
            return null;
        }

        Map<String, String> srcList = null;
        SharedPreferences sharedata2 = context.getSharedPreferences("settings_bacup", Context.MODE_PRIVATE);
        if (null != sharedata2) {
            try {
                Map<String, String> list = (Map<String, String>) sharedata2.getAll();
                srcList = list;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return srcList;
    }

    private static boolean readPreferencesData() {
        Context context = CarProvider.getProviderContext();
        if (null == context)
            return false;
        Map<String, Integer> srcList = null;
        SharedPreferences sharedata = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        if (null != sharedata) {
            try {
                Map<String, Integer> list = (Map<String, Integer>) sharedata.getAll();
                srcList = list;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (null == srcList || srcList.isEmpty())
            return false;

        //Integer firstPowerOn = StatusData.mStatus.get(SttStatus.firstPowerOn);
        Integer firstPowerOn = 1;
        if (null != firstPowerOn && firstPowerOn == 1) {
            Iterator<Map.Entry<String, SettingsItem>> iterator = mSettings.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, SettingsItem> entry = iterator.next();
                SettingsItem item = entry.getValue();
                if(null != item && !SettingsItem.isPowerOnReset(item.flag)) {
                    Integer value = srcList.get(entry.getKey());
                    if(null != value) {
                        item.value = value;
                    }
                }
            }
            mFirstPowerOnReseted = true;
        } else {
            Iterator<Map.Entry<String, SettingsItem>> iterator = mSettings.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, SettingsItem> entry = iterator.next();
                SettingsItem item = entry.getValue();
                Integer value = srcList.get(entry.getKey());
                if(null != item && null != value) {
                    item.value = value;
                }
            }
        }

        return true;
    }

    private static boolean setFactoryData(List<String> updateList) {
        if(null == updateList || updateList.isEmpty())
            return false;

        HashMap<String, SettingsItem> srcList = SettingsXmlTool.readFile(mFactoryXmlFilePath);
        if(null == srcList) {
            srcList = new HashMap<String, SettingsItem>();
        }

        for (String name : updateList) {
            if (name != null) {
                SettingsItem item = mSettings.get(name);
                if(null != item && SettingsItem.isFactoryData(item.flag)) {
                    srcList.put(name, item);
                }
            }
        }

        SettingsXmlTool.saveFile(mFactoryXmlFilePath, srcList);
        deleteBackupXml();
        return true;
    }

    public static boolean deleteBackupXml() {
        Context context = CarProvider.getProviderContext();
        if (null == context)
            return false;

        //标志未备份
        SharedPreferences sharedata = context.getSharedPreferences("settings_bacup_flag", Context.MODE_PRIVATE);
        if (null != sharedata) {
            SharedPreferences.Editor editor = sharedata.edit();
            if (null != editor) {
                editor.putBoolean("isBackup", false);
                editor.commit();
            }
        }

        SharedPreferences sharedata2 = context.getSharedPreferences("settings_bacup", Context.MODE_PRIVATE);
        if (null != sharedata2) {
            SharedPreferences.Editor editor2 = sharedata2.edit();
            if (null != editor2) {
                editor2.clear();
                editor2.commit();
                return true;
            }
        }

        return false;
    }
}
