'use strict';(function(b){"object"==typeof exports&&"object"==typeof module?b(require("../../lib/codemirror")):"function"==typeof define&&define.amd?define(["../../lib/codemirror"],b):b(CodeMirror)})(function(b){b.defineMode("cmake",function(){function b(a,d){for(var c,b,e=!1;!a.eol()&&(c=a.next())!=d.pending;){if("$"===c&&"\\"!=b&&'"'==d.pending){e=!0;break}b=c}e&&a.backUp(1);d.continueString=c==d.pending?!1:!0;return"string"}function f(a,d){var c=a.next();if("$"===c)return a.match(g)?"variable-2":
"variable";if(d.continueString)return a.backUp(1),b(a,d);if(a.match(/(\s+)?\w+\(/)||a.match(/(\s+)?\w+\ \(/))return a.backUp(1),"def";if("#"==c)return a.skipToEnd(),"comment";if("'"==c||'"'==c)return d.pending=c,b(a,d);if("("==c||")"==c)return"bracket";if(c.match(/[0-9]/))return"number";a.eatWhile(/[\w-]/);return null}var g=/({)?[a-zA-Z0-9_]+(})?/;return{startState:function(){return{inDefinition:!1,inInclude:!1,continueString:!1,pending:!1}},token:function(a,b){return a.eatSpace()?null:f(a,b)}}});
b.defineMIME("text/x-cmake","cmake")});
