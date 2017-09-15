'use strict';

const cluster = require('cluster');
const express = require('express');

if (cluster.isMaster) {
    let cpuCounter = require('os').cpus().length;
    for (let i = 0; i < cpuCounter; i++) {
        cluster.fork();
    }
} else {
    const apiRouter = require('./router/api.router');
    const webPageRouter = require('./router/web-page.router');

    let app = express();

    app.use('/api', apiRouter);
    app.use('/', webPageRouter);

    app.listen(3000, function() {
        console.log('Server started at port: 3000');
    });
}