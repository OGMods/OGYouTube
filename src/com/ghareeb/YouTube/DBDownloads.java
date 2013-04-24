package com.ghareeb.YouTube;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ghareeb.YouTube.OG.*;

public class DBDownloads extends SQLiteOpenHelper {
	public static final String DB_NAME = "Downloads.sqlite";
	public static final String Downloads_TABLE = "Downloads";
	public static final String C_ID = "id";
	public static final String C_VideoID = "vid";
	public static final String C_URL = "url";
	public static final String C_Title = "title";
	public static final String C_FileName = "filename";
	public static final String C_Status = "status";
	public static final String C_TStatus = "tstatus";
	public static final String C_TotalBytes = "tBytes";
	public static final String C_Downloaded = "downloaded";
	public static final String C_Converted = "converted";
	public static final String C_Duration = "duration";
	public static final String C_Speed = "speed";
	public static final String C_MP3FileName = "mp3";
	public static SQLiteDatabase database;
	public static final int VERSION = 2;
	public Context context;

	public DBDownloads(Context context) {
		super(context, DB_NAME, null, VERSION);
		this.context = context;
		database = getWritableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("drop table if exists " + Downloads_TABLE + ";");
		db.execSQL("create table " + Downloads_TABLE + " (" + C_ID
				+ " integer primary key not null," + C_VideoID + " text,"
				+ C_URL + " text," + C_Title + " text," + C_FileName + " text,"
				+ C_Status + " integer, " + C_TotalBytes + " integer,"
				+ C_Downloaded + " integer, " + C_TStatus + " text, " + C_Speed
				+ " integer, " + C_Converted + " integer, " + C_Duration
				+ " integer, " + C_MP3FileName + " text);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d("OGMod", "Database upgraded from " + oldVersion + " to "
				+ newVersion);
		String upgradeQuery = "ALTER TABLE " + Downloads_TABLE + " ADD COLUMN ";
		if (oldVersion == 1 && newVersion == 2) {
			db.execSQL(upgradeQuery + C_Converted + " INTEGER;");
			db.execSQL(upgradeQuery + C_Duration + " INTEGER;");
			db.execSQL(upgradeQuery + C_MP3FileName + " TEXT;");
		}
	}

	public Download[] GetDownloads() {
		if (database != null) {
			Cursor cu = database.rawQuery("SELECT * FROM " + Downloads_TABLE,
					null);
			Download downloads[] = new Download[cu.getCount()];
			cu.moveToFirst();
			int Index = 0;
			if ((cu != null) && (cu.getCount() > 0)) {
				do {
					Download download = new Download();
					download = new Download();
					download.NotifyID = cu.getInt(0);
					download.VideoID = cu.getString(1);
					download.URL = cu.getString(2);
					download.Title = cu.getString(3);
					download.FileName = cu.getString(4);
					download.Status = cu.getInt(5);
					download.TotalBytes = cu.getInt(6);
					download.Downloaded = cu.getInt(7);
					download.TStatus = cu.getString(8);
					download.Speed = cu.getInt(9);
					download.Converted = cu.getInt(10);
					download.Duration = cu.getInt(11);
					download.MP3FileName = cu.getString(12);

					downloads[Index] = download;
					Index++;
				} while (cu.moveToNext());
			}
			return downloads;
		}
		return null;
	}

	public int GetConvertsCount() {
		if (database != null) {
			Cursor cu = database
					.rawQuery("SELECT * FROM " + Downloads_TABLE + " WHERE ("
							+ C_Status + " = " + OG.CONVERTING + ")", null);
			return cu.getCount();
		}
		return 0;
	}

	public Download GetDownloadById(int id) {
		if (database != null) {
			Cursor cu = database.rawQuery("SELECT * FROM " + Downloads_TABLE
					+ " WHERE (" + C_ID + " = " + id + ")", null);
			Download download = null;
			cu.moveToFirst();
			if ((cu != null) && (cu.getCount() > 0)) {
				download = new Download();
				download.NotifyID = cu.getInt(0);
				download.VideoID = cu.getString(1);
				download.URL = cu.getString(2);
				download.Title = cu.getString(3);
				download.FileName = cu.getString(4);
				download.Status = cu.getInt(5);
				download.TotalBytes = cu.getInt(6);
				download.Downloaded = cu.getInt(7);
				download.TStatus = cu.getString(8);
				download.Speed = cu.getInt(9);
				download.Converted = cu.getInt(10);
				download.Duration = cu.getInt(11);
				download.MP3FileName = cu.getString(12);
			}
			return download;
		}
		return null;
	}

	public Boolean AddDownload(Download download) {
		Cursor cu = database.rawQuery("SELECT * FROM " + Downloads_TABLE
				+ " WHERE " + C_ID + " = " + download.NotifyID, null);
		if ((cu != null) && (cu.getCount() > 0)) {
			return false;
		} else {
			ContentValues args = new ContentValues();
			args.put(C_ID, download.NotifyID);
			args.put(C_VideoID, download.VideoID);
			args.put(C_URL, download.URL);
			args.put(C_Title, download.Title);
			args.put(C_FileName, download.FileName);
			args.put(C_Status, download.Status);
			args.put(C_TotalBytes, download.TotalBytes);
			args.put(C_Downloaded, download.Downloaded);
			args.put(C_TStatus, download.TStatus);
			args.put(C_Speed, download.Speed);
			args.put(C_Converted, download.Converted);
			args.put(C_Duration, download.Duration);
			args.put(C_MP3FileName, download.MP3FileName);
			database.insertOrThrow(Downloads_TABLE, C_ID, args);
			return true;
		}
	}

	public void ClearConverts() {
		ContentValues args = new ContentValues();
		args.put(C_Status, OG.COMPLETE);
		args.put(C_TStatus, getString("OG_Complate"));
		args.put(C_Converted, 0);
		args.put(C_Duration, 100);
		args.put(C_MP3FileName, "");
		database.update(Downloads_TABLE, args,
				C_Status + " = " + OG.CONVERTING, null);
	}

	public void UpdateDownload(Download download) {
		ContentValues args = new ContentValues();
		args.put(C_VideoID, download.VideoID);
		args.put(C_URL, download.URL);
		args.put(C_Title, download.Title);
		args.put(C_FileName, download.FileName);
		args.put(C_Status, download.Status);
		args.put(C_TotalBytes, download.TotalBytes);
		args.put(C_Downloaded, download.Downloaded);
		args.put(C_TStatus, download.TStatus);
		args.put(C_Speed, download.Speed);
		args.put(C_Converted, download.Converted);
		args.put(C_Duration, download.Duration);
		args.put(C_MP3FileName, download.MP3FileName);
		database.update(Downloads_TABLE, args, C_ID + " = ?",
				new String[] { Integer.toString(download.NotifyID) });
	}

	public void DeleteDownload(int Id) {
		database.execSQL("DELETE FROM " + Downloads_TABLE + " WHERE (" + C_ID
				+ " = " + Id + ")");
	}

	public void Clear() {
		database.execSQL("DELETE FROM " + Downloads_TABLE);
	}

	public void DeleteCompleted() {
		database.execSQL("DELETE FROM " + Downloads_TABLE + " WHERE ("
				+ C_Status + " = " + OG.COMPLETE + ")");

	}

	public String getString(String name) {
		return context.getString(context.getResources().getIdentifier(name,
				"string", context.getPackageName()));
	}

}
