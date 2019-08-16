package org.rge.assets.io;

import java.io.InputStream;

public interface InputGen {
	
	public static boolean canMount(String path) { return false; }
	
	public void init(String path);
	public boolean exists(String path);
	public InputStream getInput(String path);
	public void destroy();
	
}