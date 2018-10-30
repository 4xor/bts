#!/usr/bin/env bash
set -e
mkdir -p resources/public/css
stylus stylus/admin/index.styl -o resources/public/css/admin.css
stylus stylus/bts/index.styl -o resources/public/css/main.css
lein uberjar
lein docker-publish