# BTS

## Development

### CSS compilation
We are using stylus as css preprocessor. 
All styles contains in `stylus/admin` - for admin panel and
`stylus/bts` - for main page, all in folder `stylus` - shared between admin/main page.
For compile and watch use
```bash
stylus -w stylus/admin/index.styl -o resources/public/css/admin.css
stylus -w stylus/bts/index.styl -o resources/public/css/main.css
```

## License

Copyright © 2018 4xor

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
