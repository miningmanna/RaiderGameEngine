package org.rge.lua.compat;

import org.rge.lua.EngineObject;
import org.rge.lua.EngineReference;

import java.lang.reflect.Method;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.luaj.vm2.LuaDouble;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

public class Matrix4 extends Matrix4f implements EngineObject {
	
	EngineReference ref;
	
	public Matrix4() {
		super();
		initLuaTable();
	}
	
	public Matrix4(Matrix4 p) {
		super(p);
		initLuaTable();
	}

	private void initLuaTable() {
		ref = new EngineReference(this);
		
		ref.set("identity", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				identity();
				return ref;
			}
		});
		for(int i = 0; i < 4; i++)
			for(int j = 0; j < 4; j++)
				ref.set(("m" + i) + j, createMatrixSetter(i, j));
		ref.set("translate", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue val) {
				if(val instanceof EngineReference) {
					EngineReference ref = (EngineReference) val;
					if(ref.parent instanceof Vector3f) {
						translate((Vector3f) ref.parent);
					}
				}
				return ref;
			}
		});
	}
	
	private OneArgFunction createMatrixSetter(int column, int row) {
		OneArgFunction res = new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue val) {
				Method set = null;
				try {
					set = Matrix4.class.getMethod(("m" + column) + row, float.class);
				} catch (Exception e) {
					e.printStackTrace();
				}
				Method get = null;
				try {
					get = Matrix4.class.getMethod(("m" + column) + row, new Class<?>[]{});
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				if(val instanceof LuaDouble || val instanceof LuaInteger) {
					
					float f = val.tofloat();
					try {
						set.invoke(Matrix4.this, f);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
				
				float res = 0;
				try {
					res = (float) get.invoke(Matrix4.this);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return LuaValue.valueOf(res);
			}
		};
		
		return res;
	}
	
	@Override
	public EngineReference getEngineReference() {
		return ref;
	}
	
}
