import java.util.Arrays;

public class BasisFunction {
	
	// discount value, part of the LRQ algo.
	final private static double DISCOUNT = 0.96;

	/**
	 * Below are features being used for scoring.
	 * These are based on the _next_ state.
	 * This means these numbers represent the values that are reported if the attempted action is taken.
	 * The values starting with DIFF_ represent the differnece between the _next_ state and the current state
	 * 
	 * Notes:
	 * -	SUM_ADJ_DIFF seems to work better squared.
	 * -	A well is a whole starting at the top of the wall. Both sides of a well are blocks.
	 * 
	 */
	private static int count = 0;	
	//final private static int ROWS_COMPLETED 		= count++;	//number of rows completed.
	final private static int DIFF_ROWS_COMPLETED	= count++;	
	final private static int MAX_HEIGHT				= count++;	//maximum column height
	final private static int DIFF_MAX_HEIGHT		= count++;
	final private static int MIN_HEIGHT				= count++;	//minimum column height
	//final private static int DIFF_MIN_HEIGHT		= count++;
	final private static int AVG_HEIGHT				= count++;	//average height
	final private static int DIFF_AVG_HEIGHT		= count++;
	final private static int SUM_ADJ_DIFF			= count++;	//(sum of the difference between adjacent columns)^2 
	final private static int DIFF_SUM_ADJ_DIFF		= count++;
	final private static int COVERED_GAPS			= count++;	//holes in the tetris wall which are inaccessible from the top
	final private static int DIFF_COVERED_GAPS		= count++;
	final private static int TOTAL_BLOCKS			= count++;	//total number of blocks in the wall
	final private static int TOTAL_WELL_DEPTH		= count++;	//total depth of all wells on the tetris wall.
	final private static int DIFF_TOTAL_WELL_DEPTH	= count++;
	final private static int MAX_WELL_DEPTH			= count++;	//maximum well depth
	final private static int WEIGHTED_WELL_DEPTH	= count++;	//the deeper the well is, the heavier the "weightage".
	
	
	final public static int FEATURE_COUNT = count;
	

	/* A and b are important matrices in the LSPI algorithm. Every move the "player" makes, A and b are updated.
	 * 
	 * A += current_features * (current_features - DISCOUNT*future_features)
	 * 						 ^
	 * 						 -------This is a matrix multiplication with the transpose of features,
	 * 								 so for k features, a k x k matrix is produced.
	 * b += reward*current_features
	 * 
	 * reward = DIFF_ROWS_COMPLETED
	 */
	double[][] A = new double[FEATURE_COUNT][FEATURE_COUNT];
	double[][] b = new double[FEATURE_COUNT][1];
	double[] weight = new double[FEATURE_COUNT];

	{
		weight = new double[] {
				15.428045965663904,
				-0.07070737943334678,
				0.0036326018080596613,
				0.037135217654553236,
//				0.0037415507356168276,
				3.0794434270517255,
				14.420704931695028,
				-0.008114292884418063,
				-8.623554834958566E-4,
				-0.8477494257763638,
				-1.4447196123483657,
				-0.21705033071735108,
				-0.018093863655303587,
				2.4392515291062316E-4,
				0.025754085586906734,
				-0.0550392902653232
		};
	}
	


	private double[] features = new double[FEATURE_COUNT]; 
	private double[] past = new double[FEATURE_COUNT];


	/**
	 * Function to get feature array for current state.
	 */
	public double[] getFeatureArray(State s, FutureState fs) {
		//simple features
		//features[ROWS_COMPLETED] = fs.getRowsCleared();
		features[DIFF_ROWS_COMPLETED] = fs.getRowsCleared()- s.getRowsCleared();
		//compute height features
		heightFeatures(s, past);
		heightFeatures(fs, features);

		features[DIFF_MAX_HEIGHT] = 	features[MAX_HEIGHT]-past[MAX_HEIGHT];
		//features[DIFF_MIN_HEIGHT] = 	features[MIN_HEIGHT]-past[MIN_HEIGHT];
		features[DIFF_AVG_HEIGHT] = 	features[AVG_HEIGHT] - past[AVG_HEIGHT];
		features[DIFF_SUM_ADJ_DIFF] = 	features[SUM_ADJ_DIFF]-past[SUM_ADJ_DIFF];
		features[DIFF_COVERED_GAPS] =	features[COVERED_GAPS]-past[COVERED_GAPS];
		features[DIFF_TOTAL_WELL_DEPTH] =	features[TOTAL_WELL_DEPTH]-past[TOTAL_WELL_DEPTH];
		
		return features;
	}

