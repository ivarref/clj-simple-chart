const { DOMImplementation, XMLSerializer } = require('@xmldom/xmldom');

function createSVG() {
    const document = new DOMImplementation().createDocument('http://www.w3.org/1999/xhtml', 'html', null);
    return document.createElementNS('http://www.w3.org/2000/svg', 'svg');
}

function svgToString(svg) {
    const xmlSerializer = new XMLSerializer();
    return xmlSerializer.serializeToString(svg);
}
