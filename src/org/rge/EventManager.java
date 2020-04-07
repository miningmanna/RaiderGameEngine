package org.rge;

import java.util.HashMap;

import org.luaj.vm2.LuaValue;

public class EventManager {
	
	private HashMap<String, EventHandler> handlers;
	
	public EventManager() {
		handlers = new HashMap<>();
	}
	
	public static class EventHandler {
		private LuaValue handler;
		public void fire(LuaValue... params)
		{
			if(handler != null)
				handler.invoke(params);
		}
	}
	
	public EventHandler register(String eventName) {
		EventHandler eh = handlers.get(eventName);
		if(eh == null) {
			eh = new EventHandler();
			handlers.put(eventName, eh);
		}
		return eh;
	}
	
	public void setHandlerCallback(String eventName, LuaValue callback) {
		EventHandler eh = handlers.get(eventName);
		if(eh == null)
			return;
		eh.handler = callback;
	}
	
}
