package org.rge.graphics;

import org.rge.lua.EngineObject;

public interface Renderable extends EngineObject {
	
	public int getVAO();
	public Shader getShader();
	public int[] getUsedVBOs();
	public void advance(float dt);
	public Surface[] getSurfs();
	
}
