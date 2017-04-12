/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package chromastarserver;

/**
 * Solve hydrostatic eq for P scale on the tau scale - need to pick a depth
 * dependent kappa value! - dP/dTau = g/kappa --> dP/dlogTau = Tau*g/kappa press
 * is a 4 x numDeps array: rows 0 & 1 are linear and log *gas* pressure,
 * respectively rows 2 & 3 are linear and log *radiation* pressure Split
 * pressure into gas and radiation contributions as we calculate it:
 */
public class Hydrostat {


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
        double logE = Math.log10(Math.E);

//Compute radiation pressure for this temperature structure and add it to Pgas 
//
       double pT, pRad;
       double[] logPRad = new double[numDeps];
       double[] logPTot = new double[numDeps];
     //  System.out.println("hydroFormalSoln: ");
       for (int i = 0; i < numDeps; i++){
           logPRad[i] = radFac + 4.0 * temp[1][i];
           pRad = Math.exp(logPRad[i]);
      //System.out.println("i " + i + " pRad " + pRad);
           pT = guessPGas[0][i] + pRad;
    //       System.out.println("i " + i + " guessPGas[1] " + logE*guessPGas[1][i]);
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
//   press[1][0] = Math.log(1.0e-4); //try same upper boundary as Phoenix
//
//   press[0][0] = Math.exp(press[1][0]);
     press[0][0] = 0.1 * guessPGas[0][0];
     press[1][0] = Math.log(press[0][0]);
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

    //System.out.println("hydroFormalSoln: ");
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
       //System.out.println("i " + i + " guessPGas[1] " + logE*guessPGas[1][i] + " press[1] " + logE*press[1][i]);
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
