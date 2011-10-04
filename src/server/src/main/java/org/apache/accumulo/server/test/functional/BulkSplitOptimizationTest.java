package org.apache.accumulo.server.test.functional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.accumulo.core.conf.Property;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.accumulo.core.util.UtilWaitThread;
import org.apache.accumulo.server.test.CreateMapFiles;
import org.apache.accumulo.server.test.VerifyIngest;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;


/**
 * This test verifies that when a lot of files are
 * bulk imported into a table with one tablet and
 * then splits that not all map files go to the children
 * tablets.
 * 
 * 
 *
 */

public class BulkSplitOptimizationTest extends FunctionalTest {

	private static final String TABLE_NAME = "test_ingest";

	@Override
	public void cleanup() throws Exception {
		FileSystem fs = FileSystem.get(CachedConfiguration.getInstance());
		fs.delete(new Path("/testmf"), true);
		fs.delete(new Path("/testmf_failures"), true);
	}

	@Override
	public Map<String, String> getInitialConfig() {
		return parseConfig(Property.TSERV_MAJC_DELAY+"=0");
	}

	@Override
	public List<TableSetup> getTablesToCreate() {
		return Collections.singletonList(new TableSetup(TABLE_NAME,
				parseConfig(Property.TABLE_MAJC_RATIO+"=100",
						Property.TABLE_SPLIT_THRESHOLD+"=1G")));
	}

	@Override
	public void run() throws Exception {
		
		FileSystem fs = FileSystem.get(CachedConfiguration.getInstance());
		fs.delete(new Path("/testmf"), true);
		
		CreateMapFiles.main(new String[] {"testmf","8","0","100000", "99"});

		bulkImport(fs, TABLE_NAME, "/testmf");
		
		checkSplits(TABLE_NAME, 0, 0);
		checkMapFiles(TABLE_NAME, 1, 1, 100, 100);
		
		//initiate splits
		getConnector().tableOperations().setProperty(TABLE_NAME, Property.TABLE_SPLIT_THRESHOLD.getKey(), "100K");
		
		UtilWaitThread.sleep(1000);
		
		//wait until over split threshhold
		while(getConnector().tableOperations().getSplits(TABLE_NAME).size() < 50){
			UtilWaitThread.sleep(500);
		}
		
		checkSplits(TABLE_NAME, 50, 100);
		
		VerifyIngest.main(new String[] {"-timestamp","1","-size","50","-random","56","100000","0","1"});
		
		//ensure each tablet does not have all map files
		checkMapFiles(TABLE_NAME, 50, 100, 2, 4);
	}
}