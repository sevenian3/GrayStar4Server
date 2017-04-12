/*
 * GrayStar
 * V1.0, June 2014
 * 
 * C. Ian Short
 * Saint Mary's University
 * Department of Astronomy and Physics
 * Institute for Computational Astrophysics (ICA)
 * Halifax, NS, Canada
 *  * ian.short@smu.ca
 * www.ap.smu.ca/~ishort/
 *
 * 1D, static, plane-parallel, LTE, gray stellar atmospheric model
 * core + wing approximation to Voigt spectral line profile
 *
 * Suitable for pedagogical purposes only
 * 
 * Logic written in Java SE 8.0, JDK 1.8
 * GUI written with JavaFX 8.0
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
 */


// **********************************************

"use strict";  //testing only!

// Global variables - Doesn't work - scope not global!

var c = 2.9979249E+10; // light speed in vaccuum in cm/s
var sigma = 5.670373E-5; //Stefan-Boltzmann constant ergs/s/cm^2/K^4  
var k = 1.3806488E-16; // Boltzmann constant in ergs/K
var h = 6.62606957E-27; //Planck's constant in ergs sec
var ee = 4.80320425E-10; //fundamental charge unit in statcoulombs (cgs)
var mE = 9.10938291E-28; //electron mass (g)
//Conversion factors
var amu = 1.66053892E-24; // atomic mass unit in g
var eV = 1.602176565E-12; // eV in ergs

//Methods:
//Natural logs more useful than base 10 logs - Eg. Formal soln module: 
// Fundamental constants
var logC = Math.log(c);
var logSigma = Math.log(sigma);
var logK = Math.log(k);
var logH = Math.log(h);
var logEe = Math.log(ee); //Named so won't clash with log_10(e)
var logMe = Math.log(mE);
//Conversion factors
var logAmu = Math.log(amu);
var logEv = Math.log(eV);
// ********************************************
// 
// 
// Utility functions:
//
//
// ********************************************


