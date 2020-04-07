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
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.rge.assets.config.Config;
import org.rge.assets.io.InputGen;
import org.rge.assets.models.Model;
import org.rge.assets.models.Model.RawData;
import org.rge.assets.models.Texture;
import org.rge.assets.models.Texture.TextureRawInfo;
import org.rge.graphics.Shader;
import org.rge.lua.EngineObject;
import org.rge.lua.EngineReference;

public class AssetManager implements EngineObject {
	
	EngineReference engReference;
	
	private HashMap<String, Shader> shaders;
	
	private GLLoader glLoader;
	
	private ArrayList<InputGen> inputGens;
	private HashMap<LoaderTAR, Loader> loadersTAR;
	private HashMap<Class<?>, ArrayList<Loader>> loadersC;
	
	private static HashMap<String, ClassContainer> inputGenClasses = new HashMap<>();
	private static HashMap<LoaderTAR, ClassContainer> loaderClasses = new HashMap<>();
	
	public static <T> void registerClass(T type, HashMap<T, ClassContainer> map, Class<?> genClass) {
		
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
							typeValue = typeValue.toUpperCase();
							registerClass(typeValue, inputGenClasses, mClass);
							continue;
						}
					}
					
					if(t.getTypeName().equals("org.rge.assets.Loader")) {
						
						String[] typeValues = null;
						int wishedModifiers = 0 | Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;
						Field[] fields = mClass.getDeclaredFields();
						for(Field f : fields) {
							if(	f.getModifiers() == wishedModifiers &&
								f.getType() == String[].class &&
								f.getName().equals("LOADER_TYPES")) {
								typeValues = (String[]) f.get(null);
								break;
							}
						}
						
						Class<?>[] returnValues = null;
						wishedModifiers = 0 | Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;
						for(Field f : fields) {
							if(	f.getModifiers() == wishedModifiers &&
								f.getType() == Class[].class &&
								f.getName().equals("LOADER_RETURN_TYPES")) {
								returnValues = (Class[]) f.get(null);
								break;
							}
						}
						if(typeValues == null || returnValues == null)
							continue;
						if(typeValues.length != returnValues.length)
							continue;
						
						for(int i = 0; i < typeValues.length; i++) {
							String loaderType = typeValues[i].toUpperCase();
							LoaderTAR key = new LoaderTAR(loaderType, returnValues[i]);
							registerClass(key, loaderClasses, mClass);
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
		public Object constructInstance() {
			try {
				return mClass.getConstructors()[constructorIndex].newInstance();
			} catch (Exception e) {
				System.err.println("Failed to create instance of: " + mClass);
				e.printStackTrace();
			}
			return null;
		}
	}
	
	private static class LoaderTAR {
		String type;
		Class<?> returnType;
		public LoaderTAR(String type, Class<?> returnType) {
			this.type = type;
			this.returnType = returnType;
		}
		@Override
		public boolean equals(Object obj) {
			if(obj == null) return false;
			if(this == obj) return true;
			if(!(obj instanceof LoaderTAR))
				return false;
			LoaderTAR key = (LoaderTAR) obj;
			return key.type.equals(type) && (key.returnType == returnType);
		}
		@Override
		public int hashCode() {
			return (type.hashCode() + returnType.hashCode());
		}
	}
	
	public AssetManager() {
		
		initLuaTable();
		
		inputGens = new ArrayList<>();
		shaders = new HashMap<>();
		glLoader = new GLLoader();
		
		loadersTAR = new HashMap<>();
		loadersC = new HashMap<>();
		for(LoaderTAR key : loaderClasses.keySet()) {
			System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ " + key.type + " " + key.returnType.getCanonicalName());
			Loader loader = (Loader) loaderClasses.get(key).constructInstance();
			System.out.println("Constructing a new loader instane for type: " + key + " " + loader);
			loader.init(this);
			loadersTAR.put(key, loader);
			ArrayList<Loader> list = loadersC.get(key.returnType);
			if(list == null) {
				list = new ArrayList<>();
				loadersC.put(key.returnType, list);
			}
			list.add(loader);
		}
		
	}
	
	public Object getWithClass(String path, Class<?> rClass) {
		if(rClass == null || path == null)
			return null;
		
		ArrayList<Loader> loaders = loadersC.get(rClass);
		if(loaders == null)
			return null;
		
		Loader loader = null;
		for(Loader l : loaders) {
			if(l.canRead(path)) {
				loader = l;
				break;
			}
		}
		Object res = loader.get(path, this);
		if(res == null)
			return null;
		if(res.getClass() != rClass)
			return null;
		return res;
	}
	
	public void loadModel(Model model) {
		
		glLoader.loadModel(model);
		
	
	}
	// TODO: remove the need from functions to supply the type of a file for mounting and asset loading
	
	public Texture loadTexture(BufferedImage img) {
		if(img == null)
			return null;
		
		Texture res = new Texture();
		res.animated = false;
		res.runlen = 1;
		res.texIds = new int[] { glLoader.loadTexture(img) };
		
		return res;
	}
	
	public Texture loadTexture(TextureRawInfo info) {
		if(info == null)
			return null;
		
		Texture res = new Texture();
		int len = 0;
		if(info.animated)
			len = (info.files.length > info.times.length ? info.files.length : info.times.length);
		else
			len = 1;
		
		res.animated = info.animated;
		res.runlen = info.runlen;
		
		res.texIds = new int[len];
		for(int i = 0; i < len; i++) {
			BufferedImage img = getTextureRaw(info.files[i]);
			if(img == null)
				res.texIds[i] = -1;
			else
				res.texIds[i] = glLoader.loadTexture(img);
		}
		
		if(res.animated) {
			res.times = new float[len];
			System.arraycopy(info.times, 0, res.times, 0, len);
		}
		
		return res;
	}
	
	public Object getValueOfSub(String path, Class<?>... classes) {
		if(path == null)
			return null;
		if(classes == null)
			return null;
		
		path = sanitizeAssetPath(path);
		
		LoaderTAR mKey = null;
		for(LoaderTAR key : loadersTAR.keySet()) {
			if(isSubOfAny(key.returnType, classes) && loadersTAR.get(key).canRead(path)) {
				mKey = key;
				break;
			}
		}
		if(mKey != null)
			return loadersTAR.get(mKey).get(path, this);
		return null;
	}
	
	private static boolean isSubOfAny(Class<?> t, Class<?>[] ta) {
		for(int i = 0; i < ta.length; i++) {
			if(ta[i].isAssignableFrom(t))
				return true;
		}
		return false;
	}
	
	public RawData getModelRawData(String type, String path) {
		if(type == null || path == null)
			return null;
		path = sanitizeAssetPath(path);
		type = type.toUpperCase();
		LoaderTAR key = new LoaderTAR(type, RawData.class);
		
		Loader loader = loadersTAR.get(key);
		if(loader == null)
			return null;
		
		return (RawData) loader.get(path, this);
	}
	
	public BufferedImage getTextureRaw(String type, String path) {
		if(type == null || path == null)
			return null;
		type = type.toUpperCase();
		LoaderTAR key = new LoaderTAR(type, BufferedImage.class);
		
		Loader loader = loadersTAR.get(key);
		if(loader == null)
			return null;
		
		return (BufferedImage) loader.get(path, this);
	}
	
	public Config getConfig(String type, String path) {
		if(type == null || path == null)
			return null;
		type = type.toUpperCase();
		LoaderTAR key = new LoaderTAR(type, Config.class);
		
		Loader loader = loadersTAR.get(key);
		if(loader == null)
			return null;
		return (Config) loader.get(path, this);
	}
	
	public void registerInputGen(String type, String path) {
		
		type = type.toUpperCase();
		
		ClassContainer container = inputGenClasses.get(type);
		System.out.println("Container: " + container);
		if(container == null)
			return;
		
		InputGen inputGen = (InputGen) container.constructInstance();
		boolean succes = inputGen.init(path);
		if(succes)
			inputGens.add(inputGen);
		else
			inputGen.destroy();
	}
	
	public boolean exists(String path) {
		for(InputGen gen : inputGens)
			if(gen.exists(path))
				return true;
		return false;
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
	
	public RawData getModelRawData(String path) {
		return (RawData) getWithClass(path, RawData.class);
	}
	
	public BufferedImage getTextureRaw(String path) {
		return (BufferedImage) getWithClass(path, BufferedImage.class);
	}
	
	public Config getConfig(String path) {
		return (Config) getWithClass(path, Config.class);
	}
	
	@Override
	public EngineReference getEngineReference() {
		return engReference;
	}
	
	private void initLuaTable() {
		engReference = new EngineReference(this);
		engReference.set("getShader", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				if(arg0 instanceof LuaString) {
					String str = arg0.checkjstring();
					Shader shader = null;
					try {
						shader = getShader(str);
					} catch (IOException e) {
						e.printStackTrace();
					}
					if(shader != null)
						return shader.getEngineReference();
				}
				return NIL;
			}
		});
	}
	
	public GLLoader getGLLoader() {
		return glLoader;
	}
	
}
