/*
 *  * To change this license header, choose License Headers in Project Properties.
 *   * To change this template file, choose Tools | Templates
 *    * and open the template in the editor.
 *     */

package chromastarserver;

/* Metal b-f opacity routines taken from Moog (moogjul2014/, MOOGJUL2014.tar)
Chris Sneden (Universtiy of Texas at Austin)  and collaborators
http://www.as.utexas.edu/~chris/moog.html
//From Moog source file Opacitymetals.f
*/

public class KappasMetal {


/*
c******************************************************************************
c  The subroutines needed to calculate the Mg I, Mg II, Al I, Si I, Si II,
c  and Fe I b-f opacities are in this file.  These are from ATLAS9.
c******************************************************************************
*/

      public static double[][] masterMetal(int numDeps, int numLams, double[][] temp, double[] lambdaScale, double[][][] stagePops){

    //System.out.println("masterMetal called...");

//From Moog source file Opacitymetals.f
// From how values such as aC1[] are used in Moog file Opacit.f to compute the total opacity
// and then the optical depth scale, I infer that they are extinction coefficients 
// in cm^-1 
// There does not seem to be any correction for stimulated emission 

         double logE = Math.log10(Math.E);

         double[][] masterBF = new double[numLams][numDeps];

         double[] logUC1 = new double[2];
         double[] logUMg1 = new double[2];
         double[] logUMg2 = new double[2];
         double[] logUAl1 = new double[2];
         double[] logUSi1 = new double[2];
         double[] logUSi2 = new double[2];
         double[] logUFe1 = new double[2];

         double logStatWC1 = 0.0;
         double logStatWMg1 = 0.0;
         double logStatWMg2 = 0.0;
         double logStatWAl1 = 0.0;
         double logStatWSi1 = 0.0;
         double logStatWSi2 = 0.0;
         double logStatWFe1 = 0.0;

         double theta = 1.0;
         String species = "";
         double[] logGroundPopsC1  = new double[numDeps];
         double[] logGroundPopsMg1  = new double[numDeps];
         double[] logGroundPopsMg2  = new double[numDeps];
         double[] logGroundPopsAl1  = new double[numDeps];
         double[] logGroundPopsSi1  = new double[numDeps];
         double[] logGroundPopsSi2  = new double[numDeps];
         double[] logGroundPopsFe1  = new double[numDeps];
//
// C I: Z=6 --> iZ=5:
         double[] aC1 = new double[numDeps];
// Mg I: Z=12 --> iZ=11:
         double[] aMg1 = new double[numDeps];
// Mg II: Z=12 --> iZ=11:
         double[] aMg2 = new double[numDeps];
// Al I: Z=13 --> iZ=12:
         double[] aAl1 = new double[numDeps];
// Si I: Z=14 --> iZ=13:
         double[] aSi1 = new double[numDeps];
// Si II: Z=14 --> iZ =13:
         double[] aSi2 = new double[numDeps];
// Fe I: Z=26 --> iZ=25
         double[] aFe1 = new double[numDeps]; 

         species = "CI";
         logUC1 = PartitionFn.getPartFn(species);
         species = "MgI";
         logUMg1 = PartitionFn.getPartFn(species);
         species = "MgII";
         logUMg2 = PartitionFn.getPartFn(species);
         species = "AlI";
         logUAl1 = PartitionFn.getPartFn(species);
         species = "SiI";
         logUSi1 = PartitionFn.getPartFn(species);
         species = "SiII";
         logUSi2 = PartitionFn.getPartFn(species);
         species = "FeI";
         logUFe1 = PartitionFn.getPartFn(species);

         //System.out.println("iD     PpC1     PpMg1      PpMg2     PpAl1     PpSi1     PpSi2     PpFe1"); 
         for (int iD = 0; iD < numDeps; iD++){
//neutral stage
//Assumes ground state stat weight, g_1, is 1.0
            theta = 5040.0 / temp[0][iD];
// U[0]: theta = 1.0, U[1]: theta = 0.5
            if (theta <= 0.5){
               logStatWC1 = logUC1[1];
               logStatWMg1 = logUMg1[1];
               logStatWMg2 = logUMg2[1];
               logStatWAl1 = logUAl1[1];
               logStatWSi1 = logUSi1[1];
               logStatWSi2 = logUSi2[1];
               logStatWFe1 = logUFe1[1];
            } else if ( (theta < 1.0) && (theta > 0.5) ){
               logStatWC1 = ( (theta-0.5) * logUC1[0] ) + ( (1.0-theta) * logUC1[1] );
               //divide by common factor of interpolation interval of 0.5 = (1.0 - 0.5):
               logStatWC1 = 2.0 * logStatWC1; 
               logStatWMg1 = ( (theta-0.5) * logUMg1[0] ) + ( (1.0-theta) * logUMg1[1] );
               logStatWMg1 = 2.0 * logStatWMg1; 
               logStatWMg2 = ( (theta-0.5) * logUMg2[0] ) + ( (1.0-theta) * logUMg2[1] );
               logStatWMg2 = 2.0 * logStatWMg2; 
               logStatWAl1 = ( (theta-0.5) * logUAl1[0] ) + ( (1.0-theta) * logUAl1[1] );
               logStatWAl1 = 2.0 * logStatWAl1; 
               logStatWSi1 = ( (theta-0.5) * logUSi1[0] ) + ( (1.0-theta) * logUSi1[1] );
               logStatWSi1 = 2.0 * logStatWSi1; 
               logStatWSi2 = ( (theta-0.5) * logUSi2[0] ) + ( (1.0-theta) * logUSi2[1] );
               logStatWSi2 = 2.0 * logStatWSi2; 
               logStatWFe1 = ( (theta-0.5) * logUFe1[0] ) + ( (1.0-theta) * logUFe1[1] );
               logStatWFe1 = 2.0 * logStatWFe1; 
            } else {
               logStatWC1 = logUC1[0];
               logStatWMg1 = logUMg1[0];
               logStatWMg2 = logUMg2[0];
               logStatWAl1 = logUAl1[0];
               logStatWSi1 = logUSi1[0];
               logStatWSi2 = logUSi2[0];
               logStatWFe1 = logUFe1[0];
            }  
            logGroundPopsC1[iD] = stagePops[5][0][iD] - logStatWC1; 
            logGroundPopsMg1[iD] = stagePops[11][0][iD] - logStatWMg1; 
            logGroundPopsMg2[iD] = stagePops[11][1][iD] - logStatWMg2; 
            logGroundPopsAl1[iD] = stagePops[12][0][iD] - logStatWAl1; 
            logGroundPopsSi1[iD] = stagePops[13][0][iD] - logStatWSi1; 
            logGroundPopsSi2[iD] = stagePops[13][1][iD] - logStatWSi2; 
            logGroundPopsFe1[iD] = stagePops[25][0][iD] - logStatWFe1;

           // if (iD%5 == 1){ 
           //     System.out.format("%03d, %21.15f, %21.15f, %21.15f, %21.15f, %21.15f, %21.15f, %21.15f %n", 
           //          iD, logE*(logGroundPopsC1[iD]+temp[1][iD]+Useful.logK()), 
           //              logE*(logGroundPopsMg1[iD]+temp[1][iD]+Useful.logK()), 
           //              logE*(logGroundPopsMg2[iD]+temp[1][iD]+Useful.logK()),
           //              logE*(logGroundPopsAl1[iD]+temp[1][iD]+Useful.logK()), 
           //              logE*(logGroundPopsSi1[iD]+temp[1][iD]+Useful.logK()), 
           //              logE*(logGroundPopsSi2[iD]+temp[1][iD]+Useful.logK()),
           //              logE*(logGroundPopsFe1[iD]+temp[1][iD]+Useful.logK()));
           // }

         }   
        
         double waveno;  //cm?? 
         double freq, logFreq, kapBF;
         double stimEmExp, stimEmLogExp, stimEmLogExpHelp, stimEm;
  
         //System.out.println("iD    iL    lambda    stimEm    aC1     aMg1     aMg2     aAl1    aSi1    aSi2    aFe1 ");
         for (int iL = 0; iL < numLams; iL++){
//
//initialization:
            for (int i = 0; i < numDeps; i++){
               aC1[i] = 0.0;
               aMg1[i] = 0.0;
               aMg2[i] = 0.0;
               aAl1[i] = 0.0;
               aSi1[i] = 0.0;
               aSi2[i] = 0.0;
               aFe1[i] = 0.0;
            }
 
            waveno = 1.0 / lambdaScale[iL];  //cm^-1??
            logFreq = Useful.logC() - Math.log(lambdaScale[iL]); 
            freq = Math.exp(logFreq);
        
            stimEmLogExpHelp = Useful.logH() + logFreq - Useful.logK();   

            //System.out.println("Calling opacC1 from masterMetal..."); 
            if (freq >= 2.0761e15) { 
               aC1 = opacC1(numDeps, temp, lambdaScale[iL], logGroundPopsC1);
            }
            if (freq >= 2.997925e+14) {
               aMg1 = opacMg1(numDeps, temp, lambdaScale[iL], logGroundPopsMg1);
            }
            if (freq >= 2.564306e15) {
               aMg2 = opacMg2(numDeps, temp, lambdaScale[iL], logGroundPopsMg2);
            }
            if (freq >= 1.443e15) {
               aAl1 = opacAl1(numDeps, temp, lambdaScale[iL], logGroundPopsAl1);
            }
            if (freq >= 2.997925e+14) {
               aSi1 = opacSi1(numDeps, temp, lambdaScale[iL], logGroundPopsSi1);
            }
            if (freq >= 7.6869872e14) {
               aSi2 = opacSi2(numDeps, temp, lambdaScale[iL], logGroundPopsSi2);
            }
            if (waveno >= 21000.0) { 
               aFe1 = opacFe1(numDeps, temp, lambdaScale[iL], logGroundPopsFe1);
            }

            for (int iD = 0; iD < numDeps; iD++){

               kapBF = 0.0;
               stimEmLogExp = stimEmLogExpHelp - temp[1][iD];
               stimEmExp = -1.0 * Math.exp(stimEmLogExp);
               stimEm = ( 1.0 - Math.exp(stimEmExp) ); //LTE correction for stimulated emission  

               //kapBF = aC1[iD] + aMg1[iD] + aMg2[iD] + aAl1[iD] + aSi1[iD] + aSi2[iD] + aFe1[iD] ;
               kapBF = aC1[iD] + aMg2[iD] + aAl1[iD] + aSi2[iD] + aFe1[iD] ;
               masterBF[iL][iD] = Math.log(kapBF) + Math.log(stimEm);
             //  if ( (iD%10 == 0) && (iL%10 == 0) ) {
             //    System.out.format("%03d, %03d, %21.15f, %21.15f, %21.15f, %21.15f, %21.15f, %21.15f, %21.15f, %21.15f, %21.15f, %n", 
             //       iD, iL, lambdaScale[iL], Math.log10(stimEm), Math.log10(aC1[iD]), Math.log10(aMg1[iD]), Math.log10(aMg2[iD]), Math.log10(aAl1[iD]), Math.log10(aSi1[iD]), Math.log10(aSi2[iD]), Math.log10(aFe1[iD]));
             //  }

            } //iD
 
         } //iL

         return masterBF;

      } //end method masterMetal

