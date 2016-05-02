/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package graystar3server;

/**
 * Solve hydrostatic eq for P scale on the tau scale - need to pick a depth
 * dependent kappa value! - dP/dTau = g/kappa --> dP/dlogTau = Tau*g/kappa press
 * is a 4 x numDeps array: rows 0 & 1 are linear and log *gas* pressure,
 * respectively rows 2 & 3 are linear and log *radiation* pressure Split
 * pressure into gas and radiation contributions as we calculate it:
 */
public class Hydrostat {

    /**
     *
     * @param numDeps
     * @param grav
     * @param tauRos
     * @param kappa
     * @param temp
     * @return
     */
    public static double[][] hydrostatic(int numDeps, double grav, double[][] tauRos, double[][] kappa, double[][] temp) {

        //double c = Useful.c;
        double logC = Useful.logC();
        //double sigma = Useful.sigma;
        double logSigma = Useful.logSigma();

        //double c = 9.9989E+10; // light speed in vaccuum in cm/s
        //double sigma = 5.670373E-5;   //Stefan-Boltzmann constant ergs/s/cm^2/K^4   
        double radFac = Math.log(4.0) + logSigma - Math.log(3.0) - logC;

        double logRadiusSun = 0.0;  //solar units

        double press[][] = new double[4][numDeps];

        //double ln10 = Math.log(10.0); //handy wee quantity       
        //Upper boundary condition: total pressure at top of atmosphere
        double p1 = 1.0E-4;

        // System.out.println("HYDROSTAT: ln10= " + ln10 + " p1 " + p1 + "\r\n");
        //Finite differences in log(Tau) space - deltaX should be uniform, 
        //   but compute it on the fly anyway in case we play with other tau scales
        press[0][0] = p1;
        press[1][0] = Math.log(p1);
        press[2][0] = p1;
        press[3][0] = Math.log(p1);

        // Decalare scratch variables:
        double deltaX, deltaP, help, p2, p3, thisKap, logThisKap;
        double logPrad, pRad, helpSub, h, k1, k2, k3, k4;

        // Calculate P at the 2nd depth point in using Euler's method:
        //deltaX = tauRos[1][1] - tauRos[1][0];
        //help = (tauRos[0][0] / kappa[0][0]) * grav;
        //deltaP = help * (deltaX);
        //p2 = p1 + deltaP;
        // Compute LTE bolometric radiation contribution to total HSE pressure
        //logPrad = radFac + 4.0 * temp[1][1];
        //pRad = Math.exp(logPrad);
        //// Avoid zero or negative Pgas values in subtraction below:
        //if (pRad >= 0.99 * p2) {
        //    pRad = 0.99 * p2;
        //}
        // Avoid a direct subtraction in case Prad is close to Pgas for deeper 
        // layers of hotter stars, and both values are large:
        //pGas = p2 - pRad;
        //helpSub = 1.0E0 - (pRad / p2);
        //press[0][1] = helpSub * p2;
        //press[1][1] = Math.log(press[0][1]);
        //press[2][1] = pRad;
        //press[3][1] = Math.log(pRad);
        //System.out.println("HYDROSTAT: i " + i + " Pgas " + press[0][i] + " Prad " + pRad);
        //Set lower boundary of next step:
        //p1 = p2;
//RK4      for (int i = 2; i < numDeps; i++) {   //RK4
        for (int i = 1; i < numDeps; i++) {

            // Euler's method:
            // Delta log(tau):
            deltaX = tauRos[1][i] - tauRos[1][i - 1];
            //// I have no idea why this is necessary, bu tit seems to be needed to get the right pressure scaling with logg:
            //logThisKap = kappa[1][i] - (logRadiusSun - logRadius);             
            //thisKap = Math.exp(logThisKap);
            thisKap = kappa[0][i];
            help = (tauRos[0][i] / thisKap) * grav;
            //// deltaP = help * ( deltaX ); //log10
            deltaP = help * (deltaX);
            p2 = p1 + deltaP;
            //System.out.println("i " + i + " kappa[0][i] " + kappa[0][i] + " tauRos[0][i] " + tauRos[0][i] 
            // + " grav " + grav + " p2 " + p2);
            //// 4th order Runge-Kutte (mid-point), p. 705, Numerical Recipes in F77, 2nd Ed.
            //h = tauRos[1][i] - tauRos[1][i - 2];
            //k1 = h * (tauRos[0][i - 2] / kappa[0][i - 2]) * grav;
            //k2 = h * (tauRos[0][i - 1] / kappa[0][i - 1]) * grav;
            //k3 = k2;
            //k4 = h * (tauRos[0][i] / kappa[0][i]) * grav;
///
            //p3 = p1 + (k1 / 6.0) + (k2 / 3.0) + (k3 / 3.0) + (k4 / 6.0);

            //System.out.println("HYDROSTAT: i " + i + " deltaX " + deltaX + 
            //                   " help " + help + " deltaP " + deltaP + " p1 " + p1 + " p2 " + p2);
            // Compute LTE bolometric radiation contribution to total HSE pressure
            logPrad = radFac + 4.0 * temp[1][i];
            pRad = Math.exp(logPrad);

            // Avoid zero or negative Pgas values in subtraction below:
            if (pRad >= 0.99 * p2) {
                pRad = 0.99 * p2;
            }  //Euler
            //if (pRad >= 0.99 * p3) {
            //    pRad = 0.99 * p3;
            //} // 2nd O R-K

            // Avoid a direct subtraction in case Prad is close to Pgas for deeper 
            // layers of hotter stars, and both values are large:
            //pGas = p2 - pRad;
            helpSub = 1.0E0 - (pRad / p2); //Euler
            //helpSub = 1.0E0 - (pRad / p3);  // 2nd O R-K

            press[0][i] = helpSub * p2;  //Euler
            //press[0][i] = helpSub * p3;  // 2nd O R-K
            press[1][i] = Math.log(press[0][i]);
            press[2][i] = pRad;
            press[3][i] = Math.log(pRad);
            //press[2][i] = 0.0;  //test
            //press[3][i] = -99.0;  //test

            //System.out.println("HYDROSTAT: temp " + temp[0][i] + " Pgas " + press[0][i] + " Prad " + pRad);
            //Set lower boundary of next step:
            //p1 = p2; //Euler
            p1 = p2;  // 2nd O R-K
            //p2 = p3;  // 2nd O R-K

        }

        return press;

    }  //end method hydrostatic()


// This approach is based on integrating the formal solution of the hydrostaitc equilibrium equation
// on the otical depth (Tau) scale.  Advantage is that it makes better use of the itial guess at
// pgas    
//
//  Takes in *Gas* pressure, converts tot *total pressure*, then returns *Gas* pressure
//
    public static double[][] hydroFormalSoln(int numDeps, double grav, double[][] tauRos, double[][] kappa, double[][] temp, double[][] guessPGas) {
   
        double press[][] = new double[2][numDeps];
        double logC = Useful.logC();
        double logSigma = Useful.logSigma();

        double radFac = Math.log(4.0) + logSigma - Math.log(3.0) - logC;

        double logEg = Math.log(grav); //Natural log g!! 
        // no needed if integrating in natural log?? //double logLogE = Math.log(Math.log10(Math.E));
        double log1p5 = Math.log(1.5);

//Compute radiation pressure for this temperature structure and add it to Pgas 
//
       double pT, pRad;
       double[] logPRad = new double[numDeps];
       double[] logPTot = new double[numDeps];
       for (int i = 0; i < numDeps; i++){
           logPRad[i] = radFac + 4.0 * temp[1][i];
           pRad = Math.exp(logPRad[i]);
      //System.out.println("i " + i + " pRad " + pRad);
           pT = guessPGas[0][i] + pRad;
           logPTot[i] = Math.log(pT);
       }

  double help, logHelp, logPress;
  double term, logSum, integ, logInteg, lastInteg;
  double deltaLogT; 
  double[] sum = new double[numDeps];

//Upper boundary - inherit from intiial guess:
//Carefull here - P at upper boundary can be an underestimate, but it must not be greater than value at next depth in!
//  press[1][0] = logPTot[0];
//  press[1][0] = guessPGas[1][0];
   press[1][0] = Math.log(1.0e-4); //try same upper boundary as Phoenix
//
   press[0][0] = Math.exp(press[1][0]);
//Corresponding value of basic integrated quantity at top of atmosphere:
  logSum = 1.5 * press[1][0] + Math.log(0.666667) - logEg;
  sum[0] = Math.exp(logSum); 
  
// Integrate inward on logTau scale

// CAUTION; This is not an integral for Delta P, but for P once integral at each tau is exponentiated by 2/3!
// Accumulate basic integral to be exponentiated, then construct pressure values later:

//Jump start integration with an Euler step:
    deltaLogT = tauRos[1][1] - tauRos[1][0];
// log of integrand
    logInteg = tauRos[1][1] + 0.5*logPTot[1] - kappa[1][1];
    lastInteg = Math.exp(logInteg);  
    sum[1] = sum[0] + lastInteg * deltaLogT; 

// Continue with extended trapezoid rule:
   
    for (int i = 2; i < numDeps; i++){

      deltaLogT = tauRos[1][i] - tauRos[1][i-1];
      logInteg = tauRos[1][i] + 0.5*logPTot[i] - kappa[1][i];
      integ = Math.exp(logInteg);
      term = 0.5 * (integ + lastInteg) * deltaLogT;
      sum[i] = sum[i-1] + term; //accumulate basic integrated quantity
      lastInteg = integ;  

    } 

    for (int i = 1; i < numDeps; i++){
//Evaluate total pressures from basic integrated quantity at edach depth 
// our integration variable is the natural log, so I don't think we need the 1/log(e) factor
       logPress = 0.666667 * (log1p5 + logEg + Math.log(sum[i]));
//Subtract radiation pressure:
       logHelp = logPRad[i] - logPress;
       help = Math.exp(logHelp);
// FOr hot and low g stars: limit Prad to 50% Ptot so we doen't get netaive Pgas and rho values:
       if (help > 0.5){
          help = 0.5;
       }
       press[1][i] = logPress + Math.log(1.0 - help);
       press[0][i] = Math.exp(press[1][i]);
    }

     return press;   //*Gas* pressure

 } //end method hydroFormalSoln()
 

// Compute radiation pressure
    public static double[][] radPress(int numDeps, double[][] temp) {

        double pRad[][] = new double[2][numDeps];

        double logC = Useful.logC();
        double logSigma = Useful.logSigma();
        double radFac = Math.log(4.0) + logSigma - Math.log(3.0) - logC;
       for (int i = 0; i < numDeps; i++){
           pRad[1][i] = radFac + 4.0 * temp[1][i];
           pRad[0][i] = Math.exp( pRad[1][i]);
      } 

      return pRad;

    } //end method radPress

}
