package org.apache.accumulo.server.test.randomwalk.shard;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.accumulo.core.Constants;
import org.apache.accumulo.core.client.BatchScanner;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.server.test.randomwalk.State;
import org.apache.accumulo.server.test.randomwalk.Test;
import org.apache.hadoop.io.Text;


/**
 * Delete all documents containing a particular word.
 *
 */

public class DeleteWord extends Test {

	@Override
	public void visit(State state, Properties props) throws Exception {
		String indexTableName = (String)state.get("indexTableName");
		String docTableName = (String)state.get("docTableName");
		int numPartitions = (Integer)state.get("numPartitions");
		Random rand = (Random) state.get("rand");
		
		String wordToDelete = Insert.generateRandomWord(rand);
		
		//use index to find all documents containing word
		Scanner scanner = state.getConnector().createScanner(indexTableName, Constants.NO_AUTHS);
		scanner.fetchColumnFamily(new Text(wordToDelete));
		
		ArrayList<Range> documentsToDelete = new ArrayList<Range>();
		
		for (Entry<Key, Value> entry : scanner) 
			documentsToDelete.add(new Range(entry.getKey().getColumnQualifier()));
		
		if(documentsToDelete.size() > 0){
			//use a batch scanner to fetch all documents
			BatchScanner bscanner = state.getConnector().createBatchScanner(docTableName, Constants.NO_AUTHS, 8);
			bscanner.setRanges(documentsToDelete);
			
			BatchWriter ibw = state.getMultiTableBatchWriter().getBatchWriter(indexTableName);
			BatchWriter dbw = state.getMultiTableBatchWriter().getBatchWriter(docTableName);
			
			int count = 0;
			
			for (Entry<Key, Value> entry : bscanner){
				String docID = entry.getKey().getRow().toString();
				String doc = entry.getValue().toString();
				
				Insert.unindexDocument(ibw, doc, docID, numPartitions);
				
				Mutation m = new Mutation(docID);
				m.putDelete("doc", "");
				
				dbw.addMutation(m);
				count++;
			}
			
			bscanner.close();
			
			state.getMultiTableBatchWriter().flush();
			
			if(count != documentsToDelete.size()){
				throw new Exception("Batch scanner did not return expected number of docs "+count+" "+documentsToDelete.size());
			}
		}
		
		log.debug("Deleted "+documentsToDelete.size()+" documents containing "+wordToDelete);
	}

}