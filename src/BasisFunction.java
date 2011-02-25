public class BasisFunction {
	final private static double DISCOUNT = 0.9;

	private static int count = 0;	
	//feature list
	final private static int ROWS_COMPLETED 		= count++;
	final private static int DIFF_ROWS_COMPLETED	= count++;
	final private static int MAX_HEIGHT				= count++;
	//final private static int MIN_HEIGHT				= count++;
	final private static int DIFF_MAX_HEIGHT		= count++;
	final private static int AVG_HEIGHT				= count++;
	final private static int DIFF_AVG_HEIGHT		= count++;
	final private static int SUM_ADJ_DIFF			= count++;
	final private static int DIFF_SUM_ADJ_DIFF		= count++;
	final private static int COVERED_GAPS			= count++;
	final private static int DIFF_COVERED_GAPS		= count++;
	
	//final private static int NEXT_MOVE_LOSE			= count++;

	final public static int FEATURE_COUNT = count;
	static double[] weight = new double[FEATURE_COUNT];
	static {

		//initial weights
		weight[ROWS_COMPLETED]		=	2;
		weight[DIFF_ROWS_COMPLETED] =	2;
		weight[MAX_HEIGHT]			=	0;
		//weight[MIN_HEIGHT]			=	0;
		weight[DIFF_MAX_HEIGHT]		=	-2;
		weight[AVG_HEIGHT]			=	0;
		weight[DIFF_AVG_HEIGHT]		=	-1;
		weight[SUM_ADJ_DIFF]		=	0;
		weight[DIFF_SUM_ADJ_DIFF]	=	-2;
		weight[COVERED_GAPS]		=	0;
		weight[DIFF_COVERED_GAPS]	=	-4;

		//weight[NEXT_MOVE_LOSE]		=	-10;

		weight = new double[] {
				2.43837989469314E-5, 8.961595503848667, 0.015925823093283387, -0.01682357361806941, 0.4248430008970081, 8.00288696739108, -0.08465044114870991, -0.003900189826416073, -0.3660016454753236, -0.8824617779319843		};

	}


	private static double[] features = new double[FEATURE_COUNT]; 
	private static double[] past = new double[FEATURE_COUNT];


	public static double[] getFeatureArray(State s, FutureState fs) {
		//simple features
		features[ROWS_COMPLETED] = fs.getRowsCleared();
		features[DIFF_ROWS_COMPLETED] = fs.getRowsCleared()- s.getRowsCleared();

		//compute height features
		heightFeatures(s, past);
		heightFeatures(fs, features);

		features[DIFF_MAX_HEIGHT] = 	features[MAX_HEIGHT]-past[MAX_HEIGHT];
		features[DIFF_AVG_HEIGHT] = 	features[AVG_HEIGHT]-past[AVG_HEIGHT];
		features[DIFF_SUM_ADJ_DIFF] = 	features[SUM_ADJ_DIFF]-past[SUM_ADJ_DIFF];
		features[DIFF_COVERED_GAPS] =	features[COVERED_GAPS]-past[COVERED_GAPS];
		//features[NEXT_MOVE_LOSE] = fs.hasLost()?1:0;

		return features;
	}

	public static void heightFeatures(State s,double[] vals) {
		//max height
		int maxHeight = -1;
		//min height
		int minHeight = Integer.MAX_VALUE;
		//avg height
		int total = 0;
		//adjacent col height
		int diffTotal = 0;
		int prev=0;//holds the previous column's height
		//covered gaps
		int coveredGaps=0;

		int[][] field = s.getField();
		for(int j=0;j<field[0].length;j++){ //by column
			int i=field.length-1;
			while(i>=0 && field[i][j]==0) i--; //go down the column till first element found
			int height = i+1;
			if(maxHeight<height) maxHeight = height;
			if(minHeight>height) minHeight = height;
			total += height;
			if(j>0) diffTotal += Math.abs((height)-prev);
			prev = height;
			i--; //skip the first element.
			while(i>=0) {
				if(field[i][j]==0) coveredGaps++;
				i--;
			}
		}
		
		vals[MAX_HEIGHT] = maxHeight;
		vals[AVG_HEIGHT] = total/(double)field[0].length;
		vals[SUM_ADJ_DIFF] = diffTotal;
		vals[COVERED_GAPS] = coveredGaps;
	}

	public static double[][] A = new double[FEATURE_COUNT][FEATURE_COUNT];
	public static double[][] b = new double[FEATURE_COUNT][1];

	private static double[][] tmpA = new double[FEATURE_COUNT][FEATURE_COUNT];
	private static double[][] mWeight = new double[FEATURE_COUNT][1];
	private static double[][] mFeatures = new double[FEATURE_COUNT][1];
	private static double[][] mFutureFeatures = new double[1][FEATURE_COUNT];
	private static double[][] mRowFeatures = new double[1][FEATURE_COUNT];
	private static double[][] changeToA = new double[FEATURE_COUNT][FEATURE_COUNT];
	private static int prevRowsCompleted = 0;

	public static void updateMatrices(State s,double[] features,double[] futureFeatures) {
		//preprocessing
		Matrix.arrayToCol(features, mFeatures);
		Matrix.arrayToRow(futureFeatures, mFutureFeatures);
		Matrix.arrayToRow(features, mRowFeatures);


		Matrix.multiply(-1*DISCOUNT, mFutureFeatures);
		Matrix.sum(mRowFeatures, mFutureFeatures);
		Matrix.product(mFeatures,mRowFeatures,changeToA);
		Matrix.sum(A,changeToA);
		Matrix.multiply(features[DIFF_ROWS_COMPLETED], mFeatures);
		Matrix.sum(b,mFeatures);
		/*
		System.out.println("A:");
		printField(A);
		System.out.println("b:");
		 */
	}
	public static void computeWeights() {
		if(Matrix.premultiplyInverse(A, b,mWeight, tmpA)==null) return;;
		Matrix.colToArray(mWeight, weight);
	}


	public static void printField(double[][] field){
		for(int i=0;i<field.length;i++) {
			for(int j=0;j<field[0].length;j++){
				System.out.print(" "+field[i][j]);
			}
			System.out.println();
		}
	}

}
