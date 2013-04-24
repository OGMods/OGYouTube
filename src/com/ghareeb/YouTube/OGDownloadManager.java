package com.ghareeb.YouTube;

import android.widget.AdapterView.OnItemClickListener;
import com.ghareeb.YouTube.OG.Download;
import android.content.*;
import android.content.res.Configuration;
import android.widget.*;
import android.net.Uri;
import android.util.*;
import android.view.*;
import android.app.*;
import java.io.File;
import android.os.*;
import java.util.*;

public class OGDownloadManager extends Activity {

	ListView list;
	TextView txtLoad;
	DownloadsListAdapter AList;
	Download SelectedItem;
	Handler h;
	DBDownloads database;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int exp = (int) (Math.log(50) / Math.log(3));
		Log.e("OGMod", "exp=" + exp + "," + (50 / 3) + "." + exp);
		setContentView(getResID("og_dlmgr", "layout"));
		database = new DBDownloads(getBaseContext());
		h = new Handler();
		Intent intent = getIntent();
		if (intent.hasExtra("NotifyID")) {
			Log.i("OGMod", "Recive NotifyID");
			int ID = intent.getExtras().getInt("NotifyID");
			Intent i = new Intent(getBaseContext(), DownloadService.class);
			i.putExtra("Action", "RN");
			i.putExtra("NotifyID", ID);
			getBaseContext().startService(i);
		}
		list = (ListView) findViewById(getResID("OG_lstDownloads", "id"));
		txtLoad = (TextView) this.findViewById(getResID("txtLoad", "id"));
		txtLoad.setVisibility(View.VISIBLE);
		list.setVisibility(View.GONE);

		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {
				Download item = (Download) AList.getItemAtPosition(position);
				SelectedItem = item;
				registerForContextMenu(view);
				openContextMenu(view);
				unregisterForContextMenu(view);
			}
		});

		Download[] Downloads = database.GetDownloads();
		AList = new DownloadsListAdapter(this,
				getResID("og_download", "layout"), Downloads, list);
		list.setAdapter(AList);
		if (Downloads.length > 0) {
			txtLoad.setText(getString("OG_Load"));
			txtLoad.setVisibility(View.GONE);
			list.setVisibility(View.VISIBLE);
		} else {
			txtLoad.setText(getString("OG_NoDl"));
		}
		TimerTask task2 = new Updater();
		Timer t2 = new Timer();
		t2.schedule(task2, 1, 2000);
	}

	class Updater extends TimerTask {
		@Override
		public void run() {
			h.post(new Runnable() {
				@Override
				public void run() {
					UpdateUI();
				}
			});

		}
	}

	public void UpdateUI() {
		try {
			Download[] Downloads = database.GetDownloads();
			if (Downloads.length == AList.objects.length) {
				AList.objects = Downloads;
				AList.notifyDataSetChanged();
			} else {
				AList = new DownloadsListAdapter(this, getResID("og_download",
						"layout"), Downloads, list);
				list.setAdapter(AList);
			}
		} catch (Exception e) {
			Log.e("OGMod", "Updater,error=" + e.toString());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, Menu.FIRST + 5, 0, getString("OG_CDF"));
		menu.add(Menu.NONE, Menu.FIRST + 1, 0, getString("OG_Follow"));
		if (!Locale.getDefault().getLanguage().equals("ar")) {
			menu.add(Menu.NONE, Menu.FIRST + 4, 0, getString("OG_Donate"));
		}
		menu.add(Menu.NONE, Menu.FIRST + 3, 0, getString("OG_About"));
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case Menu.FIRST + 1:
			Intent browserIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("http://www.twitter.com/OsGhareeb"));
			startActivity(browserIntent);
			break;
		case Menu.FIRST + 3:
			// setView
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getString("OG_AboutText"))
					// Nabool Ae
					.setCancelable(false)
					.setPositiveButton(getString("OG_OK"),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.dismiss();
								}
							});
			AlertDialog alert = builder.create();
			alert.show();
			break;
		case Menu.FIRST + 4:
			browserIntent = new Intent(
					Intent.ACTION_VIEW,
					Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=ghareeb.ma12@gmail.com&lc=US&item_name=OG%20YouTube%20Downloader&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donate_SM%2egif%3aNonHosted"));
			startActivity(browserIntent);
			break;
		case Menu.FIRST + 5:
			builder = new AlertDialog.Builder(this);
			final FolderLayout fl = new FolderLayout(this, null);
			builder.setView(fl);
			builder.setTitle(getString("OG_DF"));
			builder.setNegativeButton(getString("OG_Cancel"),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
			builder.setPositiveButton(getString("OG_UCF"),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							String folder = (String) ((TextView) fl
									.findViewById(getResID("path", "id")))
									.getText();
							File f = new File(folder);
							if (f.canWrite()) {
								if (!folder.endsWith("/")) {
									folder += "/";
								}
								OG.SaveFolder(getBaseContext(), folder);
							} else {
								Toast.makeText(getBaseContext(),
										getString("OG_NotW"),
										Toast.LENGTH_SHORT).show();
							}
						}
					});
			alert = builder.create();
			alert.show();
			break;
		}
		return false;
	}

	@Override
	public void onCreateContextMenu(ContextMenu mn, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		Menu menu = mn;
		// m_menu.clear();
		File video = new File(SelectedItem.FileName);
		File audio;
		if (SelectedItem.MP3FileName == null) {
			audio = new File("notfoundfile.d");
		} else {
			audio = new File(SelectedItem.MP3FileName);
		}

		if ((SelectedItem.Status == OG.COMPLETE)
				|| (SelectedItem.Status >= OG.CONVERTING) && (video.exists())) {
			menu.add(Menu.NONE, Menu.FIRST + 1, 0, getString("OG_Open"));
			menu.add(Menu.NONE, Menu.FIRST + 2, 0, getString("OG_Share"));
		}
		if ((SelectedItem.Status < OG.COMPLETE)
				|| (SelectedItem.Status >= OG.CONVERTING) && (video.exists())) {
			menu.add(Menu.NONE, Menu.FIRST + 3, 0, getString("OG_Delete"));
		}

		if ((SelectedItem.Status == OG.COMPLETE)
				|| (SelectedItem.Status >= OG.CONVERTING)) {
			if (SelectedItem.Status == OG.CONVERTED && audio.exists()) {
				menu.add(Menu.NONE, Menu.FIRST + 9, 0, getString("OG_OpenMP3"));
				menu.add(Menu.NONE, Menu.FIRST + 10, 0,
						getString("OG_ShareMP3"));
			}
			if (SelectedItem.Status != OG.CONVERTING
					&& SelectedItem.Status != OG.CONVERT_WAIT) {
				if (SelectedItem.Status == OG.CONVERTED) {
					if (!audio.exists() && video.exists()) {
						menu.add(Menu.NONE, Menu.FIRST + 7, 0,
								getString("OG_Convert"));
					}
				} else {
					if (video.exists()) {
						menu.add(Menu.NONE, Menu.FIRST + 7, 0,
								getString("OG_Convert"));
					}
				}
			} else {
				menu.add(Menu.NONE, Menu.FIRST + 8, 0, getString("OG_CConvert"));
			}
		}

		if (SelectedItem.Status == OG.COMPLETE
				|| SelectedItem.Status == OG.CONVERTED) {
			menu.add(Menu.NONE, Menu.FIRST + 4, 0, getString("OG_DeleteFL"));
		}
		if ((SelectedItem.Status == OG.DOWNLOADING)
				|| (SelectedItem.Status == OG.ERROR)
				|| (SelectedItem.Status == OG.STOPPED)
				|| (SelectedItem.Status == OG.NOTSTARTED)) {
			menu.add(Menu.NONE, Menu.FIRST + 5, 0, getString("OG_SR"));
		}
		if (!(SelectedItem.Status == OG.DOWNLOADING)
				&& !(SelectedItem.Status == OG.CONVERTING)) {
			if (!video.exists()) {
				menu.add(Menu.NONE, Menu.FIRST + 6, 0, getString("OG_Redl"));
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		Download[] Dls;
		Intent i;
		switch (item.getItemId()) {

		case Menu.FIRST + 1: // Open
			try {
				Uri video = Uri.parse(SelectedItem.FileName);
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(video, "video/*");
				startActivity(intent);
				return true;
			} catch (Exception e) {
				Toast.makeText(getBaseContext(), getString("OG_NoPlayers"),
						Toast.LENGTH_SHORT).show();
			}
		case Menu.FIRST + 2: // Share
			Intent share = new Intent(Intent.ACTION_SEND);
			share.setType(OG.GetMimeType(OG.GetiTag(SelectedItem.URL)));
			share.putExtra(Intent.EXTRA_STREAM,
					Uri.fromFile(new File(SelectedItem.FileName)));
			startActivity(Intent.createChooser(share, getString("OG_Share")));
			return true;
		case Menu.FIRST + 3: // Delete
			txtLoad.setVisibility(View.VISIBLE);
			list.setVisibility(View.GONE);

			i = new Intent(getBaseContext(), DownloadService.class);
			i.putExtra("Action", "Delete");
			i.putExtra("NotifyID", SelectedItem.NotifyID);
			getBaseContext().startService(i);

			database.DeleteDownload(SelectedItem.NotifyID);
			File file = new File(SelectedItem.FileName);
			boolean deleted = file.delete();
			if (deleted) {
				Toast.makeText(getBaseContext(), getString("OG_FDeleted"),
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getBaseContext(), getString("OG_CDeleteF"),
						Toast.LENGTH_SHORT).show();
			}
			Dls = database.GetDownloads();
			AList = new DownloadsListAdapter(this, getResID("og_download",
					"layout"), Dls, list);
			list.setAdapter(AList);
			if (Dls.length > 0) {
				txtLoad.setText(getString("OG_Load"));
				txtLoad.setVisibility(View.GONE);
				list.setVisibility(View.VISIBLE);
			} else {
				txtLoad.setText(getString("OG_NoDl"));
			}
			return true;
		case Menu.FIRST + 4: // Delete From List
			txtLoad.setVisibility(View.VISIBLE);
			list.setVisibility(View.GONE);
			database.DeleteDownload(SelectedItem.NotifyID);
			Dls = database.GetDownloads();
			AList = new DownloadsListAdapter(this, getResID("og_download",
					"layout"), Dls, list);
			list.setAdapter(AList);
			if (Dls.length > 0) {
				txtLoad.setText(getString("OG_Load"));
				txtLoad.setVisibility(View.GONE);
				list.setVisibility(View.VISIBLE);
			} else {
				txtLoad.setText(getString("OG_NoDl"));
			}
			return true;
		case Menu.FIRST + 5: // Stop/Resume
			i = new Intent(getBaseContext(), DownloadService.class);
			i.putExtra("Action", "SR");
			i.putExtra("NotifyID", SelectedItem.NotifyID);
			getBaseContext().startService(i);

			return true;
		case Menu.FIRST + 6: // Redownload
			file = new File(SelectedItem.FileName);
			deleted = file.delete();
			i = new Intent(getBaseContext(), DownloadService.class);
			i.putExtra("Action", "Redl");
			i.putExtra("NotifyID", SelectedItem.NotifyID);
			getBaseContext().startService(i);

			return true;
		case Menu.FIRST + 7: // Convert to MP3
			i = new Intent(getBaseContext(), DownloadService.class);
			i.putExtra("Action", "Convert");
			i.putExtra("NotifyID", SelectedItem.NotifyID);
			getBaseContext().startService(i);
			return true;
		case Menu.FIRST + 8: // Stop converting
			i = new Intent(getBaseContext(), DownloadService.class);
			i.putExtra("Action", "StopConvert");
			i.putExtra("NotifyID", SelectedItem.NotifyID);
			getBaseContext().startService(i);
			return true;
		case Menu.FIRST + 9: // Open MP3
			try {
				Uri video = Uri.parse(SelectedItem.MP3FileName);
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(video, "video/*");
				startActivity(intent);
			} catch (Exception e) {
				Toast.makeText(getBaseContext(), getString("OG_NoPlayers"),
						Toast.LENGTH_SHORT).show();
			}
			return true;
		case Menu.FIRST + 10: // Share MP3
			share = new Intent(Intent.ACTION_SEND);
			share.setType("audio/mp3");
			share.putExtra(Intent.EXTRA_STREAM,
					Uri.fromFile(new File(SelectedItem.FileName)));
			startActivity(Intent.createChooser(share, getString("OG_Share")));
			return true;
		}
		return true;
	}

	public void changeLocle(String language) {
		Locale locale = new Locale(language);
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		getBaseContext().getResources().updateConfiguration(config, null);
	}

	public String getString(String name) {
		return getBaseContext().getString(
				getBaseContext().getResources().getIdentifier(name, "string",
						getBaseContext().getPackageName()));
	}

	public int getResID(String name, String Type) {
		return getBaseContext().getResources().getIdentifier(name, Type,
				getBaseContext().getPackageName());
	}

}
