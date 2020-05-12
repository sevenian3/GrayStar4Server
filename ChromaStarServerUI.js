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
 *
 *  * Co-developers:
 *  *
 *  * Lindsey Burns (SMU) - 2017 - "lburns"
 *  * Jason Bayer (SMU) - 2017 - "JB"
 *
 * 
 * Open source pedagogical computational stellar astrophysics
 *
 * 1D, static, plane-parallel, LTE stellar atmospheric model
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

var rEarth = 6.371e8;  //# cm
var logREarth = Math.log(rEarth);

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

//Links required to create elements easily
                      //JB
    var xmlns = "http://www.w3.org/2000/xmlns/";
    var xmlnsLink = "xmlns:xlink";
    var xmlnsLink2 = "http://w3.org/1999/xlink";
    var xmlW3 = "http://www.w3.org/2000/svg";
              //JB

var tiny = 1.0e-49;
var logTiny = Math.log(tiny);


var lineColor = "#000000"; //black
var lineThick = 1;
var xTickPosCnvs = 0;
var yTickPosCnvs = 0;
var xShiftCnvs = 0;
var yShiftCnvs = 0;
var xPhys = 0;
var yPhys = 0;

//We think everything will be a line or a circle:
    var listOfLineNodes = document.querySelectorAll("line");
    var listOfCircNodes = document.querySelectorAll("circle");

    //var numSVGNodes = listOfSVGNodes.length;
    var numLineNodes = listOfLineNodes.length;
    var numCircNodes = listOfCircNodes.length;

//Remove line elements (axues, tickmarks, barns, etc.)
       if (numLineNodes > 0){
           for (var iNode = 0; iNode < numLineNodes; iNode++){
               listOfLineNodes[iNode].parentNode.removeChild(listOfLineNodes[iNode]);
           }
       } //numLineNodes > 0 condition

//function to output physical data coordinates at clicked cursor location
var dataCoords = function(eventObj, cnvsId,
                          xAxisLength, minXData, rangeXData, xAxisXCnvs,
                          yAxisLength, minYData, rangeYData, yAxisYCnvs){

   //console.log("eventObj.offsetX " + eventObj.offsetX + " eventObj.offsetY " + eventObj.offsetY);
   //console.log("eventObj.pageX " + eventObj.pageX + " eventObj.pageY " + eventObj.pageY);
   //console.log("eventObj.clientX " + eventObj.clientX + " eventObj.clientY " + eventObj.clientY);
   //console.log("eventObj.screenX " + eventObj.screenX + " eventObj.screenY " + eventObj.screenY);
   //var offsetStr = String(eventObj.pageX) + " " + String(eventObj.pageY); 
   //var offsetStr = String(eventObj.clientX) + " " + String(eventObj.clientY); 

//First, erase the previous dataCoords output so output is not clobbered:
   var coordElId;
   if (document.getElementById("coordId") != null){
      //cnvsId.getElementsById("coordId").innerHTML="";
      //console.log("Removing...");
      coordElId = document.getElementById("coordId");
      coordElId.innerHTML=" ";
      coordElId.parentNode.removeChild(coordElId);
   }
   xShiftCnvs = eventObj.offsetX;
   xTickPosCnvs = xShiftCnvs - xAxisXCnvs;
   xPhys = (xTickPosCnvs * rangeXData / xAxisLength) + minXData;
   xPhys = xPhys.toFixed(2);
   yShiftCnvs = eventObj.offsetY;
   yTickPosCnvs = (yAxisYCnvs + yAxisLength) - yShiftCnvs
   yPhys = (yTickPosCnvs * rangeYData / yAxisLength) + minYData;
   yPhys = yPhys.toFixed(2);
   //var offsetStr = String(eventObj.offsetX) + " " + String(eventObj.offsetY); 
   var offsetStr = "<span id='coordId' style='font-size:small'>x,y: " + String(xPhys) + " " + String(yPhys) + "</span>";
   //console.log("offsetStr " + offsetStr);
   //txtPrint(offsetStr, eventObj.pageX, eventObj.pageY, lineColor, cnvsId);
   //txtPrint(offsetStr, eventObj.clientX, eventObj.clientY, lineColor, cnvsId);
   //txtPrint(offsetStr, eventObj.offsetX, eventObj.offsetY, lineColor, cnvsId);
   //txtPrint(offsetStr, 100, 100, lineColor, cnvsId);
   //coordBoxId.innerHTML = offsetStr;

   return offsetStr;

};


// ********************************************

//***************************  Main ******************************



