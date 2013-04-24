package com.ghareeb.YouTube;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Toast;

public class FileUtils {
	public static boolean checkIfFileExistAndNotEmpty(String fullFileName) {
		File f = new File(fullFileName);
		long lengthInBytes = f.length();
		Log.d(Prefs.TAG, fullFileName + " length in bytes: " + lengthInBytes);
		if (lengthInBytes > 100)
			return true;
		else {
			return false;
		}
	
	}
	
	public static boolean checkIfFolderExists(String fullFileName) {
		File f = new File(fullFileName);
		//Log.d(Prefs.TAG,"Checking if : " +  fullFileName + " exists" );
		if (f.exists() && f.isDirectory()) {
			//Log.d(Prefs.TAG,"Direcory: " +  fullFileName + " exists" );
			return true;
		}
		else {
			return false;
		}
	}
	
	public static boolean createFolder(String folderPath) {
		File f = new File(folderPath);
		return f.mkdirs();
	}
	
	public static void deleteFile(String fullFileName) {
		File f = new File(fullFileName);
		boolean isdeleted = f.delete();
		Log.d(Prefs.TAG, "deleteing: " + fullFileName + " isdeleted: " + isdeleted);
	}
	
	public static void createFile(String fullFileName) {
		File f = new File(fullFileName);
		try {
			f.createNewFile();
		} catch (IOException e) {
			Log.e(Prefs.TAG, e.getMessage());
		}
	}
	
	
	public static long getFileSizeInBytes(String fullFileName) {
		File f = new File(fullFileName);
		long lengthInBytes = f.length();
		 Log.d(Prefs.TAG, "fullFileName length in bytes: " + lengthInBytes);
		return lengthInBytes;
	
	}
	
	public static void writeSystemLogPart(){
		Log.d(Prefs.TAG, "start SystemLog print");
		
		//File f = new File(Prefs.LOG_FILE_PATH);
		//f.delete();
		deleteFile(Prefs.LOG_FILE_PATH);
		
		File f2 = new File(Prefs.LOG_FILE_PATH);
		String LINE_SEPARATOR = System.getProperty("line.separator");
        final StringBuffer log = new StringBuffer();
        try{
            ArrayList<String> commandLine = new ArrayList<String>();
            commandLine.add("logcat");
            commandLine.add("-d");
            commandLine.add("-v"); 
            commandLine.add("threadtime");
            commandLine.add("-t");
            commandLine.add("300");
            commandLine.add("Videokit:E"); 
            commandLine.add("Videokit:D");
            commandLine.add("Videokit:I");
            commandLine.add("*:S");
 
            Process process = Runtime.getRuntime().exec(commandLine.toArray(new String[0]));

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String line;
            //Log.d(Prefs.TAG, "Starting while loop");
            int lineCounter = 0;
            while ((line = bufferedReader.readLine()) != null){ 
                log.append(line);
                log.append(LINE_SEPARATOR); 
                lineCounter++;
             }
            //Log.d(Prefs.TAG, "out of while loop, writing file: " + lineCounter);
            FileWriter writer = new FileWriter(f2);
            writer.append(log.toString());
            writer.flush();
            writer.close();
        } 
        catch (IOException e){
            Log.e(Prefs.TAG, "CollectLogTask.doInBackground failed", e);
        } 
        Log.d(Prefs.TAG, "end SystemLog print");
     }
	
	
	
	public static String getValidFFMpgegFileNameFromPath(String path) {
		int startIndex = path.lastIndexOf("/") + 1;
		int endIndex = path.lastIndexOf(".");
		
		String name = path.substring(startIndex, endIndex) + "_sl_" + System.currentTimeMillis();
		String ext = path.substring(endIndex + 1);
		Log.d(Prefs.TAG, "name: " + name + " ext: " + ext);
		String validName = (name.replaceAll("\\Q.\\E", "_")).replaceAll(" ", "_");
		return validName + "." + ext;
	}
	
