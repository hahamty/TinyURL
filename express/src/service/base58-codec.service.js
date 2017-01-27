'use strict';

const base58String = '123456789abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ';

// Convert an integer into a string
function encode(num) {
    if (typeof num !== 'number' || parseInt(num) !== num) {
        throw 'Encode error: ' + num + ' is not an integer';
    }
    if (num < 0) {
        throw 'Encode error: ' + num + ' is less than 0';
    }
    var result = '';
    while (num >= 58) {
        let index = num % 58;
        result = base58String[index] + result;
        num = (num - index) / 58;
    }
    return base58String[num] + result;
}

// Convert a string into an integer
function decode(str) {
    if (typeof str !== 'string') {
        throw 'Decode error: ' + str + ' is not a string';
    }
    var result = 0;
    for (let i = 0; i < str.length; i++) {
        result *= 58;
        let index = base58String.indexOf(str[i]);
        if (index === -1) {
            throw 'Decode error: ' + str + ' is not a valid string';
        }
        result += index;
    }
    return result;
}

module.exports = {
    encode: encode
};