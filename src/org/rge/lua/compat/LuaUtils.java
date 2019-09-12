package org.rge.lua.compat;

import java.awt.Color;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.rge.lua.EngineReference;

public class LuaUtils {
	
	public static Vector3 getVector3Arg(LuaValue arg) {
		if(arg == null)
			return null;
		if(!(arg instanceof EngineReference))
			return null;
		EngineReference ref = (EngineReference) arg;
		if(!(ref.parent instanceof Vector3f))
			return null;
		return (Vector3) ref.parent;
	}
	
	public static Matrix4f getMatrix4Arg(LuaValue arg) {
		if(arg == null)
			return null;
		if(!(arg instanceof EngineReference))
			return null;
		EngineReference ref = (EngineReference) arg;
		if(!(ref.parent instanceof Matrix4f))
			return null;
		return (Matrix4f) ref.parent;
	}
	
	public static Varargs fromColor(Color c) {
		return LuaValue.varargsOf(new LuaValue[] {
				LuaValue.valueOf(c.getRed()),
				LuaValue.valueOf(c.getGreen()),
				LuaValue.valueOf(c.getBlue())
		});
	}
	
}
