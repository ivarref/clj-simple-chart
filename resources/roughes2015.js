"use strict";

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var rough = function () {
    "use strict";

    var t = "undefined" != typeof self;

    var e = function () {
        function e(t, _e) {
            _classCallCheck(this, e);

            this.defaultOptions = {
                maxRandomnessOffset: 2,
                roughness: 1,
                bowing: 1,
                stroke: "#000",
                strokeWidth: 1,
                curveTightness: 0,
                curveStepCount: 9,
                fillStyle: "hachure",
                fillWeight: -1,
                hachureAngle: -41,
                hachureGap: -1,
                dashOffset: -1,
                dashGap: -1,
                zigzagOffset: -1
            }, this.config = t || {}, this.surface = _e, this.config.options && (this.defaultOptions = this._options(this.config.options));
        }

        _createClass(e, [{
            key: "_options",
            value: function _options(t) {
                return t ? Object.assign({}, this.defaultOptions, t) : this.defaultOptions;
            }
        }, {
            key: "_drawable",
            value: function _drawable(t, e, s) {
                return { shape: t, sets: e || [], options: s || this.defaultOptions };
            }
        }, {
            key: "getCanvasSize",
            value: function getCanvasSize() {
                var t = function t(_t) {
                    return _t && "object" == (typeof _t === "undefined" ? "undefined" : _typeof(_t)) && _t.baseVal && _t.baseVal.value ? _t.baseVal.value : _t || 100;
                };
                return this.surface ? [t(this.surface.width), t(this.surface.height)] : [100, 100];
            }
        }, {
            key: "computePolygonSize",
            value: function computePolygonSize(t) {
                if (t.length) {
                    var _e2 = t[0][0],
                        _s = t[0][0],
                        _i = t[0][1],
                        _h = t[0][1];
                    for (var _n = 1; _n < t.length; _n++) {
                        _e2 = Math.min(_e2, t[_n][0]), _s = Math.max(_s, t[_n][0]), _i = Math.min(_i, t[_n][1]), _h = Math.max(_h, t[_n][1]);
                    }return [_s - _e2, _h - _i];
                }
                return [0, 0];
            }
        }, {
            key: "polygonPath",
            value: function polygonPath(t) {
                var e = "";
                if (t.length) {
                    e = "M" + t[0][0] + "," + t[0][1];
                    for (var _s2 = 1; _s2 < t.length; _s2++) {
                        e = e + " L" + t[_s2][0] + "," + t[_s2][1];
                    }
                }
                return e;
            }
        }, {
            key: "computePathSize",
            value: function computePathSize(e) {
                var s = [0, 0];
                if (t && self.document) try {
                    var _t2 = "http://www.w3.org/2000/svg",
                        _i2 = self.document.createElementNS(_t2, "svg");
                    _i2.setAttribute("width", "0"), _i2.setAttribute("height", "0");
                    var _h2 = self.document.createElementNS(_t2, "path");
                    _h2.setAttribute("d", e), _i2.appendChild(_h2), self.document.body.appendChild(_i2);
                    var _n2 = _h2.getBBox();
                    _n2 && (s[0] = _n2.width || 0, s[1] = _n2.height || 0), self.document.body.removeChild(_i2);
                } catch (t) {}
                var i = this.getCanvasSize();
                return s[0] * s[1] || (s = i), s;
            }
        }, {
            key: "toPaths",
            value: function toPaths(t) {
                var e = t.sets || [],
                    s = t.options || this.defaultOptions,
                    i = [];
                var _iteratorNormalCompletion = true;
                var _didIteratorError = false;
                var _iteratorError = undefined;

                try {
                    for (var _iterator = e[Symbol.iterator](), _step; !(_iteratorNormalCompletion = (_step = _iterator.next()).done); _iteratorNormalCompletion = true) {
                        var _t3 = _step.value;

                        var _e3 = null;
                        switch (_t3.type) {
                            case "path":
                                _e3 = { d: this.opsToPath(_t3), stroke: s.stroke, strokeWidth: s.strokeWidth, fill: "none" };
                                break;
                            case "fillPath":
                                _e3 = { d: this.opsToPath(_t3), stroke: "none", strokeWidth: 0, fill: s.fill || "none" };
                                break;
                            case "fillSketch":
                                _e3 = this.fillSketch(_t3, s);
                                break;
                            case "path2Dfill":
                                _e3 = { d: _t3.path || "", stroke: "none", strokeWidth: 0, fill: s.fill || "none" };
                                break;
                            case "path2Dpattern":
                                {
                                    var _i3 = _t3.size,
                                        _h3 = { x: 0, y: 0, width: 1, height: 1, viewBox: "0 0 " + Math.round(_i3[0]) + " " + Math.round(_i3[1]), patternUnits: "objectBoundingBox", path: this.fillSketch(_t3, s) };
                                    _e3 = { d: _t3.path, stroke: "none", strokeWidth: 0, pattern: _h3 };
                                    break;
                                }
                        }
                        _e3 && i.push(_e3);
                    }
                } catch (err) {
                    _didIteratorError = true;
                    _iteratorError = err;
                } finally {
                    try {
                        if (!_iteratorNormalCompletion && _iterator.return) {
                            _iterator.return();
                        }
                    } finally {
                        if (_didIteratorError) {
                            throw _iteratorError;
                        }
                    }
                }

                return i;
            }
        }, {
            key: "fillSketch",
            value: function fillSketch(t, e) {
                var s = e.fillWeight;
                return s < 0 && (s = e.strokeWidth / 2), { d: this.opsToPath(t), stroke: e.fill || "none", strokeWidth: s, fill: "none" };
            }
        }, {
            key: "opsToPath",
            value: function opsToPath(t) {
                var e = "";
                var _iteratorNormalCompletion2 = true;
                var _didIteratorError2 = false;
                var _iteratorError2 = undefined;

                try {
                    for (var _iterator2 = t.ops[Symbol.iterator](), _step2; !(_iteratorNormalCompletion2 = (_step2 = _iterator2.next()).done); _iteratorNormalCompletion2 = true) {
                        var _s3 = _step2.value;

                        var _t4 = _s3.data;
                        switch (_s3.op) {
                            case "move":
                                e += "M" + _t4[0] + " " + _t4[1] + " ";
                                break;
                            case "bcurveTo":
                                e += "C" + _t4[0] + " " + _t4[1] + ", " + _t4[2] + " " + _t4[3] + ", " + _t4[4] + " " + _t4[5] + " ";
                                break;
                            case "qcurveTo":
                                e += "Q" + _t4[0] + " " + _t4[1] + ", " + _t4[2] + " " + _t4[3] + " ";
                                break;
                            case "lineTo":
                                e += "L" + _t4[0] + " " + _t4[1] + " ";
                        }
                    }
                } catch (err) {
                    _didIteratorError2 = true;
                    _iteratorError2 = err;
                } finally {
                    try {
                        if (!_iteratorNormalCompletion2 && _iterator2.return) {
                            _iterator2.return();
                        }
                    } finally {
                        if (_didIteratorError2) {
                            throw _iteratorError2;
                        }
                    }
                }

                return e.trim();
            }
        }]);

        return e;
    }();

    function s(t, e) {
        return t.type === e;
    }

    var i = { A: 7, a: 7, C: 6, c: 6, H: 1, h: 1, L: 2, l: 2, M: 2, m: 2, Q: 4, q: 4, S: 4, s: 4, T: 4, t: 2, V: 1, v: 1, Z: 0, z: 0 };

    var h = function () {
        function h(t) {
            _classCallCheck(this, h);

            this.COMMAND = 0, this.NUMBER = 1, this.EOD = 2, this.segments = [], this.parseData(t), this.processPoints();
        }

        _createClass(h, [{
            key: "tokenize",
            value: function tokenize(t) {
                var e = new Array();
                for (; "" !== t;) {
                    if (t.match(/^([ \t\r\n,]+)/)) t = t.substr(RegExp.$1.length);else if (t.match(/^([aAcChHlLmMqQsStTvVzZ])/)) e[e.length] = {
                        type: this.COMMAND,
                        text: RegExp.$1
                    }, t = t.substr(RegExp.$1.length);else {
                        if (!t.match(/^(([-+]?[0-9]+(\.[0-9]*)?|[-+]?\.[0-9]+)([eE][-+]?[0-9]+)?)/)) return console.error("Unrecognized segment command: " + t), [];
                        e[e.length] = { type: this.NUMBER, text: "" + parseFloat(RegExp.$1) }, t = t.substr(RegExp.$1.length);
                    }
                }return e[e.length] = { type: this.EOD, text: "" }, e;
            }
        }, {
            key: "parseData",
            value: function parseData(t) {
                var e = this.tokenize(t);
                var h = 0,
                    n = e[h],
                    a = "BOD";
                for (this.segments = new Array(); !s(n, this.EOD);) {
                    var _o = void 0;
                    var _r = new Array();
                    if ("BOD" === a) {
                        if ("M" !== n.text && "m" !== n.text) return void this.parseData("M0,0" + t);
                        h++, _o = i[n.text], a = n.text;
                    } else s(n, this.NUMBER) ? _o = i[a] : (h++, _o = i[n.text], a = n.text);
                    if (h + _o < e.length) {
                        for (var _t5 = h; _t5 < h + _o; _t5++) {
                            var _i4 = e[_t5];
                            if (!s(_i4, this.NUMBER)) return void console.error("Parameter type is not a number: " + a + "," + _i4.text);
                            _r[_r.length] = +_i4.text;
                        }
                        if ("number" != typeof i[a]) return void console.error("Unsupported segment type: " + a);
                        {
                            var _t6 = { key: a, data: _r };
                            this.segments.push(_t6), n = e[h += _o], "M" === a && (a = "L"), "m" === a && (a = "l");
                        }
                    } else console.error("Path data ended before all parameters were found");
                }
            }
        }, {
            key: "processPoints",
            value: function processPoints() {
                var t = null,
                    e = [0, 0];
                for (var _s4 = 0; _s4 < this.segments.length; _s4++) {
                    var _i5 = this.segments[_s4];
                    switch (_i5.key) {
                        case "M":
                        case "L":
                        case "T":
                            _i5.point = [_i5.data[0], _i5.data[1]];
                            break;
                        case "m":
                        case "l":
                        case "t":
                            _i5.point = [_i5.data[0] + e[0], _i5.data[1] + e[1]];
                            break;
                        case "H":
                            _i5.point = [_i5.data[0], e[1]];
                            break;
                        case "h":
                            _i5.point = [_i5.data[0] + e[0], e[1]];
                            break;
                        case "V":
                            _i5.point = [e[0], _i5.data[0]];
                            break;
                        case "v":
                            _i5.point = [e[0], _i5.data[0] + e[1]];
                            break;
                        case "z":
                        case "Z":
                            t && (_i5.point = [t[0], t[1]]);
                            break;
                        case "C":
                            _i5.point = [_i5.data[4], _i5.data[5]];
                            break;
                        case "c":
                            _i5.point = [_i5.data[4] + e[0], _i5.data[5] + e[1]];
                            break;
                        case "S":
                            _i5.point = [_i5.data[2], _i5.data[3]];
                            break;
                        case "s":
                            _i5.point = [_i5.data[2] + e[0], _i5.data[3] + e[1]];
                            break;
                        case "Q":
                            _i5.point = [_i5.data[2], _i5.data[3]];
                            break;
                        case "q":
                            _i5.point = [_i5.data[2] + e[0], _i5.data[3] + e[1]];
                            break;
                        case "A":
                            _i5.point = [_i5.data[5], _i5.data[6]];
                            break;
                        case "a":
                            _i5.point = [_i5.data[5] + e[0], _i5.data[6] + e[1]];
                    }
                    "m" !== _i5.key && "M" !== _i5.key || (t = null), _i5.point && (e = _i5.point, t || (t = _i5.point)), "z" !== _i5.key && "Z" !== _i5.key || (t = null);
                }
            }
        }, {
            key: "closed",
            get: function get() {
                if (void 0 === this._closed) {
                    this._closed = !1;
                    var _iteratorNormalCompletion3 = true;
                    var _didIteratorError3 = false;
                    var _iteratorError3 = undefined;

                    try {
                        for (var _iterator3 = this.segments[Symbol.iterator](), _step3; !(_iteratorNormalCompletion3 = (_step3 = _iterator3.next()).done); _iteratorNormalCompletion3 = true) {
                            var _t7 = _step3.value;
                            "z" === _t7.key.toLowerCase() && (this._closed = !0);
                        }
                    } catch (err) {
                        _didIteratorError3 = true;
                        _iteratorError3 = err;
                    } finally {
                        try {
                            if (!_iteratorNormalCompletion3 && _iterator3.return) {
                                _iterator3.return();
                            }
                        } finally {
                            if (_didIteratorError3) {
                                throw _iteratorError3;
                            }
                        }
                    }
                }
                return this._closed;
            }
        }]);

        return h;
    }();

    var n = function () {
        function n(t) {
            _classCallCheck(this, n);

            this._position = [0, 0], this._first = null, this.bezierReflectionPoint = null, this.quadReflectionPoint = null, this.parsed = new h(t);
        }

        _createClass(n, [{
            key: "setPosition",
            value: function setPosition(t, e) {
                this._position = [t, e], this._first || (this._first = [t, e]);
            }
        }, {
            key: "segments",
            get: function get() {
                return this.parsed.segments;
            }
        }, {
            key: "closed",
            get: function get() {
                return this.parsed.closed;
            }
        }, {
            key: "linearPoints",
            get: function get() {
                if (!this._linearPoints) {
                    var _t8 = [];
                    var _e4 = [];
                    var _iteratorNormalCompletion4 = true;
                    var _didIteratorError4 = false;
                    var _iteratorError4 = undefined;

                    try {
                        for (var _iterator4 = this.parsed.segments[Symbol.iterator](), _step4; !(_iteratorNormalCompletion4 = (_step4 = _iterator4.next()).done); _iteratorNormalCompletion4 = true) {
                            var _s5 = _step4.value;

                            var _i6 = _s5.key.toLowerCase();
                            ("m" !== _i6 && "z" !== _i6 || (_e4.length && (_t8.push(_e4), _e4 = []), "z" !== _i6)) && _s5.point && _e4.push(_s5.point);
                        }
                    } catch (err) {
                        _didIteratorError4 = true;
                        _iteratorError4 = err;
                    } finally {
                        try {
                            if (!_iteratorNormalCompletion4 && _iterator4.return) {
                                _iterator4.return();
                            }
                        } finally {
                            if (_didIteratorError4) {
                                throw _iteratorError4;
                            }
                        }
                    }

                    _e4.length && (_t8.push(_e4), _e4 = []), this._linearPoints = _t8;
                }
                return this._linearPoints;
            }
        }, {
            key: "first",
            get: function get() {
                return this._first;
            },
            set: function set(t) {
                this._first = t;
            }
        }, {
            key: "position",
            get: function get() {
                return this._position;
            }
        }, {
            key: "x",
            get: function get() {
                return this._position[0];
            }
        }, {
            key: "y",
            get: function get() {
                return this._position[1];
            }
        }]);

        return n;
    }();

    var a = function () {
        function a(t, e, s, i, h, n) {
            _classCallCheck(this, a);

            if (this._segIndex = 0, this._numSegs = 0, this._rx = 0, this._ry = 0, this._sinPhi = 0, this._cosPhi = 0, this._C = [0, 0], this._theta = 0, this._delta = 0, this._T = 0, this._from = t, t[0] === e[0] && t[1] === e[1]) return;
            var _a = Math.PI / 180;
            this._rx = Math.abs(s[0]), this._ry = Math.abs(s[1]), this._sinPhi = Math.sin(i * _a), this._cosPhi = Math.cos(i * _a);
            var o = this._cosPhi * (t[0] - e[0]) / 2 + this._sinPhi * (t[1] - e[1]) / 2,
                r = -this._sinPhi * (t[0] - e[0]) / 2 + this._cosPhi * (t[1] - e[1]) / 2;
            var l = 0;
            var c = this._rx * this._rx * this._ry * this._ry - this._rx * this._rx * r * r - this._ry * this._ry * o * o;
            if (c < 0) {
                var _t9 = Math.sqrt(1 - c / (this._rx * this._rx * this._ry * this._ry));
                this._rx = this._rx * _t9, this._ry = this._ry * _t9, l = 0;
            } else l = (h === n ? -1 : 1) * Math.sqrt(c / (this._rx * this._rx * r * r + this._ry * this._ry * o * o));
            var p = l * this._rx * r / this._ry,
                u = -l * this._ry * o / this._rx;
            this._C = [0, 0], this._C[0] = this._cosPhi * p - this._sinPhi * u + (t[0] + e[0]) / 2, this._C[1] = this._sinPhi * p + this._cosPhi * u + (t[1] + e[1]) / 2, this._theta = this.calculateVectorAngle(1, 0, (o - p) / this._rx, (r - u) / this._ry);
            var f = this.calculateVectorAngle((o - p) / this._rx, (r - u) / this._ry, (-o - p) / this._rx, (-r - u) / this._ry);
            !n && f > 0 ? f -= 2 * Math.PI : n && f < 0 && (f += 2 * Math.PI), this._numSegs = Math.ceil(Math.abs(f / (Math.PI / 2))), this._delta = f / this._numSegs, this._T = 8 / 3 * Math.sin(this._delta / 4) * Math.sin(this._delta / 4) / Math.sin(this._delta / 2);
        }

        _createClass(a, [{
            key: "getNextSegment",
            value: function getNextSegment() {
                if (this._segIndex === this._numSegs) return null;
                var t = Math.cos(this._theta),
                    e = Math.sin(this._theta),
                    s = this._theta + this._delta,
                    i = Math.cos(s),
                    h = Math.sin(s),
                    n = [this._cosPhi * this._rx * i - this._sinPhi * this._ry * h + this._C[0], this._sinPhi * this._rx * i + this._cosPhi * this._ry * h + this._C[1]],
                    a = [this._from[0] + this._T * (-this._cosPhi * this._rx * e - this._sinPhi * this._ry * t), this._from[1] + this._T * (-this._sinPhi * this._rx * e + this._cosPhi * this._ry * t)],
                    o = [n[0] + this._T * (this._cosPhi * this._rx * h + this._sinPhi * this._ry * i), n[1] + this._T * (this._sinPhi * this._rx * h - this._cosPhi * this._ry * i)];
                return this._theta = s, this._from = [n[0], n[1]], this._segIndex++, { cp1: a, cp2: o, to: n };
            }
        }, {
            key: "calculateVectorAngle",
            value: function calculateVectorAngle(t, e, s, i) {
                var h = Math.atan2(e, t),
                    n = Math.atan2(i, s);
                return n >= h ? n - h : 2 * Math.PI - (h - n);
            }
        }]);

        return a;
    }();

    var o = function () {
        function o(t, e) {
            _classCallCheck(this, o);

            this.sets = t, this.closed = e;
        }

        _createClass(o, [{
            key: "fit",
            value: function fit(t) {
                var e = [];
                var _iteratorNormalCompletion5 = true;
                var _didIteratorError5 = false;
                var _iteratorError5 = undefined;

                try {
                    for (var _iterator5 = this.sets[Symbol.iterator](), _step5; !(_iteratorNormalCompletion5 = (_step5 = _iterator5.next()).done); _iteratorNormalCompletion5 = true) {
                        var _s6 = _step5.value;

                        var _i7 = _s6.length;
                        var _h4 = Math.floor(t * _i7);
                        if (_h4 < 5) {
                            if (_i7 <= 5) continue;
                            _h4 = 5;
                        }
                        e.push(this.reduce(_s6, _h4));
                    }
                } catch (err) {
                    _didIteratorError5 = true;
                    _iteratorError5 = err;
                } finally {
                    try {
                        if (!_iteratorNormalCompletion5 && _iterator5.return) {
                            _iterator5.return();
                        }
                    } finally {
                        if (_didIteratorError5) {
                            throw _iteratorError5;
                        }
                    }
                }

                var s = "";
                var _iteratorNormalCompletion6 = true;
                var _didIteratorError6 = false;
                var _iteratorError6 = undefined;

                try {
                    for (var _iterator6 = e[Symbol.iterator](), _step6; !(_iteratorNormalCompletion6 = (_step6 = _iterator6.next()).done); _iteratorNormalCompletion6 = true) {
                        var _t10 = _step6.value;

                        for (var _e5 = 0; _e5 < _t10.length; _e5++) {
                            var _i8 = _t10[_e5];
                            s += 0 === _e5 ? "M" + _i8[0] + "," + _i8[1] : "L" + _i8[0] + "," + _i8[1];
                        }
                        this.closed && (s += "z ");
                    }
                } catch (err) {
                    _didIteratorError6 = true;
                    _iteratorError6 = err;
                } finally {
                    try {
                        if (!_iteratorNormalCompletion6 && _iterator6.return) {
                            _iterator6.return();
                        }
                    } finally {
                        if (_didIteratorError6) {
                            throw _iteratorError6;
                        }
                    }
                }

                return s;
            }
        }, {
            key: "distance",
            value: function distance(t, e) {
                return Math.sqrt(Math.pow(t[0] - e[0], 2) + Math.pow(t[1] - e[1], 2));
            }
        }, {
            key: "reduce",
            value: function reduce(t, e) {
                if (t.length <= e) return t;
                var s = t.slice(0);
                for (; s.length > e;) {
                    var _t11 = -1,
                        _e6 = -1;
                    for (var _i9 = 1; _i9 < s.length - 1; _i9++) {
                        var _h5 = this.distance(s[_i9 - 1], s[_i9]),
                            _n3 = this.distance(s[_i9], s[_i9 + 1]),
                            _a2 = this.distance(s[_i9 - 1], s[_i9 + 1]),
                            _o2 = (_h5 + _n3 + _a2) / 2,
                            _r2 = Math.sqrt(_o2 * (_o2 - _h5) * (_o2 - _n3) * (_o2 - _a2));
                        (_t11 < 0 || _r2 < _t11) && (_t11 = _r2, _e6 = _i9);
                    }
                    if (!(_e6 > 0)) break;
                    s.splice(_e6, 1);
                }
                return s;
            }
        }]);

        return o;
    }();

    var r = function () {
        function r(t, e) {
            _classCallCheck(this, r);

            this.xi = Number.MAX_VALUE, this.yi = Number.MAX_VALUE, this.px1 = t[0], this.py1 = t[1], this.px2 = e[0], this.py2 = e[1], this.a = this.py2 - this.py1, this.b = this.px1 - this.px2, this.c = this.px2 * this.py1 - this.px1 * this.py2, this._undefined = 0 === this.a && 0 === this.b && 0 === this.c;
        }

        _createClass(r, [{
            key: "isUndefined",
            value: function isUndefined() {
                return this._undefined;
            }
        }, {
            key: "intersects",
            value: function intersects(t) {
                if (this.isUndefined() || t.isUndefined()) return !1;
                var e = Number.MAX_VALUE,
                    s = Number.MAX_VALUE,
                    i = 0,
                    h = 0;
                var n = this.a,
                    a = this.b,
                    o = this.c;
                return Math.abs(a) > 1e-5 && (e = -n / a, i = -o / a), Math.abs(t.b) > 1e-5 && (s = -t.a / t.b, h = -t.c / t.b), e === Number.MAX_VALUE ? s === Number.MAX_VALUE ? -o / n == -t.c / t.a && (this.py1 >= Math.min(t.py1, t.py2) && this.py1 <= Math.max(t.py1, t.py2) ? (this.xi = this.px1, this.yi = this.py1, !0) : this.py2 >= Math.min(t.py1, t.py2) && this.py2 <= Math.max(t.py1, t.py2) && (this.xi = this.px2, this.yi = this.py2, !0)) : (this.xi = this.px1, this.yi = s * this.xi + h, !((this.py1 - this.yi) * (this.yi - this.py2) < -1e-5 || (t.py1 - this.yi) * (this.yi - t.py2) < -1e-5) && (!(Math.abs(t.a) < 1e-5) || !((t.px1 - this.xi) * (this.xi - t.px2) < -1e-5))) : s === Number.MAX_VALUE ? (this.xi = t.px1, this.yi = e * this.xi + i, !((t.py1 - this.yi) * (this.yi - t.py2) < -1e-5 || (this.py1 - this.yi) * (this.yi - this.py2) < -1e-5) && (!(Math.abs(n) < 1e-5) || !((this.px1 - this.xi) * (this.xi - this.px2) < -1e-5))) : e === s ? i === h && (this.px1 >= Math.min(t.px1, t.px2) && this.px1 <= Math.max(t.py1, t.py2) ? (this.xi = this.px1, this.yi = this.py1, !0) : this.px2 >= Math.min(t.px1, t.px2) && this.px2 <= Math.max(t.px1, t.px2) && (this.xi = this.px2, this.yi = this.py2, !0)) : (this.xi = (h - i) / (e - s), this.yi = e * this.xi + i, !((this.px1 - this.xi) * (this.xi - this.px2) < -1e-5 || (t.px1 - this.xi) * (this.xi - t.px2) < -1e-5));
            }
        }]);

        return r;
    }();

    function l(t, e) {
        var s = t[1][1] - t[0][1],
            i = t[0][0] - t[1][0],
            h = s * t[0][0] + i * t[0][1],
            n = e[1][1] - e[0][1],
            a = e[0][0] - e[1][0],
            o = n * e[0][0] + a * e[0][1],
            r = s * a - n * i;
        return r ? [Math.round((a * h - i * o) / r), Math.round((s * o - n * h) / r)] : null;
    }

    var c = function () {
        function c(t, e, s, i, h, n, a, o) {
            _classCallCheck(this, c);

            this.deltaX = 0, this.hGap = 0, this.top = t, this.bottom = e, this.left = s, this.right = i, this.gap = h, this.sinAngle = n, this.tanAngle = o, Math.abs(n) < 1e-4 ? this.pos = s + h : Math.abs(n) > .9999 ? this.pos = t + h : (this.deltaX = (e - t) * Math.abs(o), this.pos = s - Math.abs(this.deltaX), this.hGap = Math.abs(h / a), this.sLeft = new r([s, e], [s, t]), this.sRight = new r([i, e], [i, t]));
        }

        _createClass(c, [{
            key: "nextLine",
            value: function nextLine() {
                if (Math.abs(this.sinAngle) < 1e-4) {
                    if (this.pos < this.right) {
                        var _t12 = [this.pos, this.top, this.pos, this.bottom];
                        return this.pos += this.gap, _t12;
                    }
                } else if (Math.abs(this.sinAngle) > .9999) {
                    if (this.pos < this.bottom) {
                        var _t13 = [this.left, this.pos, this.right, this.pos];
                        return this.pos += this.gap, _t13;
                    }
                } else {
                    var _t14 = this.pos - this.deltaX / 2,
                        _e7 = this.pos + this.deltaX / 2,
                        _s7 = this.bottom,
                        _i10 = this.top;
                    if (this.pos < this.right + this.deltaX) {
                        for (; _t14 < this.left && _e7 < this.left || _t14 > this.right && _e7 > this.right;) {
                            if (this.pos += this.hGap, _t14 = this.pos - this.deltaX / 2, _e7 = this.pos + this.deltaX / 2, this.pos > this.right + this.deltaX) return null;
                        }var _h6 = new r([_t14, _s7], [_e7, _i10]);
                        this.sLeft && _h6.intersects(this.sLeft) && (_t14 = _h6.xi, _s7 = _h6.yi), this.sRight && _h6.intersects(this.sRight) && (_e7 = _h6.xi, _i10 = _h6.yi), this.tanAngle > 0 && (_t14 = this.right - (_t14 - this.left), _e7 = this.right - (_e7 - this.left));
                        var _n4 = [_t14, _s7, _e7, _i10];
                        return this.pos += this.hGap, _n4;
                    }
                }
                return null;
            }
        }]);

        return c;
    }();

    function p(t) {
        var e = t[0],
            s = t[1];
        return Math.sqrt(Math.pow(e[0] - s[0], 2) + Math.pow(e[1] - s[1], 2));
    }

    function u(t, e) {
        var s = [],
            i = new r([t[0], t[1]], [t[2], t[3]]);
        for (var _t15 = 0; _t15 < e.length; _t15++) {
            var _h7 = new r(e[_t15], e[(_t15 + 1) % e.length]);
            i.intersects(_h7) && s.push([i.xi, i.yi]);
        }
        return s;
    }

    function f(t, e, s, i, h, n, a) {
        return [-s * n - i * h + s + n * t + h * e, a * (s * h - i * n) + i + -a * h * t + a * n * e];
    }

    function d(t, e) {
        var s = [];
        if (t && t.length) {
            var _i11 = t[0][0],
                _h8 = t[0][0],
                _n5 = t[0][1],
                _a3 = t[0][1];
            for (var _e8 = 1; _e8 < t.length; _e8++) {
                _i11 = Math.min(_i11, t[_e8][0]), _h8 = Math.max(_h8, t[_e8][0]), _n5 = Math.min(_n5, t[_e8][1]), _a3 = Math.max(_a3, t[_e8][1]);
            }var _o3 = e.hachureAngle;
            var _r3 = e.hachureGap;
            _r3 < 0 && (_r3 = 4 * e.strokeWidth), _r3 = Math.max(_r3, .1);
            var _l = _o3 % 180 * (Math.PI / 180),
                _p = Math.cos(_l),
                _f = Math.sin(_l),
                _d = Math.tan(_l),
                _g = new c(_n5 - 1, _a3 + 1, _i11 - 1, _h8 + 1, _r3, _f, _p, _d);
            var _y = void 0;
            for (; null != (_y = _g.nextLine());) {
                var _e9 = u(_y, t);
                for (var _t16 = 0; _t16 < _e9.length; _t16++) {
                    if (_t16 < _e9.length - 1) {
                        var _i12 = _e9[_t16],
                            _h9 = _e9[_t16 + 1];
                        s.push([_i12, _h9]);
                    }
                }
            }
        }
        return s;
    }

    function g(t, e, s, i, h, n) {
        var a = [];
        var o = Math.abs(i / 2),
            r = Math.abs(h / 2);
        o += t.randOffset(.05 * o, n), r += t.randOffset(.05 * r, n);
        var l = n.hachureAngle;
        var c = n.hachureGap;
        c <= 0 && (c = 4 * n.strokeWidth);
        var p = n.fillWeight;
        p < 0 && (p = n.strokeWidth / 2);
        var u = l % 180 * (Math.PI / 180),
            d = Math.tan(u),
            g = r / o,
            y = Math.sqrt(g * d * g * d + 1),
            M = g * d / y,
            x = 1 / y,
            _ = c / (o * r / Math.sqrt(r * x * (r * x) + o * M * (o * M)) / o);
        var b = Math.sqrt(o * o - (e - o + _) * (e - o + _));
        for (var _t17 = e - o + _; _t17 < e + o; _t17 += _) {
            var _i13 = f(_t17, s - (b = Math.sqrt(o * o - (e - _t17) * (e - _t17))), e, s, M, x, g),
                _h10 = f(_t17, s + b, e, s, M, x, g);
            a.push([_i13, _h10]);
        }
        return a;
    }

    var y = function () {
        function y(t) {
            _classCallCheck(this, y);

            this.helper = t;
        }

        _createClass(y, [{
            key: "fillPolygon",
            value: function fillPolygon(t, e) {
                return this._fillPolygon(t, e);
            }
        }, {
            key: "fillEllipse",
            value: function fillEllipse(t, e, s, i, h) {
                return this._fillEllipse(t, e, s, i, h);
            }
        }, {
            key: "fillArc",
            value: function fillArc(t, e, s, i, h, n, a) {
                return null;
            }
        }, {
            key: "_fillPolygon",
            value: function _fillPolygon(t, e) {
                var s = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : !1;

                var i = d(t, e);
                return { type: "fillSketch", ops: this.renderLines(i, e, s) };
            }
        }, {
            key: "_fillEllipse",
            value: function _fillEllipse(t, e, s, i, h) {
                var n = arguments.length > 5 && arguments[5] !== undefined ? arguments[5] : !1;

                var a = g(this.helper, t, e, s, i, h);
                return { type: "fillSketch", ops: this.renderLines(a, h, n) };
            }
        }, {
            key: "renderLines",
            value: function renderLines(t, e, s) {
                var i = [],
                    h = null;
                var _iteratorNormalCompletion7 = true;
                var _didIteratorError7 = false;
                var _iteratorError7 = undefined;

                try {
                    for (var _iterator7 = t[Symbol.iterator](), _step7; !(_iteratorNormalCompletion7 = (_step7 = _iterator7.next()).done); _iteratorNormalCompletion7 = true) {
                        var _n6 = _step7.value;
                        i = i.concat(this.helper.doubleLineOps(_n6[0][0], _n6[0][1], _n6[1][0], _n6[1][1], e)), s && h && (i = i.concat(this.helper.doubleLineOps(h[0], h[1], _n6[0][0], _n6[0][1], e))), h = _n6[1];
                    }
                } catch (err) {
                    _didIteratorError7 = true;
                    _iteratorError7 = err;
                } finally {
                    try {
                        if (!_iteratorNormalCompletion7 && _iterator7.return) {
                            _iterator7.return();
                        }
                    } finally {
                        if (_didIteratorError7) {
                            throw _iteratorError7;
                        }
                    }
                }

                return i;
            }
        }]);

        return y;
    }();

    var M = function (_y2) {
        _inherits(M, _y2);

        function M() {
            _classCallCheck(this, M);

            return _possibleConstructorReturn(this, (M.__proto__ || Object.getPrototypeOf(M)).apply(this, arguments));
        }

        _createClass(M, [{
            key: "fillPolygon",
            value: function fillPolygon(t, e) {
                return this._fillPolygon(t, e, !0);
            }
        }, {
            key: "fillEllipse",
            value: function fillEllipse(t, e, s, i, h) {
                return this._fillEllipse(t, e, s, i, h, !0);
            }
        }]);

        return M;
    }(y);

    var x = function (_y3) {
        _inherits(x, _y3);

        function x() {
            _classCallCheck(this, x);

            return _possibleConstructorReturn(this, (x.__proto__ || Object.getPrototypeOf(x)).apply(this, arguments));
        }

        _createClass(x, [{
            key: "fillPolygon",
            value: function fillPolygon(t, e) {
                var s = this._fillPolygon(t, e),
                    i = Object.assign({}, e, { hachureAngle: e.hachureAngle + 90 }),
                    h = this._fillPolygon(t, i);
                return s.ops = s.ops.concat(h.ops), s;
            }
        }, {
            key: "fillEllipse",
            value: function fillEllipse(t, e, s, i, h) {
                var n = this._fillEllipse(t, e, s, i, h),
                    a = Object.assign({}, h, { hachureAngle: h.hachureAngle + 90 }),
                    o = this._fillEllipse(t, e, s, i, a);
                return n.ops = n.ops.concat(o.ops), n;
            }
        }]);

        return x;
    }(y);

    var _ = function () {
        function _(t) {
            _classCallCheck(this, _);

            this.helper = t;
        }

        _createClass(_, [{
            key: "fillPolygon",
            value: function fillPolygon(t, e) {
                var s = d(t, e = Object.assign({}, e, { curveStepCount: 4, hachureAngle: 0 }));
                return this.dotsOnLines(s, e);
            }
        }, {
            key: "fillEllipse",
            value: function fillEllipse(t, e, s, i, h) {
                h = Object.assign({}, h, { curveStepCount: 4, hachureAngle: 0 });
                var n = g(this.helper, t, e, s, i, h);
                return this.dotsOnLines(n, h);
            }
        }, {
            key: "fillArc",
            value: function fillArc(t, e, s, i, h, n, a) {
                return null;
            }
        }, {
            key: "dotsOnLines",
            value: function dotsOnLines(t, e) {
                var s = [],
                    i = e.hachureGap;
                i < 0 && (i = 4 * e.strokeWidth), i = Math.max(i, .1);
                var h = e.fillWeight;
                h < 0 && (h = e.strokeWidth / 2);
                var _iteratorNormalCompletion8 = true;
                var _didIteratorError8 = false;
                var _iteratorError8 = undefined;

                try {
                    for (var _iterator8 = t[Symbol.iterator](), _step8; !(_iteratorNormalCompletion8 = (_step8 = _iterator8.next()).done); _iteratorNormalCompletion8 = true) {
                        var n = _step8.value;

                        var _t18 = p(n) / i,
                            _a4 = Math.ceil(_t18) - 1,
                            _o4 = Math.atan((n[1][1] - n[0][1]) / (n[1][0] - n[0][0]));
                        for (var _t19 = 0; _t19 < _a4; _t19++) {
                            var _a5 = i * (_t19 + 1),
                                _r4 = _a5 * Math.sin(_o4),
                                _l2 = _a5 * Math.cos(_o4),
                                _c = [n[0][0] - _l2, n[0][1] + _r4],
                                _p2 = this.helper.randOffsetWithRange(_c[0] - i / 4, _c[0] + i / 4, e),
                                _u = this.helper.randOffsetWithRange(_c[1] - i / 4, _c[1] + i / 4, e),
                                _f2 = this.helper.ellipse(_p2, _u, h, h, e);
                            s = s.concat(_f2.ops);
                        }
                    }
                } catch (err) {
                    _didIteratorError8 = true;
                    _iteratorError8 = err;
                } finally {
                    try {
                        if (!_iteratorNormalCompletion8 && _iterator8.return) {
                            _iterator8.return();
                        }
                    } finally {
                        if (_didIteratorError8) {
                            throw _iteratorError8;
                        }
                    }
                }

                return { type: "fillSketch", ops: s };
            }
        }]);

        return _;
    }();

    var b = function () {
        function b(t) {
            _classCallCheck(this, b);

            this.helper = t;
        }

        _createClass(b, [{
            key: "fillPolygon",
            value: function fillPolygon(t, e) {
                var s = [Number.MAX_SAFE_INTEGER, Number.MIN_SAFE_INTEGER],
                    i = [Number.MAX_SAFE_INTEGER, Number.MIN_SAFE_INTEGER];
                t.forEach(function (t) {
                    s[0] = Math.min(s[0], t[0]), s[1] = Math.max(s[1], t[0]), i[0] = Math.min(i[0], t[1]), i[1] = Math.max(i[1], t[1]);
                });
                var h = function (t) {
                    var e = 0,
                        s = 0,
                        i = 0;
                    for (var _s8 = 0; _s8 < t.length; _s8++) {
                        var _i14 = t[_s8],
                            _h11 = _s8 === t.length - 1 ? t[0] : t[_s8 + 1];
                        e += _i14[0] * _h11[1] - _h11[0] * _i14[1];
                    }
                    e /= 2;
                    for (var _e10 = 0; _e10 < t.length; _e10++) {
                        var _h12 = t[_e10],
                            _n7 = _e10 === t.length - 1 ? t[0] : t[_e10 + 1];
                        s += (_h12[0] + _n7[0]) * (_h12[0] * _n7[1] - _n7[0] * _h12[1]), i += (_h12[1] + _n7[1]) * (_h12[0] * _n7[1] - _n7[0] * _h12[1]);
                    }
                    return [s / (6 * e), i / (6 * e)];
                }(t),
                    n = Math.max(Math.sqrt(Math.pow(h[0] - s[0], 2) + Math.pow(h[1] - i[0], 2)), Math.sqrt(Math.pow(h[0] - s[1], 2) + Math.pow(h[1] - i[1], 2))),
                    a = e.hachureGap > 0 ? e.hachureGap : 4 * e.strokeWidth,
                    o = [];
                if (t.length > 2) for (var _e11 = 0; _e11 < t.length; _e11++) {
                    _e11 === t.length - 1 ? o.push([t[_e11], t[0]]) : o.push([t[_e11], t[_e11 + 1]]);
                }var r = [];
                var c = Math.max(1, Math.PI * n / a);

                var _loop = function _loop(_t20) {
                    var e = _t20 * Math.PI / c,
                        a = [h, [h[0] + n * Math.cos(e), h[1] + n * Math.sin(e)]];
                    o.forEach(function (t) {
                        var e = l(t, a);
                        e && e[0] >= s[0] && e[0] <= s[1] && e[1] >= i[0] && e[1] <= i[1] && r.push(e);
                    });
                };

                for (var _t20 = 0; _t20 < c; _t20++) {
                    _loop(_t20);
                }
                r = this.removeDuplocatePoints(r);
                var p = this.createLinesFromCenter(h, r);
                return { type: "fillSketch", ops: this.drawLines(p, e) };
            }
        }, {
            key: "fillEllipse",
            value: function fillEllipse(t, e, s, i, h) {
                return this.fillArcSegment(t, e, s, i, 0, 2 * Math.PI, h);
            }
        }, {
            key: "fillArc",
            value: function fillArc(t, e, s, i, h, n, a) {
                return this.fillArcSegment(t, e, s, i, h, n, a);
            }
        }, {
            key: "fillArcSegment",
            value: function fillArcSegment(t, e, s, i, h, n, a) {
                var o = [t, e],
                    r = s / 2,
                    l = i / 2,
                    c = Math.max(s / 2, i / 2);
                var p = a.hachureGap;
                p < 0 && (p = 4 * a.strokeWidth);
                var u = Math.max(1, Math.abs(n - h) * c / p);
                var f = [];
                for (var _t21 = 0; _t21 < u; _t21++) {
                    var _e12 = _t21 * ((n - h) / u) + h,
                        _s9 = c * Math.cos(_e12),
                        _i15 = c * Math.sin(_e12),
                        _a6 = Math.sqrt(r * r * _i15 * _i15 + l * l * _s9 * _s9),
                        _p3 = r * l * _s9 / _a6,
                        _d2 = r * l * _i15 / _a6;
                    f.push([o[0] + _p3, o[1] + _d2]);
                }
                f = this.removeDuplocatePoints(f);
                var d = this.createLinesFromCenter(o, f);
                return { type: "fillSketch", ops: this.drawLines(d, a) };
            }
        }, {
            key: "drawLines",
            value: function drawLines(t, e) {
                var _this3 = this;

                var s = [];
                return t.forEach(function (t) {
                    var i = t[0],
                        h = t[1];
                    s = s.concat(_this3.helper.doubleLineOps(i[0], i[1], h[0], h[1], e));
                }), s;
            }
        }, {
            key: "createLinesFromCenter",
            value: function createLinesFromCenter(t, e) {
                return e.map(function (e) {
                    return [t, e];
                });
            }
        }, {
            key: "removeDuplocatePoints",
            value: function removeDuplocatePoints(t) {
                var e = new Set();
                return t.filter(function (t) {
                    var s = t.join(",");
                    return !e.has(s) && (e.add(s), !0);
                });
            }
        }]);

        return b;
    }();

    var m = function () {
        function m(t) {
            _classCallCheck(this, m);

            this.helper = t;
        }

        _createClass(m, [{
            key: "fillPolygon",
            value: function fillPolygon(t, e) {
                var s = d(t, e);
                return { type: "fillSketch", ops: this.dashedLine(s, e) };
            }
        }, {
            key: "fillEllipse",
            value: function fillEllipse(t, e, s, i, h) {
                var n = g(this.helper, t, e, s, i, h);
                return { type: "fillSketch", ops: this.dashedLine(n, h) };
            }
        }, {
            key: "fillArc",
            value: function fillArc(t, e, s, i, h, n, a) {
                return null;
            }
        }, {
            key: "dashedLine",
            value: function dashedLine(t, e) {
                var _this4 = this;

                var s = e.dashOffset < 0 ? e.hachureGap < 0 ? 4 * e.strokeWidth : e.hachureGap : e.dashOffset,
                    i = e.dashGap < 0 ? e.hachureGap < 0 ? 4 * e.strokeWidth : e.hachureGap : e.dashGap;
                var h = [];
                return t.forEach(function (t) {
                    var n = p(t),
                        a = Math.floor(n / (s + i)),
                        o = (n + i - a * (s + i)) / 2;
                    var r = t[0],
                        l = t[1];
                    r[0] > l[0] && (r = t[1], l = t[0]);
                    var c = Math.atan((l[1] - r[1]) / (l[0] - r[0]));
                    for (var _t22 = 0; _t22 < a; _t22++) {
                        var _n8 = _t22 * (s + i),
                            _a7 = _n8 + s,
                            _l3 = [r[0] + _n8 * Math.cos(c) + o * Math.cos(c), r[1] + _n8 * Math.sin(c) + o * Math.sin(c)],
                            _p4 = [r[0] + _a7 * Math.cos(c) + o * Math.cos(c), r[1] + _a7 * Math.sin(c) + o * Math.sin(c)];
                        h = h.concat(_this4.helper.doubleLineOps(_l3[0], _l3[1], _p4[0], _p4[1], e));
                    }
                }), h;
            }
        }]);

        return m;
    }();

    var w = function () {
        function w(t) {
            _classCallCheck(this, w);

            this.helper = t;
        }

        _createClass(w, [{
            key: "fillPolygon",
            value: function fillPolygon(t, e) {
                var s = e.hachureGap < 0 ? 4 * e.strokeWidth : e.hachureGap,
                    i = e.zigzagOffset < 0 ? s : e.zigzagOffset,
                    h = d(t, e = Object.assign({}, e, { hachureGap: s + i }));
                return { type: "fillSketch", ops: this.zigzagLines(h, i, e) };
            }
        }, {
            key: "fillEllipse",
            value: function fillEllipse(t, e, s, i, h) {
                var n = h.hachureGap < 0 ? 4 * h.strokeWidth : h.hachureGap,
                    a = h.zigzagOffset < 0 ? n : h.zigzagOffset;
                h = Object.assign({}, h, { hachureGap: n + a });
                var o = g(this.helper, t, e, s, i, h);
                return { type: "fillSketch", ops: this.zigzagLines(o, a, h) };
            }
        }, {
            key: "fillArc",
            value: function fillArc(t, e, s, i, h, n, a) {
                return null;
            }
        }, {
            key: "zigzagLines",
            value: function zigzagLines(t, e, s) {
                var _this5 = this;

                var i = [];
                return t.forEach(function (t) {
                    var h = p(t),
                        n = Math.round(h / (2 * e));
                    var a = t[0],
                        o = t[1];
                    a[0] > o[0] && (a = t[1], o = t[0]);
                    var r = Math.atan((o[1] - a[1]) / (o[0] - a[0]));
                    for (var _t23 = 0; _t23 < n; _t23++) {
                        var _h13 = 2 * _t23 * e,
                            _n9 = 2 * (_t23 + 1) * e,
                            _o5 = Math.sqrt(2 * Math.pow(e, 2)),
                            _l4 = [a[0] + _h13 * Math.cos(r), a[1] + _h13 * Math.sin(r)],
                            _c2 = [a[0] + _n9 * Math.cos(r), a[1] + _n9 * Math.sin(r)],
                            _p5 = [_l4[0] + _o5 * Math.cos(r + Math.PI / 4), _l4[1] + _o5 * Math.sin(r + Math.PI / 4)];
                        i = (i = i.concat(_this5.helper.doubleLineOps(_l4[0], _l4[1], _p5[0], _p5[1], s))).concat(_this5.helper.doubleLineOps(_p5[0], _p5[1], _c2[0], _c2[1], s));
                    }
                }), i;
            }
        }]);

        return w;
    }();

    var k = {};

    function P(t, e) {
        var s = t.fillStyle || "hachure";
        if (!k[s]) switch (s) {
            case "zigzag":
                k[s] || (k[s] = new M(e));
                break;
            case "cross-hatch":
                k[s] || (k[s] = new x(e));
                break;
            case "dots":
                k[s] || (k[s] = new _(e));
                break;
            case "starburst":
                k[s] || (k[s] = new b(e));
                break;
            case "dashed":
                k[s] || (k[s] = new m(e));
                break;
            case "zigzag-line":
                k[s] || (k[s] = new w(e));
                break;
            case "hachure":
            default:
                k[s = "hachure"] || (k[s] = new y(e));
        }
        return k[s];
    }

    var v = {
        randOffset: function randOffset(t, e) {
            return W(t, e);
        }, randOffsetWithRange: function randOffsetWithRange(t, e, s) {
            return N(t, e, s);
        }, ellipse: T, doubleLineOps: function doubleLineOps(t, e, s, i, h) {
            return R(t, e, s, i, h);
        }
    };

    function S(t, e, s, i, h) {
        return { type: "path", ops: R(t, e, s, i, h) };
    }

    function A(t, e, s) {
        var i = (t || []).length;
        if (i > 2) {
            var _h14 = [];
            for (var _e13 = 0; _e13 < i - 1; _e13++) {
                _h14 = _h14.concat(R(t[_e13][0], t[_e13][1], t[_e13 + 1][0], t[_e13 + 1][1], s));
            }return e && (_h14 = _h14.concat(R(t[i - 1][0], t[i - 1][1], t[0][0], t[0][1], s))), { type: "path", ops: _h14 };
        }
        return 2 === i ? S(t[0][0], t[0][1], t[1][0], t[1][1], s) : { type: "path", ops: [] };
    }

    function E(t, e, s, i, h) {
        return function (t, e) {
            return A(t, !0, e);
        }([[t, e], [t + s, e], [t + s, e + i], [t, e + i]], h);
    }

    function O(t, e) {
        var s = D(t, 1 * (1 + .2 * e.roughness), e),
            i = D(t, 1.5 * (1 + .22 * e.roughness), e);
        return { type: "path", ops: s.concat(i) };
    }

    function T(t, e, s, i, h) {
        var n = 2 * Math.PI / h.curveStepCount;
        var a = Math.abs(s / 2),
            o = Math.abs(i / 2);
        var r = $(n, t, e, a += W(.05 * a, h), o += W(.05 * o, h), 1, n * N(.1, N(.4, 1, h), h), h),
            l = $(n, t, e, a, o, 1.5, 0, h);
        return { type: "path", ops: r.concat(l) };
    }

    function C(t, e, s, i, h, n, a, o, r) {
        var l = t,
            c = e;
        var p = Math.abs(s / 2),
            u = Math.abs(i / 2);
        p += W(.01 * p, r), u += W(.01 * u, r);
        var f = h,
            d = n;
        for (; f < 0;) {
            f += 2 * Math.PI, d += 2 * Math.PI;
        }d - f > 2 * Math.PI && (f = 0, d = 2 * Math.PI);
        var g = 2 * Math.PI / r.curveStepCount,
            y = Math.min(g / 2, (d - f) / 2),
            M = G(y, l, c, p, u, f, d, 1, r),
            x = G(y, l, c, p, u, f, d, 1.5, r);
        var _ = M.concat(x);
        return a && (o ? _ = (_ = _.concat(R(l, c, l + p * Math.cos(f), c + u * Math.sin(f), r))).concat(R(l, c, l + p * Math.cos(d), c + u * Math.sin(d), r)) : (_.push({
            op: "lineTo",
            data: [l, c]
        }), _.push({ op: "lineTo", data: [l + p * Math.cos(f), c + u * Math.sin(f)] }))), { type: "path", ops: _ };
    }

    function z(t, e) {
        var s = [];
        if (t.length) {
            var _i16 = e.maxRandomnessOffset || 0,
                _h15 = t.length;
            if (_h15 > 2) {
                s.push({ op: "move", data: [t[0][0] + W(_i16, e), t[0][1] + W(_i16, e)] });
                for (var n = 1; n < _h15; n++) {
                    s.push({ op: "lineTo", data: [t[n][0] + W(_i16, e), t[n][1] + W(_i16, e)] });
                }
            }
        }
        return { type: "fillPath", ops: s };
    }

    function L(t, e) {
        return P(e, v).fillPolygon(t, e);
    }

    function N(t, e, s) {
        return s.roughness * (Math.random() * (e - t) + t);
    }

    function W(t, e) {
        return N(-t, t, e);
    }

    function R(t, e, s, i, h) {
        var n = I(t, e, s, i, h, !0, !1),
            a = I(t, e, s, i, h, !0, !0);
        return n.concat(a);
    }

    function I(t, e, s, i, h, n, a) {
        var o = Math.pow(t - s, 2) + Math.pow(e - i, 2);
        var r = h.maxRandomnessOffset || 0;
        r * r * 100 > o && (r = Math.sqrt(o) / 10);
        var l = r / 2,
            c = .2 + .2 * Math.random();
        var p = h.bowing * h.maxRandomnessOffset * (i - e) / 200,
            u = h.bowing * h.maxRandomnessOffset * (t - s) / 200;
        p = W(p, h), u = W(u, h);
        var f = [],
            d = function d() {
            return W(l, h);
        },
            g = function g() {
            return W(r, h);
        };
        return n && (a ? f.push({ op: "move", data: [t + d(), e + d()] }) : f.push({ op: "move", data: [t + W(r, h), e + W(r, h)] })), a ? f.push({
            op: "bcurveTo",
            data: [p + t + (s - t) * c + d(), u + e + (i - e) * c + d(), p + t + 2 * (s - t) * c + d(), u + e + 2 * (i - e) * c + d(), s + d(), i + d()]
        }) : f.push({ op: "bcurveTo", data: [p + t + (s - t) * c + g(), u + e + (i - e) * c + g(), p + t + 2 * (s - t) * c + g(), u + e + 2 * (i - e) * c + g(), s + g(), i + g()] }), f;
    }

    function D(t, e, s) {
        var i = [];
        i.push([t[0][0] + W(e, s), t[0][1] + W(e, s)]), i.push([t[0][0] + W(e, s), t[0][1] + W(e, s)]);
        for (var _h16 = 1; _h16 < t.length; _h16++) {
            i.push([t[_h16][0] + W(e, s), t[_h16][1] + W(e, s)]), _h16 === t.length - 1 && i.push([t[_h16][0] + W(e, s), t[_h16][1] + W(e, s)]);
        }return q(i, null, s);
    }

    function q(t, e, s) {
        var i = t.length;
        var h = [];
        if (i > 3) {
            var n = [],
                _a8 = 1 - s.curveTightness;
            h.push({ op: "move", data: [t[1][0], t[1][1]] });
            for (var _e14 = 1; _e14 + 2 < i; _e14++) {
                var _s10 = t[_e14];
                n[0] = [_s10[0], _s10[1]], n[1] = [_s10[0] + (_a8 * t[_e14 + 1][0] - _a8 * t[_e14 - 1][0]) / 6, _s10[1] + (_a8 * t[_e14 + 1][1] - _a8 * t[_e14 - 1][1]) / 6], n[2] = [t[_e14 + 1][0] + (_a8 * t[_e14][0] - _a8 * t[_e14 + 2][0]) / 6, t[_e14 + 1][1] + (_a8 * t[_e14][1] - _a8 * t[_e14 + 2][1]) / 6], n[3] = [t[_e14 + 1][0], t[_e14 + 1][1]], h.push({
                    op: "bcurveTo",
                    data: [n[1][0], n[1][1], n[2][0], n[2][1], n[3][0], n[3][1]]
                });
            }
            if (e && 2 === e.length) {
                var _t24 = s.maxRandomnessOffset;
                h.push({ op: "lineTo", data: [e[0] + W(_t24, s), e[1] + W(_t24, s)] });
            }
        } else 3 === i ? (h.push({ op: "move", data: [t[1][0], t[1][1]] }), h.push({
            op: "bcurveTo",
            data: [t[1][0], t[1][1], t[2][0], t[2][1], t[2][0], t[2][1]]
        })) : 2 === i && (h = h.concat(R(t[0][0], t[0][1], t[1][0], t[1][1], s)));
        return h;
    }

    function $(t, e, s, i, h, n, a, o) {
        var r = W(.5, o) - Math.PI / 2,
            l = [];
        l.push([W(n, o) + e + .9 * i * Math.cos(r - t), W(n, o) + s + .9 * h * Math.sin(r - t)]);
        for (var _a9 = r; _a9 < 2 * Math.PI + r - .01; _a9 += t) {
            l.push([W(n, o) + e + i * Math.cos(_a9), W(n, o) + s + h * Math.sin(_a9)]);
        }return l.push([W(n, o) + e + i * Math.cos(r + 2 * Math.PI + .5 * a), W(n, o) + s + h * Math.sin(r + 2 * Math.PI + .5 * a)]), l.push([W(n, o) + e + .98 * i * Math.cos(r + a), W(n, o) + s + .98 * h * Math.sin(r + a)]), l.push([W(n, o) + e + .9 * i * Math.cos(r + .5 * a), W(n, o) + s + .9 * h * Math.sin(r + .5 * a)]), q(l, null, o);
    }

    function G(t, e, s, i, h, n, a, o, r) {
        var l = n + W(.1, r),
            c = [];
        c.push([W(o, r) + e + .9 * i * Math.cos(l - t), W(o, r) + s + .9 * h * Math.sin(l - t)]);
        for (var _n10 = l; _n10 <= a; _n10 += t) {
            c.push([W(o, r) + e + i * Math.cos(_n10), W(o, r) + s + h * Math.sin(_n10)]);
        }return c.push([e + i * Math.cos(a), s + h * Math.sin(a)]), c.push([e + i * Math.cos(a), s + h * Math.sin(a)]), q(c, null, r);
    }

    function B(t, e, s, i, h, n, a, o) {
        var r = [],
            l = [o.maxRandomnessOffset || 1, (o.maxRandomnessOffset || 1) + .5];
        var c = [0, 0];
        for (var _p6 = 0; _p6 < 2; _p6++) {
            0 === _p6 ? r.push({ op: "move", data: [a.x, a.y] }) : r.push({
                op: "move",
                data: [a.x + W(l[0], o), a.y + W(l[0], o)]
            }), c = [h + W(l[_p6], o), n + W(l[_p6], o)], r.push({ op: "bcurveTo", data: [t + W(l[_p6], o), e + W(l[_p6], o), s + W(l[_p6], o), i + W(l[_p6], o), c[0], c[1]] });
        }return a.setPosition(c[0], c[1]), r;
    }

    function X(t, e, s, i) {
        var h = [];
        switch (e.key) {
            case "M":
            case "m":
                {
                    var _s11 = "m" === e.key;
                    if (e.data.length >= 2) {
                        var n = +e.data[0],
                            _a10 = +e.data[1];
                        _s11 && (n += t.x, _a10 += t.y);
                        var _o6 = 1 * (i.maxRandomnessOffset || 0);
                        n += W(_o6, i), _a10 += W(_o6, i), t.setPosition(n, _a10), h.push({ op: "move", data: [n, _a10] });
                    }
                    break;
                }
            case "L":
            case "l":
                {
                    var _s12 = "l" === e.key;
                    if (e.data.length >= 2) {
                        var _n11 = +e.data[0],
                            _a11 = +e.data[1];
                        _s12 && (_n11 += t.x, _a11 += t.y), h = h.concat(R(t.x, t.y, _n11, _a11, i)), t.setPosition(_n11, _a11);
                    }
                    break;
                }
            case "H":
            case "h":
                {
                    var _s13 = "h" === e.key;
                    if (e.data.length) {
                        var _n12 = +e.data[0];
                        _s13 && (_n12 += t.x), h = h.concat(R(t.x, t.y, _n12, t.y, i)), t.setPosition(_n12, t.y);
                    }
                    break;
                }
            case "V":
            case "v":
                {
                    var _s14 = "v" === e.key;
                    if (e.data.length) {
                        var _n13 = +e.data[0];
                        _s14 && (_n13 += t.y), h = h.concat(R(t.x, t.y, t.x, _n13, i)), t.setPosition(t.x, _n13);
                    }
                    break;
                }
            case "Z":
            case "z":
                t.first && (h = h.concat(R(t.x, t.y, t.first[0], t.first[1], i)), t.setPosition(t.first[0], t.first[1]), t.first = null);
                break;
            case "C":
            case "c":
                {
                    var _s15 = "c" === e.key;
                    if (e.data.length >= 6) {
                        var _n14 = +e.data[0],
                            _a12 = +e.data[1],
                            _o7 = +e.data[2],
                            _r5 = +e.data[3],
                            _l5 = +e.data[4],
                            _c3 = +e.data[5];
                        _s15 && (_n14 += t.x, _o7 += t.x, _l5 += t.x, _a12 += t.y, _r5 += t.y, _c3 += t.y);
                        var _p7 = B(_n14, _a12, _o7, _r5, _l5, _c3, t, i);
                        h = h.concat(_p7), t.bezierReflectionPoint = [_l5 + (_l5 - _o7), _c3 + (_c3 - _r5)];
                    }
                    break;
                }
            case "S":
            case "s":
                {
                    var _n15 = "s" === e.key;
                    if (e.data.length >= 4) {
                        var _a13 = +e.data[0],
                            _o8 = +e.data[1],
                            _r6 = +e.data[2],
                            _l6 = +e.data[3];
                        _n15 && (_a13 += t.x, _r6 += t.x, _o8 += t.y, _l6 += t.y);
                        var _c4 = _a13,
                            _p8 = _o8;
                        var _u2 = s ? s.key : "";
                        var _f3 = null;
                        "c" !== _u2 && "C" !== _u2 && "s" !== _u2 && "S" !== _u2 || (_f3 = t.bezierReflectionPoint), _f3 && (_c4 = _f3[0], _p8 = _f3[1]);
                        var _d3 = B(_c4, _p8, _a13, _o8, _r6, _l6, t, i);
                        h = h.concat(_d3), t.bezierReflectionPoint = [_r6 + (_r6 - _a13), _l6 + (_l6 - _o8)];
                    }
                    break;
                }
            case "Q":
            case "q":
                {
                    var _s16 = "q" === e.key;
                    if (e.data.length >= 4) {
                        var _n16 = +e.data[0],
                            _a14 = +e.data[1],
                            _o9 = +e.data[2],
                            _r7 = +e.data[3];
                        _s16 && (_n16 += t.x, _o9 += t.x, _a14 += t.y, _r7 += t.y);
                        var _l7 = 1 * (1 + .2 * i.roughness),
                            _c5 = 1.5 * (1 + .22 * i.roughness);
                        h.push({ op: "move", data: [t.x + W(_l7, i), t.y + W(_l7, i)] });
                        var _p9 = [_o9 + W(_l7, i), _r7 + W(_l7, i)];
                        h.push({ op: "qcurveTo", data: [_n16 + W(_l7, i), _a14 + W(_l7, i), _p9[0], _p9[1]] }), h.push({
                            op: "move",
                            data: [t.x + W(_c5, i), t.y + W(_c5, i)]
                        }), _p9 = [_o9 + W(_c5, i), _r7 + W(_c5, i)], h.push({
                            op: "qcurveTo",
                            data: [_n16 + W(_c5, i), _a14 + W(_c5, i), _p9[0], _p9[1]]
                        }), t.setPosition(_p9[0], _p9[1]), t.quadReflectionPoint = [_o9 + (_o9 - _n16), _r7 + (_r7 - _a14)];
                    }
                    break;
                }
            case "T":
            case "t":
                {
                    var _n17 = "t" === e.key;
                    if (e.data.length >= 2) {
                        var _a15 = +e.data[0],
                            _o10 = +e.data[1];
                        _n17 && (_a15 += t.x, _o10 += t.y);
                        var _r8 = _a15,
                            _l8 = _o10;
                        var _c6 = s ? s.key : "";
                        var _p10 = null;
                        "q" !== _c6 && "Q" !== _c6 && "t" !== _c6 && "T" !== _c6 || (_p10 = t.quadReflectionPoint), _p10 && (_r8 = _p10[0], _l8 = _p10[1]);
                        var _u3 = 1 * (1 + .2 * i.roughness),
                            _f4 = 1.5 * (1 + .22 * i.roughness);
                        h.push({ op: "move", data: [t.x + W(_u3, i), t.y + W(_u3, i)] });
                        var _d4 = [_a15 + W(_u3, i), _o10 + W(_u3, i)];
                        h.push({ op: "qcurveTo", data: [_r8 + W(_u3, i), _l8 + W(_u3, i), _d4[0], _d4[1]] }), h.push({
                            op: "move",
                            data: [t.x + W(_f4, i), t.y + W(_f4, i)]
                        }), _d4 = [_a15 + W(_f4, i), _o10 + W(_f4, i)], h.push({
                            op: "qcurveTo",
                            data: [_r8 + W(_f4, i), _l8 + W(_f4, i), _d4[0], _d4[1]]
                        }), t.setPosition(_d4[0], _d4[1]), t.quadReflectionPoint = [_a15 + (_a15 - _r8), _o10 + (_o10 - _l8)];
                    }
                    break;
                }
            case "A":
            case "a":
                {
                    var _s17 = "a" === e.key;
                    if (e.data.length >= 7) {
                        var _n18 = +e.data[0],
                            _o11 = +e.data[1],
                            _r9 = +e.data[2],
                            _l9 = +e.data[3],
                            _c7 = +e.data[4];
                        var _p11 = +e.data[5],
                            _u4 = +e.data[6];
                        if (_s17 && (_p11 += t.x, _u4 += t.y), _p11 === t.x && _u4 === t.y) break;
                        if (0 === _n18 || 0 === _o11) h = h.concat(R(t.x, t.y, _p11, _u4, i)), t.setPosition(_p11, _u4);else for (var _e15 = 0; _e15 < 1; _e15++) {
                            var _e16 = new a([t.x, t.y], [_p11, _u4], [_n18, _o11], _r9, !!_l9, !!_c7);
                            var _s18 = _e16.getNextSegment();
                            for (; _s18;) {
                                var _n19 = B(_s18.cp1[0], _s18.cp1[1], _s18.cp2[0], _s18.cp2[1], _s18.to[0], _s18.to[1], t, i);
                                h = h.concat(_n19), _s18 = _e16.getNextSegment();
                            }
                        }
                    }
                    break;
                }
        }
        return h;
    }

    var U = function (_e17) {
        _inherits(U, _e17);

        function U() {
            _classCallCheck(this, U);

            return _possibleConstructorReturn(this, (U.__proto__ || Object.getPrototypeOf(U)).apply(this, arguments));
        }

        _createClass(U, [{
            key: "line",
            value: function line(t, e, s, i, h) {
                var n = this._options(h);
                return this._drawable("line", [S(t, e, s, i, n)], n);
            }
        }, {
            key: "rectangle",
            value: function rectangle(t, e, s, i, h) {
                var n = this._options(h),
                    a = [];
                if (n.fill) {
                    var _h17 = [[t, e], [t + s, e], [t + s, e + i], [t, e + i]];
                    "solid" === n.fillStyle ? a.push(z(_h17, n)) : a.push(L(_h17, n));
                }
                return a.push(E(t, e, s, i, n)), this._drawable("rectangle", a, n);
            }
        }, {
            key: "ellipse",
            value: function ellipse(t, e, s, i, h) {
                var n = this._options(h),
                    a = [];
                if (n.fill) if ("solid" === n.fillStyle) {
                    var _h18 = T(t, e, s, i, n);
                    _h18.type = "fillPath", a.push(_h18);
                } else a.push(function (t, e, s, i, h) {
                    return P(h, v).fillEllipse(t, e, s, i, h);
                }(t, e, s, i, n));
                return a.push(T(t, e, s, i, n)), this._drawable("ellipse", a, n);
            }
        }, {
            key: "circle",
            value: function circle(t, e, s, i) {
                var h = this.ellipse(t, e, s, s, i);
                return h.shape = "circle", h;
            }
        }, {
            key: "linearPath",
            value: function linearPath(t, e) {
                var s = this._options(e);
                return this._drawable("linearPath", [A(t, !1, s)], s);
            }
        }, {
            key: "arc",
            value: function arc(t, e, s, i, h, n) {
                var a = arguments.length > 6 && arguments[6] !== undefined ? arguments[6] : !1;
                var o = arguments[7];

                var r = this._options(o),
                    l = [];
                if (a && r.fill) if ("solid" === r.fillStyle) {
                    var _a16 = C(t, e, s, i, h, n, !0, !1, r);
                    _a16.type = "fillPath", l.push(_a16);
                } else l.push(function (t, e, s, i, h, n, a) {
                    var o = P(a, v).fillArc(t, e, s, i, h, n, a);
                    if (o) return o;
                    var r = t,
                        l = e;
                    var c = Math.abs(s / 2),
                        p = Math.abs(i / 2);
                    c += W(.01 * c, a), p += W(.01 * p, a);
                    var u = h,
                        f = n;
                    for (; u < 0;) {
                        u += 2 * Math.PI, f += 2 * Math.PI;
                    }f - u > 2 * Math.PI && (u = 0, f = 2 * Math.PI);
                    var d = (f - u) / a.curveStepCount,
                        g = [];
                    for (var _t25 = u; _t25 <= f; _t25 += d) {
                        g.push([r + c * Math.cos(_t25), l + p * Math.sin(_t25)]);
                    }return g.push([r + c * Math.cos(f), l + p * Math.sin(f)]), g.push([r, l]), L(g, a);
                }(t, e, s, i, h, n, r));
                return l.push(C(t, e, s, i, h, n, a, !0, r)), this._drawable("arc", l, r);
            }
        }, {
            key: "curve",
            value: function curve(t, e) {
                var s = this._options(e);
                return this._drawable("curve", [O(t, s)], s);
            }
        }, {
            key: "polygon",
            value: function polygon(t, e) {
                var s = this._options(e),
                    i = [];
                if (s.fill) if ("solid" === s.fillStyle) i.push(z(t, s));else {
                    var _e18 = this.computePolygonSize(t),
                        _h19 = L([[0, 0], [_e18[0], 0], [_e18[0], _e18[1]], [0, _e18[1]]], s);
                    _h19.type = "path2Dpattern", _h19.size = _e18, _h19.path = this.polygonPath(t), i.push(_h19);
                }
                return i.push(A(t, !0, s)), this._drawable("polygon", i, s);
            }
        }, {
            key: "path",
            value: function path(t, e) {
                var s = this._options(e),
                    i = [];
                if (!t) return this._drawable("path", i, s);
                if (s.fill) if ("solid" === s.fillStyle) {
                    var _e19 = { type: "path2Dfill", path: t, ops: [] };
                    i.push(_e19);
                } else {
                    var _e20 = this.computePathSize(t),
                        _h20 = L([[0, 0], [_e20[0], 0], [_e20[0], _e20[1]], [0, _e20[1]]], s);
                    _h20.type = "path2Dpattern", _h20.size = _e20, _h20.path = t, i.push(_h20);
                }
                return i.push(function (t, e) {
                    t = (t || "").replace(/\n/g, " ").replace(/(-\s)/g, "-").replace("/(ss)/g", " ");
                    var s = new n(t);
                    if (e.simplification) {
                        var _t26 = new o(s.linearPoints, s.closed).fit(e.simplification);
                        s = new n(_t26);
                    }
                    var i = [];
                    var h = s.segments || [];
                    for (var _t27 = 0; _t27 < h.length; _t27++) {
                        var _n20 = X(s, h[_t27], _t27 > 0 ? h[_t27 - 1] : null, e);
                        _n20 && _n20.length && (i = i.concat(_n20));
                    }
                    return { type: "path", ops: i };
                }(t, s)), this._drawable("path", i, s);
            }
        }]);

        return U;
    }(e);

    var V = "undefined" != typeof document;

    var j = function () {
        function j(t) {
            _classCallCheck(this, j);

            this.canvas = t, this.ctx = this.canvas.getContext("2d");
        }

        _createClass(j, [{
            key: "draw",
            value: function draw(t) {
                var e = t.sets || [],
                    s = t.options || this.getDefaultOptions(),
                    i = this.ctx;
                var _iteratorNormalCompletion9 = true;
                var _didIteratorError9 = false;
                var _iteratorError9 = undefined;

                try {
                    for (var _iterator9 = e[Symbol.iterator](), _step9; !(_iteratorNormalCompletion9 = (_step9 = _iterator9.next()).done); _iteratorNormalCompletion9 = true) {
                        var _t28 = _step9.value;
                        switch (_t28.type) {
                            case "path":
                                i.save(), i.strokeStyle = s.stroke, i.lineWidth = s.strokeWidth, this._drawToContext(i, _t28), i.restore();
                                break;
                            case "fillPath":
                                i.save(), i.fillStyle = s.fill || "", this._drawToContext(i, _t28), i.restore();
                                break;
                            case "fillSketch":
                                this.fillSketch(i, _t28, s);
                                break;
                            case "path2Dfill":
                                {
                                    this.ctx.save(), this.ctx.fillStyle = s.fill || "";
                                    var _e21 = new Path2D(_t28.path);
                                    this.ctx.fill(_e21), this.ctx.restore();
                                    break;
                                }
                            case "path2Dpattern":
                                {
                                    var _e22 = this.canvas.ownerDocument || V && document;
                                    if (_e22) {
                                        var _i17 = _t28.size,
                                            _h21 = _e22.createElement("canvas"),
                                            _n21 = _h21.getContext("2d"),
                                            a = this.computeBBox(_t28.path);
                                        a && (a.width || a.height) ? (_h21.width = this.canvas.width, _h21.height = this.canvas.height, _n21.translate(a.x || 0, a.y || 0)) : (_h21.width = _i17[0], _h21.height = _i17[1]), this.fillSketch(_n21, _t28, s), this.ctx.save(), this.ctx.fillStyle = this.ctx.createPattern(_h21, "repeat");
                                        var o = new Path2D(_t28.path);
                                        this.ctx.fill(o), this.ctx.restore();
                                    } else console.error("Cannot render path2Dpattern. No defs/document defined.");
                                    break;
                                }
                        }
                    }
                } catch (err) {
                    _didIteratorError9 = true;
                    _iteratorError9 = err;
                } finally {
                    try {
                        if (!_iteratorNormalCompletion9 && _iterator9.return) {
                            _iterator9.return();
                        }
                    } finally {
                        if (_didIteratorError9) {
                            throw _iteratorError9;
                        }
                    }
                }
            }
        }, {
            key: "computeBBox",
            value: function computeBBox(t) {
                if (V) try {
                    var _e23 = "http://www.w3.org/2000/svg",
                        s = document.createElementNS(_e23, "svg");
                    s.setAttribute("width", "0"), s.setAttribute("height", "0");
                    var _i18 = self.document.createElementNS(_e23, "path");
                    _i18.setAttribute("d", t), s.appendChild(_i18), document.body.appendChild(s);
                    var _h22 = _i18.getBBox();
                    return document.body.removeChild(s), _h22;
                } catch (t) {}
                return null;
            }
        }, {
            key: "fillSketch",
            value: function fillSketch(t, e, s) {
                var i = s.fillWeight;
                i < 0 && (i = s.strokeWidth / 2), t.save(), t.strokeStyle = s.fill || "", t.lineWidth = i, this._drawToContext(t, e), t.restore();
            }
        }, {
            key: "_drawToContext",
            value: function _drawToContext(t, e) {
                t.beginPath();
                var _iteratorNormalCompletion10 = true;
                var _didIteratorError10 = false;
                var _iteratorError10 = undefined;

                try {
                    for (var _iterator10 = e.ops[Symbol.iterator](), _step10; !(_iteratorNormalCompletion10 = (_step10 = _iterator10.next()).done); _iteratorNormalCompletion10 = true) {
                        var s = _step10.value;

                        var _e24 = s.data;
                        switch (s.op) {
                            case "move":
                                t.moveTo(_e24[0], _e24[1]);
                                break;
                            case "bcurveTo":
                                t.bezierCurveTo(_e24[0], _e24[1], _e24[2], _e24[3], _e24[4], _e24[5]);
                                break;
                            case "qcurveTo":
                                t.quadraticCurveTo(_e24[0], _e24[1], _e24[2], _e24[3]);
                                break;
                            case "lineTo":
                                t.lineTo(_e24[0], _e24[1]);
                        }
                    }
                } catch (err) {
                    _didIteratorError10 = true;
                    _iteratorError10 = err;
                } finally {
                    try {
                        if (!_iteratorNormalCompletion10 && _iterator10.return) {
                            _iterator10.return();
                        }
                    } finally {
                        if (_didIteratorError10) {
                            throw _iteratorError10;
                        }
                    }
                }

                "fillPath" === e.type ? t.fill() : t.stroke();
            }
        }]);

        return j;
    }();

    var F = function (_j) {
        _inherits(F, _j);

        function F(t, e) {
            var _this7;

            _classCallCheck(this, F);

            (_this7 = _possibleConstructorReturn(this, (F.__proto__ || Object.getPrototypeOf(F)).call(this, t)), _this7), _this7.gen = new U(e || null, _this7.canvas);
            return _this7;
        }

        _createClass(F, [{
            key: "getDefaultOptions",
            value: function getDefaultOptions() {
                return this.gen.defaultOptions;
            }
        }, {
            key: "line",
            value: function line(t, e, s, i, h) {
                var n = this.gen.line(t, e, s, i, h);
                return this.draw(n), n;
            }
        }, {
            key: "rectangle",
            value: function rectangle(t, e, s, i, h) {
                var n = this.gen.rectangle(t, e, s, i, h);
                return this.draw(n), n;
            }
        }, {
            key: "ellipse",
            value: function ellipse(t, e, s, i, h) {
                var n = this.gen.ellipse(t, e, s, i, h);
                return this.draw(n), n;
            }
        }, {
            key: "circle",
            value: function circle(t, e, s, i) {
                var h = this.gen.circle(t, e, s, i);
                return this.draw(h), h;
            }
        }, {
            key: "linearPath",
            value: function linearPath(t, e) {
                var s = this.gen.linearPath(t, e);
                return this.draw(s), s;
            }
        }, {
            key: "polygon",
            value: function polygon(t, e) {
                var s = this.gen.polygon(t, e);
                return this.draw(s), s;
            }
        }, {
            key: "arc",
            value: function arc(t, e, s, i, h, n) {
                var a = arguments.length > 6 && arguments[6] !== undefined ? arguments[6] : !1;
                var o = arguments[7];

                var r = this.gen.arc(t, e, s, i, h, n, a, o);
                return this.draw(r), r;
            }
        }, {
            key: "curve",
            value: function curve(t, e) {
                var s = this.gen.curve(t, e);
                return this.draw(s), s;
            }
        }, {
            key: "path",
            value: function path(t, e) {
                var s = this.gen.path(t, e);
                return this.draw(s), s;
            }
        }, {
            key: "generator",
            get: function get() {
                return this.gen;
            }
        }]);

        return F;
    }(j);

    var Q = "undefined" != typeof document;

    var Z = function () {
        function Z(t) {
            _classCallCheck(this, Z);

            this.svg = t;
        }

        _createClass(Z, [{
            key: "draw",
            value: function draw(t) {
                var e = t.sets || [],
                    s = t.options || this.getDefaultOptions(),
                    i = this.svg.ownerDocument || window.document,
                    h = i.createElementNS("http://www.w3.org/2000/svg", "g");
                var _iteratorNormalCompletion11 = true;
                var _didIteratorError11 = false;
                var _iteratorError11 = undefined;

                try {
                    for (var _iterator11 = e[Symbol.iterator](), _step11; !(_iteratorNormalCompletion11 = (_step11 = _iterator11.next()).done); _iteratorNormalCompletion11 = true) {
                        var _t29 = _step11.value;

                        var _e25 = null;
                        switch (_t29.type) {
                            case "path":
                                (_e25 = i.createElementNS("http://www.w3.org/2000/svg", "path")).setAttribute("d", this.opsToPath(_t29)), _e25.style.stroke = s.stroke, _e25.style.strokeWidth = s.strokeWidth + "", _e25.style.fill = "none";
                                break;
                            case "fillPath":
                                (_e25 = i.createElementNS("http://www.w3.org/2000/svg", "path")).setAttribute("d", this.opsToPath(_t29)), _e25.style.stroke = "none", _e25.style.strokeWidth = "0", _e25.style.fill = s.fill || null;
                                break;
                            case "fillSketch":
                                _e25 = this.fillSketch(i, _t29, s);
                                break;
                            case "path2Dfill":
                                (_e25 = i.createElementNS("http://www.w3.org/2000/svg", "path")).setAttribute("d", _t29.path || ""), _e25.style.stroke = "none", _e25.style.strokeWidth = "0", _e25.style.fill = s.fill || null;
                                break;
                            case "path2Dpattern":
                                if (this.defs) {
                                    var _h23 = _t29.size,
                                        _n22 = i.createElementNS("http://www.w3.org/2000/svg", "pattern"),
                                        a = "rough-" + Math.floor(Math.random() * (Number.MAX_SAFE_INTEGER || 999999));
                                    _n22.setAttribute("id", a), _n22.setAttribute("x", "0"), _n22.setAttribute("y", "0"), _n22.setAttribute("width", "1"), _n22.setAttribute("height", "1"), _n22.setAttribute("height", "1"), _n22.setAttribute("viewBox", "0 0 " + Math.round(_h23[0]) + " " + Math.round(_h23[1])), _n22.setAttribute("patternUnits", "objectBoundingBox");
                                    var o = this.fillSketch(i, _t29, s);
                                    _n22.appendChild(o), this.defs.appendChild(_n22), (_e25 = i.createElementNS("http://www.w3.org/2000/svg", "path")).setAttribute("d", _t29.path || ""), _e25.style.stroke = "none", _e25.style.strokeWidth = "0", _e25.style.fill = "url(#" + a + ")";
                                } else console.error("Cannot render path2Dpattern. No defs/document defined.");
                        }
                        _e25 && h.appendChild(_e25);
                    }
                } catch (err) {
                    _didIteratorError11 = true;
                    _iteratorError11 = err;
                } finally {
                    try {
                        if (!_iteratorNormalCompletion11 && _iterator11.return) {
                            _iterator11.return();
                        }
                    } finally {
                        if (_didIteratorError11) {
                            throw _iteratorError11;
                        }
                    }
                }

                return h;
            }
        }, {
            key: "fillSketch",
            value: function fillSketch(t, e, s) {
                var i = s.fillWeight;
                i < 0 && (i = s.strokeWidth / 2);
                var h = t.createElementNS("http://www.w3.org/2000/svg", "path");
                return h.setAttribute("d", this.opsToPath(e)), h.style.stroke = s.fill || null, h.style.strokeWidth = i + "", h.style.fill = "none", h;
            }
        }, {
            key: "defs",
            get: function get() {
                var t = this.svg.ownerDocument || Q && document;
                if (t && !this._defs) {
                    var _e26 = t.createElementNS("http://www.w3.org/2000/svg", "defs");
                    this.svg.firstChild ? this.svg.insertBefore(_e26, this.svg.firstChild) : this.svg.appendChild(_e26), this._defs = _e26;
                }
                return this._defs || null;
            }
        }]);

        return Z;
    }();

    var H = function (_Z) {
        _inherits(H, _Z);

        function H(t, e) {
            var _this8;

            _classCallCheck(this, H);

            (_this8 = _possibleConstructorReturn(this, (H.__proto__ || Object.getPrototypeOf(H)).call(this, t)), _this8), _this8.gen = new U(e || null, _this8.svg);
            return _this8;
        }

        _createClass(H, [{
            key: "getDefaultOptions",
            value: function getDefaultOptions() {
                return this.gen.defaultOptions;
            }
        }, {
            key: "opsToPath",
            value: function opsToPath(t) {
                return this.gen.opsToPath(t);
            }
        }, {
            key: "line",
            value: function line(t, e, s, i, h) {
                var n = this.gen.line(t, e, s, i, h);
                return this.draw(n);
            }
        }, {
            key: "rectangle",
            value: function rectangle(t, e, s, i, h) {
                var n = this.gen.rectangle(t, e, s, i, h);
                return this.draw(n);
            }
        }, {
            key: "ellipse",
            value: function ellipse(t, e, s, i, h) {
                var n = this.gen.ellipse(t, e, s, i, h);
                return this.draw(n);
            }
        }, {
            key: "circle",
            value: function circle(t, e, s, i) {
                var h = this.gen.circle(t, e, s, i);
                return this.draw(h);
            }
        }, {
            key: "linearPath",
            value: function linearPath(t, e) {
                var s = this.gen.linearPath(t, e);
                return this.draw(s);
            }
        }, {
            key: "polygon",
            value: function polygon(t, e) {
                var s = this.gen.polygon(t, e);
                return this.draw(s);
            }
        }, {
            key: "arc",
            value: function arc(t, e, s, i, h, n) {
                var a = arguments.length > 6 && arguments[6] !== undefined ? arguments[6] : !1;
                var o = arguments[7];

                var r = this.gen.arc(t, e, s, i, h, n, a, o);
                return this.draw(r);
            }
        }, {
            key: "curve",
            value: function curve(t, e) {
                var s = this.gen.curve(t, e);
                return this.draw(s);
            }
        }, {
            key: "path",
            value: function path(t, e) {
                var s = this.gen.path(t, e);
                return this.draw(s);
            }
        }, {
            key: "generator",
            get: function get() {
                return this.gen;
            }
        }]);

        return H;
    }(Z);

    return { canvas: function canvas(t, e) {
            return new F(t, e);
        }, svg: function svg(t, e) {
            return new H(t, e);
        }, generator: function generator(t, e) {
            return new U(t, e);
        } };
}();
