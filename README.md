# HDS-notary-client

# Running tests
Most of the tests require the server to be up, so first run the server and then:

### Run service tests only
```bash
$ mvn clean install -Dtest=ClientServiceTest
```

### Run a security tests only
```basj
$ mvn clean install -Dtest=SecurityTestCase
```

### Run a single test
```bash
$ mvn clean install -Dtest=SecurityTestCase#testManInTheMiddleInvalidCommand
```

# Run client without any tests

### Run default client (1)
```bash
$ mvn clean install -DskipTests exec:java
```

### Run client 2:
```bash
$ mvn clean install -DskipTests exec:java -Dexec.args="2"
```
