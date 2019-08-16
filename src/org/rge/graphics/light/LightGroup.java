package org.rge.graphics.light;

public class LightGroup {
	
	public static final int POINTLIGHT_COUNT = 4;
	public static final int DIRECTIONALLIGHT_COUNT = 4;
	
	public PointLight[] pointLights;
	public DirectionalLight[] directionalLights;
	public AmbientLight ambientLight;
	
	public LightGroup() {
		this.pointLights = new PointLight[POINTLIGHT_COUNT];
		this.directionalLights = new DirectionalLight[DIRECTIONALLIGHT_COUNT];
	}
	
	public int addLight(PointLight light) {
		return putObjectIntoArray(light, pointLights);
	}
	
	public int addLight(DirectionalLight light) {
		return putObjectIntoArray(light, directionalLights);
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
	
}
