package com.example.myapplication.engine;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class VirusDao {
	//1.ָ���������ݿ��·��
	public static String path = "data/data/com.example.myapplication/files/antivirus.db";
	
	//2.�������ݿ⣬��ѯ���ݿ��б��Ӧ��md5��
	public static List<String> getVirusList(){
		SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
		Cursor cursor = db.query("datable", new String[] {"md5"}, null, null, null, null, null);
		List<String> virusList = new ArrayList<String>();
		while(cursor.moveToNext()) {
			//Log.i("VirusDao", cursor.getString(0));
			virusList.add(cursor.getString(0));
		}
		cursor.close();
		db.close();
		return virusList;
	}
}
