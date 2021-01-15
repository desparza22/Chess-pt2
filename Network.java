import java.lang.Math;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;

public class Network {
  // String savedStateFileName = "networkState.dat";
  //
  private final int[][] rangesPerLong = new int[][] {{0, 64}, {8, 56}, {0, 64}, {0, 64}, {0, 64}, {0, 64}, {0, 64}, {8, 56}, {0, 64}, {0, 64}, {0, 64}, {0, 64}, {0, 64}, {0, 22}};

  String trainFilePath = "finding-elo/paeTrain.dat";
  String testFilePath = "finding-elo/paeTest.dat";

  private Random r = new Random();

  private int numLayers;
  private int[] layerSizes;
  private ArrayList<double[]> biases;
  private ArrayList<double[][]> weights;

  private boolean dataFilled = false;
  private final int positions = 136242;
  private final int numTrainPositions = (int) (positions * .9);
  private final int numTestPositions = positions - numTrainPositions;
  private List<PositionAndEvaluation> trainPositions;
  private List<PositionAndEvaluation> validatePositions;
  private List<PositionAndEvaluation> testPositions;

  public Network(int[] layerSizes) {
    this.numLayers = layerSizes.length;
    this.layerSizes = layerSizes;

    biases = new ArrayList<double[]>(numLayers-1);
    fillBiases();
    weights = new ArrayList<double[][]>(numLayers-1);
    fillWeights();
  }

  // public Network() {
  //   NetworkFileHandler nfh = new NetworkFileHandler();
  //   NetworkParameter parameters = nfh.readFromFile(savedStateFileName);
  //   if(parameters.hasData()) {
  //     this.layerSizes = parameters.layerSizes();
  //     this.numLayers = layerSizes.length;
  //     this.biases = parameters.biases();
  //     this.weights = parameters.weights();
  //   } else {
  //     System.out.println("Network parameters were never filled");
  //   }
  // }

  public Network(boolean doNothing) {

  }


  public void SGD(int epochs, int miniBatchSize, double eta, boolean compareWithTest, double lambda, int millisecondsBetweenCheckin) {
    long lastTime = System.currentTimeMillis();
    Scanner scanner = new Scanner(System.in);
    if(!dataFilled) {
      loadData();
    }
    ArrayList<double[]> nablaBiases = new ArrayList<double[]>(biases.size());
    ArrayList<double[][]> nablaWeights = new ArrayList<double[][]>(weights.size());
    for(int i = 0; i < biases.size(); i++) {
      double[] layerBiases = biases.get(i);
      nablaBiases.add(new double[layerBiases.length]);
    }
    for(int i = 0; i < weights.size(); i++) {
      double[][] layerWeights = weights.get(i);
      nablaWeights.add(new double[layerWeights.length][layerWeights[0].length]);
    }

    int trainingSize = trainPositions.size();
    for(int j = 0; j < epochs; j++) {
      for(int k = miniBatchSize; k <= trainingSize; k += miniBatchSize) {
        updateMiniBatch(k - miniBatchSize, k, eta, nablaBiases, nablaWeights, lambda);
        if(k % 5 == 0) {
          System.out.println("Epoch " + j + "." + k + ": \n" + evaluate());
          if(k % 30 == 0) {
            if(System.currentTimeMillis() - lastTime > millisecondsBetweenCheckin) {
              System.out.println("Continue? y/n");
              String response = scanner.nextLine();
              lastTime = System.currentTimeMillis();
              if(!response.equals("y")) {
                return;
              }
            }
          }
        }
      }
      // if(compareWithTest) {
      //
      // } else {
      //   System.out.println("Epoch " + j + " complete");
      // }
      shuffle(trainPositions);
    }
    // writeStateToFile();
  }

  // private void writeStateToFile() {
  //   NetworkFileHandler nfh = new NetworkFileHandler();
  //   nfh.writeToFile(biases, weights, layerSizes, savedStateFileName);
  // }

