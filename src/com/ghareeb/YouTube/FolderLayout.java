package com.ghareeb.YouTube;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class FolderLayout extends LinearLayout implements OnItemClickListener {

	Context context;
	IFolderItemListener folderListener;
	private List<String> item = null;
	private List<String> path = null;
	private String root = "/";
	private TextView myPath;
	private ListView lstView;

	@SuppressWarnings("unused")
	public FolderLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		LayoutInflater layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = layoutInflater.inflate(getResID("og_folder","layout"), this);
		myPath = (TextView) findViewById(getResID("path","id"));
		lstView = (ListView) findViewById(getResID("list","id"));
		Log.i("FolderView", "Constructed");
		getDir(OG.LoadFolder(context), lstView);
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

	public void setIFolderItemListener(IFolderItemListener folderItemListener) {
		this.folderListener = folderItemListener;
	}

	// Set Directory for view at anytime
	public void setDir(String dirPath) {
		getDir(dirPath, lstView);
	}

	private void getDir(String dirPath, ListView v) {
		myPath.setText(dirPath);
		item = new ArrayList<String>();
		path = new ArrayList<String>();
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				File sel = new File(dir, filename);
				
				return	sel.isDirectory();
			}
		};
		File f = new File(dirPath, "");
		f.mkdirs();
		File[] files = f.listFiles(filter);

		if (!dirPath.equals(root)) {

			item.add(root);
			path.add(root);
			item.add("../");
			path.add(f.getParent());

		}
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			path.add(file.getPath());
			if (file.isDirectory())
				item.add(file.getName() + "/");
			else
				item.add(file.getName());

		}

		Log.i("Folders", files.length + "");

		setItemList(item);

	}

	// can manually set Item to display, if u want
	public void setItemList(List<String> item) {
		ArrayAdapter<String> fileList = new ArrayAdapter<String>(context,
				getResID("og_row","layout"), item);

		lstView.setAdapter(fileList);
		lstView.setOnItemClickListener(this);
	}

	public void onListItemClick(ListView l, View v, int position, long id) {
		File file = new File(path.get(position));
		if (file.isDirectory()) {
			if (file.canRead())
				getDir(path.get(position), l);
			else {
				// what to do when folder is unreadable
				if (folderListener != null) {
					folderListener.OnCannotFileRead(file);
				}
			}
		} else {

			// what to do when file is clicked
			// You can add more,like checking extension,and performing separate
			// actions
			if (folderListener != null) {
				folderListener.OnFileClicked(file);
			}

		}
	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		onListItemClick((ListView) arg0, arg0, arg2, arg3);
	}

}