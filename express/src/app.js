'use strict';

const express = require('express');

const apiRouter = require('./router/api.router');
const webPageRouter = require('./router/web-page.router');

let app = express();

app.use('/api', apiRouter);
app.use('/', webPageRouter);

app.listen(3000, function() {
    console.log('Backend started');
})