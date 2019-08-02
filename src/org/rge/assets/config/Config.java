package org.rge.assets.config;

import java.util.HashMap;

public class Config {
	
	public ConfigNode baseNode;
	public String pathDelimiterRegex = "/";
	
	protected String[] splitPath(String path) {
		return path.split(pathDelimiterRegex);
	}
	
	public ConfigNode getNode(String path) {
		
		ConfigNode res = baseNode;
		
		String[] nodeNames = splitPath(path);
		for(int i = 0; i < nodeNames.length; i++) {
			res = res.getSubNode(nodeNames[i]);
			if(res == null)
				return null;
		}
		
		return res;
	}
	
	public ConfigNode getNodeFromValuePath(String path) {
		
		ConfigNode res = baseNode;
		
		String[] nodeNames = splitPath(path);
		if(nodeNames.length < 2)
			return baseNode;
		
		for(int i = 0; i < nodeNames.length-1; i++) {
			res = res.getSubNode(nodeNames[i]);
			if(res == null)
				return null;
		}
		
		return res;
	}
	
	public ConfigNode getBaseNode() {
		return baseNode;
	}
	
	public static class ConfigNode {
		
		public String name;
		public ConfigNode parent;
		public HashMap<String, ConfigNode> subNodes;
		public HashMap<String, String> values;
		
		public ConfigNode(String name, ConfigNode parent, HashMap<String, ConfigNode> subNodes, HashMap<String, String> values) {
			
			this.name = name;
			this.parent = parent;
			if(subNodes != null)
				this.subNodes = subNodes;
			else
				this.subNodes = new HashMap<String, Config.ConfigNode>();
			
			if(values != null)
				this.values = values;
			else
				this.values = new HashMap<String, String>();
			
		}
		
		public ConfigNode getSubNode(String identifier) {
			return subNodes.get(identifier);
		}
		
		public String getValue(String identifier) {
			return values.get(identifier);
		}
		
		public String getOptValue(String identifier, String alt) {
			String val = values.get(identifier);
			if(val == null)
				return alt;
			else
				return val;
		}
		
		public int getOptInt(String identifier, int alt) {
			
			try {
				String val = values.get(identifier);
				if(val == null)
					return alt;
				int res = Integer.parseInt(val);
				return res;
			} catch (Exception e) {}
			
			return alt;
		}
		
		public double getOptDouble(String identifier, double alt) {
			
			try {
				String val = values.get(identifier);
				if(val == null)
					return alt;
				double res = Double.parseDouble(val);
				return res;
			} catch (Exception e) {}
			
			return alt;
		}
		
		public void setValue(String path, String value) {
			
			
			
		}
		
	}
	
}
