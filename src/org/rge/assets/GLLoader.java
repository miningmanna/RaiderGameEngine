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
import org.newdawn.slick.opengl.Texture;
import org.rge.assets.models.Model;
import org.rge.assets.models.Model.RawData.Verts;
import org.rge.assets.MTexture;

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
		
		model.vao = glGenVertexArrays();
		vaos.add(model.vao);
		glBindVertexArray(model.vao);
		
		model.usedVBOs = new int[model.raw.verts.length];
		
		for(int i = 0; i < model.raw.verts.length; i++) {
			model.usedVBOs[i] = model.raw.verts[i].pos;
			loadVertsIntoVBO(model.raw.verts[i]);
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
	
	public int loadVertsIntoVBO(Verts verts) {
		
		int vbo = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		vbos.add(vbo);
		
		FloatBuffer buff = BufferUtils.createFloatBuffer(verts.rawVerts.length);
		buff.put(verts.rawVerts);
		buff.flip();
		
		glBufferData(GL_ARRAY_BUFFER, buff, GL_STATIC_DRAW);
		glVertexAttribPointer(verts.pos, verts.dimension, GL_FLOAT, false, 0, 0);
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		return vbo;
		
	}
	
	public Texture loadTexture(BufferedImage img) {
		
		return getTexture(img);
		
	}
	
	public Texture getTexture(BufferedImage img) {
		
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
		return new MTexture(id);
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
