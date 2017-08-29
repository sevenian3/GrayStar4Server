<?php


//initialize to blank string as part of validation and sanitization:
$teff = "";
$logg = "";
$logZScale = "";
$massStar = "";
$xiT = "";
$lineThresh = "";
$voigtThresh = "";
$lambdaStart = "";
$lambdaStop = "";
$sampling = "";
$logGammaCol = "";
$logKapFudge = "";
$macroV = "";
$rotV = "";
$rotI = "";
$nInnerIter = "";
$nOuterIter = "";
$ifTiO = "";
$logHeFe = "";
$logCO = "";
$logAlphaFe = "";


$teff = $_POST['teff'];
$logg = $_POST['logg'];
$logZScale = $_POST['logZScale'];
$massStar = $_POST['massStar'];
$xiT = $_POST['xiT'];
$lineThresh = $_POST['lineThresh'];
$voigtThresh = $_POST['voigtThresh'];
$lambdaStart = $_POST['lambdaStart'];
$lambdaStop = $_POST['lambdaStop'];
$sampling = $_POST['sampling'];
$logGammaCol = $_POST['logGammaCol'];
$logKapFudge = $_POST['logKapFudge'];
$macroV = $_POST['macroV'];
$rotV = $_POST['rotV'];
$rotI = $_POST['rotI'];
$nInnerIter = $_POST['nInnerIter'];
$nOuterIter = $_POST['nOuterIter'];
$ifTiO = $_POST['ifTiO'];
$logHeFe = $_POST['logHeFe'];
$logCO = $_POST['logCO'];
$logAlphaFe = $_POST['logAlphaFe'];


if ($_SERVER["REQUEST_METHOD"] == "POST") {
   $teff = test_input($_POST["teff"]);
   $logg = test_input($_POST['logg']);
   $logZScale = test_input($_POST['logZScale']);
   $massStar = test_input($_POST['massStar']);
   $xiT = test_input($_POST['xiT']);
   $lineThresh = test_input($_POST['lineThresh']);
   $voigtThresh = test_input($_POST['voigtThresh']);
   $lambdaStart = test_input($_POST['lambdaStart']);
   $lambdaStop = test_input($_POST['lambdaStop']);
   $logGammaCol = test_input($_POST['logGammaCol']);
   $logKapFudge = test_input($_POST['logKapFudge']);
   $macroV = test_input($_POST['macroV']);
   $rotV = test_input($_POST['rotV']);
   $rotI = test_input($_POST['rotI']);
   $nInnerIter = test_input($_POST['nInnerIter']);
   $nOuterIter = test_input($_POST['nOuterIter']);
   $ifTiO = test_input($_POST['ifTiO']);  
   $logHeFe = test_input($_POST['logHeFe']);
   $logCO = test_input($_POST['logCO']);
   $logAlphaFe = test_input($_POST['logAlphaFe']);
}

//validation and sanitization
function test_input($data) {
  $data = trim($data);
  $data = stripslashes($data);
  $data = htmlspecialchars($data);
  return $data;
}


// WARNING: Must be consistent with values in ChromaStar!

/*
////hard-wired values for testing:
//Sun:
$teff = "5780.0";
$logg = "4.44";
$logZScale = "0.0";
$massStar = "1.0";
$xiT = "1.0";
$lineThresh = "3.0";
$voigtThresh = "15.0";
$lambdaStart = "587.0";
$lambdaStop = "592.0";
$sampling = "fine";
$logGammaCol = "0.0";
$logKapFudge = "0.0";
$macroV = "2.0";
$rotV = "2.0";
$rotI = "90.0";
$nInnerIter = "2";
$nOuterIter = "2";
$ifTiO = "0";
$logHeFe = "0.0";
$logCO = "0.0";
$logAlphaFe = "0.0";
*/

//echo $teff . ' ' . $logg . ' ' . $logZScale . ' ' . $massStar . ' ' . $xiT . ' ' . $lineThresh . ' ' . $voigtThresh  . ' ' . $lambdaStart  . ' ' . $lambdaStop;
$argLine = 'java -cp ./chromastarserver -jar ChromaStarServer.jar ' . ' ' . $teff . ' ' . $logg . ' ' . $logZScale . ' ' . $massStar . ' ' . $xiT . ' ' . $lineThresh . ' ' . $voigtThresh . ' ' . $lambdaStart . ' ' . $lambdaStop  . ' ' . $sampling . ' ' . $logGammaCol  . ' ' . $logKapFudge . ' ' . $macroV . ' ' . $rotV . ' ' . $rotI . ' ' . $nOuterIter . ' ' . $nInnerIter . ' ' . $ifTiO . ' ' . $logHeFe . ' ' . $logCO . ' ' . $logAlphaFe; 

