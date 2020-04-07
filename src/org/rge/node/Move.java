package org.rge.node;

import org.joml.Matrix4f;
import org.luaj.vm2.LuaBoolean;
import org.luaj.vm2.LuaDouble;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.rge.lua.EngineObject;
import org.rge.lua.EngineReference;

public class Move implements EngineObject {
	
	EngineReference engineRef;
	private float time;
	private int index;
	
	public boolean loop;
	public float runLen;
	public float[] times;
	public LuaTable luaTimes;
	public Matrix4f[] keys;
	public LuaTable luaKeys;
	
	public Move() {
		luaTimes = new LuaTable();
		luaKeys = new LuaTable();
		index = 0;
		initLuaTable();
	}
	
	public Matrix4f getTransform() {
		if(times == null || keys == null)
			return null;
		
		int minLen = times.length < keys.length ? times.length : keys.length;
		if(minLen == 0)
			return null;
		if(index >= minLen)
			index = 0;
		
		int nextIndex = index + 1;
		if(index == 0 && times[index] > time)
			nextIndex = index;
		if(index == minLen-1 && times[index] < time)
			nextIndex = index;
		if(keys[index] == null || keys[nextIndex] == null)
			return null;
		
		float t = (time - times[index]) / (times[nextIndex] - times[index]);
		if(index == nextIndex)
			t = 1;
		Matrix4f res = new Matrix4f();
		keys[index].lerp(keys[nextIndex], t, res);
		return res;
	}
	
	public void advance(float dt) {
		if(time > runLen) {
			if(loop) {
				time += dt;
				time %= runLen;
				index = 0;
			} else
				return;
		} else {
			time += dt;
		}
		
		int minLen = times.length < keys.length ? times.length : keys.length;
		
		while(true) {
			if(index >= minLen-1)
				return;
			if(time >= times[index+1])
				index++;
			else
				return;
		}
		
	}
	
	public float getTime() {
		return time;
	}
	
	public void setTime(float time) {
		this.time = time;
	}
	
	private void initLuaTable() {
		engineRef = new EngineReference(this);
		
		engineRef.set("loop", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue val) {
				if(val instanceof LuaBoolean)
					loop = val.checkboolean();
				return LuaValue.valueOf(loop);
			}
		});
		engineRef.set("length", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue val) {
				if(val instanceof LuaDouble || val instanceof LuaInteger)
					runLen = val.tofloat();
				return LuaValue.valueOf(runLen);
			}
		});
		engineRef.set("time", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue val) {
				if(val instanceof LuaDouble || val instanceof LuaInteger)
					time = val.tofloat();
				return LuaValue.valueOf(time);
			}
		});
		engineRef.set("advance", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue val) {
				if(val instanceof LuaDouble || val instanceof LuaInteger)
					advance(val.tofloat());
				return LuaValue.valueOf(time);
			}
		});
		engineRef.set("index", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue val) {
				if(val instanceof LuaInteger)
					index = val.toint();
				return LuaValue.valueOf(index);
			}
		});
		engineRef.set("frames", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				if(times == null || keys == null)
					return LuaValue.valueOf(0);
				return LuaValue.valueOf(times.length < keys.length ? times.length : keys.length);
			}
		});
		engineRef.set("times", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return luaTimes;
			}
		});
		engineRef.set("keys", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return luaKeys;
			}
		});
		engineRef.set("applyFrames", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				
				times = new float[luaTimes.length()];
				keys  = new Matrix4f[luaKeys.length()];
				
				for(int i = 0; i < times.length; i++) {
					LuaValue atIndex = luaTimes.get(i+1);
					if(atIndex instanceof LuaDouble || atIndex instanceof LuaInteger)
						times[i] = atIndex.tofloat();
					else
						times[i] = 0;
				}
				
				for(int i = 0; i < keys.length; i++) {
					LuaValue atIndex = luaKeys.get(i+1);
					if(atIndex instanceof EngineReference) {
						EngineReference ref = (EngineReference) atIndex;
						if(ref.parent instanceof Matrix4f)
							keys[i] = (Matrix4f) ref.parent;
						else
							keys[i] = null;
					} else {
						keys[i] = null;
					}
				}
				
				return NIL;
			}
		});
	}
	
	@Override
	public EngineReference getEngineReference() {
		return engineRef;
	}
	
}
