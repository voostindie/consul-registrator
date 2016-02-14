package nl.ulso.consul.registrator;

interface ConsulClient {

    void register(Service service);

    void deregister(String serviceId);

    void storeKeyValue(String key, String value);

    void removeKey(String key);
}
