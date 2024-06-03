#!/bin/bash
#
# Start script for docs.developer.ch.gov.uk

PORT=8080

exec java -jar -Dserver.port="${PORT}" "registered-email-address-api.jar"