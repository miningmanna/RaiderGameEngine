package org.rge.assets;

import java.awt.image.BufferedImage;
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

import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;
import org.newdawn.slick.opengl.Texture;
import org.rge.assets.io.InputGen;
import org.rge.assets.models.Model;
import org.rge.assets.models.Model.RawData;
import org.rge.graphics.Shader;
import org.rge.loaders.ModelRawDataLoader;
import org.rge.loaders.TextureRawLoader;
import org.rge.lua.EngineObject;
import org.rge.lua.EngineReference;
import org.rge.node.DrawNode;

public class AssetManager implements EngineObject {
	
	EngineReference engReference;
	
	private HashMap<String, Shader> shaders;
	
	private GLLoader glLoader;
	
	private ArrayList<InputGen> inputGens;
	private HashMap<String, ModelRawDataLoader> modelRawDataLoaders;
	private HashMap<String, TextureRawLoader> textureRawLoaders;
	
	private static HashMap<String, ClassContainer> inputGenClasses = new HashMap<>();
	private static HashMap<String, ClassContainer> modelRawDataLoaderClasses = new HashMap<>();
	private static HashMap<String, ClassContainer> textureRawLoaderClasses = new HashMap<>();
	public static void registerClass(String type, HashMap<String, ClassContainer> map, Class<?> genClass) {
		
		type = type.toUpperCase();
		
		ClassContainer container = new ClassContainer();
		container.mClass = genClass;
		
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
			System.err.println("Cant use Class: " + genClass);
			System.err.println("No constructor with 0 parameters");
			return;
		}
		
		System.out.println("Adding container: " + container + " as " + type);
		
