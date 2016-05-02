/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package graystar3server;

/**
 * Initializes and re-scales a Phoenix LTE spherical reference model of 
 * Teff=10000K, log(g)=4.0, [Fe/H]=0.0, xi=1.0 km/s, l=1.0H_p, M=1M_Sun, R=1.1613E+11cm
 *
 * @author Ian
 */
public class ScaleT10000{

    public static double phxRefTeff = 10000.0;
    public static double phxRefLogEg = Math.log(10.0) * 4.0;  //base e!
//He abundance from  Grevesse Asplund et al 2010
    public static double phxRefLogAHe = Math.log(10.0) * (10.93 - 12.0);  //base e "A_12" logarithmic abundance scale!
    

    //Corresponding Tau_12000 grid (ie. lambda_0 = 1200 nm):    
    public static double[] phxRefTau64 = {
 0.00000000000000000E+00, 9.99999999999999955E-07, 1.34596032415536424E-06,
 1.81160919420041334E-06, 2.43835409826882661E-06, 3.28192787251147086E-06,
 4.41734470314007309E-06, 5.94557070854439435E-06, 8.00250227816105150E-06,
 1.07710505603676914E-05, 1.44974067037263169E-05, 1.95129342263596216E-05,
 2.62636352765333530E-05, 3.53498110503010939E-05, 4.75794431400941383E-05,
 6.40400427119728238E-05, 8.61953566475303262E-05, 1.16015530173997159E-04,
 1.56152300600049659E-04, 2.10174801133248699E-04, 2.82886943462596935E-04,
 3.80754602122237182E-04, 5.12480587696093125E-04, 6.89778537938765847E-04,
 9.28414544519474451E-04, 1.24960914129198684E-03, 1.68192432488086874E-03,
 2.26380340952144670E-03, 3.04698957090350801E-03, 4.10112707055130046E-03,
 5.51995432128156785E-03, 7.42963950759494875E-03, 1.00000000000000002E-02,
 1.34596032415536422E-02, 1.81160919420041318E-02, 2.43835409826882663E-02,
 3.28192787251147047E-02, 4.41734470314006436E-02, 5.94557070854439401E-02,
 8.00250227816105275E-02, 1.07710505603676912E-01, 1.44974067037263149E-01,
 1.95129342263596212E-01, 2.62636352765332981E-01, 3.53498110503010221E-01,
 4.75794431400941464E-01, 6.40400427119728333E-01, 8.61953566475303190E-01,
 1.16015530173997150E+00, 1.56152300600049654E+00, 2.10174801133248712E+00,
 2.82886943462596641E+00, 3.80754602122236818E+00, 5.12480587696092638E+00,
 6.89778537938765801E+00, 9.28414544519474383E+00, 1.24960914129198670E+01,
 1.68192432488086894E+01, 2.26380340952144650E+01, 3.04698957090350540E+01,
 4.10112707055129562E+01, 5.51995432128157333E+01, 7.42963950759495049E+01,
 1.00000000000000000E+02
    };

    public static double[] logPhxRefTau64() {

        double logE = Math.log10(Math.E);

        int numPhxDep = ScaleT10000.phxRefTau64.length;
        double logPhxRefTau64[] = new double[numPhxDep];
        for (int i = 1; i < numPhxDep; i++) {
            logPhxRefTau64[i] = Math.log(ScaleT10000.phxRefTau64[i]);
        }
        logPhxRefTau64[0] = logPhxRefTau64[1] - (logPhxRefTau64[numPhxDep - 1] - logPhxRefTau64[1]) / numPhxDep;
        return logPhxRefTau64;
    }

