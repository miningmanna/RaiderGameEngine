package org.rge.assets.io;

import java.io.OutputStream;

public interface OutputGen {
	
	public static boolean canWrite(String path) { return false; }
	
	public void init(String path);
	public boolean exists(String path);
	public OutputStream getOutput(String path);
	public void destroy();
	
}
