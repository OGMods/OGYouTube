package  uk.co.halfninja.videokit;

public final class Videokit {

  static {
    System.loadLibrary("videokit");
  }
	
  public native void run(String[] args);
  public native void fexit();
  public native void fexit(int i);
  
}
