'use strict';

const express = require('express');
const bodyParser = require('body-parser');

const urlConversionService = require('../service/url-conversion.service');

let router = express.Router();
let urlencodedParser = bodyParser.urlencoded({extended: false});

router.get('/', urlencodedParser, function(request, response) {
    let shortUrl = request.query['short_url'];
    if (shortUrl) {
        urlConversionService.findUrlConversionByShortUrl(shortUrl, function(error, urlConversionString) {
            if (error) {
                response.sendStatus(404);
            } else {
                response.end(urlConversionString);
            }
        });
    } else {
        response.sendStatus(404);
    }
})

router.post('/', urlencodedParser, function(request, response) {
    let longUrl = request.query['long_url'];
    if (longUrl) {
        urlConversionService.addByLongUrl(longUrl, function(error, urlConversion) {
            if (error) {
                response.json({short_url: ''});
            } else {
                response.json({short_url: urlConversion.short_url});
            }
        });
    } else {
        response.json({short_url: ''});
    }
});

module.exports = router;