      public static double[] opacC1(int numDeps, double[][] temp, double lambda, double[] logGroundPops){

//c******************************************************************************
//c     This routine computes the bound-free absorption due to C I.
//c******************************************************************************

  //System.out.println("opacC1 called...");
     // include 'Atmos.com'
     // include 'Kappa.com'

      double sigma = 0.0;
      double[] aC1 = new double[numDeps];
//cross-section is zero below threshold, so initialize:
      for (int i = 0; i < numDeps; i++){
         aC1[i] = 0.0;
      }

      double waveno = 1.0 / lambda;  //cm^-1?? 
      double freq = Useful.c / lambda;  
    
      double arg; 
      double[] c1240 = new double[numDeps];
      double[] c1444 = new double[numDeps];
      double freq1 = 0.0;
      double logTkev;
      double[] tkev = new double[numDeps];
//      int modcount = 0;
      for (int i = 0; i < numDeps; i++){
         logTkev = temp[1][i] + Useful.logK() - Useful.logEv();
         tkev[i] = Math.exp(logTkev);
      }

//c  initialize some quantities for each new model atmosphere
      for (int i = 0; i < numDeps; i++){
         c1240[i] = 5.0 * Math.exp(-1.264/tkev[i]);
         c1444[i] = Math.exp(-2.683/tkev[i]);
      }

//c  initialize some quantities for each new model atmosphere or new frequency;
//c  Luo, D. and Pradhan, A.K. 1989, J.Phys. B, 22, 3377-3395.
//c  Burke, P.G. and Taylor, K.T. 1979, J. Phys. B, 12, 2971-2984.
//      if (modelnum.ne.modcount .or. freq.ne.freq1) then
        
         double aa, bb, eeps;
         freq1 = freq;
         double ryd = 109732.298; //Rydberg constant in cm^-1
         //waveno = freq/2.99792458d10
         double xs0 = 0.0;
         double xs1 = 0.0;
         double xd0 = 0.0;
         double xd1 = 0.0;
         double xd2 = 0.0;
         double x1444 = 0.0;
         double x1240 = 0.0;
         double x1100 = 0.0;
//c        P2 3P   1
//c  I AM NOT SURE WHETHER THE CALL TO SEATON IN THE NEXT STATEMENT IS
//c  CORRECT, BUT IT ONLY AFFECTS THINGS BELOW 1100A
         if (freq >= 2.7254e15) {
           arg = -16.80 - ( (waveno-90777.000)/3.0/ryd );
            x1100 = Math.pow(10.0, arg)
              * seaton (2.7254e15, 1.219e-17, 2.0e0, 3.317e0, freq);
         }
//c        P2 1D   2
         if (freq >= 2.4196e15) { 
            arg = -16.80 - ( (waveno-80627.760)/3.0/ryd );
            xd0 = Math.pow(10.0, arg);
            eeps = (waveno-93917.0) * 2.0/9230.0;
            aa = 22.0e-18;
            bb = 26.0e-18;
            xd1 = ((aa*eeps) + bb) / (Math.pow(eeps, 2)+1.0);
            eeps = (waveno-111130.0) * 2.0/2743.0;
            aa = -10.5e-18;
            bb = 46.0e-18;
            xd2 = ( (aa*eeps) + bb) / (Math.pow(eeps, 2)+1.0);
            x1240 = xd0 + xd1 + xd2;
         } 
//c        P2 1S   3
         if (freq >= 2.0761e15) { 
            arg = -16.80 - ( (waveno-69172.400)/3.0/ryd );
            xs0 = Math.pow(10.0, arg);
            eeps = (waveno-97700.0) * 2.0/2743.0;
            aa = 68.0e-18;
            bb = 118.0e-18;
            xs1 = ( (aa*eeps) + bb) / (Math.pow(eeps, 2)+1.0);
            x1444 = xs0 + xs1;
         } 

      //System.out.println("freq " + freq + " lambda " + lambda);
      for (int i = 0; i < numDeps; i++){
         if (freq >= 2.0761e15) { 
            sigma = (x1100*9.0 + x1240*c1240[i] + x1444*c1444[i]); 
            aC1[i] = sigma * Math.exp(logGroundPops[i]);
            //System.out.println("i " + i + " sigma " + sigma + " aC1 " + aC1[i]);
            //System.out.println("i " + i + " logPop " + logGroundPops[i] + " aC1 " + aC1[i]);
         } 
      } 

      return aC1;

} //end method opacC1


