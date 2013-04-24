package com.ghareeb.YouTube;

import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class Prefs {
	public static final boolean isProd = true;
	public static final String TAG = "OGMod";
	private static final String EXPIRATION_DATE_KEY = "expiration_date223";
	private static final int TRIAL_MINUTES = 60 * 24 * 15;
	
	
	public static final String DEFAULT_WORK_FOLDER = "/sdcard/videokit/";
	public static final String LOG_FILE_PATH = DEFAULT_WORK_FOLDER + "ffmpeg4android.log";
	public static final String VIDEOKIT_LOG_FILE_PATH = DEFAULT_WORK_FOLDER + "videokit.log";
	public static final String DEFAULT_OUT_FOLDER = "/sdcard/videokit/";
	
	public static boolean forceStopFlag = false;
	public static boolean transcodingIsRunning = false;
	public static String durationOfCurrent = null;
	
	private String outFolder = null;
	
	public static long inputFileSize = -1;
	public static long outputFileSize = -1;
	
	public static final int FILE_TYPE_VIDEO = 0;
	public static final int FILE_TYPE_AUDIO = 1;
	public static final int FILE_TYPE_PIC   = 2;
	
	public static final int NOTIFICATION_ID = 5326;
	
	public static final String VK_LOG = DEFAULT_WORK_FOLDER + "vk.log";
	
	
	
	private Context mContext;
	
	
	
	public void setContext(Context ctx) {
		this.mContext = ctx;
	}
	
	public String getOutFolder() {
		if (outFolder == null) {
			return DEFAULT_OUT_FOLDER;
		}
		else {
			return outFolder;
		}
	}
	
	public Long getExpirationDate() {
		SharedPreferences preferences = mContext.getSharedPreferences(EXPIRATION_DATE_KEY, Context.MODE_PRIVATE);
		Long dateSaved = preferences.getLong(EXPIRATION_DATE_KEY, 0);
		if (dateSaved.longValue() == 0) {
			Calendar cal2DaysToTheFuture = Calendar.getInstance(); 
			cal2DaysToTheFuture.add(Calendar.MINUTE, TRIAL_MINUTES);
			Editor editor = preferences.edit();
			editor.putLong(EXPIRATION_DATE_KEY, new Long(cal2DaysToTheFuture.getTimeInMillis()));
			editor.commit();
			return cal2DaysToTheFuture.getTimeInMillis();
		} else {
		 return dateSaved;
		}
	}
	
	public boolean isTrialExpired() {
		
		if (Calendar.getInstance().getTimeInMillis() > getExpirationDate()) {
			return true;
		} else {
			Log.d(Prefs.TAG, "Trial license will expire at: " + new Date(getExpirationDate()));
			return false;
		}
		
		
	}
	
	public static String getVersionName(Context ctx) {
		String versionName = "";
		try {
			versionName = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			Log.w(Prefs.TAG, "No version code found, returning -1");
		}
		
		return versionName;

	}
	
	public enum Status {
		STATUS_IDLE, STATUS_WORKING, STATUS_FINISHED_OK, STATUS_FINISHED_FAIL, STATUS_NA, STATUS_TRIAL_FINISHED
		
	}

}
