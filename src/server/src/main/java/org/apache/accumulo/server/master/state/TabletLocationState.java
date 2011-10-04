/**
 * 
 */
package org.apache.accumulo.server.master.state;


import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.apache.accumulo.core.data.KeyExtent;
import org.apache.log4j.Logger;


/**
 * When a tablet is assigned, we mark its future location.  When the tablet is opened, we set its current location.
 * A tablet should never have both a future and current location. 
 *
 * A tablet server is always associated with a unique session id.  
 * If the current tablet server has a different session, we know the location information is out-of-date.
 */
public class TabletLocationState {
    
    private static final Logger log = Logger.getLogger(TabletLocationState.class);
    public TabletLocationState(KeyExtent extent,
                               TServerInstance future, 
                               TServerInstance current, 
                               TServerInstance last,
                               Collection<Collection<String>> walogs) {
        this.extent = extent;
        this.future = future;
        this.current = current;
        this.last = last;
        if (walogs == null)
            walogs = Collections.emptyList();
        this.walogs = walogs;
        if (current != null && future != null) {
            log.error(extent + " is both assigned and hosted, which should never happen: " + this);
        }
    }
    final public KeyExtent extent;
    final public TServerInstance future;
    final public TServerInstance current;
    final public TServerInstance last;
    final public Collection<Collection<String>> walogs;
    
    public String toString() {
        return extent +"@(" + future + "," + current + "," + last + ")";
    }
    public TServerInstance getServer() {
        TServerInstance result = null;
        if (current != null) {
            result = current;
        } else if (future != null) {
            result = future;
        } else {
            result = last;
        }
        return result;
    }

    public TabletState getState(Set<TServerInstance> liveServers) {
        TServerInstance server = getServer();
        if (server == null)
            return TabletState.UNASSIGNED;
        if (server.equals(current) || server.equals(future)) {
            if (liveServers.contains(server))
                if (server.equals(future)) {
                    return TabletState.ASSIGNED;
                } else {
                    return TabletState.HOSTED;
                }
            else {
                return TabletState.ASSIGNED_TO_DEAD_SERVER;
            }
        }
        // server == last
        return TabletState.UNASSIGNED;
    }
    
}