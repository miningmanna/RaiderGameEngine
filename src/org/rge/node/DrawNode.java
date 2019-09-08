package org.rge.node;

import java.util.ArrayList;

import org.joml.Matrix4f;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.rge.assets.models.Model;
import org.rge.lua.EngineObject;
import org.rge.lua.EngineReference;
import org.rge.node.collision.Collider;
import org.rge.sound.Source;

public class DrawNode implements EngineObject {
	
	private static final Matrix4f identityMatrix = new Matrix4f().identity();
	
	EngineReference engReference;
	
	public ArrayList<DrawNode> subNodes;
	public Model model;
	public Move move;
	public Collider collider;
	public Source source;
	
	public DrawNode() {
		subNodes = new ArrayList<>();
		initLuaTable();
	}
	
	public Matrix4f getTransform() {
		if(move != null)
			return move.getTransform();
		else
			return identityMatrix;
	}
	
	@Override
	public EngineReference getEngineReference() {
		return engReference;
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
		
		engReference.set("setModel", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				if(!(arg instanceof EngineReference))
					return NIL;
				EngineReference ref = (EngineReference) arg;
				if(!(ref.parent instanceof Model))
					return NIL;
				
				model = (Model) ref.parent;
				
				return NIL;
			}
		});
		
		// TODO: setMove, setLights, setCollider, etc..
		
	}
	
}
