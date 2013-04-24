package com.google.android.ogyoutube.core.model;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.ghareeb.YouTube.BtnDownload;

import android.app.Activity;
import android.net.Uri;
import android.view.View;

public class Video {

	public final String id=new String("564");
	public final String title=new String("564");
	  
	public void Video() {
	
	}

	public void a(Video v) {
		View paramView2 = null;
		((BtnDownload) paramView2.findViewWithTag("download_button"))
				.setVideo(v);
	}
}