    public static double[][] phxRefTemp(double teff, int numDeps, double[][] tauRos) {

        double logE = Math.log10(Math.E);

        //Theoretical radiative/convective model from Phoenix V15:
        double[] phxRefTemp64 = {
 6.07574016685149309E+03, 6.07574016685149309E+03, 6.13264671606194861E+03,
 6.20030362747541585E+03, 6.27534705504544127E+03, 6.35396254937768026E+03,
 6.43299900128272293E+03, 6.51018808525609893E+03, 6.58411555606889124E+03,
 6.65406717610081068E+03, 6.71983498258185136E+03, 6.78154367852633823E+03,
 6.83954193198123903E+03, 6.89437231818902364E+03, 6.94676889243451842E+03,
 6.99759489202792247E+03, 7.04769490055547158E+03, 7.09773520027041195E+03,
 7.14812062339764907E+03, 7.19901426577775601E+03, 7.25041827414427917E+03,
 7.30225171801659872E+03, 7.35440093819652611E+03, 7.40675066225539558E+03,
 7.45920456139609178E+03, 7.51166464185182758E+03, 7.56404228766520191E+03,
 7.61627005664532771E+03, 7.66833575187113820E+03, 7.72034173334201841E+03,
 7.77258785750414881E+03, 7.82555139374063583E+03, 7.87986936059489017E+03,
 7.93639246968124371E+03, 7.99620846303960116E+03, 8.06052820253916161E+03,
 8.13047124123426238E+03, 8.20741189262034641E+03, 8.29307358429898159E+03,
 8.38980788216330802E+03, 8.49906053657168923E+03, 8.62314483632361771E+03,
 8.76456384216990409E+03, 8.92693370905029224E+03, 9.11177170396923248E+03,
 9.32167977041711492E+03, 9.56236981551314602E+03, 9.82432656703466455E+03,
 1.01311427939962559E+04, 1.04299661074183350E+04, 1.08355089220389909E+04,
 1.12094886773674716E+04, 1.16360710406256258E+04, 1.20991237739366334E+04,
 1.25891111265208237E+04, 1.31070008299570563E+04, 1.36522498965801387E+04,
 1.42233473670298790E+04, 1.48188302103200131E+04, 1.54423659243804523E+04,
 1.60892587452310745E+04, 1.67828517694842230E+04, 1.74930217234773954E+04,
 1.82922661949382236E+04
        };

        // interpolate onto gS3 tauRos grid and re-scale with Teff:
        double[] phxRefTemp = new double[numDeps];
        double[][] scaleTemp = new double[2][numDeps];
        for (int i = 0; i < numDeps; i++) {
            phxRefTemp[i] = ToolBox.interpol(ScaleT10000.logPhxRefTau64(), phxRefTemp64, tauRos[1][i]);
            scaleTemp[0][i] = teff * phxRefTemp[i] / ScaleT10000.phxRefTeff;
            scaleTemp[1][i] = Math.log(scaleTemp[0][i]);
            //System.out.println("tauRos[1][i] " + logE * tauRos[1][i] + " scaleTemp[1][i] " + logE * scaleTemp[1][i]);
        }

        return scaleTemp;

    }

