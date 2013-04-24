package com.ghareeb.YouTube;

import java.io.*;
import java.net.*;
import android.os.*;
import android.annotation.SuppressLint;
import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.SparseArray;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import org.apache.http.client.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.*;
import com.ghareeb.YouTube.OG.*;
import android.widget.RemoteViews;

public class DownloadService extends Service {
	private NotificationManager mNM;
	private Context mCtx;
	private static final int MAX_BUFFER_SIZE = 1024; // 1kb
	private static int ID_file_name = 0;
	private static int ID_download_info = 0;
	private static int ID_download_speed = 0;
	private static int ID_downloaded = 0;
	private static int ID_og_download = 0;
	private static int ID_image1 = 0;
	private static int ID_image2 = 0;
	private static int ID_imgDled = 0;
	private static int ID_imgErr = 0;
	private static int ID_convert = 0;
	private static int ID_convert1 = 0;
	private static int ID_notifimg = 0;

	public DBDownloads database;
	public SparseArray<Downloader> Downloads = new SparseArray<Downloader>();
	public static boolean serviceState = false;
	public Activity loadedActivity;
	public boolean ConvertCanceled = false;

	// public PowerManager.WakeLock mWakeLock;

	static class State {
		public OG.Download download;
		public Notification notification;
	}

	public int getResID(String name, String Type) {
		return getBaseContext().getResources().getIdentifier(name, Type,
				getBaseContext().getPackageName());
	}

	public String getString(String name) {
		return getBaseContext().getString(
				getBaseContext().getResources().getIdentifier(name, "string",
						getBaseContext().getPackageName()));
	}

	@Override
	public void onCreate() {
		serviceState = true;
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mCtx = getBaseContext();
		database = new DBDownloads(mCtx);
		ID_file_name = getResID("file_name", "id");
		ID_download_info = getResID("download_info", "id");
		ID_download_speed = getResID("download_speed", "id");
		ID_downloaded = getResID("downloaded", "id");
		ID_notifimg = getResID("notifiation_image", "id");
		ID_og_download = getResID("og_download", "layout");
		ID_convert1 = getResID("og_convertimg", "drawable");
		ID_convert = getResID("og_convert", "drawable");
		ID_image1 = getResID("og_down", "drawable");
		ID_image2 = getResID("og_download", "drawable");
		ID_imgDled = getResID("og_downloaded", "drawable");
		ID_imgErr = getResID("og_err", "drawable");
		database.ClearConverts();

		startForeground(1, new Notification());
		for (Download download : database.GetDownloads()) {
			if (download.Status == OG.CONVERTING) {
				download.Status = OG.CONVERT_ERROR;
				download.TStatus = getString("OG_CError");
				database.UpdateDownload(download);
			}
		}
		for (Download download : database.GetDownloads()) {
			if (download.Status == OG.DOWNLOADING) {
				StartThread(download);
			}

			if ((download.Status == OG.CONVERT_WAIT)
					&& (database.GetConvertsCount() == 0)) {
				download.Status = OG.CONVERTING;
				database.UpdateDownload(download);
				StartThread(download);
				break;
			}
		}
	}

	public void StartThread(Download download) {
		if (Downloads.get(download.NotifyID) == null) {
			Downloader d = new Downloader(download);
			Downloads.put(download.NotifyID, d);
			Thread t = new Thread(d);
			t.start();
		} else {
			Log.i("OGMod", "E:StartThread " + download.NotifyID);
		}
	}

