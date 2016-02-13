package nl.ulso.consul.registrator;

interface ConsulClient {

    void deregister(String serviceId);

    void register(Service service);
}
