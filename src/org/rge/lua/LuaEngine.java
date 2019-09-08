package org.rge.lua;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.rge.RGEContext;

public class LuaEngine {
	
	Globals globals;
	
	public RGEContext context;
	
	public LuaEngine(RGEContext context) {
		
		this.context = context;
		globals = JsePlatform.standardGlobals();
		globals.set("rge", context.getEngineReference());
	}
	
	public LuaValue loadScript(String path) {
		if(path == null)
			return null;
		return globals.loadfile(path);
	}
	
	public void exec(LuaValue script) {
		if(script != null)
			script.call();
	}
	
}