      public static double seaton(double freq0, double xsect, double power, double a, double freq){
//c******************************************************************************
//c     This function is a general representation for b-f absorption above
//c     a given ionization limit freq0, using cross-section xsect, 
//c******************************************************************************

      //include 'Kappa.com'
      double freqratio = freq0/freq;
      double seaton;
      int help;

      help = (int) Math.floor( (2.0*power) + 0.01 );

      seaton = xsect * (a + freqratio*(1.0-a))*
              Math.sqrt( Math.pow(freqratio, help) );

      return seaton;

} //end method seaton


//c******************************************************************************
//c     This routine computes the bound-free absorption due to Mg I.  
//c******************************************************************************

      public static double[] opacMg1(int numDeps, double[][] temp, double lambda, double[] logGroundPops){
  //System.out.println("opacMg1 called...");

      double sigma = 0.0;
      double[] aMg1 = new double[numDeps];

//cross-section is zero below threshold, so initialize:
      for (int i = 0; i < numDeps; i++){
         aMg1[i] = 0.0;
      }

      double freq = Useful.c / lambda;  
      //System.out.println("opacMg1: lambda, freq " + lambda + " " + freq);
      double freqlg = Math.log(freq); //base e?


//      include 'Atmos.com'
//      include 'Kappa.com'
//      real*8 flog(9), freqMg(7), peach(7,15), xx(7), tlg(7)
//      real*8 dt(100)
//      integer nt(100)

      double[] xx = new double[7];
      double[] dt = new double[100];
      int[] nt = new int[100]; 

//      data peach/
      //double[][] peach = new double[7][15];
//c         4000 K 
     double[] peach0 = { -42.474, -41.808, -41.273, -45.583, -44.324, -50.969, -50.633, -53.028, -51.785, -52.285, -52.028, -52.384, -52.363, -54.704, -54.359 }; 
//c          5000 K  
     double[] peach1 = { -42.350, -41.735, -41.223, -44.008, -42.747, -48.388, -48.026, -49.643, -48.352, -48.797, -48.540, -48.876, -48.856, -50.772, -50.349 }; 
//c          6000 K  
     double[] peach2 = { -42.109, -41.582, -41.114, -42.957, -41.694, -46.630, -46.220, -47.367, -46.050, -46.453, -46.196, -46.513, -46.493, -48.107, -47.643 }; 
//c         7000 K 
     double[] peach3 = { -41.795, -41.363, -40.951, -42.205, -40.939, -45.344, -44.859, -45.729, -44.393, -44.765, -44.507, -44.806, -44.786, -46.176, -45.685 }; 
//c         8000 K 
     double[] peach4 = { -41.467, -41.115, -40.755, -41.639, -40.370, -44.355, -43.803, -44.491, -43.140, -43.486, -43.227, -43.509, -43.489, -44.707, -44.198 }; 
//c         9000 K 
     double[] peach5 = { -41.159, -40.866, -40.549, -41.198, -39.925, -43.568, -42.957, -43.520, -42.157, -42.480, -42.222, -42.488, -42.467, -43.549, -43.027 }; 
//c        10000 K 
     double[] peach6 = { -40.883, -40.631, -40.347, -40.841, -39.566, -42.924, -42.264, -42.736, -41.363, -41.668, -41.408, -41.660, -41.639, -42.611, -42.418 };

     double[][] peach = { peach0, peach1, peach2, peach3, peach4, peach5, peach6 };

     // double[] freqMg = new double[7];
      double[] freqMg = { 1.9341452e15, 1.8488510e15, 1.1925797e15,
                  7.9804046e14, 4.5772110e14, 4.1440977e14,
                  4.1113514e14 };
     // double[] flog = new double[9];
      double[] flog = { 35.23123, 35.19844, 35.15334, 34.71490, 34.31318,
                33.75728, 33.65788, 33.64994, 33.43947 };
     // double[] tlg = new double[7];
      double[] tlg = { 8.29405, 8.51719, 8.69951, 8.85367, 8.98720, 9.10498, 
               9.21034 }; //base e?
      double freq1 = 0.0; 
//modcount/0/

   int thelp, nn;
   double dd, dd1;
   //double log10E = Math.log10(Math.E);

//c  initialize some quantities for each new model atmosphere
//      if (modelnum .ne. modcount) then
//         modcount = modelnum
     //System.out.println("opacMg1 call, lambda " + lambda);
         for (int i = 0; i < numDeps; i++){
            thelp = (int) Math.floor((temp[0][i]/1000.0)) - 3;
            //System.out.println("i " + i + " temp[0] " + temp[0][i] + " thelp " + thelp);
            //n = Math.max( Math.min(6, thelp-3), 1 );
            // -1 term to adjust from FORTRAN to Java subscripting
            nn = Math.max( Math.min(6, thelp), 1 ) - 1; // -1 term to adjust from FORTRAN to Java subscripting
            nt[i] = nn;
            dt[i] = (temp[1][i]-tlg[nn]) / (tlg[nn+1]-tlg[nn]); //base e?
            //System.out.println(" nn " + nn + " temp[1] " + temp[1][i] + " tlg[nn+1] " + tlg[nn+1] + " tlg[nn] " + tlg[nn] + " dt[i] " + dt[i]);
         }

//      endif
   
//c  initialize some quantities for each new model atmosphere or new frequency;
      //if (modelnum.ne.modcount .or. freq.ne.freq1) then
         freq1 = freq;
       //  do n=1,7
       //     if (freq .gt. freqMg(n)) go to 23
       //  enddo
       //n = 7;
       //  n = 0;
       //  while ( (freq <= freqMg[n]) && (n < 6) ) {
       //     n++;
       //  }
         nn = 0;
         for (int n = 0; n < 7; n++){
            //System.out.println("freq " + freq + " n " + n + " freqMg[n] " + freqMg[n]);
            if (freq > freqMg[n]){   
               break;
            }
            nn++;
         }
         if (freq <= freqMg[6]){
            nn = 7; 
         }  
         //System.out.println("nn " + nn + " flog[nn+1] " + flog[nn+1] + " flog[nn] " + flog[nn]);
         dd = (freqlg-flog[nn]) / (flog[nn+1]-flog[nn]);
         //System.out.println("dd " + dd + " freqlg " + freqlg);
         //if (n .gt. 2) n = 2*n -2
            // -1 term to adjust from FORTRAN to Java subscripting
         //if (n > 2){
         if (nn > 1){
            // -1 term to adjust from FORTRAN to Java subscripting
            //n = 2*n - 2 - 1;
            nn = 2*nn - 2; // - 1;
         }
         dd1 = 1.0 - dd;
         //do it=1,7
         //System.out.println("nn " + nn + " dd1 " + dd1);
         for (int it = 0; it < 7; it++){
            xx[it] = peach[it][nn+1]*dd + peach[it][nn]*dd1;
            //System.out.println("it " + it + " peach[it][nn+1] " + peach[it][nn+1] + "  peach[it][nn] " +  peach[it][nn] + " xx[it] " + xx[it]);
         }
         //enddo
      //endif

      //do i=1,ntau
      for (int i = 0; i < numDeps; i++){
         //if (freq .ge. 2.997925d+14) then
         if (freq >= 2.997925e+14) {
            nn = nt[i];
            sigma = Math.exp( (xx[nn]*(1.0e0-dt[i])) + (xx[nn+1]*dt[i]) ); 
            aMg1[i] = sigma * Math.exp(logGroundPops[i]);
            //System.out.println("i " + i + " sigma " + sigma + " aMg1 " + aMg1[i]);
         //endif
         }
      }
      //enddo

      return aMg1;

 } //end method opacMg1






//c******************************************************************************
//c     This routine computes the bound-free absorption due to Mg II.  
//c******************************************************************************

