
/**** Routines for client side post-processing of raw model atmosphere/spectrum
 * synthesis output from server to produce synthetic observables
 */

/**
 * First, reality check raw colours, THEN Run Vega model and subtract off Vega
 * colours for single-point calibration
 */
var UBVRI = function(lambdaScale, flux, numDeps, tauRos, temp) {

    var filters = filterSet();

    var numCols = 7;  //five band combinations in Johnson-Bessell UxBxBVRI: Ux-Bx, B-V, V-R, V-I, R-I, V-K, J-K
    var colors = [];
    colors.length = numCols;

    var numBands = filters.length;
    var numLambdaFilt;

    var bandFlux = [];
    bandFlux.length = numBands;


    // Single-point calibration to Vega:
    // Vega colours computed self-consistntly with GrayFox 1.0 using 
    // Stellar parameters of Castelli, F.; Kurucz, R. L., 1994, A&A, 281, 817
    // Teff = 9550 K, log(g) = 3.95, ([Fe/H] = -0.5 - not directly relevent):

    //var vegaColors = [0.0, 0.0, 0.0, 0.0, 0.0]; //For re-calibrating with raw Vega colours
    // Aug 2015 - with 14-line linelist:
    //var vegaColors = [0.289244, -0.400324, 0.222397, -0.288568, -0.510965];
    //var vegaColors = [0.163003, -0.491341, 0.161940, -0.464265, -0.626204];
    //With Balmer line linear Stark broadening wings:
    //vegaColors = [0.321691, -0.248000, 0.061419, -0.463083, -0.524502];
   var vegaColors = [0.17, -0.09, 0.10, -0.44, -0.54, -3.11, -1.54];//lburns, June 2017
    var deltaLam, newY, product;

    for (var ib = 0; ib < numBands; ib++) {

        bandFlux[ib] = 0.0; //initialization
        numLambdaFilt = filters[ib][0].length;
        //console.log("ib " + ib + " numLambdaFilt " + numLambdaFilt);
        //wavelength loop is over photometric filter data wavelengths

        for (var il = 1; il < numLambdaFilt; il++) {

            //In this case - interpolate model SED onto wavelength grid of given photometric filter data

            deltaLam = filters[ib][0][il] - filters[ib][0][il - 1];  //nm
            //deltaLam = 1.0e-7 * deltaLam;  //cm
            //console.log("ib: " + ib + " il: " + il + " filters[ib][0][il] " + filters[ib][0][il] + " deltaLam: " + deltaLam + " filters[ib][1][il] " + filters[ib][1][il]);

            //hand log flux (row 1) to interpolation routine: 
            newY = interpol(lambdaScale, flux[1], filters[ib][0][il]);
            // linearize interpolated flux: - fluxes add *linearly*
            newY = Math.exp(newY);

            product = filters[ib][1][il] * newY;
            if (ib === 2) {
                //console.log("Photometry: il: " + il + " newY: " + newY + " filterLamb: " + filters[ib][0][il] + " filterTrans: " + filters[ib][1][il] + " product " + product);
            }
            //System.out.println("Photometry: filtertrans: " + filters[ib][1][il] + " product: " + product + " deltaLam: " + deltaLam);
            //Rectangular picket integration
            bandFlux[ib] = bandFlux[ib] + (product * deltaLam);
            //console.log("Photometry: ib: " + ib + " deltaLam " + deltaLam + " bandFlux: " + bandFlux[ib]);

        } //il loop - lambdas
        //console.log("Photometry: ib: " + ib + " bandFlux: " + bandFlux[ib], " product " + product + " deltaLam " + deltaLam);

    }  //ib loop - bands

    var raw;

    // Ux-Bx: 
    raw = 2.5 * logTen(bandFlux[1] / bandFlux[0]);
    colors[0] = raw - vegaColors[0];
    //console.log("U-B: " + colors[0] + " raw " + raw + " bandFlux[1] " + bandFlux[1] + " bandFlux[0] " + bandFlux[0]);

    // B-V:
    raw = 2.5 * logTen(bandFlux[3] / bandFlux[2]);
    colors[1] = raw - vegaColors[1];
    //console.log("B-V: " + colors[1]);

    // V-R:
    raw = 2.5 * logTen(bandFlux[4] / bandFlux[3]);
    colors[2] = raw - vegaColors[2];
    //console.log("V-R: " + colors[2]);

    // V-I:
    raw = 2.5 * logTen(bandFlux[5] / bandFlux[3]);
    colors[3] = raw - vegaColors[3];
    //console.log("V-I: " + colors[3]);

    // R-I:
    raw = 2.5 * logTen(bandFlux[5] / bandFlux[4]);
    colors[4] = raw - vegaColors[4];
    //console.log("R-I: " + colors[4]);

    // V-K: lburns
    raw = 2.5 * logTen(bandFlux[8] / bandFlux[3]);
    colors[5] = raw - vegaColors[5];
    //console.log("V-K: " + colors[5]);

    // J-K: lburns
    raw = 2.5 * logTen(bandFlux[8] / bandFlux[7]);
    colors[6] = raw - vegaColors[6];
    //console.log("J-K: " + colors[6]);


    return colors;

}; //UBVRI

//
//

