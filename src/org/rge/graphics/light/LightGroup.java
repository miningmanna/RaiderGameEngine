package org.rge.graphics.light;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.rge.lua.EngineObject;
import org.rge.lua.EngineReference;

public class LightGroup implements EngineObject {
	
	// TODO: add remove functions
	
	EngineReference engReference;
	
	public static final int POINTLIGHT_COUNT			= 4;
	public static final int DIRECTIONALLIGHT_COUNT	= 4;
	public static final int SPOTLIGHT_COUNT			= 4;
	
	public PointLight[]			pointLights;
	public DirectionalLight[]	directionalLights;
	public SpotLight[]			spotLights;
	public AmbientLight ambientLight;
	
	public LightGroup() {
		
		initLuaTable();
		
		this.pointLights		= new PointLight[POINTLIGHT_COUNT];
		this.directionalLights	= new DirectionalLight[DIRECTIONALLIGHT_COUNT];
		this.spotLights			= new SpotLight[SPOTLIGHT_COUNT];
	}
	
	public int addLight(PointLight light) {
		return putObjectIntoArray(light, pointLights);
	}
	
	public int addLight(DirectionalLight light) {
		return putObjectIntoArray(light, directionalLights);
	}
	
	public int addLight(SpotLight light) {
		return putObjectIntoArray(light, spotLights);
	}
	
	public void setAmbient(AmbientLight light) {
		this.ambientLight = light;
	}
	
	private <T> int putObjectIntoArray(T o, T[] a) {
		for(int i = 0; i < a.length; i++) {
			if(a[i] == null) {
				a[i] = o;
				return i;
			}
		}
		return -1;
	}
	
	@Override
	public EngineReference getEngineReference() {
		return engReference;
	}
	
	private void initLuaTable() {
		engReference = new EngineReference(this);
		
		engReference.set("addLight", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				if(!(arg0 instanceof EngineReference))
					return null;
				EngineReference ref = (EngineReference) arg0;
				if(ref.parent instanceof AmbientLight) {
					setAmbient((AmbientLight) ref.parent);
				} else if(ref.parent instanceof DirectionalLight) {
					addLight((DirectionalLight) ref.parent);
				} else if(ref.parent instanceof PointLight) {
					addLight((PointLight) ref.parent);
				} else if(ref.parent instanceof SpotLight) {
					addLight((SpotLight) ref.parent);
				}
				return null;
			}
		});
		
	}
	
}
