package org.rge.graphics;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.util.ArrayList;
import java.util.Comparator;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.rge.graphics.light.LightGroup;
import org.rge.node.DrawNode;

public class Renderer {
	
	ArrayList<DrawBatch> opaqueBatches;
	ArrayList<DrawBatch> translucentBatches;
	
	ArrayList<SurfaceDrawItem> orderedSurfs;
	Comparator<SurfaceDrawItem> comparator;
	Vector3f camTranslation = new Vector3f();
	Vector3f translatedMidPoint = new Vector3f();
	
	private Matrix4f camMatrix;
	private LightGroup group;
	
	public Renderer() {
		opaqueBatches = new ArrayList<>();
		translucentBatches = new ArrayList<>();
		orderedSurfs = new ArrayList<>();
		comparator = new Comparator<SurfaceDrawItem>() {
			
			@Override
			public int compare(SurfaceDrawItem o1, SurfaceDrawItem o2) {
				if(o1.distance < o2.distance)
					return -1;
				if(o2.distance < o1.distance)
					return 1;
				return 0;
			}
			
		};
	}
	
	public void queue(DrawNode node) {
		queue(node, null);
	}
	
	private void queue(DrawNode node, Matrix4f parTrans) {
		Matrix4f transform = null;
		if(parTrans == null)
			transform = node.getTransform();
		else
			transform = new Matrix4f(parTrans).mul(node.getTransform());
		
		if(node.model != null) {
			
			DrawBatch opaqueBatch = new DrawBatch();
			opaqueBatch.camera = camMatrix;
			opaqueBatch.lights = group;
			opaqueBatch.transform = transform;
			
			DrawBatch translucentBatch = new DrawBatch();
			translucentBatch.camera = camMatrix;
			translucentBatch.lights = group;
			translucentBatch.transform = transform;
			
			for(Surface s : node.model.getSurfs()) {
				if(s.isTranslucent)
					translucentBatch.toDraw.add(s);
				else
					opaqueBatch.toDraw.add(s);
			}
			
			opaqueBatches.add(opaqueBatch);
			translucentBatches.add(translucentBatch);
			
		}
		for(DrawNode subNode : node.subNodes)
			queue(subNode, transform);
		
	}
	
	private void drawSurface(Surface surf) {
		
		Renderable m = surf.par;
		Shader shader = m.getShader();
		int vao = m.getVAO();
		int[] usedVBOs = m.getUsedVBOs();
		
		shader.start();
		
		glBindVertexArray(vao);
		for(int vbo : usedVBOs)
			glEnableVertexAttribArray(vbo);
		
		glActiveTexture(GL_TEXTURE0);
		
		if(surf.doubleSided)
			glDisable(GL_CULL_FACE);
		
		if(surf.isTranslucent)
			glDepthMask(false);
		
		if(surf.additiveColor)
			glBlendFunc(GL_ONE, GL_ONE);
		
		if(surf.tex != null) {
			glBindTexture(GL_TEXTURE_2D, surf.tex.getCurTexId());
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
			shader.setUniBoolean("useTex", true);
		} else {
			shader.setUniBoolean("useTex", false);
		}
		
		glDrawElements(GL_TRIANGLES, surf.indLength, GL_UNSIGNED_INT, surf.indOffset*4);
		
		if(surf.doubleSided)
			glEnable(GL_CULL_FACE);
		
		if(surf.isTranslucent)
			glDepthMask(true);
		
		if(surf.additiveColor)
			glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		for(int vbo : usedVBOs)
			glDisableVertexAttribArray(vbo);
		glBindVertexArray(0);
		shader.stop();
		
	}
	
	public void renderAll() {
		
		// TODO: implement lighting, check ordered drawing of translucent surfaces
		
		for(DrawBatch batch : opaqueBatches) {
			
			if(batch.toDraw.isEmpty())
				continue;
			
			Shader batchShader = batch.toDraw.get(0).par.getShader();
			batchShader.start();
			batchShader.setCamera(batch.camera);
			batchShader.setTransform(batch.transform);
			batchShader.setLights(batch.lights);
			
			for(Surface s : batch.toDraw)
				drawSurface(s);
		}
		
		opaqueBatches.clear();
		
		// TODO: Draw translucent surfaces in order of distance from the camera
		
		for(DrawBatch batch : translucentBatches) {
			if(batch.toDraw == null)
				continue;
			
			batch.camera.getTranslation(camTranslation);
			
			for(Surface surf : batch.toDraw) {
				
				SurfaceDrawItem drawItem = new SurfaceDrawItem();
				if(surf.midPoint != null) {
					translatedMidPoint.set(surf.midPoint);
					translatedMidPoint.mulPosition(batch.transform);
					translatedMidPoint.add(camTranslation);
					drawItem.distance = translatedMidPoint.length();
				} else {
					drawItem.distance = Float.MAX_VALUE;
				}
				
				drawItem.batch = batch;
				drawItem.surf = surf;
				
				orderedSurfs.add(drawItem);
			}
			
		}
		
		orderedSurfs.sort(comparator);
		
		for(int i = 0; i < orderedSurfs.size(); i++) {
			SurfaceDrawItem item = orderedSurfs.get(i);
			DrawBatch batch = item.batch;
			Surface surf = item.surf;
			
			Shader batchShader = batch.toDraw.get(0).par.getShader();
			batchShader.start();
			batchShader.setCamera(batch.camera);
			batchShader.setTransform(batch.transform);
			batchShader.setLights(batch.lights);
			drawSurface(surf);
		}
		
		orderedSurfs.clear();
		translucentBatches.clear();
		
	}
	
	private static class SurfaceDrawItem {
		public float distance;
		public Surface surf;
		public DrawBatch batch;
	}
	
/*	private void render(Model m) {
		
		m.shader.start();
		
		if(camMatrix != null)
			m.shader.setUniMatrix4f("camera", camMatrix);
		
		glBindVertexArray(m.vao);
		for(int vbo : m.usedVBOs)
			glEnableVertexAttribArray(vbo);
		
		glActiveTexture(GL_TEXTURE0);
		
		if(m.texs != null) {
			int offset = 0;
			for(int i = 0; i < m.texs.length; i++) {
				if(m.texs[i] != null) {
					m.texs[i].bind();
					glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
					glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
					m.shader.setUniBoolean("useTex", true);
				} else {
					m.shader.setUniBoolean("useTex", false);
				}
				glDrawElements(GL_TRIANGLES, m.indCountForTex[i], GL_UNSIGNED_INT, offset);
				offset += m.indCountForTex[i]*4;
			}
		} else {
			
			m.shader.setUniInteger("tex", -1);
			
			glDrawElements(GL_TRIANGLES, m.indCount, GL_UNSIGNED_INT, 0);
			
		}
		
		for(int vbo : m.usedVBOs)
			glDisableVertexAttribArray(vbo);
		glBindVertexArray(0);
		m.shader.stop();
	}*/
	
	public void setCameraMatrix(Matrix4f matrix) {
		this.camMatrix = new Matrix4f(matrix);
	}
	
	public void setLightGroup(LightGroup group) {
		this.group = group;
	}
	
	private static class DrawBatch {
		public DrawBatch() {
			toDraw = new ArrayList<>();
		}
		public LightGroup lights;
		public ArrayList<Surface> toDraw;
		public Matrix4f camera;
		public Matrix4f transform;
	}
}