var iColors = function(lambdaScale, intens, numThetas, numLams) {
    //No! iColors now returns band-integrated intensities

    var filters = filterSet();
    var numCols = 7; //five band combinations in Johnson-Bessell UxBxBVRI: Ux-Bx, B-V, V-R, V-I, R-I, V-K, J-K

    var numBands = filters.length;
    var numLambdaFilt = filters[0][0].length;

    //var colors = [];
    //colors.length = numCols;
    //// Have to use Array constructor here:
    //for (var i = 0; i < numCols; i++) {
    //    colors[i] = new Array(numThetas);
    //}


    var bandIntens = [];
    bandIntens.length = numBands;
    // Have to use Array constructor here:
    for (var i = 0; i < numBands; i++) {
        bandIntens[i] = [];
        bandIntens[i].length = numThetas;
    }


//Unnecessary:
//Note: Calibration must be re-done!  May 2015
// Single-point Johnson UBVRI calibration to Vega:
// Vega colours computed self-consistntly with GrayFox 1.0 using 
// Stellar parameters of Castelli, F.; Kurucz, R. L., 1994, A&A, 281, 817
// Teff = 9550 K, log(g) = 3.95, ([Fe/H] = -0.5 - not directly relevent):
    var vegaColors = [0.163003, -0.491341, 0.161940, -0.464265, -0.626204];

    var deltaLam, newY, product, raw;
    var intensLam = [];
    intensLam.length = numLams;

    //Now same for each intensity spectrum:
    for (var it = 0; it < numThetas; it++) {

//Caution: This loop is over *model SED* lambdas!
        for (var jl = 0; jl < numLams; jl++) {
            intensLam[jl] = intens[jl][it];
            //System.out.println("it: " + it + " jl: " + jl + " intensLam[jl]: " + intensLam[jl]);
        }

        for (var ib = 0; ib < numBands; ib++) {

            bandIntens[ib][it] = 0.0; //initialization

//wavelength loop is over photometric filter data wavelengths

            for (var il = 1; il < numLambdaFilt; il++) {

//In this case - interpolate model SED onto wavelength grid of given photometric filter data

                deltaLam = filters[ib][0][il] - filters[ib][0][il - 1]; //nm
                //deltaLam = 1.0e-7 * deltaLam; //cm
                //hand log flux (row 1) to interpolation routine: 
                newY = interpol(lambdaScale, intensLam, filters[ib][0][il]);
                //System.out.println("Photometry: newFlux: " + newFlux + " filterlamb: " + filters[ib][0][il]);
                product = filters[ib][1][il] * newY;
                //System.out.println("Photometry: filtertrans: " + filters[ib][1][il] + " product: " + product + " deltaLam: " + deltaLam);
                //Rectangular picket integration
                bandIntens[ib][it] = bandIntens[ib][it] + (product * deltaLam);
                //console.log("Photometry: ib: " + ib + " bandIntens: " + bandIntens[ib][it]);
            } //il wavelength loop

//System.out.println("Photometry: ib: " + ib + " it: " + it + " bandIntens: " + bandIntens[ib][it]);
        }  //ib band loop

        //necessary
        //Make the colors! :-)
        //console.log("it: " + it);

        // Ux-Bx: 
        //raw = 2.5 * logTen(bandIntens[1][it] / bandIntens[0][it]);
        //colors[0][it] = raw - vegaColors[0];
        //console.log("U-B: " + colors[0][it]);

        // B-V:
        //raw = 2.5 * logTen(bandIntens[3][it] / bandIntens[2][it]);
        //colors[1][it] = raw - vegaColors[1];
        //console.log("B-V: " + colors[1][it]);

        // V-R:
        //raw = 2.5 * logTen(bandIntens[4][it] / bandIntens[3][it]);
        //colors[2][it] = raw - vegaColors[2];
        //console.log("V-R: " + colors[2][it]);

        // V-I:
        // raw = 2.5 * logTen(bandIntens[5][it] / bandIntens[3][it]);
        //colors[3][it] = raw - vegaColors[3];
        //console.log("V-I: " + colors[3][it]);

        // R-I:
        //raw = 2.5 * logTen(bandIntens[5][it] / bandIntens[4][it]);
        //colors[4][it] = raw - vegaColors[4];
        //console.log("R-I: " + colors[4][it]);
        //necessary

    } //theta it loop

    //return colors;
    return bandIntens;

}; //iColours


//Create area normalized Gaussian appropriate for interpolating onto high resolution wavelength gid
var gaussian = function(lambdaScale, numLams, lambdaIn, sigmaIn, lamUV, lamIR) {
    //No! iColors now returns band-integrated intensities

  //diskSigma = 10.0;  //test
  //diskSigma = 0.01;  //test
//wavelength sampling interval in nm for interpolation 
    var deltaLam = 0.001;  //nm
      var sigma = sigmaIn / deltaLam; //sigma of Gaussian in pixels 
//Number of wavelength elements for Gaussian
       var numSigmas = 2.5; //+/- 2.5 sigmas
       var numGauss = Math.ceil(2.0 * numSigmas * sigma);  //+/- 2.5 sigmas
 //ensure odd number of elements in Gausian kernal
       if ((numGauss % 2) == 0){
          numGauss++;
       }
//Row 0 holds wavelengths in cm
//Row 1 holds Gaussian
       var gauss = [];
       gauss.length = 2;
       gauss[0] = [];
       gauss[0].length = numGauss;
       gauss[1] = [];
       gauss[1].length = numGauss;
       var midPix = Math.floor(numGauss/2);

////Area normalization factors:
//       var rootTwoPi = Math.sqrt(2.0 * Math.PI);
//       var prefac = 1.0 / (sigma * rootTwoPi); 

       var x, expFac;
//Construct Gaussian in pixel space:
       //var sum = 0.0;  //test
       for (var i = 0; i < numGauss; i++){
          x = (i - midPix);
          expFac = x / sigma; 
          expFac = expFac * expFac; 
          gauss[1][i] = Math.exp(-0.5 * expFac); 
          //gauss[i] = prefac * gauss[i];
          //sum+= gauss[i];   //test
          //console.log("i " + i + " gauss[i] " + gauss[i]);  //test
          } 
       //console.log("Gaussian area: " + sum); 

//establish filter lambda scale:
   //var filterLam = [];
   //filterLam.length = numGauss;
   lamStart = lambdaIn - (numSigmas * sigmaIn); //nm
   var ii = 0; 
   for (var i = 0; i < numGauss; i++){
      ii = 1.0 * i;
     // filterLam[i] = lamStart + (ii * deltaLam); //nm
     // filterLam[i] = 1.0e-7 * filterLam[i]; //cm for reference to lambdaScale
      gauss[0][i] = lamStart + (ii * deltaLam); //nm
      gauss[0][i] = 1.0e-7 * gauss[0][i]; //cm for reference to lambdaScale
  //Keep wthin limits of treated SED:
    if (gauss[0][i] < 1.0e-7*lamUV){
      gauss[1][i] = 0.0;
     }  
    if (gauss[0][i] > 1.0e-7*lamIR){
      gauss[1][i] = 0.0;
     }  
   } 
       //for (var i = 0; i < numGauss; i++){
          //console.log("i " + i + " filterLam[i] " + filterLam[i] + " gauss[i] " + gauss[i]);  //test
        //  } 

  //console.log("numGauss " + numGauss + " sigma " + sigma + " lamStart " + lamStart);

    return gauss;

}; //gaussian