  private void quickEvaluate() {
    // if(!dataFilled) {
    //   fillData();
    // }
    // int end = r.nextInt(testLabels.length);
    // if(end < 5) {
    //   end = 5;
    // }
    // for(int i = end - 5; i < end; i++) {
    //   int label = testLabels[i];
    //   int[][] image = testImages.get(i);
    //   System.out.println(printImage(image));
    //   ArrayList<double[]> activations = new ArrayList<double[]>(numLayers);
    //   ArrayList<double[]> zees = new ArrayList<double[]>(numLayers-1);
    //   activateInputs(image, activations);
    //   feedForward(zees, activations);
    //   double[] active = activations.get(activations.size()-1);
    //   for(int j = 0; j < 10; j++) {
    //     System.out.println("Certainty of " + j + " = " + active[j]);
    //   }
    // }
    // System.out.println("new Eval");
    // for(int i = 0; i < weights.size(); i++) {
    //   System.out.println("new Layer");
    //   double[][] weight = weights.get(i);
    //   for(int j = 0; j < weight.length; j++) {
    //     for(int k = 0; k < weight.length; k++) {
    //       System.out.print(round(weight[j][k], 3) + " ");
    //     }
    //     System.out.println();
    //   }
    // }
    // System.out.println("new eval");
    // for(int i = 0; i < biases.size(); i++) {
    //   System.out.println("new layer");
    //   double[] bias = biases.get(i);
    //   for(int j = 0; j < bias.length; j++) {
    //     System.out.print(round(bias[j], 3) + " ");
    //   }
    //   System.out.println();
    // }
  }

  public double round(double value, int places) {
    if (places < 0) throw new IllegalArgumentException();

    BigDecimal bd = BigDecimal.valueOf(value);
    bd = bd.setScale(places, RoundingMode.HALF_UP);
    return bd.doubleValue();
  }

  public double scorePosition(long[] position) {
    ArrayList<double[]> activations = new ArrayList<double[]>(numLayers);
    ArrayList<double[]> zees = new ArrayList<double[]>(numLayers-1);
    // activateInputs(image, activations);
    activateInputs(position, activations);
    feedForward(zees, activations);
    // System.out.println(printImage(image));
    return getEvaluation(activations);
  }

  private String evaluate() {
    // if(!dataFilled) {
    //   fillData();
    // }
    int successes = 0;
    int placeSumOfCorrect = 0;
    double sumOfDifference = 0;
    for(int i = 0; i < testPositions.size(); i++) {
      PositionAndEvaluation positionAndEvaluation = testPositions.get(i);
      long[] position = positionAndEvaluation.getPositionContainer();
      double evaluation = positionAndEvaluation.getEvaluation();
      ArrayList<double[]> activations = new ArrayList<double[]>(numLayers);
      ArrayList<double[]> zees = new ArrayList<double[]>(numLayers-1);
      activateInputs(position, activations);
      feedForward(zees, activations);
      double networkEvaluation = getEvaluation(activations);
      double difference = Math.abs(evaluation - networkEvaluation);
      sumOfDifference += difference;
    }

    String testResult = "The network had an error of " + round(sumOfDifference, 3) + " on " + testPositions.size() + " evaluations \nfor an average of " + round(sumOfDifference/testPositions.size(), 6) + " per evaluation.";
    return testResult;
  }

  private double getEvaluation(ArrayList<double[]> activations) {
    return activations.get(activations.size()-1)[0];
  }

  private void updateMiniBatch(int start, int end, double eta, ArrayList<double[]> nablaBiases, ArrayList<double[][]> nablaWeights, double lambda) {
    for(int i = start; i < end; i++) {
      PositionAndEvaluation positionAndEvaluation = trainPositions.get(i);
      long[] position = positionAndEvaluation.getPositionContainer();
      double evaluation = positionAndEvaluation.getEvaluation();
      ArrayList<double[]> deltaBiases = new ArrayList<double[]>(numLayers);
      ArrayList<double[][]> deltaWeights = new ArrayList<double[][]>(numLayers);
      backprop(position, evaluation, deltaBiases, deltaWeights);
      for(int j = 0; j < nablaBiases.size(); j++) {
        nablaBiases.set(j, arrayAddition(nablaBiases.get(j), deltaBiases.get(j)));
      }
      for(int j = 0; j < nablaWeights.size(); j++) {
        nablaWeights.set(j, arrayAddition(nablaWeights.get(j), deltaWeights.get(j)));
      }
    }
    updateBiases(nablaBiases, eta, end - start);
    updateWeights(nablaWeights, eta, end - start, lambda);
  }