	public static String getValidFileNameFromPath(String path) {
		int startIndex = path.lastIndexOf("/") + 1;
		int endIndex = path.lastIndexOf(".");
		
		String name = path.substring(startIndex, endIndex);
		String ext = path.substring(endIndex + 1);
		Log.d(Prefs.TAG, "name: " + name + " ext: " + ext);
		String validName = (name.replaceAll("\\Q.\\E", "_")).replaceAll(" ", "_");
		return validName + "." + ext;
	}

	public static String getSystemLogAsString(){
		
		String LINE_SEPARATOR = System.getProperty("line.separator");
        final StringBuffer log = new StringBuffer();
        try{
            ArrayList<String> commandLine = new ArrayList<String>();
            commandLine.add("logcat");
            commandLine.add("-d");
            commandLine.add("-t");
            commandLine.add("20");
            commandLine.add("Videokit:E"); 
            commandLine.add("Videokit:D");
            commandLine.add("Videokit:I");
            commandLine.add("*:S");
 
            Process process = Runtime.getRuntime().exec(commandLine.toArray(new String[0]));

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String line;
            Log.d(Prefs.TAG, "Starting while loop");
            int lineCounter = 0;
            while ((line = bufferedReader.readLine()) != null){ 
                log.append(line);
                log.append(LINE_SEPARATOR); 
                lineCounter++;
             }
            Log.d(Prefs.TAG, "out of while loop, writing ouput: " + lineCounter);
            

        } 
        catch (IOException e){
            Log.e(Prefs.TAG, "CollectLogTask.doInBackground failed", e);
        } 
        
        return log.toString();
     }
	
	// /mnt/sdcard/DCIM/Camera/VID_20120105_173541.3gp -> /mnt/sdcard/DCIM/Camera/
	public static String getWorkingFolderFromFilePath(String filePath) {
		int index = filePath.lastIndexOf("/");
		return filePath.substring(0, index + 1);
	}
	
	// /mnt/sdcard/DCIM/Camera/VID_20120105_173541.3gp -> VID_20120105_173541.3gp
	public static String getFileNameFromFilePath(String filePath) {
		int index = filePath.lastIndexOf("/");
		return filePath.substring(index + 1 , filePath.length());
	}
	
	public static String copyFileToFolder(String filePath, String folderPath) {
		Log.i(Prefs.TAG, "Coping file: " + filePath + " to: " + folderPath);
		String validFilePathStr = filePath;
		try {
			FileInputStream is = new FileInputStream(filePath); 
			BufferedOutputStream o = null;
			String validFileName = getValidFileNameFromPath(filePath);
			validFilePathStr = folderPath + validFileName;
			File destFile = new File(validFilePathStr);
			try {
				byte[] buff = new byte[10000];
				int read = -1;
				o = new BufferedOutputStream(new FileOutputStream(destFile), 10000);
				while ((read = is.read(buff)) > -1) { 
					o.write(buff, 0, read);
				}
			} finally {
				is.close();
				if (o != null) o.close();  

			}
		} catch (FileNotFoundException e) {
			Log.e(Prefs.TAG, e.getMessage());
		} catch (IOException e) {
			Log.e(Prefs.TAG, e.getMessage());
		}
		return validFilePathStr;
	}
	
	public static String getCopyValidFilePath(String filePath, String folderPath) {
		String validFileName = getValidFileNameFromPath(filePath);
		String validFilePathStr = folderPath + validFileName;
		return validFilePathStr;
	}
	
	public static void appendToFile(String filePath, String line) {
		File f2 = new File(filePath);
		String LINE_SEPARATOR = System.getProperty("line.separator");
		StringBuffer sb = new StringBuffer();
		sb.append(LINE_SEPARATOR).append(line);
		
		try {
			// open writer in append mode (if false will overwrite the file)
			FileWriter writer = new FileWriter(f2, true);
			
			writer.append(sb.toString());
			writer.flush();
			writer.close();
		} catch (IOException e) {
			Log.e(Prefs.TAG, "Failed to write to : " + Prefs.VIDEOKIT_LOG_FILE_PATH + " " + e.getMessage());
		}
	}
	
