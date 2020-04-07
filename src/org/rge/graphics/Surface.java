package org.rge.graphics;

import org.joml.Vector3f;
import org.rge.assets.models.Texture;

public class Surface {
	
	public Renderable par;
	public int indOffset;
	public int indLength;
	public Texture tex;
	public boolean isTranslucent;
	public boolean additiveColor;
	public boolean doubleSided;
	public Vector3f midPoint;
	
}