package org.rge;

import org.luaj.vm2.LuaTable;

public abstract class EngineObject {
	
	protected LuaTable functionTable;
	
	public LuaTable getLuaInterface() {
		return functionTable;
	}
	
	protected abstract void initLuaInterface();
	
}
