package org.rge.graphics;

import org.joml.Vector3f;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;
import org.rge.assets.models.Texture;
import org.rge.lua.EngineObject;
import org.rge.lua.EngineReference;

public class Surface implements EngineObject {
	
	private EngineReference ref;
	
	public Surface() {
		initLuaTable();
	}
	
	public Renderable par;
	public int indOffset;
	public int indLength;
	public Texture tex;
	public boolean isTranslucent;
	public boolean additiveColor;
	public boolean doubleSided;
	public Vector3f midPoint;
	
	private void initLuaTable() {
		ref = new EngineReference(this);
		
		// TODO: Change all other classes to use this way
		// To comply with objects in lua (using obj:function() no obj.function() )
		ref.set("texture", TEXTURE_FUNCTION);
	}
	
	@Override
	public EngineReference getEngineReference() {
		return ref;
	}
	
	private static final Surface GET_INSTANCE_FROM_VARARGS(Varargs varargs) {
		
		if(varargs.narg() < 1)
			return null;
		
		LuaValue val = varargs.arg(1);
		if(!(val instanceof EngineReference))
			return null;
		EngineReference ref = (EngineReference) val;
		if(!(ref.parent instanceof Surface))
			return null;
		
		return (Surface) ref.parent;
	}
	
	private static final VarArgFunction TEXTURE_FUNCTION = new VarArgFunction() {
		public Varargs invoke(Varargs varargs) {
			if(varargs.narg() < 1 || varargs.narg() > 2)
				return NIL;
			
			Surface ins = GET_INSTANCE_FROM_VARARGS(varargs);
			if(ins == null)
				return NIL;
			
			System.out.println("FUCK YEAH");
			
			if(varargs.narg() == 2) {
				System.out.println("????????????????");
				LuaValue v = varargs.arg(2);
				if(v == NIL) {
					ins.tex = null;
					return NIL;
				}
				System.out.println(v.getClass());
				if(v instanceof EngineReference) {
					EngineReference ref = (EngineReference) v;
					System.out.println(ref.parent.getClass());
					if(ref.parent instanceof Texture) {
						ins.tex = (Texture) ref.parent;
						System.out.println("TEXTURE SET!");
					}
				}
			}
				
			
			if(ins.tex != null)
				return ins.tex.getEngineReference();
			return NIL;
		};
	};
	
}