//var tuneColor = function(lambdaScale, intens, numThetas, numLams, diskLambda, diskSigma, lamUV, lamIR) {
var tuneColor = function(lambdaScale, intens, numThetas, numLams, gaussian, lamUV, lamIR) {
    //No! iColors now returns band-integrated intensities

  //diskSigma = 10.0;  //test
  //diskSigma = 0.01;  //test
//wavelength sampling interval in nm for interpolation 

    var numGauss = gaussian[0].length;
    var deltaLam = gaussian[0][1] - gaussian[0][0];

    var bandIntens = [];
    bandIntens.length = numThetas;

    var product;
    var intensLam = [];
    intensLam.length = numLams;

    //Now same for each intensity spectrum:
    for (var it = 0; it < numThetas; it++) {

//Caution: This loop is over *model SED* lambdas!
        for (var jl = 0; jl < numLams; jl++) {
            intensLam[jl] = intens[jl][it];
            //System.out.println("it: " + it + " jl: " + jl + " intensLam[jl]: " + intensLam[jl]);
        }

    //    for (var ib = 0; ib < numBands; ib++) {

            bandIntens[it] = 0.0; //initialization
            //var newY = interpolV(intensLam, lambdaScale, filterLam);
            var newY = interpolV(intensLam, lambdaScale, gaussian[0]);

//wavelength loop is over photometric filter data wavelengths

            for (var il = 1; il < numGauss; il++) {

//In this case - interpolate model SED onto wavelength grid of tunable filter 

                //product = gauss[il] * newY[il];
                product = gaussian[1][il] * newY[il];
                //Rectangular picket integration
                bandIntens[it] = bandIntens[it] + (product * deltaLam);
                //console.log("Photometry: ib: " + ib + " bandIntens: " + bandIntens[ib][it]);
            } //il wavelength loop

  //console.log("tuneColor: it: " + it + " bandIntens: " + bandIntens[it]);
     //   }  //ib band loop

    } //theta it loop

    //return colors;
    return bandIntens;

}; //tuneColor

//
//

