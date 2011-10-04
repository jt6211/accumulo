package org.apache.accumulo.server.test;

import java.util.Arrays;
import java.util.Random;
import java.util.TreeSet;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.MutationsRejectedException;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.impl.HdfsZooInstance;
import org.apache.accumulo.core.client.impl.TabletServerBatchWriter;
import org.apache.accumulo.core.data.ConstraintViolationSummary;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.KeyExtent;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.file.map.MyMapFile;
import org.apache.accumulo.core.file.map.MySequenceFile.CompressionType;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.accumulo.core.security.ColumnVisibility;
import org.apache.accumulo.core.security.thrift.AuthInfo;
import org.apache.accumulo.core.trace.DistributedTrace;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.accumulo.server.security.Authenticator;
import org.apache.accumulo.server.security.ZKAuthenticator;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.Text;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import cloudtrace.instrument.Trace;

@SuppressWarnings("deprecation")
public class TestIngest {
	public static final Authorizations AUTHS = new Authorizations("L1", "L2", "G1","GROUP2");
	
    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger(TestIngest.class);
	private static AuthInfo rootCredentials;
	private static String username;
	private static String passwd;
	
	public static class CreateTable {
		public static void main(String[] args)
		throws AccumuloException, AccumuloSecurityException, TableExistsException, TableNotFoundException
		{
			long start = Long.parseLong(args[0]);
			long end = Long.parseLong(args[1]);
			long numsplits = Long.parseLong(args[2]);
			String username = args[3];
			byte[] passwd = args[4].getBytes();
			
			TreeSet<Text> splits = getSplitPoints(start, end, numsplits);

			Connector conn = HdfsZooInstance.getInstance().getConnector(username, passwd);
			conn.tableOperations().create("test_ingest");
			conn.tableOperations().addSplits("test_ingest", splits);
		}

		public static TreeSet<Text> getSplitPoints(long start, long end,
				long numsplits) {
			long splitSize = (end - start)/numsplits;
			
			long pos = start + splitSize;
			
			TreeSet<Text> splits = new TreeSet<Text>();
			
			while(pos < end)
			{
				splits.add(new Text(String.format("row_%010d", pos)));
				pos+=splitSize;
			}
			return splits;
		}
	}
	
	public static class IngestArgs {
		int rows;
		int startRow;
		int cols;
		
		boolean random = false;
		int seed = 0;
		int dataSize = 1000;
		
		boolean delete = false;
		long timestamp = 0;
		boolean hasTimestamp = false;
		boolean useGet = false;
		
		public boolean unique;
		
		boolean outputToMapFile = false;
		String outputFile;
		
		int stride;
		public boolean useTsbw = false;
		
		String columnFamily = "colf";
		
		boolean trace = false;
	}
	
	public static Options getOptions() {
	    Options opts = new Options();
	    opts.addOption(new Option("size", "size", true, "size"));
	    opts.addOption(new Option("colf", "colf", true, "colf"));
        opts.addOption(new Option("delete", "delete", false, "delete"));
	    opts.addOption(new Option("random", "random", true, "random"));
	    opts.addOption(new Option("timestamp", "timestamp", true, "timestamp"));
	    opts.addOption(new Option("stride", "stride", true, "stride"));
	    opts.addOption(new Option("useGet", "useGet", false, "use get"));
	    opts.addOption(new Option("tsbw", "tsbw", false, "tsbw"));
	    opts.addOption(new Option("username", "username", true, "username"));
	    opts.addOption(new Option("password", "password", true, "password"));
	    opts.addOption(new Option("mapFile", "mapFile", true, "map file"));
	    opts.addOption(new Option("trace", "trace", false, "turn on distributed tracing"));
	    return opts;
	}

