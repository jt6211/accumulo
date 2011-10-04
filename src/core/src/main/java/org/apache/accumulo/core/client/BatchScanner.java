package org.apache.accumulo.core.client;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.data.Value;


/**
 * Implementations of BatchScanner support efficient lookups 
 * of many ranges in accumulo.
 * 
 * Use this when looking up lots of ranges and you expect
 * each range to contain a small amount of data.  Also only use
 * this when you do not care about the returned data being
 * in sorted order.
 * 
 * If you want to lookup a few ranges and expect those
 * ranges to contain a lot of data, then use the Scanner
 * instead. Also, the Scanner will return data in sorted
 * order, this will not.
 */

public interface BatchScanner extends ScannerBase, Iterable<Entry<Key, Value>> {
	
	/**
	 * Allows scanning over multiple ranges efficiently.
	 * 
	 * @param ranges specifies the non-overlapping ranges to query
	 */
	void setRanges(Collection<Range> ranges);
	
	/**
     * Returns an iterator over a accumulo table.  This iterator uses the options
     * that are currently set for its lifetime.  So setting options will have no effect 
     * on existing iterators.
     * 
     * Keys returned by the iterator are not guaranteed to be in sorted order.
     */
    public Iterator<Entry<Key, Value>> iterator();
	
	/**
	 * Cleans up and finalizes the scanner
	 */
	void close();
}