var filterSet = function() {

    var numBands = 9; // Bessell-Johnson UxBxBVRI
    var numLambdaFilt = 25; //test for now

    //double[][][] filterCurves = new double[numBands][2][numLambdaFilt];

    var filterCurves = [];
    filterCurves.length = numBands;
    // Have to use Array constructor here:
    for (var i = 0; i < numBands; i++) {
        filterCurves[i] = [];
        filterCurves[i].length = 2;
    }
    // Have to use Array constructor here:
    for (var i = 0; i < numBands; i++) {
        filterCurves[i][0] = [];
        filterCurves[i][1] = [];
        filterCurves[i][0].length = numLambdaFilt;
        filterCurves[i][1].length = numLambdaFilt;
    }

    //Initialize all filterCurves - the real data below won't fill in all the array elements:
    for (var ib = 0; ib < numBands; ib++) {
        for (var il = 0; il < numLambdaFilt; il++) {
            filterCurves[ib][0][il] = 1000.0; //placeholder wavelength (nm)
            filterCurves[ib][1][il] = 0.0e0; // initialize filter transparency to 0.0
        }
    }

    //http://ulisse.pd.astro.it/Astro/ADPS/Systems/Sys_136/index_136.html
//Bessell, M. S., 1990, PASP, 102, 1181
//photometric filter data for Bessell UxBxBVRI system from Asiago database in Java & JavaScript syntax
//Individual bands are below master table
//        UX
    filterCurves[0][0][0] = 300.0;
    filterCurves[0][1][0] = 0.000;
    filterCurves[0][0][1] = 305.0;
    filterCurves[0][1][1] = 0.016;
    filterCurves[0][0][2] = 310.0;
    filterCurves[0][1][2] = 0.068;
    filterCurves[0][0][3] = 315.0;
    filterCurves[0][1][3] = 0.167;
    filterCurves[0][0][4] = 320.0;
    filterCurves[0][1][4] = 0.287;
    filterCurves[0][0][5] = 325.0;
    filterCurves[0][1][5] = 0.423;
    filterCurves[0][0][6] = 330.0;
    filterCurves[0][1][6] = 0.560;
    filterCurves[0][0][7] = 335.0;
    filterCurves[0][1][7] = 0.673;
    filterCurves[0][0][8] = 340.0;
    filterCurves[0][1][8] = 0.772;
    filterCurves[0][0][9] = 345.0;
    filterCurves[0][1][9] = 0.841;
    filterCurves[0][0][10] = 350.0;
    filterCurves[0][1][10] = 0.905;
    filterCurves[0][0][11] = 355.0;
    filterCurves[0][1][11] = 0.943;
    filterCurves[0][0][12] = 360.0;
    filterCurves[0][1][12] = 0.981;
    filterCurves[0][0][13] = 365.0;
    filterCurves[0][1][13] = 0.993;
    filterCurves[0][0][14] = 370.0;
    filterCurves[0][1][14] = 1.000;
    filterCurves[0][0][15] = 375.0;
    filterCurves[0][1][15] = 0.989;
    filterCurves[0][0][16] = 380.0;
    filterCurves[0][1][16] = 0.916;
    filterCurves[0][0][17] = 385.0;
    filterCurves[0][1][17] = 0.804;
    filterCurves[0][0][18] = 390.0;
    filterCurves[0][1][18] = 0.625;
    filterCurves[0][0][19] = 395.0;
    filterCurves[0][1][19] = 0.423;
    filterCurves[0][0][20] = 400.0;
    filterCurves[0][1][20] = 0.238;
    filterCurves[0][0][21] = 405.0;
    filterCurves[0][1][21] = 0.114;
    filterCurves[0][0][22] = 410.0;
    filterCurves[0][1][22] = 0.051;
    filterCurves[0][0][23] = 415.0;
    filterCurves[0][1][23] = 0.019;
    filterCurves[0][0][24] = 420.0;
    filterCurves[0][1][24] = 0.000;
//BX
    filterCurves[1][0][0] = 360.0;
    filterCurves[1][1][0] = 0.000;
    filterCurves[1][0][1] = 370.0;
    filterCurves[1][1][1] = 0.026;
    filterCurves[1][0][2] = 380.0;
    filterCurves[1][1][2] = 0.120;
    filterCurves[1][0][3] = 390.0;
    filterCurves[1][1][3] = 0.523;
    filterCurves[1][0][4] = 400.0;
    filterCurves[1][1][4] = 0.875;
    filterCurves[1][0][5] = 410.0;
    filterCurves[1][1][5] = 0.956;
    filterCurves[1][0][6] = 420.0;
    filterCurves[1][1][6] = 1.000;
    filterCurves[1][0][7] = 430.0;
    filterCurves[1][1][7] = 0.998;
    filterCurves[1][0][8] = 440.0;
    filterCurves[1][1][8] = 0.972;
    filterCurves[1][0][9] = 450.0;
    filterCurves[1][1][9] = 0.901;
    filterCurves[1][0][10] = 460.0;
    filterCurves[1][1][10] = 0.793;
    filterCurves[1][0][11] = 470.0;
    filterCurves[1][1][11] = 0.694;
    filterCurves[1][0][12] = 480.0;
    filterCurves[1][1][12] = 0.587;
    filterCurves[1][0][13] = 490.0;
    filterCurves[1][1][13] = 0.470;
    filterCurves[1][0][14] = 500.0;
    filterCurves[1][1][14] = 0.362;
    filterCurves[1][0][15] = 510.0;
    filterCurves[1][1][15] = 0.263;
    filterCurves[1][0][16] = 520.0;
    filterCurves[1][1][16] = 0.169;
    filterCurves[1][0][17] = 530.0;
    filterCurves[1][1][17] = 0.107;
    filterCurves[1][0][18] = 540.0;
    filterCurves[1][1][18] = 0.049;
    filterCurves[1][0][19] = 550.0;
    filterCurves[1][1][19] = 0.010;
    filterCurves[1][0][20] = 560.0;
    filterCurves[1][1][20] = 0.000;
    filterCurves[1][0][21] = 560.0;
    filterCurves[1][1][21] = 0.000;
    filterCurves[1][0][22] = 560.0;
    filterCurves[1][1][22] = 0.000;
    filterCurves[1][0][23] = 560.0;
    filterCurves[1][1][23] = 0.000;
    filterCurves[1][0][24] = 560.0;
    filterCurves[1][1][24] = 0.000;
//B
    filterCurves[2][0][0] = 360.0;
    filterCurves[2][1][0] = 0.000;
    filterCurves[2][0][1] = 370.0;
    filterCurves[2][1][1] = 0.030;
    filterCurves[2][0][2] = 380.0;
    filterCurves[2][1][2] = 0.134;
    filterCurves[2][0][3] = 390.0;
    filterCurves[2][1][3] = 0.567;
    filterCurves[2][0][4] = 400.0;
    filterCurves[2][1][4] = 0.920;
    filterCurves[2][0][5] = 410.0;
    filterCurves[2][1][5] = 0.978;
    filterCurves[2][0][6] = 420.0;
    filterCurves[2][1][6] = 1.000;
    filterCurves[2][0][7] = 430.0;
    filterCurves[2][1][7] = 0.978;
    filterCurves[2][0][8] = 440.0;
    filterCurves[2][1][8] = 0.935;
    filterCurves[2][0][9] = 450.0;
    filterCurves[2][1][9] = 0.853;
    filterCurves[2][0][10] = 460.0;
    filterCurves[2][1][10] = 0.740;
    filterCurves[2][0][11] = 470.0;
    filterCurves[2][1][11] = 0.640;
    filterCurves[2][0][12] = 480.0;
    filterCurves[2][1][12] = 0.536;
    filterCurves[2][0][13] = 490.0;
    filterCurves[2][1][13] = 0.424;
    filterCurves[2][0][14] = 500.0;
    filterCurves[2][1][14] = 0.325;
    filterCurves[2][0][15] = 510.0;
    filterCurves[2][1][15] = 0.235;
    filterCurves[2][0][16] = 520.0;
    filterCurves[2][1][16] = 0.150;
    filterCurves[2][0][17] = 530.0;
    filterCurves[2][1][17] = 0.095;
    filterCurves[2][0][18] = 540.0;
    filterCurves[2][1][18] = 0.043;
    filterCurves[2][0][19] = 550.0;
    filterCurves[2][1][19] = 0.009;
    filterCurves[2][0][20] = 560.0;
    filterCurves[2][1][20] = 0.000;
    filterCurves[2][0][21] = 560.0;
    filterCurves[2][1][21] = 0.000;
    filterCurves[2][0][22] = 560.0;
    filterCurves[2][1][22] = 0.000;
    filterCurves[2][0][23] = 560.0;
    filterCurves[2][1][23] = 0.000;
    filterCurves[2][0][24] = 560.0;
    filterCurves[2][1][24] = 0.000;
//V
    filterCurves[3][0][0] = 470.0;
    filterCurves[3][1][0] = 0.000;
    filterCurves[3][0][1] = 480.0;
    filterCurves[3][1][1] = 0.030;
    filterCurves[3][0][2] = 490.0;
    filterCurves[3][1][2] = 0.163;
    filterCurves[3][0][3] = 500.0;
    filterCurves[3][1][3] = 0.458;
    filterCurves[3][0][4] = 510.0;
    filterCurves[3][1][4] = 0.780;
    filterCurves[3][0][5] = 520.0;
    filterCurves[3][1][5] = 0.967;
    filterCurves[3][0][6] = 530.0;
    filterCurves[3][1][6] = 1.000;
    filterCurves[3][0][7] = 540.0;
    filterCurves[3][1][7] = 0.973;
    filterCurves[3][0][8] = 550.0;
    filterCurves[3][1][8] = 0.898;
    filterCurves[3][0][9] = 560.0;
    filterCurves[3][1][9] = 0.792;
    filterCurves[3][0][10] = 570.0;
    filterCurves[3][1][10] = 0.684;
    filterCurves[3][0][11] = 580.0;
    filterCurves[3][1][11] = 0.574;
    filterCurves[3][0][12] = 590.0;
    filterCurves[3][1][12] = 0.461;
    filterCurves[3][0][13] = 600.0;
    filterCurves[3][1][13] = 0.359;
    filterCurves[3][0][14] = 610.0;
    filterCurves[3][1][14] = 0.270;
    filterCurves[3][0][15] = 620.0;
    filterCurves[3][1][15] = 0.197;
    filterCurves[3][0][16] = 630.0;
    filterCurves[3][1][16] = 0.135;
    filterCurves[3][0][17] = 640.0;
    filterCurves[3][1][17] = 0.081;
    filterCurves[3][0][18] = 650.0;
    filterCurves[3][1][18] = 0.045;
    filterCurves[3][0][19] = 660.0;
    filterCurves[3][1][19] = 0.025;
    filterCurves[3][0][20] = 670.0;
    filterCurves[3][1][20] = 0.017;
    filterCurves[3][0][21] = 680.0;
    filterCurves[3][1][21] = 0.013;
    filterCurves[3][0][22] = 690.0;
    filterCurves[3][1][22] = 0.009;
    filterCurves[3][0][23] = 700.0;
    filterCurves[3][1][23] = 0.000;
    filterCurves[3][0][24] = 700.0;
    filterCurves[3][1][24] = 0.000;
//R
    filterCurves[4][0][0] = 550.0;
    filterCurves[4][1][0] = 0.00;
    filterCurves[4][0][1] = 560.0;
    filterCurves[4][1][1] = 0.23;
    filterCurves[4][0][2] = 570.0;
    filterCurves[4][1][2] = 0.74;
    filterCurves[4][0][3] = 580.0;
    filterCurves[4][1][3] = 0.91;
    filterCurves[4][0][4] = 590.0;
    filterCurves[4][1][4] = 0.98;
    filterCurves[4][0][5] = 600.0;
    filterCurves[4][1][5] = 1.00;
    filterCurves[4][0][6] = 610.0;
    filterCurves[4][1][6] = 0.98;
    filterCurves[4][0][7] = 620.0;
    filterCurves[4][1][7] = 0.96;
    filterCurves[4][0][8] = 630.0;
    filterCurves[4][1][8] = 0.93;
    filterCurves[4][0][9] = 640.0;
    filterCurves[4][1][9] = 0.90;
    filterCurves[4][0][10] = 650.0;
    filterCurves[4][1][10] = 0.86;
    filterCurves[4][0][11] = 660.0;
    filterCurves[4][1][11] = 0.81;
    filterCurves[4][0][12] = 670.0;
    filterCurves[4][1][12] = 0.78;
    filterCurves[4][0][13] = 680.0;
    filterCurves[4][1][13] = 0.72;
    filterCurves[4][0][14] = 690.0;
    filterCurves[4][1][14] = 0.67;
    filterCurves[4][0][15] = 700.0;
    filterCurves[4][1][15] = 0.61;
    filterCurves[4][0][16] = 710.0;
    filterCurves[4][1][16] = 0.56;
    filterCurves[4][0][17] = 720.0;
    filterCurves[4][1][17] = 0.51;
    filterCurves[4][0][18] = 730.0;
    filterCurves[4][1][18] = 0.46;
    filterCurves[4][0][19] = 740.0;
    filterCurves[4][1][19] = 0.40;
    filterCurves[4][0][20] = 750.0;
    filterCurves[4][1][20] = 0.35;
    filterCurves[4][0][21] = 800.0;
    filterCurves[4][1][21] = 0.14;
    filterCurves[4][0][22] = 850.0;
    filterCurves[4][1][22] = 0.03;
    filterCurves[4][0][23] = 900.0;
    filterCurves[4][1][23] = 0.00;
    filterCurves[4][0][24] = 900.0;
    filterCurves[4][1][24] = 0.000;

//I
    filterCurves[5][0][0] = 700.0;
    filterCurves[5][1][0] = 0.000;
    filterCurves[5][0][1] = 710.0;
    filterCurves[5][1][1] = 0.024;
    filterCurves[5][0][2] = 720.0;
    filterCurves[5][1][2] = 0.232;
    filterCurves[5][0][3] = 730.0;
    filterCurves[5][1][3] = 0.555;
    filterCurves[5][0][4] = 740.0;
    filterCurves[5][1][4] = 0.785;
    filterCurves[5][0][5] = 750.0;
    filterCurves[5][1][5] = 0.910;
    filterCurves[5][0][6] = 760.0;
    filterCurves[5][1][6] = 0.965;
    filterCurves[5][0][7] = 770.0;
    filterCurves[5][1][7] = 0.985;
    filterCurves[5][0][8] = 780.0;
    filterCurves[5][1][8] = 0.990;
    filterCurves[5][0][9] = 790.0;
    filterCurves[5][1][9] = 0.995;
    filterCurves[5][0][10] = 800.0;
    filterCurves[5][1][10] = 1.000;
    filterCurves[5][0][11] = 810.0;
    filterCurves[5][1][11] = 1.000;
    filterCurves[5][0][12] = 820.0;
    filterCurves[5][1][12] = 0.990;
    filterCurves[5][0][13] = 830.0;
    filterCurves[5][1][13] = 0.980;
    filterCurves[5][0][14] = 840.0;
    filterCurves[5][1][14] = 0.950;
    filterCurves[5][0][15] = 850.0;
    filterCurves[5][1][15] = 0.910;
    filterCurves[5][0][16] = 860.0;
    filterCurves[5][1][16] = 0.860;
    filterCurves[5][0][17] = 870.0;
    filterCurves[5][1][17] = 0.750;
    filterCurves[5][0][18] = 880.0;
    filterCurves[5][1][18] = 0.560;
    filterCurves[5][0][19] = 890.0;
    filterCurves[5][1][19] = 0.330;
    filterCurves[5][0][20] = 900.0;
    filterCurves[5][1][20] = 0.150;
    filterCurves[5][0][21] = 910.0;
    filterCurves[5][1][21] = 0.030;
    filterCurves[5][0][22] = 920.0;
    filterCurves[5][1][22] = 0.000;
    filterCurves[5][0][23] = 920.0;
    filterCurves[5][1][23] = 0.000;
    filterCurves[5][0][24] = 920.0;
    filterCurves[5][1][24] = 0.000;
//H lburns /06
    filterCurves[6][0][0] = 1460;
    filterCurves[6][1][0] = 0.000;
    filterCurves[6][0][1] = 1480;
    filterCurves[6][1][1] = 0.150;
    filterCurves[6][0][2] = 1500;
    filterCurves[6][1][2] = 0.440;
    filterCurves[6][0][3] = 1520;
    filterCurves[6][1][3] = 0.860;
    filterCurves[6][0][4] = 1540;
    filterCurves[6][1][4] = 0.940;
    filterCurves[6][0][5] = 1550;
    filterCurves[6][1][5] = 0.960;
    filterCurves[6][0][6] = 1560;
    filterCurves[6][1][6] = 0.980;
    filterCurves[6][0][7] = 1580;
    filterCurves[6][1][7] = 0.950;
    filterCurves[6][0][8] = 1600;
    filterCurves[6][1][8] = 0.990;
    filterCurves[6][0][9] = 1610;
    filterCurves[6][1][9] = 0.990;
    filterCurves[6][0][10] = 1620;
    filterCurves[6][1][10] = 0.990;
    filterCurves[6][0][11] = 1640;
    filterCurves[6][1][11] = 0.990;
    filterCurves[6][0][12] = 1660;
    filterCurves[6][1][12] = 0.990;
    filterCurves[6][0][13] = 1670;
    filterCurves[6][1][13] = 0.990;
    filterCurves[6][0][14] = 1680;
    filterCurves[6][1][14] = 0.990;
    filterCurves[6][0][15] = 1690;
    filterCurves[6][1][15] = 0.990;
    filterCurves[6][0][16] = 1700;
    filterCurves[6][1][16] = 0.990;
    filterCurves[6][0][17] = 1710;
    filterCurves[6][1][17] = 0.970;
    filterCurves[6][0][18] = 1720;
    filterCurves[6][1][18] = 0.950;
    filterCurves[6][0][19] = 1740;
    filterCurves[6][1][19] = 0.870;
    filterCurves[6][0][20] = 1760;
    filterCurves[6][1][20] = 0.840;
    filterCurves[6][0][21] = 1780;
    filterCurves[6][1][21] = 0.710;
    filterCurves[6][0][22] = 1800;
    filterCurves[6][1][22] = 0.520;
    filterCurves[6][0][23] = 1820;
    filterCurves[6][1][23] = 0.020;
    filterCurves[6][0][24] = 1840;
    filterCurves[6][1][24] = 0.000;
//J lburns /06
    filterCurves[7][0][0] = 1040;
    filterCurves[7][1][0] = 0.000;
    filterCurves[7][0][1] = 1060;
    filterCurves[7][1][1] = 0.020;
    filterCurves[7][0][2] = 1080;
    filterCurves[7][1][2] = 0.110;
    filterCurves[7][0][3] = 1100;
    filterCurves[7][1][3] = 0.420;
    filterCurves[7][0][4] = 1120;
    filterCurves[7][1][4] = 0.320;
    filterCurves[7][0][5] = 1140;
    filterCurves[7][1][5] = 0.470;
    filterCurves[7][0][6] = 1160;
    filterCurves[7][1][6] = 0.630;
    filterCurves[7][0][7] = 1180;
    filterCurves[7][1][7] = 0.730;
    filterCurves[7][0][8] = 1190;
    filterCurves[7][1][8] = 0.750;
    filterCurves[7][0][9] = 1200;
    filterCurves[7][1][9] = 0.770;
    filterCurves[7][0][10] = 1210;
    filterCurves[7][1][10] = 0.790;
    filterCurves[7][0][11] = 1220;
    filterCurves[7][1][11] = 0.810;
    filterCurves[7][0][12] = 1230;
    filterCurves[7][1][12] = 0.820;
    filterCurves[7][0][13] = 1240;
    filterCurves[7][1][13] = 0.830;
    filterCurves[7][0][14] = 1250;
    filterCurves[7][1][14] = 0.850;
    filterCurves[7][0][15] = 1260;
    filterCurves[7][1][15] = 0.880;
    filterCurves[7][0][16] = 1280;
    filterCurves[7][1][16] = 0.940;
    filterCurves[7][0][17] = 1300;
    filterCurves[7][1][17] = 0.910;
    filterCurves[7][0][18] = 1320;
    filterCurves[7][1][18] = 0.790;
    filterCurves[7][0][19] = 1340;
    filterCurves[7][1][19] = 0.680;
    filterCurves[7][0][20] = 1360;
    filterCurves[7][1][20] = 0.040;
    filterCurves[7][0][21] = 1380;
    filterCurves[7][1][21] = 0.110;
    filterCurves[7][0][22] = 1400;
    filterCurves[7][1][22] = 0.070;
    filterCurves[7][0][23] = 1420;
    filterCurves[7][1][23] = 0.030;
    filterCurves[7][0][24] = 1440;
    filterCurves[7][1][24] = 0.000;


//K lburns /06  
    filterCurves[8][0][0] = 1940;
    filterCurves[8][1][0] = 0.000;
    filterCurves[8][0][1] = 1960;
    filterCurves[8][1][1] = 0.120;
    filterCurves[8][0][2] = 1980;
    filterCurves[8][1][2] = 0.200;
    filterCurves[8][0][3] = 2000;
    filterCurves[8][1][3] = 0.300;
    filterCurves[8][0][4] = 2020;
    filterCurves[8][1][4] = 0.550;
    filterCurves[8][0][5] = 2040;
    filterCurves[8][1][5] = 0.740;
    filterCurves[8][0][6] = 2060;
    filterCurves[8][1][6] = 0.550;
    filterCurves[8][0][7] = 2080;
    filterCurves[8][1][7] = 0.770;
    filterCurves[8][0][8] = 2100;
    filterCurves[8][1][8] = 0.850;
    filterCurves[8][0][9] = 2120;
    filterCurves[8][1][9] = 0.900;
    filterCurves[8][0][10] = 2140;
    filterCurves[8][1][10] = 0.940;
    filterCurves[8][0][11] = 2160;
    filterCurves[8][1][11] = 0.940;
    filterCurves[8][0][12] = 2180;
    filterCurves[8][1][12] = 0.950;
    filterCurves[8][0][13] = 2200;
    filterCurves[8][1][13] = 0.940;
    filterCurves[8][0][14] = 2220;
    filterCurves[8][1][14] = 0.960;
    filterCurves[8][0][15] = 2240;
    filterCurves[8][1][15] = 0.980;
    filterCurves[8][0][16] = 2260;
    filterCurves[8][1][16] = 0.970;
    filterCurves[8][0][17] = 2280;
    filterCurves[8][1][17] = 0.960;
    filterCurves[8][0][18] = 2300;
    filterCurves[8][1][18] = 0.910;
    filterCurves[8][0][19] = 2320;
    filterCurves[8][1][19] = 0.880;
    filterCurves[8][0][20] = 2340;
    filterCurves[8][1][20] = 0.840;
    filterCurves[8][0][21] = 2380;
    filterCurves[8][1][21] = 0.750;
    filterCurves[8][0][22] = 2400;
    filterCurves[8][1][22] = 0.640;
    filterCurves[8][0][23] = 2440;
    filterCurves[8][1][23] = 0.010;
    filterCurves[8][0][24] = 2480;
    filterCurves[8][1][24] = 0.000;

    //
    //Check that we set up the array corectly:
//    for (var ib = 0; ib < numBands; ib++) {
//    var ib = 0;
//    for (var il = 0; il < numLambdaFilt; il++) {
//        console.log("ib: " + ib + " il: " + il + " filterCurves[ib][0][il]: " + filterCurves[0][0][il]);
//       console.log("ib: " + ib + " il: " + il + " filterCurves[ib][1][il]: " + filterCurves[0][1][il]);
//   }
//    }

    for (var ib = 0; ib < numBands; ib++) {
//wavelength loop is over photometric filter data wavelengths
        for (var il = 0; il < numLambdaFilt; il++) {
            filterCurves[ib][0][il] = filterCurves[ib][0][il] * 1.0e-7; // nm to cm
        }
    }


    return filterCurves;
}; //filterSet