	@Override
	public void onDestroy() {
		stopForeground(false);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		try {
			if (intent != null) {
				Bundle extra = intent.getExtras();
				String action = extra.getString("Action");

				if (action.equals("Convert")) {
					ConvertCanceled = false;
					int NotifyID = extra.getInt("NotifyID");
					if (database.GetConvertsCount() == 0) {
						Download dl = database.GetDownloadById(NotifyID);
						dl.Status = OG.CONVERTING;
						StartThread(dl);
					} else {
						Download dl = database.GetDownloadById(NotifyID);
						dl.Status = OG.CONVERT_WAIT;
						dl.Converted = 0;
						dl.Duration = 100;
						dl.TStatus =getString("OG_CWait");
						database.UpdateDownload(dl);
					}
				} else if (action.equals("StopConvert")) {
					int NotifyID = extra.getInt("NotifyID");
					Download dl = database.GetDownloadById(NotifyID);
					if (dl.Status == OG.CONVERTING) {
						ConvertCanceled = true;
						dl.Status = OG.CONVERT_CANCEL;
						dl.TStatus = getString("OG_CCancel");
						database.UpdateDownload(dl);
						Intent i = new Intent(getBaseContext(),
								ConvertService.class);
						i.putExtra("Action", "StopConvert");
						getBaseContext().startService(i);
					} else {
						dl.Status = OG.CONVERT_CANCEL;
						dl.TStatus = getString("OG_CCancel");
						database.UpdateDownload(dl);
					}
				} else if (action.equals("Delete")) {
					int NotifyID = extra.getInt("NotifyID");
					if (Downloads.get(NotifyID) != null) {
						Downloads.get(NotifyID).Cancel = true;
						Downloads.get(NotifyID).Delete = true;
						Downloads.remove(NotifyID);
					}
					RemoveNotification(NotifyID);
				} else if (action.equals("SR")) {
					int NotifyID = extra.getInt("NotifyID");
					if (Downloads.get(NotifyID) != null) {
						Downloads.get(NotifyID).Cancel = true;
						Downloads.get(NotifyID).Delete = false;
					} else {
						StartThread(database.GetDownloadById(NotifyID));
					}
				} else if (action.equals("Redl")) {
					int NotifyID = extra.getInt("NotifyID");
					if (Downloads.get(NotifyID) != null) {
						Downloads.get(NotifyID).Cancel = true;
						Downloads.get(NotifyID).Delete = true;
						Downloads.remove(NotifyID);
					}
					Download SelectedItem = database.GetDownloadById(NotifyID);
					Download download = new Download();
					download.URL = SelectedItem.URL;
					download.FileName = SelectedItem.FileName;
					download.Title = SelectedItem.Title;
					download.NotifyID = SelectedItem.NotifyID;
					download.VideoID = SelectedItem.VideoID;
					database.UpdateDownload(download);
					StartThread(download);
				} else if (action.equals("RN")) {
					int NotifyID = extra.getInt("NotifyID");
					RemoveNotification(NotifyID);
				} else if (action.equals("NewDownload")) {
					Download download = new Download();
					download.URL = extra.getString("downloadUrl");
					download.FileName = extra.getString("fileName");
					download.Title = extra.getString("title");
					download.NotifyID = new Random().nextInt(999999);
					download.VideoID = extra.getString("videoID");
					while (database.AddDownload(download) != true) {
						download.NotifyID = new Random().nextInt(999999);
					}
					if ((download.URL != null) && (download.FileName != null)
							&& (download.VideoID != null)
							&& (download.Title != null)) {
						StartThread(download);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return START_STICKY;
	}

	class Downloader implements Runnable {
		State state;
		Boolean Cancel = false;
		Boolean Delete = false;
		Boolean rescaned = false;

		public Downloader(Download download) {
			if (Downloads.get(download.NotifyID) == null) {
				Downloads.put(download.NotifyID, this);
			}
			State state = new State();
			state.download = download;
			this.state = state;
		}

		@SuppressWarnings("deprecation")
		@SuppressLint({ "SimpleDateFormat", "Wakelock" })
		@Override
		public void run() {
			if (state.download.Status == OG.CONVERTING) {
				File folder = new File(Prefs.DEFAULT_WORK_FOLDER);
				folder.mkdirs();
				String FN = state.download.FileName;
				state.download.Status = OG.CONVERTING;
				state.download.TStatus = getString("OG_CConverting");
				state.download.Converted = 0;
				state.download.Duration = 100;
				state.download.MP3FileName = FN.substring(0, FN.length() - 3)
						+ "mp3";
				state = ShowNotification(state);
				updateNotification(state);

				File file = new File(FN);
				if (!file.exists()) {
					state.download.Status = OG.CONVERT_ERROR;
					state.download.TStatus = getString("OG_FileNotFound");
					state.download.Converted = 0;
					state.download.Duration = 100;
					updateNotification(state);
					return;
				}
				Intent i = new Intent(getBaseContext(), ConvertService.class);
				i.putExtra("Action", "Convert");
				i.putExtra("FileName", FN);
				getBaseContext().startService(i);

				FileUtils.deleteFile(Prefs.VK_LOG);
				FileUtils.deleteFile(Prefs.VIDEOKIT_LOG_FILE_PATH);
				SimpleDateFormat _simpleDateFormat = new SimpleDateFormat(
						"HH:mm:ss.SS");
				Date ref = null;
				try {
					ref = _simpleDateFormat.parse("00:00:00.00");
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
				ref.setYear(112);
				long _timeRef = ref.getTime();
				Date durationDate = null;
				boolean isExit = false;
				boolean isErr = false;
				long last = System.currentTimeMillis();
				try {
					while (!isExit) {
						if (System.currentTimeMillis() - last >= 2000) {
							last = System.currentTimeMillis();
							if (durationDate != null) {
								String currentTimeStr = FileUtils
										.readLastTimeFromFFmpegLogFileUsingRandomAccess();

								if (currentTimeStr.equals("exit")) {
									isExit = true;
								} else if (currentTimeStr.equals("maybe_error")
										&& state.download.Percent() == 0) {
									isErr = true;
									isExit = true;
								} else {
									try {
										Date currentTimeDate = _simpleDateFormat
												.parse(currentTimeStr);
										currentTimeDate.setYear(112);
										long currentTimeLong = currentTimeDate
												.getTime() - _timeRef;
										state.download.Converted = currentTimeLong;
										state.download.Status = OG.CONVERTING;
									} catch (ParseException e) {
										e.printStackTrace();
									}
									updateNotification(state);
								}
							} else {
								try {
									String d = FileUtils
											.getDutationFromVCLogRandomAccess();
									if (d != null) {
										durationDate = _simpleDateFormat
												.parse(d);
										durationDate.setYear(112);
										long durationLong = durationDate
												.getTime() - _timeRef;
										state.download.Duration = durationLong;
									}

								} catch (ParseException e) {
									e.printStackTrace();
								}
							}

						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (ConvertCanceled) {
					state.download.Status = OG.CONVERT_CANCEL;
					state.download.TStatus = getString("OG_CCancel");
				} else if (isErr) {
					state.download.Status = OG.CONVERT_ERROR;
					state.download.TStatus = getString("OG_CError");
				} else {
					state.download.Converted = state.download.Duration;
					state.download.Status = OG.CONVERTED;
					state.download.TStatus = getString("OG_CFinish");
				}
				database.UpdateDownload(state.download);
				Downloads.remove(state.download.NotifyID);
				updateNotification(state);
				i = new Intent(getBaseContext(), ConvertService.class);
				getBaseContext().stopService(i);
				for (Download download : database.GetDownloads()) {
					if ((download.Status == OG.CONVERT_WAIT)
							&& (database.GetConvertsCount() == 0)) {
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						ConvertCanceled = false;
						download.Status = OG.CONVERTING;
						database.UpdateDownload(download);
						StartThread(download);
						break;
					}
				}
			} else {
				state.download.TStatus = getString("OG_Connect");
				state = ShowNotification(state);
				state.download.Status = OG.DOWNLOADING;
				try {
					HttpURLConnection conn;
					InputStream stream;
					OutputStream out;
					if (OG.isOnline(mCtx)) {
						conn = (HttpURLConnection) new URL(state.download.URL)
								.openConnection();

						if (state.download.Downloaded >= 0) {
							File file = new File(state.download.FileName);
							if (file.exists()) {
								state.download.Downloaded = (int) file.length();
								conn.setRequestProperty("Range", "bytes="
										+ (file.length()) + "-");
							} else {
								state.download.Downloaded = 0;
								conn.setRequestProperty("Range", "bytes=0-");
							}
						} else {
							conn.setRequestProperty("Range", "bytes=0-");
						}
						conn.setDoInput(true);
						conn.setDoOutput(true);
						if (state.download.Downloaded == 0) {
							state.download.TotalBytes = (long) conn
									.getContentLength();
						} else {
							int size = conn.getContentLength();
							if (size == state.download.TotalBytes) {
								state.download.TStatus = getString("OG_CNTR");
								state.download.Status = OG.ERROR;
								updateNotification(state);
								Downloads.remove(state.download.NotifyID);
								return;
							} else if ((size <= 1000)) {
								if (rescaned) {
									state.download.TStatus = getString("OG_Error");
									state.download.Status = OG.ERROR;
									updateNotification(state);
									Downloads.remove(state.download.NotifyID);
									return;
								} else {
									rescaned = true;
									state.download.TStatus = getString("OG_Update");
									state.download.URL = UpdateURL(
											"http://www.youtube.com/watch?v="
													+ state.download.VideoID,
											OG.GetiTag(state.download.URL));
									Thread t = new Thread(this);
									t.start();
									return;
								}
							}
						}
						if (Cancel) {
							if (Delete) {
								state.download.Status = OG.DELETED;
							} else {
								state.download.Status = OG.STOPPED;
							}
							state.download.TStatus = getString("OG_Stopped");
							updateNotification(state);
							return;
						}
						if (state.download.TotalBytes <= 1000) {
							if (rescaned) {
								state.download.TStatus = getString("OG_Error");
								state.download.Status = OG.ERROR;
								updateNotification(state);
								Downloads.remove(state.download.NotifyID);
								return;
							} else {
								rescaned = true;
								state.download.URL = UpdateURL(
										"http://www.youtube.com/watch?v="
												+ state.download.VideoID,
										OG.GetiTag(state.download.URL));
								Thread t = new Thread(this);
								t.start();
								return;
							}
						}
						state.download.TStatus = getString("OG_Download");
						updateNotification(state);
						if (state.download.Downloaded >= 0) {
							out = new FileOutputStream(state.download.FileName,
									true);
						} else {
							out = new FileOutputStream(state.download.FileName);
						}

						File f = new File(state.download.FileName);
						StatFs stat = new StatFs(f.getPath());
						long avb = (long) stat.getBlockSize()
								* (long) stat.getAvailableBlocks();
						if (avb <= state.download.TotalBytes) {
							state.download.Status = OG.ERROR;
							state.download.TStatus = getString("OG_Space");
							updateNotification(state);
							return;
						}
						if (Cancel) {
							if (Delete) {
								state.download.Status = OG.DELETED;
							} else {
								state.download.Status = OG.STOPPED;
							}
							state.download.TStatus = getString("OG_Stopped");
							updateNotification(state);
							return;
						}
						conn.connect();
						stream = conn.getInputStream();
						long last = System.currentTimeMillis();
						long lastSpeed = System.currentTimeMillis();
						int bytesInThreshold = 0;
						while (state.download.Status == OG.DOWNLOADING) {
							byte buffer[];
							if (state.download.TotalBytes
									- state.download.Downloaded > MAX_BUFFER_SIZE) {
								buffer = new byte[MAX_BUFFER_SIZE];
							} else {
								buffer = new byte[(int) (state.download.TotalBytes - state.download.Downloaded)];
							}
							int read = stream.read(buffer);
							if (read == -1) {
								break;
							}
							out.write(buffer, 0, read);
							state.download.Downloaded += read;
							bytesInThreshold += read;
							if (System.currentTimeMillis() - lastSpeed >= 1000) {
								state.download.Speed = OG.CalculateSpeed(
										System.currentTimeMillis() - lastSpeed,
										bytesInThreshold);
								bytesInThreshold = 0;
								lastSpeed = System.currentTimeMillis();

							}
							if (System.currentTimeMillis() - last >= 2000) {
								last = System.currentTimeMillis();
								state.download.TStatus = "";
								updateNotification(state);
							}
							if (Cancel) {
								if (Delete) {
									state.download.Status = OG.DELETED;
								} else {
									state.download.Status = OG.STOPPED;
								}
								state.download.TStatus = getString("OG_Stopped");
								updateNotification(state);
								break;
							}
							if (Downloads.get(state.download.NotifyID) != this) {
								RemoveNotification(state.download.NotifyID);
								return;
							}

						}

						if (state.download.Status == OG.DOWNLOADING) {
							state.download.TStatus = getString("OG_Complate");
							state.download.Status = OG.COMPLETE;

						}
						updateNotification(state);
						out.flush();
						out.close();
						stream.close();

					} else {

						state.download.Status = OG.ERROR;
						state.download.TStatus = getString("OG_NoNet");
						updateNotification(state);
					}
				} catch (Exception e) {
					state.download.Retry++;
					if (state.download.Retry == 1) {
						state.download.URL = UpdateURL(
								"http://www.youtube.com/watch?v="
										+ state.download.VideoID,
								OG.GetiTag(state.download.URL));
						StartThread(state.download);
					} else {
						state.download.TStatus = getString("OG_Error");
						state.download.Status = OG.ERROR;
						updateNotification(state);
					}
					e.printStackTrace();
					return;
				}
				try {
					updateNotification(state);
					Downloads.remove(state.download.NotifyID);
				} catch (Exception e) {
				}
			}

		}

		public String UpdateURL(String URL, int iTag) {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(URL);
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			try {
				String rs = client.execute(request, responseHandler);
				int start = rs.indexOf("url_encoded_fmt_stream_map")
						+ ("url_encoded_fmt_stream_map=\"").length();
				int end = rs.indexOf("\",", start + 1);
				String page = OG.ConvertNetChar(rs.substring(start, end));
				page = page.replace("sig=", "signature=");
				String Splitter = page.substring(0, page.indexOf("=")).trim();
				if (Splitter.startsWith("\""))
					Splitter = Splitter.substring(1);
				String[] videos = page.trim().split("," + Splitter + "=");

				for (int n = 0; n < videos.length; n++) {
					String NewURL = videos[n];
					if (!videos[n].startsWith("\"" + Splitter)) {
						URL = Splitter + "=" + videos[n];
					}
					if (OG.GetiTag(NewURL) == iTag)
						return new YouTubeVideo(NewURL).URL;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return URL;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void RemoveNotification(int id) {
		mNM.cancel(id);
	}

	@SuppressWarnings("deprecation")
	public State ShowNotification(State state) {
		long when = System.currentTimeMillis();

		Notification notification;
		if (state.download.Status == OG.CONVERTING) {
			notification = new Notification(ID_convert,
					"OG YouTube Downloader", when);
		} else {
			notification = new Notification(ID_image1, "OG YouTube Downloader",
					when);
		}

		RemoteViews contentView = new RemoteViews(mCtx.getPackageName(),
				ID_og_download);

		contentView.setTextViewText(ID_file_name, state.download.Title);
		contentView.setTextViewText(ID_download_info, "0%");
		if (state.download.TStatus != "") {
			contentView.setTextViewText(ID_download_info,
					state.download.TStatus);
		}
		contentView.setProgressBar(ID_downloaded, 100, 0, true);

		notification.contentView = contentView;
		notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		if (state.download.Downloaded >= 0) {
			notification.defaults = 0;
		} else {
			notification.defaults |= Notification.DEFAULT_LIGHTS; // LED
			notification.defaults |= Notification.DEFAULT_SOUND; // Sound
		}
		if (state.download.Status == OG.CONVERTING) {
			contentView.setImageViewResource(ID_notifimg, ID_convert1);
		}

		Intent intent = new Intent(mCtx, OGDownloadManager.class);

		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);

		PendingIntent contentIntent = PendingIntent.getActivity(mCtx, 0,
				intent, 0);
		notification.contentIntent = contentIntent;

		mNM.notify(state.download.NotifyID, notification);

		database.UpdateDownload(state.download);

		state.notification = notification;

		return state;
	}

	public void updateNotification(State state) {

		state.notification.contentView.setTextViewText(ID_download_speed,
				state.download.Percent() + "%");
		if (state.download.TStatus != "") {
			state.notification.contentView.setTextViewText(ID_download_info,
					state.download.TStatus);
		} else {
			state.download.TStatus = OG.ConvertByte(state.download.Speed)
					+ "/s  (" + OG.ConvertByte(state.download.Downloaded)
					+ " / " + OG.ConvertByte(state.download.TotalBytes) + ")";
			state.notification.contentView.setTextViewText(ID_download_info,
					state.download.TStatus);
		}
		state.notification.contentView.setProgressBar(ID_downloaded, 100,
				state.download.Percent(), false);
		if ((state.download.Status == OG.DOWNLOADING)
				|| (state.download.Status == OG.CONVERTING)) {
			state.notification.defaults = 0;
		} else {
			if ((state.download.Status == OG.COMPLETE)
					|| (state.download.Status == OG.CONVERTED)) {
				state.notification.icon = ID_imgDled;
			} else if ((state.download.Status == OG.ERROR)
					|| (state.download.Status == OG.CONVERT_ERROR)
					|| (state.download.Status == OG.CONVERT_CANCEL)) {
				state.notification.icon = ID_imgErr;
			} else if ((state.download.Status == OG.STOPPED)) {
				state.notification.icon = ID_image2;
			} else if (state.download.Status == OG.CONVERTING) {
				state.notification.icon = ID_convert;
			} else {
				state.notification.icon = ID_image2;
			}
			state.notification.flags = 0;
			state.notification.defaults |= Notification.DEFAULT_LIGHTS; // LED
			state.notification.defaults |= Notification.DEFAULT_SOUND; // Sound
			Intent intent = new Intent(mCtx, OGDownloadManager.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_SINGLE_TOP);
			intent.putExtra("NotifyID", state.download.NotifyID);
			PendingIntent contentIntent = PendingIntent.getActivity(mCtx, 0,
					intent, PendingIntent.FLAG_UPDATE_CURRENT);
			state.notification.contentIntent = contentIntent;
		}
		if (state.download.Status != OG.DELETED) {
			database.UpdateDownload(state.download);
			mNM.notify(state.download.NotifyID, state.notification);
		}
	}

}
