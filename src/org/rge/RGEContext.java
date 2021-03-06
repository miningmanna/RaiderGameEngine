package org.rge;

import static org.lwjgl.opengl.GL11.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;

import org.rge.graphics.Camera;
import org.rge.graphics.ShaderArgs;
import org.rge.graphics.Renderer;
import org.rge.graphics.light.AmbientLight;
import org.rge.graphics.light.DirectionalLight;
import org.rge.graphics.light.LightGroup;
import org.rge.graphics.light.PointLight;
import org.rge.graphics.light.SpotLight;
import org.rge.lua.LuaEngine;
import org.rge.lua.compat.LuaUtils;
import org.rge.lua.compat.Matrix4;
import org.rge.lua.compat.Vector3;
import org.rge.lua.EngineObject;
import org.rge.lua.EngineReference;
import org.rge.node.DrawNode;
import org.rge.node.Move;
import org.rge.sound.SoundSystem;
import org.rge.sound.Source;
import org.joml.Vector3f;
import org.luaj.vm2.LuaDouble;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.lwjgl.opengl.GL;
import org.rge.EventManager.EventHandler;
import org.rge.assets.AssetManager;
import org.rge.assets.audio.SoundClip.RawSoundClip;
import org.rge.assets.config.Config;
import org.rge.assets.models.Model;
import org.rge.assets.models.Model.RawData;
import org.rge.assets.models.tilemap.TileMap;
import org.rge.assets.models.tilemap.TileMap.RawTileMap;
import org.rge.assets.models.tilemap.TileMapTexture;
import org.rge.assets.models.tilemap.TileMapTexture.RawTileMapTexture;
import org.rge.assets.models.tilemap.TileType;
import org.rge.window.Window;

public class RGEContext implements EngineObject {
	
	EngineReference engReference;
	
	Window window;
	AssetManager am;
	EventManager em;
	LuaEngine luaEngine;
	Renderer renderer;
	SoundSystem soundSystem;
	
	Color clearColor;
	
	String initScriptPath;
	
	EventHandler tickHandler;
	
	public void setInitScript(String path) {
		initScriptPath = path;
	}
	
