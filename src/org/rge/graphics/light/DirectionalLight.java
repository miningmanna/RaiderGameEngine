package org.rge.graphics.light;

import java.awt.Color;

import org.joml.Vector3f;

public class DirectionalLight {
	
	public Vector3f dir;
	public Color color;
	public float intensity;
	public float clamp;
	
	public DirectionalLight(Vector3f dir, Color color, float intensity, float clamp) {
		if(dir != null)
			this.dir = new Vector3f(dir);
		else
			this.dir = new Vector3f();
		
		if(color != null)
			this.color = color;
		else
			this.color = Color.WHITE;
		
		this.intensity = intensity;
		this.clamp = clamp;
	}
	
	public DirectionalLight(Vector3f dir, float intensity) {
		this(dir, null, intensity, 100);
	}
	
}
