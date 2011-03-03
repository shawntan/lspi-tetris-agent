public class BasisFunction {
	final private static double DISCOUNT = 0.99;

	private static int count = 0;	
	//feature list
	final private static int ROWS_COMPLETED 		= count++;
	final private static int DIFF_ROWS_COMPLETED	= count++;
	final private static int MAX_HEIGHT				= count++;
	final private static int DIFF_MAX_HEIGHT		= count++;
	final private static int MIN_HEIGHT				= count++;
	final private static int DIFF_MIN_HEIGHT		= count++;
	final private static int AVG_HEIGHT				= count++;
	final private static int DIFF_AVG_HEIGHT		= count++;
	final private static int SUM_ADJ_DIFF			= count++;
	final private static int DIFF_SUM_ADJ_DIFF		= count++;
	final private static int COVERED_GAPS			= count++;
	final private static int DIFF_COVERED_GAPS		= count++;
	final private static int TOTAL_BLOCKS			= count++;
	final private static int TOTAL_WELL_DEPTH		= count++;
	final private static int DIFF_TOTAL_WELL_DEPTH	= count++;
	final private static int MAX_WELL_DEPTH			= count++;
	
	final public static int FEATURE_COUNT = count;
	
	static double[] weight = new double[FEATURE_COUNT];
	static {

		//initial weights
/*
		weight[ROWS_COMPLETED]		=	2;
		weight[DIFF_ROWS_COMPLETED] =	2;
		weight[MAX_HEIGHT]			=	0;
		weight[DIFF_MAX_HEIGHT]		=	-2;
		weight[AVG_HEIGHT]			=	0;
		weight[DIFF_AVG_HEIGHT]		=	-1;
		weight[SUM_ADJ_DIFF]		=	0;
		weight[DIFF_SUM_ADJ_DIFF]	=	-2;
		weight[COVERED_GAPS]		=	0;
		weight[DIFF_COVERED_GAPS]	=	-4;
*/
		//weight[NEXT_MOVE_LOSE]		=	-10;

		weight = new double[] {
				2.569079102888311E-5,
				91.92573819667726,
				-0.04681056168461907,
				-0.0021226395604664653,
				0.006003403006131504,
				1.361816288777823E-4,
				1.8818215988751974,
				90.76001747787525,
				-0.018794800985089267,
				2.7172078654351048E-5,
				-0.7279972238516823,
				-9.086153462279242,
				-0.09184812023609065,
				0.017283723692958077, 
				-0.07938074313737384,
				0
		};
	
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
		features[DIFF_MIN_HEIGHT] = 	features[MIN_HEIGHT]-past[MIN_HEIGHT];
		features[DIFF_AVG_HEIGHT] = 	features[AVG_HEIGHT]-past[AVG_HEIGHT];
		features[DIFF_SUM_ADJ_DIFF] = 	features[SUM_ADJ_DIFF]-past[SUM_ADJ_DIFF];
		features[DIFF_COVERED_GAPS] =	features[COVERED_GAPS]-past[COVERED_GAPS];
		features[DIFF_TOTAL_WELL_DEPTH] =	features[TOTAL_WELL_DEPTH]-past[TOTAL_WELL_DEPTH];
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
		int totalBlocks = 0;
		int totalWellDepth = 0;
		int maxWellDepth = 0;
		int[][] field = s.getField();
		for(int j=0;j<field[0].length;j++){ //by column
			int i=field.length-1;
			int wellDepth = 0;
			while(i>=0 && field[i][j]==0) {
				if(		(j==0				||field[i][j-1]!=0) && 
						(j==field[0].length-1||field[i][j+1]!=0)) wellDepth++; 
				i--;
			} //go down the column till first element found
			int height = i+1;
			if(maxHeight<height) maxHeight = height;
			if(maxWellDepth<wellDepth) maxWellDepth = wellDepth;
			else if(minHeight>height) minHeight = height;
			total += height;
			totalWellDepth += wellDepth;
			if(j>0) diffTotal += Math.pow(height-prev,2);
			prev = height;
			while(i>0) {
				i--;
				if(field[i][j]==0) coveredGaps++;
				else totalBlocks++;
			}
		}
		vals[MAX_WELL_DEPTH] = maxWellDepth;
		vals[MAX_HEIGHT] = maxHeight;
		vals[MIN_HEIGHT] = minHeight;
		vals[AVG_HEIGHT] = ((double)total/field[0].length);
		vals[SUM_ADJ_DIFF] = diffTotal;
		vals[COVERED_GAPS] = coveredGaps;
		vals[TOTAL_BLOCKS] = totalBlocks;
		vals[TOTAL_WELL_DEPTH] = totalWellDepth;
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
