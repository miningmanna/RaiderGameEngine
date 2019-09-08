package org.rge.graphics.light;

import java.awt.Color;

import org.joml.Vector3f;
import org.luaj.vm2.LuaDouble;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.rge.lua.EngineObject;
import org.rge.lua.EngineReference;

public class DirectionalLight implements EngineObject {
	
	EngineReference engReference;
	
	public Vector3f dir;
	public Color color;
	public float intensity;
	public float clamp;
	
	public DirectionalLight(Vector3f dir, Color color, float intensity, float clamp) {
		
		initLuaTable();
		
		if(dir != null)
			this.dir = new Vector3f(dir);
		else
			this.dir = new Vector3f();
		
		if(color != null)
			this.color = color;
		else
			this.color = Color.WHITE;
		
		this.intensity = intensity;
		this.clamp = clamp;
	}
	
	public DirectionalLight(Vector3f dir, float intensity) {
		this(dir, null, intensity, 100);
	}
	
	@Override
	public EngineReference getEngineReference() {
		return engReference;
	}
	
	private void initLuaTable() {
		engReference = new EngineReference(this);
		
		engReference.set("setColor", new ThreeArgFunction() {
			
			@Override
			public LuaValue call(LuaValue arg0, LuaValue arg1, LuaValue arg2) {
				if(!(arg0 instanceof LuaInteger))
					return null;
				if(!(arg1 instanceof LuaInteger))
					return null;
				if(!(arg2 instanceof LuaInteger))
					return null;
				
				color = new Color(arg0.checkint(), arg1.checkint(), arg2.checkint());
				
				return null;
			}
		});
		
		engReference.set("setIntensity", new OneArgFunction() {
			
			@Override
			public LuaValue call(LuaValue arg0) {
				if(!(arg0 instanceof LuaDouble))
					return null;
				
				intensity = (float) arg0.checkdouble();
				
				return null;
			}
		});
		
	}
	
}
