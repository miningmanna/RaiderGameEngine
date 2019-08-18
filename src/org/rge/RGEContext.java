package org.rge;

import static org.lwjgl.opengl.GL11.*;

import java.awt.Color;

import org.rge.graphics.Camera;
import org.rge.graphics.Renderer;
import org.rge.graphics.light.LightGroup;
import org.rge.lua.LuaEngine;
import org.rge.lua.EngineReference;
import org.rge.node.DrawNode;
import org.luaj.vm2.LuaValue;
import org.lwjgl.opengl.GL;
import org.rge.assets.AssetManager;
import org.rge.assets.models.Model;
import org.rge.window.Window;

public class RGEContext {
	
	Window window;
	AssetManager am;
	EngineReference luaInterface;
	LuaEngine luaEngine;
	Renderer renderer;
	
	Color clearColor;
	
	LuaValue initScript;
	LuaValue tickScript;
	
	public RGEContext() {
		
		clearColor = Color.BLACK;
		
		am = new AssetManager();
		luaEngine = new LuaEngine(this);
		
	}
	
	public void init() {
		
		window = new Window("RGE engine");
		
		GL.createCapabilities();
		glEnable(GL_DEPTH_TEST);
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
		glEnable(GL_BLEND);
		glEnable(GL_TEXTURE_2D);
		
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		renderer = new Renderer();
		
	}
	
	public void setSize(int width, int height) {
		if(width < 0 || height < 0)
			return;
		window.setSize(width, height);
		
		glViewport(0, 0, width, height);
	}
	
	public void setClearColor(Color c) {
		
		if(c != null)
			clearColor = c;
		
	}
	
	public void tick() {
		
		window.pollEvents();
		
		//luaEngine.run(tickScript);
		
	}
	
	public void render() {
		
		glClearColor(clearColor.getRed()/256.0f, clearColor.getGreen()/256.0f, clearColor.getBlue()/256.0f, 0);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		renderer.renderAll();
		
		window.swapBuffers();
		
	}
	
	public boolean shouldClose() {
		return window.shouldClose();
	}
	
	public void setShouldClose(boolean shouldClose) {
		window.setShouldClose(shouldClose);
	}
	
	public void destroy() {
		
		// TODO: clean up everything
		am.destroy();
		
		window.destroy();
		
	}
	
	public void queueRender(DrawNode node) {
		renderer.queue(node);
	}
	
	public void useCamera(Camera c) {
		renderer.setCameraMatrix(c.combined);
	}
	
	public void useLights(LightGroup lights) {
		renderer.setLightGroup(lights);
	}
	
}
