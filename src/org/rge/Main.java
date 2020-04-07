package org.rge;

import java.io.File;
import java.io.IOException;

import org.rge.assets.AssetManager;
import org.rge.assets.models.tilemap.TileMap;
import org.rge.assets.models.tilemap.TileType;
import org.rge.assets.models.tilemap.TileMap.RawTileMap;
import org.rge.assets.models.tilemap.TileMapTexture.RawTileMapTexture;
import org.rge.node.DrawNode;
import org.rge.node.Move;

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
		
		/*int w = 10, h = 10;
		RawTileMap rawMap = new RawTileMap(w, h);
		for(int i = 0; i < h; i++)
			for(int j = 0; j < w; j++)
				rawMap.heights[i*(2*w+1) + (w+1) + j] = 1;
		
		for(int x = 0; x < w; x++)
			for(int y = 0; y < h; y++)
				rawMap.tiles[x][y] = 0;
		
		int tw = 8, th = 8;
		RawTileMapTexture rawTexs = new RawTileMapTexture(tw, th);
		for(int x = 0; x < tw; x++)
			for(int y = 0; y < th; y++)
				rawTexs.texPaths[x][y] = String.format("IceSplit/ICE%d%d.BMP", x, y);
		
		TileType type0 = new TileType();
		type0.connects.add(0);
		for(int i = 0; i < 256; i++) {
			type0.texs[i].x = 7;
			type0.texs[i].y = 5;
		}
		
		System.out.println(type0);
		System.out.println(rawTexs.types);
		rawTexs.types.add(type0);
		
		rawMap.rawTexs = rawTexs;
		
		TileMap map = new TileMap(context.am, rawMap);
		
		DrawNode node = new DrawNode();
		node.model = map;
		try {
			map.shader = context.am.getShader("tilemap");
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		
		while(!context.shouldClose()) {
			context.tick();
			//context.renderer.queue(node);
			context.render();
		}
		
		context.destroy();
		
	}
	
}
