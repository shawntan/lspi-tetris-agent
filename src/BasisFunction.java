import java.util.LinkedList;

public class BasisFunction {
	
	// discount value, part of the LRQ algo.
	final private static double DISCOUNT = 0.96f;

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
	final private static int DIFF_ROWS_COMPLETED	= count++;	
	final private static int AVG_HEIGHT				= count++;	//average height
	final private static int DIFF_AVG_HEIGHT		= count++;
	final private static int MAX_MIN_DIFF			= count++;
	final private static int SUM_ADJ_DIFF			= count++;
	final private static int SUM_ADJ_DIFF_SQUARED	= count++;	//(sum of the difference between adjacent columns)^2
	final private static int COVERED_GAPS			= count++;	//holes in the tetris wall which are inaccessible from the top
	final private static int DIFF_COVERED_GAPS		= count++;
	final private static int TOTAL_BLOCKS			= count++;	//total number of blocks in the wall
	final private static int TOTAL_WELL_DEPTH		= count++;	//total depth of all wells on the tetris wall.
	final private static int MAX_WELL_DEPTH			= count++;	//maximum well depth
	final private static int WEIGHTED_WELL_DEPTH	= count++;	//the deeper the well is, the heavier the "weightage".
	final private static int COL_TRANS				= count++;
	final private static int ROW_TRANS				= count++;
	final private static int LANDING_HEIGHT			= count++;
	final private static int COL_STD_DEV			= count++;
	final private static int CENTER_DEV				= count++;
	final private static int ERODED_PIECE_CELLS		= count++;
	final private static int WEIGHTED_ERODED_PIECE_CELLS= count++;
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
	/*	weight = new double[] {
	20.578266869033975,
	-1.9961708316702538,
	19.574318704724078,
	0.034957834976101165,
	-0.03163332598052833,
	2.3203163588352842E-4,
	-0.5363685563885567,
	-1.9514805749833875,
	0.29285856962669254,
	0.013904491691460229,
	0.048574877444784864,
	-0.07528745119550233,
	0.012592014254003208,
	0.23008383517333306,
	-0.0017813203564721663,
	-0.09573483848959048,
	0.0013692744123832703,
	2.2480970728764595E-4,
	9.129958079504793E-4

		};*/
		/*
		weight[DIFF_ROWS_COMPLETED] = 20;
		weight[AVG_HEIGHT]=-2;
		weight[DIFF_AVG_HEIGHT]	= 15;
		weight[MAX_MIN_DIFF]= 0.1;
		weight[SUM_ADJ_DIFF] = -0.01; 
		weight[COVERED_GAPS] = -0.7;
		weight[DIFF_COVERED_GAPS] = -2;
		weight[TOTAL_BLOCKS] = 0.2;	//total number of blocks in the wall
		weight[TOTAL_WELL_DEPTH] = -0.006;	//total depth of all wells on the tetris wall.
		weight[MAX_WELL_DEPTH] = 0.1;	//maximum well depth
		weight[WEIGHTED_WELL_DEPTH]	= -0.07;	//the deeper the well is, the heavier the "weightage".
		weight[COL_TRANS]=0.01;
		weight[ROW_TRANS]=0.2;
		weight[LANDING_HEIGHT]=-0.1;
		weight[COL_STD_DEV]=-0.1;
		*/
		/*Pierre Dellacherie*/
		
