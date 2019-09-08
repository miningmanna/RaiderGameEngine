package org.rge.lua;

import org.luaj.vm2.LuaTable;

public class EngineReference extends LuaTable {
	
	public Object parent;
	
	public EngineReference(Object parent) {
		this.parent = parent;
	}
	
}