		map.put(type, container);
		
	}
	
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
				
				
				Type[] interfaces = mClass.getGenericInterfaces();
				for(Type t : interfaces) {
					if(t.getTypeName().equals("org.rge.assets.io.InputGen")) {
						
						String typeValue = null;
						
						int wishedModifiers = 0 | Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;
						Field[] fields = mClass.getDeclaredFields();
						for(Field f : fields) {
							if(	f.getModifiers() == wishedModifiers &&
								f.getType() == String.class &&
								f.getName().equals("INPUTGEN_TYPE")) {
								typeValue = ((String) f.get(null)).toUpperCase();
								break;
							}
						}
						if(typeValue != null) {
							registerClass(typeValue, inputGenClasses, mClass);
							continue;
						}
					}
					
					// TODO: implement for loader interfaces
					
					if(t.getTypeName().equals("org.rge.loaders.ModelRawDataLoader")) {
						System.out.println("Found ModelRawDataLoader class! " + mClass);
						String[] typeValues = null;
						
						int wishedModifiers = 0 | Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;
						Field[] fields = mClass.getDeclaredFields();
						for(Field f : fields) {
							if(	f.getModifiers() == wishedModifiers &&
								f.getType() == String[].class &&
								f.getName().equals("MODELRAWDATALOADER_TYPES")) {
								typeValues = (String[]) f.get(null);
								break;
							}
						}
						System.out.println("TYPE_VALUES: " + typeValues);
						if(typeValues != null) {
							for(String loaderType : typeValues) {
								loaderType = loaderType.toUpperCase();
								System.out.println("Registering ModelRawDataLoader type: " + loaderType + " for class " + mClass);
								registerClass(loaderType, modelRawDataLoaderClasses, mClass);
							}
							continue;
						}
						
					}
					if(t.getTypeName().equals("org.rge.loaders.TextureRawLoader")) {
						System.out.println("Found TextureRawLoader class! " + mClass);
						String[] typeValues = null;
						
						int wishedModifiers = 0 | Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;
						Field[] fields = mClass.getDeclaredFields();
						for(Field f : fields) {
							if(	f.getModifiers() == wishedModifiers &&
								f.getType() == String[].class &&
								f.getName().equals("TEXTURERAWLOADER_TYPES")) {
								typeValues = (String[]) f.get(null);
								break;
							}
						}
						System.out.println("TYPE_VALUES: " + typeValues);
						if(typeValues != null) {
							for(String loaderType : typeValues) {
								loaderType = loaderType.toUpperCase();
								System.out.println("Registering TextureRawLoader type: " + loaderType + " for class " + mClass);
								registerClass(loaderType, textureRawLoaderClasses, mClass);
							}
							continue;
						}
						
					}
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		cl.close();
		jar.close();
		
	}
	
	private static class ClassContainer {
		public int constructorIndex;
		public Class<?> mClass;
		public Object constructInputGen() {
			try {
				return mClass.getConstructors()[constructorIndex].newInstance();
			} catch (Exception e) {
				System.err.println("Failed to create instance of: " + mClass);
				e.printStackTrace();
			}
			return null;
		}
	}
	
	public AssetManager() {
		
		initLuaTable();
		
		inputGens = new ArrayList<>();
		shaders = new HashMap<>();
		glLoader = new GLLoader();
		
		modelRawDataLoaders = new HashMap<>();
		for(String key : modelRawDataLoaderClasses.keySet()) {
			System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ " + key);
			ModelRawDataLoader loader = (ModelRawDataLoader) modelRawDataLoaderClasses.get(key).constructInputGen();
			System.out.println("Constructing a new loader instane for type: " + key + " " + loader);
			loader.init();
			modelRawDataLoaders.put(key, loader);
		}
		
		textureRawLoaders = new HashMap<>();
		for(String key : textureRawLoaderClasses.keySet()) {
			System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ " + key);
			TextureRawLoader loader = (TextureRawLoader) textureRawLoaderClasses.get(key).constructInputGen();
			System.out.println("Constructing a new loader instane for type: " + key + " " + loader);
			loader.init();
			textureRawLoaders.put(key, loader);
		}
		
	}
	
	public void loadModel(Model model) {
		
		glLoader.loadModel(model);
		
	
	}
	// TODO: remove the need from functions to supply the type of a file for mounting and asset loading
	
	public Texture loadTexture(BufferedImage img) {
		if(img == null)
			return null;
		return glLoader.loadTexture(img);
		
	}
	
	public RawData getModelRawData(String type, String path) {
		if(type == null || path == null)
			return null;
		type = type.toUpperCase();
		
		ModelRawDataLoader loader = modelRawDataLoaders.get(type);
		System.out.println("Getting modelLoader: " + loader);
		if(loader == null)
			return null;
		
		RawData res = loader.getModelRawData(path, this);
		System.out.println("Loader returned RawData: " + res);
		
		return res;
	}
	
	public void registerInputGen(String type, String path) {
		
		type = type.toUpperCase();
		
		ClassContainer container = inputGenClasses.get(type);
		System.out.println("Container: " + container);
		if(container == null)
			return;
		
		InputGen inputGen = (InputGen) container.constructInputGen();
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
		
		shaderPath = "SHADERS/" + sanitizeAssetPath(shaderPath);
		
		Shader shader = shaders.get(shaderPath);
		if(shader != null)
			return shader;
		
		InputStream vertIn = getAssetSanitized(shaderPath + ".VERT");
		InputStream fragIn = getAssetSanitized(shaderPath + ".FRAG");
		if(vertIn == null || fragIn == null) {
			System.out.println("Unable to get assets");
			return null;
		}
		
		String vertSource = getStreamAsString(vertIn);
		vertIn.close();
		String fragSource = getStreamAsString(fragIn);
		fragIn.close();
		
		return new Shader(vertSource, fragSource);
	}
	
	public void destroy() {
		
		for(InputGen inputGen : inputGens)
			inputGen.destroy();
		
		for(String key : shaders.keySet())
			shaders.get(key).destroy();
		
		glLoader.destroy();
		
	}
	
	public static String sanitizeAssetPath(String path) {
		return path.replaceAll("\\\\", "/");
	}
	
	public static String getStreamAsString(InputStream in) throws IOException {
		
		StringBuilder str = new StringBuilder();
		
		byte[] buffer = new byte[4096];
		int len;
		while((len = in.read(buffer)) != -1)
			str.append(new String(buffer, 0, len));
		
		return str.toString();
	}
	
	public BufferedImage getTextureRaw(String type, String path) {
		if(type == null || path == null)
			return null;
		type = type.toUpperCase();
		
		TextureRawLoader loader = textureRawLoaders.get(type);
		System.out.println("Getting modelLoader: " + loader);
		if(loader == null)
			return null;
		
		BufferedImage res = loader.getRawImage(path, this);
		System.out.println("Loader returned raw Image: " + res);
		
		return res;
	}
	
	public BufferedImage getTextureRaw(String path) {
		if(path == null)
			return null;
		String type = null;
		for(String key : textureRawLoaders.keySet()) {
			if(textureRawLoaders.get(key).canRead(path)) {
				type = key;
				break;
			}
		}
		if(type != null)
			return getTextureRaw(type, path);
		else
			return null;
		
	}
	
	@Override
	public EngineReference getEngineReference() {
		return engReference;
	}
	
	private void initLuaTable() {
		engReference = new EngineReference(this);
		
		engReference.set("registerInputGen", new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0, LuaValue arg1) {
				if(!(arg0 instanceof LuaString))
					return null;
				if(!(arg1 instanceof LuaString))
					return null;
				
				registerInputGen(arg0.checkjstring(), arg1.checkjstring());
				
				return null;
			}
		});
		
	}
	
}
