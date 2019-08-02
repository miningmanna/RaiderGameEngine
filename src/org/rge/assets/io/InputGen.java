package org.rge.assets.io;

import java.io.InputStream;

public interface InputGen {
	
	public void init(String path);
	public InputStream getInput(String path);
	public void destroy();
	
}