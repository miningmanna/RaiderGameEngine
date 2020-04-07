package org.rge.assets;

public interface Loader {
	
	public void init(AssetManager am);
	public Object get(String path, AssetManager am);
	public boolean canRead(String path);
	public void destroy();
	
	
}
