let { DOMImplementation, XMLSerializer } = require('@xmldom/xmldom');
// const rough = require('./resources/roughes2015.js');

function createSVG() {
    const document = new DOMImplementation().createDocument('http://www.w3.org/1999/xhtml', 'html', null);
    return document.createElementNS('http://www.w3.org/2000/svg', 'svg');
}

function toString(svg) {
    const xmlSerializer = new XMLSerializer();
    return xmlSerializer.serializeToString(svg);
}

function rc() {
    return rough.svg(createSVG());
}

function janei() {
    const xmlSerializer = new XMLSerializer();
    const document = new DOMImplementation().createDocument('http://www.w3.org/1999/xhtml', 'html', null);
    const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
    const rc = rough.svg(svg);
    svg.appendChild(rc.rectangle(0, 0, 1, 1));
    return xmlSerializer.serializeToString(svg);
}

function circle(cx, cy, d, opts) {
    const xmlSerializer = new XMLSerializer();
    const document = new DOMImplementation().createDocument('http://www.w3.org/1999/xhtml', 'html', null);
    const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
    const rc = rough.svg(svg);
    svg.appendChild(rc.circle(cx, cy, d, JSON.parse(opts)));
    return xmlSerializer.serializeToString(svg);
}
