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

public class SpotLight implements EngineObject {
	
	EngineReference engReference;
	
	public Vector3f position;
	public Vector3f direction;
	public Color color;
	public float intensity;
	public float clamp;
	public float cutoff;
	
	public SpotLight(Vector3f position, Vector3f direction, Color color, float intensity, float clamp, float cutoffAngle) {
		
		initLuaTable();
		
		if(position != null)
			this.position = position;
		else
			this.position = new Vector3f(0);
		
		if(direction != null)
			this.direction = direction;
		else
			this.position = new Vector3f(0, -1, 0);
		
		if(color != null)
			this.color = color;
		else
			this.color = Color.WHITE;
		
		this.intensity = intensity;
		this.clamp = clamp;
		setCutoffFromAngle(cutoffAngle);
	}
	
	public SpotLight(Vector3f position, float intensity, float cutoffAngle) {
		this(position, null, null, intensity, 1, cutoffAngle);
	}
	
	public void setCutoffFromAngle(float a) {
		cutoff = (float) Math.cos(Math.toRadians(a));
		System.out.println("SPOT: " + cutoff);
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
