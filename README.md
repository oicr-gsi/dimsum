# Dim Sum - QC Tracking Dashboard

## Build Requirements

* Java 11 JDK
* Maven 3.8+
* Node 17.3+
* NPM 8.3+
* TSC 4.5+

## Configuration

1. Create a `config` subdirectory within the directory you will run the app from
2. Copy [example-application.properties](example-application.properties) into the `config`
   directory and rename it to `application.properties`
3. Are you enabling SAML authentication?
   * If yes:
     1. add the IdP certificate to your `config` directory
     2. generate/add your SP key and certificate to the `config` directory. An example using `openssl`:
     
        `openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048 -keyout dimsum_sp.key -out dimsum_sp.crt`

     3. fill out the SAML properties in `application.properties`, including paths to the
        above-mentioned certificates/key
   * If no, add the following line to `application.properties` to disable authentication:
     `spring.profiles.active=noauth`

## Build/Run

Maven is configured to automatically run `npm` to install node modules, compile and package
TypeScript via `tsc` and `webpack`, and include requirements from both in the Java build.

Build runnable `.jar` file: `mvn clean package`

Run server on default port (8080): `mvn spring-boot:run`

Run server on different port: `mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=8081"`

## Monitoring

Prometheus metrics are available at `/metrics` on the deployed webapp.
