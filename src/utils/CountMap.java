package utils;

import java.util.HashMap;
import java.util.Iterator;

/**
 * A {@link HashMap} extension that simplifies counting frequencies of objects via an {@link #increment(Object)} method
 * 
 * <p>This extensions always returns a non-null value from {@link #get(Object)}</p>  
 * 
 * @author Ori Garin
 */
public final class CountMap<T> extends HashMap<T, Integer> {
	private static final long serialVersionUID = -605183752005154193L;

	public CountMap() {
		super();
	}
	public CountMap(int size) {
		super(size);
	}

	/**
	 * puts the key into the map, incrementing its current value (sets its value to 1 if the key is new to the map)
	 * 
	 * @param key da key
	 * @return
	 */
	public final Integer increment(T key) {
		Integer i = super.get(key);
		return put(key, i == null ? 1 : i+1);
	}

	/**
	 * puts the key into the map, adding the given <code>quantity</code> its current value 
	 * (sets its value to <code>quantity</code> if the key is new to the map)
	 * 
	 * @param key da key
	 * @param quantity
	 * @return
	 */
	public final Integer add(T key, Integer quantity) {
		Integer i = super.get(key);
		return put(key, i == null ? quantity : i+quantity);
	}
	
	public Integer put(T key, Integer value) {
		if (value == 0)
			return remove(key);
		else
			return super.put(key, value);
	}
	
	@Override
	public Integer get(Object key) {
		Integer result = super.get(key);
		return result == null ? 0 : result;
	}
	
	public int allCounts()
	{
		
		int count = 0;
		Iterator<T> it = this.keySet().iterator();
		   while (it.hasNext()) {
			String key = (String) it.next();
			Integer val = this.get(key);
			count = count+val.intValue();
		   }
		   return count;
	}
}
