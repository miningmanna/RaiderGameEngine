package org.rge.graphics.light;

import java.awt.Color;

import org.joml.Vector3f;

public class SpotLight {
	
	public Vector3f position;
	public Vector3f direction;
	public Color color;
	public float intensity;
	public float clamp;
	public float cutoff;
	
	public SpotLight(Vector3f position, Vector3f direction, Color color, float intensity, float clamp, float cutoffAngle) {
		
		if(position != null)
			this.position = position;
		else
			this.position = new Vector3f(0);
		
		if(direction != null)
			this.direction = direction;
		else
			this.position = new Vector3f(0, -1, 0);
		
		if(color != null)
			this.color = color;
		else
			this.color = Color.WHITE;
		
		this.intensity = intensity;
		this.clamp = clamp;
		setCutoffFromAngle(cutoffAngle);
	}
	
	public SpotLight(Vector3f position, float intensity, float cutoffAngle) {
		this(position, null, null, intensity, 1, cutoffAngle);
	}
	
	public void setCutoffFromAngle(float a) {
		cutoff = (float) Math.cos(Math.toRadians(a));
		System.out.println("SPOT: " + cutoff);
	}
	
}