// //
// // numPrint function to Set up special area of screen for printing out computed values for trouble-shooting
// // requires value to be printed, and the x and y pixel positions in that order
// // Must be defined in scope of main() - ??
//

    var numPrint = function(val, x, y, RGBHex, areaId) {

        var xStr = numToPxStrng(x);
        var yStr = numToPxStrng(y);
     //   var RGBHex = colHex(r255, g255, b255);
        var valStr = val.toString(10);
        var numId = document.createElement("p");
        numId.style.position = "absolute";
        numId.style.display = "block";
        numId.style.marginTop = yStr;
        numId.style.marginLeft = xStr;
        numId.style.color = RGBHex;
        numId.innerHTML = valStr;
        //masterId.appendChild(numId);
        areaId.appendChild(numId);
    }; // end numPrint

    var txtPrint = function(text, x, y, RGBHex, areaId) {

        var xStr = numToPxStrng(x);
        var yStr = numToPxStrng(y);
       // var RGBHex = colHex(r255, g255, b255);
        var txtId = document.createElement("p");
        txtId.style.position = "absolute";
        txtId.style.display = "block";
        txtId.style.width = "500px";
        txtId.style.marginTop = yStr;
        txtId.style.marginLeft = xStr;
        txtId.style.color = RGBHex;
        txtId.innerHTML = text;
        //masterId.appendChild(numId);
        areaId.appendChild(txtId);
    }; // end txtPrint


    /*
 *      plotPnt takes in the *numerical* x- and y- DEVICE coordinates (browser pixels),
 *           hexadecimal colour, and opacity, and plots a generic plotting dot at that location:
 *                Calls numToPxStrng to convert numeric coordinates and opacity to style attribute strings for HTML
 *                     Calls colHex to convert R, G, and B amounts out of 255 into #RRGGBB hex format
 *                          */

    var plotPnt = function(x, y, RGBHex, opac, dSize, areaId) {

        var xStr = numToPxStrng(x);
        var yStr = numToPxStrng(y);
        var opacStr = numToPxStrng(opac);
        var dSizeStr = numToPxStrng(dSize);
   //     var RGBHex = colHex(r255, g255, b255);
//   var RGBHex = "#000000";
//
//   // Each dot making up the line is a separate element:
        var dotId = document.createElement("div");
        dotId.class = "dot";
        dotId.style.position = "absolute";
        dotId.style.display = "block";
        dotId.style.height = dSizeStr;
        dotId.style.width = dSizeStr;
        dotId.style.borderRadius = "100%";
        dotId.style.opacity = opacStr;
        dotId.style.backgroundColor = RGBHex;
        dotId.style.marginLeft = xStr;
        dotId.style.marginTop = yStr;
//Append the dot to the plot
///masterId.appendChild(dotId);
        areaId.appendChild(dotId);
    };
    /*
 *      plotLin takes in the *numerical* x- and y- DEVICE coordinates (browser pixels)
 *           OF TWO SUGGESSIVE DATA POITNS defining a line segment,
 *                hexadecimal colour, and opacity, and plots a generic plotting dot at that location:
 *                     Calls numToPxStrng to convert numeric coordinates and opacity to style attribute strings for HTML
 *                          Calls colHex to convert R, G, and B amounts out of 255 into #RRGGBB hex format
 *                               */


    var plotLin = function(x0, y0, x1, y1, RGBHex, opac, dSize, areaId) {

        // Parameters of a straight line - all that matters here is internal self-consistency:
               var slope = (y1 - y0) / (x1 - x0);
        var num = x1 - x0;
        var x, y, iFloat;
        for (var i = 0; i < num; i += 5) {
            iFloat = 1.0 * i;
            x = x0 + i;
            y = y0 + i * slope;
            var xStr = numToPxStrng(x);
            var yStr = numToPxStrng(y);
            var opacStr = numToPxStrng(opac);
            var dSizeStr = numToPxStrng(dSize);
            //var RGBHex = colHex(r255, g255, b255);
//   var RGBHex = "#000000";
//
//   Each dot making up the line is a separate element:
            var dotId = document.createElement("div");
            dotId.class = "dot";
            dotId.style.position = "absolute";
            dotId.style.display = "block";
            dotId.style.height = dSizeStr;
            dotId.style.width = dSizeStr;
            dotId.style.borderRadius = "100%";
            dotId.style.opacity = opacStr;
            dotId.style.backgroundColor = RGBHex;
            dotId.style.marginLeft = xStr;
            dotId.style.marginTop = yStr;
//Append the dot to the plot
//masterId.appendChild(dotId);
            areaId.appendChild(dotId);
        }
    };

    /*
 *      colHex takes in red, green, and blue (in that order!) amounts out of 255 and converts
 *           them into stringified #RRGGBB format for HTML
 *                */

    var colHex = function(r255, g255, b255) {



        var rr = Math.floor(r255); //MUST convert to integer
        var gg = Math.floor(g255); //MUST convert to integer
        var bb = Math.floor(b255); //MUST convert to integer

        var RGBHex = "rgb(";
        RGBHex = RGBHex.concat(rr);
        RGBHex = RGBHex.concat(",");
        RGBHex = RGBHex.concat(gg);
        RGBHex = RGBHex.concat(",");
        RGBHex = RGBHex.concat(bb);
        RGBHex = RGBHex.concat(")");
//////    var RGBHex = "rgb(60,80,120)";

        return RGBHex;
    };

    //
    //    //   ********* standForm()
    //        //
    //            //
    //                //
    //
    var standForm = function(x) {
        // Turn any old number into the nearest number in standard form with a whole number exponent
        //         //   and a mantissa rounded to the nearest canonical value appropriate for labeling a tick mark
        //                 //
        //
        var numParts = [2];
        var isNeg = false;
        if (x === 0.0) {
            numParts = [0.0, 0.0];
        } else {

            if (x < 0) {
                isNeg = true;
                x = -1.0 * x;
            }

            var b = logTen(x);
            var n = Math.floor(b);
            var a = x / Math.pow(10.0, n);
            if (isNeg === true) {
                a = -1.0 * a;
            }

            numParts[0] = a; //mantissa
            numParts[1] = n; //exponent
        }

        return numParts;
    };

