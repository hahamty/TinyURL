const chai = require('chai');

const expect = chai.expect;

const urlConversionService = require('../src/service/url-conversion.service');

describe('urlConversionService', function() {
    describe('#findUrlConversionByShortUrl', function() {
        it('should find without error', function(done) {
            urlConversionService.addByLongUrl('www.google.com', function(error, firstUrlConversion) {
                urlConversionService.findUrlConversionByShortUrl(firstUrlConversion.short_url, function(error, secondUrlConversion) {
                    expect(firstUrlConversion.long_url).to.equal(secondUrlConversion.long_url);
                    done();
                });
            });
        });
    });
});