    public static double[][] phxRefPGas(double grav, double zScale, double logAHe, int numDeps, double[][] tauRos) {

        double logE = Math.log10(Math.E);
        double logEg = Math.log(grav); //base e!
        double AHe = Math.exp(logAHe);
        double refAHe = Math.exp(phxRefLogAHe);
        double logZScale = Math.log(zScale);

        //Theoretical radiative/convective model from Phoenix V15:
        double[] phxRefPGas64 = {
 1.00000000000000005E-04, 8.32127743125684882E-02, 1.29584527404206007E-01,
 1.94435381478779895E-01, 2.81524759872055830E-01, 3.94850766488002047E-01,
 5.39098197994885120E-01, 7.20109114447812781E-01, 9.45331395103965466E-01,
 1.22424260721948497E+00, 1.56877812718506826E+00, 1.99379948180689048E+00,
 2.51761637911653136E+00, 3.16251087800302599E+00, 3.95513966878889667E+00,
 4.92671520309637767E+00, 6.11303768406991388E+00, 7.55464145673977505E+00,
 9.29736005428628154E+00, 1.13934670418806956E+01, 1.39033471883101818E+01,
 1.68975909460311797E+01, 2.04594801623940121E+01, 2.46878880919212804E+01,
 2.97005964646718432E+01, 3.56383534114781142E+01, 4.26698208468708984E+01,
 5.09974403334007107E+01, 6.08640463074419387E+01, 7.25594340179816726E+01,
 8.64248329112294158E+01, 1.02854593091977520E+02, 1.22294652156180661E+02,
 1.45234045163109670E+02, 1.72184927273123520E+02, 2.03652334583264832E+02,
 2.40105656346438934E+02, 2.81936164286554344E+02, 3.29393094590693863E+02,
 3.82482413201705356E+02, 4.40963324580460835E+02, 5.04333229685725428E+02,
 5.71827998329611432E+02, 6.42424030136117835E+02, 7.15115448265608620E+02,
 7.89188190751975185E+02, 8.64179477829598227E+02, 9.41037808653716070E+02,
 1.02093026109089942E+03, 1.10808816566702853E+03, 1.20591338801728261E+03,
 1.32157321934523725E+03, 1.46400967396971282E+03, 1.64395527893530380E+03,
 1.87431044562489683E+03, 2.16986659968736876E+03, 2.54753164223200429E+03,
 3.02667796755900645E+03, 3.62964225373483487E+03, 4.38288420138537458E+03,
 5.31730879832813844E+03, 6.47251190142057658E+03, 7.89413608165941059E+03,
 9.64747840003540659E+03
        };

        int numPhxDeps = phxRefPGas64.length;  //yeah, I know, 64, but that could change!
        double[] logPhxRefPGas64 = new double[numPhxDeps];
        for (int i = 0; i < phxRefPGas64.length; i++) {
            logPhxRefPGas64[i] = Math.log(phxRefPGas64[i]);
        }

        // interpolate onto gS3 tauRos grid and re-scale with Teff:
        double[] phxRefPGas = new double[numDeps];
        double[] logPhxRefPGas = new double[numDeps];
        double[][] scalePGas = new double[2][numDeps];
//exponents in scaling with g:
        double gexpTop = 0.53; //top of model
        double gexpBottom = 0.85; //bottom of model
        double gexpRange = (gexpBottom - gexpTop);
        double tauLogRange =  tauRos[1][numDeps-1] -  tauRos[1][0];
        double thisGexp;
// factor for scaling with A_He:
        double logHeDenom = 0.666667 * Math.log(1.0 + 4.0*refAHe);
        for (int i = 0; i < numDeps; i++) {
            logPhxRefPGas[i] = ToolBox.interpol(ScaleT10000.logPhxRefTau64(), logPhxRefPGas64, tauRos[1][i]);
            thisGexp = gexpTop + gexpRange * (tauRos[1][i] - tauRos[1][0]) / tauLogRange;
            //scaling with g
            scalePGas[1][i] = thisGexp*logEg + logPhxRefPGas[i] - thisGexp*ScaleT10000.phxRefLogEg;
            //scaling with zscl:
            scalePGas[1][i] = -0.5*logZScale + scalePGas[1][i];
            //scaling with A_He:
            scalePGas[1][i] = 0.666667 * Math.log(1.0 + 4.0*AHe) + scalePGas[1][i] - logHeDenom; 
            scalePGas[0][i] = Math.exp(scalePGas[1][i]);
            //System.out.println("scalePGas[1][i] " + logE * scalePGas[1][i]);
        }

        return scalePGas;

    }