		weight[LANDING_HEIGHT] = -1;
		weight[ROW_TRANS] = -1;
		weight[COL_TRANS] = -1;
		weight[COVERED_GAPS] = -4;
		weight[WEIGHTED_WELL_DEPTH] = -1;
		weight[WEIGHTED_ERODED_PIECE_CELLS] = 1;
		weight[CENTER_DEV] = 0.5;
		
		
	}



	private double[] features = new double[FEATURE_COUNT]; 
	private double[] past = new double[FEATURE_COUNT];

	/**
	 * Function to get feature array for current state.
	 */
	public double[] getFeatureArray(State s, FutureState fs,int[] move) {
		//simple features
		//features[ROWS_COMPLETED] = fs.getRowsCleared();
		features[DIFF_ROWS_COMPLETED] = fs.getRowsCleared()- s.getRowsCleared();
		//compute height features
		int currentTurn = s.getTurnNumber();
		int currentPiece = s.getNextPiece();
		heightFeatures(s, past,currentPiece,currentTurn);
		heightFeatures(fs, features,currentPiece,currentTurn);
		//System.out.println(Arrays.toString(past));
		//features[DIFF_MAX_HEIGHT] = 	features[MAX_HEIGHT]-past[MAX_HEIGHT];
		features[DIFF_AVG_HEIGHT] = 	features[AVG_HEIGHT] - past[AVG_HEIGHT];
		//features[DIFF_SUM_ADJ_DIFF] = 	features[SUM_ADJ_DIFF]-past[SUM_ADJ_DIFF];
		features[DIFF_COVERED_GAPS] =	features[COVERED_GAPS]-past[COVERED_GAPS];
		//features[DIFF_TOTAL_WELL_DEPTH] =	features[TOTAL_WELL_DEPTH]-past[TOTAL_WELL_DEPTH];
		features[LANDING_HEIGHT] = fs.getTop()[move[State.SLOT]]+(State.getpHeight()[currentPiece][move[State.ORIENT]]/2.0);
		features[CENTER_DEV] = Math.abs(move[State.SLOT] - State.COLS/2.0);
		features[WEIGHTED_ERODED_PIECE_CELLS] = (fs.getRowsCleared()- s.getRowsCleared())*features[ERODED_PIECE_CELLS];

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
	public void heightFeatures(State s,double[] vals,int currentPiece,int currentTurn) {
		int[][] field = s.getField();
		int[] top = s.getTop();
		int c = State.COLS-1;
		int maxWellDepth=0,
			totalWellDepth=0,
			totalWeightedWellDepth=0,
			maxHeight=0,
			minHeight = Integer.MAX_VALUE,
			total=0,
			totalHeightSquared = 0,
			diffTotal = 0,
			squaredDiffTotal=0,
			coveredGaps=0,
			totalBlocks=0,
			colTrans = 0,
			rowTrans = 0,
			currentPieceCells = 0;
		for(int j=0;j<field[0].length;j++){ //by column
			total += top[j];
			totalHeightSquared += Math.pow(top[j], 2);
			
			diffTotal += (j>0)?top[j-1]-top[j]:0;
			squaredDiffTotal += (j>0)?Math.pow(top[j-1],2)-Math.pow(top[j],2):0;
			maxHeight = Math.max(maxHeight,top[j]);
			minHeight = Math.min(minHeight,top[j]);
			
			if((j==0||top[j-1]>top[j]) && (j==c||top[j+1]>top[j])) {
				int wellDepth = (j==0)?top[j+1]-top[j]: (
									(j==c)?top[j-1]-top[j]:
										Math.min(top[j-1],top[j+1])-top[j]
								);
				maxWellDepth = Math.max(wellDepth, maxWellDepth);
				totalWellDepth += maxWellDepth;
				totalWeightedWellDepth += (wellDepth*(wellDepth+1))/2;
			}
			
			int t = top[j];
			while(t>0) {
				t--;
				if((field[t][j]==0) != (field[t+1][j]==0)) colTrans++;
				if(field[t][j]==0) coveredGaps++;
				if((j==0||field[t][j-1]!=0)!=(field[t][j]!=0)) rowTrans++;
				else {
					if(field[t][j]==currentTurn) currentPieceCells++;
					totalBlocks ++;
				}
			}
		}
		vals[MAX_WELL_DEPTH] = maxWellDepth;
		vals[TOTAL_WELL_DEPTH] = totalWellDepth;
		vals[WEIGHTED_WELL_DEPTH] = totalWeightedWellDepth;
		//vals[MAX_HEIGHT] = maxHeight;
		//vals[MIN_HEIGHT] = minHeight;
		vals[AVG_HEIGHT] = ((double)total)/State.COLS;
		vals[SUM_ADJ_DIFF] = diffTotal;
		vals[SUM_ADJ_DIFF_SQUARED] = squaredDiffTotal;
		vals[COVERED_GAPS] = coveredGaps;
		vals[TOTAL_BLOCKS] = totalBlocks;
		vals[COL_TRANS]	= colTrans;
		vals[ROW_TRANS] = rowTrans;
		vals[MAX_MIN_DIFF] = maxHeight-minHeight;
		vals[COL_STD_DEV] = (totalHeightSquared - total*vals[AVG_HEIGHT])/(double)(State.COLS-1);
		vals[ERODED_PIECE_CELLS] = 4 - currentPieceCells;
		//System.out.println(colTrans);
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
		LinkedList<String> test = new LinkedList<String>();
		
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
