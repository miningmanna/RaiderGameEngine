package org.rge.graphics.light;

import java.awt.Color;

public class AmbientLight {
	
	public Color color;
	public float intensity;
	
	public AmbientLight(Color color, float intensity) {
		if(color != null)
			this.color = color;
		else
			this.color = Color.WHITE;
		
		this.intensity = intensity;
	}
	
	public AmbientLight(float intensity) {
		this(null, intensity);
	}
	
}
