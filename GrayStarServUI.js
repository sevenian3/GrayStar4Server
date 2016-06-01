/*
 * The openStar project: stellar atmospheres and spectra
 *
 * grayStarServer
 * V3.0, November 2015
 * JQuery version
 * 
 * C. Ian Short
 * Saint Mary's University
 * Department of Astronomy and Physics
 * Institute for Computational Astrophysics (ICA)
 * Halifax, NS, Canada
 *  * ian.short@smu.ca
 * www.ap.smu.ca/~ishort/
 * 
 * Open source pedagogical computational stellar astrophysics
 *
 * 1D, static, plane-parallel, LTE, multi-gray stellar atmospheric model
 * Voigt spectral line profile
 *
 * Suitable for pedagogical purposes only
 * 
 * Logic developed in Java SE 8.0, JDK 1.8
 * 
 * Ported to JavaScript for deployment
 *
 * System requirements for Java version: Java run-time environment (JRE)
 * System requirements for JavaScript version: JavaScript intrepretation enabld in WWW browser (usually by default)
 *
 * Code provided "as is" - there is no formal support 
 *
 * Java default license applies:
 * End users may adapt, modify, develop, debug, and deploy at will
 * for academic and othe non-profit uses, but are asked to leave this
 * header text in place (although they may add to the header text).
 *
 * The MIT License (MIT) 
* Copyright (c) 2016 C. Ian Short 
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */


// **********************************************

"use strict"; //testing only!

// Global variables - Doesn't work - scope not global!

var c = 2.9979249E+10; // light speed in vaccuum in cm/s
var sigma = 5.670373E-5; //Stefan-Boltzmann constant ergs/s/cm^2/K^4  
var wien = 2.8977721E-1; // Wien's displacement law constant in cm K
var k = 1.3806488E-16; // Boltzmann constant in ergs/K
var h = 6.62606957E-27; //Planck's constant in ergs sec
var ee = 4.80320425E-10; //fundamental charge unit in statcoulombs (cgs)
var mE = 9.10938291E-28; //electron mass (g)
var GConst = 6.674e-8; //Newton's gravitational constant (cgs)
//Conversion factors
var amu = 1.66053892E-24; // atomic mass unit in g
var eV = 1.602176565E-12; // eV in ergs
var rSun = 6.955e10; // solar radii to cm
var mSun = 1.9891e33; // solar masses to g
var lSun = 3.846e33; // solar bolometric luminosities to ergs/s
var au = 1.4960e13; // 1 AU in cm

//Methods:
//Natural logs more useful than base 10 logs - Eg. Formal soln module: 
// Fundamental constants
var logC = Math.log(c);
var logSigma = Math.log(sigma);
var logWien = Math.log(wien);
var logK = Math.log(k);
var logH = Math.log(h);
var logEe = Math.log(ee); //Named so won't clash with log_10(e)
var logMe = Math.log(mE);
var logGConst = Math.log(GConst);
//Conversion factors
var logAmu = Math.log(amu);
var logEv = Math.log(eV);
var logRSun = Math.log(rSun);
var logMSun = Math.log(mSun);
var logLSun = Math.log(lSun);
var logAu = Math.log(au);
// ********************************************

//***************************  Main ******************************



