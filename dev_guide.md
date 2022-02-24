# Dim Sum Development Guide

Dim Sum is a Spring Boot web app.

* [Auto-configuration](https://docs.spring.io/spring-boot/docs/2.0.x/reference/html/using-boot-auto-configuration.html)
  automates a lot of application and library setup while allowing you to override any of its
  defaults
* The [IoC container](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans)
  simplifies the creation of application components via dependency injection
* Controller classes respond to web requests

## Configuration

App config is in `/resources/application.properties`. The properties in this file can be overridden
using command-line parameters, system variables, or an external copy of this file.

## HTML Templates

The Thymeleaf template engine is used for page design. Model attributes can be added via the Spring
controller and used in rendering the page.

The `layout.html` template contains elements common to all pages. Individual page templates should
set `layout:decorate="~{layout}"` as a property on the `html` tag to use the main layout, and
include a div with property `layout:fragment="content"` in the `body` to contain the page content.
Elements from the `head` section of the main layout will be included before the `head` elements
from the page template. The title from the page and layout will also be concatenated automatically.

## TypeScript

The JavaScript for the front end is compiled from TypeScript and packed using Webpack. During the
Maven build, the TypeScript in `/ui` is compiled and packed into individual files per HTML page,
and the resulting JavaScript is copied into `/target/classes/static/js/`, which will be available
at `/js/` in the webapp.

The page scripts to generate are defined in module.exports.entry in `webpack.config.js`. These
scripts are packaged as libraries to make any exports available to inline scripts in the HTML.
TypeScript compiler config is in `tsconfig.json`.

### JavaScript Library Dependencies

JavaScript libraries may be included and used in TypeScript. To keep things cleaner, these
libraries should only be used in the TypeScript, and should not be used in inline scripts in the
HTML, which should be kept as short and simple as possible. To include a JavaScript library to the
project:

1. Add it as a dependency in `package.json`
2. Add the TypeScript declaration as a development dependency in `package.json`
3. Add a type reference in any `.ts` script that uses it. e.g. `/// <reference types="jquery" />`
4. Create a plugin for the library in `webpack.config.js`