//print_r($argLine);
exec($argLine, $outArray, $returnVar);
//print_r($outArray);
//

//numLines should be equal to numDeps + 1 for header:
$numLines = count($outArray);
//echo "numLines " . $numLines . "</br>";
//print_r($outArray);
//Method of concatenating all scalar values into one variable & JSON field:

//keep track of where we are in outArray[]:
// - increment this for every 'explode()' we do
$rowPntr = 0; 

//
//    BLOCK ONE - array sizes
//
 $keyArr = explode(",", $outArray[$rowPntr]);
 $rowPntr++;
 $numKeys = count($keyArr);
//echo "numKeys= " . $numKeys;
    $rowArr = explode(",", $outArray[$rowPntr]);
    $rowPntr++;
    for ($j = 0; $j < $numKeys-1; $j++){
       //echo "j " . j . " keyArr[j]= " . $keyArr[$j];
       if ($keyArr[$j] == "numDeps"){
          $numDeps = $rowArr[$j];
           }
       if ($keyArr[$j] == "numMaster"){
          $numMaster = $rowArr[$j];
           }
       if ($keyArr[$j] == "numThetas"){
          $numThetas = $rowArr[$j];
           }
//       if ($keyArr[$j] == "numSpecSyn"){
//          $numSpecSyn = $rowArr[$j];
//           }
       if ($keyArr[$j] == "numGaussLines"){
          $numGaussLines = $rowArr[$j];
           }
       if ($keyArr[$j] == "numLams"){
          $numLams = $rowArr[$j];
           }
       if ($keyArr[$j] == "nelemAbnd"){
          $nelemAbnd = $rowArr[$j];
           }
       if ($keyArr[$j] == "numSpecies"){
          $numSpecies = $rowArr[$j];
           }
    } // $j - loop over columns 
 // Add last value at end of row (with no trailing comma appended!) 
       if ($keyArr[$numKeys-1] == "numDeps"){
          $numDeps = $rowArr[$numKeys-1];
           }
       if ($keyArr[$numKeys-1] == "numMaster"){
          $numMaster = $rowArr[$numKeys-1];
           }
       if ($keyArr[$numKeys-1] == "numThetas"){
          $numThetas = $rowArr[$numKeys-1];
           }
//       if ($keyArr[$numKeys-1] == "numSpecSyn"){
//          $numSpecSyn = $rowArr[$numKeys-1];
//           }
       if ($keyArr[$numKeys-1] == "numGaussLines"){
          $numGaussLines = $rowArr[$numKeys-1];
           }
       if ($keyArr[$numKeys-1] == "numLams"){
          $numLams = $rowArr[$numKeys-1];
           }
       if ($keyArr[$numKeys-1] == "nelemAbnd"){
          $nelemAbnd = $rowArr[$numKeys-1];
           }
       if ($keyArr[$numKeys-1] == "numSpecies"){
          $numSpecies = $rowArr[$numKeys-1];
           }

// package up the associative array:
// one key and string value per column variable: 
for ($j = 0; $j < $numKeys; $j++){
   $inputArr[$keyArr[$j]] = $rowArr[$j];
}

//
// BLOCK 2 - vertical stellar atmospheric structure:
//
$keyArr = explode(",", $outArray[$rowPntr]);
$rowPntr++;
$numKeys = count($keyArr);

//$value = "";
//initialize array of strings to be accumulated:
for ($j = 0; $j < $numKeys; $j++){
    $valArr[$j] = "";
} 

//First numDeps-1 values:
for ($i = 0; $i < $numDeps-1; $i++){
    $rowArr = explode(",", $outArray[$rowPntr]);
    $rowPntr++;
    for ($j = 0; $j < $numKeys; $j++){
       $valArr[$j] = $valArr[$j] . $rowArr[$j] . ",";
    } // $j - loop over columns 
} // $i - loop over rows

//last row - pack up without trailing comma:
$rowArr = explode(",", $outArray[$rowPntr]);
$rowPntr ++;
for ($j = 0; $j < $numKeys; $j++){
    $valArr[$j] = $valArr[$j] . $rowArr[$j];
} 

// package up the associative array:
// one key and string value per column variable: 
for ($j = 0; $j < $numKeys; $j++){
   $inputArr[$keyArr[$j]] = $valArr[$j];
}

//
//    BLOCK THREE 
//
//   Line blankted Flux spectrum (SED)
//
//
$keyArr = explode(",", $outArray[$rowPntr]);
$rowPntr++;
$numKeys = count($keyArr);
//initialize array of strings to be accumulated:
for ($j = 0; $j < $numKeys; $j++){
    $valArr[$j] = "";
} 

