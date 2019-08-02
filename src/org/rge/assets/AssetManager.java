package org.rge.assets;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.rge.assets.io.InputGen;
import org.rge.assets.models.Model;
import org.rge.assets.models.ModelLoader;
import org.rge.graphics.Shader;

public class AssetManager {
	
	private HashMap<String, Shader> shaders;
	
	private ModelLoader modelLoader;
	
	private ArrayList<InputGen> inputGens;
	
	private static HashMap<String, InputGenClassContainer> inputGenClasses = new HashMap<>();
	public static void registerInputGenClass(String type, Class<? extends InputGen> genClass) {
		
		type = type.toUpperCase();
		
		InputGenClassContainer container = new InputGenClassContainer();
		container.inputGenClass = genClass;
		
		boolean hasZeroArgConstructor = false;
		Constructor<?>[] constructors = genClass.getConstructors();
		for(int i = 0; i < constructors.length; i++) {
			if(constructors[i].getParameterCount() == 0) {
				hasZeroArgConstructor = true;
				container.constructorIndex = i;
				break;
			}
		}
		
		if(!hasZeroArgConstructor) {
			System.err.println("Cant use InputGen: " + genClass);
			System.err.println("No constructor with 0 parameters");
			return;
		}
		
		inputGenClasses.put(type, container);
		
	}
	
	private static class InputGenClassContainer {
		public int constructorIndex;
		public Class<? extends InputGen> inputGenClass;
		public InputGen constructInputGen() {
			try {
				return (InputGen) inputGenClass.getConstructors()[constructorIndex].newInstance();
			} catch (Exception e) {
				System.err.println("Failed to create instance of: " + inputGenClass);
				e.printStackTrace();
			}
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void loadClassesFromJar(File jarFile) throws IOException {
		if(jarFile == null)
			return;
		
		JarFile jar = new JarFile(jarFile);
		Enumeration<JarEntry> jarEntries = jar.entries();
		
		URL[] urls = { new URL("jar:file:" + jarFile.getAbsolutePath() + "!/")};
		URLClassLoader cl = new URLClassLoader(urls);
		
		while(jarEntries.hasMoreElements()) {
			JarEntry je = jarEntries.nextElement();
			if(je.isDirectory() || !je.getName().endsWith(".class"))
				continue;
			
			String className = je.getName();
			className = className.substring(0, className.length()-6).replace('/', '.');
			try {
				Class<?> mClass = cl.loadClass(className);
				
				String typeValue = null;
				
				int wishedModifiers = 0 | Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;
				Field[] fields = mClass.getDeclaredFields();
				for(Field f : fields) {
					if(	f.getModifiers() == wishedModifiers &&
						f.getType() == String.class &&
						f.getName().equals("TYPE")) {
						typeValue = ((String) f.get(null)).toUpperCase();
						break;
					}
				}
				if(typeValue == null)
					continue;
				
				Type[] interfaces = mClass.getGenericInterfaces();
				for(Type t : interfaces) {
					if(t.getTypeName().equals("org.rge.assets.io.InputGen"))
						registerInputGenClass(typeValue, (Class<? extends InputGen>) mClass);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		cl.close();
		jar.close();
		
	}
	
	public AssetManager() {
		
		inputGens = new ArrayList<>();
		
		modelLoader = new ModelLoader();
		
	}
	
	public void loadModel(Model model) {
		
		modelLoader.loadModelVerts(model);
		
		
		
	}
	
	public void registerInputGen(String type, String path) {
		
		type = type.toUpperCase();
		
		InputGenClassContainer container = inputGenClasses.get(type);
		System.out.println(container);
		if(container == null)
			return;
		
		InputGen inputGen = container.constructInputGen();
		inputGen.init(path);
		inputGens.add(inputGen);
		
	}
	
	public InputStream getAsset(String path) {
		path = sanitizeAssetPath(path);
		return getAssetSanitized(path);
	}
	
	private InputStream getAssetSanitized(String path) {
		
		InputStream res = null;
		for(int i = 0; i < inputGens.size(); i++) {
			res = inputGens.get(i).getInput(path);
			if(res != null)
				return res;
		}
		
		return null;
	}
	
	public Shader getShader(String shaderPath) throws IOException {
		
		shaderPath = sanitizeAssetPath(shaderPath);
		
		Shader shader = shaders.get(shaderPath);
		if(shader != null)
			return shader;
		
		InputStream vertIn = getAssetSanitized(shaderPath + ".VERT");
		InputStream fragIn = getAssetSanitized(shaderPath + ".FRAG");
		if(vertIn == null || fragIn == null)
			return null;
		
		String vertSource = getStreamAsString(vertIn);
		vertIn.close();
		String fragSource = getStreamAsString(fragIn);
		fragIn.close();
		
		return new Shader(vertSource, fragSource);
	}
	
	public void destroy() {
		
		modelLoader.destroy();
		
	}
	
	public static String sanitizeAssetPath(String path) {
		return path.replaceAll("\\\\", "/").toUpperCase();
	}
	
	public static String getStreamAsString(InputStream in) throws IOException {
		
		StringBuilder str = new StringBuilder();
		
		byte[] buffer = new byte[4096];
		int len;
		while((len = in.read(buffer)) != -1)
			str.append(new String(buffer, 0, len));
		
		return str.toString();
	}
	
}