	public static IngestArgs parseArgs(String args[]){
		
		Parser p = new BasicParser();
		Options opts = getOptions();
		CommandLine cl;

		try {
			cl = p.parse(opts, args);
		} catch (ParseException e) {
			System.out.println("Parse Error, exiting.");
			throw new RuntimeException(e);
		}
		
		if(cl.getArgs().length != 3){
			HelpFormatter hf = new HelpFormatter();
			hf.printHelp("test_ingest <rows> <start_row> <num_columns>", getOptions());
			throw new RuntimeException();
		}
		
		IngestArgs ia = new IngestArgs();
		
		if (cl.hasOption("size")) {
            ia.dataSize = Integer.parseInt(cl.getOptionValue("size"));
        }
		if (cl.hasOption("colf")) {
            ia.columnFamily = cl.getOptionValue("colf");
        }
        if (cl.hasOption("timestamp")) {
			ia.timestamp = Long.parseLong(cl.getOptionValue("timestamp"));
			ia.hasTimestamp = true;
		}
		if (cl.hasOption("mapFile")) {
			ia.outputToMapFile = true;
			ia.outputFile = cl.getOptionValue("mapFile");
		}
		ia.delete = cl.hasOption("delete");
		ia.useGet = cl.hasOption("useGet");
		if (cl.hasOption("random")) {
			ia.random = true;
			ia.seed = Integer.parseInt(cl.getOptionValue("random"));
		}
		if (cl.hasOption("stride")) {
			ia.stride = Integer.parseInt(cl.getOptionValue("stride"));
		}
		ia.useTsbw = cl.hasOption("tsbw");
		
		username = cl.getOptionValue("username", "root");
		passwd = cl.getOptionValue("password", "secret");
		
		String[] requiredArgs = cl.getArgs();
		
		ia.rows = Integer.parseInt(requiredArgs[0]);
		ia.startRow = Integer.parseInt(requiredArgs[1]);
		ia.cols = Integer.parseInt(requiredArgs[2]);
		
		if (cl.hasOption("trace")) {
            ia.trace = true;
        }
		return ia;
	}
	
	public static byte[][] generateValues(IngestArgs ingestArgs){
		
		byte[][] bytevals = new byte[10][];

		byte[] letters = {'1','2','3','4','5','6','7','8','9','0'};
		
		for(int i = 0; i < 10; i++) {
			bytevals[i] = new byte[ingestArgs.dataSize];
			for(int j=0; j< ingestArgs.dataSize; j++)
				bytevals[i][j] = letters[i]; 
		}
		
		return bytevals;
	}
	
	private static byte ROW_PREFIX[] = "row_".getBytes();
	private static byte COL_PREFIX[] = "col_".getBytes();
	
	public static Text generateRow(int rowid, int startRow) {
		return new Text(FastFormat.toZeroPaddedString(rowid + startRow, 10, 10, ROW_PREFIX));
	}
	
	public static byte[] genRandomValue(Random random, byte dest[], int seed, int row, int col){
		random.setSeed((row ^ seed) ^ col);
		random.nextBytes(dest);
		toPrintableChars(dest);
		
		return dest;
	}

	public static void toPrintableChars(byte[] dest) {
		//transform to printable chars
		for(int i = 0; i < dest.length; i++) {
			dest[i] = (byte)(( (0xff & dest[i]) % 92) + ' ');
		}
	}
	