      public static double[] opacMg2(int numDeps, double[][] temp, double lambda, double[] logGroundPops){
  //System.out.println("opacMg2 called...");

      double sigma = 0.0;
      double[] aMg2 = new double[numDeps];
//cross-section is zero below threshold, so initialize:
      for (int i = 0; i < numDeps; i++){
         aMg2[i] = 0.0;
      }

      double freq = Useful.c / lambda;  

      double logTkev;
      double[] tkev = new double[numDeps];

//      include 'Atmos.com'
//      include 'Kappa.com'
      double[] c1169 = new double[100];
      double freq1 = 0.0;
      double x824 = 0.0;
      double x1169 = 0.0;
 //data modcount/0/

//  initialize some quantities for each new model atmosphere
//      if (modelnum .ne. modcount) then
//         modcount = modelnum
      for (int i = 0; i < numDeps; i++){
         logTkev = temp[1][i] + Useful.logK() - Useful.logEv();
         tkev[i] = Math.exp(logTkev);
      }
         //do i=1,ntau
         for (int i = 0; i < numDeps; i++){
            c1169[i] = 6.0 * Math.exp(-4.43e+0/tkev[i]);
         }
//      endif

//c  initialize some quantities for each new model atmosphere or new frequency;
//c  there are two edges, one at 824 A and the other at 1169 A
//      if (modelnum.ne.modcount .or. freq.ne.freq1) then
         freq1 = freq;
         if (freq >= 3.635492e15) {
            x824 = seaton(3.635492e15, 1.40e-19, 4.0e0, 6.7e0, freq);
         } else {
            x824 = 1.0e-99;
         }
         if (freq >= 2.564306e15) {
            x1169 = 5.11e-19 * Math.pow( (2.564306e15/freq), 3.0);
         } else {
            x1169 = 1.0e-99;
         }
//      endif
      
      for (int i = 0; i < numDeps; i++){
         if (x1169 >= 1.0e-90) {
            sigma = (x824*2.0 + x1169*c1169[i]);
            aMg2[i] = sigma * Math.exp(logGroundPops[i]);
            //System.out.println("i " + i + " sigma " + sigma + " aMg2 " + aMg2[i]);
         }
      }

      return aMg2;

    } //end method opacMg2



//c******************************************************************************
//c     This routine computes the bound-free absorption due to Al I.
//c******************************************************************************

