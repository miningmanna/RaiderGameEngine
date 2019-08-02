package org.rge.assets.io;

public interface OutputBundle {
	
	public void open();
	public void getOutputStream(String identifier);
	public void close();
	
}