    public static double[][] phxRefPe(double teff, double grav, int numDeps, double[][] tauRos, double zScale, double logAHe) {

        double logE = Math.log10(Math.E);
        double logEg = Math.log(grav); //base e!
        double AHe = Math.exp(logAHe);
        double refAHe = Math.exp(phxRefLogAHe);
        double logZScale = Math.log(zScale);

        //Theoretical radiative/convective model from Phoenix V15:
        double[] phxRefPe64 = {
 4.77258390479251340E-05, 1.54333794509103339E-02, 2.24384775218179552E-02,
 3.24056217848841463E-02, 4.62639509784656192E-02, 6.49897301016105072E-02,
 8.96001972148401798E-02, 1.21161157265374353E-01, 1.60825358340301261E-01,
 2.09891146620685975E-01, 2.69867426146356171E-01, 3.42538888354808724E-01,
 4.30045384007358256E-01, 5.35006986797593842E-01, 6.60704782988379868E-01,
 8.11262305821688567E-01, 9.91741961224463009E-01, 1.20813527252446407E+00,
 1.46731521914247520E+00, 1.77705126262480850E+00, 2.14614122290851617E+00,
 2.58462667298359561E+00, 3.10405210627260297E+00, 3.71777653138435804E+00,
 4.44135288803457673E+00, 5.29279499891786465E+00, 6.29303772366266312E+00,
 7.46652989782078702E+00, 8.84221515332682451E+00, 1.04552216626003140E+01,
 1.23496848557054300E+01, 1.45813048229500843E+01, 1.72206436663779385E+01,
 2.03589457441922157E+01, 2.41156208954111868E+01, 2.86442876094033458E+01,
 3.41355927487861948E+01, 4.08398462152914732E+01, 4.90908766488638761E+01,
 5.93486059459067832E+01, 7.21405304518226842E+01, 8.81824952094146681E+01,
 1.08367129768339950E+02, 1.33856171619767082E+02, 1.65693080738235807E+02,
 2.04943252558813072E+02, 2.52705001145053956E+02, 3.07224623951268654E+02,
 3.70334137141753217E+02, 4.33722318385145741E+02, 5.08910395587106336E+02,
 5.82220694357564639E+02, 6.65278728107771599E+02, 7.62124991657425880E+02,
 8.79654481582760809E+02, 1.02622262715821921E+03, 1.21099204341081804E+03,
 1.44432886438589208E+03, 1.73838904022049860E+03, 2.10808802008476914E+03,
 2.57102379769462232E+03, 3.14976025581092108E+03, 3.86645770963505538E+03,
 4.75493678618616923E+03
        };

        int numPhxDeps = phxRefPe64.length;  //yeah, I know, 64, but that could change!
        double[] logPhxRefPe64 = new double[numPhxDeps];
        for (int i = 0; i < phxRefPe64.length; i++) {
            logPhxRefPe64[i] = Math.log(phxRefPe64[i]);
        }

        // interpolate onto gS3 tauRos grid and re-scale with Teff:
        double[] phxRefPe = new double[numDeps];
        double[] logPhxRefPe = new double[numDeps];
        double[][] scalePe = new double[2][numDeps];
//exponents in scaling with Teff ONLY VALID FOR Teff < 10000K:
        double omegaTaum1 = 0.0012; //log_10(tau) < 0.1
        double omegaTaup1 = 0.0015; //log_10(tau) > 1.0
        double omegaRange = (omegaTaup1-omegaTaum1);
        double lonOfM1 = Math.log(0.1);
//exponents in scaling with g:
        double gexpTop = 0.53; //top of model
        double gexpBottom = 0.82; //bottom of model
        double gexpRange = (gexpBottom - gexpTop);
        double tauLogRange =  tauRos[1][numDeps-1] -  tauRos[1][0];
        double thisGexp;
        double thisOmega = omegaTaum1; //default initialization
// factor for scaling with A_He:
        double logHeDenom = 0.333333 * Math.log(1.0 + 4.0*refAHe);
        for (int i = 0; i < numDeps; i++) {
            logPhxRefPe[i] = ToolBox.interpol(ScaleT10000.logPhxRefTau64(), logPhxRefPe64, tauRos[1][i]);
            thisGexp = gexpTop + gexpRange * (tauRos[1][i] - tauRos[1][0]) / tauLogRange;
            //scaling with g
            scalePe[1][i] = thisGexp*logEg + logPhxRefPe[i] - thisGexp*ScaleT10000.phxRefLogEg;
            //scale with Teff:
            if (teff < 10000.0){
               if (tauRos[0][i] < 0.1){
                 thisOmega =  omegaTaum1;
               }
               if (tauRos[0][i] > 10.0){
                 thisOmega =  omegaTaup1;
               }
               if ( (tauRos[0][i] >= 0.1) && (tauRos[0][i] <= 10.0) ){
                   thisOmega = omegaTaum1 + omegaRange * (tauRos[1][i] - lonOfM1) / tauLogRange;
               }
               scalePe[1][i] = thisOmega*teff + scalePe[1][i] - thisOmega*ScaleT10000.phxRefTeff;
            }
            //scaling with zscl:
            scalePe[1][i] = 0.5*logZScale + scalePe[1][i];
            //scaling with A_He:
            scalePe[1][i] = 0.333333 * Math.log(1.0 + 4.0*AHe) + scalePe[1][i] - logHeDenom;
            scalePe[1][i] = logEg + logPhxRefPe[i] - ScaleT10000.phxRefLogEg;
            scalePe[0][i] = Math.exp(scalePe[1][i]);
            //System.out.println("scaleNe[1][i] " + logE * scaleNe[1][i]);
        }

        return scalePe;

    }

    public static double[][] phxRefNe(int numDeps, double[][] scaleTemp, double[][] scalePe) {

        double logE = Math.log10(Math.E);
        double[][] scaleNe = new double[2][numDeps];

        for (int i = 0; i < numDeps; i++){
            scaleNe[1][i] = scalePe[1][i] - scaleTemp[1][i] - Useful.logK();
            scaleNe[0][i] = Math.exp(scaleNe[1][i]);
        }

        return scaleNe;
    }


}