/* In case it's ever needed again...
//General convolution method
// ***** Function to be convolved and kernel function are expected to *already* be 
// interpolated onto same abssica grid!
//
   var convol = function(x, yFunction, kernel) {

      var ySize = yFunction.length;
      var kernelSize = kernel.length;
      var halfKernelSize = Math.ceil(kernelSize / 2);
  
      var yFuncConv = [];
      yFuncConv.length = ySize;
      var deltaX;

//First kernelSize/2 elements of yFunction cannot be convolved
      for (var i = 0; i < halfKernelSize; i++){
         yFuncConv[i] = yFunction[i];
         //console.log("Part 1: i " + i + " yFuncConv[i] " + yFuncConv[i]);
        }
//Convolution:
//We are effectively integrating in pixel space, not physical wavelength space, so deltaX is always unity - ??
// Conserves power if kernel area-normalized - ??
      var offset = 0; //initialization
      for (var i = halfKernelSize; i < ySize - (halfKernelSize); i++){
         var accum = 0; //accumulator
         for (var j = 0; j < kernelSize; j++){ 
            //console.log("Part 2: i " + i + " j " + j + " offset " + offset);
            //deltaX = x[j] - x[j-1]; 
            //console.log("x[j] " + x[j] + " x[j-1] " + x[j-1] + " deltaX " + deltaX 
            // + " yFunction[j+offset] " + yFunction[j+offset] + " kernel[j] " + kernel[j]);
            accum = accum + ( (kernel[j] * yFunction[j+offset]) ); //* deltaX ); 
         }  //inner loop, j	
         yFuncConv[i] = accum;
         //console.log("yFuncConv[i] " + yFuncConv[i]);
         offset++;
       } //outer loop, i
//Last kernelSize/2 elements of yFunction cannot be convolved
      for (var i = (ySize - halfKernelSize - 1); i < ySize; i++){
         yFuncConv[i] = yFunction[i];
       }

  return yFuncConv; 

  }; //end method convol

*/   
/**
 *
 * THIS VERSION works with spectrum synthesis output returned from server
 * in GrayStarServer
 *
 * It will try to return the equivalenth width of EVERYTHING in the synthesis region
 * as one value!  Isolate the synthesis region to a single line to a clean result
 * for that line!
 *
 * Compute the equivalent width of the Voigt line in pm - picometers NOTE: The
 * input parameter 'flux' should be a 2 x (numPoints+1) array where the
 * numPoints+1st value is the line centre monochromatic Continuum flux
 */
