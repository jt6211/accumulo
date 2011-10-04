package org.apache.accumulo.server.upgrade;

import java.io.EOFException;
import java.util.ArrayList;

import org.apache.accumulo.core.Constants;
import org.apache.accumulo.core.client.impl.HdfsZooInstance;
import org.apache.accumulo.core.conf.AccumuloConfiguration;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.file.FileOperations;
import org.apache.accumulo.core.file.FileSKVIterator;
import org.apache.accumulo.core.iterators.DeletingIterator;
import org.apache.accumulo.core.iterators.MultiIterator;
import org.apache.accumulo.core.iterators.SortedKeyValueIterator;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.accumulo.core.zookeeper.ZooSession;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.zookeeper.ZooKeeper;


public class RunPreUpgradeCheck {
	
	static final String ZROOT_PATH = "/accumulo";
    static final String ZLOGS_PATH = "/root_tablet_logs";
	
	public static void main(String[] args) throws Exception {
		
		Configuration conf = CachedConfiguration.getInstance();
		FileSystem fs = FileSystem.get(conf);
		
		Path dvLocation = Constants.getDataVersionLocation();
		
		if(!fs.exists(new Path(dvLocation, "2"))){
			System.err.println("Did not see expected accumulo data version");
			System.exit(-1);
		}
		
		String rootTabletWALOGS = ZROOT_PATH+"/"+HdfsZooInstance.getInstance().getInstanceID()+ZLOGS_PATH;
		ZooKeeper session = ZooSession.getSession();
		
		if(session.exists(rootTabletWALOGS, false) != null && session.getChildren(rootTabletWALOGS, false).size() != 0){
			System.err.println("The root tablet has write ahead logs");
			System.exit(-1);
		}
		
		String rootTabletDir = Constants.getTablesDir()+"/!METADATA/root_tablet";
		
		FileStatus[] mapFiles = fs.listStatus(new Path(rootTabletDir));
		ArrayList<SortedKeyValueIterator<Key,Value>> inputs = new ArrayList<SortedKeyValueIterator<Key,Value>>();
		for (FileStatus fileStatus : mapFiles) {
			FileSKVIterator in = null;
			try {
			    in = FileOperations.getInstance().openReader(fileStatus.getPath().toString(), true, fs, conf, AccumuloConfiguration.getDefaultConfiguration());
			    inputs.add(in);
			} catch (EOFException ex) {
			    System.out.println("Problem opening map file "+fileStatus.getPath().toString()+", probably empty tmp file, skipping... ");
			    continue;
			}	
		}
		
		MultiIterator mi = new MultiIterator(inputs, true);
		DeletingIterator di = new DeletingIterator(mi, false);
		
		int count = 0, logCount = 0;
		
		while(di.hasTop()){
			if(di.getTopKey().getColumnFamily().equals(new Text("log"))){
				logCount++;
			}
			count++;
			di.next();
		}
		
		for (SortedKeyValueIterator<Key, Value> in : inputs) {
			((FileSKVIterator)in).close();
		}
		
		if(count == 0){
			System.err.println("Did not find any metadata entries");
			System.exit(-1);
		}
		
		if(logCount > 0){
			System.err.println("The metadata table has write ahead logs");
			System.exit(-1);
		}
			
		
	}
}