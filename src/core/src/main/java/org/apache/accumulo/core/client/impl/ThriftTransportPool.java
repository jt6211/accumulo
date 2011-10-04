package org.apache.accumulo.core.client.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.accumulo.core.conf.AccumuloConfiguration;
import org.apache.accumulo.core.conf.Property;
import org.apache.accumulo.core.util.AddressUtil;
import org.apache.accumulo.core.util.Daemon;
import org.apache.accumulo.core.util.TTimeoutTransport;
import org.apache.log4j.Logger;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;


public class ThriftTransportPool
{
    private static final Random random = new Random();
    private long killTime = 1000 * 3;

    private Map<ThriftTransportKey, List<CachedConnection>> cache;
    private Map<ThriftTransportKey, Long> errorCount;
    private Map<ThriftTransportKey, Long> errorTime;
    private Set<ThriftTransportKey> serversWarnedAbout;

    private static final Logger log = Logger.getLogger(ThriftTransportPool.class);

    private static final Long ERROR_THRESHOLD = 20l;
    private static final int STUCK_THRESHOLD = 2 * 60 * 1000;

    private static class CachedConnection
    {

        public CachedConnection(CachedTTransport t)
        {
            this.transport = t;
        }

        void setReserved(boolean reserved)
        {
            this.transport.setReserved(reserved);
        }

        boolean isReserved()
        {
            return this.transport.reserved;
        }

        CachedTTransport transport;

        long lastReturnTime;
    }

    private class Closer implements Runnable
    {