	public void init() {
		
		
		em = new EventManager();
		tickHandler = em.register("update");
		
		clearColor = Color.BLACK;
		
		am = new AssetManager();
		window = new Window("RGE engine");
		
		GL.createCapabilities();
		glEnable(GL_DEPTH_TEST);
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
		glEnable(GL_BLEND);
		glEnable(GL_TEXTURE_2D);
		
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		renderer = new Renderer();
		soundSystem = new SoundSystem();
		
		initLuaTable();
		
		luaEngine = new LuaEngine(this, am);
		
		try {
			luaEngine.exec(luaEngine.loadScript(initScriptPath));
		} catch(Exception e) {
			e.printStackTrace();
		}
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
	
	private long lastTime = -1;
	public void update() {
		
		window.pollEvents();
		
		if(lastTime == -1)
			lastTime = System.nanoTime();
		long currentTime = System.nanoTime();
		double dt = (currentTime - lastTime) / 1000000000.0;
		lastTime = currentTime;
		
		tickHandler.fire(LuaValue.valueOf(dt));
		
		soundSystem.update();
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
		soundSystem.destroy();
		
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
	
	private void useShaderArgs(ShaderArgs args) {
		renderer.setShaderArgs(args);
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
		
		engReference.set("newVector3", new ThreeArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0, LuaValue arg1, LuaValue arg2) {
				if(!(arg0 instanceof LuaInteger || arg0 instanceof LuaDouble))
					return new Vector3().getEngineReference();
				if(!(arg1 instanceof LuaInteger || arg1 instanceof LuaDouble))
					return new Vector3().getEngineReference();
				if(!(arg2 instanceof LuaInteger || arg2 instanceof LuaDouble))
					return new Vector3().getEngineReference();
				return new Vector3((float) arg0.checkdouble(), (float) arg1.checkdouble(), (float) arg2.checkdouble()).getEngineReference();
			}
		});
		
		engReference.set("newMatrix4", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return new Matrix4().getEngineReference();
			}
		});
		
		engReference.set("newDrawNode", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return new DrawNode().getEngineReference();
			}
		});
		
		engReference.set("newMove", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return new Move().getEngineReference();
			}
		});
		
		engReference.set("newCamera", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return new Camera().getEngineReference();
			}
		});
		
		engReference.set("newLightGroup", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return new LightGroup().getEngineReference();
			}
		});
		
		engReference.set("newAmbientLight", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return new AmbientLight(0).getEngineReference();
			}
		});
		
		engReference.set("newPointLight", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return new PointLight(new Vector3f(), 0).getEngineReference();
			}
		});
		
		engReference.set("newDirectionalLight", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return new DirectionalLight(new Vector3f(0, -1, 0), 0).getEngineReference();
			}
		});
		
		engReference.set("newSpotLight", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return new SpotLight(new Vector3f(), 0, 20).getEngineReference();
			}
		});
		engReference.set("newTileType", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return new TileType().getEngineReference();
			}
		});
		engReference.set("newRawTileMapTexture", new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0, LuaValue arg1) {
				if(arg0 instanceof LuaInteger && arg1 instanceof LuaInteger) {
					
					int w = arg0.checkint();
					int h = arg1.checkint();
					if(w < 0 || h < 0)
						return NIL;
					
					return new RawTileMapTexture(w, h).getEngineReference();
				}
				return NIL;
			}
		});
		engReference.set("newTileMapTexture", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				if(arg0 instanceof EngineReference) {
					EngineReference ref = (EngineReference) arg0;
					if(ref.parent instanceof RawTileMapTexture) {
						return new TileMapTexture(am, (RawTileMapTexture) ref.parent).getEngineReference();
					}
					return NIL;
				}
				return NIL;
			}
		});
		engReference.set("newRawTileMap", new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0, LuaValue arg1) {
				if(arg0 instanceof LuaInteger && arg1 instanceof LuaInteger) {
					
					int w = arg0.checkint();
					int h = arg1.checkint();
					if(w < 0 || h < 0)
						return NIL;
					
					return new RawTileMap(w, h).getEngineReference();
				}
				return NIL;
			}
		});
		engReference.set("newTileMap", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				if(arg0 instanceof EngineReference) {
					EngineReference ref = (EngineReference) arg0;
					if(ref.parent instanceof RawTileMap) {
						return new TileMap(am, (RawTileMap) ref.parent).getEngineReference();
					}
					return NIL;
				}
				return NIL;
			}
		});
		engReference.set("newSoundSource", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return new Source(soundSystem).getEngineReference();
			}
		});
		engReference.set("newShaderArgs", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return new ShaderArgs().getEngineReference();
			}
		});
		
		engReference.set("clearColor", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				if(args.narg() != 3)
					return LuaUtils.fromColor(clearColor);
				for(int i = 0; i < 3; i++)
					if(!(args.arg(1+i) instanceof LuaInteger))
						return LuaUtils.fromColor(clearColor);
				
				setClearColor(new Color(args.arg(1).checkint(), args.arg(2).checkint(), args.arg(3).checkint()));
				
				return LuaUtils.fromColor(clearColor);
			}
		});
		
		engReference.set("assetSource", new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0, LuaValue arg1) {
				if(!(arg0 instanceof LuaString))
					return null;
				if(!(arg1 instanceof LuaString))
					return null;
				
				am.registerInputGen(arg0.checkjstring(), arg1.checkjstring());
				
				return null;
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
		
		engReference.set("getConfig", new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0, LuaValue arg1) {
				if(!(arg0 instanceof LuaString))
					return NIL;
				if(!(arg1 instanceof LuaString))
					return NIL;
				
				Config conf = am.getConfig(arg0.checkjstring(), arg1.checkjstring());
				if(conf == null)
					return NIL;
				
				return conf.getEngineReference();
			}
		});
		
		engReference.set("get", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				if(!(arg0 instanceof LuaString))
					return NIL;
				String path = arg0.checkjstring();
				
				Object res = am.getValueOfSub(path, LuaValue.class, EngineObject.class, RawData.class, RawSoundClip.class, AudioInputStream.class, BufferedImage.class);
				System.out.println("1234: " + res);
				if(res == null)
					return NIL;
				LuaValue val = null;
				if(res instanceof EngineObject)
					val = ((EngineObject) res).getEngineReference();
				else if(res instanceof RawData)
					try {
						val = new Model(am, (RawData) res, false).getEngineReference();
					} catch (IOException e) { val = NIL; }
				else if(res instanceof RawSoundClip)
					return am.loadSoundClip((RawSoundClip) res).getEngineReference();
				else if(res instanceof AudioInputStream)
					return am.loadSoundStream((AudioInputStream) res).getEngineReference();
				else if(res instanceof BufferedImage)
					return am.loadTexture((BufferedImage) res).getEngineReference();
				else
					val = (LuaValue) res;
				return val;
			}
		});
		
		engReference.set("render", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				if(!(arg0 instanceof EngineReference))
					return engReference;
				EngineReference ref = (EngineReference) arg0;
				if(!(ref.parent instanceof DrawNode))
					return engReference;
				
				queueRender((DrawNode) ref.parent);
				
				return engReference;
			}
		});
		
		engReference.set("use", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				if(arg0 == NIL)
				{
					useCamera(null);
					useLights(null);
					return engReference;
				}
					
				if(!(arg0 instanceof EngineReference))
					return engReference;
				EngineReference ref = (EngineReference) arg0;
				if(ref.parent instanceof Camera)
					useCamera((Camera) ref.parent);
				else if(ref.parent instanceof LightGroup)
					useLights((LightGroup) ref.parent);
				else if(ref.parent instanceof ShaderArgs)
					useShaderArgs((ShaderArgs) ref.parent);
				
				return engReference;
			}
			
		});
		
		engReference.set("registerEvent", new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0, LuaValue arg1) {
				if(!(arg0 instanceof LuaString))
					return engReference;
				
				if(!(arg1 instanceof LuaFunction))
					return engReference;
				
				String eventName = arg0.tojstring();
				em.setHandlerCallback(eventName, arg1);
				
				return engReference;
			}
		});
		
	}
	
}
