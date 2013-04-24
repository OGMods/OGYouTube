package com.ghareeb.YouTube;

import java.util.ArrayList;

import com.ghareeb.YouTube.OG.Download;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DownloadsListAdapter extends ArrayAdapter<Download>{
	public Download[] objects;
	Download SelectedItem;
	ListView list;

	public DownloadsListAdapter(Context context, int textViewResourceId,
			Download[] objects, ListView list) {
		super(context, textViewResourceId, objects);
		this.objects = objects;
		this.list = list;
	}

	public int getResID(String name, String Type) {
		return this.getContext().getResources()
				.getIdentifier(name, Type, this.getContext().getPackageName());
	}

	@SuppressLint("DefaultLocale")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
		Download dl = objects[position];
		View row = inflater.inflate(getResID("og_download", "layout"), parent,
				false);
		row.setId(position);
		row.findViewById(getResID("notifiation_image", "id")).setVisibility(
				View.GONE);
		row.findViewById(getResID("separator1", "id")).setVisibility(View.GONE);
		if (position < objects.length - 1) {
			row.findViewById(getResID("separator2", "id")).setVisibility(
					View.VISIBLE);
		}

		TextView FileName = (TextView) row.findViewById(getResID("file_name",
				"id"));
		TextView DownloadInfo = (TextView) row.findViewById(getResID(
				"download_info", "id"));
		TextView DownloadSpeed = (TextView) row.findViewById(getResID(
				"download_speed", "id"));
		ProgressBar pb = (ProgressBar) row.findViewById(getResID("downloaded",
				"id"));
		if (pb.getMax() < 100) {
			pb.setMax(100);
		}
		for (View v : getAllChildren(row)) {
			v.setClickable(false);
			v.setFocusable(false);
		}
		if (!FileName.getText().equals(dl.Title)) {
			FileName.setText(dl.Title);
		}

		if (pb.getProgress() != dl.Percent()) {
			pb.setProgress(dl.Percent());
		}

		DownloadSpeed.setText(dl.Percent() + "%");

		if (dl.TStatus == "") {

			DownloadInfo.setText(OG.ConvertByte(dl.Speed) + "/s  ("
					+ OG.ConvertByte(dl.Downloaded) + " / "
					+ OG.ConvertByte(dl.TotalBytes) + ")");

		} else {
			DownloadInfo.setText(dl.TStatus);
		}
		pb.setProgress(dl.Percent());

		return row;
	}

	public String getString(String name) {
		return getContext().getString(
				getContext().getResources().getIdentifier(name, "string",
						getContext().getPackageName()));
	}

	private ArrayList<View> getAllChildren(View v) {

		if (!(v instanceof ViewGroup)) {
			ArrayList<View> viewArrayList = new ArrayList<View>();
			viewArrayList.add(v);
			return viewArrayList;
		}

		ArrayList<View> result = new ArrayList<View>();

		ViewGroup vg = (ViewGroup) v;
		for (int i = 0; i < vg.getChildCount(); i++) {

			View child = vg.getChildAt(i);

			ArrayList<View> viewArrayList = new ArrayList<View>();
			viewArrayList.add(v);
			viewArrayList.addAll(getAllChildren(child));

			result.addAll(viewArrayList);
		}
		return result;
	}

	public Download getItemAtPosition(int pos) {
		return objects[pos];
	}

}