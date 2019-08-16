package org.rge.graphics.light;

import java.awt.Color;

import org.joml.Vector3f;

public class PointLight {
	
	public Color color;
	public Vector3f pos;
	public float intensity;
	public float clamp;
	
	public PointLight(Vector3f pos, Color color, float intensity, float clamp) {
		if(pos != null)
			this.pos = new Vector3f(pos);
		else
			this.pos = new Vector3f();
		
		if(color != null)
			this.color = color;
		else
			this.color = Color.WHITE;
		
		this.intensity = intensity;
		this.clamp = clamp;
	}
	
	public PointLight(Vector3f dir, float intensity) {
		this(dir, null, intensity, 100);
	}
	
}