//First numMaster-1 values:
for ($i = 0; $i < $numMaster-1; $i++){
    $rowArr = explode(",", $outArray[$rowPntr]); 
    $rowPntr++;
    for ($j = 0; $j < $numKeys; $j++){
       $valArr[$j] = $valArr[$j] . $rowArr[$j] . ",";
    } // $j - loop over columns 
} // $i - loop over rows

//last entry:
$rowArr = explode(",", $outArray[$rowPntr]);
$rowPntr++;
for ($j = 0; $j < $numKeys; $j++){
    $valArr[$j] = $valArr[$j] . $rowArr[$j];
} 

//   $inputArr = array($key => $value);
// package up the associative array:
// one key and string value per column variable: 
for ($j = 0; $j < $numKeys; $j++){
   $inputArr[$keyArr[$j]] = $valArr[$j];
}


//
//    BLOCK FOUR 
   //Block 4: Specific intensity distribution:

  for ($i = 0; $i < $numThetas; $i++){
     //echo $i . "</br>";  
     $cosThetaKey = $outArray[$rowPntr];
     //echo "cosThetaKey " . $cosThetaKey . "</br>";
     $rowPntr++;
     $cosThetaVal = $outArray[$rowPntr];
     //echo "cosThetaVal " . $cosThetaVal . "</br>";
     $rowPntr++;
     $inputArr[$cosThetaKey] = $cosThetaVal;
     $intensKey = $outArray[$rowPntr];
     //echo "intensKey " . $intensKey . "</br>";
     $rowPntr++;
     $intensVal = ""; //Initialize string
     for ($j = 0; $j < $numMaster-1; $j++){
         $intensVal = $intensVal . $outArray[$rowPntr] . ","; 
         $rowPntr++;
           }  // lambda loop $j
  //last line - no trailing comma:
       $intensVal = $intensVal . $outArray[$rowPntr];
       $rowPntr++;

// package up the associative array:
// one key and string value per column variable: 
   $inputArr[$intensKey] = $intensVal;

  } //theta loop $i


////
////    BLOCK FIVE 
////
////   Flux spectrum (spectrum synthesis)
////
////
//$keyArr = explode(",", $outArray[$rowPntr]);
//$rowPntr++;
//$numKeys = count($keyArr);
////initialize array of strings to be accumulated:
//for ($j = 0; $j < $numKeys; $j++){
//    $valArr[$j] = "";
//} 
//
////First numSpecSyn-1 values:
//for ($i = 0; $i < $numSpecSyn-1; $i++){
//    $rowArr = explode(",", $outArray[$rowPntr]); 
//    $rowPntr++;
//    for ($j = 0; $j < $numKeys; $j++){
//       $valArr[$j] = $valArr[$j] . $rowArr[$j] . ",";
//    } // $j - loop over columns 
//} // $i - loop over rows
//
////last entry:
//$rowArr = explode(",", $outArray[$rowPntr]);
//$rowPntr++;
//for ($j = 0; $j < $numKeys; $j++){
//    $valArr[$j] = $valArr[$j] . $rowArr[$j];
//} 
//
////   $inputArr = array($key => $value);
//// package up the associative array:
//// one key and string value per column variable: 
//for ($j = 0; $j < $numKeys; $j++){
//   $inputArr[$keyArr[$j]] = $valArr[$j];
//}


//
//    BLOCK SIX 
//
//   Element, ion stage, and line center wavelength IDs for lines included in the spectrum 
//
//
$keyArr = explode(",", $outArray[$rowPntr]);
$rowPntr++;
$numKeys = count($keyArr);
//initialize array of strings to be accumulated:
for ($j = 0; $j < $numKeys; $j++){
    $valArr[$j] = "";
} 

//First numGaussLines-1 values:
for ($i = 0; $i < $numGaussLines-1; $i++){
    $rowArr = explode(",", $outArray[$rowPntr]); 
    $rowPntr++;
    for ($j = 0; $j < $numKeys; $j++){
       $valArr[$j] = $valArr[$j] . $rowArr[$j] . ",";
    } // $j - loop over columns 
} // $i - loop over rows

//last entry:
$rowArr = explode(",", $outArray[$rowPntr]);
$rowPntr++;
for ($j = 0; $j < $numKeys; $j++){
    $valArr[$j] = $valArr[$j] . $rowArr[$j];
} 

//   $inputArr = array($key => $value);
// package up the associative array:
// one key and string value per column variable: 
for ($j = 0; $j < $numKeys; $j++){
   $inputArr[$keyArr[$j]] = $valArr[$j];
}

//
//    BLOCK SEVEN 
//
//   Continuum Flux spectrum (SED)
//
//
$keyArr = explode(",", $outArray[$rowPntr]);
$rowPntr++;
$numKeys = count($keyArr);
//initialize array of strings to be accumulated:
for ($j = 0; $j < $numKeys; $j++){
    $valArr[$j] = "";
} 

