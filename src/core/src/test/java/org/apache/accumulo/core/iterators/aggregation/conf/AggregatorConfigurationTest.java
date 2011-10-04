package org.apache.accumulo.core.iterators.aggregation.conf;

import junit.framework.TestCase;

import org.apache.accumulo.core.iterators.aggregation.conf.AggregatorConfiguration;
import org.apache.accumulo.core.iterators.conf.PerColumnIteratorConfig;
import org.apache.hadoop.io.Text;


public class AggregatorConfigurationTest extends TestCase{
	public void testBinary(){
		Text colf = new Text();
		Text colq = new Text();
		
		for(int i = 0; i < 256; i++){
			colf.append(new byte[]{(byte)i}, 0, 1);
			colq.append(new byte[]{(byte)(255-i)}, 0, 1);
		}
		
		
		runTest(colf, colq);
		runTest(colf);
	}

	public void testBasic(){
		runTest(new Text("colf1"), new Text("cq2"));		
		runTest(new Text("colf1"));
	}
	
	private void runTest(Text colf) {
		String encodedCols;
		AggregatorConfiguration ac3 = new AggregatorConfiguration(colf, "com.foo.SuperAgg");
		encodedCols = ac3.encodeColumns();
		PerColumnIteratorConfig ac4 = PerColumnIteratorConfig.decodeColumns(encodedCols, "com.foo.SuperAgg");
		
		assertEquals(colf, ac4.getColumnFamily());
		assertNull(ac4.getColumnQualifier());
	}

	private void runTest(Text colf, Text colq) {
		AggregatorConfiguration ac = new AggregatorConfiguration(colf, colq, "com.foo.SuperAgg");
		String encodedCols = ac.encodeColumns();
		PerColumnIteratorConfig ac2 = PerColumnIteratorConfig.decodeColumns(encodedCols, "com.foo.SuperAgg");
		
		assertEquals(colf, ac2.getColumnFamily());
		assertEquals(colq, ac2.getColumnQualifier());
	}
	
	
}