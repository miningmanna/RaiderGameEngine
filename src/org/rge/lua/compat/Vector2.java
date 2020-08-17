package org.rge.lua.compat;

import org.joml.Vector2f;
import org.luaj.vm2.LuaDouble;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;
import org.rge.lua.EngineObject;
import org.rge.lua.EngineReference;

public class Vector2 extends Vector2f implements EngineObject {
	
	EngineReference ref;
	
	public Vector2(float x, float y) {
		this.x = x;
		this.y = y;
		
		initLuaTable();
	}
	
	public Vector2() { this(0, 0); }
	
	private void initLuaTable() {
		
		ref = new EngineReference(this);
		ref.set("x", X_FUNCTION);
		ref.set("y", Y_FUNCTION);
		
	}
	
	@Override
	public EngineReference getEngineReference() {
		return ref;
	}
	
	private static final TwoArgFunction X_FUNCTION = new TwoArgFunction() {
		@Override
		public LuaValue call(LuaValue arg0, LuaValue arg1) {
			if(!(arg0 instanceof EngineReference))
				return NIL;
			EngineReference enRef = (EngineReference) arg0;
			if(!(enRef.parent instanceof Vector2))
				return NIL;
			Vector2 ref = (Vector2) enRef.parent;
			
			if(!(arg1 instanceof LuaInteger || arg1 instanceof LuaDouble))
				return LuaValue.valueOf(ref.x);
			
			ref.x = arg1.tofloat();
			
			return LuaValue.valueOf(ref.x);
		}
	};
	private static final TwoArgFunction Y_FUNCTION = new TwoArgFunction() {
		@Override
		public LuaValue call(LuaValue arg0, LuaValue arg1) {
			if(!(arg0 instanceof EngineReference))
				return NIL;
			EngineReference enRef = (EngineReference) arg0;
			if(!(enRef.parent instanceof Vector2))
				return NIL;
			Vector2 ref = (Vector2) enRef.parent;
			
			if(!(arg1 instanceof LuaInteger || arg1 instanceof LuaDouble))
				return LuaValue.valueOf(ref.y);
			
			ref.y = arg1.tofloat();
			
			return LuaValue.valueOf(ref.y);
		}
	};
	
}
