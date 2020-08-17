package org.rge.graphics;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.util.HashMap;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.rge.graphics.light.DirectionalLight;
import org.rge.graphics.light.LightGroup;
import org.rge.graphics.light.PointLight;
import org.rge.graphics.light.SpotLight;
import org.rge.lua.EngineObject;
import org.rge.lua.EngineReference;

public class Shader implements EngineObject {
	
	// TODO: remember light uniform locations
	
	EngineReference engReference;
	
	private int program, vShader, fShader;
	
	public Shader(String vertShader, String fragShader) {
		
		initLuaTable();
		
		this.vShader = loadShader(vertShader, GL_VERTEX_SHADER);
		this.fShader = loadShader(fragShader, GL_FRAGMENT_SHADER);
		program = glCreateProgram();
		glAttachShader(program, vShader);
		glAttachShader(program, fShader);
		glLinkProgram(program);
		glValidateProgram(program);
		
	}
	
	public int getShaderID() {
		return program;
	}
	
	public void start() {
		glUseProgram(program);
	}
	
	public void stop() {
		glUseProgram(0);
	}
	
	public void destroy() {
		
		stop();
		glDetachShader(program, vShader);
		glDetachShader(program, fShader);
		glDeleteShader(vShader);
		glDeleteShader(fShader);
		glDeleteProgram(program);
		
	}
	
	private static int loadShader(String shaderSource, int type) {
		
		int shader = glCreateShader(type);
		glShaderSource(shader, shaderSource);
		glCompileShader(shader);
		if(glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE)
			System.err.println(glGetShaderInfoLog(shader, 512));
		return shader;
		
	}

	public void setUniFloat(int pos, float f) {
		glUniform1f(pos, f);
	}
	
	public void setUniFloat(String name, float f) {
		if(name == null)
			throw new IllegalArgumentException("Name cant be null!");
		setUniFloat(glGetUniformLocation(program, name), f);
	}

	public void setUniVector2f(int pos, Vector2f v) {
		if(v == null)
			throw new IllegalArgumentException("Vector cant be null!");
		glUniform2f(pos, v.x, v.y);
	}
	
	public void setUniVector2f(String name, Vector2f v) {
		if(name == null)
			throw new IllegalArgumentException("Name cant be null!");
		setUniVector2f(glGetUniformLocation(program, name), v);
	}
	
	public void setUniVector3f(int pos, Vector3f v) {
		if(v == null)
			throw new IllegalArgumentException("Vector cant be null!");
		glUniform3f(pos, v.x, v.y, v.z);
	}
	
	public void setUniVector3f(String name, Vector3f v) {
		if(name == null)
			throw new IllegalArgumentException("Name cant be null!");
		setUniVector3f(glGetUniformLocation(program, name), v);
	}
	
	public void setUniVector4f(int pos, Vector4f v) {
		if(v == null)
			throw new IllegalArgumentException("Vector cant be null!");
		glUniform4f(pos, v.x, v.y, v.z, v.w);
	}
	
	public void setUniVector4f(String name, Vector4f v) {
		if(name == null)
			throw new IllegalArgumentException("Name cant be null!");
		setUniVector4f(glGetUniformLocation(program, name), v);
	}
	
	public void setUniBoolean(int pos, boolean b) {
		if(b)
			glUniform1i(pos, 1);
		else
			glUniform1i(pos, 0);
	}
	
	public void setUniBoolean(String name, boolean b) {
		setUniBoolean(glGetUniformLocation(program, name), b);
	}
	
	public void setUniMatrix4f(int pos, Matrix4f m) {
		if(m == null)
			throw new IllegalArgumentException("Matrix cant be null!");
		FloatBuffer b = BufferUtils.createFloatBuffer(16);
		m.get(b);
		glUniformMatrix4fv(pos, false, b);
	}
	
	public void setUniMatrix4f(String name, Matrix4f m) {
		if(name == null)
			throw new IllegalArgumentException("Name cant be null!");
		setUniMatrix4f(glGetUniformLocation(program, name), m);
	}
	
	public int[] getUniPos(String... names) {
		if(names == null) {
			return null;
		}
		int[] res = new int[names.length];
		
		for(int i = 0; i < names.length; i++) {
			if(names[i] == null)
				res[i] = -1;
			res[i] = glGetUniformLocation(program, names[i]);
		}
		
		return res;
	}
	
	public void setUniIntegerArray(String name, int[] array) {
		if(name == null)
			throw new IllegalArgumentException("Name cant be null!");
		setUniIntegerArray(glGetUniformLocation(program, name), array);
	}
	
	public void setUniIntegerArray(int pos, int[] array) {
		if(array == null)
			return;
		glUniform1iv(pos, array);
	}
	
	public void setUniInteger(String name, int i) {
		setUniInteger(glGetUniformLocation(program, name), i);
	}
	
