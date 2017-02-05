'use strict';

const mongoose = require('mongoose');

const mongoUtil = require('../util/mongo.util');
const redisUtil = require('../util/redis.util');
const counterService = require('./counter.service');
const base58CodecService = require('./base58-codec.service');

let urlConversionMongoSchema = mongoose.Schema({
    short_url: {type: String, index: true},
    long_url: {type: String, index: true},
    created_time: Date
});

let UrlConversionMongoModel = mongoose.model('UrlConversion', urlConversionMongoSchema);

let redisClient = redisUtil.redisClient;

function addByLongUrl(longUrl, callback) {
    counterService.getNextSequenceByCounterName('shortUrlID', function(error, shortUrlIdInstance) {
        if (error) {
            console.log(error);
        }
        var shortUrl = base58CodecService.encode(shortUrlIdInstance.counter_value);
        if (shortUrl === 'api') {
            counterService.getNextSequenceByCounterName('shortUrlID', function(error, shortUrlIdInstance) {
                if (error) {
                    console.log(error);
                }
                var shortUrl = base58CodecService.encode(shortUrlIdInstance.counter_value);
                add(longUrl, shortUrl, callback);
            });
        } else {
            add(longUrl, shortUrl, callback);
        }
    });
}

function add(longUrl, shortUrl, callback) {
    let urlConversion = new UrlConversionMongoModel({short_url: shortUrl, long_url: longUrl, created_time: new Date()});
    urlConversion.save(function(error) {
        if (error) {
            console.log(error);
            callback(error, null);
        } else {
            callback(error, urlConversion);
            redisClient.set(shortUrl, JSON.stringify({
                short_url: urlConversion.short_url,
                long_url: urlConversion.long_url,
                created_time: urlConversion.created_time
            }));
        }
    });
}

/**
 * callback = function(error, urlConversionString)
 */
function findUrlConversionByShortUrl(shortUrl, callback) {
    redisClient.get(shortUrl, function(error, urlConversionString) {
        if (error) {
            console.log(error);
        }
        if (urlConversionString) {
            callback(error, urlConversionString);
        } else {
            UrlConversionMongoModel.findOne({short_url: shortUrl}, function(error, urlConversion) {
                if (error) {
                    console.log(error);
                }
                let urlConversionString = JSON.stringify({
                    short_url: urlConversion.short_url,
                    long_url: urlConversion.long_url,
                    created_time: urlConversion.created_time
                });
                callback(error, urlConversionString);
                redisClient.set(shortUrl, urlConversionString);
            });
        }
    });
}

module.exports = {
    addByLongUrl: addByLongUrl,
    findUrlConversionByShortUrl: findUrlConversionByShortUrl
};