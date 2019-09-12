package org.rge.node;

import org.joml.Matrix4f;

public class Move {
	
	private float time;
	
	public boolean loop;
	public float[] times;
	public float runLen;
	public Matrix4f[] keys;
	
	public Matrix4f getTransform() {
		return null; // TODO: implement Move class
	}
	
	public void advance(float dt) {
		this.setTime(this.getTime() + dt);
	}
	
	public float getTime() {
		return time;
	}
	
	public void setTime(float time) {
		this.time = time;
	}
	
}
