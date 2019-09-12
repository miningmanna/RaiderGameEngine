package org.rge.window;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.nio.IntBuffer;

import org.luaj.vm2.LuaBoolean;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;
import org.rge.lua.EngineObject;
import org.rge.lua.EngineReference;

public class Window implements EngineObject {
	
	public static final int DEF_WIDTH = 600;
	public static final int DEF_HEIGHT = 480;
	
	EngineReference engReference;
	
	private long window;
	private int width;
	private int height;
	public Input input;
	
	public Window(String title, int width, int height) {
		
		initLuaTable();
		
		input = new Input(this);
		
		// ----------------- GLFW INIT --------------
		GLFWErrorCallback.createPrint(System.out).set();
		
		if(!glfwInit())
			throw new IllegalStateException("Couldnt init GLFW");
		
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
		
		this.width = width;
		this.height = height;
		
		window = glfwCreateWindow(width, height, title, NULL, NULL);
		if(window == NULL)
			throw new RuntimeException("Coulnt create window!");
		
		glfwSetKeyCallback(window, input.getKbHook());
		
		glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
		glfwSetCursorPosCallback(window, input.getMsHook());
		
		glfwSetMouseButtonCallback(window, input.getMsClckHook());
		
		try (MemoryStack stack = stackPush()) {
			IntBuffer pWidth = stack.mallocInt(1);
			IntBuffer pHeight = stack.mallocInt(1);
			
			glfwGetWindowSize(window, pWidth, pHeight);
			
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
			
			glfwSetWindowPos(window,
					(vidmode.width() - pWidth.get(0))/2,
					(vidmode.height() - pHeight.get(0))/2);
		}
		
		glfwMakeContextCurrent(window);
		glfwSwapInterval(1);
		
	}
	
	public Window(String title) {
		this(title, DEF_WIDTH, DEF_HEIGHT);
	}
	
	public void setSize(int width, int height) {
		if(width < 0 || height < 0)
			return;
		
		this.width = width;
		this.height = height;
		
		glfwSetWindowSize(window, width, height);
		glViewport(0, 0, width, height);
	}
	
	public void show() {
		glfwShowWindow(window);
	}
	
	public void hide() {
		glfwHideWindow(window);
	}
	
	public void destroy() {
		
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);
		
		glfwTerminate();
		glfwSetErrorCallback(null).free();
		
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public boolean shouldClose() {
		return glfwWindowShouldClose(window);
	}
	
	public void setShouldClose(boolean shouldClose) {
		glfwSetWindowShouldClose(window, shouldClose);
	}

	public void pollEvents() {
		input.update();
		glfwPollEvents();
	}

	public void swapBuffers() {
		glfwSwapBuffers(window);
	}

	public void center() {
		try (MemoryStack stack = stackPush()) {
			IntBuffer pWidth = stack.mallocInt(1);
			IntBuffer pHeight = stack.mallocInt(1);
			
			glfwGetWindowSize(window, pWidth, pHeight);
			
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
			
			glfwSetWindowPos(window,
					(vidmode.width() - pWidth.get(0))/2,
					(vidmode.height() - pHeight.get(0))/2);
		}
	}
	
	@Override
	public EngineReference getEngineReference() {
		return engReference;
	}
	
	private void initLuaTable() {
		engReference = new EngineReference(this);
		
		engReference.set("setSize", new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0, LuaValue arg1) {
				if(!(arg0 instanceof LuaInteger))
					return null;
				if(!(arg1 instanceof LuaInteger))
					return null;
				
				setSize(arg0.checkint(), arg1.checkint());
				
				return null;
			}
		});
		
		engReference.set("center", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				center();
				return null;
			}
		});
		
		engReference.set("show", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				show();
				return null;
			}
		});
		
		engReference.set("setShouldClose", new OneArgFunction() {
			
			@Override
			public LuaValue call(LuaValue arg0) {
				if(!(arg0 instanceof LuaBoolean))
					return null;
				setShouldClose(arg0.checkboolean());
				return null;
			}
		});
		
		engReference.set("getWidth", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaValue.valueOf(width);
			}
		});
		
		engReference.set("getHeight", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaValue.valueOf(height);
			}
		});
		
	}
	
}
