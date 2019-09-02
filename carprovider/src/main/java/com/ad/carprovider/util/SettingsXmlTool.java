package com.ad.carprovider.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class SettingsXmlTool {

	public static boolean copyFile(String srcFileName, String dstFileName, boolean bDelOld) {
		if(null == srcFileName || srcFileName.isEmpty() || null == dstFileName || dstFileName.isEmpty()) {
			return false;
		}
		File fromFile = new File(srcFileName);
		File toFile = new File(dstFileName);
		if(!fromFile.exists() || !fromFile.isFile() || !fromFile.canRead()) {
			return false;
		}

		if(bDelOld && toFile.exists()) {
			try {
				toFile.delete();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		try {
			FileInputStream fosfrom = new FileInputStream(fromFile);
			FileOutputStream fosto = new FileOutputStream(toFile);
			int maxLen = (int) fromFile.length() + 1024;
			byte bt[] = new byte[maxLen];
			int len;
			while ((len = fosfrom.read(bt)) > 0) {
				fosto.write(bt, 0, len); // 将内容写到新文件当中
			}
			fosfrom.close();
			fosto.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public static boolean deleteFile(String filePath) {
		if(null == filePath || filePath.isEmpty()) {
			return false;
		}

		try {
			File file = new File(filePath);
			file.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	public static boolean checkFile(String filePath) {
		if(null == filePath || filePath.isEmpty()) {
			return false;
		}

		File file = new File(filePath);
		if(!file.exists() || !file.isFile()) {
			return false;
		}

		return true;
	}

	public static HashMap<String, SettingsItem> readFile(String filePath) {
		if(!checkFile(filePath)) {
			return null;
		}

		String content = null; //文件内容字符串
		try {
			FileInputStream fin = new FileInputStream(filePath);
			int length = fin.available();
			byte[] buffer = new byte[length];
			fin.read(buffer);
			//content = EncodingUtils.getString(buffer, "UTF-8");
			content = new String(buffer, "UTF-8");
			fin.close();
		} catch (java.io.FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if(null == content || content.isEmpty()) {
			return null;
		}

		String[] strs = content.split("<element>");
		if(null == strs || strs.length == 0) {
			return null;
		}

		HashMap<String, SettingsItem> lists = new HashMap<String, SettingsItem>();
		for(String str : strs) {
			if(null != str) {
				int index = str.indexOf("</element>");
				if(index > 0) {
					String element = str.substring(0, index);
					if(null != element && !element.isEmpty()) {
						String[] datas = element.split(",");
						if(null != datas && datas.length == 3
								&& null != datas[0] && !datas[0].isEmpty()
								&& null != datas[1] && !datas[1].isEmpty()
								&& null != datas[2] && !datas[2].isEmpty()) {
							lists.put(datas[0], new SettingsItem(Integer.parseInt(datas[1]), Integer.parseInt(datas[2])));
						}
					}
				}
			}
		}

		return lists;
	}

	public static boolean writeFile(String filePath, HashMap<String, SettingsItem> lists) {
		if(null == lists || lists.isEmpty() || !checkFile(filePath)) {
			return false;
		}

		String write_str = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		write_str += "\n" + "<sessions>";

		Iterator<Map.Entry<String, SettingsItem>> iterator = lists.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, SettingsItem> entry = iterator.next();
			SettingsItem item = entry.getValue();
			if(null != item) {
				String str = String.format("\n<element>%s,%d,%d</element>", entry.getKey(), item.value, item.flag);
				write_str += str;
			}
		}
		write_str += "\n" + "</sessions>";

		try {
			FileOutputStream fout = new FileOutputStream(filePath);
			byte[] bytes = write_str.getBytes();
			fout.write(bytes);
			fout.close();
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean saveFile(String filePath, HashMap<String, SettingsItem> lists) {
		if(null == filePath || filePath.isEmpty() || null == lists || lists.isEmpty()) {
			return false;
		}

		File file = new File(filePath);
		if(!file.exists()) {
			try{
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return writeFile(filePath, lists);
	}

}
