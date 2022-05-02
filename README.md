# Dim Sum - QC Tracking Dashboard

## Build Requirements

* Java 11 JDK
* Maven 3.8+
* Node 17.3+
* NPM 8.3+
* TSC 4.5+

## Build/Run

Maven is configured to automatically run `npm` to install node modules, compile and package
TypeScript via `tsc` and `webpack`, and include requirements from both in the Java build.

Build runnable `.jar` file: `mvn clean package`

Run server on default port (8080): `mvn spring-boot:run`

Run server on different port: `mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=8081"`

## Monitoring

Prometheus metrics are available at `/metrics` on the deployed webapp.
