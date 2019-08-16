package org.rge.loaders;

import org.rge.assets.AssetManager;
import org.rge.assets.models.Model.RawData;

public interface ModelRawDataLoader {
	
	public void init();
	public RawData getModelRawData(String path, AssetManager am);
	public void destroy();
	
}
