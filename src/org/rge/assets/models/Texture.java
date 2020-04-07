package org.rge.assets.models;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.rge.lua.EngineObject;
import org.rge.lua.EngineReference;

public class Texture implements EngineObject {
	
	private EngineReference ref;
	public int[] texIds;
	public float[] times;
	public boolean animated;
	public float runlen, time;
	public int index = 0;
	
	public Texture() {
		initLuaTable();
	}
	
	public void advance(float dt) {
		
		if(!animated)
			return;
		if(time > runlen) {
			time += dt;
			time %= runlen;
			index = 0;
		} else {
			time += dt;
		}
		
		int minLen = times.length < texIds.length ? times.length : texIds.length;
		
		while(true) {
			if(index >= minLen-1)
				return;
			if(time >= times[index+1])
				index++;
			else
				return;
		}
	}
	
	public int getCurTexId() {
		return texIds[index];
	}
	
	private void initLuaTable() {
		
		ref = new EngineReference(this);
		ref.set("reset", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				time = 0;
				return NIL;
			}
		});
		
	}
	
	@Override
	public EngineReference getEngineReference() {
		return ref;
	}
	
	public static class TextureRawInfo {
		
		public String[] files;
		public float[] times;
		public boolean animated;
		public float runlen;
		
	}
	
}
