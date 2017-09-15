const chai = require('chai');

const expect = chai.expect;

const counterService = require('../src/service/counter.service');

describe('counterService', function() {
    describe('#getNextSequenceByCounterName', function() {
        it('should increment without error', function(done) {
            counterService.getNextSequenceByCounterName('short_url_counter', function(error, firstCounter) {
                counterService.getNextSequenceByCounterName('short_url_counter', function(error, secondCounter) {
                    expect(secondCounter.counter_value).to.equal(firstCounter.counter_value + 1);
                    done();
                });
            });
        });
    });
});