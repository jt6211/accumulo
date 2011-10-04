package org.apache.accumulo.core.iterators;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.apache.accumulo.core.data.ByteSequence;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.PartialKey;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.util.TextUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.hadoop.io.Text;
import org.apache.log4j.Logger;


public class IntersectingIterator implements SortedKeyValueIterator<Key,Value> {
	
    protected Text nullText = new Text();
    
	protected Text getPartition(Key key) {
		return key.getRow();
	}
	
	protected Text getTerm(Key key) {
		return key.getColumnFamily();
	}
	
	protected Text getDocID(Key key) {
		return key.getColumnQualifier();
	}
	
	protected Key buildKey(Text partition, Text term) {
	    return new Key(partition,(term == null) ? nullText : term);
	}
	
	protected Key buildKey(Text partition, Text term, Text docID) {
	    return new Key(partition,(term == null) ? nullText : term, docID);
	}
	
	protected Key buildFollowingPartitionKey(Key key) {
		return key.followingKey(PartialKey.ROW);
	}
	
    protected static final Logger log = Logger.getLogger(IntersectingIterator.class); 
	
	protected static class TermSource {
		public SortedKeyValueIterator<Key,Value> iter;
		public Text term;
		public boolean notFlag;
		
		public TermSource(TermSource other) {
            this.iter = other.iter;
            this.term = other.term;
            this.notFlag = other.notFlag;
        }
		
		public TermSource(SortedKeyValueIterator<Key,Value> iter, Text term) {
			this.iter = iter;
			this.term = term;
			this.notFlag = false;
		}
		public TermSource(SortedKeyValueIterator<Key,Value> iter, Text term, boolean notFlag) {
			this.iter = iter;
			this.term = term;
			this.notFlag = notFlag;
		}
		
		public String getTermString() {
	        return (this.term == null) ? new String("Iterator") : this.term.toString();
	    }
	}
	
	TermSource[] sources;
	int sourcesCount = 0;

	Range overallRange;
	
	// query-time settings
	Text currentPartition = null;
	Text currentDocID = new Text(emptyByteArray);
	static final byte [] emptyByteArray = new byte[0];
	
	Key topKey = null;
	Value value = new Value(emptyByteArray);

	protected Collection<ByteSequence> seekColumnFamilies;

	protected boolean inclusive;
	

	public IntersectingIterator()
	{}
	
	@Override
	public SortedKeyValueIterator<Key, Value> deepCopy(IteratorEnvironment env) {
		return new IntersectingIterator(this, env);
	}
	
    public IntersectingIterator(IntersectingIterator other, IteratorEnvironment env)
	{
		if(other.sources != null)
		{
		    sourcesCount = other.sourcesCount;
			sources = new TermSource[sourcesCount];
			for(int i = 0; i < sourcesCount; i++)
			{
				sources[i] = new TermSource(other.sources[i].iter.deepCopy(env), other.sources[i].term);
			}
		}
	}

	@Override
	public Key getTopKey() {
		return topKey;
	}

	@Override
	public Value getTopValue() {
		// we don't really care about values
		return value;
	}

	@Override
	public boolean hasTop() {
		return currentPartition != null;
	}