  private void backprop(long[] position, double evaluation, ArrayList<double[]> deltaBiases, ArrayList<double[][]> deltaWeights) {
    ArrayList<double[]> activations = new ArrayList<double[]>(numLayers);
    ArrayList<double[]> zees = new ArrayList<double[]>(numLayers-1);
    activateInputs(position, activations);
    feedForward(zees, activations);
    backpropErrors(evaluation, zees, activations, deltaBiases, deltaWeights);
  }

  private void backpropErrors(double evaluation, ArrayList<double[]> zees, ArrayList<double[]> activations, ArrayList<double[]> deltaBiases, ArrayList<double[][]> deltaWeights) {
    LinkedList<double[]> reverseErrorOrder = new LinkedList<double[]>();

    // /* process for quadratic cost, works better when neurons are linear (as opposed to sigmoid) */ double[] error = hammondProduct(costDerivative(activations.get(activations.size()-1), label), sigmoidPrime(zees.get(zees.size()-1)));
    /* process for cross entropy, works for sigmoid */
    double[] error = costDerivative(activations.get(activations.size()-1), evaluation);

    reverseErrorOrder.add(error);

    for(int level = 2; level < numLayers; level++) {
      double[] z = zees.get(zees.size() - level);
      double[] sigmoidPrimeZ = sigmoidPrime(z);


      double[][] weight = weights.get(weights.size() - level + 1);
      error = hammondProduct(verticalDotProduct(weight, error), sigmoidPrimeZ);
      reverseErrorOrder.addFirst(error);
    }

    int index = 0;
    while(reverseErrorOrder.size() != 0) {
      error = reverseErrorOrder.removeFirst();
      deltaBiases.add(error);
      deltaWeights.add(matrixFromVectorProduct(error, activations.get(index)));
      index++;
    }
  }

  private double[][] matrixFromVectorProduct(double[] error, double[] activations) {
    double[][] matrix = new double[error.length][activations.length];
    for(int i = 0; i < error.length; i++) {
      for(int j = 0; j < activations.length; j++) {
        matrix[i][j] = error[i] * activations[j];
      }
    }
    return matrix;
  }

  private double[] verticalDotProduct(double[][] matrix, double[] vector) {
    double[] result = new double[matrix[0].length];
    for(int i = 0; i < result.length; i++) {
      double product = 0;
      for(int j = 0; j < vector.length; j++) {
        product += matrix[j][i] * vector[j];
      }
      result[i] = product;
    }
    return result;
  }

  private double[] costDerivative(double[] activations, double evaluation) {
    double[] derivative = activations.clone();
    derivative[0] -= evaluation;
    // int sign = 1;
    // if(derivative[0] < 0) {
    //   sign = -1;
    // }
    derivative[0] *= derivative[0] * derivative[0];
    return derivative;
  }

  private void activateInputs(long[] position, ArrayList<double[]> activations) {
    // double[] inputLayer = new double[layerSizes[0]];
    // for(int i = 0; i < inputLayer.length; i++) {
    //   inputLayer[i] = ithBit(position, i);
    // }
    // activations.add(inputLayer);
    double[] inputLayer = new double[layerSizes[0]];
    int index = 0;
    for(int i = 0; i < position.length; i++) {
      long positionLong = position[i];
      int[] range = rangesPerLong[i];
      index = createNodesFromLong(positionLong, range, inputLayer, index);
    }
    activations.add(inputLayer);
  }

  private int createNodesFromLong(long positionLong, int[] range, double[] inputLayer, int index) {
    for(int i = range[0]; i < range[1]; i++) {
      inputLayer[index] = ithBit(positionLong, i);
      index++;
    }
    return index;
  }

