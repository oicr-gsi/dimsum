# Dimsum - QC Tracking Dashboard

![CI](https://github.com/oicr-gsi/dimsum/actions/workflows/ci.yml/badge.svg)

## Build Requirements

* Java 17 JDK
* Maven 3.8+
* Node 20+
* NPM 9+

## Configuration

1. Create a `config` subdirectory within the directory you will run the app from
2. Copy [example-application.properties](example-application.properties) into the `config`
   directory and rename it to `application.properties`
3. Are you enabling SAML authentication?
   - If yes:
     1. add the IdP certificate to your `config` directory
     2. generate/add your SP key and certificate to the `config` directory. An example using `openssl`:
     
        `openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048 -keyout dimsum_sp.key -out dimsum_sp.crt`

     3. fill out the SAML properties in `application.properties`, including paths to the
        above-mentioned certificates/key
   - If no, add the following line to `application.properties` to disable authentication:
     `spring.profiles.active=noauth`

## Identity Provider Configuration

Dimsum information you'll likely need to configure on your IdP:

- **Entity ID**: `<base-url>/saml2/service-provider-metadata/dimsum` (this is also the URL of the service
provider metadata if you need to download the XML)
- **POST Logout URL for single logout**: `<base-url>/logout/saml2/slo`

### Example Keycloak Client Configuration

- **Client type**: SAML
- **Client ID**: `<base-url>/saml2/service-provider-metadata/dimsum`
- **Name**: Dimsum
- **Always display in UI**: On
- **Root URL**: `http://localhost:8081`
- **Valid redirect URIs**: `/*`

**Advanced settings (after saving)**

- **Logout Service POST Binding URL**: `<base-url>/logout/saml2/slo`

**Certificates**

To add the SP certificate to Keycloak, go to __Client__ -> __Keys__ -> __Import Key__. Choose
Archive format "Certificate PEM" and add the SP certificate generated above.

To get the IdP certificate from Keycloak, go to __Realm settings__ -> __Keys__ > __RS256__ ->
__Certificate__. Save the text to a file.

## Build/Run

Maven is configured to automatically run `npm` to install node modules, compile and package
TypeScript via `tsc` and `webpack`, and include requirements from both in the Java build.

Build runnable `.jar` file: 

`mvn clean package`

Or run server via Maven:

`mvn clean spring-boot:run`

The server runs on port 8080 by default. To run on a different port, add a `server.port` setting
to your `application.properties`

## Monitoring

Prometheus metrics are available at `/metrics` on the deployed webapp.
