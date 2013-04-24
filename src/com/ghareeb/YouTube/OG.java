package com.ghareeb.YouTube;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.view.View;

public class OG {
	public static final int NOTSTARTED = -1;
	public static final int DOWNLOADING = 0;
	public static final int COMPLETE = 1;
	public static final int ERROR = 2;
	public static final int STOPPED = 3;
	public static final int DELETED = 4;
	public static final int CONVERTING = 5;
	public static final int CONVERTED = 6;
	public static final int CONVERT_ERROR = 7;
	public static final int CONVERT_CANCEL = 8;
	public static final int CONVERT_WAIT = 9;

	public static class Download {
		public int Status = NOTSTARTED;
		public int NotifyID = 1;
		public int Retry = 0;
		public long TotalBytes = -1;
		public long Converted = 0;
		public long Downloaded = 0;
		public long Duration = -1;
		public long Speed = 0;
		public String TStatus = "";
		public String FileName;
		public String MP3FileName;
		public String VideoID;
		public String Title;
		public String URL;

		public int Percent() {
			if (Status >= CONVERTING) {
				if (Duration==0){return 0;}
				return (int) (Converted * 100 / Duration);
			} else {
				if (TotalBytes==0){return 0;}
				if (Status==COMPLETE){return 100;}
				return (int) (Downloaded * 100 / TotalBytes);
			}
		}

	}

	public static class YouTubeVideo {
		public String URL;
		public int iTag;

		public YouTubeVideo(String str) {
			iTag = OG.GetiTag(str);
			if (str.contains("url=")) {
				if (!str.startsWith("url")) {
					String start = str.split("url=")[0];
					URL = str.split("url=")[1];
					if (start.endsWith("&")) {
						start = start.substring(0, start.length() - 1);
					}
					URL += "&" + start;
					URL = URL.replace("&itag=" + iTag, "") + "&itag=" + iTag;
					URL = URL.replace(" ", "+");
				} else {
					URL = str.split("url=")[1];
				}

			} else {
				URL = str;
			}

		}

		public String toString() {
			return OG.GetInfo(iTag);
		}

		public String GetInfo() {
			return OG.GetInfo(iTag);
		}

		public String GetEXT() {
			return OG.GetEXT(iTag);
		}
	}

	public static String CorrectFileName(String FileName) {
		FileName = FileName.replace("\\", " ");
		FileName = FileName.replace("/", " ");
		FileName = FileName.replace(":", " ");
		FileName = FileName.replace("\"", " ");
		FileName = FileName.replace("<", " ");
		FileName = FileName.replace(">", " ");
		FileName = FileName.replace("?", " ");
		FileName = FileName.replace("*", " ");
		FileName = FileName.replace("|", " ");
		return FileName;
	}

	public static int GetiTag(String URL) {
		if (URL.indexOf("itag=") != -1) {
			String itag = "0";
			int index1 = URL.indexOf("itag=");
			int index2 = URL.indexOf("&", index1 + 1);
			if (index2 < index1) {
				itag = URL.substring(index1 + 5);
			} else {
				itag = URL.substring(index1 + 5, index2);
			}
			int iTag = Integer.parseInt(itag);
			return iTag;
		} else {
			return 0;
		}
	}

	public static String GetEXT(int iTag) {
		String info = GetInfo(iTag);
		if (info.contains("WebM")) {
			return "WebM";
		} else if (info.contains("MP4")) {
			return "mp4";
		} else if (info.contains("FLV")) {
			return "flv";
		} else if (info.contains("3GP")) {
			return "3gp";
		}
		return "";
	}

	public static String GetMimeType(int iTag) {
		String info = GetInfo(iTag);
		if (info.contains("WebM")) {
			return "video/webm";
		} else if (info.contains("MP4")) {
			return "video/mp4";
		} else if (info.contains("FLV")) {
			return "video/x-flv";
		} else if (info.contains("3GP")) {
			return "video/3gpp";
		}
		return "";
	}