// logTen function (JS only provides natural log as standard on all browsers)

var logTen = function(x) {

// JS' 'log()' is really ln()

    return Math.log(x) / Math.LN10;
} //end logTen


// numToPxStng function to convert operable and calculable numbers into strings in 'px' for setting HTML style attributes

var numToPxStrng = function(x) {

    var xStr = x.toString(10) + "px"; // argument means interpret x as base 10 number

    return xStr;
} //end numToPxStrng

/*
 Linear interpolation to a new abscissa - mainly for interpolating flux to a specific lambda
 */

var interpol = function(x, y, newX) {

    var newY;
    var num = x.length;
    // Bracket newX:
    var x1, x2;
    var p1, p2;
    p1 = 0;
    p2 = 1;
    x1 = x[p1];
    x2 = x[p2];
    for (var i = 1; i < num; i++) {

        if (x[0] < x[num - 1]) {
            // Case of monotonically increasing absicissae
            if (x[i] >= newX) {
// Found upper bracket
                p2 = i;
                p1 = i - 1;
                x2 = x[p2];
                x1 = x[p1];
                break;
            }
        } else {
            // Case of monotonically decreasing absicissae
            if (x[i] <= newX) {
// Found upper bracket
                p2 = i;
                p1 = i - 1;
                x2 = x[p2];
                x1 = x[p1];
                break;
            }
        }

    }

    var step = x2 - x1;
    //Interpolate
    newY = y[p2] * (newX - x1) / step
            + y[p1] * (x2 - newX) / step;
    //System.out.println("Interpol: p1, p2, x1, x2, y1, y2, newX, newY: " +
    //        p1 + " " + p2 + " " + x1 + " " + x2 + " " + y[1][p1] + " " + y[1][p2] + " " + newX + " " + newY + " ");
    //numPrint(roundNum, 100, 300, masterId); //debug

    return newY;
};


/*
 Linear interpolation to a new abscissa - mainly for interpoalting flux to a specific lambda
 This version for 2XN vector where we want to interpolate in row 1 - log units
 */

var interpol2 = function(x, y, newX) {

    var newY;
    // Bracket newX:
    var x1, x2;
    var p1, p2;
    p1 = 0;
    p2 = 1;
    x1 = x[p1];
    x2 = x[p2];
    for (var i = 1; i < x.length; i++) {
        if (x[i] >= newX) {
// Found upper bracket
            p2 = i;
            p1 = i - 1;
            x2 = x[p2];
            x1 = x[p1];
            break;
        }
    }

    var step = x2 - x1;
    //Interpolate
    // y is probably flux - Row 1 is log flux - let's interpolate in log space
    newY = y[1][p2] * (newX - x1) / step
            + y[1][p1] * (x2 - newX) / step;
    //System.out.println("Interpol: p1, p2, x1, x2, y1, y2, newX, newY: " + 
    //        p1 + " " + p2 + " " + x1 + " " + x2 + " " + y[1][p1] + " " + y[1][p2] + " " + newX + " " + newY + " ");
    //numPrint(roundNum, 100, 300, masterId); //debug

    return newY;
};


/**
 * Return the array index of the optical depth arry (tauRos) closest to a
 * desired value of optical depth (tau) Assumes the use wants to find a *linear*
 * tau value , NOT logarithmic
 */
