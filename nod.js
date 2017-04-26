
var fs = require('fs');
var b64 = require('base64-arraybuffer');
var opentype = require('opentype.js');

var base64str = fs.readFileSync('./hello.txt').toString();
var ab = b64.decode(base64str);
var font = opentype.parse(ab);
var path = font.getPath('hello world');
console.log(Object.keys(font))
console.log(font.names)