function main() {



//**********************************************************


//Routines for handling interaction with PHP through AJAX and JSON:

var gsAjaxParser = function(num, ajaxStr){

//console.log("num " + num);
//convert one string with a comma delimited set of num values into
// an array of numeric data type values

//go through string and recover scalar values:
// does String.split() work??:
var strArray = ajaxStr.split(","); //split on commas
var ajaxLength = strArray.length;


var numArray = [];
numArray.length = num;


if (ajaxLength == num){

   for (var i = 0; i < num; i++){
       //console.log(" " + i + " strArray[i] " + strArray[i]);
       numArray[i] = Number(strArray[i]);
       //console.log(" " + i + " numArray[i] " + numArray[i]);
     }

  } else {

    console.log("!!! BOOM !!!  AJAX returned string with wrong number of elements:");
    console.log("num: " + num + " AJAX array length: " + ajaxLength);
   }

  return numArray;

};

var gsDuplex = function(num, logVector){

 //Takes in single vector of natural log values and return the usual grayStar
// duples array of dimensions (2, num), where row 0 is the linear
// values and row 1 is the corresponding natural logarithmic values

 var duplexArr = [];
 duplexArr.length = 2;
 duplexArr[0] = [];
 duplexArr[1] = [];
 duplexArr[0].length = num;
 duplexArr[1].length = num;

  for (var i = 0; i < num; i++){
    duplexArr[0][i] = Math.exp(logVector[i]);
    duplexArr[1][i] = logVector[i];
    //console.log("i " + i + " duplexArr[0][i] " + duplexArr[0][i] + " duplexArr[1][i] " + duplexArr[1][i]);
  }

 return duplexArr;

};




// ***********************************

// Input control:

    var btnId = document.getElementById("btnId");
    btnId.onClick = function() {
    };
//
    //default initializations:

//JQuery:  Independent of order of switches in HTML file?
// Stellar atmospheric parameters
    var numInputs = 18;
//Make settingsId object array by hand:
// setId() is an object constructor
    function setId(nameIn, valueIn) {
        this.name = nameIn;
        this.value = valueIn;
    }
    //
    // settingId will be an array of objects
    var settingsId = [];
    settingsId.length = numInputs;
    //
    //1st version of each is of JQuery-ui round sliders not available
    //Sigh - IE needs it this way...
    var teffObj = $("#Teff").data("roundSlider");
    var teff = 1.0 * teffObj.getValue();
    var loggObj = $("#logg").data("roundSlider");
    var logg = 1.0 * loggObj.getValue();
    var zScaleObj = $("#zScale").data("roundSlider");
    var logZScale = 1.0 * zScaleObj.getValue();
    var massStarObj = $("#starMass").data("roundSlider");
    var massStar = 1.0 * massStarObj.getValue();
    var xiTObj = $("#xiT").data("roundSlider");
    var xiT = 1.0 * xiTObj.getValue();
// Planetary parameters for habitable zone calculation
    var greenHouseObj = $("#GHTemp").data("roundSlider");
    var greenHouse = 1.0 * greenHouseObj.getValue();
    var albedoObj = $("#Albedo").data("roundSlider");
    var albedo = 1.0 * albedoObj.getValue();

    //var xiT = 1.0 * $("#xi_T").val(); // km/s
    var lineThresh = 1.0 * $("#lineThresh").val(); // 
    //var voigtThresh = 1.0 * $("#voigtThresh").val(); // 
    var voigtThresh = 0;  //for now...
    var lambdaStart = 1.0 * $("#lambdaStart").val(); // nm 
    var lambdaStop = 1.0 * $("#lambdaStop").val(); // nm
    var logGammaCol = 1.0 * $("#gammaCol").val(); // log Lorentzian enhancement 
    var macroV = 1.0 * $("#macroV").val(); // nm
    var rotV = 1.0 * $("#rotV").val(); // nm
    var rotI = 1.0 * $("#rotI").val(); // nm
    //
    //console.log("lineThresh " + lineThresh);
    var diskLambda = 1.0 * $("#diskLam").val(); //nm
    var diskSigma = 1.0 * $("#diskSigma").val(); //nm
    var logKapFudge = 1.0 * $("#logKapFudge").val(); //log_10 cm^2/g mass extinction fudge

//    
    settingsId[0] = new setId("<em>T</em><sub>eff</sub>", teff);
    settingsId[1] = new setId("log <em>g</em>", logg);
    settingsId[2] = new setId("<em>&#954</em>", logZScale);
    settingsId[3] = new setId("<em>M</em>", massStar);

    settingsId[4] = new setId("<span style='color:green'>GHEff</span>", greenHouse);
    settingsId[5] = new setId("<span style='color:green'><em>A</em></span>", albedo);
    settingsId[6] = new setId("<em>&#958</em><sub>T</sub>", xiT);
    settingsId[7] = new setId("&#955</em><sub>Filter</sub>", diskLambda);
    settingsId[8] = new setId("Min log<sub>10</sub><em>&#954</em><sub>l</sub>/<em>&#954</em><sub>c</sub>", lineThresh);
    //settingsId[9] = new setId("Min log<sub>10</sub><em>&#954</em><sub>l</sub>/<em>&#954</em><sub>c, Voigt</sub>", voigtThresh);
    settingsId[9] = new setId(" ", voigtThresh);
    settingsId[10] = new setId("<em>&#955</em><sub>1</sub>", lambdaStart);
    settingsId[11] = new setId("<em>&#955</em><sub>2</sub>", lambdaStop);
    settingsId[12] = new setId("<em>&#947</em><sub>Col</sub>", logGammaCol);
    settingsId[13] = new setId("<em>v</em><sub>Macro</sub>", macroV);
    settingsId[14] = new setId("<em>v</em><sub>Rot</sub>", rotV);
    settingsId[15] = new setId("<em>i</em><sub>Rot</sub>", rotI);
    settingsId[16] = new setId("&#963<sub>Filter</sub>", diskSigma);
    settingsId[17] = new setId("&#954<sub>Fudge</sub>", logKapFudge);

    //
    var numPerfModes = 8;
    var switchPerf = "Fast"; //default initialization
    //JQuery:
    //Fast and Real modes are raio switches - mutuallye xclusive:
// Fast: (default)
    if ($("#fastmode").is(":checked")) {
        switchPerf = $("#fastmode").val(); // radio 
    }
//Real:
    if ($("#realmode").is(":checked")) {
        switchPerf = $("#realmode").val(); // radio 
    }
//User select:
    if ($("#usermode").is(":checked")) {
        switchPerf = $("#usermode").val(); // radio 
    }
    //console.log("switchPerf " + switchPerf);
//
//default initializations:
    var ifTcorr = false;
    var ifConvec = false;
    var ifVoigt = false;
    var ifScatt = false;
    //
    var ifShowAtmos = false;
    var ifShowRad = false;
    var ifShowLine = false;
    var ifShowLogNums = false;
    //
    var ifPrintNone = true;
    var ifPrintAtmos = false;
    var ifPrintSED = false;
    var ifPrintIntens = false;
    var ifPrintLine = false;
    var ifPrintLDC = false;
    var ifPrintAbnd = false;
    var ifPrintLogNums = false;
    var ifPrintJSON = false;
    //
    //

//
//Over-rides:

    if (switchPerf === "Fast") {
        ifTcorr = false;
        $("#tcorr").removeAttr("checked");
        ifConvec = false;
        $("#convec").removeAttr("checked");
        ifVoigt = false;
        $("#voigt").removeAttr("checked");
        ifScatt = false;
        $("#scatter").removeAttr("checked");
    }
    if (switchPerf === "Real") {
        ifTcorr = true;
        $("#tcorr").attr("checked", ":checked");
        ifConvec = false;
        ifVoigt = true;
        $("#voigt").attr("checked", ":checked");
        ifScatt = true;
        $("#scatter").attr("checked", ":checked");
    }
    if (switchPerf === "User") {
//
// Individual modules are checkboxes:
//TCorr:
        if ($("#tcorr").is(":checked")) {
            ifTcorr = true; // checkbox 
        }
//Convec:
        if ($("#convec").is(":checked")) {
            ifConvec = true; // checkbox
        }
//Voigt:
        if ($("#voigt").is(":checked")) {
            ifVoigt = true; // checkbox
        }
//Line scattering:
        if ($("#scatter").is(":checked")) {
            ifScatt = true; // checkbox
        }
    }

    // Display options:
    if ($("#showAtmos").is(":checked")) {
        ifShowAtmos = true; // checkbox
    }
    if ($("#showRad").is(":checked")) {
        ifShowRad = true; // checkbox
    }
    if ($("#showLine").is(":checked")) {
        ifShowLine = true; // checkbox
    }
    //if ($("#showLogNums").is(":checked")) {
    //    ifShowLogNums = true; // checkbox
    // }
    var ionEqElement = "None"; //default
    ionEqElement = $("#showLogNums").val();
    if (ionEqElement != "None") {
        ifShowLogNums = true; // checkbox
    }
//console.log("ionEqElement " + ionEqElement);
    //Detailed print-out options:
    if ($("#printNone").is(":checked")) {
        ifPrintNone = true; // checkbox
    }
    if ($("#printAtmos").is(":checked")) {
        ifPrintAtmos = true; // checkbox
    }
    if ($("#printSED").is(":checked")) {
        ifPrintSED = true; // checkbox
    }
    if ($("#printIntens").is(":checked")) {
        ifPrintIntens = true; // checkbox
    }
    if ($("#printLine").is(":checked")) {
        ifPrintLine = true; // checkbox
    }
    if ($("#printLDC").is(":checked")) {
        ifPrintLDC = true; // checkbox
    }
    if ($("#printAbnd").is(":checked")) {
        ifPrintAbnd = true; // checkbox
    }
    if ($("#printLogNums").is(":checked")) {
        ifPrintLogNums = true; // checkbox
    }
    if ($("#printJSON").is(":checked")) {
        ifPrintJSON = true; // checkbox
    }

  //Spectrum synthesis line sampling options:
    var switchSampl = "fine"; //default initialization
// Coarse sampling: (default)
    if ($("#coarse").is(":checked")) {
        switchSampl = $("#coarse").val(); // radio 
    }
// Fine sampling: (default)
    if ($("#fine").is(":checked")) {
        switchSampl = $("#fine").val(); // radio 
    }
   //console.log("line sampling " + switchSampl);

    //       


    var switchStar = "None";
    var numPreStars = 7;
    //JQuery:
    // None: (default)
    if ($("#none").is(":checked")) {
        switchStar = $("#none").val(); // radio 
    }

// Sun
    if ($("#sun").is(":checked")) {
        switchStar = $("#sun").val(); // radio 
    }
// Vega
    if ($("#vega").is(":checked")) {
        switchStar = $("#vega").val(); // radio 
    }
// Arcturus
    if ($("#arcturus").is(":checked")) {
        switchStar = $("#arcturus").val(); // radio 
    }

// Procyon
    if ($("#procyon").is(":checked")) {
        switchStar = $("#procyon").val(); // radio 
    }

// Regulus
    if ($("#regulus").is(":checked")) {
        switchStar = $("#regulus").val(); // radio 
    }

// 61 Cygni A
    if ($("#61cygnia").is(":checked")) {
        switchStar = $("#61cygnia").val(); // radio 
    }

// 51 Pegasi
    if ($("#51pegasi").is(":checked")) {
        switchStar = $("#51pegasi").val(); // radio 
    }


//JQuery:
    if (switchStar === "Sun") {
        var teff = 5780.0;
        settingsId[0].value = 5780.0;
        //First version is if there's no JQuery-UI round sliders
        //$("#Teff").val(5780.0);
        $("#Teff").roundSlider("setValue", "5780.0");
        var logg = 4.4;
        settingsId[1].value = 4.4;
        //$("#logg").val(4.4);
        $("#logg").roundSlider("setValue", "4.4");
        var logZScale = 0.0;
        settingsId[2].value = 0.0;
        //$("#zScale").val(0.0);
        $("#zScale").roundSlider("setValue", "0.0");
        var massStar = 1.0;
        settingsId[3].value = 1.0;
        //$("#starMass").val(1.0);
        $("#massStar").roundSlider("setValue", "1.0");
        var xiT = 1.0;
        settingsId[6].value = 1.0;
        $("#xiT").roundSlider("setValue", "1.0");
        var macroV = 2.0;
        settingsId[13].value = 2.0;
        $("#macroV").val(2.0);
        var rotV = 2.0;
        settingsId[14].value = 2.0;
        $("#rotV").val(2.0);
        var rotI = 90.0;
        settingsId[15].value = 90.0;
        $("#rotI").val(90.0);
//For late-type stars, make default synthsis region NaID 
        var lambdaStart = 587.0;
        settingsId[10].value = 587.0;
        $("#lambdaStart").val(587.0);
        var lambdaStop = 592.0;
        settingsId[11].value = 592.0;
        $("#lambdaStop").val(592.0);
        var logKapFudge = 0.0;
        settingsId[17].value = 0.0;
        $("#logKapFudge").val(0.0);
    }

    if (switchStar === "Arcturus") {
        var teff = 4250.0;
        settingsId[0].value = 4250.0;
        //$("#Teff").val(4250.0);
        $("#Teff").roundSlider("setValue", "4250.0");
        var logg = 2.0;
        settingsId[1].value = 2.0;
        //$("#logg").val(2.0);
        $("#logg").roundSlider("setValue", "2.0");
        var logZScale = -0.5;
        settingsId[2].value = -0.5;
        //$("#zScale").val(-0.5);
        $("#zScale").roundSlider("setValue", "-0.5");
        var massStar = 1.1;
        settingsId[3].value = 1.1;
        //$("#starMass").val(1.1);
        $("#massStar").roundSlider("setValue", "1.1");
        var xiT = 2.0;
        settingsId[6].value = 2.0;
        $("#xiT").roundSlider("setValue", "2.0");
        var macroV = 4.0;
        settingsId[13].value = 4.0;
        $("#macroV").val(4.0);
        var rotV = 2.0;
        settingsId[14].value = 2.0;
        $("#rotV").val(2.0);
        var rotI = 90.0;
        settingsId[15].value = 90.0;
        $("#rotI").val(90.0);
//For late-type stars, make default synthsis region NaID 
        var lambdaStart = 587.0;
        settingsId[10].value = 587.0;
        $("#lambdaStart").val(587.0);
        var lambdaStop = 592.0;
        settingsId[11].value = 592.0;
        $("#lambdaStop").val(592.0);
        var logKapFudge = 0.0;
        settingsId[17].value = 0.0;
        $("#logKapFudge").val(0.0);
    }

    if (switchStar === "Vega") {
        var teff = 9550.0;
        settingsId[0].value = 9550.0;
        //$("#Teff").val(9550.0);
        $("#Teff").roundSlider("setValue", "9550.0");
        var logg = 3.95;
        settingsId[1].value = 3.95;
        //$("#logg").val(3.95);
        $("#logg").roundSlider("setValue", "3.95");
        var logZScale = -0.5;
        settingsId[2].value = -0.5;
        //$("#zScale").val(-0.5);
        $("#zScale").roundSlider("setValue", "-0.5");
        var massStar = 2.1;
        settingsId[3].value = 2.1;
        //$("#starMass").val(2.1);
        $("#massStar").roundSlider("setValue", "2.1");
        var xiT = 2.0;
        settingsId[6].value = 2.0;
        $("#xiT").roundSlider("setValue", "2.0");
        var macroV = 2.0;
        settingsId[13].value = 2.0;
        $("#macroV").val(2.0);
        var rotV = 200.0;
        settingsId[14].value = 200.0;
        $("#rotV").val(200.0);
        var rotI = 5.0;
        settingsId[15].value = 5.0;
        $("#rotI").val(5.0);
//For rapidly rotating early stars, make default synthsis region Halpha
        var lambdaStart = 653.0;
        settingsId[10].value = 653.0;
        $("#lambdaStart").val(653.0);
        var lambdaStop = 659.0;
        settingsId[11].value = 659.0;
        $("#lambdaStop").val(659.0);
        var logKapFudge = 0.0;
        settingsId[17].value = 0.0;
        $("#logKapFudge").val(0.0);
    }

    if (switchStar === "Regulus") {
        var teff = 12460.0;
        settingsId[0].value = 12460.0;
        //$("#Teff").val(12460.0);
        $("#Teff").roundSlider("setValue", "12460.0");
        var logg = 3.5;
        settingsId[1].value = 3.5;
        //$("#logg").val(3.54);
        $("#logg").roundSlider("setValue", "3.5");
        var logZScale = 0.0;
        settingsId[2].value = 0.0;
        //$("#zScale").val(0.0);
        $("#zScale").roundSlider("setValue", "0.0");
        var massStar = 3.8;
        settingsId[3].value = 3.8;
        //$("#starMass").val(3.8);
        $("#massStar").roundSlider("setValue", "3.8");
        var xiT = 1.0;
        settingsId[6].value = 1.0;
        $("#xiT").roundSlider("setValue", "1.0");
        var macroV = 2.0;
        settingsId[13].value = 2.0;
        $("#macroV").val(2.0);
        var rotV = 350.0;
        settingsId[14].value = 350.0;
        $("#rotV").val(350.0);
        var rotI = 90.0;
        settingsId[15].value = 90.0;
        $("#rotI").val(90.0);
//For rapidly rotating early stars, make default synthsis region Halpha
        var lambdaStart = 653.0;
        settingsId[10].value = 653.0;
        $("#lambdaStart").val(653.0);
        var lambdaStop = 659.0;
        settingsId[11].value = 659.0;
        $("#lambdaStop").val(659.0);
        var logKapFudge = 0.0;
        settingsId[17].value = 0.0;
        $("#logKapFudge").val(0.0);
    }

    if (switchStar === "Procyon") {
        var teff = 6530.0;
        settingsId[0].value = 6530.0;
        //$("#Teff").val(6530.0);
        $("#Teff").roundSlider("setValue", "6530.0");
        var logg = 4.0;
        settingsId[1].value = 4.0;
        //$("#logg").val(4.0);
        $("#logg").roundSlider("setValue", "4.0");
        var logZScale = 0.0;
        settingsId[2].value = 0.0;
        //$("#zScale").val(0.0);
        $("#zScale").roundSlider("setValue", "0.0");
        var massStar = 1.4;
        settingsId[3].value = 1.4;
        //$("#starMass").val(1.4);
        $("#massStar").roundSlider("setValue", "1.4");
        var xiT = 1.0;
        settingsId[6].value = 1.0;
        $("#xiT").roundSlider("setValue", "1.0");
        var macroV = 2.0;
        settingsId[13].value = 2.0;
        $("#macroV").val(2.0);
        var rotV = 3.5;
        settingsId[14].value = 3.5;
        $("#rotV").val(3.5);
        var rotI = 90.0;
        settingsId[15].value = 90.0;
        $("#rotI").val(90.0);
//For late-type stars, make default synthsis region NaID 
        var lambdaStart = 587.0;
        settingsId[10].value = 587.0;
        $("#lambdaStart").val(587.0);
        var lambdaStop = 592.0;
        settingsId[11].value = 592.0;
        $("#lambdaStop").val(592.0);
        var logKapFudge = 0.0;
        settingsId[17].value = 0.0;
        $("#logKapFudge").val(0.0);
    }

    if (switchStar === "61CygniA") {
        var teff = 4525.0;
        settingsId[0].value = 4525.0;
        //$("#Teff").val(4526.0);
        $("#Teff").roundSlider("setValue", "4525.0");
        var logg = 4.2;
        settingsId[1].value = 4.2;
        //$("#logg").val(4.2);
        $("#logg").roundSlider("setValue", "4.2");
        var logZScale = 0.0;
        settingsId[2].value = 0.0;
        //$("#zScale").val(0.0);
        $("#zScale").roundSlider("setValue", "0.0");
        var massStar = 0.6;
        settingsId[3].value = 0.6;
        //$("#starMass").val(0.63);
        $("#massStar").roundSlider("setValue", "0.6");
        var xiT = 1.0;
        settingsId[6].value = 1.0;
        $("#xiT").roundSlider("setValue", "1.0");
        var macroV = 2.0;
        settingsId[13].value = 2.0;
        $("#macroV").val(2.0);
        var rotV = 2.0;
        settingsId[14].value = 2.0;
        $("#rotV").val(2.0);
        var rotI = 90.0;
        settingsId[15].value = 90.0;
        $("#rotI").val(90.0);
//For late-type stars, make default synthsis region NaID 
        var lambdaStart = 587.0;
        settingsId[10].value = 587.0;
        $("#lambdaStart").val(587.0);
        var lambdaStop = 592.0;
        settingsId[11].value = 592.0;
        $("#lambdaStop").val(592.0);
        var logKapFudge = 0.0;
        settingsId[17].value = 0.0;
        $("#logKapFudge").val(0.0);
    }

    if (switchStar === "51Pegasi") {
        var teff = 5570.0;
        settingsId[0].value = 5570.0;
        //$("#Teff").val(5570.0);
        $("#Teff").roundSlider("setValue", "5570.0");
        var logg = 4.3;
        settingsId[1].value = 4.3;
        //$("#logg").val(4.33);
        $("#logg").roundSlider("setValue", "4.3");
        var logZScale = 0.0;
        settingsId[2].value = 0.0;
        //$("#zScale").val(0.0);
        $("#zScale").roundSlider("setValue", "0.0");
        var massStar = 1.1;
        settingsId[3].value = 1.1;
        //$("#starMass").val(1.11);
        $("#massStar").roundSlider("setValue", "1.1");
        var xiT = 1.0;
        settingsId[6].value = 1.0;
        $("#xiT").roundSlider("setValue", "1.0");
        var macroV = 2.0;
        settingsId[13].value = 2.0;
        $("#macroV").val(2.0);
        var rotV = 2.0;
        settingsId[14].value = 2.0;
        $("#rotV").val(2.0);
        var rotI = 90.0;
        settingsId[15].value = 90.0;
        $("#rotI").val(90.0);
//For late-type stars, make default synthsis region NaID 
        var lambdaStart = 587.0;
        settingsId[10].value = 587.0;
        $("#lambdaStart").val(587.0);
        var lambdaStop = 592.0;
        settingsId[11].value = 592.0;
        $("#lambdaStop").val(592.0);
        var logKapFudge = 0.0;
        settingsId[17].value = 0.0;
        $("#logKapFudge").val(0.0);
    }

    var switchPlanet = "None";
    var numPrePlanets = 1;
    //JQuery:
    // None: (default)
    if ($("#noneplanet").is(":checked")) {
        switchPlanet = $("#noneplanet").val(); // radio 
    }

// Earth
    if ($("#earth").is(":checked")) {
        switchPlanet = $("#earth").val(); // radio 
    }

    if (switchPlanet === "Earth") {
        var GHTemp = 20.0;
        settingsId[4].value = 20.0;
        //$("#GHTemp").val(20.0);
        $("#GHTemp").roundSlider("setValue", "20.0");
        var Albedo = 0.3;
        settingsId[5].value = 0.3;
        //$("#Albedo").val(0.3);
        $("#Albedo").roundSlider("setValue", "0.3");
    }


    var switchLine = "None";
    var numPreLines = 10;


    // None: (default)
    //JQuery:
    if ($("#noneline").is(":checked")) {
        switchLine = $("#noneline").val(); // radio 
    }
// NaI D_1
    if ($("#NaID1").is(":checked")) {
        switchLine = $("#NaID1").val(); // radio        
    }
// NaI D_2
    if ($("#NaID2").is(":checked")) {
        switchLine = $("#NaID2").val(); // radio 
    }
// MgI b_1
    if ($("#MgIb1").is(":checked")) {
        switchLine = $("#MgIb1").val(); // radio 
    }
// CaII K
    if ($("#CaIIK").is(":checked")) {
        switchLine = $("#CaIIK").val(); // radio        
    }
// CaII H
    if ($("#CaIIH").is(":checked")) {
        switchLine = $("#CaIIH").val(); // radio        
    }
// CaI 4227
    if ($("#CaI4227").is(":checked")) {
        switchLine = $("#CaI4227").val(); // radio        
    }
// FeI 4271 
    if ($("#FeI4271").is(":checked")) {
        switchLine = $("#FeI4271").val(); // radio        
    }
// FeI 4045 
    if ($("#FeI4045").is(":checked")) {
        switchLine = $("#FeI4045").val(); // radio        
    }
// HeI 4471
    if ($("#HeI4471").is(":checked")) {
        switchLine = $("#HeI4471").val(); // radio        
    }
// HeI 4387
    if ($("#HeI4387").is(":checked")) {
        switchLine = $("#HeI4387").val(); // radio        
    }

//JQuery:
    if (switchLine === "NaID1") {
        ifShowLine = true; // checkbox
    var switchSampl = "fine"; //default initialization
        var lam0 = 589.592;
        var lambdaStart = lam0 - 2.0
        var lambdaStop = lam0 + 2.0
        var lineThresh = -3.0;
        settingsId[10].value = lambdaStart;
        settingsId[11].value = lambdaStop;
        settingsId[8].value = -3.0; //lineThresh
        settingsId[9].value = -3.0; //voigtThresh
        $("#lambdaStart").val(lambdaStart);
        $("#lambdaStop").val(lambdaStop);
        $("#lineThresh").val(lineThresh);
        $("#voigtThresh").val(voigtThresh);
    }

    if (switchLine === "NaID2") {
        ifShowLine = true; // checkbox
    var switchSampl = "fine"; //default initialization
        var lam0 = 588.995;
        var lambdaStart = lam0 - 2.0
        var lambdaStop = lam0 + 2.0
        var lineThresh = -3.0;
        settingsId[10].value = lambdaStart;
        settingsId[11].value = lambdaStop;
        settingsId[8].value = -3.0; //lineThresh
        settingsId[9].value = -3.0; //voigtThresh
        $("#lambdaStart").val(lambdaStart);
        $("#lambdaStop").val(lambdaStop);
        $("#lineThresh").val(lineThresh);
        $("#voigtThresh").val(voigtThresh);
    }

    if (switchLine === "MgIb1") {
        ifShowLine = true; // checkbox
    var switchSampl = "fine"; //default initialization
        var lam0 = 518.360;
        var lambdaStart = lam0 - 1.0
        var lambdaStop = lam0 + 1.0
        var lineThresh = -3.0;
        settingsId[10].value = lambdaStart;
        settingsId[11].value = lambdaStop;
        settingsId[8].value = -3.0; //lineThresh
        settingsId[9].value = -3.0; //voigtThresh
        $("#lambdaStart").val(lambdaStart);
        $("#lambdaStop").val(lambdaStop);
        $("#lineThresh").val(lineThresh);
        $("#voigtThresh").val(voigtThresh);
    }

    if (switchLine === "CaIIK") {
        ifShowLine = true; // checkbox
    var switchSampl = "fine"; //default initialization
        var lam0 = 393.366;
        var lambdaStart = lam0 - 3.0
        var lambdaStop = lam0 + 3.0
        var lineThresh = -3.0;
        settingsId[10].value = lambdaStart;
        settingsId[11].value = lambdaStop;
        settingsId[8].value = -3.0; //lineThresh
        settingsId[9].value = -3.0; //voigtThresh
        $("#lambdaStart").val(lambdaStart);
        $("#lambdaStop").val(lambdaStop);
        $("#lineThresh").val(lineThresh);
        $("#voigtThresh").val(voigtThresh);
    }

    if (switchLine === "CaIIH") {
        ifShowLine = true; // checkbox
    var switchSampl = "fine"; //default initialization
        var lam0 = 396.847;
        var lambdaStart = lam0 - 3.0
        var lambdaStop = lam0 + 3.0
        var lineThresh = -3.0;
        settingsId[10].value = lambdaStart;
        settingsId[11].value = lambdaStop;
        settingsId[8].value = -3.0; //lineThresh
        settingsId[9].value = -3.0; //voigtThresh
        $("#lambdaStart").val(lambdaStart);
        $("#lambdaStop").val(lambdaStop);
        $("#lineThresh").val(lineThresh);
        $("#voigtThresh").val(voigtThresh);
    }

    if (switchLine === "CaI4227") {
        ifShowLine = true; // checkbox
    var switchSampl = "fine"; //default initialization
        var lam0 = 422.673;
        var lambdaStart = lam0 - 2.0
        var lambdaStop = lam0 + 2.0
        var lineThresh = -3.0;
        settingsId[10].value = lambdaStart;
        settingsId[11].value = lambdaStop;
        settingsId[8].value = -3.0; //lineThresh
        settingsId[9].value = -3.0; //voigtThresh
        $("#lambdaStart").val(lambdaStart);
        $("#lambdaStop").val(lambdaStop);
        $("#lineThresh").val(lineThresh);
        $("#voigtThresh").val(voigtThresh);
    }

    if (switchLine === "FeI4045") {
        ifShowLine = true; // checkbox
    var switchSampl = "fine"; //default initialization
        var lam0 = 404.581;
        var lambdaStart = lam0 - 1.0
        var lambdaStop = lam0 + 1.0
        var lineThresh = -3.0;
        settingsId[10].value = lambdaStart;
        settingsId[11].value = lambdaStop;
        settingsId[8].value = -3.0; //lineThresh
        settingsId[9].value = -3.0; //voigtThresh
        $("#lambdaStart").val(lambdaStart);
        $("#lambdaStop").val(lambdaStop);
        $("#lineThresh").val(lineThresh);
        $("#voigtThresh").val(voigtThresh);
    }

    if (switchLine === "FeI4271") {
        ifShowLine = true; // checkbox
    var switchSampl = "fine"; //default initialization
        var lam0 = 427.176;
        var lambdaStart = lam0 - 1.0
        var lambdaStop = lam0 + 1.0
        var lineThresh = -3.0;
        settingsId[10].value = lambdaStart;
        settingsId[11].value = lambdaStop;
        settingsId[8].value = -3.0; //lineThresh
        settingsId[9].value = -3.0; //voigtThresh
        $("#lambdaStart").val(lambdaStart);
        $("#lambdaStop").val(lambdaStop);
        $("#lineThresh").val(lineThresh);
        $("#voigtThresh").val(voigtThresh);
    }

    if (switchLine === "HeI4387") {
        ifShowLine = true; // checkbox
    var switchSampl = "fine"; //default initialization
        var lam0 = 438.793;
        var lambdaStart = lam0 - 0.5
        var lambdaStop = lam0 + 0.5
        var lineThresh = -5.0;
        settingsId[10].value = lambdaStart;
        settingsId[11].value = lambdaStop;
        settingsId[8].value = -5.0; //lineThresh
        settingsId[9].value = -3.0; //voigtThresh
        $("#lambdaStart").val(lambdaStart);
        $("#lambdaStop").val(lambdaStop);
        $("#lineThresh").val(lineThresh);
        $("#voigtThresh").val(voigtThresh);
    }

    if (switchLine === "HeI4471") {
        ifShowLine = true; // checkbox
    var switchSampl = "fine"; //default initialization
        var lam0 = 447.147;
        var lambdaStart = lam0 - 0.5
        var lambdaStop = lam0 + 0.5
        var lineThresh = -5.0;
        settingsId[10].value = lambdaStart;
        settingsId[11].value = lambdaStop;
        settingsId[8].value = -5.0; //lineThresh
        settingsId[9].value = -3.0; //voigtThresh
        $("#lambdaStart").val(lambdaStart);
        $("#lambdaStop").val(lambdaStop);
        $("#lineThresh").val(lineThresh);
        $("#voigtThresh").val(voigtThresh);
    }



    //
    // Form validation and Initial sanity checks:
    // 

// Stellar parameters:
//
    var flagArr = [];
    flagArr.length = numInputs;
    flagArr[0] = false;
//
    var F0Vtemp = 7300.0;  // Teff of F0 V star (K)
    var minTeff = 500.0;
    var maxTeff = 50000.0;
    if (teff === null || teff == "") {
        alert("Teff must be filled out");
        return;
    }
    if (teff < minTeff) {
        flagArr[0] = true;
        teff = minTeff;
        var teffStr = String(minTeff);
        settingsId[0].value = minTeff;
        //first version is if there's no JQuery-UI
        //$("#Teff").val(minTeff);
        $("#Teff").roundSlider("setValue", minTeff);
    }
    if (teff > maxTeff) {
        flagArr[0] = true;
        teff = maxTeff;
        var teffStr = String(maxTeff);
        settingsId[0].value = maxTeff;
        //$("#Teff").val(maxTeff);
        $("#Teff").roundSlider("setValue", maxTeff);
    }
//logg limit is strongly Teff-dependent:
    if (logg === null || logg === "") {
        alert("log(g) must be filled out");
        return;
    }
    var minLogg = 3.0; //safe initialization
    var minLoggStr = "3.0";
    if (teff <= 4000.0) {
        minLogg = 0.0;
        minLoggStr = "0.0";
    } else if ((teff > 4000.0) && (teff <= 5000.0)) {
        minLogg = 1.0;
        minLoggStr = "1.0";
    } else if ((teff > 5000.0) && (teff <= 6000.0)) {
        minLogg = 1.5;
        minLoggStr = "1.5";
    } else if ((teff > 6000.0) && (teff <= 7000.0)) {
        minLogg = 2.0;
        minLoggStr = "2.0";
    } else if ((teff > 7000.0) && (teff < 9000.0)) {
        minLogg = 2.5;
        minLoggStr = "2.5";
    } else if (teff >= 9000.0) {
        minLogg = 3.0;
        minLoggStr = "3.0";
    }
    flagArr[1] = false;
    if (logg < minLogg) {
        flagArr[1] = true;
        logg = minLogg;
        var loggStr = minLoggStr;
        settingsId[1].value = minLogg;
        //$("#logg").val(minLogg);
        $("#logg").roundSlider("setValue", minLogg);
    }
    if (logg > 7.0) {
        flagArr[1] = true;
        logg = 7.0;
        var loggStr = "7.0";
        settingsId[1].value = 7.0;
        //$("#logg").val(5.5);
        $("#logg").roundSlider("setValue", 7.0);
    }
    if (logZScale === null || logZScale === "") {
        alert("logZScale must be filled out");
        return;
    }
    flagArr[2] = false;
    if (logZScale < -3.0) {
        flagArr[2] = true;
        logZScale = -3.0;
        var logKappaStr = "-3.0";
        settingsId[2].value = -3.0;
        //$("#zScale").val(-2.0);
        $("#zScale").roundSlider("setValue", -3.0);
    }
    if (logZScale > 1.0) {
        flagArr[2] = true;
        logZScale = 1.0;
        var kappaStr = "1.0";
        settingsId[2].value = 1.0;
        //$("#zScale").val(0.5);
        $("#zScale").roundSlider("setValue", 1.0);
    }
    if (massStar === null || massStar == "") {
        alert("mass must be filled out");
        return;
    }
    flagArr[3] = false;
    if (massStar < 0.1) {
        flagArr[3] = true;
        massStar = 0.1;
        var massStarStr = "0.1";
        settingsId[3].value = 0.1;
        //$("#starMass").val(0.1);
        $("#starMass").roundSlider("setValue", 0.1);
    }
    if (massStar > 20.0) {
        flagArr[3] = true;
        massStar = 20.0;
        var massStarStr = "20.0";
        settingsId[3].value = 20.0;
        //$("#starMass").val(8.0);
        $("#starMass").roundSlider("setValue", 20.0);
    }

    // Planetary parameters for habitable zone calculation:
    //
    if (greenHouse === null || greenHouse === "") {
        alert("greenHouse must be filled out");
        return;
    }
    flagArr[4] = false;
    if (greenHouse < 0.0) {
        flagArr[4] = true;
        greenHouse = 0.0;
        var greenHouseStr = "0.0";
        settingsId[4].value = 0.0;
        //$("#GHTemp").val(0.0);
        $("#GHTemp").roundSlider("setValue", 0.0);
    }
    if (greenHouse > 200.0) {
        flagArr[4] = true;
        greenHouse = 200.0;
        var greenHouseStr = "200.0";
        settingsId[4].value = 200.0;
        //$("#GHTemp").val(200.0);
        $("#GHTemp").roundSlider("setValue", 200.0);
    }
    if (albedo === null || albedo === "") {
        alert("albedo must be filled out");
        return;
    }
    flagArr[5] = false;
    if (albedo < 0.0) {
        flagArr[5] = true;
        albedo = 0.0;
        var albedoStr = "0.0";
        settingsId[5].value = 0.0;
        //$("#Albedo").val(0.0);
        $("#Albedo").roundSlider("setValue", 0.0);
    }
    if (albedo > 1.0) {
        flagArr[5] = true;
        greenHouse = 1.0;
        var albedoStr = "1.0";
        settingsId[5].value = 1.0;
        //$("#Albedo").val(1.0);
        $("#Albedo").roundSlider("setValue", 1.0);
    }


    if (xiT === null || xiT == "") {
        alert("xiT must be filled out");
        return;
    }
    flagArr[6] = false;
    if (xiT < 0.0) {
        flagArr[6] = true;
        xiT = 0.0;
        var xitStr = "0.0";
        settingsId[6].value = 0.0;
        $("#xi_T").val(0.0);
    }
    if (xiT > 8.0) {
        flagArr[6] = true;
        xiT = 8.0;
        var xitStr = "8.0";
        settingsId[6].value = 8.0;
        $("#xi_T").val(8.0);
    }
 
    if (lineThresh === null || lineThresh === "") {
        alert("lineThresh must be filled out");
        return;
    }
    flagArr[7] = false;
    if (lineThresh < -4.0) {
        flagArr[7] = true;
        lineThresh = -4.0;
        var lineThreshtStr = "-4.0";
        settingsId[7].value = -4.0;
        $("#lineThresh").val(-4.0);
    }
    if (lineThresh > 6.0) {
        flagArr[7] = true;
        lineThresh = 6.0;
        var lineThreshStr = "6.0";
        settingsId[7].value = 6.0;
        $("#lineThresh").val(6.0);
    }
    if (voigtThresh === null || voigtThresh === "") {
        alert("voigtThresh must be filled out");
        return;
    }
    flagArr[8] = false;
    if (voigtThresh < -30.0) {
        flagArr[8] = true;
        voigtThresh = -30.0;
        var voigtThreshStr = "-30.0";
        settingsId[8].value = -30.0;
        $("#voigtThresh").val(-30.0);
    }
    if (voigtThresh > 30.0) {
        flagArr[8] = true;
        voigtThresh = 30.0;
        var voigtThreshStr = "30.0";
        settingsId[8].value = 30.0;
        $("#voigtThresh").val(30.0);
    }

    var lamUV = 360.0;
    var lamIR = 900.0;
 
    if (lambdaStart === null || lambdaStart == "") {
        alert("lambdaStart must be filled out");
        return;
    }
    flagArr[9] = false;
    if (lambdaStart < lamUV) {
        flagArr[9] = true;
        lambdaStart = lamUV;
        var lambdaStartStr = String(lamUV);
        settingsId[9].value = lamUV;
        $("#lambdaStart").val(lamUV);
    }
    if (lambdaStart > lamIR - 1.0) {
        flagArr[9] = true;
        lambdaStart = lamIR - 1.0;
        var lambdaStartStr = String(lamIR - 1.0);
        settingsId[9].value = lamIR - 1.0;
        $("#lambdaStart").val(lamIR - 1.0);
    }
 
    if (lambdaStop === null || lambdaStop == "") {
        alert("lambdaStop must be filled out");
        return;
    }
    flagArr[10] = false;
    if (lambdaStop < lamUV + 1.0) {
        flagArr[10] = true;
        lambdaStop = lamUV + 1.0;
        var lambdaStopStr = String(lamUV + 1.0);
        settingsId[10].value = lamUV + 1.0;
        $("#lambdaStop").val(lamUV + 1.0);
    }
//Prevent negative or zero lambda range:
    if (lambdaStop <= lambdaStart) {
        flagArr[10] = true;
        lambdaStop = lamStart + 0.5; //0.5 nm = 5 A
        var lambdaStopStr = String(lambdaStop);
        settingsId[10].value = lambdaStop;
        $("#lambdaStop").val(lambdaStop);
    }

//limit size of synthesis region:
   var maxSynthRange = 10.0; //set default to minimum value
  //if we're not in the blue we can get away wth more:
   if (lambdaStart > 550.0){
      maxSynthRange = 20.0;
   }
   if (lambdaStart > 700.0){
      maxSynthRange = 50.0;
   }
    if (lambdaStop > (lambdaStart+maxSynthRange)) {
        flagArr[10] = true;
        lambdaStop = lamStart + maxSynthRange; //10 nm = 100 A
        var lambdaStopStr = String(lambdaStop);
        settingsId[10].value = lambdaStop;
        $("#lambdaStop").val(lamIR);
    }
    if (lambdaStop > lamIR) {
        flagArr[10] = true;
        lambdaStop = lamIR;
        var lambdaStopStr = String(lamIR);
        settingsId[10].value = lamIR;
        $("#lambdaStop").val(lamIR);
    }

    if (diskLambda === null || diskLambda == "") {
        alert("filter wavelength must be filled out");
        return;
    }
    flagArr[11] = false;
    if (diskLambda < lamUV) {
        flagArr[11] = true;
        diskLambda = lamUV;
        var diskLambdaStr = lamUV.toString(10);
        settingsId[11].value = lamUV;
        $("#diskLam").val(lamUV);
    }
    if (diskLambda > lamIR) {
        flagArr[11] = true;
        diskLambda = lamIR;
        var diskLambdaStr = lamIR.toString(10);
        settingsId[11].value = lamIR;
        $("#diskLam").val(lamIR);
    }

    if (logGammaCol === null || logGammaCol === "") {
        alert("logGammaCol must be filled out");
        return;
    }
    flagArr[12] = false;
    if (logGammaCol < 0.0) {
        flagArr[12] = true;
        logGammaCol = 0.0;
        var gamStr = "0.0";
        settingsId[12].value = 0.0;
        $("#gammaCol").val(0.0);
    }
    if (logGammaCol > 1.0) {
        flagArr[12] = true;
        logGammaCol = 1.0;
        var gamStr = "1.0";
        settingsId[12].value = 1.0;
        $("#gammaCol").val(1.0);
    }

    if (macroV === null || macroV === "") {
        alert("macroV must be filled out");
        return;
    }
    flagArr[13] = false;
    if (macroV < 0.0) {
        flagArr[13] = true;
        macroV = 0.0;
        var macroVStr = "0.0";
        settingsId[13].value = 0.0;
        $("#macroV").val(0.0);
    }
    if (macroV > 10.0) {
        flagArr[13] = true;
        macroV = 10.0;
        var macroVStr = "10.0";
        settingsId[13].value = 10.0;
        $("#macroV").val(10.0);
    }


    if (rotV === null || rotV === "") {
        alert("rotV must be filled out");
        return;
    }
    flagArr[14] = false;
    if (rotV < 2.0) {
        flagArr[14] = true;
        rotV = 2.0;
        var rotVStr = "2.0";
        settingsId[14].value = 2.0;
        $("#rotV").val(2.0);
    }
    if (rotV > 350.0) {
        flagArr[14] = true;
        rotV = 350.0;
        var rotVStr = "350.0";
        settingsId[14].value = 350.0;
        $("#rotV").val(350.0);
    }


    if (rotI === null || rotI === "") {
        alert("rotI must be filled out");
        return;
    }
    flagArr[15] = false;
    if (rotI < 30.0) {
        flagArr[15] = true;
        rotI = 30.0;
        var rotIStr = "30.0";
        settingsId[15].value = 30.0;
        $("#rotI").val(30.0);
    }
    if (rotI > 90.0) {
        flagArr[15] = true;
        rotI = 90.0;
        var rotIStr = "90.0";
        settingsId[15].value = 90.0;
        $("#rotI").val(90.0);
    }

    if (diskSigma === null || diskSigma == "") {
        alert("filter sigma must be filled out");
        return;
    }
    flagArr[16] = false;
    if (diskSigma < 0.01) {
        flagArr[16] = true;
        diskSigma = 0.01;
        var diskSigmaStr = "0.01";
        settingsId[16].value = 0.01;
        $("#diskSigma").val(0.01);
    }
    if (diskSigma > 10.0) {
        flagArr[16] = true;
        diskSigma = 10.0;
        var diskSigmaStr = "10";
        settingsId[16].value = 10.0;
        $("#diskSigma").val(10.0);
    }
    if (logKapFudge === null || logKapFudge === "") {
        alert("logKapFudge must be filled out");
        return;
    }
    flagArr[17] = false;
    if (logKapFudge < -2.0) {
        flagArr[17] = true;
        logKapFudge = -2.0;
        var gamStr = "-2.0";
        settingsId[17].value = -2.0;
        $("#logKapFudge").val(-2.0);
    }
    if (logKapFudge > 2.0) {
        flagArr[17] = true;
        logKapFudge = 2.0;
        var gamStr = "2.0";
        settingsId[17].value = 2.0;
        $("#logKapFudge").val(2.0);
    }


var url = "http://www.ap.smu.ca/~ishort/OpenStars/GrayStarServer/grayStarServer.php";
//var masterInput="teff="+teff+"&logg="+logg+"&logZScale="+logZScale+"&massStar="+massStar;
var masterInput="teff="+teff+"&logg="+logg+"&logZScale="+logZScale+"&massStar="+massStar
  +"&xiT="+xiT+"&lineThresh="+lineThresh+"&voigtThresh="+voigtThresh+"&lambdaStart="+lambdaStart+"&lambdaStop="+lambdaStop
  +"&sampling="+switchSampl+"&logGammaCol="+logGammaCol+"&logKapFudge="+logKapFudge;
//console.log("masterInput " + masterInput);

var xmlhttp = new XMLHttpRequest();
//CAUTION: xmlhttp.onreadystatechange is an *asynchronous* function - it canot 'return' a result:
xmlhttp.open("POST", url, true);
xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
xmlhttp.send(masterInput);

//
xmlhttp.onreadystatechange = function(){

   if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
 
//JSON object returned by XML http request
var jsonObj;

// This has to be up here for some reason:
// Get the ID of the container div:

    var textId = document.getElementById("outPanel"); // text output area

    //var masterId = document.getElementById("container"); // graphical output area
    var plotOneId = document.getElementById("plotOne");
    var cnvsOneId = document.getElementById("plotOneCnvs");
    var cnvsOneCtx = cnvsOneId.getContext("2d");
    var plotTwoId = document.getElementById("plotTwo");
    var cnvsTwoId = document.getElementById("plotTwoCnvs");
    var cnvsTwoCtx = cnvsTwoId.getContext("2d");
    var plotThreeId = document.getElementById("plotThree");
    var cnvsThreeId = document.getElementById("plotThreeCnvs");
    var cnvsThreeCtx = cnvsThreeId.getContext("2d");
    var plotFourId = document.getElementById("plotFour");
    var cnvsFourId = document.getElementById("plotFourCnvs");
    var cnvsFourCtx = cnvsFourId.getContext("2d");
    var plotFiveId = document.getElementById("plotFive");
    var cnvsFiveId = document.getElementById("plotFiveCnvs");
    var cnvsFiveCtx = cnvsFiveId.getContext("2d");
    var plotSevenId = document.getElementById("plotSeven");
    var cnvsSevenId = document.getElementById("plotSevenCnvs");
    var cnvsSevenCtx = cnvsSevenId.getContext("2d");
    var plotEightId = document.getElementById("plotEight");
    var cnvsEightId = document.getElementById("plotEightCnvs");
    var cnvsEightCtx = cnvsEightId.getContext("2d");
    var plotNineId = document.getElementById("plotNine");
    var cnvsNineId = document.getElementById("plotNineCnvs");
    var cnvsNineCtx = cnvsNineId.getContext("2d");
    var plotTenId = document.getElementById("plotTen");
    var cnvsTenId = document.getElementById("plotTenCnvs");
    var cnvsTenCtx = cnvsTenId.getContext("2d");
    var plotElevenId = document.getElementById("plotEleven");
    var cnvsElevenId = document.getElementById("plotElevenCnvs");
    var cnvsElevenCtx = cnvsElevenId.getContext("2d");
    var plotTwelveId = document.getElementById("plotTwelve");
    var cnvsTwelveId = document.getElementById("plotTwelveCnvs");
    var cnvsTwelveCtx = cnvsTwelveId.getContext("2d");
    var plotThirteenId = document.getElementById("plotThirteen");
    var cnvsThirteenId = document.getElementById("plotThirteenCnvs");
    var cnvsThirteenCtx = cnvsThirteenId.getContext("2d");
    var plotFourteenId = document.getElementById("plotFourteen");
    var cnvsFourteenId = document.getElementById("plotFourteenCnvs");
    var cnvsFourteenCtx = cnvsFourteenId.getContext("2d");

    var printModelId = document.getElementById("printModel"); //detailed model print-out area

//
    var printModelId = document.getElementById("printModel"); //detailed model print-out area

    if (ifShowAtmos === true) {
        plotOneId.style.display = "block";
        plotTwoId.style.display = "block";
        plotThreeId.style.display = "block";
    }
    if (ifShowRad === true) {
        plotFourId.style.display = "block";
        plotFiveId.style.display = "block";
    }
    if (ifShowLogNums === true) {
        //plotSixId.style.display = "block";
        plotEightId.style.display = "block";
    }
    if (ifShowAtmos === false) {
        plotOneId.style.display = "none";
        plotTwoId.style.display = "none";
        plotThreeId.style.display = "none";
    }
    if (ifShowRad === false) {
        plotFourId.style.display = "none";
        plotFiveId.style.display = "none";
    }
    if (ifShowLine === false) {
        //plotSixId.style.display = "none";
        //plotEightId.style.display = "none";
    }
    if ((ifPrintAtmos === true) ||
            (ifPrintSED === true) ||
            (ifPrintIntens === true) ||
            (ifPrintLDC === true) ||
            (ifPrintLine === true) || 
            (ifPrintLogNums === true) || 
            (ifPrintJSON === true) || 
            (ifPrintAbnd === true)) {
        printModelId.style.display = "block";
    } else if (ifPrintNone === true) {
        printModelId.style.display = "none";
    }

    // Begin compute code:

 //Unpack the data returned by the XMLHttp request: 
         //console.log(xmlhttp.responseText);  //testing
         jsonObj = JSON.parse(xmlhttp.responseText);
         //console.log("jsonObj " + jsonObj);
//Unpack the array sizes:
        var numDeps = Number(jsonObj.numDeps);  //number of vertical atmospheric depths 
        var numMaster = Number(jsonObj.numMaster); //number of line blanketed SED lambda points
        var numThetas = Number(jsonObj.numThetas); //number of angles in specific intensity distribution
        //var numSpecSyn = Number(jsonObj.numSpecSyn); //number of angles in specific intensity distribution
        var numGaussLines = Number(jsonObj.numGaussLines); //number of angles in specific intensity distribution
        var numLams = Number(jsonObj.numLams); //number of continuum SED lambda points
        var numSpecies = Number(jsonObj.numSpecies); //number of chemical speecies (ionization stages) 
        var nelemAbnd = Number(jsonObj.nelemAbnd); //number of chemical elements 
      //console.log("numDeps " + numDeps + " numMaster " + numMaster + " numThetas " + numThetas 
      //        + " numSpecSyn " + numSpecSyn + " numGaussLines " + numGaussLines);

    var grav = Math.pow(10.0, logg);
    var zScale = Math.pow(10.0, logZScale);

    //Gray structure and Voigt line code code begins here:
    // Initial set-up:
    // Solar parameters:
    var teffSun = 5778.0;
    var loggSun = 4.44;
    var gravSun = Math.pow(10.0, loggSun);
    var logZScaleSun = 0.0;
    var zScaleSun = Math.exp(logZScaleSun);
    //Solar units:
    var massSun = 1.0;
    var radiusSun = 1.0;
    var logRadius = 0.5 * (Math.log(massStar) + Math.log(gravSun) - Math.log(grav));
    var radius = Math.exp(logRadius);
    var logLum = 2.0 * Math.log(radius) + 4.0 * Math.log(teff / teffSun);
    var bolLum = Math.exp(logLum); // L_Bol in solar luminosities 

    //Composition by mass fraction - needed for opacity approximations
    //   and interior structure
    var massX = 0.70; //Hydrogen
    var massY = 0.28; //Helium
    var massZSun = 0.02; // "metals"
    var massZ = massZSun * zScale; //approximation

    var logNH = 17.0;
    var logE = logTen(Math.E); // for debug output

    //Vega parameters (of Phoenix model- Teff not quite right!)
    var teffVega = 9950.0;
    var loggVega = 3.95;
    var gravVega = Math.pow(10.0, loggVega);
    var zScaleVega = 0.333;


//
//Unpack the atmospheric structure:

         var logTauRosAjax = gsAjaxParser(numDeps, jsonObj.logTau);
         var tauRos = gsDuplex(numDeps, logTauRosAjax);
         var logZAjax = gsAjaxParser(numDeps, jsonObj.logZ);
         var depths = gsDuplex(numDeps, logZAjax);
         var logTempAjax = gsAjaxParser(numDeps, jsonObj.logTemp);
         var temp = gsDuplex(numDeps, logTempAjax);
         var logPGasAjax = gsAjaxParser(numDeps, jsonObj.logPGas);
         var  pGas = gsDuplex(numDeps, logPGasAjax);
         var logPRadAjax = gsAjaxParser(numDeps, jsonObj.logPRad);
         var  pRad = gsDuplex(numDeps, logPRadAjax);
         var logRhoAjax = gsAjaxParser(numDeps, jsonObj.logRho);
         var rho = gsDuplex(numDeps, logRhoAjax);
         var logNeAjax = gsAjaxParser(numDeps, jsonObj.logNe);
         var Ne = gsDuplex(numDeps, logNeAjax);
         var logMmwAjax = gsAjaxParser(numDeps, jsonObj.logMmw);
         var mmw = gsDuplex(numDeps, logMmwAjax);
         var logKappaAjax = gsAjaxParser(numDeps, jsonObj.logKappa);
         var kappaRos = gsDuplex(numDeps, logKappaAjax);

// Recover partial electron pressure for plot:
    var k = 1.3806488E-16; // Boltzmann constant in ergs/K
    var logK = Math.log(k);
    var Pe = [];
    Pe.length = 2;
    Pe[0] = [];
    Pe[1] = [];
    Pe[0].length = numDeps;
    Pe[1].length = numDeps;
    for (var i = 0; i < numDeps; i++){
       Pe[1][i] = Ne[1][i] + logK + temp[1][i]; 
    }

        //Rescaled  kinetic temeprature structure: 

// Set up theta grid
//  cosTheta is a 2xnumThetas array:
// row 0 is used for Gaussian quadrature weights
// row 1 is used for cos(theta) values
// Gaussian quadrature:
// Number of angles, numThetas, will have to be determined after the fact
    // Solve formal sol of rad trans eq for outgoing surfaace I(0, theta)

    var lineMode;
    //
    // ************
    //
    //  Spectrum synthesis section:
    //  
    // Establish continuum wavelength grid:
    // Set up multi-Gray continuum info:

//
//
    //default initializations:

    var masterLams = [];
    masterLams.length = numMaster;
    var masterIntens = [];
    masterIntens.length = numMaster;
    for (var i = 0; i < numMaster; i++) {
        masterIntens[i] = [];
        masterIntens[i].length = numThetas;
    }

//Unpack the line blanketed SED flux and intensity distributions 

         var logMasterLamsAjax = gsAjaxParser(numMaster, jsonObj.logWave);
         for (var i = 0; i < numMaster; i++){
          masterLams[i] = Math.exp(logMasterLamsAjax[i]); 
            }
         var logFluxAjax = gsAjaxParser(numMaster, jsonObj.logFlux);
         var masterFlux = gsDuplex(numMaster, logFluxAjax);
 

     // set up cosTheta as 2D array for now for consistency with legacy
     // first row is Gaussian-Legendre quadrature weight, but I don't think we need it
     var cosTheta = [];
     cosTheta.length = 2;
     cosTheta[0]= []; 
     cosTheta[1]= [];
     cosTheta[0].length = numThetas; 
     cosTheta[1].length = numThetas; 
 
     for (var i = 0; i < numThetas; i++){
        var cosThetaKey = "cosTheta"+i;
        cosTheta[1][i] = Number(jsonObj[cosThetaKey]); 
        cosTheta[0][i] = 0.0; // weight - not needed on client side?
        var intensKey = "Intensity"+i;
        var logIntensAjax = gsAjaxParser(numMaster, jsonObj[intensKey]);
        for (var j = 0; j < numMaster; j++){
           masterIntens[j][i] = Math.exp(logIntensAjax[j]);
        }  //j 
      }  //i

    var colors =  UBVRI(masterLams, masterFlux, numDeps, tauRos, temp);

    // UBVRI band intensity annuli - for disk rendering:
    var bandIntens = iColors(masterLams, masterIntens, numThetas, numMaster); 
    // tunable monochromatic band intensity annuli - for disk rendering:
    //var diskSigma = 1; //nm test
    //var tuneBandIntens = tuneColor(masterLams, masterIntens, numThetas, numMaster, diskLambda, diskSigma, lamUV, lamIR); 
    var gaussFilter = gaussian(masterLams, numMaster, diskLambda, diskSigma, lamUV, lamIR); 
    var tuneBandIntens = tuneColor(masterLams, masterIntens, numThetas, numMaster, gaussFilter, lamUV, lamIR); 

    //default initializations:

    //var specSynLams = [];
    //specSynLams.length = numSpecSyn;
//
////Unpack the spectrum synthsis flux distributions 
//
 //        var logSpecSynLamsAjax = gsAjaxParser(numSpecSyn, jsonObj.logWaveSS);
  //       for (var i = 0; i < numSpecSyn; i++){
   //       specSynLams[i] = Math.exp(logSpecSynLamsAjax[i]); 
    //        }
     //    var logSpecSynFluxAjax = gsAjaxParser(numSpecSyn, jsonObj.logFluxSS);
      //   var specSynFlux = gsDuplex(numSpecSyn, logSpecSynFluxAjax);


    //default initializations:

    var lambdaScale = [];
    lambdaScale.length = numLams;

//Unpack the line continuum SED flux distributions 

         var logContLamsAjax = gsAjaxParser(numLams, jsonObj.logWaveC);
         for (var i = 0; i < numLams; i++){
          lambdaScale[i] = Math.exp(logContLamsAjax[i]); 
            }
         var logFluxAjax = gsAjaxParser(numLams, jsonObj.logFluxC);
         var contFlux = gsDuplex(numLams, logFluxAjax);

//console.log("Testing...");
   var logContFluxI = interpolV(contFlux[1], lambdaScale, masterLams);
   //for (var i = 0; i < numMaster; i++){
   //  console.log("i " + " masterLams[i] " + masterLams[i] + " masterFlux[1][i] " + masterFlux[1][i] + " logContFluxI[i] " + logContFluxI[i]
   //   + " masterFlux/ContFlux " + Math.exp(masterFlux[1][i] - logContFluxI[i]));
   //}
//Unpack the linear monochromatic continuum limb darkening cofficients (LDCs) 

         var ldc = gsAjaxParser(numLams, jsonObj.LDC);

// Apply broadening spectrum synthesis region (macroturbulence, rotation, ...)
//
 //var macroV = 2.0;  //km/s   //test
 //var rotV = 10.0;  //km/s   //test
 //var inclntn = 90.0; //test

  var inclntn = Math.PI * rotI / 180;  //degrees to radians

  //Quality control:
      var tiny = 1.0e-19;
      var logTiny = Math.log(tiny);
   var iStart = lamPoint(numMaster, masterLams, (1.0e-7*lambdaStart));
   var iStop = lamPoint(numMaster, masterLams, (1.0e-7*lambdaStop));

// estimate mid-point of spectrum synthesis region 
   var lambdaMid = (lambdaStart + lambdaStop) / 2;
//find nearest lambda in continuum spectrum: 
   var iMidCont = lamPoint(numLams, lambdaScale, 1.0e-7*lambdaMid);
//continuum limb darkning coefficiient for spectrum synthesis region:
   var rotLDC = ldc[iMidCont]; 
   //console.log("iStart, iStop " + iStart + " " + iStop + " masterLams(iStart), masterLams(iStop) " + masterLams[iStart] + " " + masterLams[iStop]);
     var masterFluxBroad = [];
     masterFluxBroad.length = 2;
     masterFluxBroad[0] = [];
     masterFluxBroad[1] = [];
     masterFluxBroad[0].length = numMaster;
     masterFluxBroad[1].length = numMaster;
//
//Default initialization
     for (var i = 0; i < numMaster; i++){
         masterFluxBroad[0][i] = masterFlux[0][i];
         masterFluxBroad[1][i] = masterFlux[1][i];
     }


    if ( (macroV > 1.0) || (rotV > 1.0) ) {
     //console.log("Calling macroRot()");
        masterFluxBroad = macroRot(masterFlux, masterLams, numMaster, iStart, iStop, macroV, 
                    rotV, inclntn, rotLDC);
    }

//Unpack the structure returned by macroRot():
   var numBroad = masterFluxBroad[0].length;
//Find spectrum synthesis region in SED again:
   iStart = lamPoint(numBroad, masterFluxBroad[2], 1.0e-7*lambdaStart);
   iStop = lamPoint(numBroad, masterFluxBroad[2], 1.0e-7*lambdaStop);
   var numSpecSyn = iStop - iStart + 1;
//console.log("numMaster " + numMaster + " numBroad " + numBroad + " iStart " + iStart + " iStop " + iStop + " numSpecSyn " + numSpecSyn);

//Interpolate continuum spectrum onto line-blanketed spectrum lambda grid:
//masterFluxBroad[2] (row 3) holds the wavelength scale of the broadened spectrum
   var logContFlux2 = interpolV(contFlux[1], lambdaScale, masterFluxBroad[2]);

   var specSynLams = [];
   specSynLams.length = numSpecSyn;
   var specSynFlux = [];
   specSynFlux.length = 2;
   specSynFlux[0] = [];
   specSynFlux[1] = [];
   specSynFlux[0].length = numSpecSyn; 
   specSynFlux[1].length = numSpecSyn; 
   //values:
   var iCount = 0;
        for (var i = iStart; i <= iStop; i++){

           //specSynLams[iCount] = masterLams[i];
            specSynLams[iCount] = masterFluxBroad[2][i];
 //Do quality control here:
            if ( (masterFluxBroad[1][i] < logTiny) || (masterFluxBroad[0][i] < tiny) ){
              masterFluxBroad[1][i] = logTiny;
              masterFluxBroad[0][i] = tiny;
               }
           //approximate rectification:
          // console.log("i " + i + " masterLams[i] " + masterLams[i] + " masterFluxBroad[1][i] " + masterFluxBroad[1][i] 
          //    + " logContFlux2[i] " + logContFlux2[i]
          //    + " planck(teff, masterLams[i]) " + planck(teff, masterLams[i]) );
         //specSynFlux[1][iCount] = masterFluxBroad[1][i] - Planck.planck(teff, masterLams[i]);
         // specSynFlux[1][iCount] = masterFluxBroad[1][i] - planck(teff, masterFluxBroad[2][i]);
           specSynFlux[1][iCount] = masterFluxBroad[1][i] - logContFlux2[i];
           specSynFlux[0][iCount] = Math.exp(specSynFlux[1][iCount]);
          //console.log("iCount " + iCount + " specSynLams[iCount] " + specSynLams[iCount] 
           //  + " masterFluxBroad " + masterFluxBroad[1][i] + " logContFlux2 " + logContFlux2[i]
            // + " specSynFlux[0][iCount] " + specSynFlux[0][iCount]);
           iCount++;
         }

//
//


//
// * eqWidthSynth will try to return the equivalenth width of EVERYTHING in the synthesis region
// * as one value!  Isolate the synthesis region to a single line to a clean result
// * for that line!
// *
      var Wlambda = eqWidthSynth(specSynFlux, specSynLams);
//
//Unpack the line ID tags
//
        var listElements = jsonObj.listElement.split(",");
        var listStages = jsonObj.listStage.split(",");
        var listLams = gsAjaxParser(numGaussLines, jsonObj.listLam0);
        for (var i = 0; i < numGaussLines; i++){
              listElements[i] = listElements[i].trim(); 
              listStages[i] = listStages[i].trim(); 
             }

//Unpack the chemical abundances:
       var element = jsonObj.element.split(",");
       var abundance = gsAjaxParser(nelemAbnd, jsonObj.abundance);
       for (var i = 0; i < nelemAbnd; i++){
          element[i] = element[i].trim();
       }
        

//Unpack the checmial species with total ion stage populations, ionization Es and populations 

      var species = jsonObj.species.split(",");
      var ionStage = gsAjaxParser(numSpecies, jsonObj.ionStage);
      var chiI = gsAjaxParser(numSpecies, jsonObj.chiI);
      var logNumTau1 = gsAjaxParser(numSpecies, jsonObj.logNumTau1);
      for (var i = 0; i < numSpecies; i++){
          species[i] = species[i].trim();
       }


// Put number densities into form that Grotrian diagram plot expects
//var ionEqElement = "Ca";  //test
      var numStages = 5;  //number of ionization stages treated by GrayStarServer
      var logNums = [];
      logNums.length = numStages; 
      var ionE = [];
      ionE.length = numStages; 
      for (var i = 0; i < numSpecies; i++){
          if (ionEqElement == species[i]){
            logNums[ionStage[i]] = logNumTau1[i];
            ionE[ionStage[i]] = chiI[i];
          }
          if (ionEqElement == "H"){
            logNums[1] = 0;
            logNums[2] = 0;
            logNums[3] = 0;
            ionE[1] = 0.0;
            ionE[2] = 0.0;
            ionE[3] = 0.0;
          }
          if (ionEqElement == "He"){
            logNums[2] = 0;
            logNums[3] = 0;
            ionE[2] = 0.0;
            ionE[3] = 0.0;
          }
          if (ionEqElement == "Li"){
            logNums[3] = 0;
            ionE[3] = 0.0;
          }
      }
    

//
//
// 
// *****************************
// 
    // if JQuery-UI round sliders not available:  
    // displayAll();

// *********************



// Text output section:

//    
// Set up the canvas:
//

    // **********  Basic canvas parameters: These are numbers in px - needed for calculations:
    // All plots and other output must fit within this region to be white-washed between runs

    var xRangeText = 1550;
    var yRangeText = 65;
    var xOffsetText = 10;
    var yOffsetText = 10;
    var charToPxText = 4; // width of typical character font in pixels - CAUTION: finesse!

    var zeroInt = 0;
    //these are the corresponding strings ready to be assigned to HTML style attributes


    var xRangeTextStr = numToPxStrng(xRangeText);
    var yRangeTextStr = numToPxStrng(yRangeText);
    var xOffsetTextStr = numToPxStrng(xOffsetText);
    var yOffsetTextStr = numToPxStrng(yOffsetText);
    // Very first thing on each load: White-wash the canvas!!

    var washTId = document.createElement("div");
    var washTWidth = xRangeText + xOffsetText;
    var washTHeight = yRangeText + yOffsetText;
    var washTTop = yOffsetText;
    var washTWidthStr = numToPxStrng(washTWidth);
    var washTHeightStr = numToPxStrng(washTHeight);
    var washTTopStr = numToPxStrng(washTTop);
    washTId.id = "washT";
    washTId.style.position = "absolute";
    washTId.style.width = washTWidthStr;
    washTId.style.height = washTHeightStr;
    washTId.style.marginTop = washTTopStr;
    washTId.style.marginLeft = "0px";
    washTId.style.opacity = 1.0;
    washTId.style.backgroundColor = "#EEEEEE";
    //washId.style.zIndex = -1;
    washTId.style.zIndex = 0;
    //washTId.style.border = "2px blue solid";

    //Wash the canvas:
    textId.appendChild(washTId);
    var roundNum, remain;
    // R & L_Bol:
    var colr = 0;
    var xTab = 60;
    roundNum = radius.toPrecision(3);
    txtPrint("<span title='Stellar radius'><em>R</em> = </span> "
            + roundNum
            + " <span title='Solar radii'>\n\
<a href='http://en.wikipedia.org/wiki/Solar_radius' target='_blank'><em>R</em><sub>Sun</sub></a>\n\
</span> ",
            20 + colr * xTab, 15, lineColor, textId);
    roundNum = bolLum.toPrecision(3);
    txtPrint("<span title='Bolometric luminosity'>\n\
<a href='http://en.wikipedia.org/wiki/Luminosity' target='_blank'><em>L</em><sub>Bol</sub></a> = \n\
</span> "
            + roundNum
            + " <span title='Solar luminosities'>\n\
<a href='http://en.wikipedia.org/wiki/Solar_luminosity' target='_blank'><em>L</em><sub>Sun</sub></a>\n\
</span> ",
            20 + colr * xTab, 40, lineColor, textId);
// 

    // UBVRI indices
    var xTab = 80;
    var colr = 0;
    var roundNum0 = colors[0].toFixed(2);
    var roundNum1 = colors[1].toFixed(2);
    var roundNum2 = colors[2].toFixed(2);
    var roundNum3 = colors[3].toFixed(2);
    var roundNum4 = colors[4].toFixed(2);
    txtPrint("<a href='http://en.wikipedia.org/wiki/UBV_photometric_system' title='Johnson-Cousins U-B photometric color index' target='_blank'>\n\
<span style='color:purple'>U</span>-" +
            "<span style='color:blue'>B\n\
</span>\n\
</a>: " + roundNum0
            + " <a href='http://en.wikipedia.org/wiki/UBV_photometric_system' title='Johnson-Cousins B-V photometric color index' target='_blank'>\n\
<span style='color:blue'>B\n\
</span>-" +
            "<span style='color:#00FF88'>V</span></a>: " + roundNum1
            + " <a href='http://en.wikipedia.org/wiki/UBV_photometric_system' title='Johnson-Cousins V-R photometric color index' target='_blank'>\n\
<span style='color:#00FF88'>V\n\
</span>-" +
            "<span style='color:red'>R\n\
</span>\n\
</a>: " + roundNum2
            + " <a href='http://en.wikipedia.org/wiki/UBV_photometric_system' title='Johnson-Cousins V-I photometric color index' target='_blank'>\n\
<span style='color:#00FF88'>V\n\
</span>-" +
            "<span style='color:red'>I\n\
</span>\n\
</a>: " + roundNum3
            + " <a href='http://en.wikipedia.org/wiki/UBV_photometric_system' title='Johnson-Cousins R-I photometric color index' target='_blank'>\n\
<span style='color:red'>R</span>-" +
            "<span style='color:brown'>I\n\
</span>\n\
</a>: " + roundNum4, 180 + colr * xTab, 15, lineColor, textId);

    // Echo back the *actual* input parameters:
    var warning = "";
    if (teff < 6000) {
        //warning = "<span style='color:red'><em>T</em><sub>eff</sub> < 6000 K <br />Cool star mode";
        warning = "<span style='color:red'>Cool star mode</span>";
        txtPrint(warning, 600, 10, lineColor, textId);
    } else {
        //warning = "<span style='color:blue'><em>T</em><sub>eff</sub> > 6000 K <br />Hot star mode</span>";
        warning = "<span style='color:blue'>Hot star mode</span>";
        txtPrint(warning, 600, 10, lineColor, textId);
    }

    var spectralClass = " ";
    var luminClass = "V";
    if (teff < 3000.0) {
        spectralClass = "L";
    } else if ((teff >= 3000.0) && (teff < 3900.0)) {
        spectralClass = "M";
    } else if ((teff >= 3900.0) && (teff < 5200.0)) {
        spectralClass = "K";
    } else if ((teff >= 5200.0) && (teff < 5950.0)) {
        spectralClass = "G";
    } else if ((teff >= 5950.0) && (teff < 7300.0)) {
        spectralClass = "F";
    } else if ((teff >= 7300.0) && (teff < 9800.0)) {
        spectralClass = "A";
    } else if ((teff >= 9800.0) && (teff < 30000.0)) {
        spectralClass = "B";
    } else if (teff >= 30000.0) {
        spectralClass = "O";
    }

    if ((logg >= 0.0) && (logg < 1.0)) {
        luminClass = "I";
    } else if ((logg >= 1.0) && (logg < 1.5)) {
        luminClass = "II";
    } else if ((logg >= 1.5) && (logg < 3.0)) {
        luminClass = "III";
    } else if ((logg >= 3.0) && (logg < 4.0)) {
        luminClass = "IV";
    } else if ((logg >= 4.0) && (logg < 5.0)) {
        luminClass = "V";
    } else if ((logg >= 5.0) && (logg < 6.0)) {
        luminClass = "VI";
    } else if (logg >= 6.0) {
        luminClass = "WD";
    }

    var spectralType = "<a href='https://en.wikipedia.org/wiki/Stellar_classification' title='MK Spectral type' target='_blank'>" +
            spectralClass + " " + luminClass +
            "</a>";
    txtPrint(spectralType, 600, 40, lineColor, textId);
    xTab = 140;
    var outString, fullNum, j;
    //var numReportable = numInputs - numPreStars - numPreLines - -numPrePlanets - numPerfModes - 1;
    var echoText = "<table><tr>  ";
    //  var setName = ""; //initialization

    for (var i = 0; i < numInputs; i++) {

        var fullNum = settingsId[i].value;
        //roundNum = fullNum.toPrecision(2);
        roundNum = fullNum;
        //console.log("i " + i + " settingsId[i].name " + settingsId[i].name + " settingsId[i].value " + settingsId[i].value + " fullNum " + fullNum + " roundNum " + roundNum);
        if (flagArr[i]) {
            outString = "<td>   <span style='color:red'>   " + settingsId[i].name + "</br>" + roundNum.toString(10) + "   </span>   </td>";
            //outString = "<td>   <span style='color:red'>   " + setName + "</br>" + roundNum.toString(10) + "   </span>   </td>";
        } else {
            outString = "<td>   <span style='color:black'>   " + settingsId[i].name + "</br>" + roundNum.toString(10) + "   </span>   </td>";
            //outString = "<td>   <span style='color:black'>   " + setName + "</br>" + roundNum.toString(10) + "   </span>   </td>";
        }
        //if (i === numReportable / 2){
        //    echoText = echoText + "</tr><tr>";  // New row
        //};
        echoText = echoText + "   " + outString + "   ";
    }  // i loop

    echoText = echoText + "  </tr></table>";
    txtPrint(echoText, 750, 10, lineColor, textId);


// Graphical output section:


//  NOTE!!!
//  
//  The remaining lines of code or so are all devoted to the graphical (and textual) output.  
//  Have not been able to spin this stuff off into separare function that can be called from 
//  seperate source files by the HTML code.  This is baaaaad!  :-(
//    

// ***** WARNING: Do NOT rearrange order of plot-wise code-blocks.  Some blocks use variables declared and initialized in
// previous blocks.  If you want to re-arrange the plots in the WWW page, change the row and column number
// indices (plotRow, plotCol) at the beginning of each block

//    
// Set up the canvas:
//

// Coordinate considerations with HTML5 <canvas>:
//Display coordinates for size and location of canvas with respect to browser viewport
// are set with CSS with JavaScript (JS) as in objectID.style.width = "***px" 
// (just like scripting a <div>)
//
//"Model" (ie. canvas) coordinates set in html document with <canvas width="***", ...>
// - The CSS (panlWidth & panelHeight) and HTML "width"s and "height"s have to match 
// for model and display coordinates to be on the same scale
// AND: display (CSS) coordinates are with repect to upper left corner of browser window (viewport)
//   BUT model (canvas) coordinates are with respect to upper left corner of *canvas*!
// So - we have to run two coordinate systems with different origins, but same scale
//   camelCase variables of form ***Cnvs are in canvas coordinates

//
// Offset note: Quantities that are relative offsets with respect to other elements 
// have name of form ***Offset;
// Generally, yOffsets must be *added* to the reference element's y-coordinate AND
// xOffsets must be *subtracted* from the reference element's x-coordinate
//  (origin is always upper left corner whether viewport or canvas)
//

// **********  Basic canvas parameters: These are numbers in px - needed for calculations:
// All plots and other output must fit within this region to be gray-washed between runs

// **************************
//
// Global graphical output variables:
//
//  Panel variables 
//  - set with HTML/CSS style parameters 
//  - in pixels
//
//How many rows and columns of plots:
  var numRows = 4;
  var numCols = 3;
//
// These are with respect to browser viewport upper left corner 
// (ie. y coordinate increases *downward*) :
//
//
   //Origin of panel (upper left corner of panel)
   // - to be computeed for each panel from plotCol & spacingX 
   //    AND plotRow & spacingY
   var panelX, panelY;
//
// Whole number indices for row and column number in plot grid:
   var plotRow, plotCol;
//
   var panelWidth, panelHeight;
// 
   //horizontal and vertical intervals between panel origins
   //  - must be greater than panelWidth & panelHeight, respectively
   //  to avoid panel overlap
   var spacingX, spacingY;  //horizontal and vertical intervals between panel origins
//
   panelWidth = 450;
   panelHeight = 350;
   spacingX = panelWidth + 5;
   spacingY = panelHeight + 5;
//
// The following panel elements are clickable/hoverable HTML elements, so will be handled in CSS
// and so are with respect to the browser viewport:
// These are offsets from the panel origin (specifid by panelX & panelY), and so *seem*
// a lot like canvas coordinates:
//
// NOTE: Custom functions like txtPrint(), numPrint(), and plotPnt() always take these
// viewport oriented coordinates
// 
   var titleOffsetX, titleOffsetY; //main plot title origin
   var xAxisNameOffsetX, xAxisNameOffsetY;
   var yAxisNameOffsetX, yAxisNameOffsetY;
   titleOffsetX = 10;
   titleOffsetY = 10;
   xAxisNameOffsetX = 100;
   xAxisNameOffsetY = panelHeight - 35;
   yAxisNameOffsetX = 5;
   yAxisNameOffsetY = 100;
// ... and to hold the computed viewport coordinates:
   var titleX, titleY;
   var xAxisNameX, xAxisNameY;
   var yAxisNameX, yAxisNameY;

// Global offsets to provid white space above top-row plots
// and to the left of left-column plots (Needed??) 
   var xOffset, yOffset;
   xOffset = 3;
   yOffset = 3;

    //these are the corresponding strings in pixel units ready to be assigned to HTML/CSS style attributes
   var panelXStr;
   var panelYStr;
   //var panelWidthStr = numToPxStrng(panelWidth);
   var panelHeightStr = numToPxStrng(panelHeight);
   var titleXStr, titleYStr;
   var xAxisNameXStr, xAxisNameYStr;
   var yAxisNameXStr, yAxisNameYStr;


//Background color of panels - a gray tone will accentuate most colors:
// 24 bit RGB color in hexadecimal notation:
    var wColor = "#F0F0F0";  

    var charToPx = 4; // width of typical character font in pixels - CAUTION: finesse!

//
//  function washer() creates and inserts a panel into the HTML doc 
//   AND erases it by "gray-washing" it upon each re-execution of the script 
    var washer = function(plotRow, plotCol, thisPanelWidth, wColor, areaId, cnvsId) {
        // Very first thing on each load: gray-wash the canvas!!

// Browser viewport coordinates for upper left corner of panel:
        panelX = xOffset + plotCol * spacingX; 
        panelY = yOffsetText + yRangeText +
             yOffset + plotRow * spacingY;
        panelXStr = numToPxStrng(panelX);
        panelYStr = numToPxStrng(panelY);
        var thisPanelWidthStr = numToPxStrng(thisPanelWidth);

//script the <div> container:
        areaId.style.position = "absolute";
        areaId.style.width = thisPanelWidthStr;
        areaId.style.height = panelHeightStr;
        areaId.style.marginTop = panelYStr;
        areaId.style.marginLeft = panelXStr;
        areaId.style.backgroundColor = wColor;
//
//script the <canvas>:
        cnvsId.style.position = "absolute";
        cnvsId.style.width = thisPanelWidthStr;
        cnvsId.style.height = panelHeightStr;
        //cnvsId.style.marginTop = panelYStr;
        //cnvsId.style.marginLeft = panelXStr;
        cnvsId.style.opacity = "1.0";
        cnvsId.style.backgroundColor = wColor;
        cnvsId.style.zIndex = 0;
        //cnvsId.style.border = "1px gray solid";
        //Wash the canvas:
        areaId.appendChild(cnvsId);

        var panelOrigin = [panelX, panelY];

        return panelOrigin;

    };

//
//
// These global parameters are for the HTML5 <canvas> element 
//   - in pixls
//   - with respect to upper left corner of *panel* (not viewport!)
//   Assumes that <canvas> HTML width/height in html doc and CSS JS 
//   canvas width/height are the same for a 1:1 scaling (see main graphical
//   section comments above). 
//
    //******* axis, tick mark, and tick-value properties: 

//Origins of plot axes
   var xAxisXCnvs, xAxisYCnvs; //x-axis
   var yAxisXCnvs, yAxisYCnvs; //y-axis
   xAxisXCnvs = 95; //should be greater than yAxisNameOffset  
   xAxisYCnvs = panelHeight - 65; //should be greater than xAxisNameOffset  
   yAxisXCnvs = xAxisXCnvs; //Have x & y axes meet at common origin
   // yAxisYCnvs must be initialized below...

//
//Lengths of plot axes
   var xAxisLength, yAxisLength;
   xAxisLength = 300; //should be less than (panelWidth - yAxisXCnvs)    
   yAxisLength = 200; //should be less than (panelHeight - xAxisYCnvs)    
   yAxisYCnvs = xAxisYCnvs - yAxisLength; // *top* of the y-axis - Have x & y axes meet at common origin

    //tick marks:
    //This is either or a height or width depending on whether an x-axis or a y-axis tick: 
    var tickLength = 8; 
//Offsets with respect to relevent axis:
    var xTickYOffset = (-1 * tickLength) / 2;
    var yTickXOffset = (-1 * tickLength) / 2;
//tick mark value label offsets:
    var xValYOffset = 2 * tickLength;
    var yValXOffset = (-4 * tickLength);

//
//Other general values:
//Default color of plot elements:
  var lineColor = "#000000"; //black
//Default one thickness of plot elements:
  var lineThick = 1;
//
    //
    //
    //
    //
    // *******************************************
    //
    //
    //  This section has global physics related quantities needed for content of plots
    //
    // Line center, lambda_0 

    var numDeps = tauRos[0].length;

    //
    //Initialize *physical* quantities needed for various plots - plots are now all in if(){} blocks
    // so all this now has to be initialized ahead of time:
    // Will need this in some if blocks below:
    var tTau1 = tauPoint(numDeps, tauRos, 1.0);
    var iLamMinMax = minMax2(masterFlux);
    var iLamMax = iLamMinMax[1];
    var norm = 1.0e15; // y-axis normalization
    var wien = 2.8977721E-1; // Wien's displacement law constant in cm K
    var lamMax = 1.0e7 * (wien / teff);
    lamMax = lamMax.toPrecision(5);
    var lamMaxStr = lamMax.toString(10);
    //Vega's disk center values of B, V, R intensity normalized by B+V+R:
    //var vegaBVR = [1.0, 1.0, 1.0]; //for now
    //console.log("Vega: rr " + vegaBVR[2] +
    //        " gg " + vegaBVR[1] +
    //        " bb " + vegaBVR[0]);
    var rgbVega = [183.0 / 255.0, 160.0 / 255.0, 255.0 / 255.0];
    var bvr = bandIntens[2][0] + bandIntens[3][0] + bandIntens[4][0];
    //console.log("bandIntens[2][0]/bvr " + bandIntens[2][0] / bvr + " bandIntens[3][0]/bvr " + bandIntens[3][0] / bvr + " bandIntens[4][0]/bvr " + bandIntens[4][0] / bvr);
    //console.log("Math.max(bandIntens[2][0]/bvr, bandIntens[3][0]/bvr, bandIntens[4][0]/bvr) " + Math.max(bandIntens[2][0] / bvr, bandIntens[3][0] / bvr, bandIntens[4][0] / bvr));
    var brightScale = 255.0 / Math.max(bandIntens[2][0] / bvr, bandIntens[3][0] / bvr, bandIntens[4][0] / bvr);
    var saveRGB = []; //intialize
    var saveRadius = 0.0; //initialize
    var radiusScale = 10; //solar_radii-to-pixels!
    var logScale = 50; //amplification factor for log pixels
    // 
    // Star radius in pixels:
    //    var radiusPx = (radiusScale * radius);  //linear radius
    var radiusPx = logScale * logTen(radiusScale * radius); //logarithmic radius
    radiusPx = Math.ceil(radiusPx);
    var i = 3;
    var ii = 1.0 * i;
    // LTE Eddington-Barbier limb darkening: I(Tau=0, cos(theta)=t) = B(T(Tau=t))
    var cosFctr = cosTheta[1][i];
    var radiusPxI = Math.ceil(radiusPx * Math.sin(Math.acos(cosFctr)));
    var radiusStr = numToPxStrng(radiusPxI);
    saveRadius = radiusPxI; // For HRD, plot nine
    var i = Math.ceil(numThetas / 2);
    var rrI = Math.ceil(brightScale * (bandIntens[4][i] / bvr) / rgbVega[0]); // / vegaBVR[2]);
    var ggI = Math.ceil(brightScale * (bandIntens[3][i] / bvr) / rgbVega[1]); // / vegaBVR[1]);
    var bbI = Math.ceil(brightScale * (bandIntens[2][i] / bvr) / rgbVega[2]); // / vegaBVR[0]);
    //console.log(" rrI: " + rrI + " ggI: " + ggI + " bbI: " + bbI + " dark: " + dark);
    var RGBArr = [];
    RGBArr.length = 3;
    RGBArr[0] = rrI;
    RGBArr[1] = ggI;
    RGBArr[2] = bbI;
    saveRGB = RGBArr; // For HRD, plot nine

    //
    //
    //
    // ********* XBar()
    //
    //
    //
//// Draws a horizontal line (for any purpose) at a given DATA y-coordinate (yVal) 
//and returns the DEVICE y-coordinate (yShift) for further use by calling routine
// (such as placing an accompanying annotation)
//
    var XBar = function(yVal, minYDataIn, maxYDataIn, barWidthCnvs, barHeightCnvs,
            xFinesse, color, areaId, cnvsCtx) {

        var yBarPosCnvs = yAxisLength * (yVal - minYDataIn) / (maxYDataIn - minYDataIn);
        //       xTickPos = xTickPos;

        var yShiftCnvs = (yAxisYCnvs + yAxisLength) - yBarPosCnvs;
        yShiftCnvs = Math.floor(yShiftCnvs);
        barWidthCnvs = Math.floor(barWidthCnvs);

// Make the y-tick mark, Teff:
        cnvsCtx.beginPath();
        cnvsCtx.strokeStyle=color; 
        cnvsCtx.moveTo(yAxisXCnvs, yShiftCnvs);
        cnvsCtx.lineTo(yAxisXCnvs + barWidthCnvs, yShiftCnvs);
        cnvsCtx.stroke();  
//
        return yShiftCnvs;
    };
    //
    //
    //
    // ********* YBar()
    //
    //
    //

// Draws a vertical line (for any purpose) at a given DATA x-coordinate (xVal) 
//and returns the DEVICE x-coordinate (xShift) for further use by calling routine
// (such as placing an accompanying annotation)
// CAUTION: input parameter barHeightCnvs gets ADDED to input parameter yFinesse
// and bar will be drawn DOWNWARD from (yAxisYCnvs + yFinesse)
    var YBar = function(xVal, minXDataIn, maxXDataIn, thisXAxisLength, barWidthCnvs, barHeightCnvs,
            yFinesse, color, areaId, cnvsCtx) {

        var xBarPosCnvs = thisXAxisLength * (xVal - minXDataIn) / (maxXDataIn - minXDataIn);
        var xShiftCnvs = xAxisXCnvs + xBarPosCnvs;
        var yBarPosCnvs = yAxisYCnvs + yFinesse; 
        xShiftCnvs = Math.floor(xShiftCnvs);
        barHeightCnvs = Math.floor(barHeightCnvs);
        yBarPosCnvs = Math.floor(yBarPosCnvs);

// Make the x-tick mark, Teff:
        cnvsCtx.beginPath();
        cnvsCtx.strokeStyle=color; 
        cnvsCtx.moveTo(xShiftCnvs, yBarPosCnvs);
        cnvsCtx.lineTo(xShiftCnvs, yBarPosCnvs + barHeightCnvs);
        cnvsCtx.stroke();  
        
        return xShiftCnvs;
    };
    //
    //
    //
    //
    //
    //  ***** XAxis()
    //
    //
    //

    var XAxis = function(panelX, panelY, thisXAxisLength,
            minXDataIn, maxXDataIn, xAxisName, fineness,
            areaId, cnvsCtx) {

        var axisParams = [];
        axisParams.length = 8;
        // Variables to handle normalization and rounding:
        var numParts = [];
        numParts.length = 2;

        //axisParams[5] = xLowerYOffset;
//
        cnvsCtx.beginPath();
        cnvsCtx.strokeStyle=lineColor; //black
        cnvsCtx.fillStyle=lineColor; //black
        cnvsCtx.moveTo(xAxisXCnvs, xAxisYCnvs);
        cnvsCtx.lineTo(xAxisXCnvs + thisXAxisLength, xAxisYCnvs);
        cnvsCtx.stroke();  
//
        numParts = standForm(minXDataIn);
        //mantissa = rounder(numParts[0], 1, "down");
        //minXData = mantissa * Math.pow(10.0, numParts[1]);
        var mantissa0 = numParts[0];
        var exp0 = numParts[1];
        //numParts = standForm(maxXDataIn);
        //mantissa = rounder(numParts[0], 1, "up");
        //maxXData = mantissa * Math.pow(10.0, numParts[1]);
        var mantissa1 = maxXDataIn / Math.pow(10.0, exp0);
        //var rangeXData = maxXData - minXData;
        var reverse = false; //initialization
        var rangeXData = mantissa1 - mantissa0;
        //Catch axes that are supposed to be backwards
        if (rangeXData < 0.0) {
            rangeXData = -1.0 * rangeXData;
            reverse = true;
        }
        var deltaXData = 1.0; //default initialization
        if (rangeXData >= 100000.0) {
            deltaXData = 20000.0;
        } else if ((rangeXData >= 20000.0) && (rangeXData < 100000.0)) {
            deltaXData = 20000.0;
        } else if ((rangeXData >= 1000.0) && (rangeXData < 20000.0)) {
            deltaXData = 2000.0;
        } else if ((rangeXData >= 250.0) && (rangeXData < 1000.0)) {
            deltaXData = 200.0;
        } else if ((rangeXData >= 100.0) && (rangeXData < 250.0)) {
            deltaXData = 20.0;
        } else if ((rangeXData >= 50.0) && (rangeXData < 100.0)) {
            deltaXData = 10.0;
        } else if ((rangeXData >= 20.0) && (rangeXData < 50.0)) {
            deltaXData = 5.0;
        } else if ((rangeXData >= 8.0) && (rangeXData < 20.0)) {
            deltaXData = 2.0;
        } else if ((rangeXData > 5.0) && (rangeXData <= 8.0)) {
            deltaXData = 0.5;
        } else if ((rangeXData > 2.0) && (rangeXData <= 5.0)) {
            deltaXData = 0.5;
        } else if ((rangeXData > 0.5) && (rangeXData <= 2.0)) {
            deltaXData = 0.5;
        } else if ((rangeXData > 0.1) && (rangeXData <= 0.5)) {
            deltaXData = 0.1;
        } else if ((rangeXData > 0.01) && (rangeXData <= 0.1)) {
            deltaXData = 0.02;
        } else if (rangeXData < 0.01){
            deltaXData = 0.002;
        }

        if (fineness == "hyperfine"){
              deltaXData = deltaXData / 10.0;
             }
        if (fineness == "fine"){
             }
        if (fineness == "coarse"){
              deltaXData = deltaXData * 2.0;
             }

        var mantissa0new = mantissa0 - (mantissa0 % deltaXData) - deltaXData;
        var mantissa1new = mantissa1 - (mantissa1 % deltaXData) + deltaXData;
        var numerDiff = ((mantissa1new - mantissa0new) / deltaXData).toPrecision(6);
//        var numXTicks = Math.floor((mantissa1new - mantissa0new) / deltaXData);
        var numXTicks = Math.floor(numerDiff);
        if (reverse) {
            deltaXData = -1.0 * deltaXData;
            //minXData2 = minXData2 - deltaXData; //sigh - I dunno.
            numXTicks = (-1 * numXTicks); // + 1; //sigh - I dunno.
        }
        numXTicks++;
        var minXData2, maxXData2, rangeXData2;
        minXData2 = mantissa0new * Math.pow(10.0, exp0);
        maxXData2 = mantissa1new * Math.pow(10.0, exp0);
        rangeXData2 = (mantissa1new - mantissa0new) * Math.pow(10.0, exp0);
        deltaXData = deltaXData * Math.pow(10.0, exp0);
        //var deltaXData = rangeXData / (1.0 * numXTicks);
        //numParts = standForm(deltaXData);
        //mantissa = rounder(numParts[0], 1, "down");
        //deltaXData = mantissa * Math.pow(10.0, numParts[1]);
        var deltaXPxl = panelWidth / (numXTicks - 1);
        var deltaXPxlCnvs = thisXAxisLength / (numXTicks - 1);

        axisParams[1] = rangeXData2;
        axisParams[2] = deltaXData;
        axisParams[3] = deltaXPxl;
        axisParams[6] = minXData2;
        axisParams[7] = maxXData2;
        //
        var ii;
        for (var i = 0; i < numXTicks; i++) {

            ii = 1.0 * i;
            var xTickPos = ii * deltaXPxl;
            var xTickPosCnvs = ii * deltaXPxlCnvs;
            var xTickVal = minXData2 + (ii * deltaXData);
            var xTickRound = xTickVal.toPrecision(3); //default
            //var xTickRound = xTickVal;
        if (fineness == "hyperfine"){
            var xTickRound = xTickVal.toPrecision(5);
              }
        if (fineness == "fine"){
            var xTickRound = xTickVal.toPrecision(4);
              }
        if (fineness == "coarse"){
            var xTickRound = xTickVal.toPrecision(3);
              }

            var xTickValStr = xTickRound.toString(10);
            // horizontal position in pixels - data values increase rightward:
            var xShiftCnvs = xAxisXCnvs + xTickPosCnvs;
            xShiftCnvs = Math.floor(xShiftCnvs);
// Make the x-tick mark, Teff:

            cnvsCtx.beginPath();
            cnvsCtx.fillStyle=lineColor; //black
            cnvsCtx.strokeStyle=lineColor; //black
            cnvsCtx.moveTo(xShiftCnvs, xAxisYCnvs + xTickYOffset);
            cnvsCtx.lineTo(xShiftCnvs, xAxisYCnvs + xTickYOffset + tickLength);
            cnvsCtx.stroke();            //test

            //Make the tick label, Teff:
            cnvsCtx.font="normal normal normal 6pt arial";
            cnvsCtx.fillText(xTickValStr, xShiftCnvs, xAxisYCnvs + xValYOffset);
            
        }  // end x-tickmark loop


// Add name of x-axis:
//Axis label still needs to be html so we can use mark-up
        xAxisNameX = panelX + xAxisNameOffsetX;
        xAxisNameY = panelY + xAxisNameOffsetY;
        txtPrint("<span style='font-size:small'>" + xAxisName + "</span>",
                xAxisNameOffsetX, xAxisNameOffsetY, lineColor, areaId);

     // cnvsCtx.font="normal normal normal 12pt arial";
     // cnvsCtx.fillText(xAxisName, xNameXOffsetThisCnvs, xNameYOffsetCnvs);
        
        return axisParams;

    };

    //
    //
    //
    //  ***** YAxis()
    //
    //
    //

    var YAxis = function(panelX, panelY,
            minYDataIn, maxYDataIn, yAxisName,
            areaId, cnvsCtx) {

        var axisParams = [];
        axisParams.length = 8;
        // Variables to handle normalization and rounding:
        var numParts = [];
        numParts.length = 2;

        //axisParams[5] = xLowerYOffset;
        // Create the LEFT y-axis element and set its style attributes:

        cnvsCtx.beginPath();
        cnvsCtx.fillStyle=lineColor; //black
        cnvsCtx.strokeStyle=lineColor; //black
        cnvsCtx.moveTo(yAxisXCnvs, yAxisYCnvs);
        cnvsCtx.lineTo(yAxisXCnvs, yAxisYCnvs + yAxisLength);
        cnvsCtx.stroke();  
        
        numParts = standForm(minYDataIn);
        //mantissa = rounder(numParts[0], 1, "down");
        //minYData = mantissa * Math.pow(10.0, numParts[1]);
        var mantissa0 = numParts[0];
        var exp0 = numParts[1];
        //numParts = standForm(maxYDataIn);
        //mantissa = rounder(numParts[0], 1, "up");
        //maxYData = mantissa * Math.pow(10.0, numParts[1]);
        var mantissa1 = maxYDataIn / Math.pow(10.0, exp0);
        //var rangeYData = maxYData - minYData;
        var reverse = false; //initialization
        var rangeYData = mantissa1 - mantissa0;
        //Catch axes that are supposed to be backwards
        if (rangeYData < 0.0) {
            rangeYData = -1.0 * rangeYData;
            reverse = true;
        }
        var deltaYData = 1.0; //default initialization
        if (rangeYData >= 100000.0) {
            deltaYData = 20000.0;
        } else if ((rangeYData >= 20000.0) && (rangeYData < 100000.0)) {
            deltaXData = 25000.0;
        } else if ((rangeYData >= 1000.0) && (rangeYData < 20000.0)) {
            deltaYData = 5000.0;
        } else if ((rangeYData >= 250.0) && (rangeYData < 1000.0)) {
            deltaYData = 200.0;
        } else if ((rangeYData >= 100.0) && (rangeYData < 250.0)) {
            deltaYData = 20.0;
        } else if ((rangeYData >= 50.0) && (rangeYData < 100.0)) {
            deltaYData = 10.0;
        } else if ((rangeYData >= 20.0) && (rangeYData < 50.0)) {
            deltaYData = 5.0;
        } else if ((rangeYData >= 8.0) && (rangeYData < 20.0)) {
            deltaYData = 2.0;
        } else if ((rangeYData > 0.5) && (rangeYData <= 2.0)) {
            deltaYData = 0.20;
        } else if ((rangeYData > 0.1) && (rangeYData <= 0.5)) {
            deltaYData = 0.1;
        } else if (rangeYData <= 0.1) {
            deltaYData = 0.02;
        }

        var mantissa0new = mantissa0 - (mantissa0 % deltaYData);
        var mantissa1new = mantissa1 - (mantissa1 % deltaYData) + deltaYData;
        var numerDiff = ((mantissa1new - mantissa0new) / deltaYData).toPrecision(6);
//        var numYTicks = Math.floor((mantissa1new - mantissa0new) / deltaYData); // + 1;
        var numYTicks = Math.floor(numerDiff);
        if (reverse) {
            deltaYData = -1.0 * deltaYData;
            //minYData2 = minYData2 - deltaXData; //sigh - I dunno.
            numYTicks = (-1 * numYTicks); // + 1; //sigh - I dunno.
        }
        numYTicks++;
        deltaYData = deltaYData * Math.pow(10.0, exp0);
        var minYData2, maxYData2, rangeYData2;
        minYData2 = mantissa0new * Math.pow(10.0, exp0);
        maxYData2 = mantissa1new * Math.pow(10.0, exp0);
        rangeYData2 = (mantissa1new - mantissa0new) * Math.pow(10.0, exp0);
        //var deltaYData = rangeYData / (1.0 * numYTicks);
        //numParts = standForm(deltaYData);
        //mantissa = rounder(numParts[0], 1, "down");
        //deltaYData = mantissa * Math.pow(10.0, numParts[1]);
        var deltaYPxl = panelHeight / (numYTicks - 1);
        var deltaYPxlCnvs = yAxisLength / (numYTicks - 1);
        axisParams[1] = rangeYData2;
        axisParams[2] = deltaYData;
        axisParams[3] = deltaYPxl;
        axisParams[6] = minYData2;
        axisParams[7] = maxYData2;
        //
        cnvsCtx.fillStyle=lineColor; //black
        var ii;
        for (var i = 0; i < numYTicks; i++) {

            ii = 1.0 * i;
            var yTickPos = ii * deltaYPxl;
            var yTickPosCnvs = ii * deltaYPxlCnvs;
            // Doesn't work - ?? var yTickVal = minYDataRnd + (ii * deltaDataRnd);
            var yTickVal = minYData2 + (ii * deltaYData);
            var yTickRound = yTickVal.toPrecision(3);
            //var yTickRound = yTickVal;
            var yTickValStr = yTickRound.toString(10);
            // vertical position in pixels - data values increase upward:
            var yShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;
            yShiftCnvs = Math.floor(yShiftCnvs);
// Make the y-tick mark, Teff:
           cnvsCtx.beginPath();
           cnvsCtx.fillStyle=lineColor; //black
           cnvsCtx.strokeStyle=lineColor; //black
           cnvsCtx.moveTo(yAxisXCnvs + yTickXOffset, yShiftCnvs);
           cnvsCtx.lineTo(yAxisXCnvs + yTickXOffset + tickLength, yShiftCnvs);
           cnvsCtx.stroke();    
            

            //Make the y-tick label:
         cnvsCtx.font="normal normal normal 8pt arial";
         cnvsCtx.fillText(yTickValStr, yAxisXCnvs + yValXOffset, yShiftCnvs);

        }  // end y-tickmark loop, j

// Add name of LOWER y-axis:

//Axis label still need to be html so we can use mark-up
        yAxisNameX = panelX + yAxisNameOffsetX;
        yAxisNameY = panelY + yAxisNameOffsetY;
        txtPrint("<span style='font-size:x-small'>" + yAxisName + "</span>",
                yAxisNameOffsetX, yAxisNameOffsetY, lineColor, areaId);

        return axisParams;

    };

    //   var testVal = -1.26832e7;
    //   var numParts = standForm(testVal);
//
    //   var roundVal = rounder(numParts[0], 1, "up");

    var xFinesse = 0.0; //default initialization
    var yFinesse = 0.0; //default initialization

    //

//
//
//  *****   PLOT THIRTEEN / PLOT 13 
//
//
// Plot "thirteen": synthetic spectrum 
    if (ifShowLine === true) {

        var plotRow = 2;
        var plotCol = 0;
        var thisXAxisLength = 1200;
        var thisPanelWidth = 1350;
//Triple wide:
        var minXData = 1.0e7 * specSynLams[0];
        var maxXData = 1.0e7 * specSynLams[numSpecSyn - 1];

        var xAxisName = "<em>&#955</em> (nm)";
        //now done above var norm = 1.0e15; // y-axis normalization
        var minYData = 0.0;
        var minMaxVals = minMax2(specSynFlux);
        var maxYData = 2.0 * specSynFlux[0][minMaxVals[1]];
        //var maxYData = 10.0;
        //var yAxisName = "<span title='Normalized flux'><a href='http://en.wikipedia.org/wiki/Spectral_flux_density' target='_blank'> <em>F</em><sub>&#955</sub> / <em>B</em><sub>&#955</sub></a></span>";
        var yAxisName = "<span title='Normalized flux'><a href='http://en.wikipedia.org/wiki/Spectral_flux_density' target='_blank'> <em>F</em><sub>&#955</sub> / <em>F</em><sup>c</sup><sub>&#955</sub></a></span>";

        var panelOrigin = washer(plotRow, plotCol, thisPanelWidth, wColor, plotThirteenId, cnvsThirteenId);
        panelX = panelOrigin[0];
        panelY = panelOrigin[1];
        cnvsThirteenCtx.fillStyle = wColor;
        cnvsThirteenCtx.fillRect(0, 0, thisPanelWidth, panelHeight);

        var fineness = "hyperfine";
        //var fineness = "fine";
        var xAxisParams = XAxis(panelX, panelY, thisXAxisLength,  
                minXData, maxXData, xAxisName, fineness,
                plotThirteenId, cnvsThirteenCtx);

        var yAxisParams = YAxis(panelX, panelY,
                minYData, maxYData, yAxisName,
                plotThirteenId, cnvsThirteenCtx);
        //xOffset = xAxisParams[0];
        //yOffset = xAxisParams[4];
        var rangeXData = xAxisParams[1];
        var deltaXData = xAxisParams[2];
        var deltaXPxl = xAxisParams[3];
        var rangeYData = yAxisParams[1];
        var deltaYData = yAxisParams[2];
        var deltaYPxl = yAxisParams[3];
        var xLowerYOffset = xAxisParams[5];
        minXData = xAxisParams[6]; //updated value
        minYData = yAxisParams[6]; //updated value
        maxXData = xAxisParams[7]; //updated value
        maxYData = yAxisParams[7]; //updated value        
        //
        // Add legend annotation:

        titleX = panelX + titleOffsetX;
        titleY = panelY + titleOffsetY;
        txtPrint("<span style='font-size:small'>Spectrum synthesis: " 
                 + lambdaStart + " < &#955 < " + lambdaStop + " nm, "
                 + "Min log<sub>10</sub><em>&#954</em><sub>l</sub>/<em>&#954</em><sub>c</sub> = " + lineThresh + ", "
                 + " " + numGaussLines + " lines included.  <br /> "
                 + " <a href='InputData/gsLineList.dat' target='_blank'>View the ascii line list</a></span>",   
                titleOffsetX, titleOffsetY, lineColor, plotThirteenId);
        txtPrint("<span style='font-size:small; color:blue'><a href='http://en.wikipedia.org/wiki/Spectral_energy_distribution' target='_blank'>\n\
     Normalized spectrum synthesis region</a></span>",
                titleOffsetX+600, titleOffsetY, lineColor, plotThirteenId);
        txtPrint("<span style='font-size:small; color:blue'> <em>v</em><sub>Rot</sub>=" + rotV + " km s<sup>-1</sup>"
                  + " <em>i</em><sub>Rot</sub>=" + rotI + "<sup>o</sup>"
                  + " <em>v</em><sub>Macro</sub>=" + macroV + " km s<sup>-1</sup></span>",
                titleOffsetX+1100, titleOffsetY, lineColor, plotThirteenId);

// Equivalent width:
    roundNum = Wlambda.toFixed(2);
    txtPrint("<span title='Equivalent width of total line absorption in synthesis region'>\n\
<a href='http://en.wikipedia.org/wiki/Equivalent_width' target='_blank'>Total W<sub><em>&#955</em></sub></a>: \n\
</span>"
            + roundNum
            + " <span title='picometers'>\n\
<a href='http://en.wikipedia.org/wiki/Picometre' target='_blank'>pm</a>\n\
</span>",
           titleOffsetX + 600, titleOffsetY+35, lineColor, plotThirteenId);

        var dSize = 1;
        opac = 1;

//Does Guasian filter fall within spectrum synthesis region:
        var plotFilt = false;
        var numGauss = gaussFilter[0].length;
        if ( ( (gaussFilter[0][0] > specSynLams[0]) && (gaussFilter[0][0] < specSynLams[numSpecSyn-1]) )
          || ( (gaussFilter[0][numGauss-1] > specSynLams[0]) && (gaussFilter[0][numGauss-1] < specSynLams[numSpecSyn-1]) ) ){
          //    console.log("plotFilt condition met");
              plotFilt = true;
        } 
//Interpolate Gaussian filter used for monochromatic image onto synthetic spectrum wavelength
//grid for overplotting:
          var newFilter = [];
        if (plotFilt == true){ 
           newFilter = interpolV(gaussFilter[1], gaussFilter[0], specSynLams);
        }

        var lambdanm = 1.0e7 * specSynLams[0];
        var xTickPosCnvs = thisXAxisLength * (lambdanm - minXData) / (rangeXData); // pixels
        var lastXShiftCnvs = xAxisXCnvs + xTickPosCnvs;
        var yTickPosCnvs = yAxisLength * ((specSynFlux[0][0] / norm) - minYData) / rangeYData;
        // vertical position in pixels - data values increase upward:
        var lastYShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;
        var xShiftCnvs, yShiftCnvs, yShiftFiltCnvs;
//Gaussian filter:
        var yTickPosFiltCnvs; 
        var lastYShiftFiltCnvs; 
        if (plotFilt == true){ 
          yTickPosFiltCnvs = yAxisLength * ((newFilter[0]) - minYData) / rangeYData;
          lastYShiftFiltCnvs = (yAxisYCnvs + yAxisLength) - yTickPosFiltCnvs;
        }
        for (var i = 1; i < numSpecSyn; i++) {


            lambdanm = 1.0e7 * specSynLams[i]; //cm to nm //linear
            ii = 1.0 * i;
            xTickPosCnvs = thisXAxisLength * (lambdanm - minXData) / (rangeXData); // pixels   //linear

            // horizontal position in pixels - data values increase rightward:
            xShiftCnvs = xAxisXCnvs + xTickPosCnvs;
            xShiftCnvs = Math.floor(xShiftCnvs);

            yTickPosCnvs = yAxisLength * (specSynFlux[0][i] - minYData) / rangeYData;
            //console.log("i " + i + " 1.0e7 * specSynLams[i] " + 1.0e7 * specSynLams[i] + " specSynFlux[0][i] " + specSynFlux[0][i]);
            // vertical position in pixels - data values increase upward:
            yShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;
            yShiftCnvs = Math.floor(yShiftCnvs);

//Gaussian filter:
        if (plotFilt == true){ 
            yTickPosFiltCnvs = yAxisLength * (newFilter[i] - minYData) / rangeYData;
            yShiftFiltCnvs = (yAxisYCnvs + yAxisLength) - yTickPosFiltCnvs;
            yShiftFiltCnvs = Math.floor(yShiftFiltCnvs);
        }
//            plotPnt(xShift, yShift, r255, g255, b255, opac, dSize, plotThirteenId);

//plot points
          //  cnvsThirteenCtx.beginPath();
          //  cnvsThirteenCtx.arc(xShiftCnvs, yShiftCnvs, dSize, 0, 2*Math.PI);
          //  RGBHex = colHex(r255, g255, b255);
          //  cnvsThirteenCtx.strokeStyle = RGBHex;
          //  cnvsThirteenCtx.stroke();
//line plot - spectrum:
            cnvsThirteenCtx.beginPath();
            RGBHex = colHex(0, 0, 0);
            cnvsThirteenCtx.strokeStyle=RGBHex; 
            cnvsThirteenCtx.moveTo(lastXShiftCnvs, lastYShiftCnvs);
            cnvsThirteenCtx.lineTo(xShiftCnvs, yShiftCnvs);
            cnvsThirteenCtx.stroke(); 
        if (plotFilt == true){ 
// Gaussian filter:
            cnvsThirteenCtx.beginPath();
            RGBHex = colHex(255, 0, 0);
            cnvsThirteenCtx.strokeStyle=RGBHex; 
            cnvsThirteenCtx.moveTo(lastXShiftCnvs, lastYShiftFiltCnvs);
            cnvsThirteenCtx.lineTo(xShiftCnvs, yShiftFiltCnvs);
            cnvsThirteenCtx.stroke();  
        }
            lastXShiftCnvs = xShiftCnvs;
            lastYShiftCnvs = yShiftCnvs;
            lastYShiftFiltCnvs = yShiftFiltCnvs;
        }
    


//Spectral line labels and pointers:
        r255 = 0;
        g255 = 0;
        b255 = 0;
        barWidth = 2.0;
        barHeight = 20; //initialize
        RGBHex = "#000000"; //black
        yFinesse = -160;
        var thisYPos = xAxisYCnvs + yFinesse;
        //
        var barFinesse = 60;
        for (var i = 0; i < numGaussLines; i++) {

            if ((i % 4) === 0) {
                yPos = thisYPos - 5;
                barHeight = 30;
                barFinesse = 60;
            } else if ((i % 4) === 1) {
                yPos = thisYPos + 15;
                barHeight = 10;
                barFinesse = 80;
            } else if ((i % 4) === 2) {
                yPos = thisYPos - 25;
                barHeight = 30;
                barFinesse = 60;
            } else {
                yPos = thisYPos + 35;
                barHeight = 10;
                barFinesse = 80;
            }

            xPos = thisXAxisLength * (listLams[i] - minXData) / (maxXData - minXData);
            xPos = xPos - 5; // finesse

            nameLbl = "<span style='font-size: xx-small'>" + listElements[i] + " " + listStages[i] + "</span>";
            lamLblNum = listLams[i].toPrecision(6);
            lamLblStr = lamLblNum.toString(10);
            lamLbl = "<span style='font-size: xx-small'>" + lamLblStr + "</span>";
            //RGBHex = colHex(r255, g255, b255);
            txtPrint(nameLbl, xPos + xAxisXCnvs, (yPos - 10), RGBHex, plotThirteenId);
            txtPrint(lamLbl, xPos + xAxisXCnvs, yPos, RGBHex, plotThirteenId);
            xShiftDum = YBar(listLams[i], minXData, maxXData, thisXAxisLength, barWidth, barHeight,
                    barFinesse, RGBHex, plotThirteenId, cnvsThirteenCtx);
        }
           //monochromatic disk lambda
            barFinesse = yAxisYCnvs;
            barHeight = 18;
            barWidth = 2;
            RGBHex = "#FF0000";
            if ( (diskLambda > lambdaStart) && (diskLambda < lambdaStop) ){
                 xShiftDum = YBar(diskLambda, minXData, maxXData, thisXAxisLength,
                               barWidth, barHeight,
                               barFinesse-60, RGBHex, plotThirteenId, cnvsThirteenCtx);
                 txtPrint("<span style='font-size:xx-small'>Filter</span>",
                       xShiftDum, yAxisYCnvs, RGBHex, plotThirteenId);
            }

}  //ifShowLine condition

//
//  *****   PLOT SEVEN / PLOT 7
//
//

// Plot seven - image of limb-darkened and limb-colored WHITE LIGHT stellar disk
//

        var plotRow = 0;
        var plotCol = 0;

        var panelOrigin = washer(plotRow, plotCol, panelWidth, wColor, plotSevenId, cnvsSevenId);
        panelX = panelOrigin[0];
        panelY = panelOrigin[1];
        cnvsSevenCtx.fillStyle = wColor;
        cnvsSevenCtx.fillRect(0, 0, panelWidth, panelHeight);

        var thet1, thet2;
        var thet3;

        titleX = panelX + titleOffsetX;
        titleY = panelY + titleOffsetY;
        txtPrint("<span style='font-size:normal; color:blue'><a href='http://en.wikipedia.org/wiki/Limb_darkening' target='_blank'>White light disk</a></span> <br />\n\
     <span style='font-size:small'>(Logarithmic radius) </span>",
                titleOffsetX, titleOffsetY, lineColor, plotSevenId);
            txtPrint("<span style='font-size:normal; color:black'><em>&#952</em> = </span>",
                150 + titleOffsetX, titleOffsetY, lineColor, plotSevenId);

// Adjust position to center star:
// Radius is really the *diameter* of the symbol
            var yCenterCnvs = panelHeight / 2; 
            var xCenterCnvs = panelWidth / 2; 
            yCenterCnvs = Math.floor(yCenterCnvs);
            xCenterCnvs = Math.floor(xCenterCnvs);
        //  Loop over limb darkening sub-disks - largest to smallest
        for (var i = numThetas - 1; i >= 1; i--) {

            ii = 1.0 * i;
            // LTE Eddington-Barbier limb darkening: I(Tau=0, cos(theta)=t) = B(T(Tau=t))
            var cosFctr = cosTheta[1][i];
            var radiusPxICnvs = Math.ceil(radiusPx * Math.sin(Math.acos(cosFctr)));
            var cosFctrNext = cosTheta[1][i-1];
            var radiusPxICnvsNext = Math.ceil(radiusPx * Math.sin(Math.acos(cosFctrNext)));

            rrI = Math.ceil(brightScale * (bandIntens[4][i] / bvr) / rgbVega[0]); // / vegaBVR[2]);
            ggI = Math.ceil(brightScale * (bandIntens[3][i] / bvr) / rgbVega[1]); // / vegaBVR[1]);
            bbI = Math.ceil(brightScale * (bandIntens[2][i] / bvr) / rgbVega[2]); // / vegaBVR[0]);
            var rrINext = Math.ceil(brightScale * (bandIntens[4][i-1] / bvr) / rgbVega[0]); // / vegaBVR[2]);
            var ggINext = Math.ceil(brightScale * (bandIntens[3][i-1] / bvr) / rgbVega[1]); // / vegaBVR[1]);
            var bbINext = Math.ceil(brightScale * (bandIntens[2][i-1] / bvr) / rgbVega[2]); // / vegaBVR[0]);

            var RGBHex = colHex(rrI, ggI, bbI);
            var RGBHexNext = colHex(rrINext, ggINext, bbINext);
            cnvsSevenCtx.beginPath();
            //cnvsSevenCtx.strokeStyle = RGBHex;
            var grd=cnvsSevenCtx.createRadialGradient(xCenterCnvs, yCenterCnvs, radiusPxICnvs,
                      xCenterCnvs, yCenterCnvs, radiusPxICnvsNext);
            grd.addColorStop(0, RGBHex);
            grd.addColorStop(1, RGBHexNext);
//            cnvsSevenCtx.fillStyle=RGBHex;
            cnvsSevenCtx.fillStyle = grd;
            cnvsSevenCtx.arc(xCenterCnvs, yCenterCnvs, radiusPxICnvs, 0, 2*Math.PI);
            //cnvsSevenCtx.stroke();
            cnvsSevenCtx.fill();
            //
            //Angle indicators
            if ((i % 2) === 0) {
                thet1 = 180.0 * Math.acos(cosTheta[1][i]) / Math.PI;
                thet2 = thet1.toPrecision(2);
                thet3 = thet2.toString(10);
                txtPrint("<span style='font-size:small; background-color:#888888'>" + thet3 + "</span>",
                        150 + titleOffsetX + (i + 2) * 10, titleOffsetY, RGBHex, plotSevenId);
            }
//
        }


//
//  *****   PLOT TWELVE / PLOT 12
//
//

// Plot twelve - image of limb-darkened and limb-colored TUNABLE MONOCHROMATIC stellar disk


        var plotRow = 1;
        var plotCol = 0;

//radius parameters in pixel all done above now:
//        var radiusScale = 20; //solar_radii-to-pixels!
//        var logScale = 100; //amplification factor for log pixels
//        // 
//        // Star radius in pixels:
//        //    var radiusPx = (radiusScale * radius);  //linear radius
//        var radiusPx = logScale * logTen(radiusScale * radius); //logarithmic radius
//        radiusPx = Math.ceil(radiusPx);
        var thet1, thet2;
        var thet3;
        var panelOrigin = washer(plotRow, plotCol, panelWidth, wColor, plotTwelveId, cnvsTwelveId);
        panelX = panelOrigin[0];
        panelY = panelOrigin[1];
        cnvsTwelveCtx.fillStyle = wColor;
        cnvsTwelveCtx.fillRect(0, 0, panelWidth, panelHeight);
        // Add title annotation:

        //var titleYPos = xLowerYOffset - 1.15 * yRange;
        //var titleXPos = 1.02 * xOffset;

        titleX = panelX + titleOffsetX;
        titleY = panelY + titleOffsetY;
        txtPrint("<span style='font-size:normal; color:blue'><a href='http://en.wikipedia.org/wiki/Limb_darkening' target='_blank'>Gaussian filter</a></span><span style='font-size:small'> &#955 = " + diskLambda + " nm</span> </br>\n\
     <span style='font-size:small'>(Logarithmic radius) </span>",
                titleOffsetX, titleOffsetY + 20, lineColor, plotTwelveId);
        txtPrint("<span style='font-size:normal; color:black'><em>&#952</em> = </span>",
                270 + titleOffsetX, titleOffsetY + 20, lineColor, plotTwelveId);
        var ilLam0 = lamPoint(numMaster, masterLams, 1.0e-7 * diskLambda);
        var lambdanm = masterLams[ilLam0] * 1.0e7; //cm to nm
        //console.log("PLOT TWELVE: ilLam0=" + ilLam0 + " lambdanm " + lambdanm);
        var minZData = 0.0;
        //var maxZData = masterIntens[ilLam0][0] / norm;
        var maxZData = tuneBandIntens[0] / norm;
        var rangeZData = maxZData - minZData;

// Adjust position to center star:
// Radius is really the *diameter* of the symbol
            var yCenterCnvs = panelHeight / 2; 
            var xCenterCnvs = panelWidth / 2; 
            yCenterCnvs = Math.floor(yCenterCnvs);
            xCenterCnvs = Math.floor(xCenterCnvs);
        //  Loop over limb darkening sub-disks - largest to smallest
        for (var i = numThetas - 1; i >= 1; i--) {

            ii = 1.0 * i;
            // LTE Eddington-Barbier limb darkening: I(Tau=0, cos(theta)=t) = B(T(Tau=t))
            var cosFctr = cosTheta[1][i];
            var radiusPxICnvs = Math.ceil(radiusPx * Math.sin(Math.acos(cosFctr)));
            var cosFctrNext = cosTheta[1][i-1];
            var radiusPxICnvsNext = Math.ceil(radiusPx * Math.sin(Math.acos(cosFctrNext)));
            //logarithmic z:
            //zLevel = (logE * masterIntens[1lLam0][i] - minZData) / rangeZData;
//linear z:


//            var zLevel = ((masterIntens[ilLam0][i] / norm) - minZData) / rangeZData;
//            var zLevelNext = ((masterIntens[ilLam0][i-1] / norm) - minZData) / rangeZData;
            var zLevel = ((tuneBandIntens[i] / norm) - minZData) / rangeZData;
            var zLevelNext = ((tuneBandIntens[i-1] / norm) - minZData) / rangeZData;
            //console.log("lambdanm " + lambdanm + " zLevel " + zLevel);

            RGBHex = lambdaToRGB(lambdanm, zLevel);
            RGBHexNext = lambdaToRGB(lambdanm, zLevelNext);

            cnvsTwelveCtx.beginPath();
            //cnvsSevenCtx.strokeStyle = RGBHex;
            var grd=cnvsTwelveCtx.createRadialGradient(xCenterCnvs, yCenterCnvs, radiusPxICnvs,
                      xCenterCnvs, yCenterCnvs, radiusPxICnvsNext);
            grd.addColorStop(0, RGBHex);
            grd.addColorStop(1, RGBHexNext);
//            cnvsSevenCtx.fillStyle=RGBHex;
            cnvsTwelveCtx.fillStyle = grd;
            cnvsTwelveCtx.arc(xCenterCnvs, yCenterCnvs, radiusPxICnvs, 0, 2*Math.PI);
            //cnvsSevenCtx.stroke();
            cnvsTwelveCtx.fill();
            //
            //Angle indicators
            if ((i % 2) === 0) {
                thet1 = 180.0 * Math.acos(cosTheta[1][i]) / Math.PI;
                thet2 = thet1.toPrecision(2);
                thet3 = thet2.toString(10);
                txtPrint("<span style='font-size:small; background-color:#888888'>" + thet3 + "</span>",
                       270 + titleOffsetX + (i + 2) * 10, titleOffsetY + 20, RGBHex, plotTwelveId);
            }
//
        }

    //
    //
    //  *****   PLOT TEN / PLOT 10
    //
    //
    // Plot Ten: Spectrum image


        var plotRow = 0;
        var plotCol = 1;

        var minXData = 380.0; // (nm) blue
        var maxXData = 680.0; // (nm) red
        //var midXData = (minXData + maxXData) / 2.0;  // "green"


        //var xAxisName = "&#955 (nm)";
        var xAxisName = " ";
        //now done above var norm = 1.0e15; // y-axis normalization
        //var minYData = 0.0;
        // iLamMax established in PLOT TWO above:
        //var maxYData = masterFlux[0][iLamMax] / norm;
        // y-axis is just the arbitrary vertical scale - has no data significance
        var minYData = 0.0;
        var maxYData = 1.0;
        //
        //z-axiz (out of the screen) is really intensity level
        //Logarithmic z:
        //var minZData = 12.0;
        //var maxZData = logE * masterFlux[1][iLamMax];
        //Linear z:
        var ilLam0 = lamPoint(numMaster, masterLams, 1.0e-7 * minXData);
        var ilLam1 = lamPoint(numMaster, masterLams, 1.0e-7 * maxXData);
        var minZData = 0.0;
        var maxZData = masterFlux[0][iLamMax] / norm;
        //Make sure spectrum is normalized to brightest displayed lambda haveing level =255
        // even when lambda_Max is outside displayed lambda range:
        if (iLamMax < ilLam0) {
            maxZData = masterFlux[0][ilLam0] / norm;
        }
        if (iLamMax > ilLam1) {
            maxZData = masterFlux[0][ilLam1] / norm;
        }
        var rangeZData = maxZData - minZData;
        //var yAxisName = "<span title='Monochromatic surface flux'><a href='http://en.wikipedia.org/wiki/Spectral_flux_density' target='_blank'>Log<sub>10</sub> <em>F</em><sub>&#955</sub> <br /> ergs s<sup>-1</sup> cm<sup>-3</sup></a></span>";

        
        var fineness = "normal";
        var panelOrigin = washer(plotRow, plotCol, panelWidth, wColor, plotTenId, cnvsTenId);
        panelX = panelOrigin[0];
        panelY = panelOrigin[1];
        cnvsTenCtx.fillStyle = wColor;
        cnvsTenCtx.fillRect(0, 0, panelWidth, panelHeight);
        var xAxisParams = XAxis(panelX, panelY, xAxisLength,
                minXData, maxXData, xAxisName, fineness,
                plotTenId, cnvsTenCtx);

        //xOffset = xAxisParams[0];
        //yOffset = xAxisParams[4];
        var rangeXData = xAxisParams[1];
        var deltaXData = xAxisParams[2];
        var deltaXPxl = xAxisParams[3];
        minXData = xAxisParams[6]; //updated value
        maxXData = xAxisParams[7]; //updated value
        //

        //var rangeYData = yAxisParams[1];
        //var deltaYData = yAxisParams[2];
        //var deltaYPxl = yAxisParams[3];
        //var xLowerYOffset = xAxisParams[5];
        //minXData = xAxisParams[6];  //updated value
        //minYData = yAxisParams[6];  //updated value


        //txtPrint(" ", legendXPos, legendYPos + 10, zeroInt, zeroInt, zeroInt, plotTenId);
        titleX = panelX + titleOffsetX;
        titleY = panelY + titleOffsetY;
        txtPrint("<span style='font-size:normal; color:blue'><a href='https://en.wikipedia.org/wiki/Visible_spectrum' target='_blank'>\n\
     Visual spectrum</a></span>",
                titleOffsetX, titleOffsetY, lineColor, plotTenId);
        var xShift, zShift, xShiftDum, zLevel;
        var RGBHex; //, r255, g255, b255;
        var rangeXData = 1.0e7 * (masterLams[ilLam1] - masterLams[ilLam0]);
        //console.log("minXData " + minXData + " ilLam0 " + ilLam0 + " masterLams[ilLam0] " + masterLams[ilLam0]);

        var barWidth, xBarShift0, xBarShift1, xPos, yPos, nameLbl, lamLbl, lamLblStr, lamLblNum;
        var barHeight = 75.0;

//We can only palce vertical bars by setting marginleft, so search *AHEAD* in wavelength to find width
// of *CURRENT* bar.
        var lambdanm = masterLams[ilLam0] * 1.0e7; //cm to nm
        //console.log("ilLam0 " + ilLam0 + " ilLam1 " + ilLam1);
        yFinesse = -160;
        var thisYPos = xAxisYCnvs + yFinesse;
        for (var i = ilLam0 + 1; i < ilLam1; i++) {

            var nextLambdanm = masterLams[i] * 1.0e7; //cm to nm
            //logLambdanm = 7.0 + logTen(masterLams[i]);

            //barWidth = Math.max(1, Math.ceil(xRange * (lambdanm - lastLambdanm) / rangeXData));
            //barWidth = xRange * (nextLambdanm - lambdanm) / rangeXData;
            //Try calculating the barWidth (device coordinates) in *EXACTLY* the same way as YBar calcualtes its x-position:
            //xBarShift0 = xRange * (lambdanm - minXData) / (maxXData - minXData);
            //xBarShift1 = xRange * (nextLambdanm - minXData) / (maxXData - minXData);
            xBarShift0 = xAxisLength * (lambdanm - minXData) / (maxXData - minXData);
            xBarShift1 = xAxisLength * (nextLambdanm - minXData) / (maxXData - minXData);
            barWidth = xBarShift1 - xBarShift0; //in device pixels

            if (barWidth > 0.5) {

                barWidth = barWidth + 1.0;
//logarithmic z:
                //zLevel = (logE * masterFlux[1][i] - minZData) / rangeZData;
//linear z:


                zLevel = ((masterFlux[0][i] / norm) - minZData) / rangeZData;
                //console.log("lambdanm " + lambdanm + " zLevel " + zLevel);

            var nextRGBHex = lambdaToRGB(lambdanm, zLevel);

        var xTickPosCnvs = xAxisLength * (lambdanm - minXData) / (maxXData - minXData);
        var xShiftCnvs = xAxisXCnvs + xTickPosCnvs;
        xShiftCnvs = Math.floor(xShiftCnvs);
        var grd = cnvsTenCtx.createLinearGradient(xShiftCnvs, thisYPos, xShiftCnvs+barWidth, thisYPos);
        grd.addColorStop(0, RGBHex);
        grd.addColorStop(1, nextRGBHex);
        cnvsTenCtx.fillStyle = grd;

        cnvsTenCtx.fillRect(xShiftCnvs, thisYPos, barWidth, barHeight);
                //console.log("lambdanm " + lambdanm + " nextLambdanm " + nextLambdanm + " xShiftDum " + xShiftDum + " barWidth " + barWidth);

                lambdanm = nextLambdanm;
                RGBHex = nextRGBHex;
            }  //barWidth condition

        }  // i loop (wavelength)


//
//
//  *****   PLOT ELEVEN / PLOT 11
//
//
// Plot Eleven: Life Zone


        var plotRow = 0;
        var plotCol = 2;

        // Calculation of steam line and ice line:

        //Assuming liquid salt-free water at one atmospheric pressure is necessary:
        var steamTemp = 373.0; // K = 100 C
        var iceTemp = 273.0; //K = 0 C

        steamTemp = steamTemp - greenHouse;
        iceTemp = iceTemp - greenHouse;
        var logSteamLine, logIceLine;
        var au = 1.4960e13; // 1 AU in cm
        var rSun = 6.955e10; // solar radii to cm
        //Steam line:
        //Set steamTemp equal to planetary surface temp and find distance that balances stellar irradiance 
        //absorbed by planetary cross-section with planet's bolometric thermal emission:
        //Everything in solar units -> distance, d, in solar radii
        logSteamLine = 2.0 * (Math.log(teff) - Math.log(steamTemp)) + logRadius + 0.5 * Math.log(1.0 - albedo);
        //now the same for the ice line:
        logIceLine = 2.0 * (Math.log(teff) - Math.log(iceTemp)) + logRadius + 0.5 * Math.log(1.0 - albedo);
        var iceLineAU = Math.exp(logIceLine) * rSun / au;
        var steamLineAU = Math.exp(logSteamLine) * rSun / au;
        iceLineAU = iceLineAU.toPrecision(3);
        steamLineAU = steamLineAU.toPrecision(3);

        // Convert solar radii to pixels:

        var radiusScale = 20; //solar_radii-to-pixels!
        var logScale = 20; //amplification factor for log pixels

        // 
        // Star radius in pixels:

        //    var radiusPx = (radiusScale * radius);  //linear radius
        var radiusPx = logScale * logTen(radiusScale * radius); //logarithmic radius

        radiusPx = Math.ceil(radiusPx);
        var radiusPxSteam = logScale * logTen(radiusScale * radius * Math.exp(logSteamLine));
        radiusPxSteam = Math.ceil(radiusPxSteam);
        var radiusPxIce = logScale * logTen(radiusScale * radius * Math.exp(logIceLine));
        radiusPxIce = Math.ceil(radiusPxIce);
        // Key raii in order of *DECREASING* size (important!):
        var radii = [radiusPxIce + 2, radiusPxIce, radiusPxSteam, radiusPxSteam - 2, radiusPx];
        //
        rrI = saveRGB[0];
        ggI = saveRGB[1];
        bbI = saveRGB[2];
        var starRGBHex = "rgb(" + rrI + "," + ggI + "," + bbI + ")";
        var colors = ["#0000FF", "#00FF88", "#FF0000", wColor, starRGBHex];
        var numZone = radii.length;
        //var titleYPos = xLowerYOffset - yRange + 40;
        //var cnvsCtx = washer(xOffset - xRange / 2, yOffset, wColor, plotElevenId);
        var panelOrigin = washer(plotRow, plotCol, panelWidth, wColor, plotElevenId, cnvsElevenId);
        panelX = panelOrigin[0];
        panelY = panelOrigin[1];
        cnvsElevenCtx.fillStyle = wColor;
        cnvsElevenCtx.fillRect(0, 0, panelWidth, panelHeight);
        // Add title annotation:

        titleX = panelX + titleOffsetX;
        titleY = panelY + titleOffsetY;

        txtPrint("<span style='font-size:normal; color:blue' title='Assumes liquid salt-free water at one Earth atmosphere pressure needed for life'><a href='https://en.wikipedia.org/wiki/Circumstellar_habitable_zone' target='_blank'>Life zone for habitable planets</a></span><br />\n\
     <span style='font-size:small'>(Logarithmic radius)</span>",
                titleOffsetX, titleOffsetY, lineColor, plotElevenId);
        var legendY = titleOffsetY;
        var legendX = titleOffsetX + 320;
        txtPrint("<span style='font-size:small'>"
                + " <span style='color:#FF0000'>Steam line</span> " + steamLineAU + " <a href='https://en.wikipedia.org/wiki/Astronomical_unit' title='1 AU = Earths average distance from center of Sun'> AU</a><br /> "
                + " <span style='color:#00FF88'><strong>Life zone</strong></span><br /> "
                + " <span style='color:#0000FF'>Ice line</span> " + iceLineAU + " <a href='https://en.wikipedia.org/wiki/Astronomical_unit' title='1 AU = Earths average distance from center of Sun'> AU</a>"
                + " </span>",
                legendX, legendY, lineColor, plotElevenId);
        //Get the Vega-calibrated colors from the intensity spectrum of each theta annulus:    
        // moved earlier var intcolors = iColors(lambdaScale, intens, numDeps, numThetas, numLams, tauRos, temp);

        //  Loop over radial zones - largest to smallest
        for (var i = 0; i < radii.length; i++) {

            var radiusStr = numToPxStrng(radii[i]);
            // Adjust position to center star:
            // Radius is really the *diameter* of the symbol

// Adjust position to center star:
// Radius is really the *diameter* of the symbol
            var yCenterCnvs = panelHeight / 2; 
            var xCenterCnvs = panelWidth / 2; 
            yCenterCnvs = Math.floor(yCenterCnvs);
            xCenterCnvs = Math.floor(xCenterCnvs);

            cnvsElevenCtx.beginPath();
            //cnvsSevenCtx.strokeStyle = RGBHex;
            //var grd=cnvsElevenCtx.createRadialGradient(xOffsetICnvs, xLowerYOffsetICnvs, radiusPxICnvs,
            //          xOffsetICnvs, xLowerYOffsetICnvs, radiusPxICnvsNext);
            //grd.addColorStop(0, RGBHex);
            //grd.addColorStop(1, RGBHexNext);
            cnvsElevenCtx.fillStyle=colors[i];
//            cnvsElevenCtx.fillStyle = grd;
            cnvsElevenCtx.arc(xCenterCnvs, yCenterCnvs, radii[i], 0, 2*Math.PI);
            //cnvsSevenCtx.stroke();
            cnvsElevenCtx.fill();
            //
        }  //i loop (thetas)


    //
    //
    //  *****   PLOT NINE / PLOT 9
    //
    //
    // Plot Nine: HRDiagram


        var plotRow = 1;
        var plotCol = 1;
        // WARNING: Teff axis is backwards!!
        var minXData = logTen(100000.0); //K
        var maxXData = logTen(1000.0); //K


        var xAxisName = "<span title='Logarithmic surface temperature of spherical blackbody radiation emitter of equivalent bolometric surface flux, in Kelvins (K)'> \n\
     <a href='http://en.wikipedia.org/wiki/Effective_temperature' target='_blank'>\n\
     Log<sub>10</sub> <em>T</em><sub>eff</sub></a> \n\
     (<a href='http://en.wikipedia.org/wiki/Kelvin' target='_blank'>K</a>)</span>";
        //var numYTicks = 6;
        var minYData = -6.0; //solar luminosities;
        var maxYData = 7.0; //solar luminosities


        var yAxisName = "<span title='Logarithmic Bolometric luminosity'>\n\
     <a href='http://en.wikipedia.org/wiki/Luminosity' target='_blank'>\n\
     Log<sub>10</sub><em>L</em><sub>Bol</sub></a></span><br />  \n\
     <span title='Solar luminosities'>\n\
     <a href='http://en.wikipedia.org/wiki/Solar_luminosity' target='_blank'>\n\
     <em>L</em><sub>Sun</sub></a></span> ";
        //
        var fineness = "fine";
        var panelOrigin = washer(plotRow, plotCol, panelWidth, wColor, plotNineId, cnvsNineId);
        panelX = panelOrigin[0];
        panelY = panelOrigin[1];
        cnvsNineCtx.fillStyle = wColor;
        cnvsNineCtx.fillRect(0, 0, panelWidth, panelHeight);
        var xAxisParams = XAxis(panelX, panelY, xAxisLength,
                minXData, maxXData, xAxisName, fineness,
                plotNineId, cnvsNineCtx);

        var yAxisParams = YAxis(panelX, panelY,
                minYData, maxYData, yAxisName,
                plotNineId, cnvsNineCtx);

        //
//        xOffset = xAxisParams[0];
//        yOffset = yAxisParams[4];
        var rangeXData = xAxisParams[1];
        var deltaXData = xAxisParams[2];
        var deltaXPxl = xAxisParams[3];
        var rangeYData = yAxisParams[1];
        var deltaYData = yAxisParams[2];
        var deltaYPxl = yAxisParams[3];
//        var xLowerYOffset = xAxisParams[5];
        minXData = xAxisParams[6]; //updated value
        minYData = yAxisParams[6]; //updated value
        maxXData = xAxisParams[7]; //updated value
        maxYData = yAxisParams[7]; //updated value     
        //
        titleX = panelX + titleOffsetX;
        titleY = panelY + titleOffsetY;
        txtPrint("<span style='font-size:normal; color:blue'><a href='http://www.ap.smu.ca/~ishort/hrdtest3.html' target='_blank'>H-R Diagram</a></span>",
                titleOffsetX, titleOffsetY, lineColor, plotNineId);
        // *********  Input stellar data

        //Sun

        var sunClass = "G2";
        var sunTeff = 5778;
        // var sunTeff = 10000; //debug test
        var sunB_V = 0.656;
        var sunM_v = 4.83;
        var sunM_Bol = 4.75;
        var sunRad = 1.0;
        // var sunRad = 10.0; //debug test
        var logSunLum = 2.5 * logTen(1.0); //log Suns luminosity in solar luminosities 


        // Carroll & Ostlie, 2nd Ed. , Appendix G:

        //Main sequence

        var msClass = ["O5", "O6", "O7", "O8", "B0", "B1", "B2", "B3", "B5", "B6", "B7", "B8", "B9", "A0", "A1", "A2", "A5", "A8", "F0", "F2", "F5", "F8", "G0", "G2", "G8", "K0", "K1", "K3", "K4", "K5", "K7", "M0", "M1", "M2", "M3", "M4", "M5", "M6", "M7"];
        var msTeffs = [42000, 39500, 37500, 35800, 30000, 25400, 20900, 18800, 15200, 13700, 12500, 11400, 10500, 9800, 9400, 9020, 8190, 7600, 7300, 7050, 6650, 6250, 5940, 5790, 5310, 5150, 4990, 4690, 4540, 4410, 4150, 3840, 3660, 3520, 3400, 3290, 3170, 3030, 2860];
        var msB_V = [-0.33, -0.33, -0.32, -0.32, -0.30, -0.26, -0.24, -0.20, -0.17, -0.15, -0.13, -0.11, -0.07, -0.02, +0.01, +0.05, +0.15, +0.25, +0.30, +0.35, +0.44, +0.52, +0.58, +0.63, +0.74, +0.81, +0.86, +0.96, +1.05, +1.15, +1.33, +1.40, +1.46, +1.49, +1.51, +1.54, +1.64, +1.73, +1.80];
        var msM_v = [-5.1, -5.1, -4.9, -4.6, -3.4, -2.6, -1.6, -1.3, -0.5, -0.1, +0.3, +0.6, +0.8, +1.1, +1.3, +1.5, +2.2, +2.7, +3.0, +3.4, +3.9, +4.3, +4.7, +4.9, +5.6, +5.7, +6.0, +6.5, +6.7, +7.1, +7.8, +8.9, +9.6, 10.4, 11.1, 11.9, 12.8, 13.8, 14.7];
        var msBC = [-4.40, -3.93, -3.68, -3.54, -3.16, -2.70, -2.35, -1.94, -1.46, -1.21, -1.02, -0.80, -0.51, -0.30, -0.23, -0.20, -0.15, -0.10, -0.09, -0.11, -0.14, -0.16, -0.18, -0.20, -0.40, -0.31, -0.37, -0.50, -0.55, -0.72, -1.01, -1.38, -1.62, -1.89, -2.15, -2.38, -2.73, -3.21, -3.46];
        var msMass = [60, 37, 29, 23, 17.5, 13.0, 10.0, 7.6, 5.9, 4.8, 4.3, 3.8, 3.2, 2.9, 2.5, 2.2, 2.0, 1.7, 1.6, 1.5, 1.4, 1.2, 1.05, 0.90, 0.83, 0.79, 0.75, 0.72, 0.69, 0.67, 0.55, 0.51, 0.43, 0.40, 0.33, 0.26, 0.21, 0.13, 0.10];
        //var msRads = [13.4 ,12.2 ,11.0 ,10.0 , 6.7 , 5.2 , 4.1 , 3.8 , 3.2 , 2.9 , 2.7 , 2.5 , 2.3 , 2.2 , 2.1 , 2.0 , 1.8 , 1.5 , 1.4 , 1.3 , 1.2 , 1.1 , 1.06, 1.03, 0.96, 0.93, 0.91, 0.86, 0.83, 0.80, 0.74, 0.63, 0.56, 0.48, 0.41, 0.35, 0.29, 0.24, 0.20];
        //var msM_Bol = [-9.51,-9.04,-8.60,-8.18,-6.54,-5.26,-3.92,-3.26,-1.96,-1.35,-0.77,-0.22,+0.28,+0.75,+1.04,+1.31,+2.02,+2.61,+2.95,+3.27,+3.72,+4.18,+4.50,+4.66,+5.20,+5.39,+5.58,+5.98,+6.19,+6.40,+6.84,+7.52,+7.99,+8.47,+8.97,+9.49,10.1 ,10.6 ,11.3];

        // Main sequence data processing:

        var msNum = msClass.length;
        var msM_Bol = [];
        var logR45 = [];
        var logR = [];
        var msRads = [];
        var msLogLum = [];
        msM_Bol.length = msNum;
        logR45.length = msNum;
        logR.length = msNum;
        msRads.length = msNum;
        msLogLum.length = msNum;
        // Calculate radii in solar radii:
        // For MS stars, do the Luminosity as well

        for (var i = 0; i < msNum; i++) {

            msM_Bol[i] = msM_v[i] + msBC[i];
            var msTeffSol = msTeffs[i] / sunTeff;
            logR45[i] = 2.5 * logSunLum + sunM_Bol - 10.0 * logTen(msTeffSol) - msM_Bol[i];
            logR[i] = logR45[i] / 4.5;
            msRads[i] = Math.exp(Math.LN10 * logR[i]); //No base ten exponentiation in JS!
            var msLogL = (sunM_Bol - msM_Bol[i]) / 2.5;
            // Round log(Lum) to 1 decimal place:
            msLogL = 10.0 * msLogL;
            msLogL = Math.floor(msLogL);
            msLogLum[i] = msLogL / 10.0;
        } // end i loop


// Giants:

        var rgbClass = ["O5", "O6", "O7", "O8", "B0", "B1", "B2", "B3", "B5", "B6", "B7", "B8", "B9", "A0", "A1", "A2", "A5", "A8", "F0", "F2", "F5", "G0", "G2", "G8", "K0", "K1", "K3", "K4", "K5", "K7", "M0", "M1", "M2", "M3", "M4", "M5", "M6"];
        var rgbTeffs = [39400, 37800, 36500, 35000, 29200, 24500, 20200, 18300, 15100, 13800, 12700, 11700, 10900, 10200, 9820, 9460, 8550, 7830, 7400, 7000, 6410, 5470, 5300, 4800, 4660, 4510, 4260, 4150, 4050, 3870, 3690, 3600, 3540, 3480, 3440, 3380, 3330];
        var rgbB_V = [-0.32, -0.32, -0.32, -0.31, -0.29, -0.26, -0.24, -0.20, -0.17, -0.15, -0.13, -0.11, -0.07, -0.03, +0.01, +0.05, +0.15, +0.25, +0.30, +0.35, +0.43, +0.65, +0.77, +0.94, +1.00, +1.07, +1.27, +1.38, +1.50, +1.53, +1.56, +1.58, +1.60, +1.61, +1.62, +1.63, +1.52];
        var rgbM_v = [-5.9, -5.7, -5.6, -5.5, -4.7, -4.1, -3.4, -3.2, -2.3, -1.8, -1.4, -1.0, -0.6, -0.4, -0.2, -0.1, +0.6, +1.0, +1.3, +1.4, +1.5, +1.3, +1.3, +1.0, +1.0, +0.9, +0.8, +0.8, +0.7, +0.4, +0.0, -0.2, -0.4, -0.4, -0.4, -0.4, -0.4];
        var rgbBC = [-4.05, -3.80, -3.58, -3.39, -2.88, -2.43, -2.02, -1.60, -1.30, -1.13, -0.97, -0.82, -0.71, -0.42, -0.29, -0.20, -0.14, -0.10, -0.11, -0.11, -0.14, -0.20, -0.27, -0.42, -0.50, -0.55, -0.76, -0.94, -1.02, -1.17, -1.25, -1.44, -1.62, -1.87, -2.22, -2.48, -2.73];
        //var rgbRads = [18.5,16.8,15.4,14.3,11.4,10.0, 8.6, 8.0, 6.7, 6.1, 5.5, 5.0, 4.5, 4.1, 3.9, 3.7, 3.3, 3.1, 3.2, 3.3, 3.8, 6.0, 6.7, 9.6,10.9,12.5,16.4,18.7,21.4,27.6,39.3,48.6,58.5,69.7,82.0,96.7,16];
        //var rgbM_Bol = [-9.94,-9.55,-9.20,-8.87,-7.58,-6.53,-5.38,-4.78,-3.56,-2.96,-2.38,-1.83,-1.31,-0.83,-0.53,-0.26,+0.44,+0.95,+1.17,+1.31,+1.37,+1.10,+1.00,+0.63,+0.48,+0.32,-0.01,-0.18,-0.36,-0.73,-1.28,-1.64,-1.97,-2.28,-2.57,-2.86,-3.18];

        // RGB sequence data processing:

        var rgbNum = rgbClass.length;
        var rgbM_Bol = [];
        var logR45 = [];
        var logR = [];
        var rgbRads = [];
        var rgbLogLum = [];
        rgbM_Bol.length = rgbNum;
        logR45.length = rgbNum;
        logR.length = rgbNum;
        rgbRads.length = rgbNum;
        // Calculate radii in solar radii:

        for (var i = 0; i < rgbNum; i++) {

            rgbM_Bol[i] = rgbM_v[i] + rgbBC[i];
            var rgbTeffSol = rgbTeffs[i] / sunTeff;
            logR45[i] = 2.5 * logSunLum + sunM_Bol - 10.0 * logTen(rgbTeffSol) - rgbM_Bol[i];
            logR[i] = logR45[i] / 4.5;
            rgbRads[i] = Math.exp(Math.LN10 * logR[i]); //No base ten exponentiation in JS!

            var rgbLogL = (sunM_Bol - rgbM_Bol[i]) / 2.5;
            // Round log(Lum) to 1 decimal place:
            rgbLogL = 10.0 * rgbLogL;
            rgbLogL = Math.floor(rgbLogL);
            rgbLogLum[i] = rgbLogL / 10.0;
        } // end i loop


// No! Too bright for what GrayStar can model!
// //Supergiants:
//
 var sgbClass = ["O5", "O6", "O7", "O8", "B0", "B1", "B2", "B3", "B5", "B6", "B7", "B8", "B9", "A0", "A1", "A2", "A5", "A8", "F0", "F2", "F5", "F8", "G0", "G2", "G8", "K0", "K1", "K3", "K4", "K5", "K7", "M0", "M1", "M2", "M3", "M4", "M5", "M6"];
 var sgbTeffs = [40900, 38500, 36200, 34000, 26200, 21400, 17600, 16000, 13600, 12600, 11800, 11100, 10500, 9980, 9660, 9380, 8610, 7910, 7460, 7030, 6370, 5750, 5370, 5190, 4700, 4550, 4430, 4190, 4090, 3990, 3830, 3620, 3490, 3370, 3210, 3060, 2880, 2710];
 var sgbB_V = [-0.31, -0.31, -0.31, -0.29, -0.23, -0.19, -0.17, -0.13, -0.10, -0.08, -0.05, -0.03, -0.02, -0.01, +0.02, +0.03, +0.09, +0.14, +0.17, +0.23, +0.32, +0.56, +0.76, +0.87, +1.15, +1.24, +1.30, +1.46, +1.53, +1.60, +1.63, +1.67, +1.69, +1.71, +1.69, +1.76, +1.80, +1.86];
  var sgbM_v = [-6.5, -6.5, -6.6, -6.6, -6.9, -6.9, -6.7, -6.7, -6.6, -6.4, -6.3, -6.3, -6.3, -6.3, -6.3, -6.3, -6.3, -6.4, -6.4, -6.4, -6.4, -6.4, -6.3, -6.3, -6.1, -6.1, -6.0, -5.9, -5.8, -5.7, -5.6, -5.8, -5.8, -5.8, -5.5, -5.2, -4.8, -4.9];
 var sgbBC = [-3.87, -3.74, -3.48, -3.35, -2.49, -1.87, -1.58, -1.26, -0.95, -0.88, -0.78, -0.66, -0.52, -0.41, -0.32, -0.28, -0.13, -0.03, -0.01, 0.00, -0.03, -0.09, -0.15, -0.21, -0.42, -0.50, -0.56, -0.75, -0.90, -1.01, -1.20, -1.29, -1.38, -1.62, -2.13, -2.75, -3.47, -3.90];
//  
//  //var sgbRads = [21,  22,  23,  25,  31,  37,  42,  45,  51,  53,  56,  58,  61,  64,  67,  69,  78,  91, 102, 114, 140, 174, 202, 218, 272, 293, 314, 362, 386, 415, 473, 579, 672, 791, 967,1220,1640,2340];
//  //var sgbM_Bol = [-10.4,-10.2,-10.1, -9.9, -9.3, -8.8, -8.2, -7.9, -7.5, -7.3, -7.1, -6.9, -6.8, -6.7, -6.6, -6.5, -6.4, -6.4, -6.4, -6.4, -6.4, -6.4, -6.4, -6.4, -6.5, -6.5, -6.5, -6.6, -6.7, -6.7, -6.8, -7.0, -7.2, -7.4, -7.6, -7.9, -8.3, -8.8];
// 
  // SGB sequence data processing:
 
  var sgbNum = sgbClass.length;
 
  var sgbM_Bol = [];
  var logR45 = [];
  var logR = [];
  var sgbRads = [];
  var sgbLogLum = [];
  
 sgbM_Bol.length = sgbNum;
  logR45.length = sgbNum;
  logR.length = sgbNum;
   sgbRads.length = sgbNum;
  
   
  // Calculate radii in solar radii:
   
  for (var i = 0; i < sgbNum; i++) {
  
 sgbM_Bol[i] = sgbM_v[i] + sgbBC[i];
  var sgbTeffSol = sgbTeffs[i] / sunTeff;
  
  logR45[i] = 2.5 * logSunLum + sunM_Bol - 10.0 * logTen(sgbTeffSol) - sgbM_Bol[i];
  logR[i] = logR45[i] / 4.5;
  sgbRads[i] = Math.exp(Math.LN10 * logR[i]);  //No base ten exponentiation in JS!
  
  var sgbLogL = (sunM_Bol - sgbM_Bol[i]) / 2.5;
  // Round log(Lum) to 1 decimal place:
  sgbLogL = 10.0 * sgbLogL;
  sgbLogL = Math.floor(sgbLogL);
  sgbLogLum[i] = sgbLogL / 10.0;
  
  } // end i loop
 

//Data loops - plot the result!

//MS stars

        var dSizeCnvs = 2.0; //plot point size
        var opac = 0.7; //opacity
        // RGB color
        var r255 = 50;
        var g255 = 50;
        var b255 = 50; //dark gray
        var RGBHex = colHex(r255, r255, r255);

        var ii;
        //for (var i = 5; i < msNum - 3; i++) {
        for (var i = 4; i < msNum - 1; i++) {

            ii = 1.0 * i;
            var xTickPosCnvs = xAxisLength * (logTen(msTeffs[i]) - minXData) / rangeXData; // pixels   

            // horizontal position in pixels - data values increase rightward:
            var xShiftCnvs = xAxisXCnvs + xTickPosCnvs;
            xShiftCnvs = Math.floor(xShiftCnvs);

            var yTickPosCnvs = yAxisLength * (msLogLum[i] - minYData) / rangeYData;
            // vertical position in pixels - data values increase upward:
            var yShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;
            yShiftCnvs = Math.floor(yShiftCnvs);

            cnvsNineCtx.fillStyle = RGBHex;
            cnvsNineCtx.strokeStyle = RGBHex;
            cnvsNineCtx.beginPath();
            cnvsNineCtx.arc(xShiftCnvs, yShiftCnvs, dSizeCnvs, 0, 2*Math.PI);
            cnvsNineCtx.stroke();
        }


//RGB stars

// RGB color
        var r255 = 100;
        var g255 = 100;
        var b255 = 100; //gray
        var RGBHex = colHex(r255, r255, r255);

        var ii;
        //for (var i = 4; i < rgbNum - 2; i++) {
        for (var i = 3; i < rgbNum - 1; i++) {

            ii = 1.0 * i;
            var xTickPosCnvs = xAxisLength * (logTen(rgbTeffs[i]) - minXData) / rangeXData; // pixels   

            // horizontal position in pixels - data values increase rightward:
            var xShiftCnvs = xAxisXCnvs + xTickPosCnvs;
            xShiftCnvs = Math.floor(xShiftCnvs);

            var yTickPosCnvs = yAxisLength * (rgbLogLum[i] - minYData) / rangeYData;
            // vertical position in pixels - data values increase upward:
            var yShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;
            yShiftCnvs = Math.floor(yShiftCnvs);

            cnvsNineCtx.fillStyle = RGBHex;
            cnvsNineCtx.strokeStyle = RGBHex;
            cnvsNineCtx.beginPath();
            cnvsNineCtx.arc(xShiftCnvs, yShiftCnvs, dSizeCnvs, 0, 2*Math.PI);
            cnvsNineCtx.stroke();
        }


// No! Too bright for what GrayStar can model!
// //SGB stars
// 
// // RGB color
 var r255 = 150;
 var g255 = 150;
 var b255 = 150; //light gray
 var RGBHex = colHex(r255, r255, r255);
  
 var ii;
 for (var i = 4; i < sgbNum - 3; i++) {
  
  ii = 1.0 * i;
  var xTickPosCnvs = xAxisLength * (logTen(sgbTeffs[i]) - minXData) / rangeXData; // pixels   
  
  // horizontal position in pixels - data values increase rightward:
 var xShiftCnvs = xAxisXCnvs + xTickPosCnvs;
            xShiftCnvs = Math.floor(xShiftCnvs);
 
  var yTickPosCnvs = yAxisLength * (sgbLogLum[i] - minYData) / rangeYData;
 // vertical position in pixels - data values increase upward:
  var yShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;
            yShiftCnvs = Math.floor(yShiftCnvs);
 
            cnvsNineCtx.fillStyle = RGBHex;
            cnvsNineCtx.strokeStyle = RGBHex;
            cnvsNineCtx.beginPath();
            cnvsNineCtx.arc(xShiftCnvs, yShiftCnvs, dSizeCnvs, 0, 2*Math.PI);
            cnvsNineCtx.stroke();
 
  }


// Now overplot our star:
        var xTickPosCnvs = xAxisLength * (logTen(teff) - minXData) / rangeXData; // pixels   
        // horizontal position in pixels - data values increase rightward:
        var xShiftCnvs = xAxisXCnvs + xTickPosCnvs;
        xShiftCnvs = Math.floor(xShiftCnvs);
//
        var yTickPosCnvs = yAxisLength * (logTen(bolLum) - minYData) / rangeYData;
        // vertical position in pixels - data values increase upward:
        var yShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;
        yShiftCnvs = Math.floor(yShiftCnvs);
        //Take color and radius from the last step of the star rendering loop (plot Seve) - that should be the inner-most disk:
        var radiusPxThis = saveRadius / 5;
        if (radiusPxThis < 1){
            radiusPxThis = 1;
            }
        radiusPxThis = Math.floor(radiusPxThis);
        var rrI = saveRGB[0];
        var ggI = saveRGB[1];
        var bbI = saveRGB[2];
//
            cnvsNineCtx.beginPath();
            cnvsNineCtx.strokeStyle="#000000";
            cnvsNineCtx.arc(xShiftCnvs, yShiftCnvs, 1.1 * radiusPxThis, 0, 2*Math.PI);
            cnvsNineCtx.stroke();
            cnvsNineCtx.fill();
        //plotPnt(xShift, yShift, rrI, ggI, bbI, opac, radiusPxThis, plotNineId);
            var RGBHex = colHex(rrI, ggI, bbI);
            cnvsNineCtx.beginPath();
            cnvsNineCtx.strokeStyle=RGBHex;
            cnvsNineCtx.fillStyle=RGBHex;
            cnvsNineCtx.arc(xShiftCnvs, yShiftCnvs, 1.05 * radiusPxThis, 0, 2*Math.PI);
            cnvsNineCtx.stroke();
            cnvsNineCtx.fill();
        //Now overplot Luminosity class markers:

            //I
        var xShift = xAxisXCnvs + xAxisLength * (logTen(sgbTeffs[sgbNum-1]) - minXData) / rangeXData; // pixels 
        var yShift = (yAxisYCnvs + yAxisLength) - (yAxisLength * (sgbLogLum[sgbNum - 1] - minYData) / rangeYData);
        txtPrint("<span style='font-size:normal'><a href='http://en.wikipedia.org/wiki/Stellar_classification' target='_blank'>\n\
         I</a></span>", xShift, yShift, lineColor, plotNineId);
        //III
        xShift = xAxisXCnvs + xAxisLength * (logTen(rgbTeffs[rgbNum-1]) - minXData) / rangeXData; // pixels 
        yShift = (yAxisYCnvs + yAxisLength) - (yAxisLength * (rgbLogLum[rgbNum - 8] - minYData) / rangeYData);
        txtPrint("<span style='font-size:normal'><a href='http://en.wikipedia.org/wiki/Stellar_classification' title='Giants' target='_blank'>\n\
     III</a></span>", xShift, yShift, lineColor, plotNineId);
        //V
        xShift = xAxisXCnvs + xAxisLength * (logTen(msTeffs[msNum-1]) - minXData) / rangeXData; // pixels 
        yShift = (yAxisYCnvs + yAxisLength) - (yAxisLength * (msLogLum[msNum - 8] - minYData) / rangeYData);
        txtPrint("<span style='font-size:normal'><a href='http://en.wikipedia.org/wiki/Stellar_classification' title='Main Sequence, Dwarfs' target='_blank'>\n\
     V</a></span>", xShift, yShift, lineColor, plotNineId);



// ****************************************
    //
    //
    //  *****   PLOT ONE / PLOT 1
    //

    // Plot one: log(Tau) vs log(rho)
    // 
    if (ifShowAtmos === true) {
//
        var plotRow = 3;
        var plotCol = 2;
        var minXData = logE * tauRos[1][0] - 2.0;
        var maxXData = logE * tauRos[1][numDeps - 1];
        var xAxisName = "<span title='Rosseland mean optical depth'><a href='http://en.wikipedia.org/wiki/Optical_depth_%28astrophysics%29' target='_blank'>Log<sub>10</sub> <em>&#964</em><sub>Ros</sub></a></span>";
        // Don't use upper boundary condition as lower y-limit - use a couple of points below surface:
        //var numYTicks = 6;
        // Build total P from P_Gas & P_Rad:

        var minYData = logE * rho[1][1] - 1.0; // Avoid upper boundary condition [i]=0
        var maxYData = logE * rho[1][numDeps - 1];
        var yAxisName = "Log<sub>10</sub> <em>&#961</em> <br />(g cm<sup>-3</sup>)";

        var fineness = "normal";
        //var cnvsCtx = washer(xOffset, yOffset, wColor, plotOneId, cnvsId);
        //var cnvsCtx = washer(plotRow, plotCol, wColor, plotOneId, cnvsOneId);
        var panelOrigin = washer(plotRow, plotCol, panelWidth, wColor, plotOneId, cnvsOneId);
        panelX = panelOrigin[0];
        panelY = panelOrigin[1];
        cnvsOneCtx.fillStyle = wColor;
        cnvsOneCtx.fillRect(0, 0, panelWidth, panelHeight);
        var xAxisParams = XAxis(panelX, panelY, xAxisLength,
                minXData, maxXData, xAxisName, fineness,
                plotOneId, cnvsOneCtx);
        //xOffset = xAxisParams[0];
        var rangeXData = xAxisParams[1];
        var deltaXData = xAxisParams[2];
        var deltaXPxl = xAxisParams[3];
        //yOffset = xAxisParams[4];
        var xLowerYOffset = xAxisParams[5];
        minXData = xAxisParams[6]; //updated value
        maxXData = xAxisParams[7]; //updated value
        //no! var cnvsCtx = xAxisParams[8];
        var yAxisParams = YAxis(panelX, panelY,
                minYData, maxYData, yAxisName,
                plotOneId, cnvsOneCtx);
        var rangeYData = yAxisParams[1];
        var deltaYData = yAxisParams[2];
        var deltaYPxl = yAxisParams[3];
        minYData = yAxisParams[6]; //updated value
        maxYData = yAxisParams[7]; //updated value 

        yFinesse = 0;       
        xFinesse = 0;       
        titleX = panelX + titleOffsetX;
        titleY = panelY + titleOffsetY;
        txtPrint("log<sub>10</sub> <a href='http://en.wikipedia.org/wiki/Gas_laws' title='mass density' target='_blank'>Density</a>",
                titleOffsetX, titleOffsetY, lineColor, plotOneId);

        //Data loop - plot the result!

        //var dSizeG = 2.0;
        var dSizeCnvs = 1.0;
        var opac = 1.0; //opacity
        // RGB color
        // PTot:
        var r255 = 0;
        var g255 = 0;
        var b255 = 255; //blue 
        // PGas:
        var r255G = 0;
        var g255G = 255;
        var b255G = 100; //green
        // PRad:
        var r255R = 255;
        var g255R = 0;
        var b255R = 0; //red

        var ii;
            var xTickPosCnvs = xAxisLength * (logE * tauRos[1][0] - minXData) / rangeXData; // pixels   
            // horizontal position in pixels - data values increase rightward:
            var lastXxShiftCnvs = xAxisXCnvs + xTickPosCnvs;
            var yTickPosCnvs = yAxisLength * (logE * rho[1][0] - minYData) / rangeYData;
            // vertical position in pixels - data values increase upward:
            var lastYShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;

        for (var i = 0; i < numDeps; i++) {

            ii = 1.0 * i;
            var xTickPosCnvs = xAxisLength * (logE * tauRos[1][i] - minXData) / rangeXData; // pixels   

            // horizontal position in pixels - data values increase rightward:
            var xShiftCnvs = xAxisXCnvs + xTickPosCnvs;
            xShiftCnvs = Math.floor(xShiftCnvs);

            var yTickPosCnvs = yAxisLength * (logE * rho[1][i] - minYData) / rangeYData;
            // vertical position in pixels - data values increase upward:
            var yShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;
            yShiftCnvs = Math.floor(yShiftCnvs);

//Plot points
            cnvsOneCtx.beginPath();
            cnvsOneCtx.strokeStyle=lineColor; 
            cnvsOneCtx.arc(xShiftCnvs, yShiftCnvs, dSizeCnvs, 0, 2*Math.PI);
            cnvsOneCtx.stroke();
//Line plot 
            cnvsOneCtx.beginPath();
            cnvsOneCtx.strokeStyle=lineColor; 
            cnvsOneCtx.moveTo(lastXShiftCnvs, lastYShiftCnvs);
            cnvsOneCtx.lineTo(xShiftCnvs, yShiftCnvs);
            cnvsOneCtx.stroke();  
            lastXShiftCnvs = xShiftCnvs;
            lastYShiftCnvs = yShiftCnvs;
        }

// Tau=1 cross-hair

        var barWidth = 1.0;
        var barColor = "#777777";
        var xShift = YBar(logE * tauRos[1][tTau1], minXData, maxXData, xAxisLength,
                barWidth, yAxisLength,
                yFinesse, barColor, plotOneId, cnvsOneCtx);

        var barHeight = 1.0;
        var yShift = XBar(logE * rho[1][tTau1], minYData, maxYData, xAxisLength, barHeight,
                xFinesse, barColor, plotOneId, cnvsOneCtx);
        txtPrint("<span style='font-size:small; color:#444444'><em>&#964</em><sub>Ros</sub>=1</span>",
                xShift, yShift, lineColor, plotOneId);
    }

//
//
//  *****   PLOT TWO / PLOT 2
//
//

// Plot two: log(Tau) vs Temp
// 
    if (ifShowAtmos === true) {

        var plotRow = 3;
        var plotCol = 0;
        var minXData = logE * tauRos[1][0];
        var maxXData = logE * tauRos[1][numDeps - 1];
        var xAxisName = "<span title='Rosseland mean optical depth'><a href='http://en.wikipedia.org/wiki/Optical_depth_%28astrophysics%29' target='_blank'>Log<sub>10</sub> <em>&#964</em><sub>Ros</sub></a></span>";
        var minYData = temp[0][0];
        var maxYData = temp[0][numDeps - 1];
        var yAxisName = "<em>T</em><sub>Kin</sub> (K)";
        var fineness = "normal";

        var panelOrigin = washer(plotRow, plotCol, panelWidth, wColor, plotTwoId, cnvsTwoId);
        panelX = panelOrigin[0];
        panelY = panelOrigin[1];
        cnvsTwoCtx.fillStyle = wColor;
        cnvsTwoCtx.fillRect(0, 0, panelWidth, panelHeight);
        var xAxisParams = XAxis(panelX, panelY, xAxisLength,
                minXData, maxXData, xAxisName, fineness,
                plotTwoId, cnvsTwoCtx);
        //no! var cnvsCtx = xAxisParams[8];
        var yAxisParams = YAxis(panelX, panelY,
                minYData, maxYData, yAxisName,
                plotTwoId, cnvsTwoCtx);
        //
        //xOffset = xAxisParams[0];
        //yOffset = xAxisParams[4];
        var rangeXData = xAxisParams[1];
        var deltaXData = xAxisParams[2];
        var deltaXPxl = xAxisParams[3];
        var rangeYData = yAxisParams[1];
        var deltaYData = yAxisParams[2];
        var deltaYPxl = yAxisParams[3];
        //var xLowerYOffset = xAxisParams[5];
        minXData = xAxisParams[6]; //updated value
        minYData = yAxisParams[6]; //updated value
        maxXData = xAxisParams[7]; //updated value
        maxYData = yAxisParams[7]; //updated value    
        yFinesse = 0;       
        xFinesse = 0;       
        //
        // Tau=1 cross-hair

        var barWidth = 1.0;
        var barColor = "#777777";
        var tTau1 = tauPoint(numDeps, tauRos, 1.0);
        xShift = YBar(logE * tauRos[1][tTau1], minXData, maxXData, xAxisLength,
                barWidth, yAxisLength,
                yFinesse, barColor, plotTwoId, cnvsTwoCtx);

        yShift = XBar(temp[0][tTau1], minYData, maxYData, xAxisLength, barHeight,
                xFinesse, barColor, plotTwoId, cnvsTwoCtx);
        barHeight = 1.0;
        // Add label
        txtPrint("<span style='font-size:small; color:#444444'><em>&#964</em><sub>Ros</sub>=1</span>",
                xShift, yShift, lineColor, plotTwoId);

        titleX = panelX + titleOffsetX;
        titleY = panelY + titleOffsetY;
        //        titleXPos, titleYPos - 40, zeroInt, zeroInt, zeroInt, masterId);
        txtPrint("<span style='font-size:normal; color:blue'>Gas temperature </span>",
                titleOffsetX, titleOffsetY, lineColor, plotTwoId);
        //Data loop - plot the result!

        //var dSize = 5.0; //plot point size
        var dSizeCnvs = 1.0; //plot point size
        var opac = 1.0; //opacity
        // RGB color
        var r255 = 0;
        var g255 = 0;
        var b255 = 255; //blue

        var ii;
        var xTickPosCnvs = xAxisLength * (logE * tauRos[1][0] - minXData) / rangeXData; // pixels   

        // horizontal position in pixels - data values increase rightward:
        var lastXShiftCnvs = xAxisXCnvs + xTickPosCnvs;

        var yTickPosCnvs = yAxisLength * (temp[0][0] - minYData) / rangeYData;
        // vertical position in pixels - data values increase upward:
        var lastYShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;

        for (var i = 0; i < numDeps; i++) {

            ii = 1.0 * i;
            var xTickPosCnvs = xAxisLength * (logE * tauRos[1][i] - minXData) / rangeXData; // pixels   

            // horizontal position in pixels - data values increase rightward:
            var xShiftCnvs = xAxisXCnvs + xTickPosCnvs;
            xShiftCnvs = Math.floor(xShiftCnvs);

            var yTickPosCnvs = yAxisLength * (temp[0][i] - minYData) / rangeYData;
            // vertical position in pixels - data values increase upward:
            var yShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;
            yShiftCnvs = Math.floor(yShiftCnvs);

//Plot points
            cnvsTwoCtx.strokeStyle=lineColor; 
            cnvsTwoCtx.beginPath();
            cnvsTwoCtx.arc(xShiftCnvs, yShiftCnvs, dSizeCnvs, 0, 2*Math.PI);
            cnvsTwoCtx.stroke();
//Line plot
            cnvsTwoCtx.beginPath();
            cnvsTwoCtx.strokeStyle=lineColor; 
            cnvsTwoCtx.moveTo(lastXShiftCnvs, lastYShiftCnvs);
            cnvsTwoCtx.lineTo(xShiftCnvs, yShiftCnvs);
            cnvsTwoCtx.stroke();  
            lastXShiftCnvs = xShiftCnvs;
            lastYShiftCnvs = yShiftCnvs;
        }

//  Loop over limb darkening sub-disks - largest to smallest, and add color-coded Tau(theta) = 1 markers

        //dSize = 8.0;
        dSizeCnvs = 4.0;

        // Disk centre:
        //This approach does not allow for calibration easily:
        //now done earlier var bvr = bandIntens[2][0] + bandIntens[3][0] + bandIntens[4][0];
        var brightScale = 255.0 / Math.max(bandIntens[2][0] / bvr, bandIntens[3][0] / bvr, bandIntens[4][0] / bvr);
        // *Raw* Vega: r g b 183 160 255
        //now down above: var rgbVega = [183.0 / 255.0, 160.0 / 255.0, 255.0 / 255.0];
        for (var i = numThetas - 1; i >= 0; i--) {

            ii = 1.0 * i;
            //     iCosThetaI = limbTheta1 - ii * limbDelta;
            //     iIntMaxI = interpol(iCosTheta, iIntMax, iCosThetaI);

            //numPrint(i, 50, 100 + i * 20, zeroInt, zeroInt, zeroInt, masterId);
            // LTE Eddington-Barbier limb darkening: I(Tau=0, cos(theta)=t) = B(T(Tau=t))
            var cosFctr = cosTheta[1][i];
            //  var cosFctr = iCosThetaI;
            //numPrint(cosFctr, 100, 100+i*20, zeroInt, zeroInt, zeroInt, masterId);
            var dpthIndx = tauPoint(numDeps, tauRos, cosFctr);
            //numPrint(dpthIndx, 100, 100+i*20, zeroInt, zeroInt, zeroInt, masterId);

            r255 = Math.ceil(brightScale * (bandIntens[4][i] / bvr) / rgbVega[0]); // / vegaBVR[2]);
            g255 = Math.ceil(brightScale * (bandIntens[3][i] / bvr) / rgbVega[1]); // / vegaBVR[1]);
            b255 = Math.ceil(brightScale * (bandIntens[2][i] / bvr) / rgbVega[2]); // / vegaBVR[0]);

            var xTickPosCnvs = xAxisLength * (logE * tauRos[1][dpthIndx] - minXData) / rangeXData; // pixels   

            // horizontal position in pixels - data values increase rightward:
            //var xShift = xOffset + xTickPos;
            var xShiftCnvs = xAxisXCnvs + xTickPosCnvs;// + 200;
            xShiftCnvs = Math.floor(xShiftCnvs);
            ////stringify and add unit:
            //        var xShiftStr = numToPxStrng(xShift);

            //var yTickPos = yRange * (temp[0][dpthIndx] - minYData) / rangeYData;
            var yTickPosCnvs = yAxisLength * (temp[0][dpthIndx] - minYData) / rangeYData;
            // vertical position in pixels - data values increase upward:
            var yShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;
            yShiftCnvs = Math.floor(yShiftCnvs);

            var RGBHex = colHex(r255, g255, b255);
            cnvsTwoCtx.beginPath();
            cnvsTwoCtx.arc(xShiftCnvs, yShiftCnvs, dSizeCnvs, 0, 2*Math.PI);
            cnvsTwoCtx.strokeStyle = RGBHex;
            cnvsTwoCtx.fillStyle = RGBHex;
            //cnvsCtx.strokeStyle = "rgba(r255, g255, b255, 1.0)";
            //cnvsCtx.strokeStyle = "#FF0000"; //test
            cnvsTwoCtx.stroke();
            cnvsTwoCtx.fill();
        }

// legend using dot of last color in loop directly above:
        //plotPnt(titleX, titleY + 10, r255, g255, b255, opac, dSize, plotTwoId);
            cnvsTwoCtx.beginPath();
            cnvsTwoCtx.strokeStyle = RGBHex;
            cnvsTwoCtx.fillStyle = RGBHex;
            cnvsTwoCtx.arc(titleOffsetX + 365, titleOffsetY+10, dSizeCnvs, 0, 2*Math.PI);
            cnvsTwoCtx.stroke();
            cnvsTwoCtx.fill();
        txtPrint("<span title='Limb darkening depths of &#964_Ros(&#952) = 1'><em>&#964</em><sub>Ros</sub>(0 < <em>&#952</em> < 90<sup>o</sup>) = 1</span>",
                titleOffsetX + 200, titleOffsetY, lineColor, plotTwoId);

    }

    //
    //
    //  *****   PLOT THREE / PLOT 3
    //
    //
    // Plot three: log(Tau) vs log(Pressure)

    if (ifShowAtmos === true) {

        var plotRow = 3;
        var plotCol = 1;
        var minXData = logE * tauRos[1][0];
        var maxXData = logE * tauRos[1][numDeps - 1];
        var xAxisName = "<span title='Rosseland mean optical depth'><a href='http://en.wikipedia.org/wiki/Optical_depth_%28astrophysics%29' target='_blank'>Log<sub>10</sub> <em>&#964</em><sub>Ros</sub></a></span>";
        // From Hydrostat.hydrostat:
        //press is a 4 x numDeps array:
        // rows 0 & 1 are linear and log *gas* pressure, respectively
        // Don't use upper boundary condition as lower y-limit - use a couple of points below surface:
        //var numYTicks = 6;
        // Build total P from P_Gas & P_Rad:
        var logPTot = [];
        logPTot.length = numDeps;
        for (var i = 0; i < numDeps; i++) {
            logPTot[i] = Math.log(pGas[0][i] + pRad[0][i]);
            //console.log(logPTot[i]);
        }
        //var minYData = logE * logPTot[0] - 2.0; // Avoid upper boundary condition [i]=0
        //var minYData = logE * Math.min(pGas[1][0], pRad[1][0], newPe[1][0]) - 1.0;
        var minYData = logE * Math.min(pGas[1][0], pRad[1][0], Pe[1][0]) - 1.0;
        var maxYData = logE * logPTot[numDeps - 1];
        var yAxisName = "Log<sub>10</sub> <em>P</em> <br />(dynes <br />cm<sup>-2</sup>)";
        //washer(xRange, xOffset, yRange, yOffset, wColor, plotThreeId);

        var fineness = "normal";
        //var cnvsCtx = washer(plotRow, plotCol, wColor, plotThreeId, cnvsId);
        var panelOrigin = washer(plotRow, plotCol, panelWidth, wColor, plotThreeId, cnvsThreeId);
        panelX = panelOrigin[0];
        panelY = panelOrigin[1];
        cnvsThreeCtx.fillStyle = wColor;
        cnvsThreeCtx.fillRect(0, 0, panelWidth, panelHeight);
        var xAxisParams = XAxis(panelX, panelY, xAxisLength,
                minXData, maxXData, xAxisName, fineness,
                plotThreeId, cnvsThreeCtx);
        var yAxisParams = YAxis(panelX, panelY,
                minYData, maxYData, yAxisName,
                plotThreeId, cnvsThreeCtx);
        //xOffset = xAxisParams[0];
        //yOffset = xAxisParams[4];
        var rangeXData = xAxisParams[1];
        var deltaXData = xAxisParams[2];
        var deltaXPxl = xAxisParams[3];
        var rangeYData = yAxisParams[1];
        var deltaYData = yAxisParams[2];
        var deltaYPxl = yAxisParams[3];
        var xLowerYOffset = xAxisParams[5];
        minXData = xAxisParams[6]; //updated value
        minYData = yAxisParams[6]; //updated value
        maxXData = xAxisParams[7]; //updated value
        maxYData = yAxisParams[7]; //updated value        
        yFinesse = 0;       
        xFinesse = 0;       
        //
        titleX = panelX + titleOffsetX;
        titleY = panelY + titleOffsetY;
        txtPrint("log Pressure: <span style='color:blue' title='Total pressure'><strong><em>P</em><sub>Tot</sub></strong></span> "
                + " <a href='http://en.wikipedia.org/wiki/Gas_laws' target='_blank'><span style='color:#00FF88' title='Gas pressure'><em>P</em><sub>Gas</sub></span></a> "
                + " <a href='http://en.wikipedia.org/wiki/Radiation_pressure' target='_blank'><span style='color:red' title='Radiation pressure'><em>P</em><sub>Rad</sub></span></a> "
                + " <span style='color:black' title='Partial electron pressure'><em>P</em><sub>e</sub></span>",
                titleOffsetX, titleOffsetY, lineColor, plotThreeId);

        //Data loop - plot the result!

        var dSizeCnvs = 2.0; //plot point size
        var dSizeGCnvs = 1.0;
        var opac = 1.0; //opacity
        // RGB color
        // PTot:
        var r255 = 0;
        var g255 = 0;
        var b255 = 255; //blue 
        // PGas:
        var r255G = 0;
        var g255G = 255;
        var b255G = 100; //green
        // PRad:
        var r255R = 255;
        var g255R = 0;
        var b255R = 0; //red

        var ii;
        var xTickPosCnvs = xAxisLength * (logE * tauRos[1][0] - minXData) / rangeXData; // pixels   

        // horizontal position in pixels - data values increase rightward:
         var lastXShiftCnvs = xAxisXCnvs + xTickPosCnvs;

         var lastYTickPosCnvs = yAxisLength * (logE * logPTot[0] - minYData) / rangeYData;
         var lastYTickPosGCnvs = yAxisLength * (logE * pGas[1][0] - minYData) / rangeYData;
         var lastYTickPosRCnvs = yAxisLength * (logE * pRad[1][0] - minYData) / rangeYData;
         var lastYTickPosBCnvs = yAxisLength * (logE * Pe[1][0] - minYData) / rangeYData;

         // vertical position in pixels - data values increase upward:
         var lastYShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;
         var lastYShiftGCnvs = (yAxisYCnvs + yAxisLength) - yTickPosGCnvs;
         var lastYShiftRCnvs = (yAxisYCnvs + yAxisLength) - yTickPosRCnvs;
         var lastYShiftBCnvs = (yAxisYCnvs + yAxisLength) - yTickPosBCnvs;
        // Avoid upper boundary at i=0
        for (var i = 1; i < numDeps; i++) {

            ii = 1.0 * i;
            var xTickPosCnvs = xAxisLength * (logE * tauRos[1][i] - minXData) / rangeXData; // pixels   

            // horizontal position in pixels - data values increase rightward:
            var xShiftCnvs = xAxisXCnvs + xTickPosCnvs;
            xShiftCnvs = Math.floor(xShiftCnvs);

            var yTickPosCnvs = yAxisLength * (logE * logPTot[i] - minYData) / rangeYData;
            var yTickPosGCnvs = yAxisLength * (logE * pGas[1][i] - minYData) / rangeYData;
            var yTickPosRCnvs = yAxisLength * (logE * pRad[1][i] - minYData) / rangeYData;
            var yTickPosBCnvs = yAxisLength * (logE * Pe[1][i] - minYData) / rangeYData;
            // vertical position in pixels - data values increase upward:
            var yShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;
            yShiftCnvs = Math.floor(yShiftCnvs);
            var yShiftGCnvs = (yAxisYCnvs + yAxisLength) - yTickPosGCnvs;
            yShiftGCnvs = Math.floor(yShiftGCnvs);
            var yShiftRCnvs = (yAxisYCnvs + yAxisLength) - yTickPosRCnvs;
            yShiftRCnvs = Math.floor(yShiftRCnvs);
            var yShiftBCnvs = (yAxisYCnvs + yAxisLength) - yTickPosBCnvs;
            yShiftBCnvs = Math.floor(yShiftBCnvs);

//Plot points
            cnvsThreeCtx.beginPath();
            cnvsThreeCtx.arc(xShiftCnvs, yShiftCnvs, dSizeCnvs, 0, 2*Math.PI);
            cnvsThreeCtx.strokeStyle = "#0000FF";
            cnvsThreeCtx.stroke();
//Line plots
            cnvsThreeCtx.beginPath();
            cnvsThreeCtx.strokeStyle="#0000FF"; 
            cnvsThreeCtx.moveTo(lastXShiftCnvs, lastYShiftCnvs);
            cnvsThreeCtx.lineTo(xShiftCnvs, yShiftCnvs);
            cnvsThreeCtx.stroke();  
//
            cnvsThreeCtx.beginPath();
            cnvsThreeCtx.arc(xShiftCnvs, yShiftGCnvs, dSizeGCnvs, 0, 2*Math.PI);
            cnvsThreeCtx.strokeStyle = "#00FF00";
            cnvsThreeCtx.stroke();
            cnvsThreeCtx.beginPath();
            cnvsThreeCtx.strokeStyle="#00FF00"; 
            cnvsThreeCtx.moveTo(lastXShiftCnvs, lastYShiftGCnvs);
            cnvsThreeCtx.lineTo(xShiftCnvs, yShiftGCnvs);
            cnvsThreeCtx.stroke();  
//
            cnvsThreeCtx.beginPath();
            cnvsThreeCtx.arc(xShiftCnvs, yShiftRCnvs, dSizeGCnvs, 0, 2*Math.PI);
            cnvsThreeCtx.strokeStyle = "#FF0000";
            cnvsThreeCtx.stroke();
            cnvsThreeCtx.beginPath();
            cnvsThreeCtx.strokeStyle="#FF0000"; 
            cnvsThreeCtx.moveTo(lastXShiftCnvs, lastYShiftRCnvs);
            cnvsThreeCtx.lineTo(xShiftCnvs, yShiftRCnvs);
            cnvsThreeCtx.stroke();  
//
            cnvsThreeCtx.beginPath();
            cnvsThreeCtx.arc(xShiftCnvs, yShiftBCnvs, dSizeGCnvs, 0, 2*Math.PI);
            cnvsThreeCtx.strokeStyle = "#000000";
            cnvsThreeCtx.stroke();
            cnvsThreeCtx.beginPath();
            cnvsThreeCtx.strokeStyle="#000000"; 
            cnvsThreeCtx.moveTo(lastXShiftCnvs, lastYShiftBCnvs);
            cnvsThreeCtx.lineTo(xShiftCnvs, yShiftBCnvs);
            cnvsThreeCtx.stroke();  
//
            lastXShiftCnvs = xShiftCnvs;
            lastYShiftCnvs = yShiftCnvs;
            lastYShiftGCnvs = yShiftGCnvs;
            lastYShiftRCnvs = yShiftRCnvs;
            lastYShiftBCnvs = yShiftBCnvs;
        }

// Tau=1 cross-hair

        var tTau1 = tauPoint(numDeps, tauRos, 1.0);
        var barWidth = 1.0;
        var barColor = "#777777";
        yFinesse = 0.0;
        xShift = YBar(logE * tauRos[1][tTau1], minXData, maxXData, xAxisLength,
                barWidth, yAxisLength,
                yFinesse, barColor, plotThreeId, cnvsThreeCtx);
        //console.log("Bar: xShift = " + xShift);

        //console.log("PLOT THREE: logE*logPTot[tTau1] " + logE * logPTot[tTau1]);
        barHeight = 1.0;
        yShift = XBar(logE * logPTot[tTau1], minYData, maxYData, xAxisLength, barHeight,
                xFinesse, barColor, plotThreeId, cnvsThreeCtx);
        txtPrint("<span style='font-size:small; color:#444444'><em>&#964</em><sub>Ros</sub>=1</span>",
                xShift, yShift, lineColor, plotThreeId);
    }

    //
    //
    //  *****   PLOT FOUR / PLOT 4
    //
    //
    // Plot four: Limb darkening
 
   if (ifShowRad === true) {

        var plotRow = 4;
        var plotCol = 1;
//
        var minXData = 180.0 * Math.acos(cosTheta[1][0]) / Math.PI;
        var maxXData = 180.0 * Math.acos(cosTheta[1][numThetas - 1]) / Math.PI;
        var xAxisName = "<em>&#952</em> (<sup>o</sup>)";
        var minYData = 0.0;
        var maxYData = 1.0;
        var yAxisName = "<span title='Monochromatic surface specific intensity'><a href='http://en.wikipedia.org/wiki/Specific_radiative_intensity' target='_blank'><em>I</em><sub>&#955</sub>(<em>&#952</em>)/<br /><em>I</em><sub>&#955</sub>(0)</a></span>";

        var fineness = "normal";
//
        var panelOrigin = washer(plotRow, plotCol, panelWidth, wColor, plotFourId, cnvsFourId);
        panelX = panelOrigin[0];
        panelY = panelOrigin[1];
        cnvsFourCtx.fillStyle = wColor;
        cnvsFourCtx.fillRect(0, 0, panelWidth, panelHeight);
        var xAxisParams = XAxis(panelX, panelY, xAxisLength,
                minXData, maxXData, xAxisName, fineness,
                plotFourId, cnvsFourCtx);
        var yAxisParams = YAxis(panelX, panelY,
                minYData, maxYData, yAxisName,
                plotFourId, cnvsFourCtx);
        var rangeXData = xAxisParams[1];
        var deltaXData = xAxisParams[2];
        var deltaXPxl = xAxisParams[3];
        var rangeYData = yAxisParams[1];
        var deltaYData = yAxisParams[2];
        var deltaYPxl = yAxisParams[3];
        minXData = xAxisParams[6]; //updated value
        minYData = yAxisParams[6]; //updated value
        maxXData = xAxisParams[7]; //updated value
        maxYData = yAxisParams[7]; //updated value        
        //
        // Add legend annotation:

        //var iLamMinMax = minMax2(masterFlux);
        //var iLamMax = iLamMinMax[1];
        //var lamMax = (1.0e7 * masterLams[iLamMax]).toPrecision(3);

        var lam1 = (1.0e7 * masterLams[0]).toPrecision(3);
        var lam1Str = lam1.toString(10);
        var lamN = (1.0e7 * masterLams[numMaster - 1]).toPrecision(3);
        var lamNStr = lamN.toString(10);
        var lam0r = (1.0e7 * lam0).toPrecision(3);
        var lam0rStr = lam0r.toString(10);
//
        titleX = panelX + titleOffsetX;
        titleY = panelY + titleOffsetY;
        txtPrint("<span style='font-size:small'><span style='color:#00FF88'><em>&#955</em><sub>Max</sub> = " + lamMaxStr + "nm</span><br /> "
                + " <span style='color:blue'><em>&#955</em> = " + lam1Str + "nm</span><br /> "
                + " <span style='color:red'><em>&#955</em> = " + lamNStr + "nm</span><br />"
                + " <span style='color:#444444'>line <em>&#955</em><sub>0</sub> = " + lam0rStr + "nm</span></span>",
                xAxisXCnvs+10, titleOffsetY+yAxisLength-20, lineColor, plotFourId);
        // Add title annotation:


        txtPrint("<span style='font-size:normal; color:blue'><a href='http://en.wikipedia.org/wiki/Limb_darkening' target='_blank'>Limb darkening and reddening</a></span>",
                titleOffsetX, titleOffsetY, lineColor, plotFourId);
        //Data loop - plot the result!

        var dSizeCnvs = 2.0; //plot point size
        var dSize0Cnvs = 1.0;
        var opac = 1.0; //opacity
        // RGB color
        // PTot:
        var r255 = 0;
        var g255 = 255;
        var b255 = 100; //green 
        // PGas:
        var r2550 = 0;
        var g2550 = 0;
        var b2550 = 255; //blue
        // PRad:
        var r255N = 255;
        var g255N = 0;
        var b255N = 0; //red


        var xTickPosCnvs = xAxisLength * (180.0 * Math.acos(cosTheta[1][0]) / Math.PI - minXData) / rangeXData; // pixels   
        // horizontal position in pixels - data values increase rightward:
        var lastXShiftCnvs = xAxisXCnvs + xTickPosCnvs;
        var yTickPosCnvs = yAxisLength * ((masterIntens[iLamMax][0] / masterIntens[iLamMax][0]) - minYData) / rangeYData;
        var yTickPos0Cnvs = yAxisLength * ((masterIntens[0][0] / masterIntens[0][0]) - minYData) / rangeYData;
        var yTickPosNCnvs = yAxisLength * ((masterIntens[numMaster - 1][0] / masterIntens[numMaster - 1][0]) - minYData) / rangeYData;

        // vertical position in pixels - data values increase upward:
        var lastYShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;
        var lastYShift0Cnvs = (yAxisYCnvs + yAxisLength) - yTickPos0Cnvs;
        var lastYShiftNCnvs = (yAxisYCnvs + yAxisLength) - yTickPosNCnvs;
//
        for (var i = 1; i < numThetas; i++) {

            xTickPosCnvs = xAxisLength * (180.0 * Math.acos(cosTheta[1][i]) / Math.PI - minXData) / rangeXData; // pixels   
            // horizontal position in pixels - data values increase rightward:
            var xShiftCnvs = xAxisXCnvs + xTickPosCnvs;
            xShiftCnvs = Math.floor(xShiftCnvs);

            yTickPosCnvs = yAxisLength * ((masterIntens[iLamMax][i] / masterIntens[iLamMax][0]) - minYData) / rangeYData;
            yTickPos0Cnvs = yAxisLength * ((masterIntens[0][i] / masterIntens[0][0]) - minYData) / rangeYData;
            yTickPosNCnvs = yAxisLength * ((masterIntens[numMaster - 1][i] / masterIntens[numMaster - 1][0]) - minYData) / rangeYData;

            // vertical position in pixels - data values increase upward:
            var yShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;
            yShiftCnvs = Math.floor(yShiftCnvs);
            var yShift0Cnvs = (yAxisYCnvs + yAxisLength) - yTickPos0Cnvs;
            yShift0Cnvs = Math.floor(yShift0Cnvs);
            var yShiftNCnvs = (yAxisYCnvs + yAxisLength) - yTickPosNCnvs;
            yShiftNCnvs = Math.floor(yShiftNCnvs);

//Plot points
            cnvsFourCtx.beginPath();
            cnvsFourCtx.arc(xShiftCnvs, yShiftCnvs, dSizeCnvs, 0, 2*Math.PI);
            RGBHex = colHex(r255, g255, b255);
            cnvsFourCtx.strokeStyle = RGBHex;
            cnvsFourCtx.stroke();
//line plot
            cnvsFourCtx.beginPath();
            cnvsFourCtx.strokeStyle = RGBHex;
            cnvsFourCtx.moveTo(lastXShiftCnvs, lastYShiftCnvs);
            cnvsFourCtx.lineTo(xShiftCnvs, yShiftCnvs);
            cnvsFourCtx.stroke();  
            //
            cnvsFourCtx.beginPath();
            cnvsFourCtx.arc(xShiftCnvs, yShift0Cnvs, dSize0Cnvs, 0, 2*Math.PI);
            RGBHex = colHex(r2550, g2550, b2550);
            cnvsFourCtx.strokeStyle = RGBHex;
            cnvsFourCtx.stroke();
//
            cnvsFourCtx.beginPath();
            cnvsFourCtx.strokeStyle = RGBHex;
            cnvsFourCtx.moveTo(lastXShiftCnvs, lastYShift0Cnvs);
            cnvsFourCtx.lineTo(xShiftCnvs, yShift0Cnvs);
            cnvsFourCtx.stroke();  
//
            cnvsFourCtx.beginPath();
            cnvsFourCtx.arc(xShiftCnvs, yShiftNCnvs, dSize0Cnvs, 0, 2*Math.PI);
            RGBHex = colHex(r255N, g255N, b255N);
            cnvsFourCtx.strokeStyle = RGBHex;
            cnvsFourCtx.stroke();
//
            cnvsFourCtx.beginPath();
            RGBHex = colHex(r255N, g255N, b255N);
            cnvsFourCtx.strokeStyle = RGBHex;
            cnvsFourCtx.moveTo(lastXShiftCnvs, lastYShiftNCnvs);
            cnvsFourCtx.lineTo(xShiftCnvs, yShiftNCnvs);
            cnvsFourCtx.stroke(); 
// 
            lastXShiftCnvs = xShiftCnvs;
            lastYShiftCnvs = yShiftCnvs;
            lastYShift0Cnvs = yShift0Cnvs;
            lastYShiftNCnvs = yShiftNCnvs;
        }
    }

//
//
//  *****   PLOT FIVE / PLOT 5
//
//

// Plot five: SED
// 
    if (ifShowRad === true) {

        var plotRow = 4;
        var plotCol = 0;
//
        var minXData = 1.0e7 * masterLams[0];
        var maxXData = 1.0e7 * masterLams[numMaster - 1];
        var xAxisName = "<em>&#955</em> (nm)";
        //    ////Logarithmic x:
        //var minXData = 7.0 + logTen(masterLams[0]);
        //var maxXData = 7.0 + logTen(masterLams[numMaster - 1]);
        //var maxXData = 3.0; //finesse - Log10(lambda) = 3.5 nm
        //var xAxisName = "Log<sub>10</sub> &#955 (nm)";
        //var numYTicks = 4;
        //now done above var norm = 1.0e15; // y-axis normalization
        var minYData = 0.0;
        // iLamMax established in PLOT TWO above:
        var maxYData = masterFlux[0][iLamMax] / norm;
        var yAxisName = "<span title='Monochromatic surface flux'><a href='http://en.wikipedia.org/wiki/Spectral_flux_density' target='_blank'> <em>F</em><sub>&#955</sub> x 10<sup>15</sup><br />ergs s<sup>-1</sup> <br />cm<sup>-3</sup></a></span>";
        ////Logarithmic y:
        //var minYData = 12.0;
        //var maxYData = logE * masterFlux[1][iLamMax];
        //var yAxisName = "<span title='Monochromatic surface flux'><a href='http://en.wikipedia.org/wiki/Spectral_flux_density' target='_blank'>Log<sub>10</sub> <em>F</em><sub>&#955</sub> <br /> ergs s<sup>-1</sup> cm<sup>-3</sup></a></span>";
        //(xRange, xOffset, yRange, yOffset, wColor, plotFiveId);

        var fineness = "coarse";
        //var cnvsCtx = washer(plotRow, plotCol, wColor, plotFiveId, cnvsId);
        var panelOrigin = washer(plotRow, plotCol, panelWidth, wColor, plotFiveId, cnvsFiveId);
        panelX = panelOrigin[0];
        panelY = panelOrigin[1];
        cnvsFiveCtx.fillStyle = wColor;
        cnvsFiveCtx.fillRect(0, 0, panelWidth, panelHeight);
        var xAxisParams = XAxis(panelX, panelY, xAxisLength,
                minXData, maxXData, xAxisName, fineness,
                plotFiveId, cnvsFiveCtx);

        var yAxisParams = YAxis(panelX, panelY,
                minYData, maxYData, yAxisName,
                plotFiveId, cnvsFiveCtx);
        //xOffset = xAxisParams[0];
        //yOffset = xAxisParams[4];
        var rangeXData = xAxisParams[1];
        var deltaXData = xAxisParams[2];
        var deltaXPxl = xAxisParams[3];
        var rangeYData = yAxisParams[1];
        var deltaYData = yAxisParams[2];
        var deltaYPxl = yAxisParams[3];
        minXData = xAxisParams[6]; //updated value
        minYData = yAxisParams[6]; //updated value
        maxXData = xAxisParams[7]; //updated value
        maxYData = yAxisParams[7]; //updated value        
        //
        // Add legend annotation:

        var thet0 = 180.0 * Math.acos(cosTheta[1][0]) / Math.PI;
        var thet0lbl = thet0.toPrecision(2);
        var thet0Str = thet0lbl.toString();
        var thetN = 180.0 * Math.acos(cosTheta[1][numThetas - 2]) / Math.PI;
        var thetNlbl = thetN.toPrecision(2);
        var thetNStr = thetNlbl.toString();
//
        titleX = panelX + titleOffsetX;
        titleY = panelY + titleOffsetY;
        txtPrint("<span style='font-size:normal; color:blue'><a href='http://en.wikipedia.org/wiki/Spectral_energy_distribution' target='_blank'>\n\
     Spectral energy distribution (SED)</a></span>",
                titleOffsetX, titleOffsetY, lineColor, plotFiveId);
        txtPrint("<span style='font-size:small'>"
                + "<span><em>F</em><sub>&#955</sub> (<em>&#955</em><sub>Max</sub> = " + lamMaxStr + " nm)</span>, "
                + " <span><em>I</em><sub>&#955</sub>,</span> <span style='color:#444444'> <em>&#952</em> = " + thet0Str + "<sup>o</sup></span>,  "
                + " <span style='color:#444444'><em>&#952</em> = " + thetNStr + "<sup>o</sup></span></span>",
                titleOffsetX, titleOffsetY+35, lineColor, plotFiveId);
//
        // Photometric bands centers

        var opac = 0.5;
        var opacStr = "0.5";
//        var yTickPos = 0;
//        var yShift = (xLowerYOffset - yRange) + yTickPos;
//        var yShiftStr = numToPxStrng(yShift);
        var vBarWidth = 2; //pixels 
        var vBarHeight = yAxisLength;
//        var vBarWidthStr = numToPxStrng(vBarWidth);
//        var vBarHeightStr = numToPxStrng(vBarHeight);
        //
        yFinesse = 0; 
        var UBVRIBands = function(r255, g255, b255, band0) {

            var RGBHex = colHex(r255, g255, b255);
            //var RGBHex = "#FF0000";
            // Vertical bar:
//            var xTickPos = xRange * (band0 - minXData) / rangeXData; // pixels    
 //           var xShift = xOffset + xTickPos;
 //  
        xShift = YBar(band0, minXData, maxXData, xAxisLength,
                vBarWidth, yAxisLength,
                yFinesse, RGBHex, plotFiveId, cnvsFiveCtx);
        }; //end function UBVRIbands


//
        //
        var filters = filterSet();
        var lam0_ptr = 11; // approximate band centre
        var numBands = filters.length;
        var lamUBVRI = [];
        lamUBVRI.length = numBands;
        for (var ib = 0; ib < numBands; ib++) {
            lamUBVRI[ib] = 1.0e7 * filters[ib][0][lam0_ptr]; //linear lambda
            //lamUBVRI[ib] = 7.0 + logTen(filters[ib][0][lam0_ptr]);  //logarithmic lambda
        }

        //Ux:
        var r255 = 155;
        var g255 = 0;
        var b255 = 155; // violet
        UBVRIBands(r255, g255, b255, lamUBVRI[0]);
        //B:
        var r255 = 0;
        var g255 = 0;
        var b255 = 255; // blue
        UBVRIBands(r255, g255, b255, lamUBVRI[2]);
        //V:
        var r255 = 0;
        var g255 = 255;
        var b255 = 100; // green
        UBVRIBands(r255, g255, b255, lamUBVRI[3]);
        //R:
        var r255 = 255;
        var g255 = 0;
        var b255 = 0; // red
        UBVRIBands(r255, g255, b255, lamUBVRI[4]);
        //I:
        var r255 = 255;
        var g255 = 40;
        var b255 = 40; // dark red / brown ??
        UBVRIBands(r255, g255, b255, lamUBVRI[5]);
        //Data loop - plot the result!

        var dSizeCnvs = 1.0; //plot point size
        var dSize0Cnvs = 1.0;
        var opac = 1.0; //opacity
        // RGB color
        // PTot:
        var r255 = 0;
        var g255 = 0;
        var b255 = 0; //black
        // PGas:
        var r2550 = 90;
        var g2550 = 90;
        var b2550 = 90; //dark gray
        // PRad:
        var r255N = 120;
        var g255N = 120;
        var b255N = 120; //light gray

        // Avoid upper boundary at i=0

        //var logLambdanm = 7.0 + logTen(masterLams[0]);  //logarithmic
        var lambdanm = 1.0e7 * masterLams[0];
        var xTickPosCnvs = xAxisLength * (lambdanm - minXData) / rangeXData; // pixels
        var lastXShiftCnvs = xAxisXCnvs + xTickPosCnvs;
//Logarithmic y:
        var yTickPosCnvs = yAxisLength * ((masterFlux[0][0] / norm) - minYData) / rangeYData;
        var yTickPos0Cnvs = yAxisLength * ((masterIntens[0][0] / norm) - minYData) / rangeYData;
        var yTickPosNCnvs = yAxisLength * ((masterIntens[0][numThetas - 2] / norm) - minYData) / rangeYData;
        // vertical position in pixels - data values increase upward:
        var lastYShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;
        var lastYShift0Cnvs = (yAxisYCnvs + yAxisLength) - yTickPos0Cnvs;
        var lastYShiftNCnvs = (yAxisYCnvs + yAxisLength) - yTickPosNCnvs;
        var xShift, yShift;
        for (var i = 1; i < numMaster; i++) {

            lambdanm = masterLams[i] * 1.0e7; //cm to nm //linear
            //logLambdanm = 7.0 + logTen(masterLams[i]);  //logarithmic
            ii = 1.0 * i;
            xTickPosCnvs = xAxisLength * (lambdanm - minXData) / rangeXData; // pixels   //linear

            // horizontal position in pixels - data values increase rightward:
            xShiftCnvs = xAxisXCnvs + xTickPosCnvs;
            xShiftCnvs = Math.floor(xShiftCnvs);

//logarithmic y:
            yTickPosCnvs = yAxisLength * ((masterFlux[0][i] / norm) - minYData) / rangeYData;
            yTickPos0Cnvs = yAxisLength * ((masterIntens[i][0] / norm) - minYData) / rangeYData;
            yTickPosNCnvs = yAxisLength * ((masterIntens[i][numThetas - 2] / norm) - minYData) / rangeYData;
            // vertical position in pixels - data values increase upward:
            yShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;
            yShiftCnvs = Math.floor(yShiftCnvs);
            yShift0Cnvs = (yAxisYCnvs + yAxisLength) - yTickPos0Cnvs;
            yShift0Cnvs = Math.floor(yShift0Cnvs);
            yShiftNCnvs = (yAxisYCnvs + yAxisLength) - yTickPosNCnvs;
            yShiftNCnvs = Math.floor(yShiftNCnvs);

//plot points
            //cnvsFiveCtx.beginPath();
            //cnvsFiveCtx.arc(xShiftCnvs, yShiftCnvs, dSizeCnvs, 0, 2*Math.PI);
            //RGBHex = colHex(r255, g255, b255);
            //cnvsFiveCtx.strokeStyle = RGBHex;
            //cnvsFiveCtx.stroke();
//line plot
            cnvsFiveCtx.beginPath();
            RGBHex = colHex(r255, g255, b255);
            cnvsFiveCtx.strokeStyle=RGBHex; 
            cnvsFiveCtx.moveTo(lastXShiftCnvs, lastYShiftCnvs);
            cnvsFiveCtx.lineTo(xShiftCnvs, yShiftCnvs);
            cnvsFiveCtx.stroke();  
            //cnvsFiveCtx.beginPath();
            //cnvsFiveCtx.arc(xShiftCnvs, yShift0Cnvs, dSize0Cnvs, 0, 2*Math.PI);
            //RGBHex = colHex(r2550, g2550, b2550);
            //cnvsFiveCtx.strokeStyle = RGBHex;
            //cnvsFiveCtx.stroke();
            cnvsFiveCtx.beginPath();
            RGBHex = colHex(r2550, g2550, b2550);
            cnvsFiveCtx.strokeStyle=RGBHex; 
            cnvsFiveCtx.moveTo(lastXShiftCnvs, lastYShift0Cnvs);
            cnvsFiveCtx.lineTo(xShiftCnvs, yShift0Cnvs);
            cnvsFiveCtx.stroke();  
            //cnvsFiveCtx.beginPath();
            //cnvsFiveCtx.arc(xShiftCnvs, yShiftNCnvs, dSize0Cnvs, 0, 2*Math.PI);
            //RGBHex = colHex(r255N, g255N, b255N);
            //cnvsFiveCtx.strokeStyle = RGBHex;
            //cnvsFiveCtx.stroke();
            cnvsFiveCtx.beginPath();
            RGBHex = colHex(r255N, g255N, b255N);
            cnvsFiveCtx.strokeStyle=RGBHex; 
            cnvsFiveCtx.moveTo(lastXShiftCnvs, lastYShiftNCnvs);
            cnvsFiveCtx.lineTo(xShiftCnvs, yShiftNCnvs);
            cnvsFiveCtx.stroke();  

            lastXShiftCnvs = xShiftCnvs;
            lastYShiftCnvs = yShiftCnvs;
            lastYShift0Cnvs = yShift0Cnvs;
            lastYShiftNCnvs = yShiftNCnvs;
        }
           //monochromatic disk lambda
                yFinesse = 0.0;
                barHeight = 200;
                barWidth = 2;
                RGBHex = "#000000";
                var xShiftDum = YBar(diskLambda, minXData, maxXData, xAxisLength,
                barWidth, barHeight,
                        yFinesse, RGBHex, plotFiveId, cnvsFiveCtx);
        txtPrint("<span style='font-size:xx-small'>Filter</span>",
                xShiftDum, titleOffsetY+60, lineColor, plotFiveId);
    }


//
//
//  *****   PLOT EIGHT / PLOT 8
//
//
// Plot eight - Grotrian diagram for ionization stage and excitation level selected
//
//

// Always do this line-related stuff anyway...
    var c = 2.9979249E+10; // light speed in vaccuum in cm/s
    var h = 6.62606957E-27; //Planck's constant in ergs sec
    var logC = Math.log(c);
    var logH = Math.log(h);
    var eV = 1.602176565E-12; // eV in ergs
    var logEv = Math.log(eV);
    //Log of line-center wavelength in cm
   // var logLam0 = Math.log(lam0);
    //// energy of b-b transition
//
 //   var logTransE = logH + logC - logLam0 - logEv; // last term converts back to cgs units

  //  // Energy of upper E-level of b-b transition
   // var chiU = chiL + Math.exp(logTransE);
    if (ifShowLogNums === true) {
//
        var plotRow = 4;
        var plotCol = 2;
        // Determine which ionization stage gas the majority population and scale the axis 
        /// with that population
        // From function levelPops():
        // logNums is an array of logarithmic number densities at tau=1
        // Row 0: neutral stage population
        // Row 1: ionized stage population
        // Row 2: doubly ionized stage population
        // Row 3: triply ionized stage population
        //minXData = Math.min(logNums[0][tTau1], logNums[1][tTau1], logNums[4][tTau1]); 
        maxXData = Math.max(logNums[0], logNums[1], logNums[2], logNums[3]);
        //minXData = logE * minXData; 
        maxXData = logE * maxXData; 
        minXData = 0.0;

        var xAxisName = "<span title='Logarithmic number density of particles at <em>&#964</em>_Ros=1'>Log<sub>10</sub> <em>N</em><sub>l</sub>(<em>&#964</em><sub>Ros</sub>=1) cm<sup>-3</sup></span>";
        var minYData = 0.0;
        //if (ionized) {
        //    var maxYData = chiI1 + chiU + 1.0; //eV
        //} else {
        //    var maxYData = chiI1 + 1.0;
        //}
        var maxYData = ionE[0] + ionE[1] + ionE[2];

        var yAxisName = "<span title='Atomic excitation energy'><a href='http://en.wikipedia.org/wiki/Excited_state' target='_blank'>Excitation<br /> E</a> (<a href='http://en.wikipedia.org/wiki/Electronvolt' target='_blank'>eV</a>)</span>";
        //(xRange, xOffset, yRange, yOffset, wColor, plotEightId);

        var fineness = "coarse";
        //var cnvsCtx = washer(plotRow, plotCol, wColor, plotEightId, cnvsId);
        var panelOrigin = washer(plotRow, plotCol, panelWidth, wColor, plotEightId, cnvsEightId);
        panelX = panelOrigin[0];
        panelY = panelOrigin[1];
        cnvsEightCtx.fillStyle = wColor;
        cnvsEightCtx.fillRect(0, 0, panelWidth, panelHeight);
        var xAxisParams = XAxis(panelX, panelY, xAxisLength,
                minXData, maxXData, xAxisName, fineness,
                plotEightId, cnvsEightCtx);
        var yAxisParams = YAxis(panelX, panelY,
                minYData, maxYData, yAxisName,
                plotEightId, cnvsEightCtx);
        //
        //xOffset = xAxisParams[0];
        //yOffset = xAxisParams[4];
        var rangeXData = xAxisParams[1];
        var deltaXData = xAxisParams[2];
        var deltaXPxl = xAxisParams[3];
        var rangeYData = yAxisParams[1];
        var deltaYData = yAxisParams[2];
        var deltaYPxl = yAxisParams[3];
        var xLowerYOffset = xAxisParams[5];
        minXData = xAxisParams[6]; //updated value
        minYData = yAxisParams[6]; //updated value
        maxXData = xAxisParams[7]; //updated value
        maxYData = yAxisParams[7]; //updated value        
      
        // Add title annotation:

        titleX = panelX + titleOffsetX;
        titleY = panelY + titleOffsetY;
        txtPrint("<span style='font-size:normal; color:blue'>" + ionEqElement + ": Ionization equilibrium</span>",
                titleOffsetX, titleOffsetY, lineColor, plotEightId);
   
//legend:
        var val0 = logE*logNums[0];
        val0 = val0.toPrecision(3);
        var val1 = logE*logNums[1];
        val1 = val1.toPrecision(3);
        var val2 = logE*logNums[2];
        val2 = val2.toPrecision(3);
        txtPrint("<span style='font-size:xx-small'><strong>&#967<sub>I</sub>: (eV), log<sub>10</sub>N</strong> </br>"
             + "I: " + ionE[0] + ", " + val0 + "</br>"
             + "II: " + ionE[1] + ", " + val1 + "</br>"
             + "III: " + ionE[2] + ", " + val2 + "</span>",
             titleOffsetX+250,  titleOffsetY-10, lineColor, plotEightId);
 
        //
        // Second special "y-ticks" for lower and upper E-levels of b-b transition, and total ion stage populations 
        // ionization energy

        //var yLabelXOffset = xOffset - 3 * tickLength; //height & width reversed for y-ticks
        //var yLabelXOffsetStr = numToPxStrng(yLabelXOffset);
        // From function levelPops():
        // logNums is a 2D 3 x numDeps array of logarithmic number densities
        // Row 0: neutral stage population
        // Row 1: singly ionized stage population
        // Row 2: level population of lower level of bb transition (could be in either stage I or II!) 
        // Row 3: level population of upper level of bb transition (could be in either stage I or II!)
        // Row 4: doubly ionized stage population
        var yData = [0.0, ionE[0], ionE[0]+ionE[1], ionE[0]+ionE[1]+ionE[2]];
        var yRightTickValStr = ["<em>&#967</em><sub>I</sub>", "<em>&#967</em><sub>II</sub>", "<em>&#967</em><sub>III</sub>", "<em>&#967</em><sub>IV</sub>"];
        // Offset for labelling on right of plot
        var yRightLabelXOffset0 = xAxisXCnvs + xAxisLength;
        var yRightLabelXOffset = [yRightLabelXOffset0 + 5, 
                                  yRightLabelXOffset0 + 5, 
                                  yRightLabelXOffset0 + 30, 
                                  yRightLabelXOffset0 + 30, 
                                  yRightLabelXOffset0 + 5];
        // No!:
        // Pointers into logNums rows must be in order of increasing atomic E:
        //   var lPoint = []; // declaration
        //   if (ionized) {
        //      lPoint = [0, 1, 2, 3];
        //   } else {
        //       lPoint = [0, 2, 3, 1];
        //   }

            var RGBHex = "#FF0000";
            var tickWidthPops = 2;
        xFinesse = 0;
        yFinesse = 0;
        var yShiftL = 0;
        var yShiftU = 0;
        for (var i = 0; i < yData.length; i++) {

            ii = 1.0 * i;

        //barHeight = 1.0;
        //barWidth = xRange;
            yShift = XBar(yData[i], minYData, maxYData, xAxisLength, tickLength,
                xFinesse, lineColor, plotEightId, cnvsEightCtx);
            
            // Now over-plot with the width of the "y-tickmark" scaled by the 
            // log number density in each E-level:
            //var xRangePops = Math.floor(xRange * (logE*logNums[lPoint[i]][tTau1] / maxXData));
            var xRangePops = Math.floor(xAxisLength * ( (logE * logNums[i] - minXData) / (maxXData - minXData)));
            if (xRangePops < 0.0){
               xRangePops = 0.0;
            }
            var tickWidthPops = 2;

 // Energy level logarithmic population horizontal bars:
           yShift = XBar(yData[i], minYData, maxYData, xRangePops, tickWidthPops,
                    xFinesse, RGBHex, plotEightId, cnvsEightCtx);


           txtPrint(yRightTickValStr[i], yRightLabelXOffset[i], 
                yShift, lineColor, plotEightId);
         //cnvsEightCtx.font="normal normal normal 8pt arial";
         //cnvsEightCtx.fillText(yRightTickValStr[i], yRightLabelXOffset, yShift);
        }  // end y-tickmark loop, i

// Add ionization stage labels:

        //txtPrint("<span title='Singly ionized stage'>II</span>", xAxisXCnvs + xAxisLength - 15, 
        //        (yAxisYCnvs + yAxisLength) - yAxisLength, lineColor, plotEightId);
        //txtPrint("<span title='Neutral stage'>I</span>", xAxisXCnvs + xAxisLength - 15, 
        //        (yAxisYCnvs + yAxisLength) - yAxisLength / 2, lineColor, plotEightId);

    }

// ****************************************
    //
    //
    //  *****   PLOT FOURTEEN / PLOT 14
    //

    // Plot fourteen : log(Tau) vs log(kappa)
    //

    if (ifShowAtmos === true) {
//
        var plotRow = 4;
        var plotCol = 2;
        var minXData = logE * tauRos[1][0] - 0.0;
        var maxXData = logE * tauRos[1][numDeps - 1];
        var xAxisName = "<span title='Rosseland mean optical depth'><a href='http://en.wikipedia.org/wiki/Optical_depth_%28astrophysics%29' target='_blank'>Log<sub>10</sub> <em>&#964</em><sub>Ros</sub></a></span>";
        // Don't use upper boundary condition as lower y-limit - use a couple of points below surface:
        //var numYTicks = 6;
        // Build total P from P_Gas & P_Rad:

        //var minYData = logE * kappaRos[1][1] - 1.0; // Avoid upper boundary condition [i]=0
        //var maxYData = logE * kappaRos[1][numDeps - 1];
        var minYData = logE*kappaRos[1][1];
        var maxYData = logE*kappaRos[1][numDeps-1];
        var yAxisName = "Log<sub>10</sub> <em>&#954</em><sub>Ros</sub> <br />(cm<sup>2</sup> g<sup>-1</sup>)";

        var fineness = "normal";
        var panelOrigin = washer(plotRow, plotCol, panelWidth, wColor, plotFourteenId, cnvsFourteenId);
        panelX = panelOrigin[0];
        panelY = panelOrigin[1];
        cnvsFourteenCtx.fillStyle = wColor;
        cnvsFourteenCtx.fillRect(0, 0, panelWidth, panelHeight);
        var xAxisParams = XAxis(panelX, panelY, xAxisLength,
                minXData, maxXData, xAxisName, fineness,
                plotFourteenId, cnvsFourteenCtx);

        //xOffset = xAxisParams[0];
        var rangeXData = xAxisParams[1];
        var deltaXData = xAxisParams[2];
        var deltaXPxl = xAxisParams[3];
        //yOffset = xAxisParams[4];
        var xLowerYOffset = xAxisParams[5];
        minXData = xAxisParams[6]; //updated value
        maxXData = xAxisParams[7]; //updated value
        //no! var cnvsCtx = xAxisParams[8];
        var yAxisParams = YAxis(panelX, panelY,
                minYData, maxYData, yAxisName,
                plotFourteenId, cnvsFourteenCtx);
        var rangeYData = yAxisParams[1];
        var deltaYData = yAxisParams[2];
        var deltaYPxl = yAxisParams[3];
        minYData = yAxisParams[6]; //updated value
        maxYData = yAxisParams[7]; //updated value

        yFinesse = 0;
        xFinesse = 0;
        titleX = panelX + titleOffsetX;
        titleY = panelY + titleOffsetY;
        txtPrint("log<sub>10</sub> <a href='https://en.wikipedia.org/wiki/Absorption_(electromagnetic_radiation)' title='mass extinction coefficient' target='_blank'>Extinction</a>",
                titleOffsetX, titleOffsetY, lineColor, plotFourteenId);
        txtPrint("<span style='font-size:small'>"
                + "<span><em>&#954</em><sub>Ros</sub></span> ", 
                titleOffsetX, titleOffsetY+35, lineColor, plotFourteenId);

        //Data loop - plot the result!

        //var dSizeG = 2.0;
        var dSizeCnvs = 1.0;
        var opac = 1.0; //opacity
        // RGB color
        // PTot:
        var r255 = 0;
        var g255 = 0;
        var b255 = 255; //blue
        // PGas:
        var r255G = 0;
        var g255G = 255;
        var b255G = 100; //green
        // PRad:
        var r255R = 255;
        var g255R = 0;
        var b255R = 0; //red

       var it360 = lamPoint(numLams, lambdaScale, 1.0e-7*360.0);
       var it500 = lamPoint(numLams, lambdaScale, 1.0e-7*500.0);
//Good odea, but spectrum currently doesn't go out this far:
       //var it1600 = lamPoint(numLams, lambdaScale, 1.0e-7*1642.0);
       var it1000 = lamPoint(numLams, lambdaScale, 1.0e-7*1000.0);

        var ii;
            var xTickPosCnvs = xAxisLength * (logE * tauRos[1][0] - minXData) / rangeXData; // pixels
            // horizontal position in pixels - data values increase rightward:
            var lastXShiftCnvs = xAxisXCnvs + xTickPosCnvs;
            // vertical position in pixels - data values increase upward:
            var yTickPosCnvs = yAxisLength * (logE * kappaRos[1][0] - minYData) / rangeYData;
            var lastYShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;


        for (var i = 1; i < numDeps; i++) {

            ii = 1.0 * i;
            var xTickPosCnvs = xAxisLength * (logE * tauRos[1][i] - minXData) / rangeXData; // pixels

            // horizontal position in pixels - data values increase rightward:
            var xShiftCnvs = xAxisXCnvs + xTickPosCnvs;

            // vertical position in pixels - data values increase upward:
            var yTickPosCnvs = yAxisLength * (logE * kappaRos[1][i] - minYData) / rangeYData;
            var yShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;

 //console.log("i " + i + " lastXShiftCnvs " + lastXShiftCnvs);

//log kappa_Ros
//Plot points
//            cnvsFourteenCtx.beginPath();
//            cnvsFourteenCtx.strokeStyle=lineColor;
//            cnvsFourteenCtx.arc(xShiftCnvs, yShiftCnvs, dSizeCnvs, 0, 2*Math.PI);
//            cnvsFourteenCtx.stroke();
//Line plot
            cnvsFourteenCtx.beginPath();
            cnvsFourteenCtx.strokeStyle=lineColor;
            cnvsFourteenCtx.moveTo(lastXShiftCnvs, lastYShiftCnvs);
            cnvsFourteenCtx.lineTo(xShiftCnvs, yShiftCnvs);
            cnvsFourteenCtx.stroke();

            lastXShiftCnvs = xShiftCnvs;
            lastYShiftCnvs = yShiftCnvs;
        }

// Tau=1 cross-hair

        var barWidth = 1.0;
        var barHeight = yAxisLength;
        var barColor = "#777777";
        var xShift = YBar(logE*tauRos[1][tTau1], minXData, maxXData, xAxisLength,
                barWidth, barHeight,
                yFinesse, barColor, plotFourteenId, cnvsFourteenCtx);

        var barHeight = 1.0;
        var yShift = XBar(logE * kappaRos[1][tTau1], minYData, maxYData, xAxisLength, barHeight,
                xFinesse, barColor, plotFourteenId, cnvsFourteenCtx);
        txtPrint("<span style='font-size:small; color:#444444'><em>&#964</em><sub>Ros</sub>=1</span>",
                xShift, yShift, lineColor, plotFourteenId);
    }




// Detailed model output section:

//    
// Set up the canvas:
//

    // **********  Basic canvas parameters: These are numbers in px - needed for calculations:
    // All plots and other output must fit within this region to be white-washed between runs

    var xRangePrint = 2250;
    var yRangePrint = 20000;
    var xOffsetPrint = 10;
    var yOffsetPrint = yOffsetText + yRangeText + ((numRows+1) * spacingY) + 5;
    var charToPx = 4; // width of typical character font in pixels - CAUTION: finesse!

    var zeroInt = 0;
    //these are the corresponding strings ready to be assigned to HTML style attributes


    var xRangePrintStr = numToPxStrng(xRangePrint);
    var yRangePrintStr = numToPxStrng(yRangePrint);
    var xOffsetPrintStr = numToPxStrng(xOffsetPrint);
    var yOffsetPrintStr = numToPxStrng(yOffsetPrint);
    // Very first thing on each load: White-wash the canvas!!

    var washTId = document.createElement("div");
    var washTWidth = xRangePrint + xOffsetPrint;
    var washTHeight = yRangePrint + yOffsetPrint;
    var washTTop = yOffsetPrint;
    var washTWidthStr = numToPxStrng(washTWidth);
    var washTHeightStr = numToPxStrng(washTHeight);
    var washTTopStr = numToPxStrng(washTTop);

    washTId.id = "washT";
    washTId.style.position = "absolute";
    washTId.style.width = washTWidthStr;
    washTId.style.height = washTHeightStr;
    washTId.style.marginTop = washTTopStr;
    washTId.style.marginLeft = "0px";
    washTId.style.opacity = 1.0;
    washTId.style.backgroundColor = "#FFFFFF";
    //washId.style.zIndex = -1;
    washTId.style.zIndex = 0;
    //washTId.style.border = "2px blue solid";

    //Wash the canvas:
    printModelId.appendChild(washTId);

    // R & L_Bol:
    var colr = 0;
    var lineHeight = 17;
    var value, yTab;
    var vOffset = 60;
    var txtColor = "#000000"; //black

    if (ifPrintAtmos == true) {

        txtPrint("Vertical atmospheric structure", 10, yOffsetPrint, txtColor, printModelId);
        //Column headings:

        var xTab = 190;
        txtPrint("i", 10, yOffsetPrint + lineHeight, txtColor, printModelId);
        txtPrint("log<sub>10</sub> <em>&#964</em><sub>Rosseland</sub>", 10 + xTab, yOffsetPrint + lineHeight, txtColor, printModelId);
        txtPrint("log<sub>10</sub> depth (cm)", 10 + 2 * xTab, yOffsetPrint + lineHeight, txtColor, printModelId);
        txtPrint("log<sub>10</sub> <em>T</em><sub>Kin</sub> (K)", 10 + 3 * xTab, yOffsetPrint + lineHeight, txtColor, printModelId);
        txtPrint("log<sub>10</sub> <em>P</em><sub>Gas</sub> (dynes cm<sup>-2</sup>)", 10 + 4 * xTab, yOffsetPrint + lineHeight, txtColor, printModelId);
        txtPrint("log<sub>10</sub> <em>P</em><sub>Rad</sub> (dynes cm<sup>-2</sup>)", 10 + 5 * xTab, yOffsetPrint + lineHeight, txtColor, printModelId);
        txtPrint("log<sub>10</sub> <em>&#961</em> (g cm<sup>-3</sup>)", 10 + 6 * xTab, yOffsetPrint + lineHeight, txtColor, printModelId);
        txtPrint("log<sub>10</sub> <em>N</em><sub>e</sub> (cm<sup>-3</sup>)", 10 + 7 * xTab, yOffsetPrint + lineHeight, txtColor, printModelId);
        txtPrint("log<sub>10</sub><em>&#956</em> (g)", 10 + 8 * xTab, yOffsetPrint + lineHeight, txtColor, printModelId);
        txtPrint("log<sub>10</sub> <em>&#954</em> (cm<sup>2</sup> g<sup>-1</sup>)", 10 + 9 * xTab, yOffsetPrint + lineHeight, txtColor, printModelId);
        txtPrint("log<sub>10</sub> <em>&#954</em><sub>500</sub> (cm<sup>2</sup> g<sup>-1</sup>)", 10 + 10 * xTab, yOffsetT + lineHeight, txtColor, printModelId);


        for (var i = 0; i < numDeps; i++) {
            yTab = yOffsetPrint + vOffset + i * lineHeight;
            numPrint(i, 10, yTab, txtColor, printModelId);
            value = logE * tauRos[1][i];
            value = value.toPrecision(5);
            numPrint(value, 10 + xTab, yTab, txtColor, printModelId);
            value = logE * depths[1][i];
            value = value.toPrecision(5);
            numPrint(value, 10 + 2 * xTab, yTab, txtColor, printModelId);
            value = logE * temp[1][i];
            value = value.toPrecision(5);
            numPrint(value, 10 + 3 * xTab, yTab, txtColor, printModelId);
            value = logE * pGas[1][i];
            value = value.toPrecision(5);
            numPrint(value, 10 + 4 * xTab, yTab, txtColor, printModelId);
            value = logE * pRad[1][i];
            value = value.toPrecision(5);
            numPrint(value, 10 + 5 * xTab, yTab, txtColor, printModelId);
            value = logE * rho[1][i];
            value = value.toPrecision(5);
            numPrint(value, 10 + 6 * xTab, yTab, txtColor, printModelId);
            value = logE * Ne[1][i];
            value = value.toPrecision(5);
            numPrint(value, 10 + 7 * xTab, yTab, txtColor, printModelId);
            value = logE * mmw[1][i];
            value = value.toPrecision(5);
            numPrint(value, 10 + 8 * xTab, yTab, txtColor, printModelId);
            value = logE * kappaRos[1][i];
            value = value.toPrecision(5);
            numPrint(value, 10 + 9 * xTab, yTab, txtColor, printModelId);
            value = logE * kappa500[1][i];
            value = value.toPrecision(5);
            numPrint(value, 10 + 10 * xTab, yTab, txtColor, printModelId);
        }

    }


    if (ifPrintSED == true) {

        txtPrint("Monochromatic surface flux spectral energy distribution (SED)", 10, yOffsetPrint, txtColor, printModelId);
        //Column headings:

        var xTab = 190;
        txtPrint("log<sub>10</sub> <em>&#955</em> (cm)", 10, yOffsetPrint + lineHeight, txtColor, printModelId);
        txtPrint("log<sub>10</sub> <em>F</em><sub>&#955</sub> (ergs s<sup>-1</sup> cm<sup>-2</sup> cm<sup>-1</sup>)", 10 + xTab, yOffsetPrint + lineHeight, txtColor, printModelId);
        for (var i = 0; i < numMaster; i++) {
            yTab = yOffsetPrint + vOffset + i * lineHeight;
            value = logE * Math.log(masterLams[i]);
            value = value.toPrecision(9);
            numPrint(value, 10, yTab, txtColor, printModelId);
            value = logE * masterFlux[1][i];
            value = value.toPrecision(7);
            numPrint(value, 10 + xTab, yTab, txtColor, printModelId);
        }
    }


    if (ifPrintIntens == true) {

        txtPrint("Monochromatic specific intensity distribution", 10, yOffsetPrint, txtColor, printModelId);
        //Column headings:

        var xTab = 100;
        txtPrint("log<sub>10</sub><em>&#955</em> (cm)", 10, yOffsetPrint + lineHeight, txtColor, printModelId);
        txtPrint("log<sub>10</sub><em>I</em><sub>&#955</sub>(<em>&#952</em>) (ergs s<sup>-1</sup> cm<sup>-2</sup> cm<sup>-1</sup> steradian<sup>-1</sup>)",
                10 + xTab, yOffsetPrint + lineHeight, txtColor, printModelId);
        for (var j = 0; j < numThetas; j += 2) {
            value = cosTheta[1][j].toPrecision(5);
            txtPrint("cos <em>&#952</em>=" + value, 10 + (j + 1) * xTab, yOffsetPrint + 3 * lineHeight, txtColor, printModelId);
        }

        for (var i = 0; i < numMaster; i++) {
            yTab = yOffsetPrint + vOffset + (i+1) * lineHeight;
            value = logE * Math.log(masterLams[i]);
            value = value.toPrecision(9);
            numPrint(value, 10, yTab, txtColor, printModelId);
            for (var j = 0; j < numThetas; j += 2) {
                value = logE * masterIntens[i][j];
                value = value.toPrecision(7);
                numPrint(value, 10 + (j + 1) * xTab, yTab, txtColor, printModelId);
            }
        }
    }

    if (ifPrintLine == true) {


        txtPrint("Monochromatic surface flux: Spectrum synthesis region", 10, yOffsetPrint, txtColor, printModelId);
        //Column headings:

        var xTab = 190;
        txtPrint("<em>&#955</em> (nm)", 10, yOffsetPrint + lineHeight, txtColor, printModelId);
        txtPrint("<em>F</em><sub>&#955</sub> / <em>F</em><sup>C</sup><sub>&#955</sub>", 10 + xTab, yOffsetPrint + lineHeight, txtColor, printModelId);
        for (var i = 0; i < numSpecSyn; i++) {
            yTab = yOffsetPrint + vOffset + i * lineHeight;
            value = 1.0e7 * specSynLams[i];
            value = value.toPrecision(9);
            numPrint(value, 10, yTab, txtColor, printModelId);
            value = specSynFlux[0][i];
            value = value.toPrecision(7);
            numPrint(value, 10 + xTab, yTab, txtColor, printModelId);
        }

    }


    if (ifPrintLDC == true) {

        txtPrint("Linear monochromatic continuum limb darkening coefficients (LCD)", 10, yOffsetPrint, txtColor, printModelId);
        //Column headings:

        var xTab = 190;
        txtPrint("log<sub>10</sub> <em>&#955</em> (cm)", 10, yOffsetPrint + lineHeight, txtColor, printModelId);
        txtPrint("LDC", 10 + xTab, yOffsetPrint + lineHeight, txtColor, printModelId);
        for (var i = 0; i < numLams; i++) {
            yTab = yOffsetPrint + vOffset + i * lineHeight;
            value = logE * Math.log(lambdaScale[i]);
            value = value.toPrecision(9);
            numPrint(value, 10, yTab, txtColor, printModelId);
            value = ldc[i];
            value = value.toPrecision(7);
            numPrint(value, 10 + xTab, yTab, txtColor, printModelId);
        }
    }

  if (ifPrintAbnd == true){
     txtPrint("A_12 logarithmic abundnaces (log_10(N_X/N_H)+12)", 10, yOffsetPrint, txtColor, printModelId);
     for (var i = 0; i < nelemAbnd; i++){
        yTab = yOffsetPrint + vOffset + i * lineHeight;
        value = element[i];
        txtPrint(value, 10, yTab, txtColor, printModelId);
        value = abundance[i];
        value = value.toPrecision(3);
        numPrint(value, 10 + xTab, yTab, txtColor, printModelId);
     }
   }

  if (ifPrintJSON == true){
     txtPrint("Compound atmospheric model, SED, and synthetic spectrum output as <a href='https://en.wikipedia.org/wiki/JSON' target='_blank'> JSON</a> string", 
       10, yOffsetPrint, txtColor, printModelId);
          yTab = yOffsetPrint + vOffset;
          txtPrint(xmlhttp.responseText, 0, yTab, txtColor, printModelId);
  }

/*
  if (ifPrintLogNums == true){
     txtPrint(ionEqElement + ": Ionization stage ground state E (eV) & total ion stage log_10 N_k(tau=1))", 10, yOffsetPrint, txtColor, printModelId);
        //Column headings:

        var xTab = 190;
        txtPrint("I", 10, yOffsetPrint + lineHeight, txtColor, printModelId);
        txtPrint("II", 10 + xTab, yOffsetPrint + lineHeight, txtColor, printModelId);
        txtPrint("III", 10 + 2 * xTab, yOffsetPrint + lineHeight, txtColor, printModelId);
        txtPrint("IV", 10 + 3 * xTab, yOffsetPrint + lineHeight, txtColor, printModelId);
        yTab = yOffsetPrint + vOffset;
     for (var i = 0; i < numStages; i++){
        value = logE*ionE[i];
        value = value.toPrecision(3);
        numPrint(value, 10 + i*xTab, yTab, txtColor, printModelId);
     }
        yTab = yOffsetPrint + vOffset + i * lineHeight;
     for (var i = 0; i < numStages; i++){
        value = logE*logNums[i];
        value = value.toPrecision(3);
        numPrint(value, 10 + i*xTab, yTab, txtColor, printModelId);
     }
   }
*/

//
//
//  *******    END COMPUTE & VISUALIZATION CODE
// 
//
         } // End if (xmlhttp.readyState == 4 && xmlhttp.status == 200) condition
   };  // End xmlhttp.onreadystatechange function


    return;

}; //end function main()

