'use strict';

const mongoose = require('mongoose');

const mongoUtil = require('../util/mongo.util');
const counterService = require('./counter.service');
const base58CodecService = require('./base58-codec.service');

let urlConversionMongoSchema = mongoose.Schema({
    short_url: {type: String, index: true},
    long_url: {type: String, index: true},
    created_time: Date
});

let UrlConversionMongoModel = mongoose.model('UrlConversion', urlConversionMongoSchema);

function addIfNotExists(longUrl, callback) {
    findUrlConversionByLongUrl(longUrl, function(error, urlConversion) {
        if (error) {
            console.log(error);
        }
        if (urlConversion) {
            callback(error, urlConversion);
        } else {
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
    });
}

function findUrlConversionByLongUrl(longUrl, callback) {
    UrlConversionMongoModel.findOne({long_url: longUrl}, function(error, urlConversion) {
        if (error) {
            console.log(error);
        }
        callback(error, urlConversion);
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
        }
    });
}

function findUrlConversionByShortUrl(shortUrl, callback) {
    UrlConversionMongoModel.findOne({short_url: shortUrl}, function(error, urlConversion) {
        if (error) {
            console.log(error);
        }
        callback(error, urlConversion);
    });
}

module.exports = {
    addIfNotExists: addIfNotExists,
    findUrlConversionByShortUrl: findUrlConversionByShortUrl
};