      public static double[] opacAl1(int numDeps, double[][] temp, double lambda, double[] logGroundPops){
  //System.out.println("opacAl1 called...");
 
      double sigma = 0.0;
     double[] aAl1 = new double[numDeps];
//cross-section is zero below threshold, so initialize:
      for (int i = 0; i < numDeps; i++){
         aAl1[i] = 0.0;
      }

      double freq = Useful.c / lambda;  

//      include 'Atmos.com'
//      include 'Kappa.com'
   
      //do i=1,ntau
      for (int i = 0; i < numDeps; i++){
         //if (freq .ge. 1.443d15) then
         if (freq >= 1.443e15) {
            sigma = 6.0 * 6.5e-17 * Math.pow((1.443e15/freq), 5.0);
            aAl1[i] = sigma * Math.exp(logGroundPops[i]);
            //System.out.println("i " + i + " sigma " + sigma + " aAl1 " + aAl1[i]);
         }
      }

      return aAl1;
      } //end method opacAl1




//c******************************************************************************
//c     This routine computes the bound-free absorption due to Si I.  
//c******************************************************************************

      public static double[] opacSi1(int numDeps, double[][] temp, double lambda, double[] logGroundPops){
  //System.out.println("opacSi1 called...");

      double sigma = 0.0;
      double[] aSi1 = new double[numDeps];
//cross-section is zero below threshold, so initialize:
      for (int i = 0; i < numDeps; i++){
         aSi1[i] = 0.0;
      }

      double freq = Useful.c / lambda;  
      double freqlg = Math.log(freq); //base e?


  //    include 'Atmos.com'
  //    include 'Kappa.com'
      double[] xx = new double[9];
      double[] dt = new double[100]; 
      int[] nt = new int[100];
//      save
//c       4000  
      double[] peach0 = { 38.136, 37.834, 37.898, 40.737, 40.581, 45.521, 45.520, 55.068, 53.868, 54.133, 54.051, 54.442, 54.320, 55.691, 55.661, 55.973, 55.922, 56.828, 56.657 };
//c       5000  
      double[] peach1 = { 38.138, 37.839, 37.898, 40.319, 40.164, 44.456, 44.455, 51.783, 50.369, 50.597, 50.514, 50.854, 50.722, 51.965, 51.933, 52.193, 52.141, 52.821, 52.653 };
//c       6000  
      double[] peach2 = { 38.140, 37.843, 37.897, 40.047, 39.893, 43.753, 43.752, 49.553, 48.031, 48.233, 48.150, 48.455, 48.313, 49.444, 49.412, 49.630, 49.577, 50.110, 49.944 };
//c       7000  
      double[] peach3 = { 38.141, 37.847, 37.897, 39.855, 39.702, 43.254, 43.251, 47.942, 46.355, 46.539, 46.454, 46.733, 46.583, 47.615, 47.582, 47.769, 47.715, 48.146, 47.983 };
//c       8000   
      double[] peach4 = { 38.143, 37.850, 37.897, 39.714, 39.561, 42.878, 42.871, 46.723, 45.092, 45.261, 45.176, 45.433, 45.277, 46.221, 46.188, 46.349, 46.295, 46.654, 46.491 };
//c       9000  
      double[] peach5 = { 38.144, 37.853, 37.896, 39.604, 39.452, 42.580, 42.569, 45.768, 44.104, 44.262, 44.175, 44.415, 44.251, 45.119, 45.085, 45.226, 45.172, 45.477, 45.315 };
//c       10000 
      double[] peach6 = { 38.144, 37.855, 37.895, 39.517, 39.366, 42.332, 42.315, 44.997, 43.308, 43.456, 43.368, 43.592, 43.423, 44.223, 44.189, 44.314, 44.259, 44.522, 44.360 };
//c       11000 
      double[] peach7 = { 38.145, 37.857, 37.895, 39.445, 39.295, 42.119, 42.094, 44.360, 42.652, 42.790, 42.702, 42.912, 42.738, 43.478, 43.445, 43.555, 43.500, 43.730, 43.569 };
//c       12000 
      double[] peach8 = { 38.145, 37.858, 37.894, 39.385, 39.235, 41.930, 41.896, 43.823, 42.100, 42.230, 42.141, 42.340, 42.160, 42.848, 42.813, 42.913, 42.858, 43.061, 42.901 };
//      real*8 peach(9,19)
      //double[][] peach = new double[9][19];
      double[][] peach = { peach0, peach1, peach2, peach3, peach4, peach5, peach6, peach7, peach8 };

//c     3P, 1D, 1S, 1D, 3D, 3F, 1D, 3P
      //double[] freqSi = new double[9];
      double[] freqSi = { 2.1413750e15, 1.9723165e15, 1.7879689e15,
                  1.5152920e15, 5.5723927e14, 5.3295914e14,
                  4.7886458e14, 4.7216422e14, 4.6185133e14 };
      //double[] flog = new double[11];
      double[] flog = { 35.45438, 35.30022, 35.21799, 35.11986, 34.95438,
                33.95402, 33.90947, 33.80244, 33.78835, 33.76626,
                33.70518 };
      //double[] tlg = new double[9]; 
      double[] tlg = { 8.29405, 8.51719, 8.69951, 8.85367, 8.98720, 9.10498,
               9.21034, 9.30565, 9.39266 };
      double freq1 = 0.0;
//, modcount/0/

   int thelp, nn;
   double dd, dd1;
//c  initialize some quantities for each new model atmosphere
//      if (modelnum .ne. modcount) then
//         modcount = modelnum
         //do i=1,ntau
         for (int i = 0; i < numDeps; i++){
            thelp = (int) Math.floor(temp[0][i]/1000.0) - 3;
            // -1 term to adjust from FORTRAN to Java subscripting
            //n = Math.max( Math.min(8, thelp-3), 1 );
            nn = Math.max( Math.min(8, thelp), 1 ) - 1;
            nt[i] = nn;
            dt[i] = (temp[1][i]-tlg[nn]) / (tlg[nn+1]-tlg[nn]);
         }
//      endif

//  initialize some quantities for each new model atmosphere or new frequency
      //if (modelnum.ne.modcount .or. freq.ne.freq1) then
         freq1 = freq;
//         do n=1,9
//            if (freq .gt. freqSi(n)) go to 23
//         enddo
//         n = 9;
        // n = 0;
        // while ( (freq <= freqSi[n]) && (n < 8) ) {
        //    n++;
        // }
         nn = 0;
         for (int n = 0; n < 9; n++){
            if (freq > freqSi[n]){
               break;
            }
            nn++; 
         }
         if (freq <= freqSi[8]){
           nn = 9;
         }
//
         dd = (freqlg-flog[nn]) / (flog[nn+1]-flog[nn]);
            // -1 term to adjust from FORTRAN to Java subscripting
         //if (n > 2) { 
         if (nn > 1) { 
            // -1 term to adjust from FORTRAN to Java subscripting
            nn = 2*nn - 2; // - 1; // n already adjusted by this point?
         }
         dd1 = 1.0 - dd;
         for (int it = 0; it < 9; it++){
            xx[it] = peach[it][nn+1]*dd + peach[it][nn]*dd1;
         }
      //endif

      for (int i=0; i < numDeps; i++){
         if (freq >= 2.997925e+14) {
            nn = nt[i];
            sigma = ( 9.0 * Math.exp( -(xx[nn]*(1.-dt[i]) + xx[nn+1]*dt[i]) ) ); 
            aSi1[i] = sigma * Math.exp(logGroundPops[i]);
            //System.out.println("i " + i + " sigma " + sigma + " aSi1 " + aSi1[i]);
         }
      }

      return aSi1;

      }




//c******************************************************************************
//c     This routine computes the bound-free absorption due to Si II.
//c******************************************************************************