        public void run()
        {
            while (true) {

                ArrayList<CachedConnection> connectionsToClose = new ArrayList<CachedConnection>();

                synchronized (ThriftTransportPool.this) {
                    for (List<CachedConnection> ccl : cache.values()) {
                        Iterator<CachedConnection> iter = ccl.iterator();
                        while (iter.hasNext()) {
                            CachedConnection cachedConnection = iter.next();

                            if (!cachedConnection.isReserved() && System.currentTimeMillis() - cachedConnection.lastReturnTime > killTime) {
                                connectionsToClose.add(cachedConnection);
                                iter.remove();
                            }
                        }
                    }

                    for (List<CachedConnection> ccl : cache.values()) {
                        for (CachedConnection cachedConnection : ccl) {
                            cachedConnection.transport.checkForStuckIO(STUCK_THRESHOLD);
                        }
                    }

                    Iterator<Entry<ThriftTransportKey, Long>> iter = errorTime.entrySet().iterator();
                    while (iter.hasNext()) {
                        Entry<ThriftTransportKey, Long> entry = iter.next();
                        long delta = System.currentTimeMillis() - entry.getValue();
                        if (delta >= STUCK_THRESHOLD) {
                            errorCount.remove(entry.getKey());
                            iter.remove();
                        }
                    }
                }

                // close connections outside of sync block
                for (CachedConnection cachedConnection : connectionsToClose) {
                    cachedConnection.transport.close();
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class CachedTTransport extends TTransport
    {

        private ThriftTransportKey cacheKey;
        private TTransport wrappedTransport;
        private boolean sawError = false;

        private volatile String ioThreadName = null;
        private volatile long ioStartTime = 0;
        private volatile boolean reserved = false;

        private String stuckThreadName = null;

        int ioCount = 0;
        int lastIoCount = -1;

        private void sawError(Exception e)
        {
            sawError = true;
        }

        final void setReserved(boolean reserved)
        {
            this.reserved = reserved;
            if (reserved) {
                ioThreadName = Thread.currentThread().getName();
                ioCount = 0;
                lastIoCount = -1;
            }
            else {
                if (ioCount % 2 == 1) {
                    // connection unreserved, but it seems io may still be
                    // happening
                    log.warn("Connection returned to thrift connection pool that may still be in use " + ioThreadName + " " + Thread.currentThread().getName(), new Exception());
                }

                ioCount = 0;
                lastIoCount = -1;
                ioThreadName = null;
            }
            checkForStuckIO(STUCK_THRESHOLD);
        }

        final void checkForStuckIO(long threshold)
        {
            /*
             * checking for stuck io needs to be light weight.
             * 
             * Tried to call System.currentTimeMillis() and
             * Thread.currentThread()
             * before every io operation.... this dramatically slowed things
             * down. So switched to incrementing a counter before and after
             * each io operation.
             */

            if (ioCount % 2 == 1) {
                // when ioCount is odd, it means I/O is currently happening
                if (ioCount == lastIoCount) {
                    // still doing same I/O operation as last time this
                    // functions was called
                    long delta = System.currentTimeMillis() - ioStartTime;
                    if (delta >= threshold && stuckThreadName == null) {
                        stuckThreadName = ioThreadName;
                        log.warn("Thread \"" + ioThreadName + "\" stuck on IO  to " + cacheKey + " for at least " + delta + " ms");
                    }
                }
                else {
                    // remember this ioCount and the time we saw it, need to see
                    // if it changes
                    lastIoCount = ioCount;
                    ioStartTime = System.currentTimeMillis();

                    if (stuckThreadName != null) {
                        // doing I/O, but ioCount changed so no longer stuck
                        log.info("Thread \"" + stuckThreadName + "\" no longer stuck on IO  to " + cacheKey + " sawError = " + sawError);
                        stuckThreadName = null;
                    }
                }
            }
            else {
                // I/O is not currently happening
                if (stuckThreadName != null) {
                    // no longer stuck, and was stuck in the past
                    log.info("Thread \"" + stuckThreadName + "\" no longer stuck on IO  to " + cacheKey + " sawError = " + sawError);
                    stuckThreadName = null;
                }
            }
        }

        public CachedTTransport(TTransport transport, ThriftTransportKey cacheKey2)
        {
            this.wrappedTransport = transport;
            this.cacheKey = cacheKey2;
        }

        public boolean isOpen()
        {
            return wrappedTransport.isOpen();
        }

        public void open() throws TTransportException
        {
            try {
                ioCount++;
                wrappedTransport.open();
            } catch (TTransportException tte) {
                sawError(tte);
                throw tte;
            } finally {
                ioCount++;
            }
        }

        public int read(byte[] arg0, int arg1, int arg2) throws TTransportException
        {
            try {
                ioCount++;
                return wrappedTransport.read(arg0, arg1, arg2);
            } catch (TTransportException tte) {
                sawError(tte);
                throw tte;
            } finally {
                ioCount++;
            }
        }

        public int readAll(byte[] arg0, int arg1, int arg2) throws TTransportException
        {
            try {
                ioCount++;
                return wrappedTransport.readAll(arg0, arg1, arg2);
            } catch (TTransportException tte) {
                sawError(tte);
                throw tte;
            } finally {
                ioCount++;
            }
        }

        public void write(byte[] arg0, int arg1, int arg2) throws TTransportException
        {
            try {
                ioCount++;
                wrappedTransport.write(arg0, arg1, arg2);
            } catch (TTransportException tte) {
                sawError(tte);
                throw tte;
            } finally {
                ioCount++;
            }
        }

        public void write(byte[] arg0) throws TTransportException
        {
            try {
                ioCount++;
                wrappedTransport.write(arg0);
            } catch (TTransportException tte) {
                sawError(tte);
                throw tte;
            } finally {
                ioCount++;
            }
        }

        public void close()
        {
            try {
                ioCount++;
                wrappedTransport.close();
            } finally {
                ioCount++;
            }

        }

        public void flush() throws TTransportException
        {
            try {
                ioCount++;
                wrappedTransport.flush();
            } catch (TTransportException tte) {
                sawError(tte);
                throw tte;
            } finally {
                ioCount++;
            }
        }

        public boolean peek()
        {
            try {
                ioCount++;
                return wrappedTransport.peek();
            } finally {
                ioCount++;
            }
        }

        public ThriftTransportKey getCacheKey()
        {
            return cacheKey;
        }

    }

    private ThriftTransportPool()
    {
        cache = new HashMap<ThriftTransportKey, List<CachedConnection>>();
        errorCount = new HashMap<ThriftTransportKey, Long>();
        errorTime = new HashMap<ThriftTransportKey, Long>();
        serversWarnedAbout = new HashSet<ThriftTransportKey>();

        Thread thread = new Daemon(new Closer(), "Thrift Connection Pool Checker");
        thread.start();
    }

    public TTransport getTransport(String location, int port) throws TTransportException
    {
        return getTransport(location, port, 0);
    }

    public TTransport getTransportWithDefaultTimeout(InetSocketAddress addr, AccumuloConfiguration conf) throws TTransportException
    {
        return getTransport(addr.getAddress().getHostAddress(), addr.getPort(), conf.getTimeInMillis(Property.GENERAL_RPC_TIMEOUT));
    }

    public TTransport getTransport(InetSocketAddress addr, long timeout) throws TTransportException
    {
        return getTransport(addr.getAddress().getHostAddress(), addr.getPort(), timeout);
    }

    public TTransport getTransportWithDefaultTimeout(String location, int port, AccumuloConfiguration conf) throws TTransportException
    {
        return getTransport(location, port, conf.getTimeInMillis(Property.GENERAL_RPC_TIMEOUT));
    }

    TTransport getAnyTransport(List<ThriftTransportKey> servers) throws TTransportException
    {
    	
    	servers = new ArrayList<ThriftTransportKey>(servers);
    	
        synchronized (this) {
            // atomically find an available location that is in the list
            for (Entry<ThriftTransportKey, List<CachedConnection>> entry : cache.entrySet()) {
                if (servers.contains(entry.getKey())) {
                    for (CachedConnection cachedConnection : entry.getValue()) {
                        if (!cachedConnection.isReserved()) {
                            cachedConnection.setReserved(true);
                            if (log.isTraceEnabled()) log.trace("Using existing connection to " + entry.getKey().getLocation() + ":" + entry.getKey().getPort());
                            return cachedConnection.transport;
                        }
                    }
                }
            }
        }

        
        int retryCount = 0;
        while(servers.size() > 0 && retryCount < 10){
        	int index = random.nextInt(servers.size());
        	try{
        		return createNewTransport(servers.get(index));
        	}catch(TTransportException tte){
        		log.debug("Failed to connect to "+servers.get(index), tte);
        		servers.remove(index);
        		retryCount++;
        	}
        }
        
        throw new TTransportException("Failed to connect to a server");
    }

    public TTransport getTransport(String location, int port, long milliseconds) throws TTransportException
    {
        return getTransport(new ThriftTransportKey(location, port, milliseconds));
    }

    private TTransport getTransport(ThriftTransportKey cacheKey) throws TTransportException
    {
        synchronized (this) {
            // atomically reserve location if it exist in cache
            List<CachedConnection> ccl = cache.get(cacheKey);

            if (ccl == null) {
                ccl = new LinkedList<CachedConnection>();
                cache.put(cacheKey, ccl);
            }

            for (CachedConnection cachedConnection : ccl) {
                if (!cachedConnection.isReserved()) {
                    cachedConnection.setReserved(true);
                    if (log.isTraceEnabled()) log.trace("Using existing connection to " + cacheKey.getLocation() + ":" + cacheKey.getPort());
                    return cachedConnection.transport;
                }
            }
        }

        return createNewTransport(cacheKey);
    }

    private TTransport createNewTransport(ThriftTransportKey cacheKey) throws TTransportException
    {
        TTransport transport;
        if (cacheKey.getTimeout() == 0) {
            transport = AddressUtil.createTSocket(cacheKey.getLocation(), cacheKey.getPort());
        }
        else {
            try {
                transport = TTimeoutTransport.create(AddressUtil.parseAddress(cacheKey.getLocation(), cacheKey.getPort()), cacheKey.getTimeout());
            } catch (IOException ex) {
                throw new TTransportException(ex);
            }
        }
        transport.open();

        if (log.isTraceEnabled()) log.trace("Creating new connection to connection to " + cacheKey.getLocation() + ":" + cacheKey.getPort());

        CachedTTransport tsc = new CachedTTransport(transport, cacheKey);

        CachedConnection cc = new CachedConnection(tsc);
        cc.setReserved(true);

        synchronized (this) {
            List<CachedConnection> ccl = cache.get(cacheKey);

            if (ccl == null) {
                ccl = new LinkedList<CachedConnection>();
                cache.put(cacheKey, ccl);
            }

            ccl.add(cc);
        }
        return cc.transport;
    }

    public void returnTransport(TTransport tsc)
    {
        if (tsc == null) {
            return;
        }

        boolean existInCache = false;
        CachedTTransport ctsc = (CachedTTransport) tsc;

        synchronized (this) {
            List<CachedConnection> ccl = cache.get(ctsc.getCacheKey());
            for (Iterator<CachedConnection> iterator = ccl.iterator(); iterator.hasNext();) {
                CachedConnection cachedConnection = iterator.next();
                if (cachedConnection.transport == tsc) {
                    if (ctsc.sawError) {
                        tsc.close();
                        iterator.remove();

                        if (log.isTraceEnabled()) log.trace("Returned connection had error " + ctsc.getCacheKey());

                        Long ecount = errorCount.get(ctsc.getCacheKey());
                        if (ecount == null)
                            ecount = 0l;
                        ecount++;
                        errorCount.put(ctsc.getCacheKey(), ecount);

                        Long etime = errorTime.get(ctsc.getCacheKey());
                        if (etime == null) {
                            errorTime.put(ctsc.getCacheKey(), System.currentTimeMillis());
                        }

                        if (ecount >= ERROR_THRESHOLD && !serversWarnedAbout.contains(ctsc.getCacheKey())) {
                            log.warn("Server " + ctsc.getCacheKey() + " had " + ecount + " failures in a short time period, will not complain anymore ");
                            serversWarnedAbout.add(ctsc.getCacheKey());
                        }

                        cachedConnection.setReserved(false);

                    }
                    else {

                    	if (log.isTraceEnabled()) log.trace("Returned connection " + ctsc.getCacheKey() + " ioCount : " + cachedConnection.transport.ioCount);

                        cachedConnection.lastReturnTime = System.currentTimeMillis();
                        cachedConnection.setReserved(false);
                    }
                    existInCache = true;
                    break;
                }
            }
        }

        if (!existInCache) {
            log.warn("Returned tablet server connection to cache that did not come from cache");
            // close outside of sync block
            tsc.close();
        }
    }

    private static ThriftTransportPool instance = null;

    public static ThriftTransportPool getInstance()
    {
        if (instance == null) {
            synchronized (ThriftTransportPool.class) {
                if (instance == null)
                    instance = new ThriftTransportPool();
            }
        }
        return instance;
    }
}