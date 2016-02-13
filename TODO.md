# TODO

## 1.0.0

- [x] Load service catalog from XML on the classpath of the application
- [x] Register services with Consul's agent on localhost:8500
- [x] Configurable delay on startup, to give the application the time to come up
- [x] Configurable logging: debug, info, silent.
- [x] Add testcases; my enthusiasm got the better of me (again...)
- [ ] Environment variable substitution in `consul-catalog.xml` (`${<environment_variable>[:default]}`)
- [ ] Store service metadata from `consul-catalog.xml` in the Consul K/V store

## To be determined

- [ ] Allow Consul agent address (host/port) as javaagent arguments
- [ ] Set default HTTP check interval as javaagent argument
- [ ] Proper XSD for `consul-catalog.xml`
- [ ] Authentication on Consul (ACL)
- [ ] Alternative checks (e.g. TCP, TTL)
