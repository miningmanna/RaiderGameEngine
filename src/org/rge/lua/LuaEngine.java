package org.rge.lua;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ResourceFinder;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.rge.RGEContext;
import org.rge.assets.AssetManager;

public class LuaEngine {
	
	Globals globals;
	
	public LuaEngine(RGEContext context, AssetManager am) {
		
		globals = JsePlatform.standardGlobals();
		
		globals.finder = new ResourceFinder() {
			@Override
			public InputStream findResource(String arg0) {
				return am.getAsset(arg0);
			}
		};
		
		globals.set("rge", context.getEngineReference());
	}
	
	public LuaValue loadScript(String path) {
		if(path == null)
			return null;
		try {
			return globals.load(new FileReader(path), path);
		} catch (FileNotFoundException e) {
			return null;
		}
	}
	
	public void exec(LuaValue script) {
		if(script != null)
			script.call();
	}
	
}
