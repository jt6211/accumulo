package org.apache.accumulo.server.test.randomwalk.shard;

import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;

import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.server.test.randomwalk.State;
import org.apache.accumulo.server.test.randomwalk.Test;


public class Delete extends Test {

	@Override
	public void visit(State state, Properties props) throws Exception {
		String indexTableName = (String)state.get("indexTableName");
		String dataTableName = (String)state.get("docTableName");
		int numPartitions = (Integer)state.get("numPartitions");
		Random rand = (Random) state.get("rand");
		
		Entry<Key, Value> entry = Search.findRandomDocument(state, dataTableName, rand);
		if(entry == null)
			return;
		
		
		String docID = entry.getKey().getRow().toString();
		String doc = entry.getValue().toString();
		
		Insert.unindexDocument(state.getMultiTableBatchWriter().getBatchWriter(indexTableName), doc, docID, numPartitions);
		
		Mutation m = new Mutation(docID);
		m.putDelete("doc", "");
		
		state.getMultiTableBatchWriter().getBatchWriter(dataTableName).addMutation(m);
		
		log.debug("Deleted document "+docID);
		
		state.getMultiTableBatchWriter().flush();
	}

}