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


    //
    //    //
    //        //
    //            //  ********* rounder()
    //                //
    //                    //
    //                        //
    //
        var rounder = function(x, n, flag) {

        // Return a number rounded up or down to n decimal places (sort of?)
        //         //
        //
        var y, z;
        n = Math.abs(Math.floor(n)); //n was supposed to be a positive whole number anyway
        if (flag != "up" && flag != "down") {
            flag = "down";
        }

        if (n === 0) {
            z = x;
        } else {
            var fctr = Math.pow(10.0, n);
            var fx = 1.0 * x;
            y = fx * fctr;
            if (flag === "up") {
                z = Math.ceil(y);
            } else {
                z = Math.floor(y);
            }

            var fz = 1.0 * z;
            fz = fz / fctr;
        }

        return fz;
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
 * Find the wavelength where a spectral line profile is half of its minimum,
 * line-centre brightness Assume symmetric (pure Voigt) profile?
 *
 * Returns the integer indices of the red half-power and line-centre wavelengths
 */

var halfPower = function(numPoints, lineFlux) {

    var keyLambdas = [];
    keyLambdas.length = 2;

    // CAUTION; numPoints-1st value holds the line centre monochromatic *continuum* flux for normalization
    // Extract 1D vector of linear continuum normalized fluxes of length numPoints-1
    var flux1D = [];
    flux1D.length = numPoints - 1;

    for (var i = 0; i < numPoints - 1; i++) {
        flux1D[i] = lineFlux[0][i] / lineFlux[0][numPoints - 1];
    }

    // To be on the safe side, let's "rediscover" the line centre of wavelength of minimum brightness:
    var minmax = minMax(flux1D);
    keyLambdas[0] = minmax[0]; // line centre index

    var help = [];
    help.length = numPoints - keyLambdas[0] - 1;
    var half;
    half = flux1D[keyLambdas[0]] + ((1.0 - flux1D[keyLambdas[0]]) / 2.0);
    //System.out.println("HalfPower: half power flux: " + half);

    for (var i = keyLambdas[0]; i < numPoints - 1; i++) {

        // The last minimum of help should be the red half-depth point
        help[i - keyLambdas[0]] = Math.abs(flux1D[i] - half);
        //System.out.println("HalfPower: i, i - keyLambdas[0], fluxiD, help: " 
        //        + i + " " + (i - keyLambdas[0]) + " " + flux1D[i] + " " + help[i - keyLambdas[0]] );
    }

    minmax = minMax(help);
    keyLambdas[1] = minmax[0] + keyLambdas[0]; // red half-power index
    //System.out.println("HalfPower: minmax[0]: " + keyLambdas[1]);

    return keyLambdas;

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

