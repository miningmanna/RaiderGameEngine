package org.rge.window;

import org.joml.Vector4f;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.rge.lua.EngineObject;
import org.rge.lua.EngineReference;

import static org.lwjgl.glfw.GLFW.*;

import java.util.HashMap;

public class Input implements EngineObject {
	
	EngineReference engReference;
	
	public static final int[] charMap = {
			GLFW_KEY_0,
			GLFW_KEY_1,
			GLFW_KEY_2,
			GLFW_KEY_3,
			GLFW_KEY_4,
			GLFW_KEY_5,
			GLFW_KEY_6,
			GLFW_KEY_7,
			GLFW_KEY_8,
			GLFW_KEY_9,
			-1, -1, -1,
			GLFW_KEY_EQUAL,
			-1, -1, -1,
			GLFW_KEY_A,
			GLFW_KEY_B,
			GLFW_KEY_C,
			GLFW_KEY_D,
			GLFW_KEY_E,
			GLFW_KEY_F,
			GLFW_KEY_G,
			GLFW_KEY_H,
			GLFW_KEY_I,
			GLFW_KEY_J,
			GLFW_KEY_K,
			GLFW_KEY_L,
			GLFW_KEY_M,
			GLFW_KEY_N,
			GLFW_KEY_O,
			GLFW_KEY_P,
			GLFW_KEY_Q,
			GLFW_KEY_R,
			GLFW_KEY_S,
			GLFW_KEY_T,
			GLFW_KEY_U,
			GLFW_KEY_V,
			GLFW_KEY_W,
			GLFW_KEY_X,
			GLFW_KEY_Y,
			GLFW_KEY_Z
	};
	
	public static final HashMap<String, Integer> specialMap = new HashMap<>();
	{
		specialMap.put("ESC",			GLFW_KEY_ESCAPE);
		specialMap.put("SPACE", 		GLFW_KEY_SPACE);
		specialMap.put("UP",			GLFW_KEY_UP);
		specialMap.put("DOWN",			GLFW_KEY_DOWN);
		specialMap.put("LEFT",			GLFW_KEY_LEFT);
		specialMap.put("RIGHT",		GLFW_KEY_RIGHT);
		specialMap.put("LEFT_SHIFT",	GLFW_KEY_LEFT_SHIFT);
		specialMap.put("LEFT_CTRL",	GLFW_KEY_LEFT_CONTROL);
	}
	
	public static final int[] mouseKeyMap = {
			GLFW_MOUSE_BUTTON_LEFT,
			GLFW_MOUSE_BUTTON_MIDDLE,
			GLFW_MOUSE_BUTTON_RIGHT
	};
	
	
	public static final int HIGHEST_KEYCODE = 512;
	public static final int HIGHEST_MOUSE_KEYCODE = 16;
	
	public boolean[] isDown;
	public boolean[] justPressed;
	public boolean[] justReleased;
	
	public boolean[] mouseIsDown;
	public boolean[] mouseJustPressed;
	public boolean[] mouseJustReleased;
	
	public Vector4f mouse;
	private float lastx = 0, lasty = 0;
	private boolean first = true;
	
	private Window par;
	private Keyboard kb;
	private Mouse ms;
	private MouseClick msClck;
	
	public Input(Window par) {
		
		initLuaTable();
		
		kb = new Keyboard();
		ms = new Mouse();
		msClck = new MouseClick();
		this.par = par;
	}
	
	public GLFWKeyCallbackI getKbHook() {
		return kb;
	}
	
	public GLFWCursorPosCallbackI getMsHook() {
		return ms;
	}
	
	public GLFWMouseButtonCallbackI getMsClckHook() {
		return msClck;
	}
	
	public void update() {
		kb.update();
		ms.update();
		msClck.update();
	}
	
	private class Keyboard implements GLFWKeyCallbackI {
		
		public Keyboard() {
			isDown = new boolean[HIGHEST_KEYCODE];
			justPressed = new boolean[HIGHEST_KEYCODE];
			justReleased = new boolean[HIGHEST_KEYCODE];
		}
		
		@Override
		public void invoke(long window, int key, int scancode, int action, int mods) {
			if(key == -1)
				return;
			if(action == GLFW_PRESS) {
				justPressed[key] = true;
				isDown[key] = true;
			} else if(action == GLFW_RELEASE) {
				isDown[key] = false;
				justReleased[key] = true;
			}
		}
		
		public void update() {
			for(int i = 0; i < HIGHEST_KEYCODE; i++) {
				justPressed[i] = false;
				justReleased[i] = false;
			}
		}
		
	}
	private class Mouse implements GLFWCursorPosCallbackI {
		
		public Mouse() {
			mouse = new Vector4f();
		}
		
		public void update() {
			mouse.z = 0;
			mouse.w = 0;
		}