	public void setUniInteger(int pos, int i) {
		if(pos < 0)
			return;
		glUniform1i(pos, i);
	}
	
	public void setCamera(Matrix4f camera) {
		setUniMatrix4f("camera", camera);
	}
	
	public void setTransform(Matrix4f transform) {
		setUniMatrix4f("transform", transform);
	}
	
	public void setLights(LightGroup lights) {
		
		if(lights == null) {
			
			Vector4f ambientVec = new Vector4f();
			setUniVector4f("ambientLight", ambientVec);
			for(int i = 0; i < LightGroup.POINTLIGHT_COUNT; i++)
				setUniFloat("pointLights[" + i + "].intensity", 0);
			for(int i = 0; i < LightGroup.DIRECTIONALLIGHT_COUNT; i++)
				setUniFloat("directionalLights[" + i + "].intensity", 0);
			for(int i = 0; i < LightGroup.SPOTLIGHT_COUNT; i++)
				setUniFloat("spotLights[" + i + "].intensity", 0);
			
			return;
		}
		
		if(lights.ambientLight != null) {
			Color ambientColor = lights.ambientLight.color;
			Vector4f ambientVec = new Vector4f(	ambientColor.getRed()	/ 255.0f,
													ambientColor.getGreen()	/ 255.0f,
													ambientColor.getBlue()	/ 255.0f,
													lights.ambientLight.intensity);
			setUniVector4f("ambientLight", ambientVec);
		} else {
			Vector4f ambientVec = new Vector4f();
			setUniVector4f("ambientLight", ambientVec);
		}
		
		Vector3f temp = new Vector3f();
		for(int i = 0; i < lights.directionalLights.length; i++) {
			DirectionalLight light = lights.directionalLights[i];
			String baseName = "directionalLights[" + i + "]";
			if(light == null) {
				setUniFloat(baseName + ".intensity", 0);
				continue;
			}
			
			setUniVector3f(baseName + ".direction", light.dir);
			temp.x = light.color.getRed()	/ 255.0f;
			temp.y = light.color.getGreen()	/ 255.0f;
			temp.z = light.color.getBlue()	/ 255.0f;
			setUniVector3f(baseName + ".color", temp);
			setUniFloat(baseName + ".intensity", light.intensity);
			setUniFloat(baseName + ".clamp", light.clamp);
			
		}
		
		for(int i = 0; i < lights.pointLights.length; i++) {
			PointLight light = lights.pointLights[i];
			String baseName = "pointLights[" + i + "]";
			if(light == null) {
				setUniFloat(baseName + ".intensity", 0);
				continue;
			}
			
			setUniVector3f(baseName + ".position", light.pos);
			temp.x = light.color.getRed()	/ 255.0f;
			temp.y = light.color.getGreen()	/ 255.0f;
			temp.z = light.color.getBlue()	/ 255.0f;
			setUniVector3f(baseName + ".color", temp);
			setUniFloat(baseName + ".intensity", light.intensity);
			setUniFloat(baseName + ".clamp", light.clamp);
			
		}
		
		for(int i = 0; i < lights.spotLights.length; i++) {
			SpotLight light = lights.spotLights[i];
			String baseName = "spotLights[" + i + "]";
			if(light == null) {
				setUniFloat(baseName + ".intensity", 0);
				continue;
			}
			
			setUniVector3f(baseName + ".position", light.position);
			setUniVector3f(baseName + ".direction", light.direction);
			temp.x = light.color.getRed()	/ 255.0f;
			temp.y = light.color.getGreen()	/ 255.0f;
			temp.z = light.color.getBlue()	/ 255.0f;
			setUniVector3f(baseName + ".color", temp);
			setUniFloat(baseName + ".intensity", light.intensity);
			setUniFloat(baseName + ".clamp", light.clamp);
			setUniFloat(baseName + ".cutoff", light.cutoff);
			
		}
		
	}
	
	@Override
	public EngineReference getEngineReference() {
		return engReference;
	}
	
	private void initLuaTable() {
		engReference = new EngineReference(this);
		// No need for Lua functions (i guess?)
		// TODO: NVM need them anyways in case you want to set uni variables once
	}

	public void applyArgs(HashMap<String, Object> args) {
		if(args == null)
			return;
		
		for(String key : args.keySet()) {
			Object o = args.get(key);
			
			if(o instanceof Vector3f) {
				setUniVector3f(key, (Vector3f) o);
				continue;
			}
			if(o instanceof Matrix4f) {
				setUniMatrix4f(key, (Matrix4f) o);
				continue;
			}
			if(o instanceof Integer) {
				setUniInteger(key, (Integer) o);
				continue;
			}
			if(o instanceof Float) {
				setUniFloat(key, (Float) o);
				continue;
			}
			// TODO: add potential argument types
			
		}
		
	}
	
}
