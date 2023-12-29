"use strict";

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _slicedToArray = function () { function sliceIterator(arr, i) { var _arr = []; var _n = true; var _d = false; var _e = undefined; try { for (var _i = arr[Symbol.iterator](), _s; !(_n = (_s = _i.next()).done); _n = true) { _arr.push(_s.value); if (i && _arr.length === i) break; } } catch (err) { _d = true; _e = err; } finally { try { if (!_n && _i["return"]) _i["return"](); } finally { if (_d) throw _e; } } return _arr; } return function (arr, i) { if (Array.isArray(arr)) { return arr; } else if (Symbol.iterator in Object(arr)) { return sliceIterator(arr, i); } else { throw new TypeError("Invalid attempt to destructure non-iterable instance"); } }; }();

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) { arr2[i] = arr[i]; } return arr2; } else { return Array.from(arr); } }

var rough = function () {
  "use strict";
  function t(t, e, s) {
    if (t && t.length) {
      var _e = _slicedToArray(e, 2),
          _n = _e[0],
          _o = _e[1],
          _a = Math.PI / 180 * s,
          _h = Math.cos(_a),
          _r = Math.sin(_a);

      var _iteratorNormalCompletion = true;
      var _didIteratorError = false;
      var _iteratorError = undefined;

      try {
        for (var _iterator = t[Symbol.iterator](), _step; !(_iteratorNormalCompletion = (_step = _iterator.next()).done); _iteratorNormalCompletion = true) {
          var _e2 = _step.value;

          var _e3 = _slicedToArray(_e2, 2),
              _t = _e3[0],
              _s = _e3[1];

          _e2[0] = (_t - _n) * _h - (_s - _o) * _r + _n, _e2[1] = (_t - _n) * _r + (_s - _o) * _h + _o;
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
    }
  }function e(t, e) {
    return t[0] === e[0] && t[1] === e[1];
  }function s(s, n, o) {
    var a = arguments.length > 3 && arguments[3] !== undefined ? arguments[3] : 1;
    var h = o,
        r = Math.max(n, .1),
        i = s[0] && s[0][0] && "number" == typeof s[0][0] ? [s] : s,
        c = [0, 0];if (h) {
      var _iteratorNormalCompletion2 = true;
      var _didIteratorError2 = false;
      var _iteratorError2 = undefined;

      try {
        for (var _iterator2 = i[Symbol.iterator](), _step2; !(_iteratorNormalCompletion2 = (_step2 = _iterator2.next()).done); _iteratorNormalCompletion2 = true) {
          var _e4 = _step2.value;
          t(_e4, c, h);
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
    }var l = function (t, s, n) {
      var o = [];var _iteratorNormalCompletion3 = true;
      var _didIteratorError3 = false;
      var _iteratorError3 = undefined;

      try {
        for (var _iterator3 = t[Symbol.iterator](), _step3; !(_iteratorNormalCompletion3 = (_step3 = _iterator3.next()).done); _iteratorNormalCompletion3 = true) {
          var _s3 = _step3.value;
          var _t4 = [].concat(_toConsumableArray(_s3));e(_t4[0], _t4[_t4.length - 1]) || _t4.push([_t4[0][0], _t4[0][1]]), _t4.length > 2 && o.push(_t4);
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

      var a = [];s = Math.max(s, .1);var h = [];var _iteratorNormalCompletion4 = true;
      var _didIteratorError4 = false;
      var _iteratorError4 = undefined;

      try {
        for (var _iterator4 = o[Symbol.iterator](), _step4; !(_iteratorNormalCompletion4 = (_step4 = _iterator4.next()).done); _iteratorNormalCompletion4 = true) {
          var _t5 = _step4.value;
          for (var _e7 = 0; _e7 < _t5.length - 1; _e7++) {
            var _s4 = _t5[_e7],
                _n3 = _t5[_e7 + 1];if (_s4[1] !== _n3[1]) {
              var _t6 = Math.min(_s4[1], _n3[1]);h.push({ ymin: _t6, ymax: Math.max(_s4[1], _n3[1]), x: _t6 === _s4[1] ? _s4[0] : _n3[0], islope: (_n3[0] - _s4[0]) / (_n3[1] - _s4[1]) });
            }
          }
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

      if (h.sort(function (t, e) {
        return t.ymin < e.ymin ? -1 : t.ymin > e.ymin ? 1 : t.x < e.x ? -1 : t.x > e.x ? 1 : t.ymax === e.ymax ? 0 : (t.ymax - e.ymax) / Math.abs(t.ymax - e.ymax);
      }), !h.length) return a;var r = [],
          i = h[0].ymin,
          c = 0;for (; r.length || h.length;) {
        if (h.length) {
          var _t2 = -1;for (var _e5 = 0; _e5 < h.length && !(h[_e5].ymin > i); _e5++) {
            _t2 = _e5;
          }h.splice(0, _t2 + 1).forEach(function (t) {
            r.push({ s: i, edge: t });
          });
        }if (r = r.filter(function (t) {
          return !(t.edge.ymax <= i);
        }), r.sort(function (t, e) {
          return t.edge.x === e.edge.x ? 0 : (t.edge.x - e.edge.x) / Math.abs(t.edge.x - e.edge.x);
        }), (1 !== n || c % s == 0) && r.length > 1) for (var _t3 = 0; _t3 < r.length; _t3 += 2) {
          var _e6 = _t3 + 1;if (_e6 >= r.length) break;var _s2 = r[_t3].edge,
              _n2 = r[_e6].edge;a.push([[Math.round(_s2.x), i], [Math.round(_n2.x), i]]);
        }i += n, r.forEach(function (t) {
          t.edge.x = t.edge.x + n * t.edge.islope;
        }), c++;
      }return a;
    }(i, r, a);if (h) {
      var _iteratorNormalCompletion5 = true;
      var _didIteratorError5 = false;
      var _iteratorError5 = undefined;

      try {
        for (var _iterator5 = i[Symbol.iterator](), _step5; !(_iteratorNormalCompletion5 = (_step5 = _iterator5.next()).done); _iteratorNormalCompletion5 = true) {
          var _e8 = _step5.value;
          t(_e8, c, -h);
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

      !function (e, s, n) {
        var o = [];e.forEach(function (t) {
          return o.push.apply(o, _toConsumableArray(t));
        }), t(o, s, n);
      }(l, c, -h);
    }return l;
  }function n(t, e) {
    var n;var o = e.hachureAngle + 90;var a = e.hachureGap;a < 0 && (a = 4 * e.strokeWidth), a = Math.round(Math.max(a, .1));var h = 1;return e.roughness >= 1 && ((null === (n = e.randomizer) || void 0 === n ? void 0 : n.next()) || Math.random()) > .7 && (h = a), s(t, a, o, h || 1);
  }
  var o = function () {
    function o(t) {
      _classCallCheck(this, o);

      this.helper = t;
    }

    _createClass(o, [{
      key: "fillPolygons",
      value: function fillPolygons(t, e) {
        return this._fillPolygons(t, e);
      }
    }, {
      key: "_fillPolygons",
      value: function _fillPolygons(t, e) {
        var s = n(t, e);return { type: "fillSketch", ops: this.renderLines(s, e) };
      }
    }, {
      key: "renderLines",
      value: function renderLines(t, e) {
        var s = [];var _iteratorNormalCompletion6 = true;
        var _didIteratorError6 = false;
        var _iteratorError6 = undefined;

        try {
          for (var _iterator6 = t[Symbol.iterator](), _step6; !(_iteratorNormalCompletion6 = (_step6 = _iterator6.next()).done); _iteratorNormalCompletion6 = true) {
            var _n4 = _step6.value;
            s.push.apply(s, _toConsumableArray(this.helper.doubleLineOps(_n4[0][0], _n4[0][1], _n4[1][0], _n4[1][1], e)));
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
    }]);

    return o;
  }();

  function a(t) {
    var e = t[0],
        s = t[1];return Math.sqrt(Math.pow(e[0] - s[0], 2) + Math.pow(e[1] - s[1], 2));
  }
  var h = function (_o2) {
    _inherits(h, _o2);

    function h() {
      _classCallCheck(this, h);

      return _possibleConstructorReturn(this, (h.__proto__ || Object.getPrototypeOf(h)).apply(this, arguments));
    }

    _createClass(h, [{
      key: "fillPolygons",
      value: function fillPolygons(t, e) {
        var s = e.hachureGap;s < 0 && (s = 4 * e.strokeWidth), s = Math.max(s, .1);var o = n(t, Object.assign({}, e, { hachureGap: s })),
            h = Math.PI / 180 * e.hachureAngle,
            r = [],
            i = .5 * s * Math.cos(h),
            c = .5 * s * Math.sin(h);var _iteratorNormalCompletion7 = true;
        var _didIteratorError7 = false;
        var _iteratorError7 = undefined;

        try {
          for (var _iterator7 = o[Symbol.iterator](), _step7; !(_iteratorNormalCompletion7 = (_step7 = _iterator7.next()).done); _iteratorNormalCompletion7 = true) {
            var _step7$value = _slicedToArray(_step7.value, 2),
                _t7 = _step7$value[0],
                _e9 = _step7$value[1];

            a([_t7, _e9]) && r.push([[_t7[0] - i, _t7[1] + c], [].concat(_toConsumableArray(_e9))], [[_t7[0] + i, _t7[1] - c], [].concat(_toConsumableArray(_e9))]);
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

        return { type: "fillSketch", ops: this.renderLines(r, e) };
      }
    }]);

    return h;
  }(o);

  var r = function (_o3) {
    _inherits(r, _o3);

    function r() {
      _classCallCheck(this, r);

      return _possibleConstructorReturn(this, (r.__proto__ || Object.getPrototypeOf(r)).apply(this, arguments));
    }

    _createClass(r, [{
      key: "fillPolygons",
      value: function fillPolygons(t, e) {
        var s = this._fillPolygons(t, e),
            n = Object.assign({}, e, { hachureAngle: e.hachureAngle + 90 }),
            o = this._fillPolygons(t, n);return s.ops = s.ops.concat(o.ops), s;
      }
    }]);

    return r;
  }(o);

  var i = function () {
    function i(t) {
      _classCallCheck(this, i);

      this.helper = t;
    }

    _createClass(i, [{
      key: "fillPolygons",
      value: function fillPolygons(t, e) {
        var s = n(t, e = Object.assign({}, e, { hachureAngle: 0 }));return this.dotsOnLines(s, e);
      }
    }, {
      key: "dotsOnLines",
      value: function dotsOnLines(t, e) {
        var s = [];var n = e.hachureGap;n < 0 && (n = 4 * e.strokeWidth), n = Math.max(n, .1);var o = e.fillWeight;o < 0 && (o = e.strokeWidth / 2);var h = n / 4;var _iteratorNormalCompletion8 = true;
        var _didIteratorError8 = false;
        var _iteratorError8 = undefined;

        try {
          for (var _iterator8 = t[Symbol.iterator](), _step8; !(_iteratorNormalCompletion8 = (_step8 = _iterator8.next()).done); _iteratorNormalCompletion8 = true) {
            var _r2 = _step8.value;
            var _t8 = a(_r2),
                _i = _t8 / n,
                _c = Math.ceil(_i) - 1,
                _l = _t8 - _c * n,
                _u = (_r2[0][0] + _r2[1][0]) / 2 - n / 4,
                _p = Math.min(_r2[0][1], _r2[1][1]);for (var _t9 = 0; _t9 < _c; _t9++) {
              var _a2 = _p + _l + _t9 * n,
                  _r3 = _u - h + 2 * Math.random() * h,
                  _i2 = _a2 - h + 2 * Math.random() * h,
                  _c2 = this.helper.ellipse(_r3, _i2, o, o, e);s.push.apply(s, _toConsumableArray(_c2.ops));
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

    return i;
  }();

  var c = function () {
    function c(t) {
      _classCallCheck(this, c);

      this.helper = t;
    }

    _createClass(c, [{
      key: "fillPolygons",
      value: function fillPolygons(t, e) {
        var s = n(t, e);return { type: "fillSketch", ops: this.dashedLine(s, e) };
      }
    }, {
      key: "dashedLine",
      value: function dashedLine(t, e) {
        var _this3 = this;

        var s = e.dashOffset < 0 ? e.hachureGap < 0 ? 4 * e.strokeWidth : e.hachureGap : e.dashOffset,
            n = e.dashGap < 0 ? e.hachureGap < 0 ? 4 * e.strokeWidth : e.hachureGap : e.dashGap,
            o = [];return t.forEach(function (t) {
          var h = a(t),
              r = Math.floor(h / (s + n)),
              i = (h + n - r * (s + n)) / 2;var c = t[0],
              l = t[1];c[0] > l[0] && (c = t[1], l = t[0]);var u = Math.atan((l[1] - c[1]) / (l[0] - c[0]));for (var _t10 = 0; _t10 < r; _t10++) {
            var _a3 = _t10 * (s + n),
                _h2 = _a3 + s,
                _r4 = [c[0] + _a3 * Math.cos(u) + i * Math.cos(u), c[1] + _a3 * Math.sin(u) + i * Math.sin(u)],
                _l2 = [c[0] + _h2 * Math.cos(u) + i * Math.cos(u), c[1] + _h2 * Math.sin(u) + i * Math.sin(u)];o.push.apply(o, _toConsumableArray(_this3.helper.doubleLineOps(_r4[0], _r4[1], _l2[0], _l2[1], e)));
          }
        }), o;
      }
    }]);

    return c;
  }();

  var l = function () {
    function l(t) {
      _classCallCheck(this, l);

      this.helper = t;
    }

    _createClass(l, [{
      key: "fillPolygons",
      value: function fillPolygons(t, e) {
        var s = e.hachureGap < 0 ? 4 * e.strokeWidth : e.hachureGap,
            o = e.zigzagOffset < 0 ? s : e.zigzagOffset,
            a = n(t, e = Object.assign({}, e, { hachureGap: s + o }));return { type: "fillSketch", ops: this.zigzagLines(a, o, e) };
      }
    }, {
      key: "zigzagLines",
      value: function zigzagLines(t, e, s) {
        var _this4 = this;

        var n = [];return t.forEach(function (t) {
          var o = a(t),
              h = Math.round(o / (2 * e));var r = t[0],
              i = t[1];r[0] > i[0] && (r = t[1], i = t[0]);var c = Math.atan((i[1] - r[1]) / (i[0] - r[0]));for (var _t11 = 0; _t11 < h; _t11++) {
            var _o4 = 2 * _t11 * e,
                _a4 = 2 * (_t11 + 1) * e,
                _h3 = Math.sqrt(2 * Math.pow(e, 2)),
                _i3 = [r[0] + _o4 * Math.cos(c), r[1] + _o4 * Math.sin(c)],
                _l3 = [r[0] + _a4 * Math.cos(c), r[1] + _a4 * Math.sin(c)],
                _u2 = [_i3[0] + _h3 * Math.cos(c + Math.PI / 4), _i3[1] + _h3 * Math.sin(c + Math.PI / 4)];n.push.apply(n, _toConsumableArray(_this4.helper.doubleLineOps(_i3[0], _i3[1], _u2[0], _u2[1], s)).concat(_toConsumableArray(_this4.helper.doubleLineOps(_u2[0], _u2[1], _l3[0], _l3[1], s))));
          }
        }), n;
      }
    }]);

    return l;
  }();

  var u = {};
  var p = function () {
    function p(t) {
      _classCallCheck(this, p);

      this.seed = t;
    }

    _createClass(p, [{
      key: "next",
      value: function next() {
        return this.seed ? (2 ** 31 - 1 & (this.seed = Math.imul(48271, this.seed))) / 2 ** 31 : Math.random();
      }
    }]);

    return p;
  }();

  var f = 0,
      d = 1,
      g = 2,
      M = { A: 7, a: 7, C: 6, c: 6, H: 1, h: 1, L: 2, l: 2, M: 2, m: 2, Q: 4, q: 4, S: 4, s: 4, T: 2, t: 2, V: 1, v: 1, Z: 0, z: 0 };function k(t, e) {
    return t.type === e;
  }function b(t) {
    var e = [],
        s = function (t) {
      var e = new Array();for (; "" !== t;) {
        if (t.match(/^([ \t\r\n,]+)/)) t = t.substr(RegExp.$1.length);else if (t.match(/^([aAcChHlLmMqQsStTvVzZ])/)) e[e.length] = { type: f, text: RegExp.$1 }, t = t.substr(RegExp.$1.length);else {
          if (!t.match(/^(([-+]?[0-9]+(\.[0-9]*)?|[-+]?\.[0-9]+)([eE][-+]?[0-9]+)?)/)) return [];e[e.length] = { type: d, text: "" + parseFloat(RegExp.$1) }, t = t.substr(RegExp.$1.length);
        }
      }return e[e.length] = { type: g, text: "" }, e;
    }(t);var n = "BOD",
        o = 0,
        a = s[o];for (; !k(a, g);) {
      var _h4 = 0;var _r5 = [];if ("BOD" === n) {
        if ("M" !== a.text && "m" !== a.text) return b("M0,0" + t);o++, _h4 = M[a.text], n = a.text;
      } else k(a, d) ? _h4 = M[n] : (o++, _h4 = M[a.text], n = a.text);if (!(o + _h4 < s.length)) throw new Error("Path data ended short");for (var _t12 = o; _t12 < o + _h4; _t12++) {
        var _e10 = s[_t12];if (!k(_e10, d)) throw new Error("Param not a number: " + n + "," + _e10.text);_r5[_r5.length] = +_e10.text;
      }if ("number" != typeof M[n]) throw new Error("Bad segment: " + n);{
        var _t13 = { key: n, data: _r5 };e.push(_t13), o += _h4, a = s[o], "M" === n && (n = "L"), "m" === n && (n = "l");
      }
    }return e;
  }function y(t) {
    var _r7, _r8, _r9, _r10, _r11, _r12;

    var e = 0,
        s = 0,
        n = 0,
        o = 0;var a = [];var _iteratorNormalCompletion9 = true;
    var _didIteratorError9 = false;
    var _iteratorError9 = undefined;

    try {
      for (var _iterator9 = t[Symbol.iterator](), _step9; !(_iteratorNormalCompletion9 = (_step9 = _iterator9.next()).done); _iteratorNormalCompletion9 = true) {
        var _step9$value = _step9.value,
            _h5 = _step9$value.key,
            _r6 = _step9$value.data;
        switch (_h5) {case "M":
            a.push({ key: "M", data: [].concat(_toConsumableArray(_r6)) }), (_r7 = _r6, _r8 = _slicedToArray(_r7, 2), e = _r8[0], s = _r8[1], _r7), (_r9 = _r6, _r10 = _slicedToArray(_r9, 2), n = _r10[0], o = _r10[1], _r9);break;case "m":
            e += _r6[0], s += _r6[1], a.push({ key: "M", data: [e, s] }), n = e, o = s;break;case "L":
            a.push({ key: "L", data: [].concat(_toConsumableArray(_r6)) }), (_r11 = _r6, _r12 = _slicedToArray(_r11, 2), e = _r12[0], s = _r12[1], _r11);break;case "l":
            e += _r6[0], s += _r6[1], a.push({ key: "L", data: [e, s] });break;case "C":
            a.push({ key: "C", data: [].concat(_toConsumableArray(_r6)) }), e = _r6[4], s = _r6[5];break;case "c":
            {
              var _t14 = _r6.map(function (t, n) {
                return n % 2 ? t + s : t + e;
              });a.push({ key: "C", data: _t14 }), e = _t14[4], s = _t14[5];break;
            }case "Q":
            a.push({ key: "Q", data: [].concat(_toConsumableArray(_r6)) }), e = _r6[2], s = _r6[3];break;case "q":
            {
              var _t15 = _r6.map(function (t, n) {
                return n % 2 ? t + s : t + e;
              });a.push({ key: "Q", data: _t15 }), e = _t15[2], s = _t15[3];break;
            }case "A":
            a.push({ key: "A", data: [].concat(_toConsumableArray(_r6)) }), e = _r6[5], s = _r6[6];break;case "a":
            e += _r6[5], s += _r6[6], a.push({ key: "A", data: [_r6[0], _r6[1], _r6[2], _r6[3], _r6[4], e, s] });break;case "H":
            a.push({ key: "H", data: [].concat(_toConsumableArray(_r6)) }), e = _r6[0];break;case "h":
            e += _r6[0], a.push({ key: "H", data: [e] });break;case "V":
            a.push({ key: "V", data: [].concat(_toConsumableArray(_r6)) }), s = _r6[0];break;case "v":
            s += _r6[0], a.push({ key: "V", data: [s] });break;case "S":
            a.push({ key: "S", data: [].concat(_toConsumableArray(_r6)) }), e = _r6[2], s = _r6[3];break;case "s":
            {
              var _t16 = _r6.map(function (t, n) {
                return n % 2 ? t + s : t + e;
              });a.push({ key: "S", data: _t16 }), e = _t16[2], s = _t16[3];break;
            }case "T":
            a.push({ key: "T", data: [].concat(_toConsumableArray(_r6)) }), e = _r6[0], s = _r6[1];break;case "t":
            e += _r6[0], s += _r6[1], a.push({ key: "T", data: [e, s] });break;case "Z":case "z":
            a.push({ key: "Z", data: [] }), e = n, s = o;}
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

    return a;
  }function m(t) {
    var _l5, _l6, _l7, _l8, _l9, _l10;

    var e = [];var s = "",
        n = 0,
        o = 0,
        a = 0,
        h = 0,
        r = 0,
        i = 0;var _iteratorNormalCompletion10 = true;
    var _didIteratorError10 = false;
    var _iteratorError10 = undefined;

    try {
      for (var _iterator10 = t[Symbol.iterator](), _step10; !(_iteratorNormalCompletion10 = (_step10 = _iterator10.next()).done); _iteratorNormalCompletion10 = true) {
        var _step10$value = _step10.value,
            _c3 = _step10$value.key,
            _l4 = _step10$value.data;
        switch (_c3) {case "M":
            e.push({ key: "M", data: [].concat(_toConsumableArray(_l4)) }), (_l5 = _l4, _l6 = _slicedToArray(_l5, 2), n = _l6[0], o = _l6[1], _l5), (_l7 = _l4, _l8 = _slicedToArray(_l7, 2), a = _l8[0], h = _l8[1], _l7);break;case "C":
            e.push({ key: "C", data: [].concat(_toConsumableArray(_l4)) }), n = _l4[4], o = _l4[5], r = _l4[2], i = _l4[3];break;case "L":
            e.push({ key: "L", data: [].concat(_toConsumableArray(_l4)) }), (_l9 = _l4, _l10 = _slicedToArray(_l9, 2), n = _l10[0], o = _l10[1], _l9);break;case "H":
            n = _l4[0], e.push({ key: "L", data: [n, o] });break;case "V":
            o = _l4[0], e.push({ key: "L", data: [n, o] });break;case "S":
            {
              var _t17 = 0,
                  _a5 = 0;"C" === s || "S" === s ? (_t17 = n + (n - r), _a5 = o + (o - i)) : (_t17 = n, _a5 = o), e.push({ key: "C", data: [_t17, _a5].concat(_toConsumableArray(_l4)) }), r = _l4[0], i = _l4[1], n = _l4[2], o = _l4[3];break;
            }case "T":
            {
              var _l11 = _slicedToArray(_l4, 2),
                  _t18 = _l11[0],
                  _a6 = _l11[1];

              var _h6 = 0,
                  _c4 = 0;"Q" === s || "T" === s ? (_h6 = n + (n - r), _c4 = o + (o - i)) : (_h6 = n, _c4 = o);var _u3 = n + 2 * (_h6 - n) / 3,
                  _p2 = o + 2 * (_c4 - o) / 3,
                  _f = _t18 + 2 * (_h6 - _t18) / 3,
                  _d = _a6 + 2 * (_c4 - _a6) / 3;e.push({ key: "C", data: [_u3, _p2, _f, _d, _t18, _a6] }), r = _h6, i = _c4, n = _t18, o = _a6;break;
            }case "Q":
            {
              var _l12 = _slicedToArray(_l4, 4),
                  _t19 = _l12[0],
                  _s5 = _l12[1],
                  _a7 = _l12[2],
                  _h7 = _l12[3],
                  _c5 = n + 2 * (_t19 - n) / 3,
                  _u4 = o + 2 * (_s5 - o) / 3,
                  _p3 = _a7 + 2 * (_t19 - _a7) / 3,
                  _f2 = _h7 + 2 * (_s5 - _h7) / 3;

              e.push({ key: "C", data: [_c5, _u4, _p3, _f2, _a7, _h7] }), r = _t19, i = _s5, n = _a7, o = _h7;break;
            }case "A":
            {
              var _t20 = Math.abs(_l4[0]),
                  _s6 = Math.abs(_l4[1]),
                  _a8 = _l4[2],
                  _h8 = _l4[3],
                  _r13 = _l4[4],
                  _i4 = _l4[5],
                  _c6 = _l4[6];if (0 === _t20 || 0 === _s6) e.push({ key: "C", data: [n, o, _i4, _c6, _i4, _c6] }), n = _i4, o = _c6;else if (n !== _i4 || o !== _c6) {
                x(n, o, _i4, _c6, _t20, _s6, _a8, _h8, _r13).forEach(function (t) {
                  e.push({ key: "C", data: t });
                }), n = _i4, o = _c6;
              }break;
            }case "Z":
            e.push({ key: "Z", data: [] }), n = a, o = h;}s = _c3;
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

    return e;
  }function w(t, e, s) {
    return [t * Math.cos(s) - e * Math.sin(s), t * Math.sin(s) + e * Math.cos(s)];
  }function x(t, e, s, n, o, a, h, r, i, c) {
    var l = (u = h, Math.PI * u / 180);var u;var p = [],
        f = 0,
        d = 0,
        g = 0,
        M = 0;if (c) {
      ;
      var _c7 = _slicedToArray(c, 4);

      f = _c7[0];
      d = _c7[1];
      g = _c7[2];
      M = _c7[3];
    } else {
      var _w, _w2, _w3, _w4;

      (_w = w(t, e, -l), _w2 = _slicedToArray(_w, 2), t = _w2[0], e = _w2[1], _w), (_w3 = w(s, n, -l), _w4 = _slicedToArray(_w3, 2), s = _w4[0], n = _w4[1], _w3);var _h9 = (t - s) / 2,
          _c8 = (e - n) / 2;var _u5 = _h9 * _h9 / (o * o) + _c8 * _c8 / (a * a);_u5 > 1 && (_u5 = Math.sqrt(_u5), o *= _u5, a *= _u5);var _p4 = o * o,
          _k = a * a,
          _b = _p4 * _k - _p4 * _c8 * _c8 - _k * _h9 * _h9,
          _y = _p4 * _c8 * _c8 + _k * _h9 * _h9,
          _m = (r === i ? -1 : 1) * Math.sqrt(Math.abs(_b / _y));g = _m * o * _c8 / a + (t + s) / 2, M = _m * -a * _h9 / o + (e + n) / 2, f = Math.asin(parseFloat(((e - M) / a).toFixed(9))), d = Math.asin(parseFloat(((n - M) / a).toFixed(9))), t < g && (f = Math.PI - f), s < g && (d = Math.PI - d), f < 0 && (f = 2 * Math.PI + f), d < 0 && (d = 2 * Math.PI + d), i && f > d && (f -= 2 * Math.PI), !i && d > f && (d -= 2 * Math.PI);
    }var k = d - f;if (Math.abs(k) > 120 * Math.PI / 180) {
      var _t21 = d,
          _e11 = s,
          _r14 = n;d = i && d > f ? f + 120 * Math.PI / 180 * 1 : f + 120 * Math.PI / 180 * -1, p = x(s = g + o * Math.cos(d), n = M + a * Math.sin(d), _e11, _r14, o, a, h, 0, i, [d, _t21, g, M]);
    }k = d - f;var b = Math.cos(f),
        y = Math.sin(f),
        m = Math.cos(d),
        P = Math.sin(d),
        v = Math.tan(k / 4),
        S = 4 / 3 * o * v,
        O = 4 / 3 * a * v,
        L = [t, e],
        T = [t + S * y, e - O * b],
        D = [s + S * P, n - O * m],
        A = [s, n];if (T[0] = 2 * L[0] - T[0], T[1] = 2 * L[1] - T[1], c) return [T, D, A].concat(p);{
      p = [T, D, A].concat(p);var _t22 = [];for (var _e12 = 0; _e12 < p.length; _e12 += 3) {
        var _s7 = w(p[_e12][0], p[_e12][1], l),
            _n5 = w(p[_e12 + 1][0], p[_e12 + 1][1], l),
            _o5 = w(p[_e12 + 2][0], p[_e12 + 2][1], l);_t22.push([_s7[0], _s7[1], _n5[0], _n5[1], _o5[0], _o5[1]]);
      }return _t22;
    }
  }var P = { randOffset: function randOffset(t, e) {
      return G(t, e);
    }, randOffsetWithRange: function randOffsetWithRange(t, e, s) {
      return E(t, e, s);
    }, ellipse: function ellipse(t, e, s, n, o) {
      var a = T(s, n, o);return D(t, e, o, a).opset;
    }, doubleLineOps: function doubleLineOps(t, e, s, n, o) {
      return $(t, e, s, n, o, !0);
    } };function v(t, e, s, n, o) {
    return { type: "path", ops: $(t, e, s, n, o) };
  }function S(t, e, s) {
    var n = (t || []).length;if (n > 2) {
      var _o6 = [];for (var _e13 = 0; _e13 < n - 1; _e13++) {
        _o6.push.apply(_o6, _toConsumableArray($(t[_e13][0], t[_e13][1], t[_e13 + 1][0], t[_e13 + 1][1], s)));
      }return e && _o6.push.apply(_o6, _toConsumableArray($(t[n - 1][0], t[n - 1][1], t[0][0], t[0][1], s))), { type: "path", ops: _o6 };
    }return 2 === n ? v(t[0][0], t[0][1], t[1][0], t[1][1], s) : { type: "path", ops: [] };
  }function O(t, e, s, n, o) {
    return function (t, e) {
      return S(t, !0, e);
    }([[t, e], [t + s, e], [t + s, e + n], [t, e + n]], o);
  }function L(t, e) {
    if (t.length) {
      var _s8 = "number" == typeof t[0][0] ? [t] : t,
          _n6 = j(_s8[0], 1 * (1 + .2 * e.roughness), e),
          _o7 = e.disableMultiStroke ? [] : j(_s8[0], 1.5 * (1 + .22 * e.roughness), z(e));for (var _t23 = 1; _t23 < _s8.length; _t23++) {
        var _a9 = _s8[_t23];if (_a9.length) {
          var _t24 = j(_a9, 1 * (1 + .2 * e.roughness), e),
              _s9 = e.disableMultiStroke ? [] : j(_a9, 1.5 * (1 + .22 * e.roughness), z(e));var _iteratorNormalCompletion11 = true;
          var _didIteratorError11 = false;
          var _iteratorError11 = undefined;

          try {
            for (var _iterator11 = _t24[Symbol.iterator](), _step11; !(_iteratorNormalCompletion11 = (_step11 = _iterator11.next()).done); _iteratorNormalCompletion11 = true) {
              var _e14 = _step11.value;
              "move" !== _e14.op && _n6.push(_e14);
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

          var _iteratorNormalCompletion12 = true;
          var _didIteratorError12 = false;
          var _iteratorError12 = undefined;

          try {
            for (var _iterator12 = _s9[Symbol.iterator](), _step12; !(_iteratorNormalCompletion12 = (_step12 = _iterator12.next()).done); _iteratorNormalCompletion12 = true) {
              var _t25 = _step12.value;
              "move" !== _t25.op && _o7.push(_t25);
            }
          } catch (err) {
            _didIteratorError12 = true;
            _iteratorError12 = err;
          } finally {
            try {
              if (!_iteratorNormalCompletion12 && _iterator12.return) {
                _iterator12.return();
              }
            } finally {
              if (_didIteratorError12) {
                throw _iteratorError12;
              }
            }
          }
        }
      }return { type: "path", ops: _n6.concat(_o7) };
    }return { type: "path", ops: [] };
  }function T(t, e, s) {
    var n = Math.sqrt(2 * Math.PI * Math.sqrt((Math.pow(t / 2, 2) + Math.pow(e / 2, 2)) / 2)),
        o = Math.ceil(Math.max(s.curveStepCount, s.curveStepCount / Math.sqrt(200) * n)),
        a = 2 * Math.PI / o;var h = Math.abs(t / 2),
        r = Math.abs(e / 2);var i = 1 - s.curveFitting;return h += G(h * i, s), r += G(r * i, s), { increment: a, rx: h, ry: r };
  }function D(t, e, s, n) {
    var _F = F(n.increment, t, e, n.rx, n.ry, 1, n.increment * E(.1, E(.4, 1, s), s), s),
        _F2 = _slicedToArray(_F, 2),
        o = _F2[0],
        a = _F2[1];

    var h = q(o, null, s);if (!s.disableMultiStroke && 0 !== s.roughness) {
      var _F3 = F(n.increment, t, e, n.rx, n.ry, 1.5, 0, s),
          _F4 = _slicedToArray(_F3, 1),
          _o8 = _F4[0],
          _a10 = q(_o8, null, s);

      h = h.concat(_a10);
    }return { estimatedPoints: a, opset: { type: "path", ops: h } };
  }function A(t, e, s, n, o, a, h, r, i) {
    var c = t,
        l = e;var u = Math.abs(s / 2),
        p = Math.abs(n / 2);u += G(.01 * u, i), p += G(.01 * p, i);var f = o,
        d = a;for (; f < 0;) {
      f += 2 * Math.PI, d += 2 * Math.PI;
    }d - f > 2 * Math.PI && (f = 0, d = 2 * Math.PI);var g = 2 * Math.PI / i.curveStepCount,
        M = Math.min(g / 2, (d - f) / 2),
        k = V(M, c, l, u, p, f, d, 1, i);if (!i.disableMultiStroke) {
      var _t26 = V(M, c, l, u, p, f, d, 1.5, i);k.push.apply(k, _toConsumableArray(_t26));
    }return h && (r ? k.push.apply(k, _toConsumableArray($(c, l, c + u * Math.cos(f), l + p * Math.sin(f), i)).concat(_toConsumableArray($(c, l, c + u * Math.cos(d), l + p * Math.sin(d), i)))) : k.push({ op: "lineTo", data: [c, l] }, { op: "lineTo", data: [c + u * Math.cos(f), l + p * Math.sin(f)] })), { type: "path", ops: k };
  }function _(t, e) {
    var s = m(y(b(t))),
        n = [];var o = [0, 0],
        a = [0, 0];var _iteratorNormalCompletion13 = true;
    var _didIteratorError13 = false;
    var _iteratorError13 = undefined;

    try {
      for (var _iterator13 = s[Symbol.iterator](), _step13; !(_iteratorNormalCompletion13 = (_step13 = _iterator13.next()).done); _iteratorNormalCompletion13 = true) {
        var _step13$value = _step13.value,
            _t27 = _step13$value.key,
            _h10 = _step13$value.data;
        switch (_t27) {case "M":
            a = [_h10[0], _h10[1]], o = [_h10[0], _h10[1]];break;case "L":
            n.push.apply(n, _toConsumableArray($(a[0], a[1], _h10[0], _h10[1], e))), a = [_h10[0], _h10[1]];break;case "C":
            {
              var _h11 = _slicedToArray(_h10, 6),
                  _t28 = _h11[0],
                  _s10 = _h11[1],
                  _o9 = _h11[2],
                  _r15 = _h11[3],
                  _i5 = _h11[4],
                  _c9 = _h11[5];

              n.push.apply(n, _toConsumableArray(Z(_t28, _s10, _o9, _r15, _i5, _c9, a, e))), a = [_i5, _c9];break;
            }case "Z":
            n.push.apply(n, _toConsumableArray($(a[0], a[1], o[0], o[1], e))), a = [o[0], o[1]];}
      }
    } catch (err) {
      _didIteratorError13 = true;
      _iteratorError13 = err;
    } finally {
      try {
        if (!_iteratorNormalCompletion13 && _iterator13.return) {
          _iterator13.return();
        }
      } finally {
        if (_didIteratorError13) {
          throw _iteratorError13;
        }
      }
    }

    return { type: "path", ops: n };
  }function I(t, e) {
    var s = [];var _iteratorNormalCompletion14 = true;
    var _didIteratorError14 = false;
    var _iteratorError14 = undefined;

    try {
      for (var _iterator14 = t[Symbol.iterator](), _step14; !(_iteratorNormalCompletion14 = (_step14 = _iterator14.next()).done); _iteratorNormalCompletion14 = true) {
        var _n7 = _step14.value;
        if (_n7.length) {
          var _t29 = e.maxRandomnessOffset || 0,
              _o10 = _n7.length;if (_o10 > 2) {
            s.push({ op: "move", data: [_n7[0][0] + G(_t29, e), _n7[0][1] + G(_t29, e)] });for (var _a11 = 1; _a11 < _o10; _a11++) {
              s.push({ op: "lineTo", data: [_n7[_a11][0] + G(_t29, e), _n7[_a11][1] + G(_t29, e)] });
            }
          }
        }
      }
    } catch (err) {
      _didIteratorError14 = true;
      _iteratorError14 = err;
    } finally {
      try {
        if (!_iteratorNormalCompletion14 && _iterator14.return) {
          _iterator14.return();
        }
      } finally {
        if (_didIteratorError14) {
          throw _iteratorError14;
        }
      }
    }

    return { type: "fillPath", ops: s };
  }function C(t, e) {
    return function (t, e) {
      var s = t.fillStyle || "hachure";if (!u[s]) switch (s) {case "zigzag":
          u[s] || (u[s] = new h(e));break;case "cross-hatch":
          u[s] || (u[s] = new r(e));break;case "dots":
          u[s] || (u[s] = new i(e));break;case "dashed":
          u[s] || (u[s] = new c(e));break;case "zigzag-line":
          u[s] || (u[s] = new l(e));break;default:
          s = "hachure", u[s] || (u[s] = new o(e));}return u[s];
    }(e, P).fillPolygons(t, e);
  }function z(t) {
    var e = Object.assign({}, t);return e.randomizer = void 0, t.seed && (e.seed = t.seed + 1), e;
  }function W(t) {
    return t.randomizer || (t.randomizer = new p(t.seed || 0)), t.randomizer.next();
  }function E(t, e, s) {
    var n = arguments.length > 3 && arguments[3] !== undefined ? arguments[3] : 1;
    return s.roughness * n * (W(s) * (e - t) + t);
  }function G(t, e) {
    var s = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : 1;
    return E(-t, t, e, s);
  }function $(t, e, s, n, o) {
    var a = arguments.length > 5 && arguments[5] !== undefined ? arguments[5] : !1;
    var h = a ? o.disableMultiStrokeFill : o.disableMultiStroke,
        r = R(t, e, s, n, o, !0, !1);if (h) return r;var i = R(t, e, s, n, o, !0, !0);return r.concat(i);
  }function R(t, e, s, n, o, a, h) {
    var r = Math.pow(t - s, 2) + Math.pow(e - n, 2),
        i = Math.sqrt(r);var c = 1;c = i < 200 ? 1 : i > 500 ? .4 : -.0016668 * i + 1.233334;var l = o.maxRandomnessOffset || 0;l * l * 100 > r && (l = i / 10);var u = l / 2,
        p = .2 + .2 * W(o);var f = o.bowing * o.maxRandomnessOffset * (n - e) / 200,
        d = o.bowing * o.maxRandomnessOffset * (t - s) / 200;f = G(f, o, c), d = G(d, o, c);var g = [],
        M = function M() {
      return G(u, o, c);
    },
        k = function k() {
      return G(l, o, c);
    },
        b = o.preserveVertices;return a && (h ? g.push({ op: "move", data: [t + (b ? 0 : M()), e + (b ? 0 : M())] }) : g.push({ op: "move", data: [t + (b ? 0 : G(l, o, c)), e + (b ? 0 : G(l, o, c))] })), h ? g.push({ op: "bcurveTo", data: [f + t + (s - t) * p + M(), d + e + (n - e) * p + M(), f + t + 2 * (s - t) * p + M(), d + e + 2 * (n - e) * p + M(), s + (b ? 0 : M()), n + (b ? 0 : M())] }) : g.push({ op: "bcurveTo", data: [f + t + (s - t) * p + k(), d + e + (n - e) * p + k(), f + t + 2 * (s - t) * p + k(), d + e + 2 * (n - e) * p + k(), s + (b ? 0 : k()), n + (b ? 0 : k())] }), g;
  }function j(t, e, s) {
    if (!t.length) return [];var n = [];n.push([t[0][0] + G(e, s), t[0][1] + G(e, s)]), n.push([t[0][0] + G(e, s), t[0][1] + G(e, s)]);for (var _o11 = 1; _o11 < t.length; _o11++) {
      n.push([t[_o11][0] + G(e, s), t[_o11][1] + G(e, s)]), _o11 === t.length - 1 && n.push([t[_o11][0] + G(e, s), t[_o11][1] + G(e, s)]);
    }return q(n, null, s);
  }function q(t, e, s) {
    var n = t.length,
        o = [];if (n > 3) {
      var _a12 = [],
          _h12 = 1 - s.curveTightness;o.push({ op: "move", data: [t[1][0], t[1][1]] });for (var _e15 = 1; _e15 + 2 < n; _e15++) {
        var _s11 = t[_e15];_a12[0] = [_s11[0], _s11[1]], _a12[1] = [_s11[0] + (_h12 * t[_e15 + 1][0] - _h12 * t[_e15 - 1][0]) / 6, _s11[1] + (_h12 * t[_e15 + 1][1] - _h12 * t[_e15 - 1][1]) / 6], _a12[2] = [t[_e15 + 1][0] + (_h12 * t[_e15][0] - _h12 * t[_e15 + 2][0]) / 6, t[_e15 + 1][1] + (_h12 * t[_e15][1] - _h12 * t[_e15 + 2][1]) / 6], _a12[3] = [t[_e15 + 1][0], t[_e15 + 1][1]], o.push({ op: "bcurveTo", data: [_a12[1][0], _a12[1][1], _a12[2][0], _a12[2][1], _a12[3][0], _a12[3][1]] });
      }if (e && 2 === e.length) {
        var _t30 = s.maxRandomnessOffset;o.push({ op: "lineTo", data: [e[0] + G(_t30, s), e[1] + G(_t30, s)] });
      }
    } else 3 === n ? (o.push({ op: "move", data: [t[1][0], t[1][1]] }), o.push({ op: "bcurveTo", data: [t[1][0], t[1][1], t[2][0], t[2][1], t[2][0], t[2][1]] })) : 2 === n && o.push.apply(o, _toConsumableArray(R(t[0][0], t[0][1], t[1][0], t[1][1], s, !0, !0)));return o;
  }function F(t, e, s, n, o, a, h, r) {
    var i = [],
        c = [];if (0 === r.roughness) {
      t /= 4, c.push([e + n * Math.cos(-t), s + o * Math.sin(-t)]);for (var _a13 = 0; _a13 <= 2 * Math.PI; _a13 += t) {
        var _t31 = [e + n * Math.cos(_a13), s + o * Math.sin(_a13)];i.push(_t31), c.push(_t31);
      }c.push([e + n * Math.cos(0), s + o * Math.sin(0)]), c.push([e + n * Math.cos(t), s + o * Math.sin(t)]);
    } else {
      var _l13 = G(.5, r) - Math.PI / 2;c.push([G(a, r) + e + .9 * n * Math.cos(_l13 - t), G(a, r) + s + .9 * o * Math.sin(_l13 - t)]);var _u6 = 2 * Math.PI + _l13 - .01;for (var _h13 = _l13; _h13 < _u6; _h13 += t) {
        var _t32 = [G(a, r) + e + n * Math.cos(_h13), G(a, r) + s + o * Math.sin(_h13)];i.push(_t32), c.push(_t32);
      }c.push([G(a, r) + e + n * Math.cos(_l13 + 2 * Math.PI + .5 * h), G(a, r) + s + o * Math.sin(_l13 + 2 * Math.PI + .5 * h)]), c.push([G(a, r) + e + .98 * n * Math.cos(_l13 + h), G(a, r) + s + .98 * o * Math.sin(_l13 + h)]), c.push([G(a, r) + e + .9 * n * Math.cos(_l13 + .5 * h), G(a, r) + s + .9 * o * Math.sin(_l13 + .5 * h)]);
    }return [c, i];
  }function V(t, e, s, n, o, a, h, r, i) {
    var c = a + G(.1, i),
        l = [];l.push([G(r, i) + e + .9 * n * Math.cos(c - t), G(r, i) + s + .9 * o * Math.sin(c - t)]);for (var _a14 = c; _a14 <= h; _a14 += t) {
      l.push([G(r, i) + e + n * Math.cos(_a14), G(r, i) + s + o * Math.sin(_a14)]);
    }return l.push([e + n * Math.cos(h), s + o * Math.sin(h)]), l.push([e + n * Math.cos(h), s + o * Math.sin(h)]), q(l, null, i);
  }function Z(t, e, s, n, o, a, h, r) {
    var i = [],
        c = [r.maxRandomnessOffset || 1, (r.maxRandomnessOffset || 1) + .3];var l = [0, 0];var u = r.disableMultiStroke ? 1 : 2,
        p = r.preserveVertices;for (var _f3 = 0; _f3 < u; _f3++) {
      0 === _f3 ? i.push({ op: "move", data: [h[0], h[1]] }) : i.push({ op: "move", data: [h[0] + (p ? 0 : G(c[0], r)), h[1] + (p ? 0 : G(c[0], r))] }), l = p ? [o, a] : [o + G(c[_f3], r), a + G(c[_f3], r)], i.push({ op: "bcurveTo", data: [t + G(c[_f3], r), e + G(c[_f3], r), s + G(c[_f3], r), n + G(c[_f3], r), l[0], l[1]] });
    }return i;
  }function Q(t) {
    return [].concat(_toConsumableArray(t));
  }function H(t) {
    var e = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : 0;
    var s = t.length;if (s < 3) throw new Error("A curve must have at least three points.");var n = [];if (3 === s) n.push(Q(t[0]), Q(t[1]), Q(t[2]), Q(t[2]));else {
      var _s12 = [];_s12.push(t[0], t[0]);for (var _e16 = 1; _e16 < t.length; _e16++) {
        _s12.push(t[_e16]), _e16 === t.length - 1 && _s12.push(t[_e16]);
      }var _o12 = [],
          _a15 = 1 - e;n.push(Q(_s12[0]));for (var _t33 = 1; _t33 + 2 < _s12.length; _t33++) {
        var _e17 = _s12[_t33];_o12[0] = [_e17[0], _e17[1]], _o12[1] = [_e17[0] + (_a15 * _s12[_t33 + 1][0] - _a15 * _s12[_t33 - 1][0]) / 6, _e17[1] + (_a15 * _s12[_t33 + 1][1] - _a15 * _s12[_t33 - 1][1]) / 6], _o12[2] = [_s12[_t33 + 1][0] + (_a15 * _s12[_t33][0] - _a15 * _s12[_t33 + 2][0]) / 6, _s12[_t33 + 1][1] + (_a15 * _s12[_t33][1] - _a15 * _s12[_t33 + 2][1]) / 6], _o12[3] = [_s12[_t33 + 1][0], _s12[_t33 + 1][1]], n.push(_o12[1], _o12[2], _o12[3]);
      }
    }return n;
  }function N(t, e) {
    return Math.pow(t[0] - e[0], 2) + Math.pow(t[1] - e[1], 2);
  }function B(t, e, s) {
    var n = N(e, s);if (0 === n) return N(t, e);var o = ((t[0] - e[0]) * (s[0] - e[0]) + (t[1] - e[1]) * (s[1] - e[1])) / n;return o = Math.max(0, Math.min(1, o)), N(t, J(e, s, o));
  }function J(t, e, s) {
    return [t[0] + (e[0] - t[0]) * s, t[1] + (e[1] - t[1]) * s];
  }function K(t, e, s, n) {
    var o = n || [];if (function (t, e) {
      var s = t[e + 0],
          n = t[e + 1],
          o = t[e + 2],
          a = t[e + 3];var h = 3 * n[0] - 2 * s[0] - a[0];h *= h;var r = 3 * n[1] - 2 * s[1] - a[1];r *= r;var i = 3 * o[0] - 2 * a[0] - s[0];i *= i;var c = 3 * o[1] - 2 * a[1] - s[1];return c *= c, h < i && (h = i), r < c && (r = c), h + r;
    }(t, e) < s) {
      var _s13 = t[e + 0];if (o.length) {
        (a = o[o.length - 1], h = _s13, Math.sqrt(N(a, h))) > 1 && o.push(_s13);
      } else o.push(_s13);o.push(t[e + 3]);
    } else {
      var _n8 = .5,
          _a16 = t[e + 0],
          _h14 = t[e + 1],
          _r16 = t[e + 2],
          _i6 = t[e + 3],
          _c10 = J(_a16, _h14, _n8),
          _l14 = J(_h14, _r16, _n8),
          _u7 = J(_r16, _i6, _n8),
          _p5 = J(_c10, _l14, _n8),
          _f4 = J(_l14, _u7, _n8),
          _d2 = J(_p5, _f4, _n8);K([_a16, _c10, _p5, _d2], 0, s, o), K([_d2, _f4, _u7, _i6], 0, s, o);
    }var a, h;return o;
  }function U(t, e) {
    return X(t, 0, t.length, e);
  }function X(t, e, s, n, o) {
    var a = o || [],
        h = t[e],
        r = t[s - 1];var i = 0,
        c = 1;for (var _n9 = e + 1; _n9 < s - 1; ++_n9) {
      var _e18 = B(t[_n9], h, r);_e18 > i && (i = _e18, c = _n9);
    }return Math.sqrt(i) > n ? (X(t, e, c + 1, n, a), X(t, c, s, n, a)) : (a.length || a.push(h), a.push(r)), a;
  }function Y(t) {
    var e = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : .15;
    var s = arguments[2];
    var n = [],
        o = (t.length - 1) / 3;for (var _s14 = 0; _s14 < o; _s14++) {
      K(t, 3 * _s14, e, n);
    }return s && s > 0 ? X(n, 0, n.length, s) : n;
  }var tt = "none";
  var et = function () {
    function et(t) {
      _classCallCheck(this, et);

      this.defaultOptions = { maxRandomnessOffset: 2, roughness: 1, bowing: 1, stroke: "#000", strokeWidth: 1, curveTightness: 0, curveFitting: .95, curveStepCount: 9, fillStyle: "hachure", fillWeight: -1, hachureAngle: -41, hachureGap: -1, dashOffset: -1, dashGap: -1, zigzagOffset: -1, seed: 0, disableMultiStroke: !1, disableMultiStrokeFill: !1, preserveVertices: !1, fillShapeRoughnessGain: .8 }, this.config = t || {}, this.config.options && (this.defaultOptions = this._o(this.config.options));
    }

    _createClass(et, [{
      key: "_o",
      value: function _o(t) {
        return t ? Object.assign({}, this.defaultOptions, t) : this.defaultOptions;
      }
    }, {
      key: "_d",
      value: function _d(t, e, s) {
        return { shape: t, sets: e || [], options: s || this.defaultOptions };
      }
    }, {
      key: "line",
      value: function line(t, e, s, n, o) {
        var a = this._o(o);return this._d("line", [v(t, e, s, n, a)], a);
      }
    }, {
      key: "rectangle",
      value: function rectangle(t, e, s, n, o) {
        var a = this._o(o),
            h = [],
            r = O(t, e, s, n, a);if (a.fill) {
          var _o13 = [[t, e], [t + s, e], [t + s, e + n], [t, e + n]];"solid" === a.fillStyle ? h.push(I([_o13], a)) : h.push(C([_o13], a));
        }return a.stroke !== tt && h.push(r), this._d("rectangle", h, a);
      }
    }, {
      key: "ellipse",
      value: function ellipse(t, e, s, n, o) {
        var a = this._o(o),
            h = [],
            r = T(s, n, a),
            i = D(t, e, a, r);if (a.fill) if ("solid" === a.fillStyle) {
          var _s15 = D(t, e, a, r).opset;_s15.type = "fillPath", h.push(_s15);
        } else h.push(C([i.estimatedPoints], a));return a.stroke !== tt && h.push(i.opset), this._d("ellipse", h, a);
      }
    }, {
      key: "circle",
      value: function circle(t, e, s, n) {
        var o = this.ellipse(t, e, s, s, n);return o.shape = "circle", o;
      }
    }, {
      key: "linearPath",
      value: function linearPath(t, e) {
        var s = this._o(e);return this._d("linearPath", [S(t, !1, s)], s);
      }
    }, {
      key: "arc",
      value: function arc(t, e, s, n, o, a) {
        var h = arguments.length > 6 && arguments[6] !== undefined ? arguments[6] : !1;
        var r = arguments[7];
        var i = this._o(r),
            c = [],
            l = A(t, e, s, n, o, a, h, !0, i);if (h && i.fill) if ("solid" === i.fillStyle) {
          var _h15 = Object.assign({}, i);_h15.disableMultiStroke = !0;var _r17 = A(t, e, s, n, o, a, !0, !1, _h15);_r17.type = "fillPath", c.push(_r17);
        } else c.push(function (t, e, s, n, o, a, h) {
          var r = t,
              i = e;var c = Math.abs(s / 2),
              l = Math.abs(n / 2);c += G(.01 * c, h), l += G(.01 * l, h);var u = o,
              p = a;for (; u < 0;) {
            u += 2 * Math.PI, p += 2 * Math.PI;
          }p - u > 2 * Math.PI && (u = 0, p = 2 * Math.PI);var f = (p - u) / h.curveStepCount,
              d = [];for (var _t34 = u; _t34 <= p; _t34 += f) {
            d.push([r + c * Math.cos(_t34), i + l * Math.sin(_t34)]);
          }return d.push([r + c * Math.cos(p), i + l * Math.sin(p)]), d.push([r, i]), C([d], h);
        }(t, e, s, n, o, a, i));return i.stroke !== tt && c.push(l), this._d("arc", c, i);
      }
    }, {
      key: "curve",
      value: function curve(t, e) {
        var s = this._o(e),
            n = [],
            o = L(t, s);if (s.fill && s.fill !== tt) if ("solid" === s.fillStyle) {
          var _e19 = L(t, Object.assign(Object.assign({}, s), { disableMultiStroke: !0, roughness: s.roughness ? s.roughness + s.fillShapeRoughnessGain : 0 }));n.push({ type: "fillPath", ops: this._mergedShape(_e19.ops) });
        } else {
          var _e20 = [],
              _o14 = t;if (_o14.length) {
            var _t35 = "number" == typeof _o14[0][0] ? [_o14] : _o14;var _iteratorNormalCompletion15 = true;
            var _didIteratorError15 = false;
            var _iteratorError15 = undefined;

            try {
              for (var _iterator15 = _t35[Symbol.iterator](), _step15; !(_iteratorNormalCompletion15 = (_step15 = _iterator15.next()).done); _iteratorNormalCompletion15 = true) {
                var _n10 = _step15.value;
                _n10.length < 3 ? _e20.push.apply(_e20, _toConsumableArray(_n10)) : 3 === _n10.length ? _e20.push.apply(_e20, _toConsumableArray(Y(H([_n10[0], _n10[0], _n10[1], _n10[2]]), 10, (1 + s.roughness) / 2))) : _e20.push.apply(_e20, _toConsumableArray(Y(H(_n10), 10, (1 + s.roughness) / 2)));
              }
            } catch (err) {
              _didIteratorError15 = true;
              _iteratorError15 = err;
            } finally {
              try {
                if (!_iteratorNormalCompletion15 && _iterator15.return) {
                  _iterator15.return();
                }
              } finally {
                if (_didIteratorError15) {
                  throw _iteratorError15;
                }
              }
            }
          }_e20.length && n.push(C([_e20], s));
        }return s.stroke !== tt && n.push(o), this._d("curve", n, s);
      }
    }, {
      key: "polygon",
      value: function polygon(t, e) {
        var s = this._o(e),
            n = [],
            o = S(t, !0, s);return s.fill && ("solid" === s.fillStyle ? n.push(I([t], s)) : n.push(C([t], s))), s.stroke !== tt && n.push(o), this._d("polygon", n, s);
      }
    }, {
      key: "path",
      value: function path(t, e) {
        var s = this._o(e),
            n = [];if (!t) return this._d("path", n, s);t = (t || "").replace(/\n/g, " ").replace(/(-\s)/g, "-").replace("/(ss)/g", " ");var o = s.fill && "transparent" !== s.fill && s.fill !== tt,
            a = s.stroke !== tt,
            h = !!(s.simplification && s.simplification < 1),
            r = function (t, e, s) {
          var n = m(y(b(t))),
              o = [];var a = [],
              h = [0, 0],
              r = [];var i = function i() {
            var _a17;

            r.length >= 4 && (_a17 = a).push.apply(_a17, _toConsumableArray(Y(r, e))), r = [];
          },
              c = function c() {
            i(), a.length && (o.push(a), a = []);
          };var _iteratorNormalCompletion16 = true;
          var _didIteratorError16 = false;
          var _iteratorError16 = undefined;

          try {
            for (var _iterator16 = n[Symbol.iterator](), _step16; !(_iteratorNormalCompletion16 = (_step16 = _iterator16.next()).done); _iteratorNormalCompletion16 = true) {
              var _step16$value = _step16.value,
                  _t36 = _step16$value.key,
                  _e21 = _step16$value.data;
              switch (_t36) {case "M":
                  c(), h = [_e21[0], _e21[1]], a.push(h);break;case "L":
                  i(), a.push([_e21[0], _e21[1]]);break;case "C":
                  if (!r.length) {
                    var _t37 = a.length ? a[a.length - 1] : h;r.push([_t37[0], _t37[1]]);
                  }r.push([_e21[0], _e21[1]]), r.push([_e21[2], _e21[3]]), r.push([_e21[4], _e21[5]]);break;case "Z":
                  i(), a.push([h[0], h[1]]);}
            }
          } catch (err) {
            _didIteratorError16 = true;
            _iteratorError16 = err;
          } finally {
            try {
              if (!_iteratorNormalCompletion16 && _iterator16.return) {
                _iterator16.return();
              }
            } finally {
              if (_didIteratorError16) {
                throw _iteratorError16;
              }
            }
          }

          if (c(), !s) return o;var l = [];var _iteratorNormalCompletion17 = true;
          var _didIteratorError17 = false;
          var _iteratorError17 = undefined;

          try {
            for (var _iterator17 = o[Symbol.iterator](), _step17; !(_iteratorNormalCompletion17 = (_step17 = _iterator17.next()).done); _iteratorNormalCompletion17 = true) {
              var _t38 = _step17.value;
              var _e22 = U(_t38, s);_e22.length && l.push(_e22);
            }
          } catch (err) {
            _didIteratorError17 = true;
            _iteratorError17 = err;
          } finally {
            try {
              if (!_iteratorNormalCompletion17 && _iterator17.return) {
                _iterator17.return();
              }
            } finally {
              if (_didIteratorError17) {
                throw _iteratorError17;
              }
            }
          }

          return l;
        }(t, 1, h ? 4 - 4 * (s.simplification || 1) : (1 + s.roughness) / 2),
            i = _(t, s);if (o) if ("solid" === s.fillStyle) {
          if (1 === r.length) {
            var _e23 = _(t, Object.assign(Object.assign({}, s), { disableMultiStroke: !0, roughness: s.roughness ? s.roughness + s.fillShapeRoughnessGain : 0 }));n.push({ type: "fillPath", ops: this._mergedShape(_e23.ops) });
          } else n.push(I(r, s));
        } else n.push(C(r, s));return a && (h ? r.forEach(function (t) {
          n.push(S(t, !1, s));
        }) : n.push(i)), this._d("path", n, s);
      }
    }, {
      key: "opsToPath",
      value: function opsToPath(t, e) {
        var s = "";var _iteratorNormalCompletion18 = true;
        var _didIteratorError18 = false;
        var _iteratorError18 = undefined;

        try {
          for (var _iterator18 = t.ops[Symbol.iterator](), _step18; !(_iteratorNormalCompletion18 = (_step18 = _iterator18.next()).done); _iteratorNormalCompletion18 = true) {
            var _n11 = _step18.value;
            var _t39 = "number" == typeof e && e >= 0 ? _n11.data.map(function (t) {
              return +t.toFixed(e);
            }) : _n11.data;switch (_n11.op) {case "move":
                s += "M" + _t39[0] + " " + _t39[1] + " ";break;case "bcurveTo":
                s += "C" + _t39[0] + " " + _t39[1] + ", " + _t39[2] + " " + _t39[3] + ", " + _t39[4] + " " + _t39[5] + " ";break;case "lineTo":
                s += "L" + _t39[0] + " " + _t39[1] + " ";}
          }
        } catch (err) {
          _didIteratorError18 = true;
          _iteratorError18 = err;
        } finally {
          try {
            if (!_iteratorNormalCompletion18 && _iterator18.return) {
              _iterator18.return();
            }
          } finally {
            if (_didIteratorError18) {
              throw _iteratorError18;
            }
          }
        }

        return s.trim();
      }
    }, {
      key: "toPaths",
      value: function toPaths(t) {
        var e = t.sets || [],
            s = t.options || this.defaultOptions,
            n = [];var _iteratorNormalCompletion19 = true;
        var _didIteratorError19 = false;
        var _iteratorError19 = undefined;

        try {
          for (var _iterator19 = e[Symbol.iterator](), _step19; !(_iteratorNormalCompletion19 = (_step19 = _iterator19.next()).done); _iteratorNormalCompletion19 = true) {
            var _t40 = _step19.value;
            var _e24 = null;switch (_t40.type) {case "path":
                _e24 = { d: this.opsToPath(_t40), stroke: s.stroke, strokeWidth: s.strokeWidth, fill: tt };break;case "fillPath":
                _e24 = { d: this.opsToPath(_t40), stroke: tt, strokeWidth: 0, fill: s.fill || tt };break;case "fillSketch":
                _e24 = this.fillSketch(_t40, s);}_e24 && n.push(_e24);
          }
        } catch (err) {
          _didIteratorError19 = true;
          _iteratorError19 = err;
        } finally {
          try {
            if (!_iteratorNormalCompletion19 && _iterator19.return) {
              _iterator19.return();
            }
          } finally {
            if (_didIteratorError19) {
              throw _iteratorError19;
            }
          }
        }

        return n;
      }
    }, {
      key: "fillSketch",
      value: function fillSketch(t, e) {
        var s = e.fillWeight;return s < 0 && (s = e.strokeWidth / 2), { d: this.opsToPath(t), stroke: e.fill || tt, strokeWidth: s, fill: tt };
      }
    }, {
      key: "_mergedShape",
      value: function _mergedShape(t) {
        return t.filter(function (t, e) {
          return 0 === e || "move" !== t.op;
        });
      }
    }], [{
      key: "newSeed",
      value: function newSeed() {
        return Math.floor(Math.random() * 2 ** 31);
      }
    }]);

    return et;
  }();

  var st = function () {
    function st(t, e) {
      _classCallCheck(this, st);

      this.canvas = t, this.ctx = this.canvas.getContext("2d"), this.gen = new et(e);
    }

    _createClass(st, [{
      key: "draw",
      value: function draw(t) {
        var e = t.sets || [],
            s = t.options || this.getDefaultOptions(),
            n = this.ctx,
            o = t.options.fixedDecimalPlaceDigits;var _iteratorNormalCompletion20 = true;
        var _didIteratorError20 = false;
        var _iteratorError20 = undefined;

        try {
          for (var _iterator20 = e[Symbol.iterator](), _step20; !(_iteratorNormalCompletion20 = (_step20 = _iterator20.next()).done); _iteratorNormalCompletion20 = true) {
            var _a18 = _step20.value;
            switch (_a18.type) {case "path":
                n.save(), n.strokeStyle = "none" === s.stroke ? "transparent" : s.stroke, n.lineWidth = s.strokeWidth, s.strokeLineDash && n.setLineDash(s.strokeLineDash), s.strokeLineDashOffset && (n.lineDashOffset = s.strokeLineDashOffset), this._drawToContext(n, _a18, o), n.restore();break;case "fillPath":
                {
                  n.save(), n.fillStyle = s.fill || "";var _e25 = "curve" === t.shape || "polygon" === t.shape || "path" === t.shape ? "evenodd" : "nonzero";this._drawToContext(n, _a18, o, _e25), n.restore();break;
                }case "fillSketch":
                this.fillSketch(n, _a18, s);}
          }
        } catch (err) {
          _didIteratorError20 = true;
          _iteratorError20 = err;
        } finally {
          try {
            if (!_iteratorNormalCompletion20 && _iterator20.return) {
              _iterator20.return();
            }
          } finally {
            if (_didIteratorError20) {
              throw _iteratorError20;
            }
          }
        }
      }
    }, {
      key: "fillSketch",
      value: function fillSketch(t, e, s) {
        var n = s.fillWeight;n < 0 && (n = s.strokeWidth / 2), t.save(), s.fillLineDash && t.setLineDash(s.fillLineDash), s.fillLineDashOffset && (t.lineDashOffset = s.fillLineDashOffset), t.strokeStyle = s.fill || "", t.lineWidth = n, this._drawToContext(t, e, s.fixedDecimalPlaceDigits), t.restore();
      }
    }, {
      key: "_drawToContext",
      value: function _drawToContext(t, e, s) {
        var n = arguments.length > 3 && arguments[3] !== undefined ? arguments[3] : "nonzero";
        t.beginPath();var _iteratorNormalCompletion21 = true;
        var _didIteratorError21 = false;
        var _iteratorError21 = undefined;

        try {
          for (var _iterator21 = e.ops[Symbol.iterator](), _step21; !(_iteratorNormalCompletion21 = (_step21 = _iterator21.next()).done); _iteratorNormalCompletion21 = true) {
            var _n12 = _step21.value;
            var _e26 = "number" == typeof s && s >= 0 ? _n12.data.map(function (t) {
              return +t.toFixed(s);
            }) : _n12.data;switch (_n12.op) {case "move":
                t.moveTo(_e26[0], _e26[1]);break;case "bcurveTo":
                t.bezierCurveTo(_e26[0], _e26[1], _e26[2], _e26[3], _e26[4], _e26[5]);break;case "lineTo":
                t.lineTo(_e26[0], _e26[1]);}
          }
        } catch (err) {
          _didIteratorError21 = true;
          _iteratorError21 = err;
        } finally {
          try {
            if (!_iteratorNormalCompletion21 && _iterator21.return) {
              _iterator21.return();
            }
          } finally {
            if (_didIteratorError21) {
              throw _iteratorError21;
            }
          }
        }

        "fillPath" === e.type ? t.fill(n) : t.stroke();
      }
    }, {
      key: "getDefaultOptions",
      value: function getDefaultOptions() {
        return this.gen.defaultOptions;
      }
    }, {
      key: "line",
      value: function line(t, e, s, n, o) {
        var a = this.gen.line(t, e, s, n, o);return this.draw(a), a;
      }
    }, {
      key: "rectangle",
      value: function rectangle(t, e, s, n, o) {
        var a = this.gen.rectangle(t, e, s, n, o);return this.draw(a), a;
      }
    }, {
      key: "ellipse",
      value: function ellipse(t, e, s, n, o) {
        var a = this.gen.ellipse(t, e, s, n, o);return this.draw(a), a;
      }
    }, {
      key: "circle",
      value: function circle(t, e, s, n) {
        var o = this.gen.circle(t, e, s, n);return this.draw(o), o;
      }
    }, {
      key: "linearPath",
      value: function linearPath(t, e) {
        var s = this.gen.linearPath(t, e);return this.draw(s), s;
      }
    }, {
      key: "polygon",
      value: function polygon(t, e) {
        var s = this.gen.polygon(t, e);return this.draw(s), s;
      }
    }, {
      key: "arc",
      value: function arc(t, e, s, n, o, a) {
        var h = arguments.length > 6 && arguments[6] !== undefined ? arguments[6] : !1;
        var r = arguments[7];
        var i = this.gen.arc(t, e, s, n, o, a, h, r);return this.draw(i), i;
      }
    }, {
      key: "curve",
      value: function curve(t, e) {
        var s = this.gen.curve(t, e);return this.draw(s), s;
      }
    }, {
      key: "path",
      value: function path(t, e) {
        var s = this.gen.path(t, e);return this.draw(s), s;
      }
    }, {
      key: "generator",
      get: function get() {
        return this.gen;
      }
    }]);

    return st;
  }();

  var nt = "http://www.w3.org/2000/svg";
  var ot = function () {
    function ot(t, e) {
      _classCallCheck(this, ot);

      this.svg = t, this.gen = new et(e);
    }

    _createClass(ot, [{
      key: "draw",
      value: function draw(t) {
        var e = t.sets || [],
            s = t.options || this.getDefaultOptions(),
            n = this.svg.ownerDocument || window.document,
            o = n.createElementNS(nt, "g"),
            a = t.options.fixedDecimalPlaceDigits;var _iteratorNormalCompletion22 = true;
        var _didIteratorError22 = false;
        var _iteratorError22 = undefined;

        try {
          for (var _iterator22 = e[Symbol.iterator](), _step22; !(_iteratorNormalCompletion22 = (_step22 = _iterator22.next()).done); _iteratorNormalCompletion22 = true) {
            var h = _step22.value;
            var _e27 = null;switch (h.type) {case "path":
                _e27 = n.createElementNS(nt, "path"), _e27.setAttribute("d", this.opsToPath(h, a)), _e27.setAttribute("stroke", s.stroke), _e27.setAttribute("stroke-width", s.strokeWidth + ""), _e27.setAttribute("fill", "none"), s.strokeLineDash && _e27.setAttribute("stroke-dasharray", s.strokeLineDash.join(" ").trim()), s.strokeLineDashOffset && _e27.setAttribute("stroke-dashoffset", "" + s.strokeLineDashOffset);break;case "fillPath":
                _e27 = n.createElementNS(nt, "path"), _e27.setAttribute("d", this.opsToPath(h, a)), _e27.setAttribute("stroke", "none"), _e27.setAttribute("stroke-width", "0"), _e27.setAttribute("fill", s.fill || ""), "curve" !== t.shape && "polygon" !== t.shape || _e27.setAttribute("fill-rule", "evenodd");break;case "fillSketch":
                _e27 = this.fillSketch(n, h, s);}_e27 && o.appendChild(_e27);
          }
        } catch (err) {
          _didIteratorError22 = true;
          _iteratorError22 = err;
        } finally {
          try {
            if (!_iteratorNormalCompletion22 && _iterator22.return) {
              _iterator22.return();
            }
          } finally {
            if (_didIteratorError22) {
              throw _iteratorError22;
            }
          }
        }

        return o;
      }
    }, {
      key: "fillSketch",
      value: function fillSketch(t, e, s) {
        var n = s.fillWeight;n < 0 && (n = s.strokeWidth / 2);var o = t.createElementNS(nt, "path");return o.setAttribute("d", this.opsToPath(e, s.fixedDecimalPlaceDigits)), o.setAttribute("stroke", s.fill || ""), o.setAttribute("stroke-width", n + ""), o.setAttribute("fill", "none"), s.fillLineDash && o.setAttribute("stroke-dasharray", s.fillLineDash.join(" ").trim()), s.fillLineDashOffset && o.setAttribute("stroke-dashoffset", "" + s.fillLineDashOffset), o;
      }
    }, {
      key: "getDefaultOptions",
      value: function getDefaultOptions() {
        return this.gen.defaultOptions;
      }
    }, {
      key: "opsToPath",
      value: function opsToPath(t, e) {
        return this.gen.opsToPath(t, e);
      }
    }, {
      key: "line",
      value: function line(t, e, s, n, o) {
        var a = this.gen.line(t, e, s, n, o);return this.draw(a);
      }
    }, {
      key: "rectangle",
      value: function rectangle(t, e, s, n, o) {
        var a = this.gen.rectangle(t, e, s, n, o);return this.draw(a);
      }
    }, {
      key: "ellipse",
      value: function ellipse(t, e, s, n, o) {
        var a = this.gen.ellipse(t, e, s, n, o);return this.draw(a);
      }
    }, {
      key: "circle",
      value: function circle(t, e, s, n) {
        var o = this.gen.circle(t, e, s, n);return this.draw(o);
      }
    }, {
      key: "linearPath",
      value: function linearPath(t, e) {
        var s = this.gen.linearPath(t, e);return this.draw(s);
      }
    }, {
      key: "polygon",
      value: function polygon(t, e) {
        var s = this.gen.polygon(t, e);return this.draw(s);
      }
    }, {
      key: "arc",
      value: function arc(t, e, s, n, o, a) {
        var h = arguments.length > 6 && arguments[6] !== undefined ? arguments[6] : !1;
        var r = arguments[7];
        var i = this.gen.arc(t, e, s, n, o, a, h, r);return this.draw(i);
      }
    }, {
      key: "curve",
      value: function curve(t, e) {
        var s = this.gen.curve(t, e);return this.draw(s);
      }
    }, {
      key: "path",
      value: function path(t, e) {
        var s = this.gen.path(t, e);return this.draw(s);
      }
    }, {
      key: "generator",
      get: function get() {
        return this.gen;
      }
    }]);

    return ot;
  }();

  return { canvas: function canvas(t, e) {
      return new st(t, e);
    }, svg: function svg(t, e) {
      return new ot(t, e);
    }, generator: function generator(t) {
      return new et(t);
    }, newSeed: function newSeed() {
      return et.newSeed();
    } };
}();