		@Override
		public void invoke(long window, double _x, double _y) {
			
			float x = (float) _x;
			float y = (float) _y;
			
			float dx = x-lastx;
			float dy = y-lasty;
			lastx = x;
			lasty = y;
			
			if(first) {
				first = false;
				return;
			}
			
			mouse.x = clamp(mouse.x + dx, 0, par.getWidth());
			mouse.y = clamp(mouse.y + dy, 0, par.getHeight());
			mouse.z = dx;
			mouse.w = dy;
			
		}
		
	}
	
	private class MouseClick implements GLFWMouseButtonCallbackI {
		
		public MouseClick() {
			mouseIsDown = new boolean[HIGHEST_MOUSE_KEYCODE];
			mouseJustPressed = new boolean[HIGHEST_MOUSE_KEYCODE];
			mouseJustReleased = new boolean[HIGHEST_MOUSE_KEYCODE];
		}
		
		@Override
		public void invoke(long window, int button, int action, int mods) {
			if(button == -1)
				return;
			if(action == GLFW_PRESS) {
				System.out.println("Just pressed: " + button);
				mouseJustPressed[button] = true;
				mouseIsDown[button] = true;
			} else if(action == GLFW_RELEASE) {
				mouseIsDown[button] = false;
				mouseJustReleased[button] = true;
			}
		}
		
		public void update() {
			for(int i = 0; i < HIGHEST_MOUSE_KEYCODE; i++) {
				mouseJustPressed[i] = false;
				mouseJustReleased[i] = false;
			}
		}
		
	}
	
	private static float clamp(float val, float min, float max) {
		if(val < min)
			return min;
		if(val > max)
			return max;
		return val;
	}
	
	@Override
	public EngineReference getEngineReference() {
		return engReference;
	}
	
	private static int getGLFWInt(String key) {
		key = key.trim().toUpperCase();
		
		int glfwInt = -1;
		
		if(key.length() == 1) {
			// CHARACTER KEY
			char c = key.charAt(0);
			int off = c-'0';
			if(off < 0 || off >= charMap.length)
				return -1;
			
			glfwInt = charMap[off];
			
		} else {
			// SPECIAL KEY
			if(specialMap.containsKey(key))
				glfwInt = specialMap.get(key);
		}
		
		return glfwInt;
	}
	
	private void initLuaTable() {
		engReference = new EngineReference(this);
		
		engReference.set("isDown", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				if(!(arg0 instanceof LuaString))
					return null;
				String key = arg0.checkjstring();
				
				int glfwInt = getGLFWInt(key);
				if(glfwInt == -1)
					return LuaValue.FALSE;
				
				if(isDown[glfwInt])
					return LuaValue.TRUE;
				return LuaValue.FALSE;
			}
		});
		
		engReference.set("justPressed", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				if(!(arg0 instanceof LuaString))
					return null;
				String key = arg0.checkjstring();
				
				int glfwInt = getGLFWInt(key);
				if(glfwInt == -1)
					return LuaValue.FALSE;
				
				if(justPressed[glfwInt])
					return LuaValue.TRUE;
				return LuaValue.FALSE;
			}
		});
		
		engReference.set("justReleased", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				if(!(arg0 instanceof LuaString))
					return null;
				String key = arg0.checkjstring();
				
				int glfwInt = getGLFWInt(key);
				if(glfwInt == -1)
					return LuaValue.FALSE;
				
				if(justReleased[glfwInt])
					return LuaValue.TRUE;
				return LuaValue.FALSE;
			}
		});
		
		engReference.set("mouseIsDown", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				if(!(arg0 instanceof LuaInteger))
					return null;
				
				// TODO: merge with isDown function
				
				int key = arg0.checkint();
				if(key < 0 || key >= mouseKeyMap.length)
					return LuaValue.FALSE;
				
				if(mouseIsDown[key])
					return LuaValue.TRUE;
				return LuaValue.FALSE;
			}
		});
		
		engReference.set("mouseJustPressed", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				if(!(arg0 instanceof LuaInteger))
					return null;
				
				// TODO: merge with justPressed function
				
				int key = arg0.checkint();
				if(key < 0 || key >= mouseKeyMap.length)
					return LuaValue.FALSE;
				
				if(mouseJustPressed[key])
					return LuaValue.TRUE;
				return LuaValue.FALSE;
			}
		});
		
		engReference.set("mouseJustReleased", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				if(!(arg0 instanceof LuaInteger))
					return null;
				
				// TODO: merge with justReleased function
				
				int key = arg0.checkint();
				if(key < 0 || key >= mouseKeyMap.length)
					return LuaValue.FALSE;
				
				if(mouseJustReleased[key])
					return LuaValue.TRUE;
				return LuaValue.FALSE;
			}
		});
		
		engReference.set("getMouseX", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaValue.valueOf(mouse.x);
			}
		});
		
		engReference.set("getMouseY", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaValue.valueOf(mouse.y);
			}
		});
		
		engReference.set("getMouseDX", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaValue.valueOf(mouse.z);
			}
		});
		
		engReference.set("getMouseDY", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaValue.valueOf(mouse.w);
			}
		});
		
	}
	
}
