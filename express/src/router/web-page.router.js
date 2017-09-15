'use strict';

const express = require('express');

let router = express.Router();

router.get('/', function(request, response) {
    response.end('index');
})
router.get('/:short_url', function(request, response) {
    response.json(request.params);
})

module.exports = router;