	// precondition: currentRow is not null
	private boolean seekOneSource(int sourceID) throws IOException
	{
		// find the next key in the appropriate column family that is at or beyond the cursor (currentRow, currentCQ)
		// advance the cursor if this source goes beyond it
		// return whether we advanced the cursor

		// within this loop progress must be made in one of the following forms:
		// - currentRow or currentCQ must be increased
		// - the given source must advance its iterator
		// this loop will end when any of the following criteria are met
		// - the iterator for the given source is pointing to the key (currentRow, columnFamilies[sourceID], currentCQ)
		// - the given source is out of data and currentRow is set to null
		// - the given source has advanced beyond the endRow and currentRow is set to null
		boolean advancedCursor = false;

		if (sources[sourceID].notFlag)
		{
			while(true)
			{
				if(sources[sourceID].iter.hasTop() == false)
				{
					// an empty column that you are negating is a valid condition
					break;
				}
				// check if we're past the end key
				int endCompare = -1;
				// we should compare the row to the end of the range
				if(overallRange.getEndKey() != null)
				{
					endCompare = overallRange.getEndKey().getRow().compareTo(sources[sourceID].iter.getTopKey().getRow());
					if((!overallRange.isEndKeyInclusive() && endCompare <= 0) || endCompare < 0)
					{
						// an empty column that you are negating is a valid condition
						break;
					}
				}
				int partitionCompare = currentPartition.compareTo(getPartition(sources[sourceID].iter.getTopKey()));
				// check if this source is already at or beyond currentRow
				// if not, then seek to at least the current row

				if(partitionCompare > 0)
				{
					// seek to at least the currentRow
					Key seekKey = buildKey(currentPartition,sources[sourceID].term);
					sources[sourceID].iter.seek(new Range(seekKey,true, null, false), seekColumnFamilies, inclusive);
					continue;
				}
				// check if this source has gone beyond currentRow
				// if so, this is a valid condition for negation
				if(partitionCompare < 0)
				{
					break;
				}
				// we have verified that the current source is positioned in currentRow
				// now we must make sure we're in the right columnFamily in the current row
                // Note: Iterators are auto-magically set to the correct columnFamily
                if(sources[sourceID].term != null)
                {
    				int termCompare = sources[sourceID].term.compareTo(getTerm(sources[sourceID].iter.getTopKey()));
    				// check if this source is already on the right columnFamily
    				// if not, then seek forwards to the right columnFamily
    				if(termCompare > 0)
    				{
    					Key seekKey = buildKey(currentPartition,sources[sourceID].term,currentDocID);
    					sources[sourceID].iter.seek(new Range(seekKey,true,null,false), seekColumnFamilies, inclusive);
    					continue;
    				}
    				// check if this source is beyond the right columnFamily
    				// if so, then this is a valid condition for negating
    				if(termCompare < 0)
    				{
    					break;
    				}
                }
				
				// we have verified that we are in currentRow and the correct column family
				// make sure we are at or beyond columnQualifier
				Text docID = getDocID(sources[sourceID].iter.getTopKey());
				int docIDCompare = currentDocID.compareTo(docID);
				// If we are past the target, this is a valid result
				if(docIDCompare < 0)
				{
					break;
				}
				// if this source is not yet at the currentCQ then advance in this source
				if(docIDCompare > 0)
				{
					// seek forwards
					Key seekKey = buildKey(currentPartition, sources[sourceID].term, currentDocID);
					sources[sourceID].iter.seek(new Range(seekKey, true, null, false), seekColumnFamilies, inclusive);
					continue;
				}
				// if we are equal to the target, this is an invalid result.
				// Force the entire process to go to the next row.
				// We are advancing column 0 because we forced that column to not contain a !
				//    when we did the init()
				if(docIDCompare == 0)
				{
					sources[0].iter.next();
					advancedCursor = true;
					break;
				}	
			}
		}
		else
		{
			while(true)
			{
				if(sources[sourceID].iter.hasTop() == false)
				{
					currentPartition = null;
					// setting currentRow to null counts as advancing the cursor
					return true;
				}
				// check if we're past the end key
				int endCompare = -1;
				// we should compare the row to the end of the range

				if(overallRange.getEndKey() != null)
				{
					endCompare = overallRange.getEndKey().getRow().compareTo(sources[sourceID].iter.getTopKey().getRow());
					if((!overallRange.isEndKeyInclusive() && endCompare <= 0) || endCompare < 0)
					{
						currentPartition = null;
						// setting currentRow to null counts as advancing the cursor
						return true;
					}
				}
				int partitionCompare = currentPartition.compareTo(getPartition(sources[sourceID].iter.getTopKey()));
				// check if this source is already at or beyond currentRow
				// if not, then seek to at least the current row
				if(partitionCompare > 0)
				{
					// seek to at least the currentRow
					Key seekKey = buildKey(currentPartition,sources[sourceID].term);
					sources[sourceID].iter.seek(new Range(seekKey,true, null, false), seekColumnFamilies, inclusive);
					continue;
				}
				// check if this source has gone beyond currentRow
				// if so, advance currentRow
				if(partitionCompare < 0)
				{
					currentPartition.set(getPartition(sources[sourceID].iter.getTopKey()));
					currentDocID.set(emptyByteArray);
					advancedCursor = true;
					continue;
				}
				// we have verified that the current source is positioned in currentRow
				// now we must make sure we're in the right columnFamily in the current row
                // Note: Iterators are auto-magically set to the correct columnFamily

                if(sources[sourceID].term != null)
                {
    				int termCompare = sources[sourceID].term.compareTo(getTerm(sources[sourceID].iter.getTopKey()));
    				// check if this source is already on the right columnFamily
    				// if not, then seek forwards to the right columnFamily
    				if(termCompare > 0)
    				{
    					Key seekKey = buildKey(currentPartition,sources[sourceID].term,currentDocID);
    					sources[sourceID].iter.seek(new Range(seekKey,true,null,false), seekColumnFamilies, inclusive);
    					continue;
    				}
    				// check if this source is beyond the right columnFamily
    				// if so, then seek to the next row
    				if(termCompare < 0)
    				{
					    // we're out of entries in the current row, so seek to the next one
                        //              byte[] currentRowBytes = currentRow.getBytes();
                        //              byte[] nextRow = new byte[currentRowBytes.length + 1];
                        //              System.arraycopy(currentRowBytes, 0, nextRow, 0, currentRowBytes.length);
                        //              nextRow[currentRowBytes.length] = (byte)0;
                        //              // we should reuse text objects here
                        //              sources[sourceID].seek(new Key(new Text(nextRow),columnFamilies[sourceID]));
    					if(endCompare == 0)
    					{
    						// we're done
    						currentPartition = null;
    						// setting currentRow to null counts as advancing the cursor
    						return true;
    					}
    					Key seekKey = buildFollowingPartitionKey(sources[sourceID].iter.getTopKey());
    					sources[sourceID].iter.seek(new Range(seekKey, true, null, false), seekColumnFamilies, inclusive);
    					continue;
    				}
                }
				// we have verified that we are in currentRow and the correct column family
				// make sure we are at or beyond columnQualifier
				Text docID = getDocID(sources[sourceID].iter.getTopKey());
				int docIDCompare = currentDocID.compareTo(docID);
				// if this source has advanced beyond the current column qualifier then advance currentCQ and return true
				if(docIDCompare < 0)
				{
					currentDocID.set(docID);
					advancedCursor = true;
					break;
				}
				// if this source is not yet at the currentCQ then seek in this source
				if(docIDCompare > 0)
				{
					// seek forwards
					Key seekKey = buildKey(currentPartition, sources[sourceID].term, currentDocID);
					sources[sourceID].iter.seek(new Range(seekKey, true, null, false), seekColumnFamilies, inclusive);
					continue;
				}
				// this source is at the current row, in its column family, and at currentCQ
				break;
			}
		}
		return advancedCursor;
	}

