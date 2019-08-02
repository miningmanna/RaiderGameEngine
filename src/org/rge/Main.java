package org.rge;

import static org.lwjgl.glfw.GLFW.*;

import java.io.File;
import java.io.IOException;

import org.rge.assets.AssetManager;

public class Main {
	
	public static void main(String[] args) {
		
		try {
			AssetManager.loadClassesFromJar(new File("AssetsTest.jar"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		RGEContext context = new RGEContext();
		
		context.init();
		context.window.show();
		
		while(!context.shouldClose()) {
			
			if(context.window.input.justReleased[GLFW_KEY_ESCAPE])
				context.setShouldClose(true);
			
			context.tick();
			context.render();
		}
		
		context.destroy();
		
	}
	
}