      public static double[] opacSi2(int numDeps, double[][] temp, double lambda, double[] logGroundPops){
  //System.out.println("opacSi2 called...");

      double sigma = 0.0;
      double[] aSi2 = new double[numDeps];
//cross-section is zero below threshold, so initialize:
      for (int i = 0; i < numDeps; i++){
         aSi2[i] = 0.0;
      }

      double freq = Useful.c / lambda;  
      double freqlg = Math.log(freq); //base e?

   int thelp, nn;
   double dd, dd1;

//      include 'Atmos.com'
//      include 'Kappa.com'
      double[] xx = new double[6];
      double[] dt = new double[100];
      int[] nt = new int[100];
      //double peach = new double[6][14]; 
//c        10000
      double[] peach0 = { -43.8941, -42.2444, -40.6054, -54.2389, -50.4108, -52.0936, -51.9548, -54.2407, -52.7355, -53.5387, -53.2417, -53.5097, -54.0561, -53.8469 };
//c        12000 
      double[] peach1 = { -43.8941, -42.2444, -40.6054, -52.2906, -48.4892, -50.0741, -49.9371, -51.7319, -50.2218, -50.9189, -50.6234, -50.8535, -51.2365, -51.0256 };
//c        14000 
      double[] peach2 = { -43.8941, -42.2444, -40.6054, -50.8799, -47.1090, -48.5999, -48.4647, -49.9178, -48.4059, -49.0200, -48.7252, -48.9263, -49.1980, -48.9860 };
//c        16000 
      double[] peach3 = { -43.8941, -42.2444, -40.6054, -49.8033, -46.0672, -47.4676, -47.3340, -48.5395, -47.0267, -47.5750, -47.2810, -47.4586, -47.6497, -47.4368 };
//c        18000 
      double[] peach4 = { -43.8941, -42.2444, -40.6054, -48.9485, -45.2510, -46.5649, -46.4333, -47.4529, -45.9402, -46.4341, -46.1410, -46.2994, -46.4302, -46.2162 };
//c        20000  
      double[] peach5 = { -43.8941, -42.2444, -40.6054, -48.2490, -44.5933, -45.8246, -45.6947, -46.5709, -45.0592, -45.5082, -45.2153, -45.3581, -45.4414, -45.2266 };
 
      double[][] peach = { peach0, peach1, peach2, peach3, peach4, peach5 };

      //double[] freqSi = new double[7];
      double[] freqSi = { 4.9965417e15, 3.9466738e15, 1.5736321e15,
                  1.5171539e15, 9.2378947e14, 8.3825004e14,
                  7.6869872e14 };
//c     2P, 2D, 2P, 2D, 2P
      //double[] flog = new double[9]; 
      double[] flog = { 36.32984, 36.14752, 35.91165, 34.99216, 34.95561,
                34.45951, 34.36234, 34.27572, 34.20161 };
      //double[] tlg = new double[6];
      double tlg[] = { 9.21034, 9.39266, 9.54681, 9.68034, 9.79813, 9.90349 };
      double freq1 = 0.0;
 // modcount/0/

//c  set up some data upon first entrance with a new model atmosphere
//      if (modelnum .ne. modcount) then
//         modcount = modelnum
         for (int i = 0; i < numDeps; i++){
            thelp = (int) Math.floor(temp[0][i]/2000.0) - 4;
            // -1 term to adjust from FORTRAN to Java subscripting
            //n = Math.max( Math.min(5, thelp-4), 1 );
            nn = Math.max( Math.min(5, thelp), 1 ) - 1;
            nt[i] = nn;
            dt[i] = (temp[1][i]-tlg[nn]) / (tlg[nn+1]-tlg[nn]);
         }
//      endif

//c  initialize some quantities for each new model atmosphere or new frequency
//      if (modelnum.ne.modcount .or. freq.ne.freq1) then
         freq1 = freq;
//         do n=1,7
//            if (freq .gt. freqSi(n)) go to 23
//         enddo
//         n = 8
         //n = 0;
         //while ( (freq <= freqSi[n]) && (n < 6) ) {
         //   n++;
        // }
         nn = 0;
         for (int n = 0; n < 7; n++){
            if (freq > freqSi[n]){
               break;
            }
            nn++; 
         }
         if (freq <= freqSi[6]){
           nn = 7;
         }
//
//
         dd = (freqlg-flog[nn]) / (flog[nn+1]-flog[nn]);
            // -1 term to adjust from FORTRAN to Java subscripting
         //if (n > 2){
         if (nn > 1){
            // -1 term to adjust from FORTRAN to Java subscripting
            //n = 2*n - 2;
            nn = 2*nn - 2; // - 1; //n already adjusted by this point?
         }
            // -1 term to adjust from FORTRAN to Java subscripting
         //if (n == 14){
         if (nn == 13){
            // -1 term to adjust from FORTRAN to Java subscripting
            //n = 13;
            nn = 12;
         }
         dd1 = 1.0 - dd;
         for (int it = 0; it < 6; it++){
            xx[it] = peach[it][nn+1]*dd + peach[it][nn]*dd1;
         }
//      endif

      for (int i = 0; i < numDeps; i++){
         if (freq >= 7.6869872e14) {
            nn = nt[i];
            sigma = ( 6.0 * Math.exp(xx[nn]*(1.0-dt[i]) + xx[nn+1]*dt[i]) );
            aSi2[i] = sigma * Math.exp(logGroundPops[i]);
            //System.out.println("i " + i + " sigma " + sigma + " aSi2 " + aSi2[i]);
         }
      }

      return aSi2;

      } //end method opacSi2


//c******************************************************************************
//c     This routine computes the bound-free absorption due to Fe I.
//c******************************************************************************