	/**
	 * Shared method for obtaining features about the height of the tetris wall.
	 * Its pretty messy, but I didn't want to split extracting these features into
	 * separate methods, or it might end up doing redundant stuff.
	 * 
	 * @param s
	 * @param vals
	 */
	public void heightFeatures(State s,double[] vals) {
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
		int totalWeightedBlocks = 0;
		int totalWellDepth = 0;
		int totalWeightedWellDepth=0;
		int maxWellDepth = 0;
		int[][] field = s.getField();
		for(int j=0;j<field[0].length;j++){ //by column
			int i=field.length-1;
			int wellDepth = 0;
			int weightedWellDepth = 0;
			int wellWeight = 1;
			
			while(i>=0 && field[i][j]==0) {
				if(		(j==0				 ||field[i][j-1]!=0) && 
						(j==field[0].length-1||field[i][j+1]!=0)){
					wellDepth++;
					weightedWellDepth += wellWeight++;
				}
				i--;
			} //go down the column till first element found
			int height = i+1;
			if(maxHeight<height) maxHeight = height;
			if(maxWellDepth<wellDepth) maxWellDepth = wellDepth;
			else if(minHeight>height) minHeight = height;
			total += height;
			totalWellDepth += wellDepth;
			totalWeightedWellDepth += weightedWellDepth;
			if(j>0) diffTotal += Math.pow(height-prev,2);
			prev = height;
			int blockWeight = 1;
			while(i>0) {
				i--;
				if(field[i][j]==0) coveredGaps += 1;
				else{
					totalBlocks++;
					totalWeightedBlocks += blockWeight++;
				}
			}
		}
		vals[MAX_WELL_DEPTH] = maxWellDepth;
		vals[MAX_HEIGHT] = maxHeight;
		vals[MIN_HEIGHT] = minHeight;
		vals[AVG_HEIGHT] = (double)total/field[0].length;
		vals[SUM_ADJ_DIFF] = diffTotal;
		vals[COVERED_GAPS] = coveredGaps;
		vals[TOTAL_BLOCKS] = totalBlocks;
		vals[TOTAL_WELL_DEPTH] = totalWellDepth;
		vals[WEIGHTED_WELL_DEPTH] = totalWeightedWellDepth;
		
	}


	private double[][] tmpA = new double[FEATURE_COUNT][FEATURE_COUNT];
	private double[][] mWeight = new double[FEATURE_COUNT][1];
	private double[][] mFeatures = new double[FEATURE_COUNT][1];
	private double[][] mFutureFeatures = new double[1][FEATURE_COUNT];
	private double[][] mRowFeatures = new double[1][FEATURE_COUNT];
	private double[][] changeToA = new double[FEATURE_COUNT][FEATURE_COUNT];

	/**
	 * Matrix update function. See above for descriptions.
	 * 
	 * ( I know there are many arrays all over the place,
	 * 	 but I'm trying to _NOT_ create any new arrays to reduce compute time and memory
	 * 	 that's why you see all the System.arraycopy everywhere )
	 * @param s
	 * @param features
	 * @param futureFeatures
	 */
	public void updateMatrices(State s,double[] features,double[] futureFeatures) {
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
	}
	
	/**
	 * The computation of the weights can be separate from the updating of matrix A & b.
	 * 
	 * weights = A^(-1)b
	 * 
	 * The way I'm doing this in the back-end is running the Gauss-Jordan Elimination algo alongside the b matrix.
	 * This saves computing inverse of A and then multiplying it with b.
	 */
	public void computeWeights() {
		if(Matrix.premultiplyInverse(A, b,mWeight, tmpA)==null) return;;
		Matrix.colToArray(mWeight, weight);
	}


	public void printField(double[][] field){
		for(int i=0;i<field.length;i++) {
			for(int j=0;j<field[0].length;j++){
				System.out.print(" "+field[i][j]);
			}
			System.out.println();
		}
	}

}
