package org.apache.accumulo.core.util;

import java.net.InetSocketAddress;
import java.util.EnumMap;

import org.apache.accumulo.core.conf.AccumuloConfiguration;
import org.apache.accumulo.core.conf.Property;


public class ServerServices implements Comparable<ServerServices>
{
    public static enum Service
    {
        TSERV_CLIENT, MASTER_CLIENT, GC_CLIENT;

        int getDefaultPort()
        {
            switch (this)
            {
                case TSERV_CLIENT:
                    return AccumuloConfiguration.getSystemConfiguration().getPort(Property.TSERV_CLIENTPORT);
                case MASTER_CLIENT:
                    return AccumuloConfiguration.getSystemConfiguration().getPort(Property.MASTER_CLIENTPORT);
                case GC_CLIENT:
                    return AccumuloConfiguration.getSystemConfiguration().getPort(Property.GC_PORT);
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    public static String SERVICE_SEPARATOR = ";";
    public static String SEPARATOR_CHAR = "=";

    private EnumMap<Service, String> services;
    private String stringForm = null;

    public ServerServices(String services)
    {
        this.services = new EnumMap<Service, String>(Service.class);

        String[] addresses = services.split(SERVICE_SEPARATOR);
        for (String address : addresses) {
            String[] sa = address.split(SEPARATOR_CHAR, 2);
            this.services.put(Service.valueOf(sa[0]), sa[1]);
        }
    }

    public ServerServices(String address, Service service)
    {
        this(service.name() + SEPARATOR_CHAR + address);
    }

    public String getAddressString(Service service)
    {
        return services.get(service);
    }

    public InetSocketAddress getAddress(Service service)
    {
        String address = getAddressString(service);
        String[] parts = address.split(":", 2);
        if (parts.length == 2) {
            if (parts[1].isEmpty())
                return new InetSocketAddress(parts[0], service.getDefaultPort());
            return new InetSocketAddress(parts[0], Integer.parseInt(parts[1]));
        }
        return new InetSocketAddress(address, service.getDefaultPort());
    }

    // DON'T CHANGE THIS; WE'RE USING IT FOR SERIALIZATION!!!
    public String toString()
    {
        if (stringForm == null) {
            StringBuilder sb = new StringBuilder();
            String prefix = "";
            for (Service service : new Service[] { Service.MASTER_CLIENT, Service.TSERV_CLIENT, Service.GC_CLIENT }) {
                if (services.containsKey(service))
                {
                    sb.append(prefix).append(service.name()).append(SEPARATOR_CHAR).append(services.get(service));
                    prefix = SERVICE_SEPARATOR;
                }
            }
            stringForm = sb.toString();
        }
        return stringForm;
    }

    public int hashCode()
    {
        return toString().hashCode();
    }

    public boolean equals(Object o)
    {
        if (o instanceof ServerServices)
            return toString().equals(((ServerServices) o).toString());
        return false;
    }

    public int compareTo(ServerServices other)
    {
        return toString().compareTo(other.toString());
    }
}