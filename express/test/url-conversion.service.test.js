const chai = require('chai');

const expect = chai.expect;

const urlConversionService = require('../src/service/url-conversion.service');

describe('urlConversionService', function() {
    describe('#addIfNotExists', function() {
        it('should add without duplicates', function(done) {
            urlConversionService.addIfNotExists('www.google.com', function(error, firstUrlConversion) {
                urlConversionService.addIfNotExists('www.google.com', function(error, secondUrlConversion) {
                    expect(firstUrlConversion.short_url).to.equal(secondUrlConversion.short_url);
                    done();
                });
            });
        });
    });
    describe('#findUrlConversionByShortUrl', function() {
        it('should find without error', function(done) {
            urlConversionService.addIfNotExists('www.google.com', function(error, firstUrlConversion) {
                urlConversionService.findUrlConversionByShortUrl(firstUrlConversion.short_url, function(error, secondUrlConversion) {
                    expect(firstUrlConversion.long_url).to.equal(secondUrlConversion.long_url);
                    done();
                });
            });
        });
    });
});