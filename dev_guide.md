# Dimsum Development Guide

Dimsum is a Spring Boot web app.

- [Auto-configuration](https://docs.spring.io/spring-boot/docs/2.0.x/reference/html/using-boot-auto-configuration.html)
  automates a lot of application and library setup while allowing you to override any of its
  defaults
- The [IoC container](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans)
  simplifies the creation of application components via dependency injection
- Controller classes respond to web requests
- The `src/main/resources/static` contains assets that are served as static resources:
  - `css`: custom css (see [Tailwind](#tailwind-css))
  - `img`: images or svg's such as logos
  - `libs`: external libraries
  - `js`: generated javascript (see [Typescript](#typescript))

## Configuration

App config is in `/resources/application.yml`. The properties in this file can be overridden
using command-line parameters, system variables, or an external properties file. See the
[README](/README.md) for more information on the external properties file.

## Debugging

Example: Run the server with debugging enabled on port 8000 and wait for debugger connection before
starting:

```
mvn clean spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=8000"
```

A VSCode launch configuration named "Debug local Dimsum" is provided to connect to this.

## Code Formatting

Dimsum has formatters configured for Java, HTML, and TypeScript in VSCode. For these to work, you
must have the following extensions installed:

- Language Support for Java(TM) by Red Hat
- Prettier - Code Formatter

## Authentication

SAML authentication is configured using Spring Security. Documentation for that is
[here](https://docs.spring.io/spring-security/reference/servlet/saml2/index.html). Authentication
may be disabled for development/testing using the "noauth" Spring profile

## HTML Templates

The Thymeleaf template engine is used for page design. Model attributes can be added via the Spring
controller and used in rendering the page.

The `layout.html` template contains elements common to all pages. Individual page templates should
set `layout:decorate="~{layout}"` as a property on the `html` tag to use the main layout, and
include a div with property `layout:fragment="content"` in the `body` to contain the page content.
Elements from the `head` section of the main layout will be included before the `head` elements
from the page template. The title from the page and layout will also be concatenated automatically.

## Font Awesome

Icons are sourced from [Font Awesome](https://fontawesome.com/). Dimsum strictly uses its free solid variants for consistent branding.

## Tailwind CSS

[Tailwind](https://tailwindcss.com) is a utility-based CSS framework that has been configured for Dimsum's design systems. Custom styles are defined in `tailwind.config.js` and can be reused in different files.

The config currently scans `.html` and `.ts` files in the `templates` and `ts` directories. The generated CSS can be found in `/src/resources/static/css/output.css`. New templates not on this path should be added.

If you've updated any template files, use

`npx tailwindcss -i src/main/resources/static/css/input.css -o src/main/resources/static/css/output.css --watch`

before building/running as usual outlined in the [README](/README.md)

**Tailwind should be used in most cases**. If truly [custom styles](https://tailwindcss.com/docs/adding-custom-styles)
are needed, they can be defined in `/src/resources/static/css/input.css`.

## TypeScript

The JavaScript for the front end is compiled from TypeScript and packed using Webpack. During the
Maven build, the TypeScript in `/ts` is compiled and packed into individual files per HTML page,
and the resulting JavaScript is copied into `/target/classes/static/js/`, which will be available
at `/js/` in the webapp.

The page scripts to generate are defined in `module.exports.entry` in `webpack.config.js`. These
scripts are packaged as libraries to make any exports available to inline scripts in the HTML.
TypeScript compiler config is in `tsconfig.json`.

### Best practices

When possible, re-use components and & utility functions. Some notable examples:

- `urls.ts` should be used to generate all URLs.
- `html-utils.ts` has a variety of functions for making icons, adding links and more (see its documentation for more details).
- `requests.ts` contains functions for making HTTP requests.

### JavaScript Library Dependencies

JavaScript libraries may be included and used in TypeScript. To keep things cleaner, these
libraries should only be used in the TypeScript, and should not be used in inline scripts in the
HTML, which should be kept as short and simple as possible. To include a JavaScript library to the
project:

1. Add it as a dependency in `package.json`
2. Add the TypeScript declaration as a development dependency in `package.json`
3. Add a type reference in any `.ts` script that uses it. e.g. `/// <reference types="jquery" />`
4. Create a plugin for the library in `webpack.config.js`

## Data Loading

Dimsum uses QC Gate data generated by QC Gate ETL. The data directory is specified by the
`datadirectory` property in `application.properties`. The
[CaseLoader](src/main/java/ca/on/oicr/dimsum/CaseLoader.java) class is responsible for loading the
data from file. [CaseService](src/main/java/ca/on/oicr/dimsum/service/CaseService.java) polls the
directory for changes, invokes `CaseLoader` to reload the data when appropriate, and maintains a
cache of the data to provide to other parts of the application.