var eqWidthSynth = function(flux, linePoints) { //, fluxCont) {

    var logE = logTen(Math.E); // for debug output
    var Wlambda = 0.0; // Equivalent width in pm - picometers
    var numPoints = linePoints.length;
    //console.log("numPoints " + numPoints);
    var delta, logDelta, term, integ, integ2, logInteg, lastInteg, lastTerm, term2;

//Spectrum now continuum rectified before eqWidth called

    //Trapezoid rule:
    // First integrand:

    lastInteg = 1.0 - flux[0][0];
    
    lastTerm = lastInteg; //initialization

    for (var il = 1; il < numPoints-1; il++) {

        delta = linePoints[il] - linePoints[il - 1];
        delta = delta * 1.0E+7;  // cm to nm - W_lambda in pm
        logDelta = Math.log(delta);

        integ = 1.0 - flux[0][il];

        //Extended trapezoid rule:
        integ2 = 0.5 * (lastInteg + integ);
        //logInteg = Math.log(integ2);
        //term = Math.exp(logInteg + logDelta);
        term = integ2 * delta;
        //console.log("linePoints[il] " + linePoints[il] + " flux[0][il] " + flux[0][il]
//  + " integ " + integ + " term " + term);

        //Wlambda = Wlambda + (term * delta);
        Wlambda = Wlambda + term;

        lastInteg = integ;

        //System.out.println("EqWidth: Wlambda: " + Wlambda);
    }

    // Convert area in nm to pm - picometers
    Wlambda = Wlambda * 1.0E3;

    return Wlambda;

};


 //Discrete cosine and sine Fourier transform of input narrow-band intensity profile
 //
 // We will interpret theta/(theta/2) with respect to the local surface normal of the star to
 // be the spatial domain "x" coordinate - this is INDEPENDENT of the distance to, and linear
 // radius of, the star! :-)
 var fourier = function(numThetas, cosTheta, filtIntens){

  var pi = Math.PI; //a handy enough wee quantity
  var halfPi = pi / 2.0;

    //number of sample points in full intensity profile I(theta), theta = -pi/2 to pi/2 RAD:
      var numX0 = 2 * numThetas - 1;

//We have as input the itnesity half-profile I(cos(theta)), cos(theta) = 1 to 0
 //create the doubled root-intensity profile sqrt(I(theta/halfPi)), theta/halfPi = -1 to 1
 //this approach assumes the real (cosine) and imaginary (sine) components are in phase
     var rootIntens2 = [];
     var x0 = [];
     rootIntens2.length = numX0;
     x0.length = numX0;
     var normIntens;
//negative x domain of doubled profile:
     var j = 0;
     for (var i = numThetas-1; i >=0; i--){
        x0[j] = -1.0*Math.acos(cosTheta[1][i]) / halfPi;
        normIntens = filtIntens[i] / filtIntens[0]; //normalize
        rootIntens2[j] = Math.sqrt(normIntens);
        //console.log("i " + i + " cosTheta " + cosTheta[1][i] + " filtIntens " + filtIntens[i] + " normIntens " + normIntens
        //  + " j " + j + " x0 " + x0[j] + " rootIntens2 " + rootIntens2[j] );
        j++;
     }
//positive x domain of doubled profile:
     for (var i = numThetas; i < numX0; i++){
        j = i - (numThetas-1);
        x0[i] = Math.acos(cosTheta[1][j]) / halfPi;
        normIntens = filtIntens[j] / filtIntens[0]; //normalize
        //rootIntens2[i] = Math.sqrt(normIntens);
        rootIntens2[i] = normIntens;
        //console.log("j " + j + " cosTheta " + cosTheta[1][j] + " filtIntens " + filtIntens[j] + " normIntens " + normIntens
        //  + " i " + i + " x0 " + x0[i] + " rootIntens2 " + rootIntens2[i] );
     }

//create the uniformly sampled spatial domain ("x") and the complementary
//spatial frequecy domain "k" domain
//
//We're interpreting theta/halfPi with respect to local surface normal at surface
//of star as the spatial domain, "x"
  var minX = -2.0;
  var maxX = 1.0;
  var numX = 100;  //(is also "numK" - ??)
  var deltaX = (maxX - minX) / numX;

//Complentary limits in "k" domain; k = 2pi/lambda (radians)
//  - lowest k value corresponds to one half spatial wavelength (lambda) = 2 (ie. 1.0 - (-1.0)):
  var maxLambda = 2.0 * 2.0;
  // stupid?? var minK = 2.0 * pi / maxLambda;  //(I know, I know, but let's keep this easy for the human reader)
//  - highest k value has to do with number of points sampling x:  Try Nyquist sampling rate of
//     two x points per lambda
  var minLambda = 8.0 * 2.0 * deltaX;
  var maxK = 2.0 * pi / minLambda; //"right-going" waves
  var minK = -1.0 * maxK;  //"left-going" waves
  var deltaK = (maxK - minK) / numX;
 // console.log("maxK " + maxK + " minK " + minK + " deltaK " + deltaK);


  var x = [];
  var k = [];
  x.length = numX;
  k.length = numX;
  var ii;
  for (var i = 0; i < numX; i++){
     ii = 1.0 * i;
     x[i] = minX + ii*deltaX;
     k[i] = minK + ii*deltaK;
    // console.log("i " + i + " x " + x[i] + " k " + k[i]);
  }

//Interpolate the rootIntens2(theta/halfpi) signal onto uniform spatial sampling:
  //doesn't work: var rootIntens3 = interpolV(rootIntens2, x0, x);
  var rootIntens3 = [];
  rootIntens3.length = numX;
  for (var i = 0; i < numX; i++){
     rootIntens3[i] = interpol(x0, rootIntens2, x[i]);
     //console.log("i " + i + " x " + x[i] + " rootIntens3 " + rootIntens3[i]);
  }

//returned variable ft:
//  Row 0: wavenumber, spatial frequency, k (radians)
//  Row 1: cosine transform (real component)
//  Row 2: sine transform (imaginary component
      var ft = [];
      ft.length = 3;
      ft[0] = [];
      ft[1] = [];
      ft[2] = [];
      ft[0].length = numX-1;
      ft[1].length = numX-1;
      ft[2].length = numX-1;

 var argument, rootFt;
 //numXFloat = 1.0 * numX;
//Outer loop is over the elements of vector holding the power at each frequency "k"
    for (var ik = 0; ik < numX-1; ik++){
//intiialize ft
       ft[0][ik] = k[ik];
       rootFtCos = 0.0;
       rootFtSin = 0.0;
       ft[1][ik] = 0.0;
       ft[2][ik] = 0.0;
//Inner llop is cumulative summation over spatial positions "x" - the Fourier cosine and sine series
       for (var ix = 0; ix < numX-1; ix++){
         //ixFloat = 1.0 * ix;
         argument = -1.0 * k[ik] * x[ix];
         //console.log("ik " + ik + " ix " + ix + " argument " + argument + " x " + x[ix] + " rootIntens3 " + rootIntens3[ix]);
         // cosine series:
         rootFtCos = rootFtCos + rootIntens3[ix] * Math.cos(argument);
         // sine series:
         rootFtSin = rootFtSin + rootIntens3[ix] * Math.sin(argument);
       } //ix loop
         ft[1][ik] = rootFtCos; // * rootFtCos; //Power
         ft[2][ik] = rootFtSin; // * rootFtSin;
         //console.log("ik " + ik + " k " + k[ik] + " ft[1] " + ft[1][ik]);
    } //ik loop

      return ft;

   }; //end method fourier