	public static String GetInfo(int iTag) {
		switch (iTag) {
		case 5:
			return "FLV - 240p";
		case 6:
			return "FLV - 270p";
		case 13:
			return "3GP";
		case 17:
			return "3GP - 144p";
		case 18:
			return "MP4 - 270p/360p";
		case 22:
			return "MP4 - 720p";
		case 34:
			return "FLV - 360p";
		case 35:
			return "FLV - 480p";
		case 36:
			return "3GP - 240p";
		case 37:
			return "MP4 - 1080p";
		case 38:
			return "MP4 - 3072p";
		case 43:
			return "WebM - 360p";
		case 44:
			return "WebM - 480p";
		case 45:
			return "WebM - 720p";
		case 46:
			return "WebM - 1080p";
		case 82:
			return "MP4 - 360p";
		case 83:
			return "MP4 - 240p";
		case 84:
			return "MP4 - 720p";
		case 85:
			return "MP4 - 520p";
		case 100:
			return "WebM - 360p";
		case 101:
			return "WebM - 360p";
		case 102:
			return "WebM - 720p";
		case 120:
			return "FLV - 720p";
		default:
			return "Unkown";
		}
	}

	public static boolean isOnline(Context mCtx) {
		try {
			ConnectivityManager cm = (ConnectivityManager) mCtx
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			return cm.getActiveNetworkInfo().isConnectedOrConnecting();
		} catch (Exception e) {
			return false;
		}
	}

	public static long CalculateSpeed(long downloadTime, long bytesIn) {
		return (bytesIn / downloadTime) * 1024;
	}

	@SuppressLint("DefaultLocale")
	public static String ConvertByte(long bytes) {
		int unit = 1024;
		if (bytes < unit)
			return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = ("KMGTPE").charAt(exp - 1) + "";
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	public static String ConvertNetChar(String Text) {
		Text = Text.replace("%25", "%");
		Text = Text.replace("+", " ");
		Text = Text.replace("%3A", ":");
		Text = Text.replace("%3B", ";");
		Text = Text.replace("%2F", "/");
		Text = Text.replace("%3F", "?");
		Text = Text.replace("%3D", "=");
		Text = Text.replace("%D9%8E", "ó");
		Text = Text.replace("%D9%8B", "ð");
		Text = Text.replace("%D9%8F", "õ");
		Text = Text.replace("%D9%8C", "ñ");
		Text = Text.replace("%E2%80%98", "‘");
		Text = Text.replace("%C3%B7", "÷");
		Text = Text.replace("%C3%97", "×");
		Text = Text.replace("%D8%9B", "º");
		Text = Text.replace("%3C", "<");
		Text = Text.replace("%3E", ">");
		Text = Text.replace("%D9%90", "ö");
		Text = Text.replace("%D9%8D", "ò");
		Text = Text.replace("%5D", "]");
		Text = Text.replace("%5B", "[");
		Text = Text.replace("%D9%80", "Ü");
		Text = Text.replace("%D8%8C", "¡");
		Text = Text.replace("%2F", "/");
		Text = Text.replace("%3A", ":");
		Text = Text.replace("%22", "\"");
		Text = Text.replace("%7C", "|");
		Text = Text.replace("%7E", "~");
		Text = Text.replace("%D9%92", "ú");
		Text = Text.replace("%7D", "}");
		Text = Text.replace("%7B", "{");
		Text = Text.replace("%E2%80%99", "’");
		Text = Text.replace("%2C", ",");
		Text = Text.replace("%D8%9F", "¿");
		Text = Text.replace("%D9%91", "ø");
		Text = Text.replace("%21", "!");
		Text = Text.replace("%40", "@");
		Text = Text.replace("%23", "#");
		Text = Text.replace("%24", "$");
		Text = Text.replace("%5E", "^");
		Text = Text.replace("%26", "&");
		Text = Text.replace("%2A", "*");
		Text = Text.replace("%2B", "+");
		Text = Text.replace("%3D", "=");
		Text = Text.replace("\\u0026", "&");
		return Text;
	}

	public static void SaveFolder(Context mContext, String folder) {
		SharedPreferences sp = mContext.getSharedPreferences("OG_Downloader",
				Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putString("DlFolder", folder);
		editor.apply();
		editor.commit();
	}

	public static String LoadFolder(Context mContext) {
		SharedPreferences sp = mContext.getSharedPreferences("OG_Downloader",
				Context.MODE_PRIVATE);
		return sp.getString("DlFolder", Environment
				.getExternalStorageDirectory().getPath() + "/Download");
	}
}
