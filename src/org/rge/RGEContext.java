package org.rge;

import org.luaj.vm2.LuaValue;
import org.rge.assets.AssetManager;
import org.rge.window.Window;

public class RGEContext {
	
	Window window;
	AssetManager am;
	LuaInterface luaInterface;
	LuaEngine luaEngine;
	
	LuaValue initScript;
	LuaValue tickScript;
	
	public RGEContext() {
		
		am = new AssetManager();
		am.registerInputGen("dir", "./");
		am.getAsset("Läl");
		luaInterface = new LuaInterface();
		luaEngine = new LuaEngine(luaInterface);
		
	}
	
	public void init() {
		
		window = new Window("RGE engine");
		
	}
	
	public void tick() {
		
		window.pollEvents();
		
		//luaEngine.run(tickScript);
		
	}
	
	public void render() {
		
		
		
	}
	
	public boolean shouldClose() {
		return window.shouldClose();
	}
	
	public void setShouldClose(boolean shouldClose) {
		window.setShouldClose(shouldClose);
	}
	
	public void destroy() {
		
		// TODO: clean up everything
		
		window.destroy();
		
	}
	
}
