package org.rge.assets.io;

import java.io.InputStream;

public interface InputGen {
	
	public boolean init(String path);
	public boolean exists(String path);
	public boolean isDead();
	public InputStream getInput(String path);
	public void destroy();
	
}