var tauPoint = function(numDeps, tauRos, tau) {

    var index;
    var help = [];
    help.length = numDeps;
    for (var i = 0; i < numDeps; i++) {

        help[i] = tauRos[0][i] - tau;
        help[i] = Math.abs(help[i]);
    }
    index = 0;
    var min = help[index];
    for (var i = 1; i < numDeps; i++) {

        if (help[i] < min) {
            min = help[i];
            index = i;
        }

    }

    return index;
};

/**
 * Return the array index of the wavelength array (lambdas) closest to a desired
 * value of wavelength (lam)
 */

var lamPoint = function(numLams, lambdas, lam) {

    var index;

    var help = [];
    help.length = numLams;

    for (var i = 0; i < numLams; i++) {

        help[i] = lambdas[i] - lam;
        help[i] = Math.abs(help[i]);

    }
    index = 0;
    var min = help[index];

    for (var i = 1; i < numLams; i++) {

        if (help[i] < min) {
            min = help[i];
            index = i;
        }

    }

    return index;

};

/**
 * Return the array indices of minimum and maximum values of an input 1D array CAUTION; Will
 * return the *first* occurence if min and/or max values occur in multiple
 * places iMinMax[0] = first occurence of minimum iMinMax[1] = first occurence
 * of maximum
 */
var minMax = function(x) {

    var iMinMax = [];
    iMinMax.length = 2;

    var num = x.length;
    //System.out.println("MinMax: num: " + num);

    var iMin = 0;
    var iMax = 0;
    var min = x[iMin];
    var max = x[iMax];

    for (var i = 1; i < num; i++) {

        //System.out.println("MinMax: i , current min, x : " + i + " " + min + " " + x[i]);
        if (x[i] < min) {
            //System.out.println("MinMax: new min: if branch triggered" );
            min = x[i];
            iMin = i;
        }
        //System.out.println("MinMax: new min: " + min);

        if (x[i] > max) {
            max = x[i];
            iMax = i;
        }

    }
    //System.out.println("MinMax: " + iMin + " " + iMax);

    iMinMax[0] = iMin;
    iMinMax[1] = iMax;

    return iMinMax;

};


/**
 * Version of MinMax.minMax for 2XnumDep & 2XnumLams arrays where row 0 is
 * linear and row 1 is logarithmic
 *
 * Return the array indices of the minimum and maximum values of an input 1D array CAUTION; Will
 * return the *first* occurence if min and/or max values occur in multiple
 * places iMinMax[0] = first occurence of minimum iMinMax[1] = first occurence
 * of maximum
 */
