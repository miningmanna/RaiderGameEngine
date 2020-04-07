package org.rge.node;

import java.util.ArrayList;

import org.joml.Matrix4f;
import org.luaj.vm2.LuaDouble;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.rge.graphics.Renderable;
import org.rge.lua.EngineObject;
import org.rge.lua.EngineReference;
import org.rge.node.collision.Collider;
import org.rge.sound.Source;

public class DrawNode implements EngineObject {
	
	private static final Matrix4f identityMatrix = new Matrix4f().identity();
	
	EngineReference engReference;
	
	public String name;
	public ArrayList<DrawNode> subNodes;
	public Renderable model;
	public Move move;
	public Collider collider;
	public Source source;
	
	public DrawNode() {
		name = "";
		subNodes = new ArrayList<>();
		initLuaTable();
	}
	
	public Matrix4f getTransform() {
		if(move != null)
			return move.getTransform();
		else
			return identityMatrix;
	}
	
	public void advance(float dt) {
		if(move != null)
			move.advance(dt);
		if(model != null)
			model.advance(dt);
		for(int i = 0; i < subNodes.size(); i++)
			subNodes.get(i).advance(dt);
	}
	
	@Override
	public EngineReference getEngineReference() {
		return engReference;
	}
	
	public void luaSetMoves(LuaTable moves) {
		LuaValue[] keys = moves.keys();
		for(LuaValue key : keys) {
			if(!(key instanceof LuaString))
				continue;
			String name = key.checkjstring();
			if(name.equals("move")) {
				LuaValue atKey = moves.get(key);
				if(!(atKey instanceof EngineReference))
					continue;
				EngineReference ref = (EngineReference) atKey;
				if(!(ref.parent instanceof Move))
					continue;
				move = (Move) ref.parent;
			} else {
				System.out.println("SUBNODE?: " + key);
				for(int i = 0; i < subNodes.size(); i++) {
					System.out.println(subNodes.get(i).name);
					System.out.println(subNodes.get(i).name.equals(name));
					if(subNodes.get(i).name.equals(name)) {
						System.out.println("FOUND IT!");
						LuaValue atKey = moves.get(key);
						if(atKey instanceof LuaTable)
							subNodes.get(i).luaSetMoves((LuaTable) atKey);
						break;
					}
				}
			}
		}
	}
	
	private void initLuaTable() {
		engReference = new EngineReference(this);
		
		engReference.set("addSubNode", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				if(!(arg instanceof EngineReference))
					return NIL;
				EngineReference ref = (EngineReference) arg;
				if(!(ref.parent instanceof DrawNode))
					return NIL;
				
				subNodes.add((DrawNode) ref.parent);
				
				return NIL;
			}
		});
		
		engReference.set("model", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				if(!(arg instanceof EngineReference))
					if(model == null)
						return NIL;
					else
						return model.getEngineReference();
				EngineReference ref = (EngineReference) arg;
				if(!(ref.parent instanceof Renderable))
					if(model == null)
						return NIL;
					else
						return model.getEngineReference();
				
				model = (Renderable) ref.parent;
				
				if(model == null)
					return NIL;
				else
					return model.getEngineReference();
			}
		});
		
		engReference.set("name", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue val) {
				if(val instanceof LuaString)
					DrawNode.this.name = val.checkjstring();
				return LuaValue.valueOf(DrawNode.this.name);
			}
		});
		
		engReference.set("move", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				if(!(arg instanceof EngineReference))
					if(move == null)
						return NIL;
					else
						return move.getEngineReference();
				EngineReference ref = (EngineReference) arg;
				if(!(ref.parent instanceof Move))
					if(move == null)
						return NIL;
					else
						return move.getEngineReference();
				
				move = (Move) ref.parent;
				
				if(move == null)
					return NIL;
				else
					return move.getEngineReference();
			}
		});
		
		engReference.set("setMoves", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				if(arg0 instanceof LuaTable)
					luaSetMoves((LuaTable) arg0);
				return NIL;
			}
		});
		
		engReference.set("advance", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue val) {
				if(val instanceof LuaDouble || val instanceof LuaInteger)
					advance(val.tofloat());
				return NIL;
			}
		});
		// TODO: setCollider, etc..
		
	}
	
}