//First numLams-1 values:
for ($i = 0; $i < $numLams-1; $i++){
    $rowArr = explode(",", $outArray[$rowPntr]); 
    $rowPntr++;
    for ($j = 0; $j < $numKeys; $j++){
       $valArr[$j] = $valArr[$j] . $rowArr[$j] . ",";
    } // $j - loop over columns 
} // $i - loop over rows

//last entry:
$rowArr = explode(",", $outArray[$rowPntr]);
$rowPntr++;
for ($j = 0; $j < $numKeys; $j++){
    $valArr[$j] = $valArr[$j] . $rowArr[$j];
} 

//   $inputArr = array($key => $value);
// package up the associative array:
// one key and string value per column variable: 
for ($j = 0; $j < $numKeys; $j++){
   $inputArr[$keyArr[$j]] = $valArr[$j];
}


//
//    BLOCK EIGHT 
//
//   Linear monochromatic continuum limb darkening coefficients (LDCs) 
//
//
$keyArr = explode(",", $outArray[$rowPntr]);
$rowPntr++;
$numKeys = count($keyArr);
//initialize array of strings to be accumulated:
for ($j = 0; $j < $numKeys; $j++){
    $valArr[$j] = "";
} 

//First numLams-1 values:
for ($i = 0; $i < $numLams-1; $i++){
    $rowArr = explode(",", $outArray[$rowPntr]); 
    $rowPntr++;
    for ($j = 0; $j < $numKeys; $j++){
       $valArr[$j] = $valArr[$j] . $rowArr[$j] . ",";
    } // $j - loop over columns 
} // $i - loop over rows

//last entry:
$rowArr = explode(",", $outArray[$rowPntr]);
$rowPntr++;
for ($j = 0; $j < $numKeys; $j++){
    $valArr[$j] = $valArr[$j] . $rowArr[$j];
} 

//   $inputArr = array($key => $value);
// package up the associative array:
// one key and string value per column variable: 
for ($j = 0; $j < $numKeys; $j++){
   $inputArr[$keyArr[$j]] = $valArr[$j];
}


//
//    BLOCK NINE 
//
//  Detailed A12 abundances 
//
//
$keyArr = explode(",", $outArray[$rowPntr]);
$rowPntr++;
$numKeys = count($keyArr);
//initialize array of strings to be accumulated:
for ($j = 0; $j < $numKeys; $j++){
    $valArr[$j] = "";
} 

//First numSpecies-1 values:
for ($i = 0; $i < $nelemAbnd-1; $i++){
    $rowArr = explode(",", $outArray[$rowPntr]); 
    $rowPntr++;
    for ($j = 0; $j < $numKeys; $j++){
       $valArr[$j] = $valArr[$j] . $rowArr[$j] . ",";
    } // $j - loop over columns 
} // $i - loop over rows

//last entry:
$rowArr = explode(",", $outArray[$rowPntr]);
$rowPntr++;
for ($j = 0; $j < $numKeys; $j++){
    $valArr[$j] = $valArr[$j] . $rowArr[$j];
} 

//   $inputArr = array($key => $value);
// package up the associative array:
// one key and string value per column variable: 
for ($j = 0; $j < $numKeys; $j++){
   $inputArr[$keyArr[$j]] = $valArr[$j];
}


//
//    BLOCK TEN 
//
//  Chem species w  Ground state ionization energies and log_e populations 
//
//
$keyArr = explode(",", $outArray[$rowPntr]);
$rowPntr++;
$numKeys = count($keyArr);
//initialize array of strings to be accumulated:
for ($j = 0; $j < $numKeys; $j++){
    $valArr[$j] = "";
} 

//First numSpecies-1 values:
for ($i = 0; $i < $numSpecies-1; $i++){
    $rowArr = explode(",", $outArray[$rowPntr]); 
    $rowPntr++;
    for ($j = 0; $j < $numKeys; $j++){
       $valArr[$j] = $valArr[$j] . $rowArr[$j] . ",";
    } // $j - loop over columns 
} // $i - loop over rows

//last entry:
$rowArr = explode(",", $outArray[$rowPntr]);
$rowPntr++;
for ($j = 0; $j < $numKeys; $j++){
    $valArr[$j] = $valArr[$j] . $rowArr[$j];
} 

//   $inputArr = array($key => $value);
// package up the associative array:
// one key and string value per column variable: 
for ($j = 0; $j < $numKeys; $j++){
   $inputArr[$keyArr[$j]] = $valArr[$j];
}


//Encode associative array with all output data as JSON

$inputJSON = json_encode($inputArr);
//
print($inputJSON);


?>