var minMax2 = function(x) {

    var iMinMax = [];
    iMinMax.length = 2;

    var num = x[0].length;

    var iMin = 0;
    var iMax = 0;

    // Search for minimum and maximum in row 0 - linear values:
    var min = x[0][iMin];
    var max = x[0][iMax];

    for (var i = 1; i < num; i++) {

        if (x[0][i] < min) {
            min = x[0][i];
            iMin = i;
        }

        if (x[0][i] > max) {
            max = x[0][i];
            iMax = i;
        }

    }

    iMinMax[0] = iMin;
    iMinMax[1] = iMax;

    return iMinMax;

};

  var lambdaToRGB = function(Wavelength, zLevel){

// Okay - now the question is:  Which crayon in the box does your optic nerve and visual cortex
// // think corresponds to each wavelength??   This is actually beyond the realm of physics and astrophsyics...
// // Taken from Earl F. Glynn's web page:
// // <a href="http://www.efg2.com/Lab/ScienceAndEngineering/Spectra.htm">Spectra Lab Report</a>
// //Converted from C to JS by Ian Short, Aug 2015
// //
//
        var Gamma = 0.80;
        var IntensityMax = 255;
        var factor = 1.0;
        var Red, Green, Blue, Wavelength, r255, g255, b255;
        var rgb = [];
        rgb.length = 3;

                if ((Wavelength >= 370) && (Wavelength < 440)) {
                    Red = -(Wavelength - 440) / (440 - 370);
                    Green = 0.0;
                    Blue = 1.0;
                } else if ((Wavelength >= 440) && (Wavelength < 490)) {
                    Red = 0.0;
                    Green = (Wavelength - 440) / (490 - 440);
                    Blue = 1.0;
                } else if ((Wavelength >= 490) && (Wavelength < 510)) {
                    Red = 0.0;
                    Green = 1.0;
                    Blue = -(Wavelength - 510) / (510 - 490);
                } else if ((Wavelength >= 510) && (Wavelength < 580)) {
                    Red = (Wavelength - 510) / (580 - 510);
                    Green = 1.0;
                    Blue = 0.0;
                } else if ((Wavelength >= 580) && (Wavelength < 645)) {
                    Red = 1.0;
                    Green = -(Wavelength - 645) / (645 - 580);
                    Blue = 0.0;
                } else if ((Wavelength >= 645) && (Wavelength < 781)) {
                    Red = 1.0;
                    Green = 0.0;
                    Blue = 0.0;
                } else {
                    Red = 0.0;
                    Green = 0.0;
                    Blue = 0.0;
                }


                rgb[0] = Math.floor(IntensityMax * Math.pow(Red * factor, Gamma));
                rgb[1] = Math.floor(IntensityMax * Math.pow(Green * factor, Gamma));
                rgb[2] = Math.floor(IntensityMax * Math.pow(Blue * factor, Gamma));

                r255 = Math.floor(rgb[0] * zLevel);
                g255 = Math.floor(rgb[1] * zLevel);
                b255 = Math.floor(rgb[2] * zLevel);

            if (Wavelength < 370.0) {
                r255 = Math.floor(255.0 * zLevel);
                g255 = 0;
                b255 = Math.floor(255.0 * zLevel);
            }
            if (Wavelength >= 781.0) {
                r255 = Math.floor(128.0 * zLevel);
                g255 = Math.floor(0.0 * zLevel);
                b255 = 0;
            }


                var RGBHex = colHex(r255, g255, b255);


       return RGBHex;

  };


/**
 *
 * @author Ian vectorized version of simple linear 1st order interpolation
 * // Caution: Assumes new and old abscissae are in monotonic increasing order
 */

   var interpolV = function(y, x, newX) {

        var num = x.length;
        if (num != y.length){
          //System.out.println("Toolbox.interpolV(): Old x and y must be same length");  
        }
        var newNum = newX.length;
        //System.out.println("interpolV: newNum " + newNum + " num " + num); 
        var newY = [];
        newY.length = newNum;

//Renormalize ordinates:
    
    var iMinAndMax = minMax(y);
    
    var norm = y[iMinAndMax[1]];
    //System.out.println("norm " + norm);
    var yNorm = [];
    yNorm.length = num; 
    var newYNorm = [];
    newYNorm.length = newNum;
    for (var i = 0; i < num; i++){
      yNorm[i] = y[i] / norm; 
    }

// Set any newX elements that are *less than* the first x element to th first 
// x element - "0th order extrapolation"
//
        var start = 0;
         for (var i = 0; i < newNum; i++) {
            if (newX[i] <= x[1]){
              newYNorm[i] = yNorm[0];
              start++;
            }
            if (newX[i] > x[1]){
               break;
            }
         }   
  //System.out.println("start " + start);
  //System.out.println("x[0] " + x[0] + " x[1] " + x[1] + " newX[start] " + newX[start]);
  var jWght, jm1Wght, denom;
  
  if (start < newNum-1){

        var j = 1; //initialize old abscissae index
        //outer loop over new abscissae
        for (var i = start; i < newNum; i++) {

            //System.out.println("i " + i + " j " + j);

// break out if current element newX is *greater* that last x element
            if ( (newX[i] > x[num-1]) || (j > (num-1)) ){
               break; 
            }

            while (x[j] < newX[i]) {
                j++;
            }
            //console.log("i " + i + " newX[i] " + newX[i] + " j " + j + " x[j-1] " + x[j-1] + " x[j] " + x[j]);
            //1st order Lagrange method:
            jWght = newX[i] * (1.0 - (x[j-1]/newX[i])); //(newX[i]-x[j-1])
            jm1Wght = x[j] * (1.0 - (newX[i]/x[j])); //(x[j]-newX[i])
            denom = x[j] * (1.0 - (x[j-1]/x[j])); //(x[j]-x[j-1])
            jWght = jWght / denom;
            jm1Wght = jm1Wght / denom;
            //newYNorm[i] = (yNorm[j]*(newX[i]-x[j-1])) + (yNorm[j-1]*(x[j]-newX[i]));
            newYNorm[i] = (yNorm[j]*jWght) + (yNorm[j-1]*jm1Wght);
            //console.log("i " + i + " newYNorm[i] " + newYNorm[i] + " j " + j + " yNorm[j-1] " + yNorm[j-1] + " yNorm[j] " + yNorm[j]);
        }

    } //start condition

// Set any newX elements that are *greater than* the first x element to the last 
// x element - "0th order extrapolation"
//
         for (var i = 0; i < newNum; i++) {
            if (newX[i] >= x[num-1]){
              newYNorm[i] = yNorm[num-1];
            }
         }   

//Restore ordinate scale
    //console.log("norm " + norm);
    for (var i = 0; i < newNum; i++){
      newY[i] = newYNorm[i] * norm; 
      //console.log("i " + i + " newYNorm[i] " + newYNorm[i] + " newY[i] " + Math.log(newY[i]));
    }


        return newY;
 };

