package org.rge;

import java.io.File;
import java.io.IOException;

import org.rge.assets.AssetManager;

public class Main {
	
	public static void main(String[] args) {
		
		File pluginDir = new File("plugins");
		if(pluginDir.exists() && pluginDir.isDirectory()) {
			try {
				AssetManager.loadClassesFromJars(pluginDir.listFiles());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		RGEContext context = new RGEContext();
		context.setInitScript("init.lua");
		context.init();
		
		while(!context.shouldClose()) {
			context.update();
			//context.renderer.queue(node);
			context.render();
		}
		
		context.destroy();
		
	}
	
}
