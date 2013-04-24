package com.ghareeb.YouTube;

import java.io.*;
import java.util.*;
import com.ghareeb.YouTube.OG.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.android.ogyoutube.core.model.Video;

import android.annotation.SuppressLint;
import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

@SuppressLint("DefaultLocale")
public class BtnDownload extends ImageButton implements OnClickListener {

	public String[] Videos;
	public YouTubeVideo[] YTVideos;
	public Boolean loaded = false;
	public String id;
	public String title;
	public String ext;

	public BtnDownload(Context context) {
		super(context);
		this.setOnClickListener(this);
	}

	public BtnDownload(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setOnClickListener(this);
	}

	public BtnDownload(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.setOnClickListener(this);
	}

	public class DownloadPage extends AsyncTask<String, Integer, String> {
		protected String doInBackground(String... urls) {
			int count = urls.length;
			String rs = null;
			 for (int i = 0; i < count; i++) {

				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet(urls[i]);
				ResponseHandler<String> responseHandler = new BasicResponseHandler();
				try {
					rs = client.execute(request, responseHandler);
					int start = rs.indexOf("url_encoded_fmt_stream_map")
							+ ("url_encoded_fmt_stream_map=\"").length();
					int end = rs.indexOf("\",", start + 1);
					String page = OG.ConvertNetChar(rs.substring(start, end));
					page = page.replace("sig=", "signature=");
					String Splitter = page.substring(0, page.indexOf("="))
							.trim();
					if (Splitter.startsWith("\""))
						Splitter = Splitter.substring(1);
					String[] videos = page.trim().split("," + Splitter + "=");
					YTVideos = new YouTubeVideo[videos.length];
					for (int n = 0; n < videos.length; n++) {
						String URL = videos[n];
						if (!videos[n].startsWith("\"" + Splitter)) {
							URL = Splitter + "=" + videos[n];
						}
						if (!OG.GetInfo(OG.GetiTag(URL)).contains("WebM"))
							YTVideos[n] = new YouTubeVideo(URL);
					}
					List<YouTubeVideo> list = new ArrayList<YouTubeVideo>();
					for (YouTubeVideo video : YTVideos) {
						if (video != null) {
							list.add(video);
						}
					}
					YTVideos = list.toArray(new YouTubeVideo[list.size()]);

				} catch (Exception ex) {
					Toast.makeText(getContext(), getString("OG_Fail"),
							Toast.LENGTH_SHORT).show();
					Log.d("OGMod", "DownloadPage,error=" + ex.toString());
					return null;
				}
				if (isCancelled())
					break;
			}
			return rs;
		}

		protected void onPostExecute(String result) {
			if (result != null)
				loaded = true;
		}
	}

	public void setVideo(Video video) {
		id = video.id;
		title = video.title;
		loaded = false;
		new DownloadPage().execute("http://www.youtube.com/watch?v=" + id);
	}

	@Override
	public void onClick(View v) {
		if (loaded) {

			if (YTVideos == null) {
				loaded = false;
				new DownloadPage().execute("http://www.youtube.com/watch?v="
						+ id);
			} else {

				AlertDialog.Builder builder = new AlertDialog.Builder(
						getContext());
				builder.setTitle("OG Youtube Downloader");
				Videos = new String[YTVideos.length];
				for (int i = 0; i < YTVideos.length; i++) {
					Videos[i] = YTVideos[i].GetInfo();
				}
				builder.setItems(Videos, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						ext = YTVideos[which].GetEXT();
						int id = 0;

						File folder = new File(OG.LoadFolder(getContext()));
						folder.mkdirs();
						String f = folder.getPath();
						if (!f.endsWith("/")) {
							f += "/";
						}
						String name = OG.CorrectFileName(title) + "." + ext;
						File file = new File(f + name);
						while (file.exists()) {
							id++;
							name = OG.CorrectFileName(title) + "_" + id + "."
									+ ext;
							file = new File(f + name);
						}
						final String url = YTVideos[which].URL;
						final String fname = f + name;
						final String title = BtnDownload.this.title;
						Intent i = new Intent(getContext(),
								DownloadService.class);
						i.putExtra("Action", "NewDownload");
						i.putExtra("downloadUrl", url);
						i.putExtra("fileName", fname);
						i.putExtra("title", title);
						i.putExtra("videoID", BtnDownload.this.id);
						getContext().startService(i);
					}
				});
				Dialog dg = builder.create();
				dg.show();
			}

		} else {
			Toast.makeText(getContext(), getString("OG_Scan"),
					Toast.LENGTH_SHORT).show();

		}
	}

	public String getString(String name) {
		return getContext().getString(
				getContext().getResources().getIdentifier(name, "string",
						getContext().getPackageName()));
	}

	public int getResID(String name, String Type) {
		return getContext().getResources().getIdentifier(name, Type,
				getContext().getPackageName());
	}

}