  // private int ithBit(long[] position, int i) {
  //   int longIndex = i / 64;
  //   int bitIndex = i % 64;
  //   return ithBit(position[longIndex], bitIndex);
  // }

  private int ithBit(long position, int i) {
    long mask = 1L<<i;
    long value = mask & position;
    return (int) (value>>i);
  }

  // private void activateInputsSideways(int[][] image, ArrayList<double[]> activations) {
  //   double[] inputLayer = new double[layerSizes[0]];
  //   for(int i = 0; i < image[0].length; i++) {
  //     for(int j = 0; j < image.length; j++) {
  //       inputLayer[i * image.length + j] = ((double)image[j][i]) / 5000;
  //     }
  //   }
  //   activations.add(inputLayer);
  // }

  private double[] hammondProduct(double[] toReturn, double[] toMultiply) {
    for(int i = 0; i < toReturn.length; i++) {
      toReturn[i] *= toMultiply[i];
    }
    return toReturn;
  }

  private double[][] hammondProduct(double[][] toReturn, double[][] toMultiply) {
    for(int i = 0; i < toReturn.length; i++) {
      for(int j = 0; j < toReturn[0].length; j++) {
        toReturn[i][j] *= toMultiply[i][j];
      }
    }
    return toReturn;
  }

  private void updateBiases(ArrayList<double[]> nablaBiases, double eta, int miniBatchSize) {
    for(int i = 0; i < biases.size(); i++) {
      double[] biasesAtLayer = biases.get(i);
      double[] changesAtLayer = nablaBiases.get(i);
      for(int j = 0; j < biasesAtLayer.length; j++) {
        biasesAtLayer[j] -= (eta / miniBatchSize) * changesAtLayer[j];
        changesAtLayer[j] = 0; //resets nabla for next usage
      }
    }
  }

  private void updateWeights(ArrayList<double[][]> nablaWeights, double eta, int miniBatchSize, double lambda) {
    for(int i = 0; i < weights.size(); i++) {
      double[][] weightsAtLayer = weights.get(i);
      double[][] changesAtLayer = nablaWeights.get(i);
      for(int j = 0; j < weightsAtLayer.length; j++) {
        for(int k = 0; k < weightsAtLayer[0].length; k++) {
          weightsAtLayer[j][k] -= (eta / miniBatchSize) * changesAtLayer[j][k];
          weightsAtLayer[j][k] -= eta*(lambda); //control weights better
          changesAtLayer[j][k] = 0;
        }
      }
    }
  }

  private void feedForward(ArrayList<double[]> zees, ArrayList<double[]> activations) {
    double[] a = activations.get(0);
    for(int i = 0; i < numLayers-1; i++) {
      double[][] weightsAtLayer = weights.get(i);
      double[] biasesAtLayer = biases.get(i);


      double[] outputsPreBiases = dotProduct(weightsAtLayer, a);
      double[] outputsPlusBiases = arrayAddition(outputsPreBiases, biasesAtLayer);
      zees.add(outputsPlusBiases);
      a = sigmoid(outputsPlusBiases);
      activations.add(a);
    }
  }

  private double[] sigmoid(double[] z) {
    double[] sigmoided = new double[z.length];
    for(int i = 0; i < z.length; i++) {
      sigmoided[i] = sigmoid(z[i]);
    }
    return sigmoided;
  }

  private double sigmoid(double z) {
    return 1.0 / (1.0 + Math.exp(-z));
  }

  private double[] sigmoidPrime(double[] z) {
    double[] sigmoidPrimed = new double[z.length];
    for(int i = 0; i < z.length; i++) {
      sigmoidPrimed[i] = sigmoidPrime(z[i]);
    }
    return sigmoidPrimed;
  }

  private double sigmoidPrime(double z) {
    return sigmoid(z) * (1 - sigmoid(z));
  }

