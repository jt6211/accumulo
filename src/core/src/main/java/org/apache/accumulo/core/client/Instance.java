package org.apache.accumulo.core.client;

import java.util.List;

import org.apache.accumulo.core.conf.AccumuloConfiguration;



/**
 * This class represents the information a client
 * needs to know to connect to an instance of
 * accumulo. 
 * 
 */
public interface Instance {
    /**
     * Get the location of the tablet server that is serving the root tablet.
     * 
     * @return location in "hostname:port" form. 
     */
    public abstract String getRootTabletLocation();
    
    /**
     * Get the location(s) of the accumulo master and any redundant servers.
     * 
     * @return a list of locations in "hostname:port" form.
     */
    public abstract List<String> getMasterLocations();
    
    /**
     * Get a unique string that identifies this instance of accumulo.
     * @return a UUID
     */
    public abstract String getInstanceID();
    
    /**
     * Returns the instance name given at system initialization time.
     * 
     * @return current instance name
     */
    public abstract String getInstanceName();

    /**
     * @return the zoo keepers this instance is using
     */
	public abstract String getZooKeepers();
	/**
	 * @return the configured timeout to connect to zookeeper
	 */
	public abstract int getZooKeepersSessionTimeOut();

	/**
	 * 
	 * @param user
	 * @param pass  A UTF-8 encoded password.  The password may be cleared after making this call.
	 * @return
	 * @throws AccumuloException
	 * @throws AccumuloSecurityException
	 */
    public abstract Connector getConnector(String user, byte[] pass) throws AccumuloException, AccumuloSecurityException;

    /**
     * 
     * @param user
     * @param pass  If a mutable CharSequence is passed in, it may be cleared after this call.
     * @return
     * @throws AccumuloException
     * @throws AccumuloSecurityException
     */
    public abstract Connector getConnector(String user, CharSequence pass) throws AccumuloException, AccumuloSecurityException;
    
    
    /**
     * get the AccumuloConfiguration to use when interacting with this instance
     * @return the AccumuloConfiguration that specifies properties related to interacting with this instance
     */
    public abstract AccumuloConfiguration getConfiguration();

    /**
     * set the AccumuloConfiguration to use when interacting with this instance
     * @param conf
     */
    public abstract void setConfiguration(AccumuloConfiguration conf);
}