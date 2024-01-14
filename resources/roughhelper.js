let { DOMImplementation, XMLSerializer } = require('@xmldom/xmldom');
// const rough = require('./resources/roughes2015.js');
let getBounds = require('svg-path-bounds');

function getSvg() {
    const document = new DOMImplementation().createDocument('http://www.w3.org/1999/xhtml', 'html', null);
    const svgElem = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
    svgElem.setAttribute("height", "900");
    svgElem.setAttribute("width", "900");
    return svgElem;
}

function toString(svg) {
    const xmlSerializer = new XMLSerializer();
    return xmlSerializer.serializeToString(svg);
}

function appendAndSerialize(svg, what) {
    svg.appendChild(what);
    return toString(svg);
}

function circle(cx, cy, d, opts) {
    const svg = getSvg();
    return appendAndSerialize(svg, rough.svg(svg).circle(cx, cy, d, JSON.parse(opts)));
}

function rectangle(x, y, w, h, opts) {
    const svg = getSvg();
    return appendAndSerialize(svg, rough.svg(svg).rectangle(x, y, w, h, JSON.parse(opts)));
}

function path(s, opts) {
    const svg = getSvg();
    return appendAndSerialize(svg, rough.svg(svg).path(s, JSON.parse(opts)));
}

function line(x1, y1, x2, y2, opts) {
    const svg = getSvg();
    return appendAndSerialize(svg, rough.svg(svg).line(x1, y1, x2, y2, JSON.parse(opts)));
}
