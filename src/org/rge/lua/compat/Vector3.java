package org.rge.lua.compat;

import org.joml.Vector3f;
import org.luaj.vm2.LuaDouble;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.rge.lua.EngineObject;
import org.rge.lua.EngineReference;

public class Vector3 extends Vector3f implements EngineObject {
	
	EngineReference engReference;
	
	public Vector3(float x, float y, float z) {
		super(x, y, z);
		initLuaTable();
	}
	
	public Vector3() {
		this(0, 0, 0);
	}
	
	public Vector3(Vector3 o) {
		this(o.x, o.y, o.z);
	}

	@Override
	public EngineReference getEngineReference() {
		return engReference;
	}
	
	private void initLuaTable() {
		engReference = new EngineReference(this);
		
		engReference.set("x", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				if(!(arg0 instanceof LuaInteger || arg0 instanceof LuaDouble))
					return LuaValue.valueOf(x);
				x = (float) arg0.checkdouble();
				return LuaValue.valueOf(x);
			}
		});
		engReference.set("y", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				if(!(arg0 instanceof LuaInteger || arg0 instanceof LuaDouble))
					return LuaValue.valueOf(y);
				y = (float) arg0.checkdouble();
				return LuaValue.valueOf(y);
			}
		});
		engReference.set("z", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				if(!(arg0 instanceof LuaInteger || arg0 instanceof LuaDouble))
					return LuaValue.valueOf(z);
				z = (float) arg0.checkdouble();
				return LuaValue.valueOf(z);
			}
		});
		engReference.set("distance", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				Vector3f other = LuaUtils.getVector3Arg(arg0);
				if(other == null)
					return NIL;
				return LuaValue.valueOf(distance(other));
			}
		});
		engReference.set("add", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				Vector3f other = LuaUtils.getVector3Arg(arg0);
				if(other == null)
					return NIL;
				Vector3.this.add(other);
				return engReference;
			}
		});
		engReference.set("sub", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				Vector3f other = LuaUtils.getVector3Arg(arg0);
				if(other == null)
					return NIL;
				Vector3.this.sub(other);
				return engReference;
			}
		});
		engReference.set("mul", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				Vector3f other = LuaUtils.getVector3Arg(arg0);
				if(other != null)
					Vector3.this.mul(other);
				else if(arg0 instanceof LuaInteger || arg0 instanceof LuaDouble)
					mul(arg0.checkdouble());
				return engReference;
			}
		});
		engReference.set("div", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				Vector3f other = LuaUtils.getVector3Arg(arg0);
				if(other != null)
					Vector3.this.div(other);
				else if(arg0 instanceof LuaInteger || arg0 instanceof LuaDouble)
					div(arg0.checkdouble());
				return engReference;
			}
		});
		engReference.set("dot", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				Vector3f other = LuaUtils.getVector3Arg(arg0);
				if(other == null)
					return NIL;
				return LuaValue.valueOf(dot(other));
			}
		});
		engReference.set("normalize", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				normalize();
				return engReference;
			}
		});
		engReference.set("rotate", new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0, LuaValue arg1) {
				Vector3f axis = LuaUtils.getVector3Arg(arg0);
				if(axis == null)
					return engReference;
				if(!(arg1 instanceof LuaInteger || arg1 instanceof LuaDouble))
					return engReference;
				rotateAxis((float) arg1.checkdouble(), axis.x, axis.y, axis.z);
				return engReference;
			}
		});
	}
}
