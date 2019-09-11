package org.rge;

import java.io.File;
import java.io.IOException;

import org.rge.assets.AssetManager;

public class Main {
	
	public static void main(String[] args) {
		
		File pluginDir = new File("plugins");
		if(pluginDir.exists() && pluginDir.isDirectory()) {
			try {
				for(File f : pluginDir.listFiles())
					if(f.getName().toUpperCase().endsWith(".JAR")) {
						AssetManager.loadClassesFromJar(f);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		RGEContext context = new RGEContext();
		context.setInitScript("init.lua");
		context.setTickScript("tick.lua");
		context.init();
		
		while(!context.shouldClose()) {
			context.tick();
			context.render();
		}
		
		context.destroy();
		
	}
	
}
