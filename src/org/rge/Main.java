package org.rge;

import static org.lwjgl.glfw.GLFW.*;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.rge.assets.AssetManager;
import org.rge.assets.models.Model;
import org.rge.assets.models.Model.RawData;
import org.rge.assets.models.Model.RawData.Verts;
import org.rge.graphics.Camera;
import org.rge.graphics.light.AmbientLight;
import org.rge.graphics.light.DirectionalLight;
import org.rge.graphics.light.LightGroup;
import org.rge.graphics.light.PointLight;
import org.rge.graphics.light.SpotLight;
import org.rge.node.DrawNode;
import org.rge.node.Move;
import org.rge.window.Input;

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
		
		context.am.registerInputGen("dir", "Data");
		
		context.init();
		context.setSize(1920, 1080);
		context.window.center();
		context.window.show();
		context.setClearColor(Color.BLACK);
		
//		Shader shader = null;
//		try {
//			shader = context.am.getShader("shaders/tri");
//		} catch (IOException e) {
//			System.out.println("Couldnt load shader");
//			e.printStackTrace();
//		}
		
		RawData teapotRaw = context.am.getModelRawData("obj", "teapot.obj");
		teapotRaw.verts[0].scaleAll(0.3f);
		
		System.out.println("Teapot: " + teapotRaw);
		
		RawData lwoRaw = context.am.getModelRawData("lwo", "dock2.lwo");
		RawData slugRaw = context.am.getModelRawData("lwo", "N_Slime.lwo");
		
		RawData modelData = new RawData();
		modelData.rawInds = new int[] { 0, 1, 2 };
		modelData.verts = new Verts[1];
		Verts verts = new Verts();
		verts.pos = 0;
		verts.dimension = 3;
		verts.rawVerts = new float[] {
				0, -1, 0.5f,
				1, 1, 0.5f,
				-1, 1, 0.5f
		};
		modelData.verts[0] = verts;
		
		//Model m = new Model(context.am, modelData, false);
		//m.shader = shader;
		
//		Model teapotModel = null;
		Model lwoModel = null;
		Model slugModel = null;
		try {
//			teapotModel = new Model(context.am, teapotRaw, false);
			lwoModel = new Model(context.am, lwoRaw, false);
			slugModel = new Model(context.am, slugRaw, false);
		} catch (IOException e) {
			System.out.println("Failed to get default shader");
			e.printStackTrace();
		}
		
		Input input = context.window.input;
		
		Camera c = new Camera();
		c.setFrustum(60, (float) context.window.getWidth()/context.window.getHeight(), 0.1f, 1000);
		
		float speed = 2.1f;
		float mouseSens = 0.005f;
		
		DrawNode root = new DrawNode();
		root.model = lwoModel;
		
		Matrix4f slugTrans = new Matrix4f().identity();
		slugTrans.translate(0, 40, 0);
		
		DrawNode slugNode = new DrawNode();
		slugNode.model = slugModel;
		slugNode.move = new Move() {
			@Override
			public Matrix4f getTransform() {
				return slugTrans;
			}
		};
		root.subNodes.add(slugNode);
		
		LightGroup lights = new LightGroup();
		lights.ambientLight = new AmbientLight(0.1f);
		
		DirectionalLight dirLight = new DirectionalLight(new Vector3f(1, -1, 1), 0.1f);
		dirLight.color = Color.YELLOW;
		lights.addLight(dirLight);
		
		PointLight camLight = new PointLight(c.position, 100.0f);
		camLight.color = Color.GREEN;
		camLight.clamp = 1;
		camLight.pos = c.position;
		lights.addLight(camLight);
		
		SpotLight spot = new SpotLight(null, 100000, 10);
		spot.position = c.position;
		spot.direction = c.direction;
		lights.addLight(spot);
		
		while(!context.shouldClose()) {
			
			if(input.justReleased[GLFW_KEY_ESCAPE])
				context.setShouldClose(true);
			
			if(input.isDown[GLFW_KEY_W])
				c.move(new Vector3f(0, 0, speed));
			if(input.isDown[GLFW_KEY_A])
				c.move(new Vector3f(-speed, 0, 0));
			if(input.isDown[GLFW_KEY_S])
				c.move(new Vector3f(0, 0, -speed));
			if(input.isDown[GLFW_KEY_D])
				c.move(new Vector3f(speed, 0, 0));
			if(input.isDown[GLFW_KEY_LEFT_CONTROL])
				c.move(new Vector3f(0, -speed, 0));
			if(input.isDown[GLFW_KEY_SPACE])
				c.move(new Vector3f(0, speed, 0));
			
			c.rotateY(input.mouse.w*mouseSens);
			c.rotateX(input.mouse.z*mouseSens);
			
			c.update();
			context.useCamera(c);
			context.useLights(lights);
			
			context.tick();
			
			context.queueRender(root);
			
			context.render();
		}
		
		context.destroy();
		
	}
	
}