function main() {



//**********************************************************


//Routines for handling interaction with PHP through AJAX and JSON:

var gsAjaxParser = function(num, ajaxStr){

//console.log("gsAjaxParser: ajaxStr " + ajaxStr);
//console.log("num " + num);
//convert one string with a comma delimited set of num values into
// an array of numeric data type values

//go through string and recover scalar values:
// does String.split() work??:
var strArray = ajaxStr.split(","); //split on commas
var ajaxLength = strArray.length;

//console.log("gsAjaxParser: num " + num + " ajaxLength " + ajaxLength);
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
    var numInputs = 26;
//Make settingsId object array by hand:
// setId() is an object constructor
    function setId(nameIn, valueIn) {
        this.name = nameIn;
        this.value = valueIn;
    }
    //
    // settingsId will be an array of objects
    var settingsId = [];
    settingsId.length = numInputs;
    //
    //1st version of each is of JQuery-ui round sliders not available
    // jquery-ui round sliders -->
    //Round sliders Copyright (c) 2015-2016, Soundar
    //      http://roundsliderui.com/
    //
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
    var atmosPressObj = $("#AtmPress").data("roundSlider");
    var atmosPress = 1.0 * atmosPressObj.getValue();
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
    var macroV = 1.0 * $("#macroV").val(); // km/s 
    var rotV = 1.0 * $("#rotV").val(); // km/s 
    var rotI = 1.0 * $("#rotI").val(); // degrees 
    //
    //console.log("lineThresh " + lineThresh);
    var diskLambda = 1.0 * $("#diskLam").val(); //nm
    var diskSigma = 1.0 * $("#diskSigma").val(); //nm
    var logKapFudge = 1.0 * $("#logKapFudge").val(); //log_10 cm^2/g mass extinction fudge
    var RV = 1.0 * $("#RV").val(); // radial velocity of star 
//
    var nOuterIter = $("#nOuterIter").val(); //number of outer HSE-EOS-Opacity iterations
    var nInnerIter = $("#nInnerIter").val(); //number of inner Pe-(ion. fraction) iterations
   //var nOuterIter = 3;
   //var nInnerIter = 3;
    // Add new variables to hold values for new metallicity controls lburns
    var logHeFe = 1.0 * $("#logHeFe").val(); // lburns
    var logCO = 1.0 * $("#logCO").val(); // lburns
    var logAlphaFe = 1.0 * $("#logAlphaFe").val(); // lburns
    var rOrbit = 1.0 * $("#rOrbit").val();  //transiting planet orbital radius (AU)
    var rPlanet = 1.0 * $("#rPlanet").val(); //transiting planet radius (R_Earth)
    var orbI = 1.0 * $("#orbI").val(); //planet's orbital inclincation wrt line-of-sight (degrees)

//    
    settingsId[0] = new setId("<em>T</em><sub>eff</sub>", teff);
    settingsId[1] = new setId("log <em>g</em>", logg);
    settingsId[2] = new setId("[M/H]", logZScale);
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
    settingsId[18] = new setId("RV", RV);
    settingsId[19] = new setId("<span style='color:green'>AtmP</span>", atmosPress);
    settingsId[20] = new setId("<em>[He/Fe]</em>", logHeFe); // lburns
    settingsId[21] = new setId("<em>[C/O]</em>", logCO); // lburns
    settingsId[22] = new setId("<em>[&#945/Fe]</em>", logAlphaFe); // lburns
    settingsId[23] = new setId("<em>r</em><sub>Orb</sub>", rOrbit);
    settingsId[24] = new setId("<em>r</em><sub>Planet</sub>", rPlanet);
    settingsId[25] = new setId("<em>I</em><sub>Orb</sub>", orbI);

    var solvent = "water"; //default intialization

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
    var ifShowLogPP = false;
    //
    var ifPrintNone = true;
    var ifPrintAtmos = false;
    var ifPrintSED = false;
    var ifPrintIntens = false;
    var ifPrintLine = false;
    var ifPrintLDC = false;
    var ifPrintPP = false;
    var ifPrintTrans = false;
    var ifPrintAbnd = false;
    var ifPrintLogNums = false;
    var ifPrintJSON = false;
    //
    var ifTiO = 0;
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

    if ($("#ifTiO").is(":checked")) {
        ifTiO = 1; // checkbox
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

    var ppSpecies = "None"; //default
    ppSpecies = $("#showLogPP").val();
    if (ppSpecies != "None") {
        ifShowLogPP = true; // checkbox
    }


    ////if ($("#showLogNums").is(":checked")) {
    ////    ifShowLogNums = true; // checkbox
    //// }
    //var ionEqElement = "None"; //default
    //ionEqElement = $("#showLogNums").val();
    //if (ionEqElement != "None") {
    //    ifShowLogNums = true; // checkbox
   // }
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
    if ($("#printPP").is(":checked")) {
        ifPrintPP = true; // checkbox
    }
    if ($("#printTrans").is(":checked")) {
        ifPrintTrans = true; // checkbox
    }
    if ($("#printJSON").is(":checked")) {
        ifPrintJSON = true; // checkbox
    }

  //Spectrum synthesis line sampling options:
    var switchSampl = "fine"; //default initialization
// Coarse sampling: 
    if ($("#coarse").is(":checked")) {
        switchSampl = $("#coarse").val(); // radio 
    }
// Fine sampling: (default)
    if ($("#fine").is(":checked")) {
        switchSampl = $("#fine").val(); // radio 
    }
   //console.log("line sampling " + switchSampl);

  //Spectrum synthesis wavelength scale options:
    var vacAir = "vacuum"; //default initialization
// Wavelengths in Air : 
    if ($("#air").is(":checked")) {
        vacAir = $("#air").val(); // radio 
    }
// Wavelengths in vacuum: (default)
    if ($("#vacuum").is(":checked")) {
        vacAir = $("#vacuum").val(); // radio 
    }

    //       


    var switchStar = "None";
    var numPreStars = 9;
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

// Proxima Centauri (lburns)
    if ($("#alphacentc").is(":checked")) {
      switchStar = $("#alphacentc").val(); // radio
    }

// Fomalhaut (lburns)
    if ($("#fomalhaut").is(":checked")) {
      switchStar = $("#fomalhaut").val(); // radio
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
        $("#starMass").roundSlider("setValue", "1.0");
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
        $("#starMass").roundSlider("setValue", "1.1");
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
        logAlphaFe = 0.3;
        settingsId[22].value = 0.3;
        $("#logAlphaFe").val(0.3);
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
        $("#starMass").roundSlider("setValue", "2.1");
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
        $("#starMass").roundSlider("setValue", "3.8");
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
        $("#starMass").roundSlider("setValue", "1.4");
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
        $("#starMass").roundSlider("setValue", "0.6");
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
        $("#starMass").roundSlider("setValue", "1.1");
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
//Alpha Centauri C added 05/24 lburns
    if (switchStar === "Alpha Centauri C") {
      var teff = 3050.0;
      settingsId[0].value = 3050.0;
      //$("#Teff").val(3050.0);
      $("#Teff").roundSlider("setValue", "3050.0");
      var logg = 4.6;
      settingsId[1].value = 4.6;
      //$("#logg").val(4.6);
      $("#logg").roundSlider("setValue", "4.6");
      var log10ZScale = 0.0;
      settingsId[2].value = 0.0;
      //$("#zScale").val(0.0);
      $("#zScale").roundSlider("setValue", "0.0");
      var massStar = 0.1;
      settingsId[3].value = 0.1;
      //$("#starMass").val(0.12);
      $("#starMass").roundSlider("setValue", "0.1");
      var logKapFudge = 0.0;
      settingsId[17].value = 0.0;
      $("#logKapFudge").val(0.0);
    } 

//Fomalhaut added 06/15 lburns
    if (switchStar === "Fomalhaut") {
      var teff = 8590.0;
      settingsId[0].value = 8590.0;
      //$("#Teff").val(8590.0);
      $("#Teff").roundSlider("setValue", "8590.0");
      var logg = 4.2;
      settingsId[1].value = 4.2;
      //$("#logg").val(4.2);
      $("#logg").roundSlider("setValue", "4.2");
      var log10ZScale = 0.0;
      settingsId[2].value = 0.0;
      //$("#zScale").val(0.0);
      $("#zScale").roundSlider("setValue", "0.0");
      var massStar = 1.9;
      settingsId[3].value = 1.9;
      //$("#starMass").val(1.92);
      $("#starMass").roundSlider("setValue", "1.9");
      var logKapFudge = 0.0;
      settingsId[17].value = 0.0;
      $("#logKapFudge").val(0.0);   
    }



    var switchSolvent = "Water";
    var numSolvents = 4;

// Water 
    if ($("#water").is(":checked")) {
        switchSolvent = $("#water").val(); // radio 
        solvent = "water";
    }
// Methane 
    if ($("#methane").is(":checked")) {
        switchSolvent = $("#methane").val(); // radio 
        solvent = "methane";
    }
// Ammonia 
    if ($("#ammonia").is(":checked")) {
        switchSolvent = $("#ammonia").val(); // radio 
        solvent = "ammonia";
    }
// Carbon dioxide 
    if ($("#carbonDioxide").is(":checked")) {
        switchSolvent = $("#carbonDioxide").val(); // radio 
        solvent = "carbonDioxide";
    }


    var switchPlanet = "None";
    var numPrePlanets = 4;
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
        var solvent = "water";
        $('input[name="switchSolvent"]').val(["water"]);
        atmosPress = 101.3;
        var AtmPress = 101.3;
        settingsId[19].value = 101.3;
        //$("#AtmPress").val(101.3);
        $("#AtmPress").roundSlider("setValue", "100.0");
        greenHouse = 33.0;
        var GHTemp = 33.0;
        settingsId[4].value = 33.0;
        //$("#GHTemp").val(33.0);
        $("#GHTemp").roundSlider("setValue", "33.0");
        var albedo = 0.3;
        var Albedo = 0.3;
        settingsId[5].value = 0.3;
        //$("#Albedo").val(0.3);
        $("#Albedo").roundSlider("setValue", "0.3");
    }

// Mars 
    if ($("#mars").is(":checked")) {
        switchPlanet = $("#mars").val(); // radio 
    }

    if (switchPlanet === "Mars") {
        var solvent = "water";
        $('input[name="switchSolvent"]').val(["water"]);
        atmosPress = 0.6;
        var AtmPress = 0.6; //kPa
        settingsId[19].value = 0.6;
        //$("#AtmPress").val(0.6);
        $("#AtmPress").roundSlider("setValue", "0.6");
        greenHouse = 5.0;
        var GHTemp = 5.0;
        settingsId[4].value = 5.0;
        //$("#GHTemp").val(5.0);
        $("#GHTemp").roundSlider("setValue", "5.0");
        albedo = 0.25;
        var Albedo = 0.25;
        settingsId[5].value = 0.25;
        //$("#Albedo").val(0.25);
        $("#Albedo").roundSlider("setValue", "0.25");
    }

// Venus 
    if ($("#venus").is(":checked")) {
        switchPlanet = $("#venus").val(); // radio 
    }

    if (switchPlanet === "Venus") {
        solvent = "water";
        $('input[name="switchSolvent"]').val(["water"]);
        atmosPress = 9300.0;
        var AtmPress = 9300.0;
        settingsId[19].value = 9300;
        //$("#AtmPress").val(9300);
        $("#AtmPress").roundSlider("setValue", "9300");
        greenHouse = 510.0;
        var GHTemp = 510.0;
        settingsId[4].value = 510.0;
        //$("#GHTemp").val(510.0);
        $("#GHTemp").roundSlider("setValue", "510.0");
        albedo = 0.75;
        var Albedo = 0.75;
        settingsId[5].value = 0.75;
        //$("#Albedo").val(0.75);
        $("#Albedo").roundSlider("setValue", "0.75");
    }

// Titan
    if ($("#titan").is(":checked")) {
        switchPlanet = $("#titan").val(); // radio 
    }

    if (switchPlanet === "Titan") {
        var solvent = "methane";
        $('input[name="switchSolvent"]').val(["methane"]);
        atmosPress = 145.0;
        var AtmPress = 145.0;
        settingsId[19].value = 145.0;
        //$("#AtmPress").val(145.0);
        $("#AtmPress").roundSlider("setValue", "145.0");
        greenHouse = 12.0;
        var GHTemp = 12.0;
        settingsId[4].value = 12.0;
        //$("#GHTemp").val(12.0);
        $("#GHTemp").roundSlider("setValue", "12.0");
        albedo = 0.75;
        var Albedo = 0.21;
        settingsId[5].value = 0.21;
        //$("#Albedo").val(0.21);
        $("#Albedo").roundSlider("setValue", "0.21");
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
    var minTeff = 1000.0;
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
      if (teff <= 3000.0){
        minLogg = 4.0; //Brown dwarf regime
    } else if ((teff > 3000.0) && (teff <= 4000.0)) {
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
        var earthP = 101.3; //Earth sea level pressure in kPa
//initialize variables with values for water:
       var phaseA = 4.6543;
       var phaseB = 1435.264;
       var phaseC = -64.848;
       var criticalTemp = 647.096; //K 
       var criticalPress = 22.06e3; //kPa 
       var criticalPressStr = "22060"; //kPa 
       var tripleTemp = 273.16; //K 
       var triplePress = 0.611657; //kPa 
       var triplePressStr = "0.62"; //kPa 
//Solvent blocks with Antoine coefficients (A, B, C) for approximate relation between 
//temperature in K and vapor pressure bar 
// and critical- and triple- point temperatures and pressures
// - taken from NIST Chemistry WebBook
// Brown, R.L. & Stein, S.E., "Boiling Point Data" in NIST Chemistry WebBook, NIST Standard Reference Database Number 69, Eds. P.J. Linstrom and W.G. Mallard, National Institute of Standards and Technology, Gaithersburg MD, 20899, doi:10.18434/T4D303, (retrieved April 12, 2017).
//
   if (solvent == "water"){
//  Values for T below 100 C = 373 K 
       if (atmosPress <= earthP){
         phaseA = 4.6543;
         phaseB = 1435.264;
         phaseC = -64.848;
       } else {
         phaseA = 3.55959;
         phaseB = 643.748;
         phaseC = -198.043;
       }
       criticalTemp = 647.096; //K 
       criticalPress = 22.06e3; //kPa 
       criticalPressStr = "22060"; //kPa 
       tripleTemp = 273.16; //K 
       triplePress = 0.611657; //kPa 
       triplePressStr = "0.62"; //kPa 
   } //water block
   if (solvent == "methane"){
       phaseA = 3.9895;
       phaseB = 443.028;
       phaseC = -0.49;
       criticalTemp = 190.8; //K 
       criticalPress = 4640.0; //kPa 
       criticalPressStr = "4640"; //kPa 
       tripleTemp = 90.68; //K 
       triplePress = 11.7; //kPa 
       triplePressStr = "12"; //kPa 
   } //methane block
   if (solvent == "ammonia"){
//  Values for T below ??? K 
      // if (atmosPress <= ????){
         phaseA = 3.18757;
         phaseB = 506.713;
         phaseC = -80.78;
      // } else {
      //   phaseA = 4.86886;
      //   phaseB = 1113.928;
      //   phaseC = -10.409;
      // }
       criticalTemp = 405.5; //K 
       criticalPress = 11280; //kPa 
       criticalPressStr = "11280"; //kPa 
       tripleTemp = 195.40; //K 
       triplePress = 6.076; //kPa 
       triplePressStr = "6.1"; //kPa 
   } //ammonia block
   if (solvent == "carbonDioxide"){
       phaseA = 6.81228;
       phaseB = 1301.679;
       phaseC = -3.494;
       criticalTemp = 304.19; //K 
       criticalPress = 7380; //kPa 
       criticalPressStr = "7380"; //kPa 
       tripleTemp = 216.55; //K 
       triplePress = 517.0; //kPa 
       triplePressStr = "517"; //kPa 
   } //carbonDioxide block

    if (atmosPress === null || atmosPress === "") {
        alert("atmosPress must be filled out");
        return;
    }
    flagArr[19] = false;
    if (atmosPress < triplePress) {
        flagArr[19] = true;
        atmosPress = triplePress;
        var atmosPressStr = triplePressStr;
        settingsId[19].value = triplePress;
        //$("#AtmPress").val(triplePress);
        $("#AtmPress").roundSlider("setValue", triplePress);
    }
    if (atmosPress > criticalPress) {
        flagArr[19] = true;
        atmosPress = criticalPress;
        var atmosPressStr = criticalPressStr;
        settingsId[19].value = criticalPress;
        //$("#AtmPress").val(criticalPress);
        $("#AtmPress").roundSlider("setValue", criticalPress);
    }
    if (greenHouse === null || greenHouse === "") {
        alert("greenHouse must be filled out");
        return;
    }
    flagArr[4] = false;
    if (greenHouse < -200.0) {
        flagArr[4] = true;
        greenHouse = -200.0;
        var greenHouseStr = "-200.0";
        settingsId[4].value = -200.0;
        //$("#GHTemp").val(-200.0);
        $("#GHTemp").roundSlider("setValue", -200.0);
    }
    if (greenHouse > 600.0) {
        flagArr[4] = true;
        greenHouse = 600.0;
        var greenHouseStr = "600.0";
        settingsId[4].value = 600.0;
        //$("#GHTemp").val(600.0);
        $("#GHTemp").roundSlider("setValue", 600.0);
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
    if (albedo > 0.95) {
        flagArr[5] = true;
        albedo = 0.95;
        var albedoStr = "0.95";
        settingsId[5].value = 0.95;
        //$("#Albedo").val(0.95);
        $("#Albedo").roundSlider("setValue", 0.95);
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

    var lamUV = 260.0;
    var lamIR = 2600.0;
 
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
        lambdaStop = lambdaStart + 0.5; //0.5 nm = 5 A
        var lambdaStopStr = String(lambdaStop);
        settingsId[10].value = lambdaStop;
        $("#lambdaStop").val(lambdaStop);
    }

//limit size of synthesis region (nm):
   var maxSynthRange = 5.0; //set default to minimum value //nm
  //if we're not in the blue we can get away wth more:
   if (lambdaStart > 350.0){
      maxSynthRange = 10.0;
   }
   if (lambdaStart > 550.0){
      maxSynthRange = 20.0;
   }
   if (lambdaStart > 700.0){
      maxSynthRange = 50.0;
   }
   if (lambdaStart > 1000.0){
      maxSynthRange = 100.0;
   }
   if (lambdaStart > 1600.0){
      maxSynthRange = 200.0;
   }
    //console.log("maxSynthRange " + maxSynthRange + " lambdaStop " + lambdaStop);
    if (lambdaStop > (lambdaStart+maxSynthRange)) {
        //console.log("lambdaStop > (lambdaStart+maxSynthRange) condition");
        flagArr[10] = true;
        lambdaStop = lambdaStart + maxSynthRange; //10 nm = 100 A
        var lambdaStopStr = String(lambdaStop);
        settingsId[10].value = lambdaStop;
        $("#lambdaStop").val(lambdaStop);
    }
    //console.log("lambdaStop " + lambdaStop);
    if (lambdaStop > lamIR) {
        //console.log("lambdaStop > lamIR condition");
        flagArr[10] = true;
        lambdaStop = lamIR;
        var lambdaStopStr = String(lamIR);
        settingsId[10].value = lamIR;
        $("#lambdaStop").val(lamIR);
    }
    //console.log("lambdaStop " + lambdaStop);

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
    if (macroV > 8.0) {
        flagArr[13] = true;
        macroV = 8.0;
        var macroVStr = "8.0";
        settingsId[13].value = 8.0;
        $("#macroV").val(8.0);
    }


    if (rotV === null || rotV === "") {
        alert("rotV must be filled out");
        return;
    }
    flagArr[14] = false;
    if (rotV < 0.0) {
        flagArr[14] = true;
        rotV = 0.0;
        var rotVStr = "0.0";
        settingsId[14].value = 0.0;
        $("#rotV").val(0.0);
    }
    if (rotV > 20.0) {
        flagArr[14] = true;
        rotV = 20.0;
        var rotVStr = "20.0";
        settingsId[14].value = 20.0;
        $("#rotV").val(20.0);
    }


    if (rotI === null || rotI === "") {
        alert("rotI must be filled out");
        return;
    }
    flagArr[15] = false;
    if (rotI < 0.0) {
        flagArr[15] = true;
        rotI = 0.0;
        var rotIStr = "0.0";
        settingsId[15].value = 0.0;
        $("#rotI").val(0.0);
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
    if (diskSigma < 0.005) {
        flagArr[16] = true;
        diskSigma = 0.005;
        var diskSigmaStr = "0.005";
        settingsId[16].value = 0.005;
        $("#diskSigma").val(0.005);
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
        var fudgeStr = "-2.0";
        settingsId[17].value = -2.0;
        $("#logKapFudge").val(-2.0);
    }
    if (logKapFudge > 2.0) {
        flagArr[17] = true;
        logKapFudge = 2.0;
        var fudgeStr = "2.0";
        settingsId[17].value = 2.0;
        $("#logKapFudge").val(2.0);
    }

    if (RV === null || RV === "") {
        alert("RV must be filled out");
        return;
    }
    flagArr[18] = false;
    if (RV < -200.0) {
        flagArr[18] = true;
        RV = -200.0;
        var RVStr = "-200";
        settingsId[18].value = -200;
        $("#RV").val(-200);
    }
    if (RV > 200.0) {
        flagArr[18] = true;
        RV = 200.0;
        var RVStr = "200";
        settingsId[18].value = 200;
        $("#RV").val(200);
    }

    if (nOuterIter === null || nOuterIter === "") {
        alert("nOuterIter must be filled out");
        return;
    }
    //flagArr[28] = false;
    if (nOuterIter < 5) {
        //flagArr[28] = true;
        nOuterIter = 5;
        var nOuterIterStr = "5";
        //settingsId[28].value = 0.0;
        $("#nOuterIter").val(5);
    }
    if (nOuterIter > 12) {
        //flagArr[28] = true;
        nOuterIter = 12;
        var nOuterIterStr = "12";
        //settingsId[28].value = 90.0;
        $("#nOuterIter").val(12);
    }

    if (nInnerIter === null || nInnerIter === "") {
        alert("nInnerIter must be filled out");
        return;
    }
    //flagArr[28] = false;
    if (nInnerIter < 5) {
        //flagArr[28] = true;
        nInnerIter = 5;
        var nInnerIterStr = "5";
        //settingsId[28].value = 0.0;
        $("#nInnerIter").val(5);
    }
    if (nInnerIter > 12) {
        //flagArr[28] = true;
        nInnerIter = 12;
        var nInnerIterStr = "12";
        //settingsId[28].value = 90.0;
        $("#nInnerIter").val(12);
    }

// For new metallicity commands lburns
// For logHeFe: (lburns)
    var flagArr = [];
    flagArr.length = numInputs;
    if (logHeFe === null || logHeFe === "") {
      alert("logHeFe must be filled out");
      return;
    }
    flagArr[20] = false;
    if (logHeFe < -1.0) {
      flagArr[20] = true;
      logHeFe = -1.0;
      var logHeFeStr = "-1.0";
      settingsId[20].value = -1.0;
      $("#logHeFe").val(-1.0);
    }
    if (logHeFe > 1.0) {
      flagArr[20] = true;
      logHeFe = 1.0;
      var logHeFeStr = "1.0";
      settingsId[20].value = 1.0;
      $("#logHeFe").val(1.0);
    }
// For logCO: (lburns)
    if (logCO === null || logCO === "") {
      alert("logCO must be filled out");
      return;
    }
    flagArr[21] = false;
    if (logCO < -2.0) {
      flagArr[21] = true;
      logCO = -2.0;
      var logCOStr = "-2.0";
      settingsId[21].value = -2.0;
      $("#logCO").val(-2.0);
    }
    if (logCO > 2.0) {
      flagArr[21] = true;
      logCO = 2.0;
      var logCOStr = "2.0";
      settingsId[21].value = 2.0;
      $("#logCO").val(2.0);
    }
// For logAlphaFe: (lburns)
    if (logAlphaFe === null || logAlphaFe === "") {
      alert("logAlphaFe must be filled out");
      return;
    }
    flagArr[22] = false;
    if (logAlphaFe < -0.5) {
      flagArr[22] = true;
      logAlphaFe = -0.5;
      var logAlphaFeStr = "-0.5";
      settingsId[22].value = -0.5;
      $("#logAlphaFe").val(-0.5);
    }
    if (logAlphaFe > 0.5) {
      flagArr[22] = true;
      logAlphaFe = 0.5;
      var logAlphaFeStr = "0.5";
      settingsId[22].value = 0.5;
      $("#logAlphaFe").val(0.5);
    }

//For exo-planet light curve:
  var inclntn = Math.PI * rotI / 180;  //degrees to radians
  var ifTransit = true;


var url = "http://www.ap.smu.ca/~ishort/OpenStars/ChromaStarServer/ChromaStarServer.php";
//var masterInput="teff="+teff+"&logg="+logg+"&logZScale="+logZScale+"&massStar="+massStar;
var masterInput="teff="+teff+"&logg="+logg+"&logZScale="+logZScale+"&massStar="+massStar
  +"&xiT="+xiT+"&lineThresh="+lineThresh+"&voigtThresh="+voigtThresh+"&lambdaStart="+lambdaStart+"&lambdaStop="+lambdaStop
  +"&sampling="+switchSampl+"&logGammaCol="+logGammaCol+"&logKapFudge="+logKapFudge
  +"&macroV="+macroV+"&rotV="+rotV+"&rotI="+rotI
  +"&nInnerIter="+nInnerIter+"&nOuterIter="+nOuterIter+"&ifTiO="+ifTiO
  +"&logHeFe="+logHeFe+"&logCO="+logCO+"&logAlphaFe="+logAlphaFe;
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

                           //JB
    var plotOneId = document.getElementById("plotOne");
    var cnvsOneId = document.getElementById("plotOneCnvs");
    var plotTwoId = document.getElementById("plotTwo");
    var cnvsTwoId = document.getElementById("plotTwoCnvs");
    var plotThreeId = document.getElementById("plotThree");
    var cnvsThreeId = document.getElementById("plotThreeCnvs");
    var plotFourId = document.getElementById("plotFour");
    var cnvsFourId = document.getElementById("plotFourCnvs");
    var plotFiveId = document.getElementById("plotFive");
    var cnvsFiveId = document.getElementById("plotFiveCnvs");
    var plotSevenId = document.getElementById("plotSeven");
    var cnvsSevenId = document.getElementById("plotSevenCnvs");
    var plotNineId = document.getElementById("plotNine");
    var cnvsNineId = document.getElementById("plotNineCnvs");
    var plotTenId = document.getElementById("plotTen");
    var cnvsTenId = document.getElementById("plotTenCnvs");
    var plotElevenId = document.getElementById("plotEleven");
    var cnvsElevenId = document.getElementById("plotElevenCnvs");
    var plotTwelveId = document.getElementById("plotTwelve");
    var cnvsTwelveId = document.getElementById("plotTwelveCnvs");
    var plotThirteenId = document.getElementById("plotThirteen");
    var cnvsThirteenId = document.getElementById("plotThirteenCnvs");
    var plotFourteenId = document.getElementById("plotFourteen");
    var cnvsFourteenId = document.getElementById("plotFourteenCnvs");
    var plotFifteenId = document.getElementById("plotFifteen");
    var cnvsFifteenId = document.getElementById("plotFifteenCnvs");
    var plotSixteenId = document.getElementById("plotSixteen");
    var cnvsSixteenId = document.getElementById("plotSixteenCnvs");
    var plotNineteenId = document.getElementById("plotNineteen");
    var cnvsNineteenId = document.getElementById("plotNineteenCnvs");                                //JB

    var printModelId = document.getElementById("printModel"); //detailed model print-out area

//

    if (ifShowAtmos === true) {
        //plotOneId.style.display = "block";
        plotTwoId.style.display = "block";
        plotThreeId.style.display = "block";
        if($("#showLogPP").val()=="None"){
           plotSixteenId.style.display = "none";
        }

    }
    if (ifShowRad === true) {
        plotFourId.style.display = "block";
        plotFiveId.style.display = "block";
        plotFifteenId.style.display = "block";
        if($("#showLogPP").val()=="None"){
           plotSixteenId.style.display = "block";
        }

    }
    if (ifShowLogPP === true) {
        if($("#showLogPP").val()=="None"){
           plotSixteenId.style.display = "none";
        }
    }
    if (ifShowAtmos === false) {
        //plotOneId.style.display = "none";
        plotTwoId.style.display = "none";
        plotThreeId.style.display = "none";
    }
    if (ifShowRad === false) {
        plotFourId.style.display = "none";
        //plotFiveId.style.display = "none";
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
            (ifPrintPP === true) || 
            (ifPrintTrans === true) ||
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
        //var numSpecSyn = Number(jsonObj.numSpecSyn); //number of wavelengths in continuum rectified flux distribution
        var numGaussLines = Number(jsonObj.numGaussLines); //number of angles in specific intensity distribution
        var numLams = Number(jsonObj.numLams); //number of continuum SED lambda points
        var numSpecies = Number(jsonObj.numSpecies); //number of chemical speecies (ionization stages) 
        var nelemAbnd = Number(jsonObj.nelemAbnd); //number of chemical elements 
//Variables for Phil Bennett's GAS ChemEquil/IonizEquil/EOS package: 
        var numGasDepths = Number(jsonObj.numGasDepths); //number of depths at which GAS partial pressures reported 
        var numGas = Number(jsonObj.numGas); //number of chemical species in GAS (incl. molecules) 
      //console.log("numDeps " + numDeps + " numMaster " + numMaster + " numThetas " + numThetas 
      //        + " numGaussLines " + numGaussLines);
      //console.log("numGas " + numGas + " numGasDepths " + numGasDepths);


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

//    //Vega parameters (of Phoenix model- Teff not quite right!)
//    var teffVega = 9550.0;
//    var loggVega = 3.95;
//    var gravVega = Math.pow(10.0, loggVega);
//    var zScaleVega = 0.333;

    flagArr[24] = false;
var rPlanetSol = rPlanet * rEarth / rSun; //#Earth radii to solar radii
if (rPlanetSol > 0.1*radius){
    rPlanetSol = 0.1*radius;
    rPlanet = rPlanetSol * rSun / rEarth;
    flagArr[24] = true;
    var rPlanStr = rPlanet.toString(2);
    settingsId[24].value = rPlanet;
    $("#rPlanet").val(rPlanet);
    }
if (rPlanet <= 0.0){
    rPlanetSol = 0.001*radius;
    rPlanet = rPlanetSol * rSun / rEarth;
    flagArr[24] = true;
    var rPlanStr = rPlanet.toString(2);
    settingsId[24].value = rPlanet;
    $("#rPlanet").val(rPlanet);
    }

    flagArr[23] = false;
var rOrbitSol = rOrbit * au / rSun;
if (rOrbitSol < radius){
    rOrbitSol = radius;
    rOrbit = rOrbitSol * rSun / au;
    flagArr[23] = true;
    var rOrbStr = rOrbit.toString(2);
    settingsId[23].value = rOrbit;
    $("#rOrbit").val(rOrbit);
    }
if (rOrbit > 100.0){
    rOrbit = 100.0;
    flagArr[23] = true;
    var rOrbStr = rOrbit.toString(2);
    settingsId[23].value = rOrbit;
    $("#rOrbit").val(rOrbit);
    }

    if (orbI === null || orbI === "") {
        alert("orbI must be filled out");
        return;
    }
    flagArr[25] = false;
    if (orbI < 0.0) {
        flagArr[25] = true;
        orbI = 0.0;
        var orbIStr = "0.0";
        settingsId[25].value = 0.0;
        $("#orbI").val(0.0);
    }
    if (orbI > 90.0) {
        flagArr[25] = true;
        orbI = 90.0;
        var orbIStr = "90.0";
        settingsId[25].value = 90.0;
        $("#orbI").val(90.0);
    }



var logMassStar = Math.log(massStar) + logMSun; //#MSun to g
//#print("MassStar ", Math.exp(logMassStar))
var logROrbCm = Math.log(rOrbit) + logAu; //#AU to cm
//#print("ROrbCm ", Math.exp(logROrbCm))

//#linear velocity of planetary orbit from Kepler's 3rd law
//#Assumes planet at same distance as stellar surface
var logVtransSq = logGConst + logMassStar - logROrbCm;
var logVtrans = 0.5*logVtransSq;
var vTrans = Math.exp(logVtrans);  //#cm/s approximately at star's surface
//#print("vTrans ", vTrans)

//#For period calculation only:
//#angular velocity of planetary orbit from Kepler's 3rd law
var logOmegaSq = logGConst + logMassStar - 3*logROrbCm;
var logOmega = 0.5 * logOmegaSq;  //# RAD/s
//#print("Omega ", Math.exp(logOmega))

//#Orbital period - for interest
var logPplanet = Math.log(2.0) + Math.log(Math.PI) - logOmega;
var pPlanet = Math.exp(logPplanet);
//console.log("Planetary orbital period (s) " + pPlanet);  //# in s
var pPlanetYrs = pPlanet / (3600.0 * 24.0 * 365.25);
//#Establish ephemeris with zero epoch (phase = 0) at mid-transit
//#time interval should be equal to or less than time taken for plane to
//#move through its own diameter - time interval of ingress or egress
var ingressT = ( 2.0*rPlanet*rEarth ) / vTrans;


//
//Unpack the atmospheric structure:

         //console.log("logTauRosAjax numDeps " + numDeps);
         //console.log("jsonObj.logTau " + jsonObj.logTau);
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

//Special Tau_Ros axis to go with GAS partial pressure - must be consistent with depth sampling of partial pressure
//report from ChromaStarServer
//
    var logTauRosGas = [];
    logTauRosGas.length = numGasDepths; 
    var count = 0;
    for (var i = 1; i < numDeps; i+=4){
       logTauRosGas[count] = tauRos[1][i];
       count++;
    } 

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

     // set up GAS species and partial pressure arrays 
     var gsSpec = [];
     gsSpec.length = numGas;
     var log10GasPpAjax = [];
     log10GasPpAjax.length = numGasDepths;
     var log10GasPp = [];
     log10GasPp.length = numGas;
     for (var i = 0; i < numGas; i++){
        log10GasPp[i] = [];
        log10GasPp[i].length = numGasDepths;
     }
 
     for (var i = 0; i < numGas; i++){
        var gsSpecKey = "Gas"+i;
        gsSpec[i] = jsonObj[gsSpecKey]; 
        //console.log("i " + i + " gsSpecKey " + gsSpecKey + " gsSpec " + gsSpec[i]);
        var prtlPrsKey = "PrtlPrs"+i;
        var log10GasPpAjax = gsAjaxParser(numGasDepths, jsonObj[prtlPrsKey]);
        for (var j = 0; j < numGasDepths; j++){
           log10GasPp[i][j] = log10GasPpAjax[j];
        }  //j 
      }  //i

     //for (var i = 0; i < numGasDepths; i++){
     //    console.log("logTauRosGas " + logTauRosGas[i] + " log10GasPp " + log10GasPp[0][i]);
     //}

    var lineMode;
    //
    // ************
    //
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
          //console.log("i " + i + " masterLams[i] " + masterLams[i]);
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

//Unpack the spectrum synthsis flux distributions 
//
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

   var logContFluxI = interpolV(contFlux[1], lambdaScale, masterLams);
//Unpack the linear monochromatic continuum limb darkening cofficients (LDCs) 

         var ldc = gsAjaxParser(numLams, jsonObj.LDC);

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

//Continuum rectification
   var numSpecSyn = iStop - iStart + 1;
   var specSynLams = [];
   specSynLams.length = numSpecSyn;
   var specSynFlux = [];
   specSynFlux.length = 2;
   specSynFlux[0] = [];
   specSynFlux[1] = [];
   specSynFlux[0].length = numSpecSyn; 
   specSynFlux[1].length = numSpecSyn;
   for (var iCount = 0; iCount < numSpecSyn; iCount++){
      specSynLams[iCount] = masterLams[iStart+iCount];
      specSynFlux[1][iCount] = masterFlux[1][iStart+iCount] - logContFluxI[iStart+iCount];
      specSynFlux[0][iCount] = Math.exp(specSynFlux[1][iCount]);
   }
    
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
/*
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
*/    

//#Planetary transit quantities
//#Angle of orbital axis wrt plane-of-sky
var iPrime = 90.0 - orbI;

//#Degrees to RAD
var iPrimeRad = iPrime * Math.PI / 180.0;

//#Right angle triangle with hypoteneuse = rOrbit
//#  angle = orbIRad,
//#  and opposite = planet's minimum impact parameters wrt substellar point
//#impact parameter (minimum offset from substellar point)in AU
//console.log("rOrbit " + rOrbit + " iPrimeRad " + iPrimeRad);
var impct = rOrbit * Math.sin(iPrimeRad);
//#impact parameter in solar radii
var impct = impct * au / rSun;
//#print("orbI ", orbI, " iPrime ", iPrime, " iPrimeRad ", iPrimeRad,\
if ( impct >= (radius-(2*rPlanetSol)) ){
    //#There is no eclipse (transit)
    ifTransit = false;
    console.log("ifTransit turned OFF " + ifTransit);
}


//#      " impct/radius ", impct/radius)
//#thetaMinRad is also the minimum theta of the eclipse path chord, in RAD
var ingressT1 = 0.0;
//#Duration of ingress (and egress) from planetary mid-point contact to 2nd contact
var ingressT2 = 0.0;
var numTransThetas = 0.0;
var thetaMinRad = 1.0;
var iFirstTheta = 0;
var contact1x = 0.0;
var contact2x = 0.0;
var contactMidx = 0.0;
var cosThetaMax = 0.0;
var midContAngle = 0.0;
var halfHelpAngle = 0.0;
var logOmegaLens = logTiny;
//#omegaLens = 0.0

if (ifTransit){
    //#First contact position along cord, in solar radii:
    contact1x = Math.sqrt( (radius + rPlanetSol)**2 - impct**2 );
    //#Planetary mid-point contact
    contactMidx = Math.sqrt(radius**2 - impct**2);
    //#Second contact position along cord in solar radii:
    contact2x = Math.sqrt( (radius - rPlanetSol)**2 - impct**2 );
    //#ingressT = ( (contact1x - contact2x)*Useful.rSun() ) / vTrans
    ingressT1 = ( (contact1x - contactMidx)*rSun ) / vTrans;
    ingressT2 = ( (contactMidx - contact2x)*rSun ) / vTrans;
   // console.log("New ingressT1 "+ ingressT1+ " ingressT2 "+ ingressT2)
    //#cos(theta) *decreases* with increasing theta in Quadrant I:
    thetaMinRad =  Math.asin(impct/radius);
    cosThetaMax = Math.cos(thetaMinRad);

    //console.log(" thetaMinRad "+ thetaMinRad+ " cosThetaMax "+ cosThetaMax);

    //#quantities for computing the blocking factor at planetary mid-point contact
    //#Angle at planet's centre of lens-shaped occultation area:
    halfHelpAngle = Math.atan( (rPlanetSol/2.0)/radius );
    midContAngle = ( Math.PI - (2.0*halfHelpAngle) ) / 2.0;
    //#Area of lens-shaped area occulted at planetary mid-point contact in solar-radii^2
    //# - (2*angle/2*Pi) * Pi*rPlanet^2 = angle*rPlanet^2
    logOmegaLens = Math.log(midContAngle) + 2.0*Math.log(rPlanetSol);
    //#As fraction of host star projected radius
    logOmegaLens = logOmegaLens - Math.log(Math.PI) - 2.0*Math.log(radius);
    //console.log("log midContBlock/radius^2 "+ logOmegaLens)
    //#omegaLens = Math.exp(logOmegaLens)
}
i = 0;

//#ifFirst = False
//#for i in range(numThetas):
//#cosTheta[1] *decreases* (ie. theta increases) with increasing array number
if (ifTransit){
    while ( (cosTheta[1][i] >= cosThetaMax)
           & (i < numThetas) ){
        //#print("In while loop: i ", i, " cosTheta[1] ", cosTheta[1][i])
        //#if (ifFirst == False):
        //#    iFirstTheta = i
        //#    ifFirst = True
        //# We are on the eclipse semi-chord:
        i+=1;
    }
    iFirstTheta = i;
}
numTransThetas = numThetas - i;
var numTransThetas2 = (2*numTransThetas + 4);
//#print("iFirstTheta ", iFirstTheta, " numTransThetas ", numTransThetas, " numTransThetas2 ", numTransThetas2)

var transit = [];
transit.length = numTransThetas;
var transit2 = [];
transit2.length = numTransThetas2;
for (var i = 0; i < numTransThetas; i++){
   transit[i] = 0.0;
}
for (var i = 0; i < numTransThetas2; i++){
   transit2[i] = 0.0;
}
var transDuration = 0.0
var transTime0 = 0.0
var transTime1 = 0.0
var totalDuration = 0.0
var deltaT = 1.0
var numEpochs = 1
var ephemT = [];
//# blocking factor should be projected planet area over that annulus area
//#transit[][] is array of distances traveled, r, along semi-chord from position of
//#minimum impact parameter, and corresponding theta values:
//# 2D array of 2 x numThetas
//#row 0 is log_e of ratio of projected planet area to area of annulus for each theta being transited
//#row 1 is times corresponding to linear distance travelled along transit
//#  semi-path at surface of star in solar radii

//#transit = [[numpy.double(0.0) for i in range(numTransThetas)] for j in range(2)] # Default
//#Row 0 is logarithmic ratio of planet area to annulus area
//# - set default value to log of neglible value:
//#transit[0] = [logTiny for i in range(numThetas)]
if (ifTransit){
    //console.log("radius " + radius + " vTrans " + vTrans + " iFirstTheta " + iFirstTheta + " numTransThetas " + numTransThetas + " impct" + impct);
    transit = transLight2(radius, cosTheta, vTrans, iFirstTheta, numTransThetas, impct);
    //#print("numTransThetas ", numTransThetas)
    //#reflect the half-transit profile and add the first and last points just before
    //#ingress and just after egress
    for (var i = 0; i < numTransThetas; i++){
        transit2[2+i] = -1.0 * transit[(numTransThetas-1)-i];
        //#print("1st half: i ", i, " (numTransThetas-1)-i ", (numTransThetas-1)-i)
    }
    for (var i = 0; i < numTransThetas; i++){
        transit2[2+(numTransThetas+i)] = transit[i];
        //#print("2nd half: i ", i)
    }
    transit2[1] = transit2[2] - ingressT2;
    transit2[0] = transit2[1] - ingressT1;
    transit2[numTransThetas2-2] = transit2[numTransThetas2-3] + ingressT2;
    transit2[numTransThetas2-1] = transit2[numTransThetas2-2] + ingressT1;

    transDuration = transit2[numTransThetas2-1] - transit2[0];
    //#print("transit2[0] ", transit2[0], " transit2[numTransThetas2-1] ", transit2[numTransThetas2-1])
    transTime0 = transit2[0] - transDuration/4;
    transTime1 = transit2[numTransThetas2-1] + transDuration/4;
    totalDuration = transTime1 - transTime0;
    //console.log("transTime0 "+ transTime0+ " transTime1 "+ transTime1)
    //console.log("transDuration "+ transDuration/3600.0+ " totalDuration "+ totalDuration/3600.0)
    //#numEpochs = 200
    //#deltaT = transDuration / numEpochs
    //#Make time sampling interval equal to the time of ingress/egress
    var ingressHelp = [ingressT1, ingressT2];
    var deltaTHelp = minMax(ingressHelp); //#/ 2.0;
    //console.log("deltaTHelp " + deltaTHelp);
    deltaT = ingressHelp[deltaTHelp[0]];
    numEpochs = Math.ceil(totalDuration / deltaT);
    //#print("deltaT ", deltaT, " numEpochs ", numEpochs)
    //#ephemeris in time units (s)
}

ephemT.length = numEpochs;
for (var i = 0; i < numEpochs; i++){
   ephemT[i] = (i*deltaT)+transTime0;;
}
//console.log("numEpochs " + numEpochs + " deltaT " + deltaT);


//Apply corrections to wavelength scale before filter integrations:
// - do them in the order nature does them in...
//
//Radial velocity correction:
//We have to correct both masterLams AND specSynLams to correct both the overall SED and the spectrum synthesis region:
     var masterLams2 = [];
     masterLams2.length = numMaster;
     var specSynLams2 = [];
     specSynLams2.length = numSpecSyn;
//refresh default each run:
     for (var i = 0; i < numMaster; i++){
        masterLams2[i] = masterLams[i];
     }
     for (var i = 0; i < numSpecSyn; i++){
        specSynLams2[i] = specSynLams[i];
     }
     var deltaLam = 0.0;
     var c = 2.9979249E+10; // light speed in vaccuum in cm/s
     var RVfac = RV / (1.0e-5*c);
     if (RV != 0.0){
       for (var i = 0; i < numMaster; i++){
          deltaLam = RVfac * masterLams[i];
          masterLams2[i] = masterLams2[i] + deltaLam; 
       }
       for (var i = 0; i < numSpecSyn; i++){
          deltaLam = RVfac * specSynLams[i];
          specSynLams2[i] = specSynLams2[i] + deltaLam; 
       }
     }
     var invnAir = 1.0 / 1.000277; // reciprocal of refractive index of air at STP 
     if (vacAir == "air"){
       for (var i = 0; i < numMaster; i++){
         masterLams2[i] = invnAir * masterLams2[i];
       }
       for (var i = 0; i < numSpecSyn; i++){
         specSynLams2[i] = invnAir * specSynLams2[i];
       }
     }

var masterFluxTrans = [];
masterFluxTrans.length = 2;
for (var j = 0; j < 2; j++){
   masterFluxTrans[j] = [];
   masterFluxTrans[j].length = numMaster;
   for (var k = 0; k < numMaster; k++){
      masterFluxTrans[j][k] = [];
      masterFluxTrans[j][k].length = numTransThetas;
      for (var i = 0; i < numTransThetas; i++){
         masterFluxTrans[j][k][i] = 0.0;
      }
   }
}

var masterFluxTrans2 = [];
masterFluxTrans2.length = 2;
for (var j = 0; j < 2; j++){
   masterFluxTrans2[j] = [];
   masterFluxTrans2[j].length = numMaster;
   for (var k = 0; k < numMaster; k++){
      masterFluxTrans2[j][k] = [];
      masterFluxTrans2[j][k].length = numTransThetas2;
      for (var i = 0; i < numTransThetas2; i++){
         masterFluxTrans2[j][k][i] = 0.0;
      }
   }
}

var helper = 0.0
var logHelper = 0.0;

if (ifTransit){
    //console.log("fluxTrans called radius " + radius + " iFirstTheta " + iFirstTheta + " numTransThetas " + numTransThetas + " rPlanet " + rPlanet);
    masterFluxTrans = fluxTrans(masterIntens, masterFlux, masterLams, cosTheta,
          radius, iFirstTheta, numTransThetas, rPlanet);
    //#reflect the half-transit profile and add the first and last points just before
    //#ingress and just after egress
    for (var j = 0; j < numMaster; j++){
        //#lens-shaped occultation area at planetary mid-point contact:
        //#Ingress:
        //#Subtracting the very small from the very large - let's be sophisticated about it:
        logHelper = Math.log(masterIntens[j][numThetas-1]) + logOmegaLens - masterFlux[1][j];
        helper = 1.0 - Math.exp(logHelper);
        masterFluxTrans2[1][j][0] = masterFlux[1][j];
        masterFluxTrans2[0][j][0] = masterFlux[0][j];
        masterFluxTrans2[1][j][1] = masterFlux[1][j] + Math.log(helper);
        masterFluxTrans2[0][j][1] = Math.exp(masterFluxTrans2[1][j][1]);
        //#Full occultation:
        //#Ingress to minimum impact parameter
        for (var i =0; i < numTransThetas; i++){
            masterFluxTrans2[1][j][2+i] = masterFluxTrans[1][j][(numTransThetas-1)-i];
            masterFluxTrans2[0][j][2+i] = masterFluxTrans[0][j][(numTransThetas-1)-i];
        }
        //#Minimum impact parameter to egress
        for (var i = 0; i < numTransThetas; i++){
            masterFluxTrans2[1][j][2+(numTransThetas+i)] = masterFluxTrans[1][j][i];
            masterFluxTrans2[0][j][2+(numTransThetas+i)] = masterFluxTrans[0][j][i];
        }
        //#Egress:
        masterFluxTrans2[1][j][numTransThetas2-2] = masterFlux[1][j] + Math.log(helper);
        masterFluxTrans2[0][j][numTransThetas2-2] = Math.exp(masterFluxTrans2[1][j][numTransThetas2-2]);
        masterFluxTrans2[1][j][numTransThetas2-1] = masterFlux[1][j];
        masterFluxTrans2[0][j][numTransThetas2-1] = masterFlux[0][j];
    }
}


    //var colors =  UBVRI(masterLams2, masterFlux, numDeps, tauRos, temp);
        var bandFlux =  UBVRIraw(masterLams, masterFlux);
        var colors = UBVRI(bandFlux);

    // UBVRI band intensity annuli - for disk rendering:
    var bandIntens = iColors(masterLams2, masterIntens, numThetas, numMaster); 
    // tunable monochromatic band intensity annuli - for disk rendering:
    //var diskSigma = 1; //nm test
    //var tuneBandIntens = tuneColor(masterLams2, masterIntens, numThetas, numMaster, diskLambda, diskSigma, lamUV, lamIR); 
    //Use UN-shifted wavelength scale (masterLams) for defining the user-filter:
    var gaussFilter = gaussian(masterLams, numMaster, diskLambda, diskSigma, lamUV, lamIR); 
    //Use *shifted* wavelength scale (masterLams2) for user-filter integration of spectrum:
    var tuneBandIntens = tuneColor(masterLams2, masterIntens, numThetas, numMaster, gaussFilter, lamUV, lamIR); 

    //Fourier transform of narrow band image:
    var ft = fourier(numThetas, cosTheta, tuneBandIntens);
    var numK = ft[0].length;

//#Planetary transit light curves as seen through photometric filters:
var numBands = bandFlux.length;
//#Sigh - I don't know how to directly assign a 2D list column slice (2nd index):
var bandFluxTransit = [];
bandFluxTransit.length = numBands;
for (var j = 0; j < numBands; j++){
   bandFluxTransit[j] = [];
   bandFluxTransit[j].length = numTransThetas2;
   for (var i = 0; i < numTransThetas2; i++){
      bandFluxTransit[j][i] = 0.0;
   }
}
var helpBandFlux = [];
helpBandFlux.length = numBands;
for (var j = 0; j < numBands; j++){
   helpBandFlux[j] = 0.0;
}
var helpMasterFlux = [];
helpMasterFlux.length = 2;
for (var j = 0; j < 2; j++){
   helpMasterFlux[j] = [];
   helpMasterFlux[j].length = numMaster;
   for (var i = 0; i < numMaster; i++){
      helpMasterFlux[j][i] = 0.0;
   }
}
for (var iEpoch = 0; iEpoch < numTransThetas2; iEpoch++){
    for (var il = 0; il < numMaster; il++){
        helpMasterFlux[1][il] = masterFluxTrans2[1][il][iEpoch];
        helpMasterFlux[0][il] = masterFluxTrans2[0][il][iEpoch];
        //if (il == 150){
        //  console.log("masterFluxTrans2[1][il][iEpoch] " + masterFluxTrans2[1][il][iEpoch]);
        //}
    }
    helpBandFlux = UBVRIraw(masterLams, helpMasterFlux);
    for (var iBand = 0; iBand < numBands; iBand++){
        bandFluxTransit[iBand][iEpoch] = helpBandFlux[iBand];
        //if (iBand == 0){
        //  console.log("bandFluxTransit[iBand][iEpoch] " + bandFluxTransit[iBand][iEpoch]);
        //}
    }
}
var bandFluxTransit2 = [];
bandFluxTransit2.length = numBands;
for (var j = 0; j < numBands; j++){
   bandFluxTransit2[j] = [];
   bandFluxTransit2[j].length = numEpochs;
   for (var i = 0; i < numEpochs; i++){
      bandFluxTransit2[j][i] = 0.0;
   }
}
//#Interpolate transit light curves onto total duration we are following:
for (var iBand = 0; iBand < numBands; iBand++){
    bandFluxTransit2[iBand] = interpolV(bandFluxTransit[iBand], transit2, ephemT);
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
            20 + colr * xTab, 15, 200, lineColor, textId);
    roundNum = bolLum.toPrecision(3);
    txtPrint("<span title='Bolometric luminosity'>\n\
<a href='http://en.wikipedia.org/wiki/Luminosity' target='_blank'><em>L</em><sub>Bol</sub></a> = \n\
</span> "
            + roundNum
            + " <span title='Solar luminosities'>\n\
<a href='http://en.wikipedia.org/wiki/Solar_luminosity' target='_blank'><em>L</em><sub>Sun</sub></a>\n\
</span> ",
            20 + colr * xTab, 40, 200, lineColor, textId);
// 
//
// Planetary orbital period:
    roundNum = pPlanetYrs.toFixed(1);
    txtPrint("<span title='Planets orbital period'>\n\
<em>P</em><sub>Orb</sub>: \n\
</span>"
            + roundNum
            + " <span >\n\ yrs\n\
</span>",
            350, 40, 200, lineColor, textId);

    // UBVRI indices
    var xTab = 80;
    var colr = 0;
    var roundNum0 = colors[0].toFixed(2);
    var roundNum1 = colors[1].toFixed(2);
    var roundNum2 = colors[2].toFixed(2);
    var roundNum3 = colors[3].toFixed(2);
    var roundNum4 = colors[4].toFixed(2);
    var roundNum5 = colors[5].toFixed(2);// lburns
    var roundNum6 = colors[6].toFixed(2);//lburns

    txtPrint("<a href='http://en.wikipedia.org/wiki/UBV_photometric_system' title='Johnson-Cousins U-B photometric color index' target='_blank'>\n\
<span style='color:purple'>U</span>-" +
            "<span style='color:blue'>B\n\
</span>\n\
</a>: " + roundNum0
            + " <a href='http://en.wikipedia.org/wiki/UBV_photometric_system' title='Johnson-Cousins B-V photometric color index' target='_blank'>\n\
<span style='color:blue'>B\n\
</span>-" +
            "<span style='color:#00AA00'>V</span></a>: " + roundNum1
            + " <a href='http://en.wikipedia.org/wiki/UBV_photometric_system' title='Johnson-Cousins V-R photometric color index' target='_blank'>\n\
<span style='color:#00AA00'>V\n\
</span>-" +
            "<span style='color:red'>R\n\
</span>\n\
</a>: " + roundNum2
            + " <a href='http://en.wikipedia.org/wiki/UBV_photometric_system' title='Johnson-Cousins V-I photometric color index' target='_blank'>\n\
<span style='color:#00AA00'>V\n\
</span>-" +
            "<span style='color:red'>I\n\
</span>\n\
</a>: " + roundNum3
            + " <a href='http://en.wikipedia.org/wiki/UBV_photometric_system' title='Johnson-Cousins R-I photometric color index' target='_blank'>\n\
<span style='color:red'>R</span>-" +
            "<span style='color:brown'>I\n\
</span>\n\
</a>: " 
       + roundNum4, 180 + colr * xTab, 15, 400, lineColor, textId);
//Added another txtPrint function to display V-K and J-K. Adjusted spectralLine over to fit in these new colors. lburns 06
    txtPrint("<a href='http://en.wikipedia.org/wiki/UBV_photometric_system' title='Johnson-Cousins V-K photometric color index' target='_blank'>\n\
<span style='color:#00FF88'>V</span>-" +
            "<span style='color:sienna'>K\n\
</span>\n\
</a>: " + roundNum5
            + " <a href='http://en.wikipedia.org/wiki/UBV_photometric_system' title='Johnson-Cousins J-K photometric color index' target='_blank'>\n\
<span style='color:firebrick'>J</span>-" +
            "<span style='color:sienna'>K\n\
</span>\n\
</a>: " + roundNum6, 180, 40, 400, lineColor, textId);


    // Echo back the *actual* input parameters:
    var warning = "";
    if (teff < 6000) {
        //warning = "<span style='color:red'><em>T</em><sub>eff</sub> < 6000 K <br />Cool star mode";
        warning = "<span style='color:red'>Cool star mode</span>";
        txtPrint(warning, 600, 10, 200, lineColor, textId);
    } else {
        //warning = "<span style='color:blue'><em>T</em><sub>eff</sub> > 6000 K <br />Hot star mode</span>";
        warning = "<span style='color:blue'>Hot star mode</span>";
        txtPrint(warning, 600, 10, 200, lineColor, textId);
    }

    //Add subclass to each spectral class (lburns)
    var spectralClass = " ";
    var subClass = " "; //Create a variable for the subclass of the star. lburns
    var luminClass = "V";//defaults to V
//Determine the spectralClass and subClass of main sequence stars, subdwarfs and white dwarfs
//var luminClass = "V" or luminClass = "VI" or luminClass = "WD"
//#// Based on the data in Appendix G of An Introduction to Modern Astrophysics, 2nd Ed. by
//#// Carroll & Ostlie
//Brown dwarf calibration loosely estimated from Fig. L4.2 of "M dwarfs, L dwarfs and T dwarfs"
//by Neill Reid??  http://www.stsci.edu/~inr/ldwarf3.html
if ((logg >= 4.0) && (logg <= 6.0)){
      if (teff <= 1200.0){
        spectralClass = "T";
    } else if ((teff > 1200.0) && (teff < 3000.0)) {
        spectralClass = "L";
        if ((teff > 1200.0) && (teff <= 1400.0)) {
           subClass = "8";
        } else if ((teff > 1400.0) && (teff <= 1600.0)) {
           subClass = "6";
        } else if ((teff > 1600.0) && (teff <= 1800.0)) {
           subClass = "4";
        } else if ((teff > 1800.0) && (teff <= 2100.0)) {
           subClass = "2";
        } else if ((teff > 2200.0) && (teff <= 2500.0)) {
           subClass = "0";
        }
    } else if ((teff >= 3000.0) && (teff < 3900.0)) {
        spectralClass = "M";
        if ((teff >= 3000.0) && (teff <= 3030.0)) {
            subClass = "6";
        } else if ((teff > 3030.0) && (teff <= 3170.0)) {
            subClass = "5";
        } else if ((teff > 3170.0) && (teff <= 3290.0)) {
            subClass = "4";
        } else if ((teff > 3290.0) && (teff <= 3400.0)) {
            subClass = "3";
        } else if ((teff > 3400.0) && (teff <= 3520.0)) {
            subClass = "2";
        } else if ((teff > 3520.0) && (teff <= 3660.0)) {
            subClass = "1";
        } else if ((teff > 3660.0) && (teff < 3900.0)) {
            subClass = "0";
        }
    } else if ((teff >= 3900.0) && (teff < 5200.0)) {
        spectralClass = "K";
        if ((teff >= 3900.0) && (teff <= 4150.0)) {
            subClass = "7";
        } else if ((teff > 4150.0) && (teff <= 4410.0)) {
            subClass = "5";
        } else if ((teff > 4410.0) && (teff <= 4540.0)) {
            subClass = "4";
        } else if ((teff > 4540.0) && (teff <= 4690.0)) {
            subClass = "3";
        } else if ((teff > 4690.0) && (teff <= 4990.0)) {
            subClass = "1";
        } else if ((teff > 4990.0) && (teff < 5200.0)) {
            subClass = "0";
        }
    } else if ((teff >= 5200.0) && (teff < 5950.0)) {
        spectralClass = "G";
        if ((teff >= 5200.0) && (teff <= 5310.0)) {
            subClass = "8";
        } else if ((teff > 5310.0) && (teff <= 5790.0)) {
            subClass = "2";
        } else if ((teff > 5790.0) && (teff < 5950.0)) {
            subClass = "0";
        }
    } else if ((teff >= 5950.0) && (teff < 7300.0)) {
        spectralClass = "F";
        if ((teff >= 5950.0) && (teff <= 6250.0)) {
            subClass = "8";
        } else if ((teff > 6250.0) && (teff <= 6650.0)) {
            subClass = "5";
        } else if ((teff > 6650.0) && (teff <= 7050.0)) {
            subClass = "2";
        } else if ((teff > 7050.0) && (teff < 7300.0)) {
            subClass = "0";
        }
    } else if ((teff >= 7300.0) && (teff < 9800.0)) {
        spectralClass = "A";
        if ((teff >= 7300.0) && (teff <= 7600.0)) {
            subClass = "8";
        } else if ((teff > 7600.0) && (teff <= 8190.0)) {
            subClass = "5";
        } else if ((teff > 8190.0) && (teff <= 9020.0)) {
            subClass = "2";
        } else if ((teff > 9020.0) && (teff <= 9400.0)) {
            subClass = "1";
        } else if ((teff > 9400.0) && (teff < 9800.0)) {
            subClass = "0";
        }
    } else if ((teff >= 9800.0) && (teff < 30000.0)) {
        spectralClass = "B";
        if ((teff >= 9300.0) && (teff <= 10500.0)) {
            subClass = "9";
        } else if ((teff > 10500.0) && (teff <= 11400.0)) {
            subClass = "8";
        } else if ((teff > 11400.0) && (teff <= 12500.0)) {
            subClass = "7";
        } else if ((teff > 12500.0) && (teff <= 13700.0)) {
            subClass = "6";
        } else if ((teff > 13700.0) && (teff <= 15200.0)) {
            subClass = "5";
        } else if ((teff > 15200.0) && (teff <= 18800.0)) {
            subClass = "3";
        } else if ((teff > 18800.0) && (teff <= 20900.0)) {
            subClass = "2";
        } else if ((teff > 20900.0) && (teff <= 25400.0)) {
            subClass = "1";
        } else if ((teff > 25400.0) && (teff < 30000.0)) {
            subClass = "0";
        }
    } else if (teff >= 30000.0) {
        spectralClass = "O";
        if ((teff >= 30000.0) && (teff <= 35800.0)) {
            subClass = "8";
        } else if ((teff > 35800.0) && (teff <= 37500.0)) {
            subClass = "7";
        } else if ((teff > 37500.0) && (teff <= 39500.0)) {
            subClass = "6";
        } else if ((teff > 39500.0) && (teff <= 42000.0)) {
            subClass = "5";
        }
    }
}
//Determine the spectralClass and subClass of giants and subgiants. lburns
//var luminClass = "III" or luminClass = "IV"
if ((logg >= 1.5) && (logg < 4.0)){
    if (teff < 3000.0) {
        spectralClass = "L";
        } else if ((teff >= 3000.0) && (teff < 3700.0))  {
        spectralClass = "M";
        if ((teff >= 3000.0) && (teff <= 3330.0)) {
            subClass = "6";
        } else if ((teff > 3330.0) && (teff <= 3380.0)) {
            subclass = "5";
        } else if ((teff > 3380.0) && (teff <= 3440.0)) {
            subClass = "4";
        } else if ((teff > 3440.0) && (teff <= 3480.0)) {
            subClass = "3";
        } else if ((teff > 3480.0) && (teff <= 3540.0)) {
            subClass = "2";
        } else if ((teff > 3540.0) && (teff <= 3600.0)) {
            subClass = "1";
        } else if ((teff > 3600.0) && (teff < 3700.0)) {
            subClass = "0";
        }
    } else if ((teff >= 3700.0) && (teff < 4700.0)) {
        spectralClass = "K";
        if ((teff >= 3700.0) && (teff <= 3870.0)) {
            subClass = "7";
        } else if ((teff > 3870.0) && (teff <= 4050.0)) {
            subClass = "5";
        } else if ((teff > 4050.0) && (teff <= 4150.0)) {
            subClass = "4";
        } else if ((teff > 4150.0) && (teff <= 4260.0)) {
            subClass = "3";
        } else if ((teff > 4260.0) && (teff <= 4510.0)) {
            subClass = "1";
        } else if ((teff > 4510.0) && (teff < 4700.0)) {
            subClass = "0";
        }
    } else if ((teff >= 4700.0) && (teff < 5500.0)) {
        spectralClass = "G";
        if ((teff >= 4700.0) && (teff <= 4800.0)) {
            subClass = "8";
        } else if ((teff > 4800.0) && (teff <= 5300.0)) {
            subClass = "2";
        } else if ((teff > 5300.0) && (teff < 5500.0)) {
            subClass = "0";
        }
    } else if ((teff >= 5500.0) && (teff < 7500.0)) {
        spectralClass = "F";
        if ((teff >= 5500.0) && (teff <= 6410.0)) {
            subClass = "5";
        } else if ((teff > 6410.0) && (teff <= 7000.0)) {
            subClass = "2";
        } else if ((teff > 7000.0) && (teff < 7500.0)) {
            subClass = "0";
        }
    } else if ((teff >= 7500.0) && (teff < 10300.0)) {
        spectralClass = "A";
        if ((teff >= 7500.0) && (teff <= 7830.0)) {
            subClass = "8";
        } else if ((teff > 7830.0) && (teff <= 8550.0)) {
            subClass = "5";
        } else if ((teff > 8550.0) && (teff <= 9460.0)) {
            subClass = "2";
        } else if ((teff > 9460.0) && (teff <= 9820.0)) {
            subClass = "1";
        } else if ((teff > 9820.0) && (teff < 10300.0)) {
            subClass = "0";
        }
    } else if ((teff >= 10300.0) && (teff < 29300.0)) {
        spectralClass = "B";
        if ((teff >= 10300.0) && (teff <= 10900.0)) {
            subClass = "9";
        } else if ((teff > 10900.0) && (teff <= 11700.0)) {
            subClass = "8";
        } else if ((teff > 11700.0) && (teff <= 12700.0)) {
            subClass = "7";
        } else if ((teff > 12700.0) && (teff <= 13800.0)) {
            subClass = "6";
        } else if ((teff > 13800.0) && (teff <= 15100.0)) {
            subClass = "5";
        } else if ((teff > 15100.0) && (teff <= 18300.0)) {
            subClass = "3";
        } else if ((teff > 18300.0) && (teff <= 20200.0)) {
            subClass = "2";
        } else if ((teff > 20200.0) && (teff <= 24500.0)) {
            subClass = "1";
        } else if ((teff > 24500.0) && (teff < 29300.0)) {
            subClass = "0";
        }
    } else if ((teff >= 29300.0) && (teff < 40000.0)) {
        spectralClass = "O";
        if ((teff >= 29300.0) && (teff <= 35000.0)) {
            subClass = "8";
        } else if ((teff > 35000.0) && (teff <= 36500.0)) {
            subClass = "7";
        } else if ((teff > 36500.0) && (teff <= 37800.0)) {
            subClass = "6";
        } else if ((teff > 37800.0) && (teff < 40000.0)) {
            subClass = "5";
        }
    }
}

//Determine the spectralClass and subClass of supergiants and bright giants. lburns
//var luminClass = "I" or luminClass = "II"
if ((logg >= -0.5) && (logg < 1.5)){
    if (teff < 2700.0) {
        spectralClass = "L";
        } else if ((teff >= 2700.0) && (teff < 3650.0)) {
        spectralClass = "M";
        if ((teff >= 2700.0) && (teff <= 2710.0)) {
            subClass = "6";
        } else if ((teff > 2710.0) && (teff <= 2880.0)) {
            subClass = "5";
        } else if ((teff > 2880.0) && (teff <= 3060.0)) {
            subClass = "4";
        } else if ((teff > 3060.0) && (teff <= 3210.0)) {
            subClass = "3";
        } else if ((teff > 3210.0) && (teff <= 3370.0)) {
            subClass = "2";
        } else if ((teff > 3370.0) && (teff <= 3490.0)) {
            subClass = "1";
        } else if ((teff > 3490.0) && (teff < 3650.0)) {
            subClass = "0";
        }
    } else if ((teff >= 3650.0) && (teff < 4600.0)) {
        spectralClass = "K";
        if ((teff >= 3650.0) && (teff <= 3830.0)) {
            subClass = "7";
        } else if ((teff > 3830.0) && (teff <= 3990.0)) {
            subClass = "5";
        } else if ((teff > 3990.0) && (teff <= 4090.0)) {
            subClass = "4";
        } else if ((teff > 4090.0) && (teff <= 4190.0)) {
            subClass = "3";
        } else if ((teff > 4190.0) && (teff <= 4430.0)) {
            subClass = "1";
        } else if ((teff > 4430.0) && (teff < 4600.0)) {
            subClass = "0";
        }
    } else if ((teff >= 4600.0) && (teff < 5500.0)) {
        spectralClass = "G";
        if ((teff >= 4600.0) && (teff <= 4700.0)) {
            subClass = "8";
        } else if ((teff > 4700.0) && (teff <= 5190.0)) {
            subClass = "2";
        } else if ((teff > 5190.0) && (teff < 5500.0)) {
            subClass = "0";
        }
    } else if ((teff >= 5500.0) && (teff < 7500.0)) {
        spectralClass = "F";
        if ((teff >= 5500.0) && (teff <= 5750.0)) {
            subClass = "8";
        } else if ((teff > 5750.0) && (teff <= 6370.0)) {
            subClass = "5";
        } else if ((teff > 6370.0) && (teff <= 7030.0)) {
            subClass = "2";
        } else if ((teff > 7030.0) && (teff < 7500.0)) {
            subClass = "0";
        }
    } else if ((teff >= 7500.0) && (teff < 10000.0)) {
        spectralClass = "A";
        if ((teff >= 7500.0) && (teff <= 7910.0)) {
            subClass = "8";
        } else if ((teff > 7910.0) && (teff <= 8610.0)) {
            subClass = "5";
        } else if ((teff > 8610.0) && (teff <= 9380.0)) {
            subClass = "2";
        } else if ((teff > 9380.0) && (teff < 10000.0)) {
            subClass = "0";
        }
    } else if ((teff >= 10000.0) && (teff < 27000.0)) {
        spectralClass = "B";
        if ((teff >= 10000.0) && (teff <= 10500.0)) {
            subClass = "9";
        } else if ((teff > 10500.0) && (teff <= 11100.0)) {
            subClass = "8";
        } else if ((teff > 11100.0) && (teff <= 11800.0)) {
            subClass = "7";
        } else if ((teff > 11800.0) && (teff <= 12600.0)) {
            subClass = "6";
        } else if ((teff > 12600.0) && (teff <= 13600.0)) {
            subClass = "5";
        } else if ((teff > 13600.0) && (teff <= 16000.0)) {
            subClass = "3";
        } else if ((teff > 16000.0) && (teff <= 17600.0)) {
            subClass = "2";
        } else if ((teff > 17600.0) && (teff <= 21400.0)) {
            subClass = "1";
        } else if ((teff > 21400.0) && (teff < 27000.0)) {
            subClass = "0";
        }
    } else if ((teff >= 27000.0) && (teff < 42000.0)) {
        spectralClass = "O";
        if ((teff >= 27000.0) && (teff <= 34000.0)) {
            subClass = "8";
        } else if ((teff > 34000.0) && (teff <= 36200.0)) {
            subClass = "7";
        } else if ((teff > 36200.0) && (teff <= 38500.0)) {
            subClass = "6";
        } else if ((teff > 38500.0) && (teff < 42000.0)) {
            subClass = "5";
        }
    }
}

//Determine luminClass based on logg
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
    } else if ((logg >= 5.0)){
        luminClass = "WD";
    }

    var spectralType = "<a href='https://en.wikipedia.org/wiki/Stellar_classification' title='MK Spectral type' target='_blank'>" +
            spectralClass + subClass +  " " + luminClass +
            "</a>";
    txtPrint(spectralType, 600, 40, 200, lineColor, textId);

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
    txtPrint(echoText, 750, 10, 200, lineColor, textId);


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


//Based on canvas --> SVG adaptation in ChromaStar:
// From CS: June 2017 - Graphics converted from <canvas> to scale-invariant <SVG> by Jason Bayer

// **************************
//
// Global graphical output variables:
//
//  Panel variables 
//  - set with HTML/CSS style parameters 
//  - in pixels
//
//How many rows and columns of plots:
  var numRows = 5;
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

    var wDefaultColor = "#F0F0F0"; //default value
    var wDiskColor = wDefaultColor; //needed to finesse background colour of white light image

    var charToPx = 4; // width of typical character font in pixels - CAUTION: finesse!


//First, remove all SVG elements:
    //var listOfSVGNodes = document.querySelectorAll("svg");
//We think everything will be a line or a circle:
    var listOfLineNodes = document.querySelectorAll("line");
    var listOfCircNodes = document.querySelectorAll("circle");

    //var numSVGNodes = listOfSVGNodes.length;
    var numLineNodes = listOfLineNodes.length;
    var numCircNodes = listOfCircNodes.length;

//We have to be prepared that this might be our first time through - ??:
    //if (numSVGNodes > 0){

//Remove line elements (axues, tickmarks, barns, etc.)
       if (numLineNodes > 0){
           for (var iNode = 0; iNode < numLineNodes; iNode++){
               listOfLineNodes[iNode].parentNode.removeChild(listOfLineNodes[iNode]);
           }
       } //numLineNodes > 0 condition

//Remove circle elements (axes, tickmarks, barns, etc.)
       if (numCircNodes > 0){
           for (var iNode = 0; iNode < numCircNodes; iNode++){
               listOfCircNodes[iNode].parentNode.removeChild(listOfCircNodes[iNode]);
           }
       } //numCircNodes > 0 condition

    //} //numSVGNdodes > 0 condition


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

//?? }


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
    //var iLamMinMaxBroad = minMax2(masterFluxBroad2);
    var iLamMax = iLamMinMax[1];
    var iLamMin = iLamMinMax[0];
    //var iLamMaxBroad = iLamMinMaxBroad[1];
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

    //Leave uncommented:
    var bNormVega, vNormVega, rNormVega;
    ////Uncomment this block and set input stellar parameters to Vega to re-calibrate 
    //// colors to make Vega's disk centre intensity white 
    var bvrVega0 = bandIntens[2][0] + bandIntens[3][0] + bandIntens[4][0];
    var bNormVega = bvrVega0 / bandIntens[2][0];
    var vNormVega = bvrVega0 / bandIntens[3][0];
    var rNormVega = bvrVega0 / bandIntens[4][0];
    //console.log("bNormVega " + bNormVega + " vNormVega  " + vNormVega + " rNormVega " + rNormVega);
//Set Vega disk centre intensity calibration factors (reciprocals of these should total to 1.0):
    bNormVega = 2.223444;
    vNormVega = 3.813167;
    rNormVega = 3.472246;

// Total B + V + R band intensity of prgram object at disk centre: 
    var bvr0 = bandIntens[2][0] + bandIntens[3][0] + bandIntens[4][0];
    //console.log("bvr0 " + bvr0);
//Find greatest disk-centre band-integrated I value among B, V, and R for final renormalization:
    var rrI = bandIntens[4][0] / bvr0 * rNormVega;
    var ggI = bandIntens[3][0] / bvr0 * vNormVega;
    var bbI = bandIntens[2][0] / bvr0 * bNormVega;
    //console.log("rrI " + rrI + " ggI " + ggI + " bbI " + bbI);
    var rrggbbI = [rrI, ggI, bbI];
    var minmaxI = minMax(rrggbbI);
    var maxI = minmaxI[1];
    var renormI = rrggbbI[maxI];
    //console.log("rrggbbI " + rrggbbI + " minmaxI " + minmaxI + " maxI " + maxI + " renormI " + renormI);
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

    rrI = bandIntens[4][i] / bvr0 * rNormVega;
    ggI = bandIntens[3][i] / bvr0 * vNormVega;
    bbI = bandIntens[2][i] / bvr0 * bNormVega;
    //console.log("i = 3:");
    //console.log("Before renorm: rrI " + rrI + " ggI " + ggI + " bbI " + bbI);
//Renormalize:
    rrI = rrI /renormI;
    ggI = ggI /renormI;
    bbI = bbI /renormI;
    //console.log("After renorm: rrI " + rrI + " ggI " + ggI + " bbI " + bbI);

    var RGBArr = [];
    RGBArr.length = 3;
    RGBArr[0] = Math.ceil(255.0 * rrI);
    RGBArr[1] = Math.ceil(255.0 * ggI);
    RGBArr[2] = Math.ceil(255.0 * bbI);
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
            xFinesse, color, areaId, cnvsId) {

        var yBarPosCnvs = yAxisLength * (yVal - minYDataIn) / (maxYDataIn - minYDataIn);
        //       xTickPos = xTickPos;

        var yShiftCnvs = (yAxisYCnvs + yAxisLength) - yBarPosCnvs;
        yShiftCnvs = Math.floor(yShiftCnvs);
        barWidthCnvs = Math.floor(barWidthCnvs);

// Make the y-tick mark, Teff:
                                //JB
// Make the y-tick mark, Teff:
        var thisLine = document.createElementNS(xmlW3, 'line');
        thisLine.setAttributeNS(null, 'x1', yAxisXCnvs);
        thisLine.setAttributeNS(null, 'x2', yAxisXCnvs + barWidthCnvs);
        thisLine.setAttributeNS(null, 'y1', yShiftCnvs);
        thisLine.setAttributeNS(null, 'y2', yShiftCnvs);
        thisLine.setAttributeNS(null, 'stroke', color);
        thisLine.setAttributeNS(null, 'stroke-width', 2);
        cnvsId.appendChild(thisLine);

                                //JB
//
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
            yFinesse, color, areaId, cnvsId) {

        var xBarPosCnvs = thisXAxisLength * (xVal - minXDataIn) / (maxXDataIn - minXDataIn);
        var xShiftCnvs = xAxisXCnvs + xBarPosCnvs;
        var yBarPosCnvs = yAxisYCnvs + yFinesse; 
        xShiftCnvs = Math.floor(xShiftCnvs);
        barHeightCnvs = Math.floor(barHeightCnvs);
        yBarPosCnvs = Math.floor(yBarPosCnvs);

// Make the x-tick mark, Teff:
                                        //JB
        var thisLine = document.createElementNS(xmlW3, 'line');
        thisLine.setAttributeNS(null, 'x1', xShiftCnvs);
        thisLine.setAttributeNS(null, 'x2', xShiftCnvs);
        thisLine.setAttributeNS(null, 'y1', yBarPosCnvs);
        thisLine.setAttributeNS(null, 'y2', yBarPosCnvs + barHeightCnvs);
        thisLine.setAttributeNS(null, 'stroke', color);
        thisLine.setAttributeNS(null, 'stroke-width', 2);
        cnvsId.appendChild(thisLine);

        return xShiftCnvs;
    };

//return the x position without creating a bar
    var YBarXVal = function(xVal, minXDataIn, maxXDataIn, barWidthCnvs, barHeightCnvs,yFinesse, color, areaId, cnvsId) {
                                        //JB
        var xBarPosCnvs = xAxisLength * (xVal - minXDataIn) / (maxXDataIn - minXDataIn);
        var xShiftCnvs = xAxisXCnvs + xBarPosCnvs;
        var yBarPosCnvs = yAxisYCnvs + yFinesse;

// Make the x-tick mark, Teff:
        return xShiftCnvs;
    };
                                        //JB

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
            areaId, cnvsId) {

        var axisParams = [];
        axisParams.length = 8;
        // Variables to handle normalization and rounding:
        var numParts = [];
        numParts.length = 2;

        //axisParams[5] = xLowerYOffset;
//
                                //JB
        var thisLine = document.createElementNS(xmlW3, 'line');
        thisLine.setAttributeNS(null, 'x1', xAxisXCnvs);
        thisLine.setAttributeNS(null, 'x2', xAxisXCnvs + thisXAxisLength);
        thisLine.setAttributeNS(null, 'y1', xAxisYCnvs);
        thisLine.setAttributeNS(null, 'y2', xAxisYCnvs);
        thisLine.setAttributeNS(null, 'stroke', lineColor);
        thisLine.setAttributeNS(null, 'stroke-width', 2);
        cnvsId.appendChild(thisLine);

//
        numParts = standForm(minXDataIn);
        //minXData = mantissa * Math.pow(10.0, numParts[1]);
        var mantissa0 = numParts[0];
        var exp0 = numParts[1];
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
        } else if ((rangeXData >= 5000.0) && (rangeXData < 20000.0)) {
            deltaXData = 2000.0;
        } else if ((rangeXData >= 1000.0) && (rangeXData < 5000.0)) {
            deltaXData = 200.0;
        } else if ((rangeXData >= 200.0) && (rangeXData < 1000.0)) {
            deltaXData = 100.0;
        } else if ((rangeXData >= 100.0) && (rangeXData < 200.0)) {
            deltaXData = 25.0;
        } else if ((rangeXData >= 50.0) && (rangeXData < 100.0)) {
            deltaXData = 10.0;
        } else if ((rangeXData >= 20.0) && (rangeXData < 50.0)) {
            deltaXData = 10.0;
        } else if ((rangeXData >= 10.0) && (rangeXData < 20.0)) {
            deltaXData = 5.0;
        } else if ((rangeXData > 5.0) && (rangeXData <= 10.0)) {
            deltaXData = 2.0;
        } else if ((rangeXData > 2.0) && (rangeXData <= 5.0)) {
            deltaXData = 0.5;
        } else if ((rangeXData > 1.0) && (rangeXData <= 2.0)) {
            deltaXData = 0.25;
        } else if ((rangeXData > 0.5) && (rangeXData <= 1.0)) {
            deltaXData = 0.2;
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

                                //JB
            var thisLine = document.createElementNS(xmlW3, 'line');
            thisLine.setAttributeNS(null, 'x1', xShiftCnvs);
            thisLine.setAttributeNS(null, 'x2', xShiftCnvs);
            thisLine.setAttributeNS(null, 'y1', xAxisYCnvs + xTickYOffset);
            thisLine.setAttributeNS(null, 'y2', xAxisYCnvs + xTickYOffset + tickLength);
            thisLine.setAttributeNS(null, 'stroke', lineColor);
            thisLine.setAttributeNS(null, 'stroke-width', 2);
            cnvsId.appendChild(thisLine);


            //Make the tick label, Teff:
            txtPrint("<span style='font-size:small'>" + xTickValStr + "</span>",
                xShiftCnvs, xAxisYCnvs + xValYOffset, 50, lineColor, areaId);

                                //JB

        }  // end x-tickmark loop


// Add name of x-axis:
//Axis label still needs to be html so we can use mark-up
        xAxisNameX = panelX + xAxisNameOffsetX;
        xAxisNameY = panelY + xAxisNameOffsetY;
        txtPrint("<span style='font-size:small'>" + xAxisName + "</span>",
                xAxisNameOffsetX, xAxisNameOffsetY, 75, lineColor, areaId);

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

    var pointsYA = "";
    var YAxis = function(panelX, panelY,
            minYDataIn, maxYDataIn, yAxisName,
            fineness, areaId, cnvsId) {

        var axisParams = [];
        axisParams.length = 8;
        // Variables to handle normalization and rounding:
        var numParts = [];
        numParts.length = 2;

        //axisParams[5] = xLowerYOffset;
        // Create the LEFT y-axis element and set its style attributes:

        //axisParams[5] = xLowerYOffset;
        // Create the LEFT y-axis element and set its style attributes:
                                //JB
        var thisLine = document.createElementNS(xmlW3, 'line');
        thisLine.setAttributeNS(null, 'x1', yAxisXCnvs);
        thisLine.setAttributeNS(null, 'x2', yAxisXCnvs);
        thisLine.setAttributeNS(null, 'y1', yAxisYCnvs);
        thisLine.setAttributeNS(null, 'y2', yAxisYCnvs + yAxisLength);
        thisLine.setAttributeNS(null, 'stroke', lineColor);
        thisLine.setAttributeNS(null, 'stroke-width', 2);
        cnvsId.appendChild(thisLine);

        numParts = standForm(minYDataIn);
        //minYData = mantissa * Math.pow(10.0, numParts[1]);
        var mantissa0 = numParts[0];
        var exp0 = numParts[1];
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
        //deltaYData = mantissa * Math.pow(10.0, numParts[1]);
        var deltaYPxl = panelHeight / (numYTicks - 1);
        var deltaYPxlCnvs = yAxisLength / (numYTicks - 1);
        axisParams[1] = rangeYData2;
        axisParams[2] = deltaYData;
        axisParams[3] = deltaYPxl;
        axisParams[6] = minYData2;
        axisParams[7] = maxYData2;
        //
        var ii;
        var pointsYt="";
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
                                           //JB
          var thisLine = document.createElementNS(xmlW3, 'line');
           thisLine.setAttributeNS(null, 'x1', yAxisXCnvs + yTickXOffset);
           thisLine.setAttributeNS(null, 'x2', yAxisXCnvs + yTickXOffset + tickLength);
           thisLine.setAttributeNS(null, 'y1', yShiftCnvs);
           thisLine.setAttributeNS(null, 'y2', yShiftCnvs);
           thisLine.setAttributeNS(null, 'stroke', lineColor);
           thisLine.setAttributeNS(null, 'stroke-width', 2);
           cnvsId.appendChild(thisLine);

            //Make the y-tick label:
         txtPrint("<span style='font-size:small'>" + yTickValStr + "</span>",
                   yAxisXCnvs + yValXOffset, yShiftCnvs, 50, lineColor, areaId);

        }  // end y-tickmark loop, j

// Add name of LOWER y-axis:

//Axis label still need to be html so we can use mark-up
        yAxisNameX = panelX + yAxisNameOffsetX;
        yAxisNameY = panelY + yAxisNameOffsetY;
        txtPrint("<span style='font-size:x-small'>" + yAxisName + "</span>",
                yAxisNameOffsetX, yAxisNameOffsetY, 75, lineColor, areaId);

        return axisParams;

    };

    //   var testVal = -1.26832e7;
//

    var xFinesse = 0.0; //default initialization
    var yFinesse = 0.0; //default initialization
    var barFinesse = 60; //default initialization

    //

// PLOT GRID PLAN as of 20 Jul 2016:
//     Cell entries:  Plot number Plot contents
// 
//   Col           0                 |  1                |  2
// 
//   Row: 0        7 Whte Lght Img   |  10 Spctrm Img    |  11 Life Zn
// 
//        1       12 Gauss Filt      |   9 HRD           |  5 SED   
// 
//        2       13 Synth Spec    <==> 13 Synth Spec  <==> 13 Synth Spec  
// 
//        3        4 Limb darkng     |  15 Four trnsfrm  | 16 GAS partial press   
// 
//        4        2 T_kin(tau)      |   3 P(tau)        |  14 kap(tau)
// 
//        5                          |                   |              
//
//
//
//  *****   PLOT THIRTEEN / PLOT 13 
//

//
// Plot "thirteen": synthetic spectrum 

        var plotRow = 2;
        var plotCol = 0;
        var thisXAxisLength = 1200;
        var thisPanelWidth = 1350;
//Triple wide:
        var minXData = 1.0e7 * specSynLams2[0];
        var maxXData = 1.0e7 * specSynLams2[numSpecSyn - 1];

        var xAxisName = "<em>&#955</em> (nm)";
        //now done above var norm = 1.0e15; // y-axis normalization
        var minYData = 0.0;
        var minMaxVals = minMax2(specSynFlux);
        var maxYData = 2.0 * specSynFlux[0][minMaxVals[1]];
        //var maxYData = 10.0;
        //var yAxisName = "<span title='Normalized flux'><a href='http://en.wikipedia.org/wiki/Spectral_flux_density' target='_blank'> <em>F</em><sub>&#955</sub> / <em>B</em><sub>&#955</sub></a></span>";
        var yAxisName = "<span title='Normalized flux'><a href='http://en.wikipedia.org/wiki/Spectral_flux_density' target='_blank'> <em>F</em><sub>&#955</sub> / <em>F</em><sup>c</sup><sub>&#955</sub></a></span>";

        var panelOrigin = washer(plotRow, plotCol, thisPanelWidth, wDefaultColor, plotThirteenId, cnvsThirteenId);
        panelX = panelOrigin[0];
        panelY = panelOrigin[1];
                               //JB

        var fineness = "hyperfine";
        //var fineness = "fine";
        var xAxisParams = XAxis(panelX, panelY, thisXAxisLength,  
                minXData, maxXData, xAxisName, fineness,
                plotThirteenId, cnvsThirteenId);

        var yAxisParams = YAxis(panelX, panelY,
                minYData, maxYData, yAxisName, fineness,
                plotThirteenId, cnvsThirteenId);
        //xOffset = xAxisParams[0];
        //yOffset = xAxisParams[4];
        var rangeXData13 = xAxisParams[1];
        var deltaXData13 = xAxisParams[2];
        var deltaXPxl13 = xAxisParams[3];
        var rangeYData13 = yAxisParams[1];
        var deltaYData13 = yAxisParams[2];
        var deltaYPxl13 = yAxisParams[3];
        var xLowerYOffset = xAxisParams[5];
        var minXData13 = xAxisParams[6]; //updated value
        var minYData13 = yAxisParams[6]; //updated value
        var maxXData13 = xAxisParams[7]; //updated value
        var maxYData13 = yAxisParams[7]; //updated value    
        var xAxisLength13 = xAxisLength;  //special case    
        //
        // Add legend annotation:

        titleX = panelX + titleOffsetX;
        titleY = panelY + titleOffsetY;
        txtPrint("<span style='font-size:small'>Spectrum synthesis: "
                 + lambdaStart + " < &#955 < " + lambdaStop + " nm, "
                 + "Min log<sub>10</sub><em>&#954</em><sub>l</sub>/<em>&#954</em><sub>c</sub> = " + lineThresh + ", "
                 + " " + numGaussLines + " lines included.  <br /> "
                 + " <a href='InputData/gsLineList.dat' target='_blank'>View the line list</a></span>",
                titleOffsetX, titleOffsetY, 300, lineColor, plotThirteenId);
        txtPrint("<span style='font-size:small; color:blue'><a href='http://en.wikipedia.org/wiki/Spectral_energy_distribution' target='_blank'>\n\
     Normalized spectrum synthesis region</a></span>",
                titleOffsetX+600, titleOffsetY, 300, lineColor, plotThirteenId);
        txtPrint("<span style='font-size:small; color:blue'> <em>v</em><sub>Rot</sub>=" + rotV + " km s<sup>-1</sup>"
                  + " <em>i</em><sub>Rot</sub>=" + rotI + "<sup>o</sup>"
                  + " <em>v</em><sub>Macro</sub>=" + macroV + " km s<sup>-1</sup></span>",
                titleOffsetX+1100, titleOffsetY, 300, lineColor, plotThirteenId);

// Equivalent width:
    roundNum = Wlambda.toFixed(2);
    txtPrint("<span title='Equivalent width of total line absorption in synthesis region'>\n\
<a href='http://en.wikipedia.org/wiki/Equivalent_width' target='_blank'>Total W<sub><em>&#955</em></sub></a>: \n\
</span>"
            + roundNum
            + " <span title='picometers'>\n\
<a href='http://en.wikipedia.org/wiki/Picometre' target='_blank'>pm</a>\n\
</span>",
           titleOffsetX + 600, titleOffsetY+35, 300, lineColor, plotThirteenId);

        var dSize = 1;
        opac = 1;

//Does Guasian filter fall within spectrum synthesis region:
        var plotFilt = false;
        var numGauss = gaussFilter[0].length;
        if ( ( (gaussFilter[0][0] > specSynLams2[0]) && (gaussFilter[0][0] < specSynLams2[numSpecSyn-1]) )
          || ( (gaussFilter[0][numGauss-1] > specSynLams2[0]) && (gaussFilter[0][numGauss-1] < specSynLams2[numSpecSyn-1]) ) ){
          //    console.log("plotFilt condition met");
              plotFilt = true;
        }
//Interpolate Gaussian filter used for monochromatic image onto synthetic spectrum wavelength
//grid for overplotting:
          var newFilter = [];
        if (plotFilt == true){
           newFilter = interpolV(gaussFilter[1], gaussFilter[0], specSynLams2);
        }

    //cnvsFiveId.addEventListener("mouseover", function() { 
    cnvsThirteenId.addEventListener("click", function() {
       //dataCoords(event, plotFiveId);
       var xyString = dataCoords(event, cnvsThirteenId, xAxisLength13, minXData13, rangeXData13, xAxisXCnvs,
                               yAxisLength, minYData13, rangeYData13, yAxisYCnvs);
       txtPrint(xyString, titleOffsetX+200, titleOffsetY+320, 150, lineColor, plotThirteenId);
    });


        var lambdanm = 1.0e7 * specSynLams2[0];
        var xTickPosCnvs = thisXAxisLength * (lambdanm - minXData13) / (rangeXData13); // pixels
        var lastXShiftCnvs = xAxisXCnvs + xTickPosCnvs;
        var yTickPosCnvs = yAxisLength * (specSynFlux[0][0] - minYData13) / rangeYData13;
        // vertical position in pixels - data values increase upward:
        var lastYShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;
        var xShiftCnvs, yShiftCnvs, yShiftFiltCnvs;
//Gaussian filter:
        var yTickPosFiltCnvs;
        var lastYShiftFiltCnvs;
        if (plotFilt == true){
          yTickPosFiltCnvs = yAxisLength * ((newFilter[0]) - minYData13) / rangeYData13;
          lastYShiftFiltCnvs = (yAxisYCnvs + yAxisLength) - yTickPosFiltCnvs;
        }

        var RGBHex = colHex(0, 0, 0);

        for (var i = 1; i < numSpecSyn; i++) {


            lambdanm = 1.0e7 * specSynLams2[i]; //cm to nm //linear
            ii = 1.0 * i;
            xTickPosCnvs = thisXAxisLength * (lambdanm - minXData13) / (rangeXData13); // pixels   //linear

            // horizontal position in pixels - data values increase rightward:
            xShiftCnvs = xAxisXCnvs + xTickPosCnvs;
            xShiftCnvs = Math.floor(xShiftCnvs);

            yTickPosCnvs = yAxisLength * (specSynFlux[0][i] - minYData13) / rangeYData13;
            //console.log("i " + i + " 1.0e7 * specSynLams2[i] " + 1.0e7 * specSynLams2[i] + " specSynFlux[0][i] " + specSynFlux[0][i]);
            // vertical position in pixels - data values increase upward:
            yShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;
            yShiftCnvs = Math.floor(yShiftCnvs);

//Gaussian filter:
        if (plotFilt == true){
            yTickPosFiltCnvs = yAxisLength * (newFilter[i] - minYData13) / rangeYData13;
            yShiftFiltCnvs = (yAxisYCnvs + yAxisLength) - yTickPosFiltCnvs;
            yShiftFiltCnvs = Math.floor(yShiftFiltCnvs);
        }

            var thisLine = document.createElementNS(xmlW3, 'line');
            thisLine.setAttributeNS(null, 'x1', lastXShiftCnvs);
            thisLine.setAttributeNS(null, 'x2', xShiftCnvs);
            thisLine.setAttributeNS(null, 'y1', lastYShiftCnvs);
            thisLine.setAttributeNS(null, 'y2', yShiftCnvs);
            thisLine.setAttributeNS(null, 'stroke', RGBHex);
            thisLine.setAttributeNS(null, 'stroke-width', 2);
            cnvsThirteenId.appendChild(thisLine);

            lastXShiftCnvs = xShiftCnvs;
            lastYShiftCnvs = yShiftCnvs;


//Gaussian filter:
        if (plotFilt == true){
            yTickPosFiltCnvs = yAxisLength * (newFilter[i] - minYData13) / rangeYData13;
            yShiftFiltCnvs = (yAxisYCnvs + yAxisLength) - yTickPosFiltCnvs;
            yShiftFiltCnvs = Math.floor(yShiftFiltCnvs);

            var thisLineFilt = document.createElementNS(xmlW3, 'line');
            thisLineFilt.setAttributeNS(null, 'x1', lastXShiftCnvs);
            thisLineFilt.setAttributeNS(null, 'x2', xShiftCnvs);
            thisLineFilt.setAttributeNS(null, 'y1', lastYShiftFiltCnvs);
            thisLineFilt.setAttributeNS(null, 'y2', yShiftFiltCnvs);
            thisLineFilt.setAttributeNS(null, 'stroke', RGBHex);
            thisLineFilt.setAttributeNS(null, 'stroke-width', 2);
            cnvsThirteenId.appendChild(thisLineFilt);
            lastYShiftFiltCnvs = yShiftFiltCnvs;
          }
 
        }


      //  txtPrint("<span style='font-size:small; color:blue'> <em>v</em><sub>Rot</sub>=" + rotV + " km s<sup>-1</sup>"
      //            + " <em>i</em><sub>Rot</sub>=" + rotI + "<sup>o</sup>"
      //            + " <em>v</em><sub>Macro</sub>=" + macroV + " km s<sup>-1</sup></span>",
      //          titleOffsetX+1100, titleOffsetY, lineColor, plotThirteenId);

//Spectral line labels and pointers:
        var r255 = 0;
        var g255 = 0;
        var b255 = 0;
        barWidth = 2.0;
        barHeight = 20; //initialize
        RGBHex = "#000000"; //black
        yFinesse = -160;
        var thisYPos = xAxisYCnvs + yFinesse;
        //
        var barFinesse = 60;
        for (var i = 0; i < numGaussLines; i++) {
     
            //console.log("listElements[i] " + listElements[i] + " listStages[i] " + listStages[i]);
            //if ( (listElements[i] === "Fe" ) && (listStages[i] === "I" ) ){
              //  console.log("Label condition enetered");

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

             //  console.log("thisXAxisLength " + thisXAxisLength + " listLams[i] " + listLams[i] + " minXData " + minXData + " maxXData " + maxXData);
               xPos = thisXAxisLength * (listLams[i] - minXData13) / (maxXData13 - minXData13);
               xPos = xPos - 5; // finesse

               nameLbl = "<span style='font-size: xx-small'>" + listElements[i] + " " + listStages[i] + "</span>";
               lamLblNum = listLams[i].toPrecision(6);
               lamLblStr = lamLblNum.toString(10);
               lamLbl = "<span style='font-size: xx-small'>" + lamLblStr + "</span>";
               //RGBHex = colHex(r255, g255, b255);

            //console.log("xPos " + xPos + " xAxisXCnvs " + xAxisXCnvs + " yPos " + yPos);
               txtPrint(nameLbl, xPos + xAxisXCnvs, (yPos - 10), 100, RGBHex, plotThirteenId);
               txtPrint(lamLbl, xPos + xAxisXCnvs, yPos, 100, RGBHex, plotThirteenId);
               xShiftDum = YBar(listLams[i], minXData13, maxXData13, thisXAxisLength, barWidth, barHeight,
                       barFinesse, RGBHex, 100, plotThirteenId, cnvsThirteenId);


        }

//Label TiO band origins:
//Set up for molecules with JOLA bands:
   var jolaTeff = 5000.0;
   var numJola = 3; //for now
   var jolaSpecies = [];
   jolaSpecies.length = numJola; // molecule name
   var jolaSystem = []
   jolaSystem.length = numJola; //band system
   var jolaLabel = []
   jolaLabel.length = numJola; //band system

   jolaSpecies[0] = "TiO"; // molecule name
   jolaSystem[0] = "TiO_C3Delta_X3Delta"; //band system //DeltaLambda=0
   jolaLabel[0] = "TiO C<sup>3</sup>&#916-X<sup>3</sup>&#916"; //band system //DeltaLambda=0
   jolaSpecies[1] = "TiO"; // molecule name
   jolaSystem[1] = "TiO_c1Phi_a1Delta"; //band system //DeltaLambda=1
   jolaLabel[1] = "TiO c<sup>1</sup>&#934-a<sup>1</sup>&#916"; //band system //DeltaLambda=1
   jolaSpecies[2] = "TiO"; // molecule name
   jolaSystem[2] = "TiO_A3Phi_X3Phi"; //band system //DeltaLambda=0
   jolaLabel[2] = "TiO A<sup>3</sup>&#934_X<sup>3</sup>&#934"; //band system //DeltaLambda=0
   RGBHex = colHex(255, 0, 0);
   if (ifTiO == 1){
   if (teff < jolaTeff){

        for (var i = 0; i < numJola; i++) {

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

            var jolaOmega0 = getOrigin(jolaSystem[i]);
            var lambda0 = 1.0e7 / jolaOmega0;
            //console.log("lambda0 " + lambda0);
            xPos = thisXAxisLength * (lambda0 - minXData13) / (maxXData13 - minXData13);
            xPos = xPos - 5; // finesse

            nameLbl = "<span style='font-size: xx-small'>" + jolaLabel[i] + "</span>";
            //lamLblNum = listLams[i].toPrecision(6);
            //lamLblStr = lamLblNum.toString(10);
            //lamLbl = "<span style='font-size: xx-small'>" + lamLblStr + "</span>";
            //RGBHex = colHex(r255, g255, b255);
            txtPrint(nameLbl, xPos + xAxisXCnvs, (yPos - 10), 100, RGBHex, plotThirteenId);
            //txtPrint(lamLbl, xPos + xAxisXCnvs, yPos, RGBHex, plotThirteenId);
            xShiftDum = YBar(lambda0, minXData13, maxXData13, thisXAxisLength, barWidth, barHeight,
                    barFinesse, RGBHex, plotThirteenId, cnvsThirteenId);
        }
   } //jolaTeff condition
   } // ifTiO condition


           //monochromatic disk lambda
            barFinesse = yAxisYCnvs;
            barHeight = 18;
            barWidth = 2;
            RGBHex = "#FF0000";
            if ( (diskLambda > lambdaStart) && (diskLambda < lambdaStop) ){
                 xShiftDum = YBar(diskLambda, minXData13, maxXData13, thisXAxisLength,
                               barWidth, barHeight,
                               barFinesse-60, RGBHex, plotThirteenId, cnvsThirteenId);
                 txtPrint("<span style='font-size:xx-small'>Filter</span>",
                       xShiftDum, yAxisYCnvs, 100, RGBHex, plotThirteenId);
            }


//
//  *****   PLOT SEVEN / PLOT 7
//
//

// Plot seven - image of limb-darkened and limb-colored WHITE LIGHT stellar disk
//

        var plotRow = 0;
        var plotCol = 0;

//background color needs to be finessed so that white-ish stars will stand out:
       if (teff > 6000.0){
  //hotter white or blue-white star - darken the background (default background in #F0F0F0
           wDiskColor = "#808080";  
       } else {
           wDiskColor = wDefaultColor;
       }
				//JB
        var panelOrigin = washer(plotRow, plotCol, panelWidth, wDiskColor, plotSevenId, cnvsSevenId);
				//JB
	panelX = panelOrigin[0];
        panelY = panelOrigin[1];
 //console.log("plotRow, plotCol,panelX panelY " + plotRow + " " + plotCol + " " + panelX + " " + panelY);
				//JB
	cnvsSevenId.setAttribute('fill', wDiskColor);

        var thet1, thet2;
        var thet3;

        titleX = panelX + titleOffsetX;
        titleY = panelY + titleOffsetY;
        txtPrint("<span style='font-size:normal; color:blue'><a href='http://en.wikipedia.org/wiki/Limb_darkening' target='_blank'>White light disk</a></span> <br />\n\
     <span style='font-size:small'>(Logarithmic radius) </span>",
                titleOffsetX, titleOffsetY, 300, lineColor, plotSevenId);
            txtPrint("<span style='font-size:normal; color:black'><em>&#952</em> = </span>",
                150 + titleOffsetX, titleOffsetY, 300, lineColor, plotSevenId);
                                //JB
            var yCenterCnvs = panelHeight / 2; 
            var xCenterCnvs = panelWidth / 2;
				//JB

	var limbRadius = Math.ceil(radiusPx * Math.sin(Math.acos(cosTheta[1][numThetas-1])));

                        
				//JB
//console.log(numThetas);
				
// Adjust position to center star:
// Radius is really the *diameter* of the symbol

        //  Loop over limb darkening sub-disks - largest to smallest
         for (var i = numThetas - 1; i >= 1; i--) {
         //for (var i = numThetas - 1; i >= numThetas - 1; i--) {
//	for (var i = numThetas - 1; i <= 1; i++) {
	
            ii = 1.0 * i;

            // LTE Eddington-Barbier limb darkening: I(Tau=0, cos(theta)=t) = B(T(Tau=t))
            var cosFctr = cosTheta[1][i];

            var cosFctrNext = cosTheta[1][i-1];
			
            var radiusPxICnvs = Math.ceil(radiusPx * Math.sin(Math.acos(cosFctr)));
            var radiusPxICnvsNext = Math.ceil(radiusPx * Math.sin(Math.acos(cosFctrNext)));

            rrI = bandIntens[4][i] / bvr0 * rNormVega; 
            ggI = bandIntens[3][i] / bvr0 * vNormVega; 
            bbI = bandIntens[2][i] / bvr0 * bNormVega; 
            //console.log("ii " + ii + " rrI " + rrI + " ggI " + ggI + " bbI " + bbI);
            rrI = Math.ceil(255.0 * rrI / renormI); 
            ggI = Math.ceil(255.0 * ggI / renormI); 
            bbI = Math.ceil(255.0 * bbI / renormI); 
            //console.log("Renormalized: rrI " + rrI + " ggI " + ggI + " bbI " + bbI);
            var rrINext = bandIntens[4][i-1] / bvr0 * rNormVega; 
            var ggINext = bandIntens[3][i-1] / bvr0 * vNormVega; 
            var bbINext = bandIntens[2][i-1] / bvr0 * bNormVega;
            rrINext = Math.ceil(255.0 * rrINext / renormI); 
            ggINext = Math.ceil(255.0 * ggINext / renormI); 
            bbINext = Math.ceil(255.0 * bbINext / renormI); 

            var RGBHex = colHex(rrI, ggI, bbI);
            var RGBHexNext = colHex(rrINext, ggINext, bbINext);
 				//JB
            //console.log("RGBHex " + RGBHex + " RGBHexNext "  + RGBHexNext);

//	if((radiusPxICnvs==radiusPxICnvsNext)){ radiusPxICnvsNext = radiusPxICnvs - 0.9*i/3;}

//create gradient for each circle
        var thisCircR = radiusPxICnvsNext/radiusPxICnvs;
        //console.log("thisCircR " + thisCircR);
/* Can't get radial gradient to work
	var grd = document.createElementNS(xmlW3, 'radialGradient');
	grd.setAttributeNS(null, 'id', 'grdId');
        grd.setAttributeNS(null, 'cx', "50%");
        grd.setAttributeNS(null, 'cy', "50%");
        grd.setAttributeNS(null, 'r', thisCircR);
        //grd.setAttributeNS(null, 'r', 0.5);
        //grd.setAttributeNS(xmlns,xmlnsLink,xmlnsLink2);			

       // console.log(radiusPxICnvsNext/radius);
	
        var stop0 = document.createElementNS(xmlW3, 'stop');
        //stop0.setAttributeNS(null, 'offset', 0.0);
        stop0.setAttributeNS(null, 'offset', thisCircR);
        stop0.setAttributeNS(null, 'stop-color', RGBHexNext);
	stop0.setAttributeNS(null, 'stop-opacity', 1.0);
        grd.appendChild(stop0);

       //  console.log(radiusPxICnvsNext/radiusPxICnvs);

        var stop1 = document.createElementNS(xmlW3, 'stop');
        stop1.setAttributeNS(null, 'offset', 1.0);
        stop1.setAttributeNS(null, 'stop-color', RGBHex);
	stop1.setAttributeNS(null, 'stop-opacity', 1.0);
        grd.appendChild(stop1);

	//gradN1.setAttributeNS(xmlns,xmlnsLink,xmlnsLink2);
        //gradN2.setAttributeNS(xmlns,xmlnsLink,xmlnsLink2);
				//JB
*/
//create circle for each theta
        var circ = document.createElementNS(xmlW3, 'circle');
        circ.setAttributeNS(null, 'cx', xCenterCnvs);
        circ.setAttributeNS(null, 'cy', yCenterCnvs);
        circ.setAttributeNS(null, 'r', radiusPxICnvs);
        //circ.setAttributeNS(null, 'fill', 'url(#grdId)');
        circ.setAttributeNS(null, 'fill', RGBHexNext);
        //circ.setAttributeNS(xmlns, xmlnsLink, xmlnsLink2);

        //cnvsSevenId.appendChild(grd);
        cnvsSevenId.appendChild(circ);
       
 				//JB
            //
            //Angle indicators
            if ((i % 2) === 0) {
                thet1 = 180.0 * Math.acos(cosTheta[1][i]) / Math.PI;
                thet2 = thet1.toPrecision(2);
                thet3 = thet2.toString(10);
				//JB
                txtPrint("<span style='font-size:small; background-color:#888888'>" + thet3 + "</span>",
                        150 + titleOffsetX + (i + 2) * 10, titleOffsetY, 50, RGBHex, plotSevenId);

				//JB    
	    }
//
        }  // numThetas loop, i
//	document.body.appendChild(SVGSeven);


//
//  *****   PLOT TWELVE / PLOT 12
//
//

// Plot twelve - image of limb-darkened and limb-colored TUNABLE MONOCHROMATIC stellar disk

        var plotRow = 5;
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
				//JB
        var panelOrigin = washer(plotRow, plotCol, panelWidth, wDefaultColor, plotTwelveId, cnvsTwelveId);
				
				//JB
        panelX = panelOrigin[0];
        panelY = panelOrigin[1];
				//JB
	cnvsTwelveId.setAttribute('fill',wDefaultColor);
				//JB
        // Add title annotation:

        //var titleYPos = xLowerYOffset - 1.15 * yRange;
        //var titleXPos = 1.02 * xOffset;

        titleX = panelX + titleOffsetX;
        titleY = panelY + titleOffsetY;
					//JB
        txtPrint("<span style='font-size:normal; color:blue'><a href='http://en.wikipedia.org/wiki/Limb_darkening' target='_blank'>Gaussian filter</a></span><span style='font-size:small'> &#955 = " + diskLambda + " nm</span> </br>\n\
     <span style='font-size:small'>(Logarithmic radius) </span>",
                titleOffsetX, titleOffsetY + 20, 300, lineColor, plotTwelveId);
        txtPrint("<span style='font-size:normal; color:black'><em>&#952</em> = </span>",
                220 + titleOffsetX, titleOffsetY + 20, 300, lineColor, plotTwelveId);
        var ilLam0 = lamPoint(numMaster, masterLams, 1.0e-7 * diskLambda);
        var lambdanm = masterLams[ilLam0] * 1.0e7; //cm to nm
        //console.log("PLOT TWELVE: ilLam0=" + ilLam0 + " lambdanm " + lambdanm);
 
					//JB
        var minZData = 0.0;
        //var maxZData = masterIntens[ilLam0][0] / norm;
        var maxZData = tuneBandIntens[0] / norm;
        var rangeZData = maxZData - minZData;

// Adjust position to center star:
// Radius is really the *diameter* of the symbol
            var yCenterCnvs = panelHeight / 2; 
            var xCenterCnvs = panelWidth / 2; 

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


            //var zLevel = ((masterIntens[ilLam0][i] / norm) - minZData) / rangeZData;
            //var zLevelNext = ((masterIntens[ilLam0][i-1] / norm) - minZData) / rangeZData;
            var zLevel = ((tuneBandIntens[i] / norm) - minZData) / rangeZData;
            var zLevelNext = ((tuneBandIntens[i-1] / norm) - minZData) / rangeZData;

            //console.log("lambdanm " + lambdanm + " zLevel " + zLevel);

            RGBHex = lambdaToRGB(lambdanm, zLevel);
            RGBHexNext = lambdaToRGB(lambdanm, zLevelNext);

            thisCircR = radiusPxICnvsNext/radiusPxICnvs;
				//JB

/* Can't get SVG ring gradients to work
//create gradient for each circle
	var grd = document.createElementNS(xmlW3,'radialGradient');
	grd.setAttributeNS(null, 'id', 'grdId');
        grd.setAttributeNS(null, 'cx', 0.5);
        grd.setAttributeNS(null, 'cy', 0.5);
        grd.setAttributeNS(null, 'r', 0.5);
        //grd.setAttributeNS(xmlns,xmlnsLink,xmlnsLink2);			

       // console.log(radiusPxICnvsNext/radius);
	
        var stop0 = document.createElementNS(xmlW3, 'stop');
        stop0.setAttributeNS(null, 'offset', 1.0);
        stop0.setAttributeNS(null, 'stop-color', RGBHex);
	stop0.setAttributeNS(null, 'stop-opacity', 1);
        grd.appendChild(stop0);

       //  console.log(radiusPxICnvsNext/radiusPxICnvs);

        var stop1 = document.createElementNS(xmlW3, 'stop');
        stop1.setAttributeNS(null, 'offset', radiusPxICnvsNext/radiusPxICnvs)//"0%");
        stop1.setAttributeNS(null, 'stop-color', RGBHexNext);
	stop1.setAttributeNS(null, 'stop-opacity', 1);
        grd.appendChild(stop1);

	//gradN1.setAttributeNS(xmlns,xmlnsLink,xmlnsLink2);
        //gradN2.setAttributeNS(xmlns,xmlnsLink,xmlnsLink2);
				//JB
*/
//create circle for each theta
        var circ = document.createElementNS(xmlW3, 'circle');
        circ.setAttributeNS(null, 'cx',xCenterCnvs);
        circ.setAttributeNS(null, 'cy',yCenterCnvs);
        circ.setAttributeNS(null, 'r',radiusPxICnvs);
   //     circ.setAttributeNS(null, 'fill', 'url(grdId)');
        circ.setAttributeNS(null, 'fill', RGBHex);
        //circ.setAttributeNS(xmlns, xmlnsLink, xmlnsLink2);

        //cnvsTwelveId.appendChild(grd);
        cnvsTwelveId.appendChild(circ);
       
				//JB
        //
       //Angle indicators
        if ((i % 2) === 0) {
           thet1 = 180.0 * Math.acos(cosTheta[1][i]) / Math.PI;
           thet2 = thet1.toPrecision(2);
           thet3 = thet2.toString(10);
				//JB
           txtPrint("<span style='font-size:small; background-color:#888888'>" + thet3 + "</span>",
                   220 + titleOffsetX + (i + 2) * 10, titleOffsetY + 20, 300, RGBHex, plotTwelveId);

                             }
//
        } //numThetas loop, i
                          
    //
    //
    //  *****   PLOT TEN / PLOT 10
    //
    //
    // Plot Ten: Spectrum image

        var plotRow = 0;
        var plotCol = 1;

        var minXData = 380.0; // (nm) blue
        if($("[name='Teff']").val() >= 7000){
         minXData = 360.0;
        }
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
        var xAxisName = "<em>&#955</em> (nm)";

        
        var fineness = "normal";
				//JB
        var panelOrigin = washer(plotRow, plotCol, panelWidth, wDefaultColor, plotTenId, cnvsTenId);
				//JB

        panelX = panelOrigin[0];
        panelY = panelOrigin[1];
				//JB
//	SVGTen.setAttribute('fill',wDefaultColor);
				//JB
				//JB
        var xAxisParams = XAxis(panelX, panelY, xAxisLength,
                                minXData, maxXData, xAxisName, fineness, plotTenId, cnvsTenId);

				//JB
        //xOffset = xAxisParams[0];
        //yOffset = xAxisParams[4];
        var rangeXData10 = xAxisParams[1];
        var deltaXData10 = xAxisParams[2];
        var deltaXPxl10 = xAxisParams[3];
        var minXData10 = xAxisParams[6]; //updated value
        var maxXData10 = xAxisParams[7]; //updated value
        var xAxisLength10 = xAxisLength;
        //

        // var yAxisParams = YAxis(plotRow, plotCol,
        //        minYData, maxYData, yAxisName,
        //        plotTenId);

        //var zRange = 255.0;  //16-bit each for RGB (48-bit colour??)

        //var rangeXData = xAxisParams[1];
        //var rangeYData = yAxisParams[1];
        //var deltaYData = yAxisParams[2];
        //var deltaYPxl = yAxisParams[3];
        //var xLowerYOffset = xAxisParams[5];
        //minXData = xAxisParams[6];  //updated value
        //minYData = yAxisParams[6];  //updated value


        //txtPrint(" ", legendXPos, legendYPos + 10, zeroInt, zeroInt, zeroInt, plotTenId);
        titleX = panelX + titleOffsetX;
        titleY = panelY + titleOffsetY;
					//JB
        txtPrint("<span style='font-size:normal; color:blue'><a href='https://en.wikipedia.org/wiki/Visible_spectrum' target='_blank'>\n\
     Visual spectrum</a></span>",
                titleOffsetX, titleOffsetY, 300, lineColor, plotTenId);
					//JB
     var TiOString = "Off";
     if (ifTiO == 1){
        TiOString = "On";
     }
					//JB
     txtPrint("TiO bands: " + TiOString, titleOffsetX + 10, titleOffsetY+35, 300, lineColor, plotTenId);
					//JB
        var xShift, zShift, xShiftDum, zLevel;
        var RGBHex; //, r255, g255, b255;
        //var rangeXData10 = 1.0e7 * (masterLams[ilLam1] - masterLams[ilLam0]); //already consistenty computed by XAxis()
        //console.log("minXData " + minXData + " ilLam0 " + ilLam0 + " masterLams[ilLam0] " + masterLams[ilLam0]);

        var barWidth, xBarShift0, xBarShift1, xPos, yPos, nameLbl, lamLbl, lamLblStr, lamLblNum;
        var barHeight = 75.0;

//We can only palce vertical bars by setting marginleft, so search *AHEAD* in wavelength to find width
// of *CURRENT* bar.
        var lambdanm = masterLams[ilLam0] * 1.0e7; //cm to nm
        //console.log("ilLam0 " + ilLam0 + " ilLam1 " + ilLam1);
        yFinesse = -160;
        var thisYPos = xAxisYCnvs + yFinesse;


//variables needed in the loop, mostly for scaling/converting to nm

                              //JB
        for (var i = ilLam0 - 1; i < ilLam1 + 1; i++) {
        //for (var i = ilLam0 - 1; i < ilLam0 + 5; i++) {

            var nextLambdanm = masterLams[i] * 1.0e7; //cm to nm

            xBarShift0 = xAxisLength10 * (lambdanm - minXData10) / (maxXData10 - minXData10);
            xBarShift1 = xAxisLength10 * (nextLambdanm - minXData10) / (maxXData10 - minXData10);
            barWidth = xBarShift1 - xBarShift0; //in device pixels

if (barWidth > 0.5) {
//count ++;     
                barWidth = barWidth + 1.0;
                zLevel = ((masterFlux[0][i] / norm) - minZData) / rangeZData;
            var nextRGBHex = lambdaToRGB(lambdanm, zLevel);

        var xTickPosCnvs = xAxisLength10 * (lambdanm - minXData10) / (maxXData10 - minXData10);
        var xShiftCnvs = xAxisXCnvs + xTickPosCnvs;
/*
 * Gradients not working
//create gradient for each bar 
	var grd = document.createElementNS(xmlW3, 'linearGradient');
	grd.setAttributeNS(null, 'id', 'grdId');
        grd.setAttributeNS(null, 'x1', 0.0);
        grd.setAttributeNS(null, 'x2', 1.0);
        grd.setAttributeNS(null, 'y1', 0.0);
        grd.setAttributeNS(null, 'y2', 0.0);
        //grd.setAttributeNS(xmlns,xmlnsLink,xmlnsLink2);			

//create TWO stops per loop and add the to the gradient
        var stop0 = document.createElementNS(xmlW3, 'stop');
        stop0.setAttributeNS(null, 'offset', 0.0);
        //stop0.setAttributeNS(null, 'stop-color', RGBHex);
        stop0.setAttributeNS(null, 'stop-color', '#FF0000');
	//stop0.setAttributeNS(null, 'stop-opacity', 1);
        grd.appendChild(stop0);

        var stop1 = document.createElementNS(xmlW3, 'stop');
        stop1.setAttributeNS(null, 'offset', 1.0);
        //stop1.setAttributeNS(null, 'stop-color', RGBHexNext);
        stop1.setAttributeNS(null, 'stop-color', '#FF0000');
	//stop1.setAttributeNS(null, 'stop-opacity', 1);
        grd.appendChild(stop1);

        console.log("stop0 " + stop0 + " stop1 " + stop1 + " grd " + grd);
*/
				//JB
//create rectangle for each theta
  //console.log("xShiftCnvs " + xShiftCnvs + " thisYPos " + thisYPos + " barWidth " + barWidth + " barHeight " + barHeight);
  //console.log("RGBHex " + RGBHex + " RGBHexNext " + RGBHexNext);
        var rect = document.createElementNS(xmlW3, 'rect');
        rect.setAttributeNS(null, 'x', xShiftCnvs);
        rect.setAttributeNS(null, 'y', thisYPos);
        rect.setAttributeNS(null, 'width', barWidth);
        rect.setAttributeNS(null, 'height', barHeight);
        //rect.setAttributeNS(null, 'fill', 'url(grdId)');
        rect.setAttributeNS(null, 'fill', RGBHex);
        //rect.setAttributeNS(xmlns, xmlnsLink, xmlnsLink2);

        //cnvsTenId.appendChild(grd);
        cnvsTenId.appendChild(rect);
       

                      //JB
//
//THIS CODE IS USED TO CREATE SEPERATE RECTANGLES WITH THEIR OWN GRADIENTS 
// (ABOVE HAS MANY STOPS AND A SINGLE GRADIENT)
//
//
//!function I(ii){
//        var   trueCoord = lastNm;
//}(i);
//                lambdanm = nextLambdanm;
//                RGBHex = nextRGBHex;
//            }  //barWidth condition
//        }  // i loop (wavelength)

                              //JB

                //console.log("lambdanm " + lambdanm + " nextLambdanm " + nextLambdanm + " xShiftDum " + xShiftDum + " barWidth " + barWidth);

                lambdanm = nextLambdanm;
                RGBHex = nextRGBHex;
            }  //barWidth condition
        }  // i loop (wavelength)


       var yAxisLength10 = 1.0;  //special
       var minYData10 = -0.5;
       var rangeYData10 = 1.0; 
    //cnvsTenId.addEventListener("mouseover", function() { 
    cnvsTenId.addEventListener("click", function() {
       //dataCoords(event, plotTenId);
       //Fix - this is an image, not a plot - the y-axis doesn't mean anything:
       var xyString = dataCoords(event, cnvsTenId, xAxisLength10, minXData10, rangeXData10, xAxisXCnvs,
                               yAxisLength10, minYData10, rangeYData10, yAxisYCnvs);
       //console.log("PLOT 10: xyString: " + xyString);
       txtPrint(xyString, titleOffsetX+200, titleOffsetY+320, 150, lineColor, plotTenId);
    });



           //monochromatic disk lambda
            var barFinesse = yAxisYCnvs;
            barHeight = 108;
            barWidth = 2;
            RGBHex = "#FF0000";

           if ( (diskLambda > 380.0) && (diskLambda < 680.0) ){
                 xShiftDum = YBar(diskLambda, minXData, maxXData, xAxisLength10,
                               barWidth, barHeight,
                               barFinesse-60, RGBHex, plotTenId, cnvsTenId);
                 txtPrint("<span style='font-size:xx-small'>Filter</span>",
                       xShiftDum, yAxisYCnvs, 100, RGBHex, plotTenId);
            }


                                      //JB

				
    //
    //
    //  *****   PLOT NINE / PLOT 9
    //
    //
    // Plot Nine: HRDiagram

        var plotRow = 1;
        var plotCol = 1;

//background color needs to be finessed so that white-ish stars will stand out:
       if (teff > 6000.0){
  //hotter white or blue-white star - darken the background (default background in #F0F0F0
           wDiskColor = "#808080";  
       } else {
           wDiskColor = wDefaultColor;
       }
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
				//JB
       var panelOrigin = washer(plotRow, plotCol, panelWidth, wDiskColor, plotNineId, cnvsNineId);

				//JB
        panelX = panelOrigin[0];
        panelY = panelOrigin[1];

				//JB
        var xAxisParams = XAxis(panelX, panelY, xAxisLength,
                minXData, maxXData, xAxisName, fineness,
                plotNineId, cnvsNineId);

        var yAxisParams = YAxis(panelX, panelY,
                minYData, maxYData, yAxisName, fineness,
                plotNineId, cnvsNineId);
				//JB
        //
//        xOffset = xAxisParams[0];
//        yOffset = yAxisParams[4];
        var rangeXData9 = xAxisParams[1];
        var deltaXData9 = xAxisParams[2];
        var deltaXPxl9 = xAxisParams[3];
        var rangeYData9 = yAxisParams[1];
        var deltaYData9 = yAxisParams[2];
        var deltaYPxl9 = yAxisParams[3];
//        var xLowerYOffset = xAxisParams[5];
        var minXData9 = xAxisParams[6]; //updated value
        var minYData9 = yAxisParams[6]; //updated value
        var maxXData9 = xAxisParams[7]; //updated value
        var maxYData9 = yAxisParams[7]; //updated value     
        //
        titleX = panelX + titleOffsetX;
        titleY = panelY + titleOffsetY;
				//JB
        txtPrint("<span style='font-size:normal; color:blue'><a href='http://www.ap.smu.ca/~ishort/hrdtest3.html' target='_blank'>H-R Diagram</a></span>",
                titleOffsetX, titleOffsetY, 300, lineColor, plotNineId);
				//JB
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

//Lines of constant radius first
//
     var RGBHex = colHex(100, 100, 100);
     var dSizeCnvs = 2;
     var deltaLog10Teff = 0.2;  //Delta Log(Teff) in K
     var numRadLine = (maxXData9 - minXData9) / deltaLog10Teff;
     numRadLine = Math.abs(Math.ceil(numRadLine));
     //console.log(numRadLine);
     var log10TeffSun = logTen(teffSun);
     var thisLog10Teff, thisLog10TeffSol, log10L;

     var thisLog10Rad;
     var HRradii = [0.01, 0.1, 1.0, 10.0, 100.0, 1000.0]; //solar radii
     var numRad = HRradii.length;
     //console.log("numRad " + numRad);

     for (var r = 0; r < numRad; r++){
 
       thisLog10Rad = logTen(HRradii[r]); //solar units
//Seed first data point
        thisLog10Teff = minXData9; //K
        thisLog10TeffSol = thisLog10Teff - log10TeffSun; //solar units  
        var xTickPosCnvs = xAxisLength * (thisLog10Teff - minXData9) / rangeXData9; // pixels
        var lastXShiftCnvs = xAxisXCnvs + xTickPosCnvs;
        log10L = (4.0*thisLog10TeffSol) + (2.0*thisLog10Rad);
        var yTickPosCnvs = yAxisLength * (log10L - minYData9) / rangeYData9;
        var lastYShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;

    for (var i = 1; i < numRadLine; i++){
   //Caution: Teff axis backwards so minXData > maxXData:
        thisLog10Teff = minXData9 - (i * deltaLog10Teff); //K
        thisLog10TeffSol = thisLog10Teff - log10TeffSun; //solar units  
        log10L = (4.0*thisLog10TeffSol) + (2.0*thisLog10Rad);

        //console.log("thisLog10Teff " + thisLog10Teff + " log10L " + log10L);
 
          var xTickPosCnvs = xAxisLength * (thisLog10Teff - minXData9) / rangeXData9; // pixels   

          // horizontal position in pixels - data values increase rightward:
          var xShiftCnvs = xAxisXCnvs + xTickPosCnvs;

          var yTickPosCnvs = yAxisLength * (log10L - minYData9) / rangeYData9;
          // vertical position in pixels - data values increase upward:
          var yShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;

           var thisLine = document.createElementNS(xmlW3, 'line');
           thisLine.setAttributeNS(null, 'x1', lastXShiftCnvs);
           thisLine.setAttributeNS(null, 'x2', xShiftCnvs); 
           thisLine.setAttributeNS(null, 'y1', lastYShiftCnvs);
           thisLine.setAttributeNS(null, 'y2', yShiftCnvs); 
           thisLine.setAttributeNS(null, 'stroke', 'black');
           thisLine.setAttributeNS(null, 'stroke-width', 1);

           cnvsNineId.appendChild(thisLine);

           
            lastXShiftCnvs = xShiftCnvs;
            lastYShiftCnvs = yShiftCnvs;
  } //i loop over Teff
				//JB
          txtPrint("<span style='font-size:xx-small'>" + HRradii[r] + " R<sub>Sun</sub></span>",
                    xShiftCnvs+5, yShiftCnvs-5, 300, RGBHex, plotNineId);
				//JB
 } //r loop over radii
 

//Data loops - plot the result!

//MS stars

        var dSizeCnvs = 2.0; //plot point size
        var opac = 0.7; //opacity
        // RGB color
        var r255 = 0;
        var g255 = 0;
        var b255 = 0; 
        var RGBHex = colHex(r255, r255, r255);

        var ii;
        //for (var i = 5; i < msNum - 3; i++) {
        for (var i = 4; i < msNum - 1; i++) {

            ii = 1.0 * i;
            var xTickPosCnvs = xAxisLength * (logTen(msTeffs[i]) - minXData9) / rangeXData9; // pixels   

            // horizontal position in pixels - data values increase rightward:
            var xShiftCnvs = xAxisXCnvs + xTickPosCnvs;

            var yTickPosCnvs = yAxisLength * (msLogLum[i] - minYData9) / rangeYData9;
        //console.log("logTen(msTeffs[i] " + logTen(msTeffs[i]) + " msLogLum[i] " + msLogLum[i]);
            // vertical position in pixels - data values increase upward:
            var yShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;
				//JB
//plot MS stars
	    var dot = document.createElementNS(xmlW3,'circle');
	    dot.setAttributeNS(null, 'cx', xShiftCnvs);
            dot.setAttributeNS(null, 'cy', yShiftCnvs);
            dot.setAttributeNS(null, 'r', dSizeCnvs);
            dot.setAttributeNS(null, 'stroke', RGBHex);
            dot.setAttributeNS(null, 'fill', wDefaultColor);
            dot.setAttributeNS(null, 'id', "dot"+i);
	    //dot.setAttributeNS(xmlns,xmlnsLink,xmlnsLink2);
	    cnvsNineId.appendChild(dot);
				//JB

} // msNum loop, i


//RGB stars

// RGB color
        var r255 = 0;
        var g255 = 0;
        var b255 = 0; 
        var RGBHex = colHex(r255, r255, r255);

        var ii;
        //for (var i = 4; i < rgbNum - 2; i++) {
        for (var i = 3; i < rgbNum - 1; i++) {

            ii = 1.0 * i;
            var xTickPosCnvs = xAxisLength * (logTen(rgbTeffs[i]) - minXData9) / rangeXData9; // pixels   

            // horizontal position in pixels - data values increase rightward:
            var xShiftCnvs = xAxisXCnvs + xTickPosCnvs;

            var yTickPosCnvs = yAxisLength * (rgbLogLum[i] - minYData9) / rangeYData9;
            // vertical position in pixels - data values increase upward:
            var yShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;

				//JB
            var dot = document.createElementNS(xmlW3, 'circle');
            dot.setAttributeNS(null, 'cx', xShiftCnvs);
            dot.setAttributeNS(null, 'cy', yShiftCnvs);
            dot.setAttributeNS(null, 'r', dSizeCnvs);
            dot.setAttributeNS(null, 'stroke', RGBHex);
            dot.setAttributeNS(null, 'fill', wDefaultColor);
            dot.setAttributeNS(null, 'id', "dotTwo"+i);
            //dot.setAttributeNS(xmlns,xmlnsLink,xmlnsLink2);
            cnvsNineId.appendChild(dot);
                                //JB

 }  //rgbNum loop, i


// //SGB stars
// 
// // RGB color
 var r255 = 0;
 var g255 = 0;
 var b255 = 0; 
 var RGBHex = colHex(r255, r255, r255);
  
 var ii;
 for (var i = 4; i < sgbNum - 3; i++) {
  
  ii = 1.0 * i;
  var xTickPosCnvs = xAxisLength * (logTen(sgbTeffs[i]) - minXData9) / rangeXData9; // pixels   
  
  // horizontal position in pixels - data values increase rightward:
 var xShiftCnvs = xAxisXCnvs + xTickPosCnvs;
 
  var yTickPosCnvs = yAxisLength * (sgbLogLum[i] - minYData9) / rangeYData9;
 // vertical position in pixels - data values increase upward:
  var yShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;
				
				//JB
            var dot = document.createElementNS(xmlW3, 'circle');
            dot.setAttributeNS(null, 'cx', xShiftCnvs);
            dot.setAttributeNS(null, 'cy', yShiftCnvs);
            dot.setAttributeNS(null, 'r', dSizeCnvs);
            dot.setAttributeNS(null, 'stroke', RGBHex);
            dot.setAttributeNS(null, 'fill', wDefaultColor);
            dot.setAttributeNS(null, 'id', "dotThree"+i);
            //dot.setAttributeNS(xmlns,xmlnsLink,xmlnsLink2);
            cnvsNineId.appendChild(dot);

				//JB
  } //sgbNum loop, i

    //cnvsNineId.addEventListener("mouseover", function() { 
    cnvsNineId.addEventListener("click", function() {
       //dataCoords(event, plotNineId);
       var xyString = dataCoords(event, cnvsNineId, xAxisLength, minXData9, rangeXData9, xAxisXCnvs,
                               yAxisLength, minYData9, rangeYData9, yAxisYCnvs);
       txtPrint(xyString, titleOffsetX+200, titleOffsetY+320, 150, lineColor, plotNineId);
    });



// Now overplot our star:
        var xTickPosCnvs = xAxisLength * (logTen(teff) - minXData9) / rangeXData9; // pixels   
        // horizontal position in pixels - data values increase rightward:
        var xShiftCnvs = xAxisXCnvs + xTickPosCnvs;
//
        var yTickPosCnvs = yAxisLength * (logTen(bolLum) - minYData9) / rangeYData9;
        // vertical position in pixels - data values increase upward:
        var yShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;
        //Take color and radius from the last step of the star rendering loop (plot Seve) - that should be the inner-most disk:
        var radiusPxThis = saveRadius / 5;
        if (radiusPxThis < 1){
            radiusPxThis = 1;
            }
    
        var rrI = saveRGB[0];
        var ggI = saveRGB[1];
        var bbI = saveRGB[2];
        var RGBHex = colHex(rrI, ggI, bbI);

//
			//JB
            var x4 = logTen(teff);
            var y4 = logTen(bolLum);
//create a circle representing our star
	    var dot = document.createElementNS(xmlW3, 'circle');
	    dot.setAttributeNS(null, 'cx', xShiftCnvs);
            dot.setAttributeNS(null, 'cy', yShiftCnvs);
            dot.setAttributeNS(null, 'r', 1.1 * radiusPxThis);
            dot.setAttributeNS(null, 'stroke', "white");
            dot.setAttributeNS(null, 'fill', wDefaultColor);
            dot.setAttributeNS(null, 'opacity', 0.5);
            cnvsNineId.appendChild(dot);

	    //dot4.setAttributeNS(xmlns,xmlnsLink,xmlnsLink2);

//create another circle behind our star to make it more visable

	    var dot = document.createElementNS(xmlW3, 'circle');
            dot.setAttributeNS(null, 'cx', xShiftCnvs);
            dot.setAttributeNS(null, 'cy', yShiftCnvs);
            dot.setAttributeNS(null, 'r', 1.05 * radiusPxThis);
	    dot.setAttributeNS(null, 'stroke', RGBHex);
            dot.setAttributeNS(null, 'fill', RGBHex);
            dot.setAttributeNS(null, 'opacity', 0.5);
//event listeners added directly onto the one circle
	    //dot5.setAttributeNS(xmlns,xmlnsLink,xmlnsLink2);
            cnvsNineId.appendChild(dot);
	    

			//JB

        //Now overplot Luminosity class markers:

            //I
        var xShift = xAxisXCnvs + xAxisLength * (logTen(sgbTeffs[sgbNum-1]) - minXData9) / rangeXData9; // pixels 
        var yShift = (yAxisYCnvs + yAxisLength) - (yAxisLength * (sgbLogLum[sgbNum - 1] - minYData9) / rangeYData9);
				//JB
        txtPrint("<span style='font-size:normal'><a href='http://en.wikipedia.org/wiki/Stellar_classification' target='_blank'>\n\
I</a></span>", xShift, yShift, 300, lineColor, plotNineId);
				//JB
        //III
        xShift = xAxisXCnvs + xAxisLength * (logTen(rgbTeffs[rgbNum-1]) - minXData9) / rangeXData9; // pixels 
        yShift = (yAxisYCnvs + yAxisLength) - (yAxisLength * (rgbLogLum[rgbNum - 8] - minYData9) / rangeYData9);
				//JB
        txtPrint("<span style='font-size:normal'><a href='http://en.wikipedia.org/wiki/Stellar_classification' title='Giants' target='_blank'>\n\
     III</a></span>", xShift, yShift, 300, lineColor, plotNineId);
				//JB
        //V
        xShift = xAxisXCnvs + xAxisLength * (logTen(msTeffs[msNum-1]) - minXData9) / rangeXData9; // pixels 
        yShift = (yAxisYCnvs + yAxisLength) - (yAxisLength * (msLogLum[msNum - 8] - minYData9) / rangeYData9);
				//JB
        txtPrint("<span style='font-size:normal'><a href='http://en.wikipedia.org/wiki/Stellar_classification' title='Main Sequence, Dwarfs' target='_blank'>\n\
     V</a></span>", xShift, yShift, 300, lineColor, plotNineId);


					
/*
// ****************************************
    //
    //
    //  *****   PLOT ONE / PLOT 1
    //

    // Plot one: log(Tau) vs log(rho)
    // 
    if ((ifLineOnly === false) && (ifShowAtmos === true)) {
//
  // console.log("PLOT ONE");
        var plotRow = 3;
        var plotCol = 2;
        var minXData = logE * tauRos[1][0] - 0.0;
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
        var panelOrigin = washer(plotRow, plotCol, wColor, plotOneId, cnvsOneId);
        panelX = panelOrigin[0];
        panelY = panelOrigin[1];
        cnvsOneCtx.fillStyle = wColor;
        cnvsOneCtx.fillRect(0, 0, panelWidth, panelHeight);
        var xAxisParams = XAxis(panelX, panelY,
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
            var lastXShiftCnvs = xAxisXCnvs + xTickPosCnvs;
            var yTickPosCnvs = yAxisLength * (logE * rho[1][0] - minYData) / rangeYData;
            // vertical position in pixels - data values increase upward:
            var lastYShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;

        for (var i = 2; i < numDeps; i++) {

            ii = 1.0 * i;
            var xTickPosCnvs = xAxisLength * (logE * tauRos[1][i] - minXData) / rangeXData; // pixels   

            // horizontal position in pixels - data values increase rightward:
            var xShiftCnvs = xAxisXCnvs + xTickPosCnvs;

            var yTickPosCnvs = yAxisLength * (logE * rho[1][i] - minYData) / rangeYData;
            // vertical position in pixels - data values increase upward:
            var yShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;

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

    //cnvsOneId.addEventListener("mouseover", function() { 
    cnvsOneId.addEventListener("click", function() {
       //dataCoords(event, plotOneId);
       dataCoords(event, p1Id, xAxisLength, minXData, rangeXData, xAxisXCnvs,
                               yAxisLength, minYData, rangeYData, yAxisYCnvs);
       txtPrint(xyString, titleOffsetX+120, titleOffsetY+300, 150, lineColor, plotTenId);
    });


// Tau=1 cross-hair

        var barWidth = 1.0;
        var barColor = "#777777";
        var xShift = YBar(logE * tauRos[1][tTau1], minXData, maxXData, barWidth, yAxisLength,
                yFinesse, barColor, plotOneId, cnvsOneCtx);

        var barHeight = 1.0;
        var yShift = XBar(logE * rho[1][tTau1], minYData, maxYData, xAxisLength, barHeight,
                xFinesse, barColor, plotOneId, cnvsOneCtx);
        txtPrint("<span style='font-size:small; color:#444444'><em>&#964</em><sub>Ros</sub>=1</span>",
                xShift, yShift, lineColor, plotOneId);
    }
*/

    //
    //
    //  *****   PLOT THREE / PLOT 3
    //
    //
    // Plot three: log(Tau) vs log(Pressure)

    if (ifShowAtmos === true) {

        var plotRow = 4;
        var plotCol = 1;
        var minXData = logE * tauRos[1][0];
        var maxXData = logE * tauRos[1][numDeps - 1];
        var xAxisName = "<span title='Rosseland mean optical depth'><a href='http://en.wikipedia.org/wiki/Optical_depth_%28astrophysics%29' target='_blank'>Log<sub>10</sub> <em>&#964</em><sub>Ros</sub></a></span>";
        // From Hydrostat.hydrostat:
        //pGas is a 2 x numDeps array:
        // rows 0 & 1 are linear and log *gas* pressure, respectively
        // Don't use upper boundary condition as lower y-limit - use a couple of points below surface:
        //var numYTicks = 6;
        // Build total P from P_Gas & P_Rad:
        var logPTot = [];
        logPTot.length = numDeps;
        for (var i = 0; i < numDeps; i++) {
            logPTot[i] = Math.log(pGas[0][i] + pRad[0][i]);
            //console.log(" i " + i + " logPTot[i] " + logPTot[i] + " pGas[0][i] " + pGas[0][i] + " pRad[0][i] " + pRad[0][i]);
        }
        //var minYData = logE * logPTot[0] - 2.0; // Avoid upper boundary condition [i]=0
        var minYData = logE * Math.min(pGas[1][0], pRad[1][0], Pe[1][0]) - 1.0;
        var maxYData = logE * logPTot[numDeps - 1];
        var yAxisName = "Log<sub>10</sub> <em>P</em> <br />(dynes <br />cm<sup>-2</sup>)";
        //console.log("minYData " + minYData + " maxYData " + maxYData);
        //washer(xRange, xOffset, yRange, yOffset, wDefaultColor, plotThreeId);

        var fineness = "normal";
        //var cnvsCtx = washer(plotRow, plotCol, wDefaultColor, plotThreeId, cnvsId);
				//JB
        var panelOrigin = washer(plotRow, plotCol, panelWidth, wDefaultColor, plotThreeId, cnvsThreeId);
				//JB
        panelX = panelOrigin[0];
        panelY = panelOrigin[1];
	cnvsThreeId.setAttribute('fill', wDefaultColor);
        var xAxisParams = XAxis(panelX, panelY, xAxisLength,
                minXData, maxXData, xAxisName, fineness,
                plotThreeId, cnvsThreeId);
        var yAxisParams = YAxis(panelX, panelY,
                minYData, maxYData, yAxisName,
                fineness, plotThreeId, cnvsThreeId);

        //xOffset = xAxisParams[0];
        //yOffset = xAxisParams[4];
        var rangeXData3 = xAxisParams[1];
        var deltaXData3 = xAxisParams[2];
        var deltaXPxl3 = xAxisParams[3];
        var rangeYData3 = yAxisParams[1];
        var deltaYData3 = yAxisParams[2];
        var deltaYPxl3 = yAxisParams[3];
        var xLowerYOffset3 = xAxisParams[5];
        var minXData3 = xAxisParams[6]; //updated value
        var minYData3 = yAxisParams[6]; //updated value
        var maxXData3 = xAxisParams[7]; //updated value
        var maxYData3 = yAxisParams[7]; //updated value        
        yFinesse = 0;       
        xFinesse = 0;       
        //
        titleX = panelX + titleOffsetX;
        titleY = panelY + titleOffsetY;
					//JB
        txtPrint("log Pressure: <span style='color:blue' title='Total pressure'><strong><em>P</em><sub>Tot</sub></strong></span> "
                + " <a href='http://en.wikipedia.org/wiki/Gas_laws' target='_blank'><span style='color:#00FF88' title='Gas pressure'><em>P</em><sub>Gas</sub></span></a> "
                + " <a href='http://en.wikipedia.org/wiki/Radiation_pressure' target='_blank'><span style='color:red' title='Radiation pressure'><em>P</em><sub>Rad</sub></span></a> " +
                  " <span style='color:black' title='Partial electron pressure'><em>P</em><sub>e</sub></span>",
                titleOffsetX, titleOffsetY, 300, lineColor, plotThreeId);

					//JB
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

//initializations:
        var ii;
        var xTickPosCnvs = xAxisLength * (logE * tauRos[1][0] - minXData3) / rangeXData3; // pixels   
        var yTickPosCnvs = yAxisLength * (logE * pGas[1][0] - minYData3) / rangeYData3; // pixels   
        var yTickPosGCnvs = yAxisLength * (logE * pGas[1][0] - minYData3) / rangeYData3; // pixels   
        var yTickPosBCnvs = yAxisLength * (logE * Pe[1][0] - minYData3) / rangeYData3; // pixels   
        var yTickPosRCnvs = yAxisLength * (logE * pRad[1][0] - minYData3) / rangeYData3; // pixels   

        // horizontal position in pixels - data values increase rightward:
         var lastXShiftCnvs = xAxisXCnvs + xTickPosCnvs;

         var lastYTickPosCnvs = yAxisLength * (logE * logPTot[0] - minYData3) / rangeYData3;
         var lastYTickPosGCnvs = yAxisLength * (logE * pGas[1][0] - minYData3) / rangeYData3;
         var lastYTickPosRCnvs = yAxisLength * (logE * pRad[1][0] - minYData3) / rangeYData3;
         var lastYTickPosBCnvs = yAxisLength * (logE * Pe[1][0] - minYData3) / rangeYData3;
         // vertical position in pixels - data values increase upward:
         var lastYShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;
         var lastYShiftGCnvs =(yAxisYCnvs + yAxisLength) - yTickPosGCnvs;
         var lastYShiftRCnvs = (yAxisYCnvs + yAxisLength) - yTickPosRCnvs;
         var lastYShiftBCnvs = (yAxisYCnvs + yAxisLength) - yTickPosBCnvs;

        // Avoid upper boundary at i=0
        for (var i = 1; i < numDeps; i++) {

            ii = 1.0 * i;
            var xTickPosCnvs = xAxisLength * (logE * tauRos[1][i] - minXData3) / rangeXData3; // pixels   

            // horizontal position in pixels - data values increase rightward:
            var xShiftCnvs = xAxisXCnvs + xTickPosCnvs;

            var yTickPosCnvs = yAxisLength * (logE * logPTot[i] - minYData3) / rangeYData3;
	    var yTickPosGCnvs = yAxisLength * (logE * pGas[1][i] - minYData3) / rangeYData3;
            var yTickPosRCnvs = yAxisLength * (logE * pRad[1][i] - minYData3) / rangeYData3;
            var yTickPosBCnvs = yAxisLength * (logE * Pe[1][i] - minYData3) / rangeYData3;
            // vertical position in pixels - data values increase upward:
            var yShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;
            var yShiftGCnvs = (yAxisYCnvs + yAxisLength) - yTickPosGCnvs;
            var yShiftRCnvs = (yAxisYCnvs + yAxisLength) - yTickPosRCnvs;
            var yShiftBCnvs = (yAxisYCnvs + yAxisLength) - yTickPosBCnvs;

		
            		
            //console.log("lastXShiftCnvs " + lastXShiftCnvs + " lastYShiftCnvs " + lastYShiftGCnvs + " xShiftCnvs " + xShiftCnvs + " yShiftCnvs " + yShiftGCnvs);

            var thisLine = document.createElementNS(xmlW3, 'line');
            thisLine.setAttributeNS(null, 'x1', lastXShiftCnvs);
            thisLine.setAttributeNS(null, 'x2', xShiftCnvs);
            thisLine.setAttributeNS(null, 'y1', lastYShiftCnvs);
            thisLine.setAttributeNS(null, 'y2', yShiftCnvs);
            thisLine.setAttributeNS(null, 'stroke', "#0000FF");
            thisLine.setAttributeNS(null, 'stroke-width', 2);
            cnvsThreeId.appendChild(thisLine);

            var thisLine = document.createElementNS(xmlW3, 'line');
            thisLine.setAttributeNS(null, 'x1', lastXShiftCnvs);
            thisLine.setAttributeNS(null, 'x2', xShiftCnvs);
            thisLine.setAttributeNS(null, 'y1', lastYShiftGCnvs);
            thisLine.setAttributeNS(null, 'y2', yShiftGCnvs);
            thisLine.setAttributeNS(null, 'stroke', "#00FF00");
            thisLine.setAttributeNS(null, 'stroke-width', 2);
            cnvsThreeId.appendChild(thisLine);

            var thisLine = document.createElementNS(xmlW3, 'line');
            thisLine.setAttributeNS(null, 'x1', lastXShiftCnvs);
            thisLine.setAttributeNS(null, 'x2', xShiftCnvs);
            thisLine.setAttributeNS(null, 'y1', lastYShiftRCnvs);
            thisLine.setAttributeNS(null, 'y2', yShiftRCnvs);
            thisLine.setAttributeNS(null, 'stroke', "#FF0000");
            thisLine.setAttributeNS(null, 'stroke-width', 2);
            cnvsThreeId.appendChild(thisLine);

            var thisLine = document.createElementNS(xmlW3, 'line');
            thisLine.setAttributeNS(null, 'x1', lastXShiftCnvs);
            thisLine.setAttributeNS(null, 'x2', xShiftCnvs);
            thisLine.setAttributeNS(null, 'y1', lastYShiftBCnvs);
            thisLine.setAttributeNS(null, 'y2', yShiftBCnvs);
            thisLine.setAttributeNS(null, 'stroke', "#000000");
            thisLine.setAttributeNS(null, 'stroke-width', 2);
            cnvsThreeId.appendChild(thisLine);
           
  
				//JB
            lastXShiftCnvs = xShiftCnvs;
            lastYShiftCnvs = yShiftCnvs;
            lastYShiftGCnvs = yShiftGCnvs;
            lastYShiftRCnvs = yShiftRCnvs;
            lastYShiftBCnvs = yShiftBCnvs;
        }

    //cnvsThreeId.addEventListener("mouseover", function() { 
    cnvsThreeId.addEventListener("click", function() {
       //dataCoords(event, plotThreeId);
       var xyString = dataCoords(event, cnvsThreeId, xAxisLength, minXData3, rangeXData3, xAxisXCnvs,
                               yAxisLength, minYData3, rangeYData3, yAxisYCnvs);
       txtPrint(xyString, titleOffsetX+200, titleOffsetY+320, 150, lineColor, plotThreeId);
    }); 


// Tau=1 cross-hair

        var tTau1 = tauPoint(numDeps, tauRos, 1.0);
        var barWidth = 1.0;
        var barColor = "#777777";
        yFinesse = 0.0;
				//JB
        xShift = YBar(logE * tauRos[1][tTau1], minXData3, maxXData3, xAxisLength, barWidth, yAxisLength,
                yFinesse, barColor, plotThreeId, cnvsThreeId);
        barHeight = 1.0;
        yShift = XBar(logE * logPTot[tTau1], minYData3, maxYData3, xAxisLength, barHeight,
                xFinesse, barColor, plotThreeId, cnvsThreeId);
        txtPrint("<span style='font-size:small; color:#444444'><em>&#964</em><sub>Ros</sub>=1</span>",
                xShift, yShift, 300, lineColor, plotThreeId);

				//JB
    }

    //
    //
    //  *****   PLOT FOUR / PLOT 4
    //
    //
    // Plot four: Limb darkening


//ifShowRad = false; //For movie 
   if (ifShowRad === true) {

        var plotRow = 3;
        var plotCol = 0;
        // For movie:
        //var plotRow = 3;
        //var plotCol = 1;
//
        var minXData = 180.0 * Math.acos(cosTheta[1][0]) / Math.PI;
        var maxXData = 180.0 * Math.acos(cosTheta[1][numThetas - 1]) / Math.PI;
        var xAxisName = "<em>&#952</em> (<sup>o</sup>)";
        var minYData = 0.0;
        var maxYData = 1.0;
        //var maxYData = tuneBandIntens[0] / norm;
        var yAxisName = "<span title='Monochromatic surface specific intensity'><a href='http://en.wikipedia.org/wiki/Specific_radiative_intensity' target='_blank'><em>I</em><sub>&#955</sub>(<em>&#952</em>)/<br /><em>I</em><sub>&#955</sub>(0)</a></span>";

        var fineness = "normal";
//
				//JB
        var panelOrigin = washer(plotRow, plotCol, panelWidth, wDefaultColor, plotFourId, cnvsFourId);
				//JB
        panelX = panelOrigin[0];
        panelY = panelOrigin[1];
				//JB
	cnvsFourId.setAttribute('fill', wDefaultColor);
 var xAxisParams = XAxis(panelX, panelY, xAxisLength,
                minXData, maxXData, xAxisName, fineness,
                plotFourId, cnvsFourId);
        var yAxisParams = YAxis(panelX, panelY,
                minYData, maxYData, yAxisName,
                fineness,plotFourId, cnvsFourId);
				//JB
        var rangeXData4 = xAxisParams[1];
        var deltaXData4 = xAxisParams[2];
        var deltaXPxl4 = xAxisParams[3];
        var rangeYData4 = yAxisParams[1];
        var deltaYData4 = yAxisParams[2];
        var deltaYPxl4 = yAxisParams[3];
        var minXData4 = xAxisParams[6]; //updated value
        var minYData4 = yAxisParams[6]; //updated value
        var maxXData4 = xAxisParams[7]; //updated value
        var maxYData4 = yAxisParams[7]; //updated value        
        //
        // Add legend annotation:

        //var iLamMinMax = minMax2(masterFlux);
        //var iLamMax = iLamMinMax[1];
        //var lamMax = (1.0e7 * masterLams[iLamMax]).toPrecision(3);

//
        lineColor = "#000000";
        var diskLamLbl = diskLambda.toPrecision(3);
        var diskLamStr = diskLamLbl.toString(10);
        titleX = panelX + titleOffsetX;
        titleY = panelY + titleOffsetY;
					//JB
        txtPrint("<span style='font-size:small'><span style='color:#000000'><em>&#955</em><sub>Filter</sub> = " + diskLamStr + "nm</span><br /> ",
                xAxisXCnvs+10, titleOffsetY+20, 300, lineColor, plotFourId);
        // Add title annotation:
                 txtPrint("<span style='font-size:normal; color:blue'><a href='http://en.wikipedia.org/wiki/Limb_darkening' target='_blank'>Limb darkening </a></span>",
        titleOffsetX, titleOffsetY, 300, lineColor, plotFourId);
					//JB
        //Data loop - plot the result!

        var dSizeCnvs = 4.0; //plot point size
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

        var xTickPosCnvs = xAxisLength * (180.0 * Math.acos(cosTheta[1][0]) / Math.PI - minXData4) / rangeXData4; // pixels   
        // horizontal position in pixels - data values increase rightward:
        var lastXShiftCnvs = xAxisXCnvs + xTickPosCnvs;
        var yTickPosCnvs = yAxisLength * ((tuneBandIntens[0] / tuneBandIntens[0]) - minYData4) / rangeYData4;

        // vertical position in pixels - data values increase upward:
        var lastYShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;

//


//variables required to create an array of colors based on the gaussian filter
var ilLam0 = lamPoint(numMaster,masterLams,1.0e-7 * diskLambda);
var lambdanm = masterLams[ilLam0]*1.0e7;
var minZData = 0.0;
var maxZData = tuneBandIntens[0]/norm;
var rangeZData = maxZData - minZData;
//console.log(diskLambda);
                              

//
        for (var i = 1; i < numThetas; i++) {

//other variables required to create an array of colors based
// on the gaussian filter
//
var zLevel = ((tuneBandIntens[i]/norm)-minZData)/rangeZData;

var RGBHex = lambdaToRGB(lambdanm,zLevel);
//console.log (RGBHex);


            xTickPosCnvs = xAxisLength * (180.0 * Math.acos(cosTheta[1][i]) / Math.PI - minXData4) / rangeXData4; // pixels   
            // horizontal position in pixels - data values increase rightward:
            var xShiftCnvs = xAxisXCnvs + xTickPosCnvs;

            yTickPosCnvs = yAxisLength * ((tuneBandIntens[i] / tuneBandIntens[0]) - minYData4) / rangeYData4;

            // vertical position in pixels - data values increase upward:
            var yShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;

//Plot points
			//JB
            //RGBHex = colHex(0, 0, 0);
	    var circle = document.createElementNS(xmlW3, 'circle');
	    circle.setAttributeNS(null, 'cx', xShiftCnvs);
            circle.setAttributeNS(null, 'cy', yShiftCnvs);
            circle.setAttributeNS(null, 'r', dSizeCnvs);
            circle.setAttributeNS(null, 'stroke', RGBHex);
            circle.setAttributeNS(null, 'fill', RGBHex);
            cnvsFourId.appendChild(circle);
			//JB
//line plot
			//JB
	    var line = document.createElementNS(xmlW3, 'line');
            line.setAttributeNS(null, 'x1', lastXShiftCnvs);
            line.setAttributeNS(null, 'x2', xShiftCnvs);
            line.setAttributeNS(null, 'y1', lastYShiftCnvs);
            line.setAttributeNS(null, 'y2', yShiftCnvs);
            line.setAttributeNS(null, 'stroke', 'black');
            line.setAttributeNS(null, 'stroke-width', 2);
	    cnvsFourId.appendChild(line);
			//JB
            lastXShiftCnvs = xShiftCnvs;
            lastYShiftCnvs = yShiftCnvs;
        }

    //cnvsFourId.addEventListener("mouseover", function() { 
    cnvsFourId.addEventListener("click", function() {
       //dataCoords(event, plotFourId);
       var xyString = dataCoords(event, cnvsFourId, xAxisLength, minXData4, rangeXData4, xAxisXCnvs,
                               yAxisLength, minYData4, rangeYData4, yAxisYCnvs);
       txtPrint(xyString, titleOffsetX+200, titleOffsetY+320, 150, lineColor, plotFourId);
    });


    }
//ifShowRad = true; //For movie 

//
//
//  *****   PLOT FIVE / PLOT 5
//
//

// Plot five: SED
// 
    //if (ifShowRad === true) { 
//    //For movie:

        var plotRow = 1;
        var plotCol = 2;
        ////For movie:
        //var plotRow = 1;
        //var plotCol = 1;
//
       //console.log("masterLams[0] " + masterLams[0] + " [1] " + masterLams[1]);
        //var minXData = 1.0e7 * masterLams[0];
        //var maxXData = 1.0e7 * masterLams[numMaster - 1];
        //var xAxisName = "<em>&#955</em> (nm)";
            ////Logarithmic x:
        var minXData = 7.0 + logTen(masterLams[0]);
        var maxXData = 7.0 + logTen(masterLams[numMaster - 1]);
        //var maxXData = 3.0; //finesse - Log10(lambda) = 3.5 nm
        var xAxisName = "Log<sub>10</sub> &#955 (nm)";
        //var numYTicks = 4;
        //now done above var norm = 1.0e15; // y-axis normalization
        //var minYData = 0.0;
        //// iLamMax established in PLOT TWO above:
        //var maxYData = masterFlux[0][iLamMax] / norm;
        //var yAxisName = "<span title='Monochromatic surface flux'><a href='http://en.wikipedia.org/wiki/Spectral_flux_density' target='_blank'> <em>F</em><sub>&#955</sub> x 10<sup>15</sup><br />ergs s<sup>-1</sup> <br />cm<sup>-3</sup></a></span>";
        //Logarithmic y:
        var minYData = logE * masterFlux[1][iLamMin];
        var maxYData = logE * masterFlux[1][iLamMax];
        var yAxisName = "<span title='Monochromatic surface flux'><a href='http://en.wikipedia.org/wiki/Spectral_flux_density' target='_blank'>Log<sub>10</sub> <em>F</em><sub>&#955</sub> <br /> ergs s<sup>-1</sup> cm<sup>-3</sup></a></span>";
        //(xRange, xOffset, yRange, yOffset, wDefaultColor, plotFiveId);

        //var fineness = "ultrafine";
        //var cnvsCtx = washer(plotRow, plotCol, wDefaultColor, plotFiveId, cnvsId);

					//JB
        var panelOrigin = washer(plotRow, plotCol, panelWidth, wDefaultColor, plotFiveId, cnvsFiveId);
					//JB

        panelX = panelOrigin[0];
        panelY = panelOrigin[1];


		//console.log(SVGFive); is good, created fine
		//console.log("Before: minXData, maxXData " + minXData + ", " + maxXData);
                //console.log("XAxis called from PLOT 5:");
        var xAxisParams = XAxis(panelX, panelY, xAxisLength,
                minXData, maxXData, xAxisName, fineness,
                plotFiveId, cnvsFiveId);

        var yAxisParams = YAxis(panelX, panelY,
                 minYData, maxYData, yAxisName, 
                 fineness, plotFiveId, cnvsFiveId);   

        //xOffset = xAxisParams[0];
        //yOffset = xAxisParams[4];
        var rangeXData5 = xAxisParams[1];
        var deltaXData5 = xAxisParams[2];
        var deltaXPxl5 = xAxisParams[3];
        var rangeYData5 = yAxisParams[1];
        var deltaYData5 = yAxisParams[2];
        var deltaYPxl5 = yAxisParams[3];
        var minXData5 = xAxisParams[6]; //updated value
        var minYData5 = yAxisParams[6]; //updated value
        var maxXData5 = xAxisParams[7]; //updated value
        var maxYData5 = yAxisParams[7]; //updated value        
		//console.log("After : minXData, maxXData " + minXData + ", " + maxXData + " rangeXData " + rangeXData);
        //console.log("2:  minXData5 " + minXData5);
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
					//JB
        txtPrint("<span style='font-size:normal; color:blue'><a href='http://en.wikipedia.org/wiki/Spectral_energy_distribution' target='_blank'>\n\
     Spectral energy distribution (SED)</a></span>",
                titleOffsetX, titleOffsetY, 300, lineColor, plotFiveId);
        txtPrint("<span style='font-size:small'>"
                + "<span><em>F</em><sub>&#955</sub> (<em>&#955</em><sub>Max</sub> = " + lamMaxStr + " nm)</span>, "
                + " <span><em>I</em><sub>&#955</sub>,</span> <span style='color:#444444'> <em>&#952</em> = " + thet0Str + "<sup>o</sup></span>,  "
                + " <span style='color:#444444'><em>&#952</em> = " + thetNStr + "<sup>o</sup></span></span>",
                titleOffsetX, titleOffsetY+35, 250, lineColor, plotFiveId);
					//JB
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

					//JB
        xShift = YBar(band0, minXData5, maxXData5, yAxisLength, 
                  vBarWidth, yAxisLength,
                yFinesse, RGBHex, plotFiveId, cnvsFiveId);
        }; //end function UBVRIbands
					

//
        //
        var filters = filterSet();
        var lam0_ptr = 11; // approximate band centre
        var numBands = filters.length;
        var lamUBVRI = [];
        lamUBVRI.length = numBands;

        
        for (var ib = 0; ib < numBands; ib++) {
            //lamUBVRI[ib] = 1.0e7 * filters[ib][0][lam0_ptr]; //linear lambda
            lamUBVRI[ib] = 7.0 + logTen(filters[ib][0][lam0_ptr]);  //logarithmic lambda
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
	//J: (lburns)
	var r255 = 178;
	var g255 = 34;
	var b255 = 34; // firebrick 
	UBVRIBands(r255, g255, b255, lamUBVRI[6]);
	//H: (lburns)
	var r255 = 128;
	var g255 = 0;
	var b255 = 0; // maroon
	UBVRIBands(r255, g255, b255, lamUBVRI[7]);
	//K: (lburns)
	var r255 = 160;
	var g255 = 82;
	var b255 = 45; // sienna
	UBVRIBands(r255, g255, b255, lamUBVRI[8]);
        //Data loop - plot the result!

//Continuum spectrum - For testing: 
//        var contFlux3 = interpolV(contFlux[0], lambdaScale, masterLams);

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

//linear x:
        var yShiftCnvs, yShiftCCnvs, yShift0Cnvs, yShiftNCnvs;
        //var logLambdanm = 7.0 + logTen(masterLams[0]);  //logarithmic
        var lambdanm = 1.0e7 * masterLams[0];
        var xTickPosCnvs = xAxisLength * (lambdanm - minXData5) / rangeXData5; // pixels
//Logarithmic x:
        var logLambdanm = 7.0 + logTen(masterLams[0]);  //logarithmic
        var xTickPosCnvs = xAxisLength * (logLambdanm - minXData5) / rangeXData5; // pixels
//
        var lastXShiftCnvs = xAxisXCnvs + xTickPosCnvs;
//linear y:
        var yTickPosCnvs = yAxisLength * ((masterFlux[0][0] / norm) - minYData5) / rangeYData5;
        //var yTickPosCCnvs = yAxisLength * ((contFlux3[0] / norm) - minYData) / rangeYData;
        var yTickPos0Cnvs = yAxisLength * ((masterIntens[0][0] / norm) - minYData5) / rangeYData5;
        var yTickPosNCnvs = yAxisLength * ((masterIntens[0][numThetas - 2] / norm) - minYData5) / rangeYData5;
//Logarithmic y:
        var yTickPosCnvs = yAxisLength * ((logE*masterFlux[1][0]) - minYData5) / rangeYData5;
        //var yTickPosCCnvs = yAxisLength * ((contFlux3[0] / norm) - minYData) / rangeYData;
        var yTickPos0Cnvs = yAxisLength * ((logE*masterIntens[1][0]) - minYData5) / rangeYData5;
        var yTickPosNCnvs = yAxisLength * ((logE*masterIntens[1][numThetas - 2]) - minYData5) / rangeYData5;
//
        // vertical position in pixels - data values increase upward:
        var lastYShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;
        //var lastYShiftCCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCCnvs;
        var lastYShift0Cnvs = (yAxisYCnvs + yAxisLength) - yTickPos0Cnvs;
        var lastYShiftNCnvs = (yAxisYCnvs + yAxisLength) - yTickPosNCnvs;
        var xShift, yShift;

    //cnvsFiveId.addEventListener("mouseover", function() { 
    cnvsFiveId.addEventListener("click", function() {
       //dataCoords(event, plotFiveId);
       var xyString = dataCoords(event, cnvsFiveId, xAxisLength, minXData5, rangeXData5, xAxisXCnvs,
                               yAxisLength, minYData5, rangeYData5, yAxisYCnvs);
       txtPrint(xyString, titleOffsetX+200, titleOffsetY+320, 150, lineColor, plotFiveId);
    });


        for (var i = 1; i < numMaster; i++) {

            //lambdanm = masterLams[i] * 1.0e7; //cm to nm //linear
            //xTickPosCnvs = xAxisLength * (lambdanm - minXData5) / rangeXData5; // pixels   //linear
            logLambdanm = 7.0 + logTen(masterLams[i]);  //logarithmic
            xTickPosCnvs = xAxisLength * (logLambdanm - minXData5) / rangeXData5; // pixels   //logarithmic
            ii = 1.0 * i;

            // horizontal position in pixels - data values increase rightward:
            xShiftCnvs = xAxisXCnvs + xTickPosCnvs;

//linear y:
            yTickPosCnvs = yAxisLength * ((masterFlux[0][i] / norm) - minYData5) / rangeYData5;
            //yTickPosCCnvs = yAxisLength * ((contFlux3[i] / norm) - minYData) / rangeYData;
            yTickPos0Cnvs = yAxisLength * ((masterIntens[i][0] / norm) - minYData5) / rangeYData5;
            yTickPosNCnvs = yAxisLength * ((masterIntens[i][numThetas - 2] / norm) - minYData5) / rangeYData5;
//logarithmic y:
            yTickPosCnvs = yAxisLength * ((logE*masterFlux[1][i]) - minYData5) / rangeYData5;
            //yTickPosCCnvs = yAxisLength * ((contFlux3[i] / norm) - minYData) / rangeYData;
            yTickPos0Cnvs = yAxisLength * ((logE*Math.log(masterIntens[i][0])) - minYData5) / rangeYData5;
            yTickPosNCnvs = yAxisLength * ((logE*Math.log(masterIntens[i][numThetas - 2])) - minYData5) / rangeYData5;
            // vertical position in pixels - data values increase upward:
            yShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;
            //yShiftCCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCCnvs;
            yShift0Cnvs = (yAxisYCnvs + yAxisLength) - yTickPos0Cnvs;
            yShiftNCnvs = (yAxisYCnvs + yAxisLength) - yTickPosNCnvs;

//line plot
            var RGBHex = colHex(r255, g255, b255);

            var thisLine = document.createElementNS(xmlW3, 'line');
            thisLine.setAttributeNS(null, 'x1', lastXShiftCnvs);
            thisLine.setAttributeNS(null, 'x2', xShiftCnvs);
            thisLine.setAttributeNS(null, 'y1', lastYShiftCnvs);
            thisLine.setAttributeNS(null, 'y2', yShiftCnvs);
            thisLine.setAttributeNS(null, 'stroke', RGBHex);
            thisLine.setAttributeNS(null, 'stroke-width', 2);
            cnvsFiveId.appendChild(thisLine);

            var RGBHex = colHex(r2550, g2550, b2550);

            var thisLine = document.createElementNS(xmlW3, 'line');
            thisLine.setAttributeNS(null, 'x1', lastXShiftCnvs);
            thisLine.setAttributeNS(null, 'x2', xShiftCnvs);
            thisLine.setAttributeNS(null, 'y1', lastYShift0Cnvs);
            thisLine.setAttributeNS(null, 'y2', yShift0Cnvs);
            thisLine.setAttributeNS(null, 'stroke', RGBHex);
            thisLine.setAttributeNS(null, 'stroke-width', 2);
            cnvsFiveId.appendChild(thisLine);

            var RGBHex = colHex(r255N, g255N, b255N);

            var thisLine = document.createElementNS(xmlW3, 'line');
            thisLine.setAttributeNS(null, 'x1', lastXShiftCnvs);
            thisLine.setAttributeNS(null, 'x2', xShiftCnvs);
            thisLine.setAttributeNS(null, 'y1', lastYShiftNCnvs);
            thisLine.setAttributeNS(null, 'y2', yShiftNCnvs);
            thisLine.setAttributeNS(null, 'stroke', RGBHex);
            thisLine.setAttributeNS(null, 'stroke-width', 2);


            lastXShiftCnvs = xShiftCnvs;
            lastYShiftCnvs = yShiftCnvs;
            //lastYShiftCCnvs = yShiftCCnvs;
            lastYShift0Cnvs = yShift0Cnvs;
            lastYShiftNCnvs = yShiftNCnvs;

        }


        //monochromatic disk lambda
           yFinesse = 0.0;
           barHeight = 200;
           barWidth = 2;
           RGBHex = "#000000";
		//JB

   //linear wavelength
          //var xShiftDum = YBar(diskLambda, minXData5, maxXData5, barWidth, barHeight,
          //      yFinesse, RGBHex, plotFiveId, cnvsFiveId);
   //logarithmic wavelength
          var logDiskLambda = logTen(diskLambda);
          var xShiftDum = YBar(logDiskLambda, minXData5, maxXData5, xAxisLength, barWidth, barHeight,
                yFinesse, RGBHex, plotFiveId, cnvsFiveId);

        txtPrint("<span style='font-size:xx-small'>Filter</span>",
                xShiftDum, titleOffsetY+60, 100, lineColor, plotFiveId);

					//JB    
//} 


//
//
//  *****   PLOT TWO / PLOT 2
//
//

// Plot two: log(Tau) vs Temp
// 
    if (ifShowAtmos === true) {

        var plotRow = 4;
        var plotCol = 0;

        var minXData = logE * tauRos[1][0];
        var maxXData = logE * tauRos[1][numDeps - 1];
        var xAxisName = "<span title='Rosseland mean optical depth'><a href='http://en.wikipedia.org/wiki/Optical_depth_%28astrophysics%29' target='_blank'>Log<sub>10</sub> <em>&#964</em><sub>Ros</sub></a></span>";
        var minYData = temp[0][0];
        var maxYData = temp[0][numDeps - 1];
        var yAxisName = "<em>T</em><sub>Kin</sub> (K)";
        var fineness = "normal";
					//JB
        var panelOrigin = washer(plotRow, plotCol, panelWidth, wDefaultColor, plotTwoId, cnvsTwoId);
					//JB
        panelX = panelOrigin[0];
        panelY = panelOrigin[1];
					//JB
	cnvsTwoId.setAttribute('fill', wDefaultColor);
        var xAxisParams = XAxis(panelX, panelY, xAxisLength,
                minXData, maxXData, xAxisName, fineness,
                plotTwoId, cnvsTwoId);
            var cnvsCtx = xAxisParams[8];
            var yAxisParams = YAxis(panelX, panelY, minYData, maxYData, yAxisName,fineness, plotTwoId, cnvsTwoId);
                                                
					//JB

        //
        //xOffset = xAxisParams[0];
        //yOffset = xAxisParams[4];
        var rangeXData2 = xAxisParams[1];
        var deltaXData2 = xAxisParams[2];
        var deltaXPxl2 = xAxisParams[3];
        var rangeYData2 = yAxisParams[1];
        var deltaYData2 = yAxisParams[2];
        var deltaYPxl2 = yAxisParams[3];
        //var xLowerYOffset = xAxisParams[5];
        var minXData2 = xAxisParams[6]; //updated value
        var minYData2 = yAxisParams[6]; //updated value
        var maxXData2 = xAxisParams[7]; //updated value
        var maxYData2 = yAxisParams[7]; //updated value    
        yFinesse = 0;       
        xFinesse = 0;       
        //
        // Tau=1 cross-hair

        var barWidth = 1.0;
        var barColor = "#777777";
        var tTau1 = tauPoint(numDeps, tauRos, 1.0);
					//JB
        xShift = YBar(logE * tauRos[1][tTau1], minXData2, maxXData2, xAxisLength, barWidth, yAxisLength,
                yFinesse, barColor, plotTwoId, cnvsTwoId);

        yShift = XBar(temp[0][tTau1], minYData2, maxYData2, xAxisLength, barHeight,
                xFinesse, barColor, plotTwoId, cnvsTwoId);
        barHeight = 1.0;
        // Add label
                 txtPrint("<span style='font-size:small; color:#444444'><em>&#964</em><sub>Ros</sub>=1</span>", xShift, yShift, 300, lineColor, plotTwoId);
					//JB
        titleX = panelX + titleOffsetX;
        titleY = panelY + titleOffsetY;
        txtPrint("<span style='font-size:normal; color:blue'>Gas temperature </span>",
                titleOffsetX, titleOffsetY, 300, lineColor, plotTwoId);
					//JB
        //var dSize = 5.0; //plot point size
        var dSizeCnvs = 1.0; //plot point size
        var opac = 1.0; //opacity
        // RGB color
        var r255 = 0;
        var g255 = 0;
        var b255 = 255; //blue

        var ii;
        var xTickPosCnvs = xAxisLength * (logE * tauRos[1][0] - minXData2) / rangeXData2; // pixels   

        // horizontal position in pixels - data values increase rightward:
        var lastXShiftCnvs = xAxisXCnvs + xTickPosCnvs;

        var yTickPosCnvs = yAxisLength * (temp[0][0] - minYData2) / rangeYData2;
        // vertical position in pixels - data values increase upward:
        var lastYShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;


       for (var i = 0; i < numDeps; i++) {
              

            ii = 1.0 * i;
            var xTickPosCnvs = xAxisLength * (logE * tauRos[1][i] - minXData2) / rangeXData2; // pixels   

            // horizontal position in pixels - data values increase rightward:
            var xShiftCnvs = xAxisXCnvs + xTickPosCnvs;

            var yTickPosCnvs = yAxisLength * (temp[0][i] - minYData2) / rangeYData2;
            // vertical position in pixels - data values increase upward:
            var yShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;


           var thisLine = document.createElementNS(xmlW3, 'line');
            thisLine.setAttributeNS(null, 'x1', lastXShiftCnvs);
            thisLine.setAttributeNS(null, 'x2', xShiftCnvs);
            thisLine.setAttributeNS(null, 'y1', lastYShiftCnvs);
            thisLine.setAttributeNS(null, 'y2', yShiftCnvs);
            thisLine.setAttributeNS(null, 'stroke', lineColor);
            thisLine.setAttributeNS(null, 'stroke-width', 2);
            cnvsTwoId.appendChild(thisLine);

            lastXShiftCnvs = xShiftCnvs;
            lastYShiftCnvs = yShiftCnvs;

        }

    cnvsTwoId.addEventListener("click", function() {
       //dataCoords(event, plotTwoId);
       var xyString = dataCoords(event, cnvsTwoId, xAxisLength, minXData2, rangeXData2, xAxisXCnvs,
                               yAxisLength, minYData2, rangeYData2, yAxisYCnvs);
       //console.log("PLOT TWO INSIDE: xAxisLength " + xAxisLength + " minXData " + minXData2 + " rangeXData " + rangeXData2 + " xAxisXCnvs " + xAxisXCnvs2);
       txtPrint(xyString, titleOffsetX+200, titleOffsetY+320, 150, lineColor, plotTwoId);
    });


//  Loop over limb darkening sub-disks - largest to smallest, and add color-coded Tau(theta) = 1 markers

        //dSize = 8.0;
        dSizeCnvs = 4.0;

        // Disk centre:
        //This approach does not allow for calibration easily:
        //now done earlier var bvr = bandIntens[2][0] + bandIntens[3][0] + bandIntens[4][0];
        //now down above: var rgbVega = [183.0 / 255.0, 160.0 / 255.0, 255.0 / 255.0];
    //console.log("PLOT TWO OUTSIDE: xAxisLength " + xAxisLength + " minXData " + minXData2 + " rangeXData " + rangeXData2 + " xAxisXCnvs " + xAxisXCnvs);
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

            rrI = bandIntens[4][i] / bvr0 * rNormVega; 
            r255 = Math.ceil(255.0 * rrI / renormI); 
            ggI = bandIntens[3][i] / bvr0 * vNormVega; 
            g255 = Math.ceil(255.0 * ggI / renormI); 
            bbI = bandIntens[2][i] / bvr0 * bNormVega; 
            b255 = Math.ceil(255.0 * bbI / renormI); 

            var xTickPosCnvs = xAxisLength * (logE * tauRos[1][dpthIndx] - minXData2) / rangeXData2; // pixels   

            // horizontal position in pixels - data values increase rightward:
            //var xShift = xOffset + xTickPos;
            var xShiftCnvs = xAxisXCnvs + xTickPosCnvs;// + 200;
            ////stringify and add unit:
            //        var xShiftStr = numToPxStrng(xShift);

            //var yTickPos = yRange * (temp[0][dpthIndx] - minYData) / rangeYData;
            var yTickPosCnvs = yAxisLength * (temp[0][dpthIndx] - minYData2) / rangeYData2;
            // vertical position in pixels - data values increase upward:
            var yShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;

            var RGBHex = colHex(r255, g255, b255);

            var thisCirc = document.createElementNS(xmlW3, 'circle');
            thisCirc.setAttributeNS(null, 'cx', xShiftCnvs);
            thisCirc.setAttributeNS(null, 'cy', yShiftCnvs);
            thisCirc.setAttributeNS(null, 'r', dSizeCnvs);
            thisCirc.setAttributeNS(null, 'stroke', RGBHex);
            thisCirc.setAttributeNS(null, 'fill', RGBHex);
            thisCirc.setAttributeNS(null, 'stroke-width', 2);
            cnvsTwoId.appendChild(thisCirc);

        }

// legend using dot of last color in loop directly above:

            var thisCirc = document.createElementNS(xmlW3, 'circle');
            thisCirc.setAttributeNS(null, 'cx', titleOffsetX + 365);
            thisCirc.setAttributeNS(null, 'cy', titleOffsetY + 10);
            thisCirc.setAttributeNS(null, 'r', dSizeCnvs);
            thisCirc.setAttributeNS(null, 'stroke', RGBHex);
            thisCirc.setAttributeNS(null, 'fill', RGBHex);
            thisCirc.setAttributeNS(null, 'stroke-width', 2);
            cnvsTwoId.appendChild(thisCirc);
                              
                              //JB
                              
        txtPrint("<span title='Limb darkening depths of &#964_Ros(&#952) = 1'><em>&#964</em><sub>Ros</sub>(0 < <em>&#952</em> < 90<sup>o</sup>) = 1</span>",
                titleOffsetX + 200, titleOffsetY, 300, lineColor, plotTwoId);
//legend for the colored dots corresponding with the spectral line (Plot 6)
        txtPrint("<span title='Limb darkening depths of &#964_Ros(&#952) = 1'>Specral Line <em>&#964</em><sub>&#955</sub>= 1</span>",
                titleOffsetX + 200, titleOffsetY+25, 300, lineColor, plotTwoId);

//Now overplot symbols for T_Ros values of 2-level atom monochromatic line depth = 1
//
//
//    console.log("Condition reached, numPoints " + numPoints);
//    for (var jj = 0; jj < numDeps; jj++){
//       console.log("logTauL[19] " + logTauL[19][jj]);
//    }


    } //Endplot two

					
//
//
//  *****   PLOT ELEVEN / PLOT 11
//
//
// Plot Eleven: Life Zone

        var plotRow = 0;
        var plotCol = 2;

//background color needs to be finessed so that white-ish stars will stand out:
       if (teff > 6000.0){
  //hotter white or blue-white star - darken the background (default background in #F0F0F0
           wDiskColor = "#808080";  
       } else {
           wDiskColor = wDefaultColor;
       }

        // Calculation of steam line and ice line:

        //Assuming liquid salt-free water at one atmospheric pGasressure is necessary:
        //var atmosPres = 101.0;  // test - kPa
//        var steamTemp = waterPhase(atmosPress);
       var steamTemp = solventPhase(atmosPress, phaseA, phaseB, phaseC);
        //console.log("steamTemp " + steamTemp); // + " steamTemp2 " + steamTemp2);
        //var steamTemp = 373.0; // K = 100 C
        //var iceTemp = 273.0; //K = 0 C
        var iceTemp = tripleTemp; 

        steamTemp = steamTemp - greenHouse;
        iceTemp = iceTemp - greenHouse;
        var logSteamLine, logIceLine;
//        var au = 1.4960e13; // 1 AU in cm
//        var rSun = 6.955e10; // solar radii to cm
        var log1AULine = logAu - logRSun; // 1 AU in solar radii
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
//Transiting exo-planet orbit:
        var rOrbitAU = rOrbit.toPrecision(3); //in AU
        var logTransLine = Math.log(rOrbit) + logAu - logRSun; //in soalr radii
        var steamTempRound = steamTemp.toPrecision(3);

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
        var radiusPx1AU = logScale * logTen(radiusScale * radius * Math.exp(log1AULine));
        radiusPx1AU = Math.ceil(radiusPx1AU);
//Transiting exo-planet orbit:
        var radiusPxTrans = logScale * logTen(radiusScale * radius * Math.exp(logTransLine));
        radiusPxTrans = Math.ceil(radiusPxTrans);

    //console.log("radius " + radius + " radiusPx " + radiusPx + " radiusPxSteam " + radiusPxSteam + " radiusPxIce " + radiusPxIce + " radiusPx1AU " + radiusPx1AU)
        // Key radii in order of *DECREASING* size (important!):
        //var numZone = 7;
        //var radii = [];
        //radii.length = numZone;
//// Safety defaults:
        //radii = [radiusPx1AU, radiusPx1AU, radiusPx1AU, radiusPx1AU, radiusPx1AU, radiusPx1AU, radiusPx1AU];
        rrI = saveRGB[0];
        ggI = saveRGB[1];
        bbI = saveRGB[2];
        var starRGBHex = "rgb(" + rrI + "," + ggI + "," + bbI + ")";
        //var colors = [];
        //colors.length = numZone;

        //radii = [radiusPx1AU+1, radiusPx1AU, radiusPxIce + 3, radiusPxIce, radiusPxSteam, radiusPxSteam - 3, radiusPx];
        var lzWdthPx = radiusPxIce - radiusPxSteam; //life zone width in pixels
        lzWdthPx = Math.round(lzWdthPx);
        var lzRdsPx = ( radiusPxSteam + radiusPxIce ) / 2.0;
        lzRdsPx = Math.round(lzRdsPx);


/*        if (radiusPx1AU >= (radiusPxIce + 3)){
           radii = [radiusPx1AU+1, radiusPx1AU, radiusPxIce + 3, radiusPxIce, radiusPxSteam, radiusPxSteam - 3, radiusPx];
           colors = ["#000000", wDiskColor, "#0000FF", "#00FF88", "#FF0000", wDiskColor, starRGBHex];
           //console.log("If branch 1");
           //console.log("radii " + radii);
        }
        if ( (radiusPx1AU >= radiusPxIce) && (radiusPx1AU < (radiusPxIce + 3)) ){
           radii = [radiusPxIce + 3, radiusPx1AU, radiusPx1AU-1, radiusPxIce, radiusPxSteam, radiusPxSteam - 3, radiusPx];
           colors = ["#0000FF", "#000000", "#0000FF", "#00FF88", "#FF0000", wDiskColor, starRGBHex];
           //console.log("If branch 2");
           //console.log("radii " + radii);
        }
        if ( (radiusPx1AU >= radiusPxSteam) && (radiusPx1AU < radiusPxIce) ){
           radii = [radiusPxIce + 3, radiusPxIce, radiusPx1AU+1, radiusPx1AU, radiusPxSteam, radiusPxSteam - 3, radiusPx];
           colors = ["#0000FF", "#00FF88", "#000000", "#00FF88", "#FF0000", wDiskColor, starRGBHex];
           //console.log("If branch 3");
           //console.log("radii " + radii);
        }
        if ( (radiusPx1AU >= (radiusPxSteam - 3)) && (radiusPx1AU < radiusPxSteam) ){
           radii = [radiusPxIce + 3, radiusPxIce, radiusPxSteam, radiusPx1AU+1, radiusPx1AU, radiusPxSteam - 3, radiusPx];
           colors = ["#0000FF", "#00FF88", "#FF0000", "#000000", "#FF0000", wDiskColor, starRGBHex];
           //console.log("If branch 4");
           //console.log("radii " + radii);
        }
        if ( (radiusPx1AU >= radiusPx) && (radiusPx1AU < (radiusPxSteam - 3)) ){
           radii = [radiusPxIce + 3, radiusPxIce, radiusPxSteam, radiusPxSteam - 3, radiusPx1AU, radiusPx1AU-1,  radiusPx];
           colors = ["#0000FF", "#00FF88", "#FF0000", wDiskColor, "#000000", wDiskColor, starRGBHex];
           //console.log("If branch 5");
           //console.log("radii " + radii);
        }
        if (radiusPx1AU <= radiusPx){
           radii = [radiusPxIce + 3, radiusPxIce, radiusPxSteam, radiusPxSteam - 3, radiusPx, radiusPx1AU, radiusPx1AU-1];
           colors = ["#0000FF", "#00FF88", "#FF0000", wDiskColor, starRGBHex, "#000000", starRGBHex];
           //console.log("If branch 6");
           //console.log("radii " + radii);
        }*/

     //console.log("radii " + radii)
        //
        //var titleYPos = xLowerYOffset - yRange + 40;
					//JB
      var panelOrigin = washer(plotRow, plotCol, panelWidth, wDiskColor, plotElevenId, cnvsElevenId);
					//JB
	panelX = panelOrigin[0];
        panelY = panelOrigin[1];
	cnvsElevenId.setAttribute('fill', wDiskColor);
        // Add title annotation:

        titleX = panelX + titleOffsetX;
        titleY = panelY + titleOffsetY;
			//JB
      
        txtPrint("<span style='font-size:normal; color:blue' title='Assumes liquid salt-free water at one Earth atmosphere pressure needed for life'><a href='https://en.wikipedia.org/wiki/Circumstellar_habitable_zone' target='_blank'>Life zone for habitable planets</a></span><br />\n\
     <span style='font-size:small'>(Logarithmic radius)</span>",
                titleOffsetX, titleOffsetY, 300, lineColor, plotElevenId);
        var legendY = titleOffsetY;
        var legendX = titleOffsetX + 320;
        txtPrint("<span style='font-size:small'>"
                + " <span style='color:#FF0000'>Steam line</span> " + steamLineAU + " <a href='https://en.wikipedia.org/wiki/Astronomical_unit' title='1 AU = Earths average distance from center of Sun'> AU</a><br /> "
                + " <span style='color:#00FF88'><strong>Life zone</strong></span><br /> "
                + " <span style='color:#0000FF'>Ice line</span> " + iceLineAU + " <a href='https://en.wikipedia.org/wiki/Astronomical_unit' title='1 AU = Earths average distance from center of Sun'> AU</a><br /> " 
                + " <span style='color:#990099'>Transit Orbit</span> " + rOrbitAU + " <a href='https://en.wikipedia.org/wiki/Methods_of_detecting_exoplanets#Transit_photometry' title='Orbit of transiting exoplanet'> AU</a><br /> "
                + " <span style='color:#000000'>Reference line: 1 <a href='https://en.wikipedia.org/wiki/Astronomical_unit' title='1 AU = Earths average distance from center of Sun'>AU</a></span>",
                legendX, legendY, 300, lineColor, plotElevenId);
//
        txtPrint("<span style='font-size:small'>" + solvent + " boiling temp = " + steamTempRound + " K</span>", 
          (legendX-75), (legendY+300), 300, lineColor, plotElevenId);
        //Get the Vega-calibrated colors from the intensity spectrum of each theta annulus:    
        // moved earlier var intcolors = iColors(lambdaScale, intens, numDeps, numThetas, numLams, tauRos, temp);
			//JB

// Adjust position to center star:
// Radius is really the *diameter* of the symbol
            var yCenterCnvs = panelHeight / 2;
            var xCenterCnvs = panelWidth / 2;

        //  Loop over radial zones - largest to smallest
   //console.log("cx " + xCenterCnvs + " cy " + yCenterCnvs);
/*        for (var i = 0; i < radii.length; i++) { // for (var i = parseFloat(radii.length); i > 2; i--) {
       //console.log(i, radii[i])
            var radiusStr = numToPxStrng(radii[i]);
            // Adjust position to center star:
            // Radius is really the *diameter* of the symbol

// Adjust position to center star:
// Radius is really the *diameter* of the symbol
            var yCenterCnvs = panelHeight / 2; 
            var xCenterCnvs = panelWidth / 2; 
				
				//JB
	
                //console.log("i " + i + " radii " + radii[i] + " colors " + colors[i]);
	
		var thisCirc = document.createElementNS(xmlW3, 'circle');
		//cric.setAttribute('id',"circ"+i);
		thisCirc.setAttributeNS(null, 'cx', xCenterCnvs);
                thisCirc.setAttributeNS(null, 'cy', yCenterCnvs);
		thisCirc.setAttributeNS(null, 'r', radii[i]);
                thisCirc.setAttributeNS(null, 'stroke', colors[i]);
                thisCirc.setAttributeNS(null, 'stroke-width', 2);
		thisCirc.setAttributeNS(null, 'fill', colors[i]);
		cnvsElevenId.appendChild(thisCirc);
				
//console.log(radii[i]-33);
				//JB
				
        }  //i loop (thetas)
*/

//Add host star:

        var thisCirc = document.createElementNS(xmlW3, 'circle');
        thisCirc.setAttributeNS(null, 'cx', xCenterCnvs);
        thisCirc.setAttributeNS(null, 'cy', yCenterCnvs);
        thisCirc.setAttributeNS(null, 'r', radiusPx);
        thisCirc.setAttributeNS(null, 'stroke', starRGBHex);
        thisCirc.setAttributeNS(null, 'stroke-width', 2);
        thisCirc.setAttributeNS(null, 'stroke-opacity', 1);
        thisCirc.setAttributeNS(null, 'fill', starRGBHex);
        thisCirc.setAttributeNS(null, 'fill-opacity', 1);
        cnvsElevenId.appendChild(thisCirc);

//Add life zone:

        var thisCirc = document.createElementNS(xmlW3, 'circle');
        thisCirc.setAttributeNS(null, 'cx', xCenterCnvs);
        thisCirc.setAttributeNS(null, 'cy', yCenterCnvs);
        thisCirc.setAttributeNS(null, 'r', lzRdsPx);
        thisCirc.setAttributeNS(null, 'stroke', "#00FF88");
        thisCirc.setAttributeNS(null, 'stroke-width', lzWdthPx);
        thisCirc.setAttributeNS(null, 'stroke-opacity', 1);
        thisCirc.setAttributeNS(null, 'fill', "#FFFFFF");
        thisCirc.setAttributeNS(null, 'fill-opacity', 0);
        cnvsElevenId.appendChild(thisCirc);

//Add steam line:

        var thisCirc = document.createElementNS(xmlW3, 'circle');
        thisCirc.setAttributeNS(null, 'cx', xCenterCnvs);
        thisCirc.setAttributeNS(null, 'cy', yCenterCnvs);
        thisCirc.setAttributeNS(null, 'r', radiusPxSteam);
        thisCirc.setAttributeNS(null, 'stroke', "#FF0000");
        thisCirc.setAttributeNS(null, 'stroke-width', 2);
        thisCirc.setAttributeNS(null, 'stroke-opacity', 1);
        thisCirc.setAttributeNS(null, 'fill', "#FFFFFF");
        thisCirc.setAttributeNS(null, 'fill-opacity', 0);
        cnvsElevenId.appendChild(thisCirc);


//Add ice line:

        var thisCirc = document.createElementNS(xmlW3, 'circle');
        thisCirc.setAttributeNS(null, 'cx', xCenterCnvs);
        thisCirc.setAttributeNS(null, 'cy', yCenterCnvs);
        thisCirc.setAttributeNS(null, 'r', radiusPxIce);
        thisCirc.setAttributeNS(null, 'stroke', "#0000FF");
        thisCirc.setAttributeNS(null, 'stroke-width', 2);
        thisCirc.setAttributeNS(null, 'stroke-opacity', 1);
        thisCirc.setAttributeNS(null, 'fill', "#FFFFFF");
        thisCirc.setAttributeNS(null, 'fill-opacity', 0);
        cnvsElevenId.appendChild(thisCirc);

//Add 1 AU line:

        var thisCirc = document.createElementNS(xmlW3, 'circle');
        thisCirc.setAttributeNS(null, 'cx', xCenterCnvs);
        thisCirc.setAttributeNS(null, 'cy', yCenterCnvs);
        thisCirc.setAttributeNS(null, 'r', radiusPx1AU);
        thisCirc.setAttributeNS(null, 'stroke', "#000000");
        thisCirc.setAttributeNS(null, 'stroke-width', 2);
        thisCirc.setAttributeNS(null, 'stroke-opacity', 1);
        thisCirc.setAttributeNS(null, 'fill', "#FFFFFF");
        thisCirc.setAttributeNS(null, 'fill-opacity', 0);
        cnvsElevenId.appendChild(thisCirc);

//Add orbit of transiting exo-planet:

        var thisCirc = document.createElementNS(xmlW3, 'circle');
        thisCirc.setAttributeNS(null, 'cx', xCenterCnvs);
        thisCirc.setAttributeNS(null, 'cy', yCenterCnvs);
        thisCirc.setAttributeNS(null, 'r', radiusPxTrans);
        thisCirc.setAttributeNS(null, 'stroke', "#990099");
        thisCirc.setAttributeNS(null, 'stroke-width', 2);
        thisCirc.setAttributeNS(null, 'stroke-opacity', 1);
        thisCirc.setAttributeNS(null, 'fill', "#FFFFFF");
        thisCirc.setAttributeNS(null, 'fill-opacity', 0);
        cnvsElevenId.appendChild(thisCirc);



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

        var minYData = logE * kappaRos[1][1] - 1.0; // Avoid upper boundary condition [i]=0
        var maxYData = logE * kappaRos[1][numDeps - 1];
        var yAxisName = "Log<sub>10</sub> <em>&#954<sub>&#955</sub></em> <br />(cm<sup>2</sup> g<sup>-1</sup>)";

        var fineness = "normal";
				//JB
	        var panelOrigin = washer(plotRow, plotCol, panelWidth, wDefaultColor, plotFourteenId, cnvsFourteenId);

				//JB        
panelX = panelOrigin[0];
        panelY = panelOrigin[1];
				//JB
        cnvsFourteenId.setAttribute('fill', wDefaultColor); 
        var xAxisParams = XAxis(panelX, panelY, xAxisLength,
                minXData, maxXData, xAxisName, fineness,
                plotFourteenId, cnvsFourteenId);
				//JB
        //xOffset = xAxisParams[0];
        var rangeXData14 = xAxisParams[1];
        var deltaXData14 = xAxisParams[2];
        var deltaXPxl14 = xAxisParams[3];
        //yOffset = xAxisParams[4];
        var xLowerYOffset14 = xAxisParams[5];
        var minXData14 = xAxisParams[6]; //updated value
        var maxXData14 = xAxisParams[7]; //updated value
			//JB
	        var yAxisParams = YAxis(panelX, panelY,
                minYData, maxYData, yAxisName,
                fineness, plotFourteenId, cnvsFourteenId);
			//JB
        var rangeYData14 = yAxisParams[1];
        var deltaYData14 = yAxisParams[2];
        var deltaYPxl14 = yAxisParams[3];
        var minYData14 = yAxisParams[6]; //updated value
        var maxYData14 = yAxisParams[7]; //updated value 

        yFinesse = 0;       
        xFinesse = 0;       
        titleX = panelX + titleOffsetX;
        titleY = panelY + titleOffsetY;
				//JB
        txtPrint("log<sub>10</sub> <a href='https://en.wikipedia.org/wiki/Absorption_(electromagnetic_radiation)' title='mass extinction coefficient' target='_blank'>Extinction</a>",
                titleOffsetX, titleOffsetY, 300, lineColor, plotFourteenId);
        txtPrint("<span style='font-size:small'>"
                + "<span><em>&#954</em><sub>Ros</sub></span>,  "
                + " <span style='color:#0000FF'><em>&#954<sub>&#955</sub></em> 360 nm</span>,  "
                + " <span style='color:#00FF00'><em>&#954<sub>&#955</sub></em> 500 nm</span>,  "
               // + " <span style='color:#FF0000'><em>&#954<sub>&#955</sub></em> 1640 nm</span> ",
                + " <span style='color:#FF0000'><em>&#954<sub>&#955</sub></em> 1000 nm</span> ",
                   titleOffsetX, titleOffsetY+35, 350, lineColor, plotFourteenId);
               			//JB
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
       var xTickPosCnvs = xAxisLength * (logE * tauRos[1][0] - minXData14) / rangeXData14; // pixels   
       // horizontal position in pixels - data values increase rightward:
       var lastXShiftCnvs = xAxisXCnvs + xTickPosCnvs;
       // vertical position in pixels - data values increase upward:
       var yTickPosCnvs = yAxisLength * (logE * kappaRos[1][0] - minYData14) / rangeYData14;
       var lastYShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;


        for (var i = 1; i < numDeps; i++) {

            ii = 1.0 * i;
            var xTickPosCnvs = xAxisLength * (logE * tauRos[1][i] - minXData14) / rangeXData14; // pixels   

            // horizontal position in pixels - data values increase rightward:
            var xShiftCnvs = xAxisXCnvs + xTickPosCnvs;

            // vertical position in pixels - data values increase upward:
            var yTickPosCnvs = yAxisLength * (logE * kappaRos[1][i] - minYData14) / rangeYData14;
            var yShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;

 //console.log("i " + i + " lastXShiftCnvs " + lastXShiftCnvs);

//log kappa_Ros
//line plot
				//JB
	    var line = document.createElementNS(xmlW3, 'line');
            line.setAttributeNS(null, 'x1', lastXShiftCnvs);
            line.setAttributeNS(null, 'x2', xShiftCnvs);
            line.setAttributeNS(null, 'y1', lastYShiftCnvs);
            line.setAttributeNS(null, 'y2', yShiftCnvs);
	    line.setAttributeNS(null, 'stroke', lineColor);
	    line.setAttributeNS(null, 'stroke-width', 2);
	    cnvsFourteenId.appendChild(line);
				//JB
            lastXShiftCnvs = xShiftCnvs;
            lastYShiftCnvs = yShiftCnvs;
        }


    //cnvsFourteenId.addEventListener("mouseover", function() { 
    cnvsFourteenId.addEventListener("click", function() {
       //dataCoords(event, plotFourteenId);
       var xyString = dataCoords(event, cnvsFourteenId, xAxisLength, minXData14, rangeXData14, xAxisXCnvs,
                               yAxisLength, minYData14, rangeYData14, yAxisYCnvs);
       txtPrint(xyString, titleOffsetX+200, titleOffsetY+320, 150, lineColor, plotFourteenId);
    });


// Tau=1 cross-hair

        var barWidth = 1.0;
        var barColor = "#777777";
					//JB
        var xShift = YBar(logE * tauRos[1][tTau1], minXData14, maxXData14, xAxisLength, barWidth, yAxisLength,
                yFinesse, barColor, plotFourteenId, cnvsFourteenId);

        var barHeight = 1.0;
        var yShift = XBar(logE * kappaRos[1][tTau1], minYData14, maxYData14, xAxisLength, barHeight,
                xFinesse, barColor, plotFourteenId, cnvsFourteenId);
        txtPrint("<span style='font-size:small; color:#444444'><em>&#964</em><sub>Ros</sub>=1</span>",
                xShift, yShift, 65, lineColor, plotFourteen);
					//JB

}

    //
    //
    //  *****   PLOT FIFTEEN / PLOT 15 (PLOT 17 in ChromaStar)
    //
    //
    // Plot fifteen:  Fourier (cosine) transform of Intensity profile across disk (I(theta/(pi/2)))
 
//ifShowRad = false; //For movie 
   if (ifShowRad === true) {

        var plotRow = 3;
        var plotCol = 1;
//
        var minXData = ft[0][0];
        var maxXData = ft[0][numK-1];
// console.log("minXData " + minXData + " maxXData " + maxXData);
        var xAxisName = "<em>k</em> (RAD/RAD)";
        var iFtMinMax = minMax(ft[1]);
// console.log("iFtMinMax[1] " + iFtMinMax[1] + " ft[1][iFtMinMax[1]] " + ft[1][iFtMinMax[1]]);
//logarithmic        var minYData = -2.0;  //logarithmic
//logarithmic        var maxYData = logE*Math.log(ft[1][iFtMinMax[1]]);  //logarithmic
        var minYData = ft[1][iFtMinMax[0]];  //logarithmic
        var maxYData = ft[1][iFtMinMax[1]];  //logarithmic
        //var maxYData = tuneBandIntens[0] / norm;
        var yAxisName = "<span title='Monochromatic surface specific intensity'><a href='http://en.wikipedia.org/wiki/Specific_radiative_intensity' target='_blank'><em>I</em><sub>&#955</sub>(<em>k</em>)/<br /><em>I</em><sub>&#955</sub>(0)</a></span>";

        var fineness = "normal";
//
				//JB
        var panelOrigin = washer(plotRow, plotCol, panelWidth, wDefaultColor, plotFifteenId, cnvsFifteenId);
				//JB
	panelX = panelOrigin[0];
        panelY = panelOrigin[1];
				//JB
	cnvsFifteenId.setAttribute('fill',wDefaultColor);
        var xAxisParams = XAxis(panelX, panelY, xAxisLength,
                minXData, maxXData, xAxisName, fineness,
                plotFifteenId, cnvsFifteenId);
        var yAxisParams = YAxis(panelX, panelY,
                minYData, maxYData, yAxisName,
                fineness, plotFifteenId, cnvsFifteenId);
				//JB
        var rangeXData15 = xAxisParams[1];
        var deltaXData15 = xAxisParams[2];
        var deltaXPxl15 = xAxisParams[3];
        var rangeYData15 = yAxisParams[1];
        var deltaYData15 = yAxisParams[2];
        var deltaYPxl15 = yAxisParams[3];
        var minXData15 = xAxisParams[6]; //updated value
        var minYData15 = yAxisParams[6]; //updated value
        var maxXData15 = xAxisParams[7]; //updated value
        var maxYData15 = yAxisParams[7]; //updated value        
// console.log("minXData " + minXData + " maxXData " + maxXData);
        //
        // Add legend annotation:

        //var iLamMinMax = minMax2(masterFlux);
        //var iLamMax = iLamMinMax[1];
        //var lamMax = (1.0e7 * masterLams[iLamMax]).toPrecision(3);

//
        lineColor = "#000000";
        var diskLamLbl = diskLambda.toPrecision(3);
        var diskLamStr = diskLamLbl.toString(10);
        titleX = panelX + titleOffsetX;
        titleY = panelY + titleOffsetY;
				//JB
				
        txtPrint("<span style='font-size:small'><span style='color:#000000'><em>&#955</em><sub>Filter</sub> = " + diskLamStr + "nm</span><br /> ",
                xAxisXCnvs+10, titleOffsetY+20, 150, lineColor, plotFifteenId);
         //Add title annotation:


        txtPrint("<span style='font-size:normal; color:blue'><a href='https://en.wikipedia.org/wiki/Discrete_Fourier_transform' target='_blank'> Fourier transform of <em>I</em><sub>&#955</sub>(<em>&#952</em>)/ <em>I</em><sub>&#955</sub>(0)</a> </a></span>",
                titleOffsetX, titleOffsetY, 100, lineColor, plotFifteenId);
				//JB

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


        var xTickPosCnvs = xAxisLength * (ft[0][0] - minXData15) / rangeXData15; // pixels   
        // horizontal position in pixels - data values increase rightward:
        var lastXShiftCnvs = xAxisXCnvs + xTickPosCnvs;
//logarithmic        var yTickPosCnvs = yAxisLength * (logE*Math.log(ft[1][0]) - minYData) / rangeYData; //logarithmic
       var yTickPosCnvs = yAxisLength * (ft[1][0] - minYData15) / rangeYData15;
       //var yTickPos2Cnvs = yAxisLength * (ft[2][0] - minYData) / rangeYData;

        // vertical position in pixels - data values increase upward:
        var lastYShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;
        //var lastYShift2Cnvs = (yAxisYCnvs + yAxisLength) - yTickPos2Cnvs;
//

        for (var i = 1; i < numK; i++) {

  //console.log("i " + i + " ft[0] " + ft[0][i] + " ft[1] " + ft[1][i]);
            xTickPosCnvs = xAxisLength * (ft[0][i] - minXData15) / rangeXData15; // pixels   
            // horizontal position in pixels - data values increase rightward:
            var xShiftCnvs = xAxisXCnvs + xTickPosCnvs;

//logarithmic            yTickPosCnvs = yAxisLength * (logE*Math.log(ft[1][i]) - minYData) / rangeYData; //logarithmic
            yTickPosCnvs = yAxisLength * (ft[1][i] - minYData15) / rangeYData15;
           // yTickPos2Cnvs = yAxisLength * (ft[2][i] - minYData) / rangeYData;

            // vertical position in pixels - data values increase upward:
            var yShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;
           // var yShift2Cnvs = (yAxisYCnvs + yAxisLength) - yTickPos2Cnvs;

			//JB
            RGBHex = colHex(0, 0, 0);

 	    var line = document.createElementNS(xmlW3, 'line');
            line.setAttributeNS(null, 'x1', lastXShiftCnvs); 
            line.setAttributeNS(null, 'x2', xShiftCnvs); 
            line.setAttributeNS(null, 'y1', lastYShiftCnvs); 
            line.setAttributeNS(null, 'y2', yShiftCnvs); 
	    line.setAttributeNS(null, 'stroke',  RGBHex);
	    line.setAttributeNS(null, 'stroke-width', 2);
	    cnvsFifteenId.appendChild(line);
	//

            lastXShiftCnvs = xShiftCnvs;
            lastYShiftCnvs = yShiftCnvs;
           // lastYShift2Cnvs = yShift2Cnvs;
        }


    //cnvsSeventeenId.addEventListener("mouseover", function() { 
    cnvsFifteenId.addEventListener("click", function() {
       //dataCoords(event, plotFifteenId);
       var xyString = dataCoords(event, cnvsFifteenId, xAxisLength, minXData15, rangeXData15, xAxisXCnvs,
                               yAxisLength, minYData15, rangeYData15, yAxisYCnvs);
       txtPrint(xyString, titleOffsetX+200, titleOffsetY+320, 150, lineColor, plotFifteenId);
    });

    }


    //
    //
    //  *****   PLOT SIXTEEN / PLOT 16 
    //
    //
    // Plot sixteen: log(Tau) vs log(Species Partial Pressure)

    if ( (ifShowAtmos === true) && (ppSpecies != "None") ) {

        var plotRow = 3;
        var plotCol = 2;
        var minXData = logE * logTauRosGas[0];
        var maxXData = logE * logTauRosGas[numGasDepths - 1];
        var xAxisName = "<span title='Rosseland mean optical depth'><a href='http://en.wikipedia.org/wiki/Optical_depth_%28astrophysics%29' target='_blank'>Log<sub>10</sub> <em>&#964</em><sub>Ros</sub></a></span>";

//Find the GAS package species we want to plot:

          var iPP = 0; //initialization
          for (var jj = 0; jj < numGas; jj++){
             if (ppSpecies.trim() == gsSpec[jj].trim()){
                   break;   //we found it
                 }
             iPP++;
          } //jj loop
          //console.log("ppSpecies "+ ppSpecies + " iPP " + iPP + " gsSpec " + gsSpec[iPP]);
 
        var log10P = [];
        log10P.length = numGasDepths;
        for (var i = 0; i < numGasDepths; i++) {
            log10P[i] = (log10GasPp[iPP][i]);
            //console.log(" i " + i + " log10P " + log10P[i]);
        }
       var iPPMinMax = minMax(log10P);
       //var iLamMinMaxBroad = minMax2(masterFluxBroad2);
       var iPPMax = iPPMinMax[1];
       var iPPMin = iPPMinMax[0];
        //var minYData = logE * logPTot[0] - 2.0; // Avoid upper boundary condition [i]=0
        var minYData = log10P[iPPMin] - 1.0;
        var maxYData = log10P[iPPMax] + 1.0;
        var yAxisName = "Log<sub>10</sub> <em>P</em> <br />(dynes <br />cm<sup>-2</sup>)";
        //console.log("minYData " + minYData + " maxYData " + maxYData);
        //washer(xRange, xOffset, yRange, yOffset, wDefaultColor, plotThreeId);

        var fineness = "normal";
        //var cnvsCtx = washer(plotRow, plotCol, wDefaultColor, plotThreeId, cnvsId);
				//JB
        var panelOrigin = washer(plotRow, plotCol, panelWidth, wDefaultColor, plotSixteenId, cnvsSixteenId);
				//JB
        panelX = panelOrigin[0];
        panelY = panelOrigin[1];
	cnvsSixteenId.setAttribute('fill', wDefaultColor);
        var xAxisParams = XAxis(panelX, panelY, xAxisLength,
                minXData, maxXData, xAxisName, fineness,
                plotSixteenId, cnvsSixteenId);
        var yAxisParams = YAxis(panelX, panelY,
                minYData, maxYData, yAxisName,
                fineness, plotSixteenId, cnvsSixteenId);

        //xOffset = xAxisParams[0];
        //yOffset = xAxisParams[4];
        var rangeXData16 = xAxisParams[1];
        var deltaXData16 = xAxisParams[2];
        var deltaXPxl16 = xAxisParams[3];
        var rangeYData16 = yAxisParams[1];
        var deltaYData16 = yAxisParams[2];
        var deltaYPxl16 = yAxisParams[3];
        var xLowerYOffset16 = xAxisParams[5];
        var minXData16 = xAxisParams[6]; //updated value
        var minYData16 = yAxisParams[6]; //updated value
        var maxXData16 = xAxisParams[7]; //updated value
        var maxYData16 = yAxisParams[7]; //updated value        
        yFinesse = 0;       
        xFinesse = 0;       
        //
        titleX = panelX + titleOffsetX;
        titleY = panelY + titleOffsetY;
					//JB
        txtPrint("log Pressure: <span style='color:blue' title='Partial pressure'><strong><em>P</em><sub>i</sub></strong></span> "
                + gsSpec[iPP],
                titleOffsetX, titleOffsetY, 300, lineColor, plotSixteenId);

					//JB
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

//initializations:
        var ii;
        var xTickPosCnvs = xAxisLength * (logE*logTauRosGas[0] - minXData16) / rangeXData16; // pixels   
        var yTickPosCnvs = yAxisLength * (log10P[0] - minYData16) / rangeYData16; // pixels   

        // horizontal position in pixels - data values increase rightward:
         var lastXShiftCnvs = xAxisXCnvs + xTickPosCnvs;

         var lastYTickPosCnvs = yAxisLength * (log10P[0] - minYData16) / rangeYData16;
         // vertical position in pixels - data values increase upward:
         var lastYShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;

        // Avoid upper boundary at i=0
        for (var i = 1; i < numGasDepths; i++) {

            ii = 1.0 * i;
            var xTickPosCnvs = xAxisLength * (logE*logTauRosGas[i] - minXData16) / rangeXData16; // pixels   

            // horizontal position in pixels - data values increase rightward:
            var xShiftCnvs = xAxisXCnvs + xTickPosCnvs;

            var yTickPosCnvs = yAxisLength * (logE * log10P[i] - minYData16) / rangeYData16;
            // vertical position in pixels - data values increase upward:
            var yShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;

		
            		
            //console.log("lastXShiftCnvs " + lastXShiftCnvs + " lastYShiftCnvs " + lastYShiftGCnvs + " xShiftCnvs " + xShiftCnvs + " yShiftCnvs " + yShiftGCnvs);

            var thisLine = document.createElementNS(xmlW3, 'line');
            thisLine.setAttributeNS(null, 'x1', lastXShiftCnvs);
            thisLine.setAttributeNS(null, 'x2', xShiftCnvs);
            thisLine.setAttributeNS(null, 'y1', lastYShiftCnvs);
            thisLine.setAttributeNS(null, 'y2', yShiftCnvs);
            thisLine.setAttributeNS(null, 'stroke', "#0000FF");
            thisLine.setAttributeNS(null, 'stroke-width', 2);
            cnvsSixteenId.appendChild(thisLine);

				//JB
            lastXShiftCnvs = xShiftCnvs;
            lastYShiftCnvs = yShiftCnvs;
        }

    //cnvsThreeId.addEventListener("mouseover", function() { 
    cnvsSixteenId.addEventListener("click", function() {
       //dataCoords(event, plotThreeId);
       var xyString = dataCoords(event, cnvsSixteenId, xAxisLength, minXData16, rangeXData16, xAxisXCnvs,
                               yAxisLength, minYData16, rangeYData16, yAxisYCnvs);
       txtPrint(xyString, titleOffsetX+200, titleOffsetY+320, 150, lineColor, plotSixteenId);
    }); 


// Tau=1 cross-hair

        var tTau1 = tauPoint(numDeps, tauRos, 1.0);
        var barWidth = 1.0;
        var barColor = "#777777";
        yFinesse = 0.0;
				//JB
        xShift = YBar(logE * tauRos[1][tTau1], minXData16, maxXData16, xAxisLength, barWidth, yAxisLength,
                yFinesse, barColor, plotSixteenId, cnvsSixteenId);
        barHeight = 1.0;
        yShift = XBar(logE * logPTot[tTau1], minYData16, maxYData16, xAxisLength, barHeight,
                xFinesse, barColor, plotSixteenId, cnvsSixteenId);
        txtPrint("<span style='font-size:small; color:#444444'><em>&#964</em><sub>Ros</sub>=1</span>",
                xShift, yShift, 300, lineColor, plotSixteenId);

				//JB
    }

//
//
//  *****   PLOT NINETEEN / PLOT 19
//
//

// Plot nineteen: Exoplanet transit light curves
//
    //if ((ifLineOnly === false) && (ifShowRad === true)) {
    //if ((ifLineOnly === false)) {
//    //For movie:
//    if (ifLineOnly === false) {

        var plotRow = 1;
        var plotCol = 0;
//
var ephemTHrs = [];
ephemTHrs.length = numEpochs;
for (var i = 0; i < numEpochs; i++){
   ephemTHrs[i] = ephemT[i] / 3600.0; //s to hours
}

        var minXData = ephemTHrs[0];
        var maxXData = ephemTHrs[numEpochs-1];
        var xAxisName = "Time, t (hrs)";

var yMinMaxUV = minMax(bandFluxTransit2[0]);
var yMinUV = bandFluxTransit2[0][yMinMaxUV[0]] / bandFluxTransit2[0][0]; //#minimum UV flux during transit
//console.log("yMinMaxUV " + yMinMaxUV, " yMinUV ", yMinUV);
var yMinMaxIR = minMax(bandFluxTransit2[numBands-1]);
var yMinIR = bandFluxTransit2[numBands-1][yMinMaxIR[0]] / bandFluxTransit2[numBands-1][0]; //#minimum IR flux during transit
var bothMins = [yMinUV, yMinIR];
var yMinMax = minMax(bothMins); // # minimum of the two
var minYData = bothMins[yMinMax[0]];
//Finesse:
//minYData = 0.9 * minYData;

//var maxYData = 1.0 + (1.0 - minYData);
var maxYData = 1.0;

//Try renormalizing to 0 untransited flux:
minYData = minYData - 1.0;
maxYData = maxYData - 1.0;
//console.log("minYData " + minYData + " maxYData " + maxYData);
var textStep = (maxYData-minYData)/10.0;

        var yAxisName = "<span title='Relative surface flux in band'><em>F</em><sub>band</sub>(t)/<br /><em>F</em><sub>band - 1</span>";

var whichBands = [0, 1, 3, 4, 5, 8];
var numPlotBands = whichBands.length;
var bandLbls = ["U", "B", "V", "R", "I", "K"];
var transR = [200,
              0,
              0,
              200,
              100,
              150];
var transG = [0,
              0,
              200,
              0,
              100,
              150];
var transB = [200,
              200,
              0,
              0,
              0,
              150];

var normFluxTransit = [];
normFluxTransit.length = numPlotBands;
for (var j = 0; j < numPlotBands; j++){
   normFluxTransit[j] = [];
   normFluxTransit[j].length = numEpochs;
   for (var i = 0; i < numEpochs; i++){
      normFluxTransit[j][i] = 0.0;
   }
}

        //var fineness = "ultrafine";
                                        //JB
        var panelOrigin = washer(plotRow, plotCol, panelWidth, wDefaultColor, plotNineteenId, cnvsNineteenId);
                                        //JB

        panelX = panelOrigin[0];
        panelY = panelOrigin[1];


                //console.log(SVGFive); is good, created fine
                //console.log("Before: minXData, maxXData " + minXData + ", " + maxXData);
                //console.log("XAxis called from PLOT 5:");
        var xAxisParams = XAxis(panelX, panelY, xAxisLength,
                minXData, maxXData, xAxisName, fineness,
                plotNineteenId, cnvsNineteenId);

        var yAxisParams = YAxis(panelX, panelY,
                 minYData, maxYData, yAxisName,
                 fineness, plotNineteenId, cnvsNineteenId);

        var rangeXData19 = xAxisParams[1];
        var deltaXData19 = xAxisParams[2];
        var deltaXPxl19 = xAxisParams[3];
        var rangeYData19 = yAxisParams[1];
        var deltaYData19 = yAxisParams[2];
        var deltaYPxl19 = yAxisParams[3];
        var minXData19 = xAxisParams[6]; //updated value
        var minYData19 = yAxisParams[6]; //updated value
        var maxXData19 = xAxisParams[7]; //updated value
        var maxYData19 = yAxisParams[7]; //updated value
                //console.log("After : minXData, maxXData " + minXData + ", " + maxXData + " rangeXData " + rangeXData);
        //
//
        // Add legend annotation:
        titleX = panelX + titleOffsetX;
        titleY = panelY + titleOffsetY;

        txtPrint("<span style='font-size:normal; color:blue'><a href='https://en.wikipedia.org/wiki/Methods_of_detecting_exoplanets#Transit_photometry' target='_blank'>\n\
     Planetary transit lightcurves</a></span>",
                titleOffsetX, titleOffsetY, 300, lineColor, plotNineteenId);
        txtPrint("<span style='font-size:small'>"
                + "<span style='color:violet'><em>U</em> </span>"
                + "<span style='color:blue'><em>B</em> </span>"
                + "<span style='color:green'><em>V</em> </span>"
                + "<span style='color:red'><em>R</em> </span>"
                + "<span style='color:brown'><em>I</em> </span>"
                + "<span style='color:gray'><em>K</em> </span>",
                titleOffsetX, titleOffsetY+35, 250, lineColor, plotNineteenId);
                                        //JB
                                        //JB
        roundNum = pPlanetYrs.toFixed(1);
        txtPrint("<span style='font-size:normal; color:black'>\n\
     <em>P</em><sub>Orbit</sub></span> " + roundNum + " yrs",
                titleOffsetX+300, titleOffsetY, 300, lineColor, plotNineteenId);

        var dSizeCnvs = 1.0; //plot point size
        var dSize0Cnvs = 1.0;
        var opac = 1.0; //opacity

        var yShiftCnvs, yShiftCCnvs, yShift0Cnvs, yShiftNCnvs;

  for (var iB = 0; iB < numPlotBands; iB++){

      for (var iE = 0; iE < numEpochs; iE++){
          normFluxTransit[iB][iE] = bandFluxTransit2[whichBands[iB]][iE]/bandFluxTransit2[whichBands[iB]][0];
          normFluxTransit[iB][iE] = normFluxTransit[iB][iE] - 1.0;
//Try re-normalizing to 0 flux:
      }

        var xTickPosCnvs = xAxisLength * (ephemTHrs[0] - minXData19) / rangeXData19; // pixels
//
        var lastXShiftCnvs = xAxisXCnvs + xTickPosCnvs;
        var yTickPosCnvs = yAxisLength * (normFluxTransit[iB][0] - minYData19) / rangeYData19;
        //console.log("iB " + iB + " normFluxTransit[iB][0] " + normFluxTransit[iB][0]);
//
        // vertical position in pixels - data values increase upward:
        //console.log("yAxisYCnvs " + yAxisYCnvs + " yAxisLength " + yAxisLength + " yTickPosCnvs " + yTickPosCnvs);
        var lastYShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;
        var xShift, yShift;

    //cnvsFiveId.addEventListener("mouseover", function() {
    cnvsNineteenId.addEventListener("click", function() {
       //dataCoords(event, plotFiveId);
       var xyString = dataCoords(event, cnvsNineteenId, xAxisLength, minXData19, rangeXData19, xAxisXCnvs,
                               yAxisLength, minYData19, rangeYData19, yAxisYCnvs);
       txtPrint(xyString, titleOffsetX+200, titleOffsetY+320, 150, lineColor, plotNineteenId);
    });

        for (var i = 1; i < numEpochs; i++) {

            xTickPosCnvs = xAxisLength * (ephemTHrs[i] - minXData19) / rangeXData19;
            ii = 1.0 * i;

            // horizontal position in pixels - data values increase rightward:
            xShiftCnvs = xAxisXCnvs + xTickPosCnvs;

            yTickPosCnvs = yAxisLength * (normFluxTransit[iB][i] - minYData19) / rangeYData19;
            // vertical position in pixels - data values increase upward:
            yShiftCnvs = (yAxisYCnvs + yAxisLength) - yTickPosCnvs;

//line plot
            var RGBHex = colHex(transR[iB], transG[iB], transB[iB]);
            //var RGBHex = colHex(200, 0, 200);

            var thisLine = document.createElementNS(xmlW3, 'line');
            thisLine.setAttributeNS(null, 'x1', lastXShiftCnvs);
            thisLine.setAttributeNS(null, 'x2', xShiftCnvs);
            thisLine.setAttributeNS(null, 'y1', lastYShiftCnvs);
            thisLine.setAttributeNS(null, 'y2', yShiftCnvs);
            thisLine.setAttributeNS(null, 'stroke', RGBHex);
            //thisLine.setAttributeNS(null, 'stroke', lineColor);
            thisLine.setAttributeNS(null, 'stroke-width', 2);
            cnvsNineteenId.appendChild(thisLine);

            lastXShiftCnvs = xShiftCnvs;
            lastYShiftCnvs = yShiftCnvs;

        } //iE plot point loop

  } //iB photometric band loop


// ****************************************


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

        var modelBanner = "Model: Teff " + teff + " K, log(g) " + logg + " log cm/s/s, [A/H] " + zScale + ", mass " + massStar + " M_Sun";
        txtPrint(modelBanner, 10, yOffsetPrint, 500, txtColor, printModelId);
        txtPrint("Vertical atmospheric structure", 10, yOffsetPrint + lineHeight, 200, txtColor, printModelId);
        //Column headings:

        var xTab = 190;
        txtPrint("i", 10, yOffsetPrint + lineHeight, 100, txtColor, printModelId);
        txtPrint("log<sub>10</sub> <em>&#964</em><sub>Rosseland</sub>", 10 + xTab, yOffsetPrint + 2*lineHeight, 400, txtColor, printModelId);
        txtPrint("log<sub>10</sub> depth (cm)", 10 + 2 * xTab, yOffsetPrint + 2*lineHeight, 400, txtColor, printModelId);
        txtPrint("log<sub>10</sub> <em>T</em><sub>Kin</sub> (K)", 10 + 3 * xTab, yOffsetPrint + 2*lineHeight, 400, txtColor, printModelId);
        txtPrint("log<sub>10</sub> <em>P</em><sub>Gas</sub> (dynes cm<sup>-2</sup>)", 10 + 4 * xTab, yOffsetPrint + 2*lineHeight, 400, txtColor, printModelId);
        txtPrint("log<sub>10</sub> <em>P</em><sub>Rad</sub> (dynes cm<sup>-2</sup>)", 10 + 5 * xTab, yOffsetPrint + 2*lineHeight, 400, txtColor, printModelId);
        txtPrint("log<sub>10</sub> <em>&#961</em> (g cm<sup>-3</sup>)", 10 + 6 * xTab, yOffsetPrint + 2*lineHeight, 400, txtColor, printModelId);
        txtPrint("log<sub>10</sub> <em>N</em><sub>e</sub> (cm<sup>-3</sup>)", 10 + 7 * xTab, yOffsetPrint + 2*lineHeight, 400, txtColor, printModelId);
        txtPrint("log<sub>10</sub><em>&#956</em> (g)", 10 + 8 * xTab, yOffsetPrint + 2*lineHeight, 400, txtColor, printModelId);
        txtPrint("log<sub>10</sub> <em>&#954</em> (cm<sup>2</sup> g<sup>-1</sup>)", 10 + 9 * xTab, yOffsetPrint + 2*lineHeight, 400, txtColor, printModelId);
        //txtPrint("log<sub>10</sub> <em>&#954</em><sub>500</sub> (cm<sup>2</sup> g<sup>-1</sup>)", 10 + 10 * xTab, yOffsetPrint + lineHeight, txtColor, printModelId);


        for (var i = 0; i < numDeps; i++) {
            yTab = yOffsetPrint + vOffset + (i+1) * lineHeight;
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
            //value = logE * kappa500[1][i];
            //value = value.toPrecision(5);
            //numPrint(value, 10 + 10 * xTab, yTab, txtColor, printModelId);
        }

    }


    if (ifPrintSED == true) {

        var modelBanner = "Model: Teff " + teff + " K, log(g) " + logg + " log cm/s/s, [A/H] " + zScale + ", mass " + massStar + " M_Sun";
        txtPrint(modelBanner, 10, yOffsetPrint, 500, txtColor, printModelId);
        txtPrint("Monochromatic surface flux spectral energy distribution (SED)", 10, yOffsetPrint + lineHeight, 400, txtColor, printModelId);
        //Column headings:

        var xTab = 190;
        txtPrint("log<sub>10</sub> <em>&#955</em> (cm)", 10, yOffsetPrint + 2*lineHeight, 400, txtColor, printModelId);
        txtPrint("log<sub>10</sub> <em>F</em><sub>&#955</sub> (ergs s<sup>-1</sup> cm<sup>-2</sup> cm<sup>-1</sup>)", 10 + xTab, yOffsetPrint + 2*lineHeight, 400, txtColor, printModelId);
        for (var i = 0; i < numMaster; i++) {
            yTab = yOffsetPrint + vOffset + (i+1) * lineHeight;
            value = logE * Math.log(masterLams2[i]);
            value = value.toPrecision(9);
            numPrint(value, 10, yTab, txtColor, printModelId);
            value = logE * masterFlux[1][i];
            value = value.toPrecision(7);
            numPrint(value, 10 + xTab, yTab, txtColor, printModelId);
        }
    }


    if (ifPrintIntens == true) {

        var modelBanner = "Model: Teff " + teff + " K, log(g) " + logg + " log cm/s/s, [A/H] " + zScale + ", mass " + massStar + " M_Sun";
        txtPrint(modelBanner, 10, yOffsetPrint, 500, txtColor, printModelId);
        txtPrint("Monochromatic specific intensity distribution", 10, yOffsetPrint + lineHeight, 400, txtColor, printModelId);
        //Column headings:

        var xTab = 100;
        txtPrint("log<sub>10</sub><em>&#955</em> (cm)", 10, yOffsetPrint + 2*lineHeight, 400, txtColor, printModelId);
        txtPrint("log<sub>10</sub><em>I</em><sub>&#955</sub>(<em>&#952</em>) (ergs s<sup>-1</sup> cm<sup>-2</sup> cm<sup>-1</sup> steradian<sup>-1</sup>)",
                10 + xTab, yOffsetPrint + lineHeight, 400, txtColor, printModelId);
        for (var j = 0; j < numThetas; j += 2) {
            value = cosTheta[1][j].toPrecision(5);
            txtPrint("cos <em>&#952</em>=" + value, 10 + (j + 1) * xTab, yOffsetPrint + 3 * lineHeight, 400, txtColor, printModelId);
        }

        for (var i = 0; i < numMaster; i++) {
            yTab = yOffsetPrint + vOffset + (i+2) * lineHeight;
            value = logE * Math.log(masterLams2[i]);
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


        var modelBanner = "Model: Teff " + teff + " K, log(g) " + logg + " log cm/s/s, [A/H] " + zScale + ", mass " + massStar + " M_Sun";
        txtPrint(modelBanner, 10, yOffsetPrint, 500, txtColor, printModelId);
        txtPrint("Monochromatic surface flux: Spectrum synthesis region", 10, yOffsetPrint + lineHeight, 400, txtColor, printModelId);
        //Column headings:

        var xTab = 190;
        txtPrint("<em>&#955</em> (nm)", 10, yOffsetPrint + lineHeight, 400, txtColor, printModelId);
        txtPrint("<em>F</em><sub>&#955</sub> / <em>F</em><sup>C</sup><sub>&#955</sub>", 10 + xTab, yOffsetPrint + 2*lineHeight, 400, txtColor, printModelId);
        for (var i = 0; i < numSpecSyn; i++) {
            yTab = yOffsetPrint + vOffset + (i+1) * lineHeight;
            value = 1.0e7 * specSynLams2[i];
            value = value.toPrecision(9);
            numPrint(value, 10, yTab, txtColor, printModelId);
            value = specSynFlux[0][i];
            value = value.toPrecision(7);
            numPrint(value, 10 + xTab, yTab, txtColor, printModelId);
        }

    }


    if (ifPrintLDC == true) {

        var modelBanner = "Model: Teff " + teff + " K, log(g) " + logg + " log cm/s/s, [A/H] " + zScale + ", mass " + massStar + " M_Sun";
        txtPrint(modelBanner, 10, yOffsetPrint, 500, txtColor, printModelId);
        txtPrint("Linear monochromatic continuum limb darkening coefficients (LCD)", 10, yOffsetPrint + lineHeight, 500, txtColor, printModelId);
        //Column headings:

        var xTab = 190;
        txtPrint("log<sub>10</sub> <em>&#955</em> (cm)", 10, yOffsetPrint + 2*lineHeight, 400, txtColor, printModelId);
        txtPrint("LDC", 10 + xTab, yOffsetPrint + lineHeight, 400, txtColor, printModelId);
        for (var i = 0; i < numLams; i++) {
            yTab = yOffsetPrint + vOffset + (i+1) * lineHeight;
            value = logE * Math.log(lambdaScale[i]);
            value = value.toPrecision(9);
            numPrint(value, 10, yTab, txtColor, printModelId);
            value = ldc[i];
            value = value.toPrecision(7);
            numPrint(value, 10 + xTab, yTab, txtColor, printModelId);
        }
    }

  if (ifPrintAbnd == true){
        var modelBanner = "Model: Teff " + teff + " K, log(g) " + logg + " log cm/s/s, [A/H] " + zScale + ", mass " + massStar + " M_Sun";
        txtPrint(modelBanner, 10, yOffsetPrint, 500, txtColor, printModelId);
     txtPrint("A_12 logarithmic abundnaces (log_10(N_X/N_H)+12)", 10, yOffsetPrint + lineHeight, 400, txtColor, printModelId);
     for (var i = 0; i < nelemAbnd; i++){
        yTab = yOffsetPrint + vOffset + (i+1) * lineHeight;
        value = element[i];
        txtPrint(value, 10, yTab, 400, txtColor, printModelId);
        value = abundance[i];
        value = value.toPrecision(3);
        numPrint(value, 10 + xTab, yTab, txtColor, printModelId);
     }
   }

    if (ifPrintPP == true) {

        var modelBanner = "Model: Teff " + teff + " K, log(g) " + logg + " log cm/s/s, [A/H] " + zScale + ", mass " + massStar + " M_Sun";
        txtPrint(modelBanner, 10, yOffsetPrint, 500, txtColor, printModelId);
        txtPrint("Partial pressures every 4th depth", 10, yOffsetPrint + lineHeight, 400, txtColor, printModelId);
        //Column headings:

        var xTab = 100;
        txtPrint("log<sub>10</sub><em>&#964</em> ", 10, yOffsetPrint + 2*lineHeight, 400, txtColor, printModelId);
        txtPrint("log<sub>10</sub><em>P</em><sub>i</sub>(<em>&#964</em>) (log<sub>10</sub> dynes cm<sup>-2</sup>)",
                200 + xTab, yOffsetPrint + lineHeight, 400, txtColor, printModelId);
        for (var j = 0; j < numGas; j++) {
            value = gsSpec[j];
            txtPrint(value, 10 + (j + 1) * xTab, yOffsetPrint + 3 * lineHeight, 400, txtColor, printModelId);
        }

        for (var i = 0; i < numGasDepths; i++) {
            yTab = yOffsetPrint + vOffset + (i+2) * lineHeight;
            value = logE*logTauRosGas[i];
            value = value.toPrecision(5);
            numPrint(value, 10, yTab, txtColor, printModelId);
            for (var j = 0; j < numGas; j ++) {
                value = log10GasPp[j][i];
                value = value.toPrecision(7);
                numPrint(value, 10 + (j + 1) * xTab, yTab, txtColor, printModelId);
            }
        }
    }

//Exo-planet transit light curves:
    if (ifPrintTrans == true) {

        var modelBanner = "Model: Teff " + teff + " K, log(g) " + logg + " log cm/s/s, [A/H] " + zScale + ", mass " + massStar + " M_Sun <br />"
         + " Planet radius " + rPlanet + " R_Earth " + " Orbital radius " + rOrbit + " AU";
        txtPrint(modelBanner, 10, yOffsetPrint, 500, txtColor, printModelId);
        txtPrint("Relative flux <em>F</em><sub>Trans</sub>/<em>F</em> in band", 10, yOffsetPrint + 2*lineHeight, 400, txtColor, printModelId);
        //Column headings:

        var xTab = 130;
        txtPrint("Time from mid-transit (hrs) ", 10, yOffsetPrint + 3*lineHeight, 400, txtColor, printModelId);
        //txtPrint("log<sub>10</sub><em>P</em><sub>i</sub>(<em>&#964</em>) (log<sub>10</sub> dynes cm<sup>-2</sup>)",
        //        200 + xTab, yOffsetPrint + lineHeight, 400, txtColor, printModelId);
        for (var iB = 0; iB < numPlotBands; iB++) {
            value = bandLbls[iB];
            txtPrint(value, 10 + (iB + 1) * xTab, yOffsetPrint + 4 * lineHeight, 400, txtColor, printModelId);
        }

        for (var iE = 0; iE < numEpochs; iE++) {
            yTab = yOffsetPrint + vOffset + (iE+2) * lineHeight;
            value = ephemTHrs[iE];
            value = value.toPrecision(5);
            numPrint(value, 10, yTab, txtColor, printModelId);
            for (var iB = 0; iB < numPlotBands; iB ++) {
                normFluxTransit[iB][iE] = bandFluxTransit2[whichBands[iB]][iE]/bandFluxTransit2[whichBands[iB]][0];
                value = normFluxTransit[iB][iE];
                value = value.toPrecision(10);
                numPrint(value, 10 + (iB + 1) * xTab, yTab, txtColor, printModelId);
            }
        }
    }



  if (ifPrintJSON == true){
        var modelBanner = "Model: Teff " + teff + " K, log(g) " + logg + " log cm/s/s, [A/H] " + zScale + ", mass " + massStar + " M_Sun";
        txtPrint(modelBanner, 10, yOffsetPrint, 400, txtColor, printModelId);
     txtPrint("Compound atmospheric model, SED, and synthetic spectrum output as <a href='https://en.wikipedia.org/wiki/JSON' target='_blank'> JSON</a> string", 
       10, yOffsetPrint + lineHeight, 400, txtColor, printModelId);
          yTab = yOffsetPrint + vOffset;
          txtPrint(xmlhttp.responseText, 0, yTab, 1000000, txtColor, printModelId);
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

