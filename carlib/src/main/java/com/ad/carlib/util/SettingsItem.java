package com.ad.carlib.util;

//flag:
//0x01 工厂设置数据(只能在工厂设置中修改)
//0x02 1:开机复位(对工厂设置数据无效)  0:开机不复位
//0x04 1:恢复出厂设置复位(对工厂设置数据无效)  0:恢复出厂设置不复位

public class SettingsItem {

	public int value;//值
	public int flag; //标志

	public SettingsItem(int value, int flag) {
		this.value = value;
		this.flag =  flag;
	}

	public static boolean isFactoryData(int f) {
		if((f & 0x01) != 0) {
			return true;
		}
		return false;
	}

	public static boolean isPowerOnReset(int f) {
		if((f & 0x02) != 0) {
			return true;
		}
		return false;
	}

	public static boolean isFactoryReset(int f) {
		if((f & 0x04) != 0) {
			return true;
		}
		return false;
	}

}
