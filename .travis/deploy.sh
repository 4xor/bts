#!/usr/bin/env bash
set -e

stylus stylus/admin/index.styl -o resources/public/css/admin.css
stylus stylus/bts/index.styl -o resources/public/css/main.css
lein uberjar
lein docker-publish