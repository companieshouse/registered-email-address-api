# registered-email-address-api

### Overview
API for Registered Email Address - provides functionality for changing and retrieving a company's registered email address

### Requirements
In order to run the service locally you will need the following:
- [Java 11](https://www.oracle.com/java/technologies/downloads/#java11)
- [Maven](https://maven.apache.org/download.cgi)
- [Git](https://git-scm.com/downloads)

### Getting started
To checkout and build the service:
1. Clone [Docker CHS Development](https://github.com/companieshouse/docker-chs-development) and follow the steps in the README.
2. Run ./bin/chs-dev modules enable registered-email-address
3. Run ./bin/chs-dev development enable registered-email-address-api (this will allow you to make changes).
4. Run docker using "tilt up" in the docker-chs-development directory.
5. Use spacebar in the command line to open tilt window - wait for registered-email-address-api to become green.
6. Open your browser and go to page http://chs.local/register-email-address
7. If you are using the api directly, then use this url: http://api.chs.local/registered-email-address

These instructions are for a local docker environment.

### Endpoints

The full path for each public endpoints that requires a transaction id begins with the app url:
`${API_URL}/registered-email-address`

Method    | Path                                                                         | Description
:---------|:-----------------------------------------------------------------------------|:-----------