	public static void main(String[] args) {
		// log.error("usage : test_ingest [-delete] [-size <value size>] [-random <seed>] [-timestamp <ts>] [-stride <size>] <rows> <start row> <# cols> ");
	    
	    IngestArgs ingestArgs = parseArgs(args);
	    Instance instance = HdfsZooInstance.getInstance();
        
	    try {
	        if (ingestArgs.trace) { 
	            String name = TestIngest.class.getSimpleName();
	            DistributedTrace.enable(instance, name, null);
	            Trace.on(name);
	            Trace.currentTrace().data("cmdLine", Arrays.asList(args).toString());
	        }
	        
	        
			Logger.getLogger(TabletServerBatchWriter.class.getName()).setLevel(Level.TRACE);
			
			// test batch update
			
			long stopTime;
			
			byte[][] bytevals = generateValues(ingestArgs);
			
			byte randomValue[] = new byte[ingestArgs.dataSize];
			Random random = new Random();
			
			long bytesWritten = 0;
			
			BatchWriter bw = null;
			MyMapFile.Writer mfw = null;
			
			if(ingestArgs.outputToMapFile){
				rootCredentials = new AuthInfo(username, passwd.getBytes(), instance.getInstanceID());
				FileSystem fs = FileSystem.get(CachedConfiguration.getInstance());
				mfw = new MyMapFile.Writer(CachedConfiguration.getInstance(), fs, ingestArgs.outputFile, Key.class, Value.class, CompressionType.BLOCK);
			}else{
                rootCredentials = new AuthInfo(username, passwd.getBytes(), instance.getInstanceID());
			    Connector connector =instance.getConnector(rootCredentials.user, rootCredentials.password);
				bw = connector.createBatchWriter("test_ingest", 20000000l, 60000l, 10);
			}
			
			Authenticator authenticator = ZKAuthenticator.getInstance();
			authenticator.changeAuthorizations(rootCredentials, rootCredentials.user, AUTHS);
			ColumnVisibility le = new ColumnVisibility("L1&L2&G1&GROUP2");
			Text labBA = new Text(le.getExpression());
			
			//int step = 100;
			
			long startTime = System.currentTimeMillis();
			for(int i=0; i < ingestArgs.rows; i++) {
				
				int rowid;
				
				if(ingestArgs.stride > 0){
					rowid = ((i%ingestArgs.stride) * (ingestArgs.rows / ingestArgs.stride)) + (i/ingestArgs.stride);	
				}else{
					rowid = i;
				}
				
				Text row = generateRow(rowid, ingestArgs.startRow);
				Mutation m = new Mutation(row);
				for(int j =0; j < ingestArgs.cols; j++) {
					Text colf = new Text(ingestArgs.columnFamily);
					Text colq = new Text(FastFormat.toZeroPaddedString(j, 5, 10, COL_PREFIX));
					
					if(ingestArgs.outputToMapFile){
						Key key = new Key(row, colf, colq, labBA);
						if(ingestArgs.hasTimestamp) {
							key.setTimestamp(ingestArgs.timestamp);
						}else{
							key.setTimestamp(System.currentTimeMillis());
						}
						
						if(ingestArgs.delete){
							key.setDeleted(true);
						}else{
							key.setDeleted(false);
						}
						
						bytesWritten += key.getSize();
						
						if(ingestArgs.delete){
							mfw.append(key, new Value(new byte[0]));
						}else{
							byte value[];
							if(ingestArgs.random){
								value = genRandomValue(random, randomValue, ingestArgs.seed, rowid + ingestArgs.startRow, j);
							}else{
								value = bytevals[j % bytevals.length];
							}
							
							Value v = new Value(value);
							mfw.append(key, v);
							bytesWritten += v.getSize();
						}
						
					}else{
					    Key key = new Key(row, colf, colq, labBA);
					    bytesWritten += key.getSize();
	
					    if(ingestArgs.delete){
							if(ingestArgs.hasTimestamp)
								m.putDelete(colf, colq, le, ingestArgs.timestamp);
							else
								m.putDelete(colf, colq, le);
						}else{
							byte value[];
							if(ingestArgs.random){
								value = genRandomValue(random, randomValue, ingestArgs.seed, rowid + ingestArgs.startRow, j);
							}else{
								value = bytevals[j % bytevals.length];
							}
							bytesWritten += value.length;
	
							if(ingestArgs.hasTimestamp) {
								m.put(colf, colq, le, ingestArgs.timestamp, new Value(value, true));
							}
							else {
								m.put(colf, colq, le, new Value(value, true));
	
							}
						}
					}
					
				}
				if (bw != null) 
				    bw.addMutation(m);
				
			}
			
			if(ingestArgs.outputToMapFile){
				mfw.close();
			}else if(bw != null){
				try {
					bw.close();
				} catch (MutationsRejectedException e) {
					if(e.getAuthorizationFailures().size() > 0){
						for (KeyExtent ke : e.getAuthorizationFailures()) {
							System.err.println("ERROR : Not authorized to write to : "+ke);
						}
					}
					
					if(e.getConstraintViolationSummaries().size() > 0){
						for (ConstraintViolationSummary cvs : e.getConstraintViolationSummaries()) {
							System.err.println("ERROR : Constraint violates : "+cvs);
						}
					}
					
					
					throw e;
				}
			}
			
			stopTime = System.currentTimeMillis();
			
			int totalValues = ingestArgs.rows*ingestArgs.cols;
			double elapsed = (stopTime - startTime) / 1000.0;
			
			System.out.printf("%,12d records written | %,8d records/sec | %,12d bytes written | %,8d bytes/sec | %6.3f secs   \n",totalValues, (int)(totalValues/elapsed),bytesWritten, (int)(bytesWritten/elapsed), elapsed );
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
		    Trace.off();
		}
	}
}