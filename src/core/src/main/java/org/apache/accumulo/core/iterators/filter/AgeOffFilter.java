package org.apache.accumulo.core.iterators.filter;

import java.util.Map;
import java.util.TreeMap;

import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.iterators.OptionDescriber;


public class AgeOffFilter implements Filter, OptionDescriber {
	private long threshold;
	private long currentTime;
	
	@Override
	public boolean accept(Key k, Value v) {
		if (currentTime - k.getTimestamp() > threshold)
			return false;
		return true;
	}
	
	@Override
	public void init(Map<String, String> options) {
		threshold = -1;
		if (options == null)
			throw new IllegalArgumentException("ttl must be set for AgeOffFilter");		

		String ttl = options.get("ttl");
		if (ttl == null)
			throw new IllegalArgumentException("ttl must be set for AgeOffFilter");		
		
		threshold = Long.parseLong(ttl);
		
		String time = options.get("currentTime");
		if (time != null)
			currentTime = Long.parseLong(time);
		else
			currentTime = System.currentTimeMillis();
		
		// add sanity checks for threshold and currentTime?
	}
	
	@Override
	public IteratorOptions describeOptions() {
	    Map<String, String> options = new TreeMap<String, String>();
	    options.put("ttl","time to live (milliseconds)");
	    options.put("currentTime", "if set, use the given value as the absolute time in milliseconds as the current time of day");
		return new IteratorOptions("ageoff","AgeOffFilter removes entries with timestamps more than <ttl> milliseconds old",
				options,null);
	}

	@Override
	public boolean validateOptions(Map<String, String> options) {
		Long.parseLong(options.get("ttl"));
		return true;
	}
}