//
//Input data on water phase in temp-press place
// - only boiling point significantly affected
//
   var waterPhase = function(atmPresIn){

//Input: Planetary atmospheric surface pressure in kPa 

//Data from
//The Engineering ToolBox
//http://www.engineeringtoolbox.com/boiling-point-water-d_926.html
//Water  Pressure and Boiling Point
//Pressure Boiling Point
// kPa     deg C

  var numDataPnts = 99;
  var atmPresKPa = [];
  atmPresKPa.length = numDataPnts;
  var boilTempC = [];
  boilTempC.length = numDataPnts;

 atmPresKPa[0] = 3.45   ; boilTempC[0] = 26.4 ;
 atmPresKPa[1] = 6.90   ; boilTempC[1] = 38.7 ;
 atmPresKPa[2] = 13.79  ; boilTempC[2] = 52.2 ;
 atmPresKPa[3] = 20.69  ; boilTempC[3] = 60.8 ;
 atmPresKPa[4] = 27.58  ; boilTempC[4] = 67.2 ;
 atmPresKPa[5] = 34.48  ; boilTempC[5] = 72.3 ;
 atmPresKPa[6] = 41.37  ; boilTempC[6] = 76.7 ;
 atmPresKPa[7] = 48.27  ; boilTempC[7] = 80.4 ;
 atmPresKPa[8] = 55.16  ; boilTempC[8] = 83.8 ;
 atmPresKPa[9] = 62.06  ; boilTempC[9] = 86.8 ;
 atmPresKPa[10] = 68.95  ; boilTempC[10] = 89.6 ;
 atmPresKPa[11] = 75.85  ; boilTempC[11] = 92.1 ;
 atmPresKPa[12] = 82.74  ; boilTempC[12] = 94.4 ;
 atmPresKPa[13] = 89.64  ; boilTempC[13] = 96.6 ;
 atmPresKPa[14] = 96.53  ; boilTempC[14] = 98.7 ;
 atmPresKPa[15] = 101.3  ; boilTempC[15] = 100 ;
 atmPresKPa[16] = 103.4  ; boilTempC[16] = 101 ;
 atmPresKPa[17] = 110.3  ; boilTempC[17] = 102 ;
 atmPresKPa[18] = 117.2  ; boilTempC[18] = 104 ;
 atmPresKPa[19] = 124.1  ; boilTempC[19] = 106 ;
 atmPresKPa[20] = 131.0  ; boilTempC[20] = 107 ;
 atmPresKPa[21] = 137.9  ; boilTempC[21] = 109 ;
 atmPresKPa[22] = 151.7  ; boilTempC[22] = 112 ;
 atmPresKPa[23] = 165.5  ; boilTempC[23] = 114 ;
 atmPresKPa[24] = 179.3  ; boilTempC[24] = 117 ;
 atmPresKPa[25] = 193.1  ; boilTempC[25] = 119 ;
 atmPresKPa[26] = 206.9  ; boilTempC[26] = 121 ;
 atmPresKPa[27] = 220.6  ; boilTempC[27] = 123 ;
 atmPresKPa[28] = 234.4  ; boilTempC[28] = 125 ;
 atmPresKPa[29] = 248.2  ; boilTempC[29] = 127 ;
 atmPresKPa[30] = 262.0  ; boilTempC[30] = 129 ;
 atmPresKPa[31] = 275.8  ; boilTempC[31] = 131 ;
 atmPresKPa[32] = 289.6  ; boilTempC[32] = 132 ;
 atmPresKPa[33] = 303.4  ; boilTempC[33] = 134 ;
 atmPresKPa[34] = 317.2  ; boilTempC[34] = 135 ;
 atmPresKPa[35] = 331.0  ; boilTempC[35] = 137 ;
 atmPresKPa[36] = 344.8  ; boilTempC[36] = 138 ;
 atmPresKPa[37] = 358.5  ; boilTempC[37] = 140 ;
 atmPresKPa[38] = 372.3  ; boilTempC[38] = 141 ;
 atmPresKPa[39] = 386.1  ; boilTempC[39] = 142 ;
 atmPresKPa[40] = 399.9  ; boilTempC[40] = 144 ;
 atmPresKPa[41] = 413.7  ; boilTempC[41] = 145 ;
 atmPresKPa[42] = 427.5  ; boilTempC[42] = 146 ;
 atmPresKPa[43] = 441.3  ; boilTempC[43] = 147 ;
 atmPresKPa[44] = 455.1  ; boilTempC[44] = 148 ;
 atmPresKPa[45] = 468.9  ; boilTempC[45] = 149 ;
 atmPresKPa[46] = 482.7  ; boilTempC[46] = 151 ;
 atmPresKPa[47] = 496.4  ; boilTempC[47] = 152 ;
 atmPresKPa[48] = 510.2  ; boilTempC[48] = 153 ;
 atmPresKPa[49] = 524.0  ; boilTempC[49] = 154 ;
 atmPresKPa[50] = 537.8  ; boilTempC[50] = 155 ;
 atmPresKPa[51] = 551.6  ; boilTempC[51] = 156 ;
 atmPresKPa[52] = 565.4  ; boilTempC[52] = 157 ;
 atmPresKPa[53] = 579.2  ; boilTempC[53] = 158 ;
 atmPresKPa[54] = 593.0  ; boilTempC[54] = 158 ;
 atmPresKPa[55] = 606.8  ; boilTempC[55] = 159 ;
 atmPresKPa[56] = 620.6  ; boilTempC[56] = 160 ;
 atmPresKPa[57] = 634.3  ; boilTempC[57] = 161 ;
 atmPresKPa[58] = 648.1  ; boilTempC[58] = 162 ;
 atmPresKPa[59] = 661.9  ; boilTempC[59] = 163 ;
 atmPresKPa[60] = 675.7  ; boilTempC[60] = 164 ;
 atmPresKPa[61] = 689.5  ; boilTempC[61] = 164 ;
 atmPresKPa[62] = 724.0  ; boilTempC[62] = 166 ;
 atmPresKPa[63] = 758.5  ; boilTempC[63] = 168 ;
 atmPresKPa[64] = 792.9  ; boilTempC[64] = 170 ;
 atmPresKPa[65] = 827.4  ; boilTempC[65] = 172 ;
 atmPresKPa[66] = 1034   ; boilTempC[66] = 181 ;
 atmPresKPa[67] = 1207   ; boilTempC[67] = 189 ;
 atmPresKPa[68] = 1379   ; boilTempC[68] = 194 ;
 atmPresKPa[69] = 1551   ; boilTempC[69] = 200 ;
 atmPresKPa[70] = 1724   ; boilTempC[70] = 205 ;
 atmPresKPa[71] = 1896   ; boilTempC[71] = 210 ;
 atmPresKPa[72] = 2069   ; boilTempC[72] = 214 ;
 atmPresKPa[73] = 2241   ; boilTempC[73] = 218 ;
 atmPresKPa[74] = 2413   ; boilTempC[74] = 222 ;
 atmPresKPa[75] = 2586   ; boilTempC[75] = 226 ;
 atmPresKPa[76] = 2758   ; boilTempC[76] = 229 ;
 atmPresKPa[77] = 2930   ; boilTempC[77] = 233 ;
 atmPresKPa[78] = 3103   ; boilTempC[78] = 236 ;
 atmPresKPa[79] = 3275   ; boilTempC[79] = 239 ;
 atmPresKPa[80] = 3448   ; boilTempC[80] = 242 ;
 atmPresKPa[81] = 3620   ; boilTempC[81] = 245 ;
 atmPresKPa[82] = 3792   ; boilTempC[82] = 247 ;
 atmPresKPa[83] = 3965   ; boilTempC[83] = 250 ;
 atmPresKPa[84] = 4137   ; boilTempC[84] = 252 ;
 atmPresKPa[85] = 4309   ; boilTempC[85] = 255 ;
 atmPresKPa[86] = 4482   ; boilTempC[86] = 257 ;
 atmPresKPa[87] = 4654   ; boilTempC[87] = 260 ;
 atmPresKPa[88] = 4827   ; boilTempC[88] = 262 ;
 atmPresKPa[89] = 4999   ; boilTempC[89] = 264 ;
 atmPresKPa[90] = 5171   ; boilTempC[90] = 266 ;
 atmPresKPa[91] = 5344   ; boilTempC[91] = 268 ;
 atmPresKPa[92] = 5516   ; boilTempC[92] = 270 ;
 atmPresKPa[93] = 5688   ; boilTempC[93] = 272 ;
 atmPresKPa[94] = 5861   ; boilTempC[94] = 274 ;
 atmPresKPa[95] = 6033   ; boilTempC[95] = 276 ;
 atmPresKPa[96] = 6206   ; boilTempC[96] = 278 ;
 atmPresKPa[97] = 6550   ; boilTempC[97] = 281 ;
 atmPresKPa[98] = 6895   ; boilTempC[98] = 285 ;

    var steamTempK, steamTempC;

    //steamTempC = interpolV(boilTempC, atmPresKPa, atmPresIn);
    steamTempC = interpol(atmPresKPa, boilTempC, atmPresIn);
    steamTempK = steamTempC + 273.0;

    return steamTempK;

   }; //end function waterPhase

//
//Input data on solvent phase in temp-press space
// - only boiling point significantly affected
//

//Input: Planetary atmospheric surface pressure in kPa
//     : Antoine coefficients for approximation to temperature-vapor pressure relation, A, B, C 
//Ouput: boiling point in K


   var solventPhase = function(atmPresIn, A, B, C){

      //var pTorr = atmPresIn / 0.133322; //kPa to torr 
      var pBar = 0.01 * atmPresIn; //kPa to bar 

      //var steamTempC = ( B / (A - logTen(pBar) ) ) - C;    
      var steamTempK = ( B / (A - logTen(pBar) ) ) - C;

      //var steamTempK = steamTempC + 273.0;

      return steamTempK;

   }; //end function solventPhase

