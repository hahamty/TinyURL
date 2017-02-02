'use strict';

const redis = require('redis');

let redisClient = redis.createClient();

module.exports = {
    redisClient: redisClient
}