	@Override
	public void next() throws IOException {
		if(currentPartition == null)
		{
			return;
		}
		// precondition: the current row is set up and the sources all have the same column qualifier
		// while we don't have a match, seek in the source with the smallest column qualifier
		sources[0].iter.next();
		advanceToIntersection();
	}
	
	protected void advanceToIntersection() throws IOException
	{
		boolean cursorChanged = true;
		while(cursorChanged)
		{
			// seek all of the sources to at least the highest seen column qualifier in the current row
			cursorChanged = false;
			for(int i = 0; i < sourcesCount; i++)
			{
				if(currentPartition == null)
				{
					topKey = null;
					return;
				}
				if(seekOneSource(i))
				{
					cursorChanged = true;
					break;
				}
			}
		}
		topKey = buildKey(currentPartition,nullText,currentDocID);
	}
	
	public static String stringTopKey(SortedKeyValueIterator<Key, Value> iter) {
		if (iter.hasTop())
			return iter.getTopKey().toString();
		return "";
	}
	
	public static final String columnFamiliesOptionName = "columnFamilies";
	public static final String notFlagOptionName = "notFlag";

	public static String encodeColumns(Text[] columns)
	{
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < columns.length; i++)
		{
			sb.append(new String(Base64.encodeBase64(TextUtil.getBytes(columns[i]))));
			sb.append('\n');
		}
		return sb.toString();
	}

	public static String encodeBooleans(boolean[] flags)
	{
		byte[] bytes = new byte[flags.length];
		for(int i = 0; i < flags.length; i++)
		{
			if(flags[i])
				bytes[i] = 1;
			else
				bytes[i] = 0;
		}
		return new String(Base64.encodeBase64(bytes));
	}
	
	public static Text[] decodeColumns(String columns)
	{
		String[] columnStrings = columns.split("\n");
		Text[] columnTexts = new Text[columnStrings.length];
		for(int i = 0; i < columnStrings.length; i++)
		{
			columnTexts[i] = new Text(Base64.decodeBase64(columnStrings[i].getBytes()));
		}
		return columnTexts;
	}

	public static boolean[] decodeBooleans(String flags)
	{
		// return null of there were no flags
		if(flags == null)
			return null;				
		
		byte[] bytes = Base64.decodeBase64(flags.getBytes());
		boolean[] bFlags = new boolean[bytes.length];
		for(int i = 0; i < bytes.length; i++)
		{
			if(bytes[i] == 1)
				bFlags[i] = true;
			else
				bFlags[i] = false;
		}
		return bFlags; 
	}
	
    @Override
	public void init(SortedKeyValueIterator<Key, Value> source,
			Map<String, String> options, IteratorEnvironment env) throws IOException {
		Text[] terms = decodeColumns(options.get(columnFamiliesOptionName));
		boolean[] notFlag = decodeBooleans(options.get(notFlagOptionName));

		if(terms.length < 2)
		{
			throw new IOException("IntersectionIterator requires two or more columns families");
		}

		// Scan the not flags.
		// There must be at least one term that isn't negated
		// And we are going to re-order such that the first term is not a ! term
		if(notFlag == null)
		{
			notFlag = new boolean[terms.length];
			for(int i = 0; i < terms.length; i++)
				notFlag[i] = false;
		}
		if(notFlag[0]) {
			for(int i = 1; i < notFlag.length; i++)
			{				
				if(notFlag[i] == false)
				{
					Text swapFamily = new Text(terms[0]);
					terms[0].set(terms[i]);
					terms[i].set(swapFamily);
					notFlag[0] = false;
					notFlag[i] = true;
					break;
				}
			}
			if(notFlag[0])
			{
				throw new IOException("IntersectionIterator requires at lest one column family without not");				
			}
		}
		
		
		sources = new TermSource[terms.length];
		sources[0] = new TermSource(source, terms[0]);
		for(int i = 1; i < terms.length; i++)
		{
			sources[i] = new TermSource(source.deepCopy(env), terms[i], notFlag[i]);
		}
		sourcesCount = terms.length;
	}

	@Override
	public void seek(Range range, Collection<ByteSequence> seekColumnFamilies, boolean inclusive) throws IOException {
		overallRange = new Range(range);
		currentPartition = new Text();
		currentDocID.set(emptyByteArray);
		
		this.seekColumnFamilies = seekColumnFamilies;
		this.inclusive = inclusive;
		
		// seek each of the sources to the right column family within the row given by key
		for(int i = 0; i < sourcesCount; i++)
		{
			Key sourceKey;
			if(range.getStartKey() != null)
			{
                if(range.getStartKey().getColumnQualifier() != null)
                {
                    sourceKey = buildKey(getPartition(range.getStartKey()),sources[i].term,range.getStartKey().getColumnQualifier());
                }
                else
                {
                    sourceKey = buildKey(getPartition(range.getStartKey()),sources[i].term);
                }
				sources[i].iter.seek(new Range(sourceKey, true, null, false), seekColumnFamilies, inclusive);
			}
			else
			{
				sources[i].iter.seek(range, seekColumnFamilies, inclusive);
			}
		}
		advanceToIntersection();
	}

    public void addSource(SortedKeyValueIterator<Key, Value> source, IteratorEnvironment env, 
            Text term, boolean notFlag) {
        // Check if we have space for the added Source
        if(sources == null)
        {
            sources = new TermSource[1];
        }
        else 
        {
            // allocate space for node, and copy current tree.
            // TODO:  Should we change this to an ArrayList so that we can just add() ?
            TermSource[] localSources = new TermSource[sources.length + 1];
            int currSource = 0;
            for(TermSource myTerm : sources)
            {
                // TODO:  Do I need to call new here? or can I just re-use the term?
                localSources[currSource] = new TermSource(myTerm);
                currSource++;
            }
            sources = localSources;
        }
        sources[sourcesCount] = new TermSource(source.deepCopy(env), term, notFlag);
        sourcesCount++; 
    }
}