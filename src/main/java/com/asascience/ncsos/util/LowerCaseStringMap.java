package com.asascience.ncsos.util;
import java.util.HashMap;

public class LowerCaseStringMap  extends HashMap<String, Object>{
	@Override
    public Object put(String key, Object value) {
       return super.put(key.toLowerCase(), value);
    }

	@Override
	public boolean containsKey(Object key){
		String keyStr = ((String) key).toLowerCase();
		return super.containsKey(keyStr);
	}
	
	@Override
	public Object get(Object key){
		String keyStr = ((String) key).toLowerCase();
		return super.get(keyStr);
	}
}
