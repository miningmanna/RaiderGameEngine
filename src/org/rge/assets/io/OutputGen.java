package org.rge.assets.io;

public interface OutputGen {

	public void init();
	public OutputBundle getOutput(String identifier);
	public void destroy();
	
}
