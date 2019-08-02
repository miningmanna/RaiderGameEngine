package org.rge.assets.models;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.rge.assets.models.Model.RawData.Verts;

public class ModelLoader {
	
	private ArrayList<Integer> vaos;
	private ArrayList<Integer> vbos;
	
	public ModelLoader() {
		vaos = new ArrayList<>();
		vbos = new ArrayList<>();
	}
	
	public void loadModelVerts(Model model) {
		
		model.vao = glGenVertexArrays();
		vaos.add(model.vao);
		glBindVertexArray(model.vao);
		
		for(int i = 0; i < model.raw.verts.length; i++)
			model.usedVBOs[i] = loadVertsIntoVBO(model.raw.verts[i], i);
		
		int indVbo = glGenBuffers();
		vbos.add(indVbo);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indVbo);
		IntBuffer intBuff = BufferUtils.createIntBuffer(model.raw.rawInds.length);
		intBuff.put(model.raw.rawInds);
		intBuff.flip();
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, intBuff, GL_STATIC_READ);
		
		glBindVertexArray(0);
		
	}
	
	public int loadVertsIntoVBO(Verts verts, int pos) {
		
		int vbo = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		vbos.add(vbo);
		
		FloatBuffer buff = BufferUtils.createFloatBuffer(verts.rawVerts.length);
		buff.put(verts.rawVerts);
		buff.flip();
		
		glBufferData(GL_ARRAY_BUFFER, buff, GL_STATIC_DRAW);
		glVertexAttribPointer(pos, verts.dimension, GL_FLOAT, false, 0, 0);
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		return vbo;
		
	}
	
	public void destroy() {
		
		for(int vbo : vbos)
			glDeleteBuffers(vbo);
		for(int vao : vaos)
			glDeleteVertexArrays(vao);
		
	}
	
	
}