  private double[] dotProduct(double[][] weights, double[] input) {
    int weightsHeight = weights.length;
    int weightsWidth = weights[0].length;
    double[] output = new double[weightsHeight];
    for(int i = 0; i < weightsHeight; i++) {
      double outputAtNode = 0.0;
      for(int j = 0; j < weightsWidth; j++) {
        outputAtNode += (weights[i][j] * input[j]);
      }
      output[i] = outputAtNode;
    }
    return output;
  }

  private double[] arrayAddition(double[] arrReturn, double[] arrAdd) {
    for(int i = 0; i < arrReturn.length; i++) {
      arrReturn[i] += arrAdd[i];
    }
    return arrReturn;
  }

  private double[][] arrayAddition(double[][] arrReturn, double[][] arrAdd) {
    for(int i = 0; i < arrReturn.length; i++) {
      for(int j = 0; j < arrReturn[0].length; j++) {
        arrReturn[i][j] += arrAdd[i][j];
      }
    }
    return arrReturn;
  }

  private void fillBiases() {
    double mean = 0;
    double sd = 1;
    for(int i = 0; i < numLayers-1; i++) {
      biases.add(randomDArray(layerSizes[i+1], mean, sd));
    }
  }

  private void fillWeights() {
    // double mean = 0;
    // double sd = 1 / Math.sqrt(layerSizes[0]);
    for(int i = 0; i < numLayers-1; i++) {
      weights.add(randomDMatrixWeights(layerSizes[i+1], layerSizes[i], i));
    }
  }

  private double[][] randomDMatrixWeights(int height, int width, int layer) {
    double[][] randomMatrix = new double[height][width];
    for(int i = 0; i < height; i++) {
      randomMatrix[i] = randomDArrayWeights(width, layer);
    }
    return randomMatrix;
  }

  private int weightID = 0;
  private double[] randomDArrayWeights(int width, int layer) {
    double sd = 1 / Math.sqrt(layerSizes[layer]);
    double[] randomArray = new double[width];
    for(int i = 0; i < width; i++) {
      randomArray[i] = r.nextGaussian() * sd;
    }
    return randomArray;
  }

  private double[][] randomDMatrix(int height, int width, double mean, double sd) {
    double[][] randomMatrix = new double[height][width];
    for(int i = 0; i < height; i++) {
      randomMatrix[i] = randomDArray(width, mean, sd);
    }
    return randomMatrix;
  }

  private double[] randomDArray(int width, double mean, double sd) {
    double[] randomArray = new double[width];
    for(int i = 0; i < width; i++) {
      randomArray[i] = r.nextGaussian()*sd + mean;
    }
    return randomArray;
  }

  private void loadData() {
    PositionEvalReader readTraining = new PositionEvalReader(trainFilePath);
    PositionEvalReader readTesting = new PositionEvalReader(testFilePath);
    trainPositions = new ArrayList<PositionAndEvaluation>(numTrainPositions);
    testPositions = new ArrayList<PositionAndEvaluation>(numTestPositions);
    fillDataList(trainPositions, readTraining);
    fillDataList(testPositions, readTesting);
    dataFilled = true;
  }

  private void fillDataList(List<PositionAndEvaluation> list, PositionEvalReader reader) {
    while(reader.hasNext()) {
      list.add(reader.next());
    }
  }


  private void shuffle(List<PositionAndEvaluation> positions) {
    int size = positions.size();
    for(int i = 0; i < size; i++) {
      int pool = size - i;
      int selection = r.nextInt(pool) + i;
      swapPositions(positions, i, selection);
    }
  }

  private void swapPositions(List<PositionAndEvaluation> positions, int positionOne, int positionTwo) {
    PositionAndEvaluation temp = positions.get(positionOne);
    positions.set(positionOne, positions.get(positionTwo));
    positions.set(positionTwo, temp);
  }

  public static void main(String[] args) {
    int secondsBetweenCheck = 90;
    int redundantBits = 74;
    Network firstAttempt = new Network(new int[] {896-redundantBits, 500, 100, 100, 1});
    firstAttempt.SGD(1, 30, .004, true, .00002, secondsBetweenCheck * 1000);
  }
}