      public static double[] opacFe1(int numDeps, double[][] temp, double lambda, double[] logGroundPops){
  //System.out.println("opacFe1 called...");

      double sigma = 0.0;
      double[] aFe1 = new double[numDeps];
//cross-section is zero below threshold, so initialize:
      for (int i = 0; i < numDeps; i++){
         aFe1[i] = 0.0;
      }

      double waveno = 1.0 / lambda;  //cm^-1?? 
      double freq = Useful.c / lambda;  

//      include 'Atmos.com'
//      include 'Kappa.com'
      //real*8 bolt(48,100), gg(48), ee(48), wno(48), xsect(48)
      double[][] bolt = new double[48][100];
      double[] xsect = new double[48];
// double[] gg = new double[48];
      double[] gg = 
     {25.0, 35.0, 21.0, 15.0, 9.0,  35.0, 33.0, 21.0, 27.0, 49.0, 9.0,  21.0, 27.0, 9.0,  9.0,
      25.0, 33.0, 15.0, 35.0, 3.0,   5.0, 11.0, 15.0, 13.0, 15.0, 9.0,  21.0, 15.0, 21.0, 25.0, 35.0,
      9.0,  5.0,  45.0, 27.0, 21.0, 15.0, 21.0, 15.0, 25.0, 21.0, 35.0, 5.0,  15.0, 45.0, 35.0, 55.0, 25.0};
// double[] ee = new double[48];
      double[] ee = 
     {500.0,   7500.0,  12500.0, 17500.0, 19000.0, 19500.0, 19500.0, 21000.0,
      22000.0, 23000.0, 23000.0, 24000.0, 24000.0, 24500.0, 24500.0, 26000.0, 26500.0,
      26500.0, 27000.0, 27500.0, 28500.0, 29000.0, 29500.0, 29500.0, 29500.0, 30000.0,
      31500.0, 31500.0, 33500.0, 33500.0, 34000.0, 34500.0, 34500.0, 35000.0, 35500.0,
      37000.0, 37000.0, 37000.0, 38500.0, 40000.0, 40000.0, 41000.0, 41000.0, 43000.0,
      43000.0, 43000.0, 43000.0, 44000.0};

// double[] wno = new double[48];
      double[] wno = 
      {63500.0, 58500.0, 53500.0, 59500.0, 45000.0, 44500.0, 44500.0, 43000.0,
       58000.0, 41000.0, 54000.0, 40000.0, 40000.0, 57500.0, 55500.0, 38000.0, 57500.0,
       57500.0, 37000.0, 54500.0, 53500.0, 55000.0, 34500.0, 34500.0, 34500.0, 34000.0,
       32500.0, 32500.0, 32500.0, 32500.0, 32000.0, 29500.0, 29500.0, 31000.0, 30500.0,
       29000.0, 27000.0, 54000.0, 27500.0, 24000.0, 47000.0, 23000.0, 44000.0, 42000.0,
       42000.0, 21000.0, 42000.0, 42000.0};

      //data freq1, modcount/0., 0/
      double freq1 = 0.0;
 
      double hkt;

//c  set up some data upon first entrance with a new model atmosphere
//      if (modelnum .ne. modcount) then
//         modcount = modelnum
         for (int i = 0; i < numDeps; i++){
            hkt = 6.6256e-27 / (1.38054e-16*temp[0][i]);
            //do k=1,48
            for (int k = 0; k < 48; k++){
               bolt[k][i] = gg[k] * Math.exp(-ee[k]*Useful.c*hkt);
            }
         }
//      endif

//c  initialize some quantities for each new model atmosphere or new frequency;
//c  the absorption begins at 4762 A.
//      if (modelnum.ne.modcount .or. freq.ne.freq1) then
         freq1 = freq;
         //waveno = freq/2.99792458d10
         //if (waveno .ge. 21000.) then
         if (waveno >= 21000.0){ 
            //do k=1,48
            for (int k = 0; k < 48; k++){
               xsect[k] = 0.0;
               //if (wno(k) .lt. waveno){ 
               if (wno[k] < waveno){ 
                  xsect[k]= 3.0e-18 /
                           ( 1.0 + Math.pow( ( (wno[k]+3000.0-waveno)/wno[k]/0.1 ), 4 ) );
               }
            }
         }
//      endif

      //do i=1,ntau
      for (int i = 0; i < numDeps; i++){
//aFe1 seems to be cumulative.  Moog does not seem to have this reset for each depth, but my aFe is blowing up, so let's try it...
         aFe1[i] = 0.0; //reset accumulator each depth- ???
         //if (waveno .ge. 21000.) then
         if (waveno >= 21000.0) { 
            //do k=1,48
              for (int k = 0; k < 48; k++){
               aFe1[i] = 0.0; //reset accumulator each 'k' - ??? (like removing aFe1 term in expression below...
               sigma = aFe1[i] + xsect[k]*bolt[k][i]; 
               aFe1[i] = sigma * Math.exp(logGroundPops[i]);
               //System.out.println("i " + i + " sigma " + sigma + " aFe1 " + aFe1[i]);
              }
            }
         }
            
      return aFe1;

   } // end opacFe1 method


} //end class KappasMetal
