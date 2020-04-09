package org.rge.assets;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.rge.assets.models.Model;
import org.rge.assets.models.Verts;
import org.rge.assets.models.tilemap.TileMap;

public class GLLoader {
	
	private ArrayList<Integer> texsIds;
	private ArrayList<Integer> vaos;
	private ArrayList<Integer> vbos;
	
	public GLLoader() {
		texsIds = new ArrayList<>();
		vaos = new ArrayList<>();
		vbos = new ArrayList<>();
	}
	
	public void loadModel(Model model) {
		System.out.println(model.vao);
		model.vao = glGenVertexArrays();
		vaos.add(model.vao);
		glBindVertexArray(model.vao);
		
		model.usedVBOs = new int[model.raw.verts.length];
		
		for(int i = 0; i < model.raw.verts.length; i++) {
			model.usedVBOs[i] = model.raw.verts[i].pos;
			loadVertsIntoVBO(model.raw.verts[i], GL_STATIC_DRAW);
		}
		
		int indVbo = glGenBuffers();
		vbos.add(indVbo);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indVbo);
		IntBuffer intBuff = BufferUtils.createIntBuffer(model.raw.rawInds.length);
		intBuff.put(model.raw.rawInds);
		intBuff.flip();
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, intBuff, GL_STATIC_READ);
		
		model.indCount = model.raw.rawInds.length;
		
		glBindVertexArray(0);
		
	}
	
	private int loadVertsIntoVBO(Verts verts, int mode) {
		
		int vbo = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		vbos.add(vbo);
		
		FloatBuffer buff = BufferUtils.createFloatBuffer(verts.rawVerts.length);
		buff.put(verts.rawVerts);
		buff.flip();
		
		glBufferData(GL_ARRAY_BUFFER, buff, mode);
		glVertexAttribPointer(verts.pos, verts.dimension, GL_FLOAT, false, 0, 0);
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		return vbo;
		
	}
	
	public int loadTexture(BufferedImage img) {
		
		int id = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, id);
		
		int[] pixels = new int[img.getHeight()*img.getWidth()];
		img.getRGB(0, 0, img.getWidth(), img.getHeight(), pixels, 0, img.getWidth());
		ByteBuffer buffer = BufferUtils.createByteBuffer(img.getWidth() * img.getHeight() * 4); //4 for RGBA, 3 for RGB
		for(int y = 0; y < img.getHeight(); y++){
			for(int x = 0; x < img.getWidth(); x++){
				int pixel = pixels[y * img.getWidth() + x];
				buffer.put((byte) ((pixel >> 16) & 0xFF));
				buffer.put((byte) ((pixel >> 8) & 0xFF));
				buffer.put((byte) (pixel & 0xFF));
				buffer.put((byte) ((pixel >> 24) & 0xFF));
			}
		}
		
		buffer.flip();
		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, img.getWidth(), img.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
		
		glBindTexture(GL_TEXTURE_2D, 0);
		
		texsIds.add(id);
		return id;
	}
	
	public void updateTilesTiles(TileMap tileMap, int... tile) {
		int tileLim = tileMap.width*tileMap.height - 1;
		
		for(int i : tile) {
			if(i < 0 || i > tileLim)
				continue;
			
			for(int j = 0; j < tileMap.verts.length; j++) {
				int dim = tileMap.verts[j].dimension;
				int off = i*12*dim;
				
				System.out.println(j + " : " + dim + " " + tileMap.usedVBOs[j]);
				System.out.println(12*dim);
				System.out.println(off);
				
				bufferVertexSub(
						tileMap.vbos[j],
						off,
						tileMap.verts[j].rawVerts,
						off,
						12*dim);
				
			}
			
		}
		
	}
	
	private static void bufferVertexSub(int vbo, long boff, float[] data, int voff, int length) {
		
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		FloatBuffer buff = BufferUtils.createFloatBuffer(length);
		buff.put(data, voff, length);
		buff.flip();
		glBufferSubData(GL_ARRAY_BUFFER, boff*4, buff);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	public void loadTileMap(TileMap tileMap) {
		
		tileMap.vao = glGenVertexArrays();
		vaos.add(tileMap.vao);
		glBindVertexArray(tileMap.vao);
		
		tileMap.usedVBOs = new int[tileMap.verts.length];
		tileMap.vbos = new int[tileMap.verts.length];
		
		for(int i = 0; i < tileMap.verts.length; i++) {
			tileMap.usedVBOs[i] = tileMap.verts[i].pos;
			tileMap.vbos[i] = loadVertsIntoVBO(tileMap.verts[i], GL_DYNAMIC_DRAW);
		}
		
		int[] indices = new int[tileMap.width * tileMap.height * 4 * 3]; // For each tile -> 4 triangles -> 3 indices per triangle
		for(int i = 0; i < indices.length; i++)
			indices[i] = i;
		
		int indVbo = glGenBuffers();
		vbos.add(indVbo);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indVbo);
		IntBuffer intBuff = BufferUtils.createIntBuffer(indices.length);
		intBuff.put(indices);
		intBuff.flip();
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, intBuff, GL_STATIC_READ);
		
		glBindVertexArray(0);
		
	}
	
	public void destroy() {
		
		for(int tex : texsIds)
			glDeleteTextures(tex);
		for(int vbo : vbos)
			glDeleteBuffers(vbo);
		for(int vao : vaos)
			glDeleteVertexArrays(vao);
		
	}
	
}
