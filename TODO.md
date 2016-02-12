# TODO

## Minimum viable product

- [ ] Implement basic functionality as described in the README
- [ ] Environment variable substitution in `consul-services.xml` (`${<environment_variable>[:default]}`)

## 1.0.0

- [ ] Configurable delay on startup, to give the application the time to come up
- [ ] Deregister before registering, to clean up an earlier crash
- [ ] Allow Consul agent address (host/port) as javaagent arguments
- [ ] Store service metadata from `consul-services.xml` in the Consul K/V store

## To be determined

- [ ] Set default HTTP check interval as javaagent argument
- [ ] Proper XSD for `consul-services.xml`
- [ ] Authentication on Consul (ACL)
- [ ] Alternative checks (e.g. TCP, TTL)
