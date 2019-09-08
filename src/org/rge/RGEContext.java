package org.rge;

import static org.lwjgl.opengl.GL11.*;

import java.awt.Color;
import java.io.IOException;

import org.rge.graphics.Camera;
import org.rge.graphics.Renderer;
import org.rge.graphics.light.AmbientLight;
import org.rge.graphics.light.DirectionalLight;
import org.rge.graphics.light.LightGroup;
import org.rge.graphics.light.PointLight;
import org.rge.graphics.light.SpotLight;
import org.rge.lua.LuaEngine;
import org.rge.lua.EngineObject;
import org.rge.lua.EngineReference;
import org.rge.node.DrawNode;
import org.joml.Vector3f;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.lwjgl.opengl.GL;
import org.rge.assets.AssetManager;
import org.rge.assets.models.Model;
import org.rge.assets.models.Model.RawData;
import org.rge.window.Window;

public class RGEContext implements EngineObject {
	
	EngineReference engReference;
	
	Window window;
	AssetManager am;
	LuaEngine luaEngine;
	Renderer renderer;
	
	Color clearColor;
	
	String initScriptPath, tickSciptPath;
	LuaValue tickScript;
	
	public RGEContext() {
		
		clearColor = Color.BLACK;
		
		am = new AssetManager();
		
	}
	
	public void setInitScript(String path) {
		initScriptPath = path;
	}
	
	public void setTickScript(String path) {
		if(window != null)
			tickScript = luaEngine.loadScript(path);
		else
			tickSciptPath = path;
	}
	
	private static void printFullStack(Throwable t) {
		for(StackTraceElement elem : t.getStackTrace())
			System.err.println("at " + elem.getClassName() + "." + elem.getMethodName() + "(" + elem.getFileName() + ":" + elem.getLineNumber() + ")");
		if(t.getCause() != null) {
			Throwable cause = t.getCause();
			System.err.println("Casued by: " + cause + " " + cause.getMessage() + " FULL:");
			printFullStack(cause);
		}
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
		
		initLuaTable();
		
		luaEngine = new LuaEngine(this);
		
		try {
			luaEngine.exec(luaEngine.loadScript(initScriptPath));
		} catch(Exception e) {
			e.printStackTrace();
		}
		tickScript = luaEngine.loadScript(tickSciptPath);
		
	}
	
	public void setSize(int width, int height) {
		if(width < 0 || height < 0)
			return;
		window.setSize(width, height);
	}
	
	public void setClearColor(Color c) {
		
		if(c != null)
			clearColor = c;
		
	}
	
	public void tick() {
		
		window.pollEvents();
		
		luaEngine.exec(tickScript);
		
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
	
	public void renderAll() {
		renderer.renderAll();
	}
	
	public void useCamera(Camera c) {
		renderer.setCameraMatrix(c.combined);
	}
	
	public void useLights(LightGroup lights) {
		renderer.setLightGroup(lights);
	}
	
	@Override
	public EngineReference getEngineReference() {
		return engReference;
	}
	
	private void initLuaTable() {
		engReference = new EngineReference(this);
		
		engReference.set("window", window.getEngineReference());
		engReference.set("assets", am.getEngineReference());
		engReference.set("input", window.input.getEngineReference());
		
		engReference.set("createDrawNode", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return new DrawNode().getEngineReference();
			}
		});
		
		engReference.set("createCamera", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return new Camera().getEngineReference();
			}
		});
		
		engReference.set("createLightGroup", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return new LightGroup().getEngineReference();
			}
		});
		
		engReference.set("createAmbientLight", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return new AmbientLight(0).getEngineReference();
			}
		});
		
		engReference.set("createPointLight", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return new PointLight(new Vector3f(), 0).getEngineReference();
			}
		});
		
		engReference.set("createDirectionalLight", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return new DirectionalLight(new Vector3f(0, -1, 0), 0).getEngineReference();
			}
		});
		
		engReference.set("createSpotLight", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return new SpotLight(new Vector3f(), 0, 20).getEngineReference();
			}
		});
		
		engReference.set("setClearColor", new ThreeArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0, LuaValue arg1, LuaValue arg2) {
				if(!(arg0 instanceof LuaInteger))
					return NIL;
				if(!(arg1 instanceof LuaInteger))
					return NIL;
				if(!(arg2 instanceof LuaInteger))
					return NIL;
				
				setClearColor(new Color(arg0.checkint(), arg1.checkint(), arg2.checkint()));
				
				return NIL;
			}
		});
		
		engReference.set("getModel", new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0, LuaValue arg1) {
				if(!(arg0 instanceof LuaString))
					return NIL;
				if(!(arg1 instanceof LuaString))
					return NIL;
				
				RawData raw = am.getModelRawData(arg0.checkjstring(), arg1.checkjstring());
				if(raw == null)
					return NIL;
				Model m = null;
				try {
					m = new Model(am, raw, false);
				} catch (IOException e) {
					return NIL;
				}
				
				return m.getEngineReference();
			}
		});
		
		engReference.set("queueRender", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				if(!(arg0 instanceof EngineReference))
					return NIL;
				EngineReference ref = (EngineReference) arg0;
				if(!(ref.parent instanceof DrawNode))
					return NIL;
				
				queueRender((DrawNode) ref.parent);
				
				return NIL;
			}
		});
		
		engReference.set("useCamera", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				if(!(arg0 instanceof EngineReference))
					return NIL;
				EngineReference ref = (EngineReference) arg0;
				if(!(ref.parent instanceof Camera))
					return NIL;
				useCamera((Camera) ref.parent);
				return NIL;
			}
		});
		
		engReference.set("useLightGroup", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				if(!(arg0 instanceof EngineReference))
					return NIL;
				EngineReference ref = (EngineReference) arg0;
				if(!(ref.parent instanceof LightGroup))
					return NIL;
				useLights((LightGroup) ref.parent);
				return NIL;
			}
		});
		
	}
	
}
