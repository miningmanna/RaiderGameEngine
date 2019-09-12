package org.rge.graphics.light;

import java.awt.Color;

import org.joml.Vector3f;
import org.luaj.vm2.LuaDouble;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.rge.lua.EngineObject;
import org.rge.lua.EngineReference;
import org.rge.lua.compat.LuaUtils;
import org.rge.lua.compat.Vector3;

public class PointLight implements EngineObject {
	
	EngineReference engReference;
	
	public Color color;
	public Vector3 pos;
	public float intensity;
	public float clamp;
	
	public PointLight(Vector3f pos, Color color, float intensity, float clamp) {
		
		initLuaTable();
		
		this.pos = new Vector3();
		if(pos != null)
			this.pos.set(pos);
		
		if(color != null)
			this.color = color;
		else
			this.color = Color.WHITE;
		
		this.intensity = intensity;
		this.clamp = clamp;
	}
	
	public PointLight(Vector3f dir, float intensity) {
		this(dir, null, intensity, 100);
	}
	
	@Override
	public EngineReference getEngineReference() {
		return engReference;
	}
	
	private void initLuaTable() {
		engReference = new EngineReference(this);
		
		engReference.set("color", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				if(args.narg() != 3)
					return LuaUtils.fromColor(color);
				for(int i = 0; i < 3; i++)
					if(!(args.arg(1+i) instanceof LuaInteger || args.arg(1+i) instanceof LuaDouble))
						return LuaUtils.fromColor(color);
				
				color = new Color(args.arg(1).checkint(), args.arg(2).checkint(), args.arg(3).checkint());
				return LuaUtils.fromColor(color);
			}
		});
		engReference.set("intensity", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				if(!(arg0 instanceof LuaDouble || arg0 instanceof LuaInteger))
					return LuaValue.valueOf(intensity);
				
				intensity = (float) arg0.checkdouble();
				
				return LuaValue.valueOf(intensity);
			}
		});
		engReference.set("clamp", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				if(!(arg0 instanceof LuaDouble || arg0 instanceof LuaInteger))
					return LuaValue.valueOf(clamp);
				
				clamp = (float) arg0.checkdouble();
				
				return LuaValue.valueOf(clamp);
			}
		});
		engReference.set("position", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				Vector3 pos = LuaUtils.getVector3Arg(arg0);
				if(pos == null)
					return PointLight.this.pos.getEngineReference();
				PointLight.this.pos = pos;
				return PointLight.this.pos.getEngineReference();
			}
		});
		
	}
	
}
