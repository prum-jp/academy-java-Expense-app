#!/bin/sh
set -e
mkdir -p /app/uploads/receipts
chown -R spring:spring /app/uploads/receipts
exec runuser -u spring -- java -jar /app/app.jar
