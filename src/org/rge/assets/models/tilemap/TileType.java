package org.rge.assets.models.tilemap;

import java.util.ArrayList;

import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.rge.lua.EngineObject;
import org.rge.lua.EngineReference;
import org.rge.lua.compat.Vector3;

public class TileType implements EngineObject {	
	
	EngineReference ref;
	
	public int id;
	public TileTexPosRot[] texs;
	public ArrayList<Integer> connects;
	
	public TileType() {
		id = 0;
		texs = new TileTexPosRot[256];
		for(int i = 0; i < texs.length; i++)
			texs[i] = new TileTexPosRot();
		connects = new ArrayList<>();
		initLua();
	}
	
	public int findConnect(int s) {
		for(int i = 0; i < connects.size(); i++)
			if(connects.get(i) == s)
				return i;
		return -1;
	}
	
	public void initLua() {
		ref = new EngineReference(this);
		ref.set("id", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				if(arg0 instanceof LuaInteger)
					id = arg0.checkint();
				
				return LuaValue.valueOf(id);
			}
		});
		
		ref.set("connects", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				
				if(arg0 instanceof LuaTable) {
					LuaTable t = (LuaTable) arg0;
					
					connects.clear();
					
					for(int i = 0; i < t.length(); i++) {
						LuaValue v = t.get(i+1);
						if(v instanceof LuaInteger)
							connects.add(v.checkint());
					}
				}
				
				LuaTable ret = new LuaTable();
				for(int i = 0; i < connects.size(); i++)
					ret.set(i+1, LuaValue.valueOf(connects.get(i)));
				return ret;
			}
		});
		
		ref.set("addConnect", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				
				if(arg0 instanceof LuaInteger) {
					int i = arg0.checkint();
					if(findConnect(i) >= 0)
						return LuaValue.FALSE;
					connects.add(i);
					return LuaValue.TRUE;
				}
				
				return LuaValue.FALSE;
			}
		});
		
		ref.set("removeConnect", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				
				if(arg0 instanceof LuaInteger) {
					int index = findConnect(arg0.checkint());
					if(index < 0)
						return LuaValue.FALSE;
					connects.remove(index);
					return LuaValue.TRUE;
				}
				return LuaValue.FALSE;
			}
		});
		
		ref.set("doesConnect", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				if(arg0 instanceof LuaInteger)
					if(findConnect(arg0.checkint()) >= 0)
						return LuaValue.TRUE;
				return LuaValue.FALSE;
			}
		});
		
		ref.set("state", new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0, LuaValue arg1) {
				
				if(arg0 instanceof LuaInteger) {
					int i = arg0.checkint();
					if(i < 0 || i >= 256)
						return NIL;
					
					if(!(arg1 instanceof EngineReference))
						return new Vector3(texs[i].x, texs[i].y, texs[i].rot*90).getEngineReference();
					
					
					EngineReference ref = (EngineReference) arg1;
					Vector3 v;
					if(!(ref.parent instanceof Vector3))
						return new Vector3(texs[i].x, texs[i].y, texs[i].rot*90).getEngineReference();
					v = (Vector3) ref.parent;
					TileTexPosRot r = texs[i];
					r.x = (int) v.x;
					r.y = (int) v.y;
					r.rot = (int) (v.z / 90);
					return new Vector3(texs[i].x, texs[i].y, texs[i].rot*90).getEngineReference();
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
