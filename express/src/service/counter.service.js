'use strict';

const mongoose = require('mongoose');

const mongoUtil = require('../util/mongo.util');

let counterSchema = mongoose.Schema({
    counter_name: {type: String, index: true},
    counter_value: {type: Number, default: 0}
});

let CounterModel = mongoose.model('Counter', counterSchema);

let options = {
    upsert: true,
    fields: {
        _id: false,
        counter_name: true,
        counter_value: true
    }
}

function getNextSequenceByCounterName(counterName, callback) {
    CounterModel.findOneAndUpdate({counter_name: counterName}, {$inc: {counter_value: 1}}, options, function(error, counter) {
        if (error) {
            console.error(error);
            callback(error, counter);
        }
        if (counter == null) {
            counter = {
                counter_name: counterName,
                counter_value: 0
            };
        }
        callback(null, counter);
    });
}

module.exports = {
    getNextSequenceByCounterName: getNextSequenceByCounterName
};