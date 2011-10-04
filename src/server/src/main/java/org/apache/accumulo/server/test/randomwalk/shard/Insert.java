package org.apache.accumulo.server.test.randomwalk.shard;

import java.util.HashSet;
import java.util.Properties;
import java.util.Random;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.server.test.randomwalk.State;
import org.apache.accumulo.server.test.randomwalk.Test;


public class Insert extends Test {

	static final int NUM_WORDS = 100000;
	static final int MIN_WORDS_PER_DOC = 10;
	static final int MAX_WORDS_PER_DOC = 3000;
	
	@Override
	public void visit(State state, Properties props) throws Exception {
		String indexTableName = (String)state.get("indexTableName");
		String dataTableName = (String)state.get("docTableName");
		int numPartitions = (Integer)state.get("numPartitions");
		Random rand = (Random) state.get("rand");
		long nextDocID = (Long)state.get("nextDocID");
		
		insertRandomDocument(nextDocID++, state, indexTableName, dataTableName, numPartitions, rand);
		
		state.set("nextDocID", new Long(nextDocID));
	}

	private void insertRandomDocument(long did, State state, String indexTableName,
			String dataTableName, int numPartitions, Random rand)
			throws TableNotFoundException, Exception, AccumuloException,
			AccumuloSecurityException {
		String doc = createDocument(rand);
		
		String docID = new StringBuilder(String.format("%016x", did)).reverse().toString();
		
		saveDocument(state.getMultiTableBatchWriter().getBatchWriter(dataTableName), docID, doc);
		indexDocument(state.getMultiTableBatchWriter().getBatchWriter(indexTableName), doc, docID, numPartitions);
		
		log.debug("Inserted document "+docID);
	}

	private void saveDocument(BatchWriter bw, String docID, String doc) throws Exception {
		
		Mutation m = new Mutation(docID);
		m.put("doc", "", doc);
		
		bw.addMutation(m);
	}

	private String createDocument(Random rand) {
		StringBuilder sb = new StringBuilder();
		
		int numWords = rand.nextInt(MAX_WORDS_PER_DOC - MIN_WORDS_PER_DOC) + MIN_WORDS_PER_DOC;
		
		for(int i = 0; i < numWords; i++){
			String word = generateRandomWord(rand);
			
			if(i > 0)
				sb.append(" ");
			
			sb.append(word);
		}
		
		return sb.toString();
	}

	static String generateRandomWord(Random rand) {
		return Integer.toString(rand.nextInt(NUM_WORDS), Character.MAX_RADIX);
	}

	static String genPartition(int partition){
		return String.format("%06x", Math.abs(partition));
	}
	
	static void indexDocument(BatchWriter bw, String doc, String docId, int numPartitions) throws Exception {
		indexDocument(bw, doc, docId, numPartitions, false);
	}
	
	static void unindexDocument(BatchWriter bw, String doc, String docId, int numPartitions) throws Exception {
		indexDocument(bw, doc, docId, numPartitions, true);
	}
	
	static void indexDocument(BatchWriter bw, String doc, String docId, int numPartitions, boolean delete) throws Exception {
		
		String[] tokens = doc.split("\\W+");
		
		String partition = genPartition(doc.hashCode() % numPartitions);
		
		Mutation m = new Mutation(partition);
		
		HashSet<String> tokensSeen = new HashSet<String>();
		
		for (String token : tokens) {
			token = token.toLowerCase();
			
			if(!tokensSeen.contains(token)){
				tokensSeen.add(token);
				if(delete)
					m.putDelete(token, docId);
				else
					m.put(token, docId, new Value(new byte[0]));
			}
		}
		
		if(m.size() > 0)
			bw.addMutation(m);
	}
	
}