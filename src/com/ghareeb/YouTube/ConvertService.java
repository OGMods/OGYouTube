package com.ghareeb.YouTube;

import java.io.*;
import android.os.*;
import android.app.*;
import android.content.Intent;
import android.util.Log;
import uk.co.halfninja.videokit.Videokit;

public class ConvertService extends Service {
	public boolean Working = false;

	@Override
	public void onCreate() {
		Log.i("OGMod", "Convert Service onCreate");
		Working = false;
	}

	@Override
	public void onDestroy() {
		stopForeground(false);
		Log.i("OGMod", "Convert Service Destroied");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		try {
			if (intent != null) {
				Bundle extra = intent.getExtras();
				String action = extra.getString("Action");
				if (action.equals("Convert")) {
					String FileName = extra.getString("FileName");
					if (!Working) {
						Working = true;
						startForeground(1, new Notification());
						Log.i("OGMod", "Start Converter");
						Thread t = new Thread(new Converter(FileName));
						t.start();
					} else {
						Log.e("OGMod",
								"Can't start new converter when converting ");
					}
				} else if (action.equals("StopConvert")) {
					Log.e("OGMod", "StopConvert");
					Videokit vk = new Videokit();
					vk.fexit();
				}

			}

		} catch (Exception e) {
			Log.e("OGMod", "onStartCommand,error=" + e.toString());
		}
		return START_NOT_STICKY;
	}

	class Converter implements Runnable {
		String FileName;

		public Converter(String FileName) {
			this.FileName = FileName;
		}


		@Override
		public void run() {
			Log.i(Prefs.TAG, "Convrter Run called.");
			android.os.Process
					.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
			File folder = new File(Prefs.DEFAULT_WORK_FOLDER);
			folder.mkdirs();
			Videokit vk = new Videokit();
			try {
				Log.e(Prefs.TAG, "FFMPEG running");
				String FN = FileName;
				String[] cmd = new String[] { "ffmpeg", "-i", FN,
						FN.substring(0, FN.length() - 3) + "mp3" };
				FileUtils.deleteFile(Prefs.VK_LOG);
				FileUtils.deleteFile(Prefs.VIDEOKIT_LOG_FILE_PATH);
				FileUtils.deleteFile(FN.substring(0, FN.length() - 3) + "mp3");
				vk.run(cmd);
				Log.e(Prefs.TAG, "FFMPEG finished");
				stopForeground(false);
			} catch (Exception e) {
				Log.e(Prefs.TAG, "FFMPEG finished with errors..");
				return;
			}
			Log.i(Prefs.TAG, "RemoteService: FFMPEG finished.");

		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
