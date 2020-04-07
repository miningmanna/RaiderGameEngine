package org.rge.assets.config;

import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaNil;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.rge.lua.EngineObject;
import org.rge.lua.EngineReference;

public class Config implements EngineObject {
	
	EngineReference engReference;
	
	public ConfigNode baseNode;
	public String pathDelimiter = "/";
	public String pathDelimiterRegex = "/";
	
	public Config() {
		initLuaTable();
	}
	
	protected String[] splitPath(String path) {
		if(path.endsWith(pathDelimiter))
			path = path.substring(0, path.length()-pathDelimiter.length());
		return path.split(pathDelimiterRegex);
	}
	
	public ConfigNode getNode(String path) {
		
		ConfigNode res = baseNode;
		
		String[] nodeNames = splitPath(path);
		for(int i = 0; i < nodeNames.length; i++) {
			res = res.getSubNode(nodeNames[i]);
			if(res == null)
				return null;
		}
		
		return res;
	}
	
	public ConfigNode getNodeFromValuePath(String path) {
		
		ConfigNode res = baseNode;
		
		String[] nodeNames = splitPath(path);
		if(nodeNames.length < 2)
			return baseNode;
		
		for(int i = 0; i < nodeNames.length-1; i++) {
			res = res.getSubNode(nodeNames[i]);
			if(res == null)
				return null;
		}
		
		return res;
	}
	
	public String getValue(String path) {
		LuaValue luaRes = getLuaValue(path);
		if(!(luaRes instanceof LuaNil))
			return luaRes.checkjstring();
		return null;
	}
	
	private LuaValue getLuaValue(String path) {
		
		if(path == null)
			return LuaValue.NIL;
		
		ConfigNode node = baseNode;
		
		String[] nodeNames = splitPath(path);
		if(nodeNames.length < 2)
			return baseNode;
		
		for(int i = 0; i < nodeNames.length-1; i++) {
			node = node.getSubNode(nodeNames[i]);
			if(node == null)
				return LuaValue.NIL;
		}
		
		return node.get(nodeNames[nodeNames.length-1]);
	}
	
	public ConfigNode getBaseNode() {
		return baseNode;
	}
	
	public static class ConfigNode extends LuaTable {
		
		public ConfigNode getSubNode(String identifier) {
			if(identifier == null)
				return null;
			LuaValue res = get(identifier);
			if(res instanceof ConfigNode)
				return (ConfigNode) res;
			return null;
		}
		
		public String getValue(String identifier) {
			if(identifier == null)
				return null;
			LuaValue res = get(identifier);
			if(res instanceof LuaString)
				return res.checkjstring();
			return null;
		}
		
		public String getOptValue(String identifier, String alt) {
			if(identifier == null)
				return alt;
			LuaValue res = get(identifier);
			if(res instanceof LuaString)
				return res.checkjstring();
			return alt;
		}
		
		public int getOptInt(String identifier, int alt) {
			if(identifier == null)
				return alt;
			LuaValue res = get(identifier);
			if(res instanceof LuaInteger)
				return res.checkint();
			return alt;
		}
		
		public double getOptDouble(String identifier, double alt) {
			if(identifier == null)
				return alt;
			LuaValue res = get(identifier);
			if(res instanceof LuaInteger)
				return res.checkint();
			return alt;
		}
		
		public void setValue(String path, String value) {
			if(path == null)
				return;
			if(value == null)
				set(path, NIL);
			else
				set(path, value);
		}
		
		public void setSubNode(String path, ConfigNode node) {
			if(path == null)
				return;
			if(node == null)
				set(path, NIL);
			else
				set(path, node);
		}
		
	}
	
	@Override
	public EngineReference getEngineReference() {
		return engReference;
	}
	
	private void initLuaTable() {
		engReference = new EngineReference(this);
		
		engReference.set("delimiter", LuaValue.valueOf(pathDelimiter));
		
		engReference.set("getBaseNode", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return baseNode;
			}
		});
		engReference.set("getNode", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				if(!(arg0 instanceof LuaString))
					return NIL;
				ConfigNode res = getNode(arg0.checkjstring());
				if(res != null)
					return res;
				return NIL;
			}
		});
		engReference.set("getParentNode", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				if(!(arg0 instanceof LuaString))
					return NIL;
				ConfigNode res = getNodeFromValuePath(arg0.checkjstring());
				if(res != null)
					return res;
				return NIL;
			}
		});
		engReference.set("getValue", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				if(!(arg0 instanceof LuaString))
					return NIL;
				return getLuaValue(arg0.checkjstring());
			}
		});
	}
	
}
