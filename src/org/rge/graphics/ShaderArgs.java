package org.rge.graphics;

import java.util.HashMap;

import org.luaj.vm2.LuaDouble;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.rge.lua.EngineObject;
import org.rge.lua.EngineReference;
import org.rge.lua.compat.Matrix4;
import org.rge.lua.compat.Vector3;

public class ShaderArgs implements EngineObject {
	
	EngineReference ref;
	public HashMap<String, Object> args;
	
	public ShaderArgs() {
		args = new HashMap<>();
		initLuaTable();
	}
	
	private void initLuaTable() {
		ref = new EngineReference(this);
		
		ref.set("set", new TwoArgFunction() {
			
			@Override
			public LuaValue call(LuaValue arg0, LuaValue arg1) {
				if(!(arg0 instanceof LuaString))
					return NIL;
				
				String name = arg0.checkjstring();
				if(arg1 == NIL) {
					args.remove(name);
					return NIL;
				}
				
				if(arg1 instanceof EngineReference) {
					
					Object o = ((EngineReference) arg1).parent;
					
					// REF TYPE
					if(o instanceof Vector3)		args.put(name, new Vector3((Vector3) o));
					else if(o instanceof Matrix4)	args.put(name, new Matrix4((Matrix4) o));
					
				} else {
					
					// VALUE TYPE
					if (arg1 instanceof LuaInteger)			args.put(name, arg1.checkint());
					else if (arg1 instanceof LuaDouble)		args.put(name, (float) arg1.checkdouble());
					else if (arg1 instanceof LuaString)		args.put(name, arg1.checkjstring());
					
				}
				
				return NIL;
			}
		});
		
		ref.set("get", new OneArgFunction() {
			
			@Override
			public LuaValue call(LuaValue arg0) {
				
				if(!(arg0 instanceof LuaString))
					return NIL;
				
				String name = arg0.checkjstring();
				
				Object o = args.get(name);
				
				if(o instanceof EngineReference) {
					
					Object p = ((EngineReference) o).parent;
					if(p instanceof Vector3)		return new Vector3((Vector3) p).getEngineReference();
					else if(p instanceof Matrix4)	return new Matrix4((Matrix4) p).getEngineReference();
				} else {
					if(o instanceof String)			return LuaValue.valueOf((String) o);
					else if(o instanceof Float)		return LuaValue.valueOf((Float) o);
					else if(o instanceof Integer)	return LuaValue.valueOf((Integer) o);
				}
				
				return NIL;
			}
		});
		
	}
	
	@Override
	public EngineReference getEngineReference() {
		return ref;
	}
	
}
