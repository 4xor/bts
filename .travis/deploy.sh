#!/usr/bin/env bash
stylus stylus/admin/index.styl -o resources/public/css/admin.css
stylus stylus/bts/index.styl -o resources/public/css/main.css
lein uberjar
lein docker-publish