	public static void writeToLocalLog(String line) {
		//appendToFile(Prefs.VIDEOKIT_LOG_FILE_PATH, line);
	}
	
	public static boolean isFileContainSpaces(String fileName) {
		if (fileName.contains(" ")) {
			return true;
		}
		else {
			return false;
		}
		
	}
	
	public static long checkIfFileExistAndNotEmptyReturnSize(String fullFileName) {
		File f = new File(fullFileName);
		long lengthInBytes = f.length();
		Log.d(Prefs.TAG, fullFileName + " length in bytes: " + lengthInBytes);
		return lengthInBytes;
	
	}
	
	public static String getDutationFromVCLog() {
		String duration = null;
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(Prefs.VK_LOG)));
			String line;
			int lineCounter = 0;
			
			while ((line = bufferedReader.readLine()) != null){ 
				// Duration: 00:00:04.20, start: 0.000000, bitrate: 1601 kb/s 
				int i1 = line.indexOf("Duration:");
				int i2 = line.indexOf(", start");
				if (i1 != -1 && i2 != -1) {
					duration = line.substring(i1 + 10, i2 );
					break;
				}
			}
		} catch (FileNotFoundException e) {
			Log.w(Prefs.TAG, e.getMessage());

		} catch (IOException e) {
			Log.w(Prefs.TAG, e.getMessage());
		}
		return duration;
	}
	
	
	public static String getDutationFromVCLogRandomAccess() {
		String duration = null;
		try {
			RandomAccessFile f = new RandomAccessFile(Prefs.VK_LOG, "r");
			String line;
			//f.seek(0);
			
			while ((line = f.readLine()) != null){ 
				//Log.d(Prefs.TAG, line);
				// Duration: 00:00:04.20, start: 0.000000, bitrate: 1601 kb/s 
				int i1 = line.indexOf("Duration:");
				int i2 = line.indexOf(", start");
				if (i1 != -1 && i2 != -1) {
					duration = line.substring(i1 + 10, i2 );
					break;
				}
			}
		} catch (FileNotFoundException e) {
			Log.w(Prefs.TAG, e.getMessage());

		} catch (IOException e) {
			Log.w(Prefs.TAG, e.getMessage());
		}
		return duration;
	}
	
	
	
	
	public static String readLastSizeInKBFromFFmpegLogFile() {
		String sizeStr = "0";
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(Prefs.LOG_FILE_PATH)));
			String line;
			//Log.d(Prefs.TAG, "Starting while loop");
			int lineCounter = 0;
			
			while ((line = bufferedReader.readLine()) != null){ 
				// size= 244kB time=00: 
				int i1 = line.indexOf("size=");
				int i2 = line.indexOf("time=");
				if (i1 != -1 && i2 != -1) {
					sizeStr = line.substring(i1 + 6, i2 - 3);
				}
			}
		} catch (FileNotFoundException e) {
			Log.w(Prefs.TAG, e.getMessage());

		} catch (IOException e) {
			Log.w(Prefs.TAG, e.getMessage());
		}
		return sizeStr.trim();
		
	}
	
	
	public static String readLastTimeFromFFmpegLogFile() {
		String timeStr = "00:00:00.00";
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(Prefs.LOG_FILE_PATH)));
			String line;
			//Log.d(Prefs.TAG, "Starting while loop");
			int lineCounter = 0;
			
			while ((line = bufferedReader.readLine()) != null){ 
				// frame=   26 fps=  0 q=2.0 size=      11kB time=00:00:01.73 bitrate=  50.2kbits/s dup=1 drop=17 
				int i1 = line.indexOf("time=");
				int i2 = line.indexOf("bitrate=");
				if (i1 != -1 && i2 != -1) {
					timeStr = line.substring(i1 + 5, i2 - 1);
				}
			}
		} catch (FileNotFoundException e) {
			Log.w(Prefs.TAG, e.getMessage());

		} catch (IOException e) {
			Log.w(Prefs.TAG, e.getMessage());
		}
		return timeStr.trim();
		
	}
	
	public static String readLastTimeFromFFmpegLogFileUsingRandomAccess() {
		String timeStr = "00:00:00.00";
		try {
			RandomAccessFile f = new RandomAccessFile(Prefs.LOG_FILE_PATH, "r");
			String line;
			long endLocation  = f.length();
			long seekLocation = -1;
			if (( seekLocation = endLocation - 100) < 0) {
				seekLocation = 0;
			}
			f.seek(seekLocation);
			//Log.d(Prefs.TAG, "Starting while loop seekLocation: " + seekLocation);

			while ((line = f.readLine()) != null){ 
				// frame=   26 fps=  0 q=2.0 size=      11kB time=00:00:01.73 bitrate=  50.2kbits/s dup=1 drop=17 
				int i1 = line.indexOf("time=");
				int i2 = line.indexOf("bitrate=");
				if (i1 != -1 && i2 != -1) {
					timeStr = line.substring(i1 + 5, i2 - 1);
				}
				else if (line.contains("ffmpeg_exit(0) called")) {
					timeStr = "exit";
				}
				else  if (line.startsWith("main():") || line.startsWith("  Stream #0.0 -> #0.0") 
						|| line.startsWith("  Stream #0.1 -> #0.1")
						|| line.startsWith("Press [q] to stop") 
						|| line.contains("muxing overhead") 
						|| line.startsWith("Warning")
						|| line.contains("from container frame")
						|| line.contains("frames in a packet from")
						|| line.contains("bitrate parameter is set too low")
						) {
					Log.d(Prefs.TAG, "startup lines ignoring...");
					Log.d(Prefs.TAG, line);
				}
				else {
					long diff = f.length() - f.getFilePointer();
				//	Log.d(Prefs.TAG, "diff: " + diff );
					if (diff < 30) {
						Log.w(Prefs.TAG, "line: " + line);
						Log.w(Prefs.TAG, "Looks like error in the log");
						timeStr = "maybe_error";
						writeToLocalLog("maybe error line: " + line);
					}
				}
			}
		} catch (FileNotFoundException e) {
			Log.w(Prefs.TAG, e.getMessage());

		} catch (IOException e) {
			Log.w(Prefs.TAG, e.getMessage());
		}
		return timeStr.trim();
		
	}
	
	
	public static boolean isExitFromFFmpegLogFileUsingRandomAccess() {
		try {
			RandomAccessFile f = new RandomAccessFile(Prefs.LOG_FILE_PATH, "r");
			String line;
			long endLocation  = f.length();
			long seekLocation = -1;
			if (( seekLocation = endLocation - 50) < 0) {
				seekLocation = 0;
			}
			f.seek(seekLocation);
			
			while ((line = f.readLine()) != null){ 
				if (line.contains("ffmpeg_exit(0) called")) {
					return true;
				}
			}
			
		} catch (FileNotFoundException e) {
			Log.w(Prefs.TAG, e.getMessage());

		} catch (IOException e) {
			Log.w(Prefs.TAG, e.getMessage());
		}
		return false;
		
	}
	
	public static int getFileTypeFromFile(String fileName) {
		int endIndex = fileName.lastIndexOf(".");
		String ext = fileName.substring(endIndex + 1);
		if ( (ext.toLowerCase().equals("mp3") ) ||
			 (ext.toLowerCase().equals("wav") ) ||
			 (ext.toLowerCase().equals("ogg") ) ||
			 (ext.toLowerCase().equals("aac") ) ||
			 (ext.toLowerCase().equals("wma") ) 
				) {
			return Prefs.FILE_TYPE_AUDIO;
			
		}
		else if ( (ext.toLowerCase().equals("jpg") ) ||
				(ext.toLowerCase().equals("bmp") ) ||
				(ext.toLowerCase().equals("png") ) ||
				(ext.toLowerCase().equals("jpeg") ) 
		   ) {
			return Prefs.FILE_TYPE_PIC;
		}
		else {
			return Prefs.FILE_TYPE_VIDEO;
		}
			
			
	}
	


}
