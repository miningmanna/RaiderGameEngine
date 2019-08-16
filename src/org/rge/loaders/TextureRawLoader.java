package org.rge.loaders;

import java.awt.image.BufferedImage;

import org.rge.assets.AssetManager;

public interface TextureRawLoader {
	
	public void init();
	public boolean canRead(String path);
	public BufferedImage getRawImage(String path, AssetManager am);
	public void destroy();
	
}
