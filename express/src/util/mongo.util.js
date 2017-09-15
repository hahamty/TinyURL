'use strict';

const mongoose = require('mongoose');

let mongoUri = 'mongodb://localhost/tinyurl';

mongoose.connect(mongoUri);

let mongoDB = mongoose.connection;

mongoDB.on('error', console.error.bind(console, 'connection error:'));
mongoDB.once('open', function() {
    // console.log('MongoDB connected');
});

module.exports = {
    mongoDB: mongoDB
};