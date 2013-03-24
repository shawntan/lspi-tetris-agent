
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

	final private static int MAX_HEIGHT						= count++;
	final private static int COVERED_GAPS					= count++;	//(PD)holes in the tetris wall which are inaccessible from the top
	final private static int DIFF_ROWS_COMPLETED			= count++;	//(LSPI paper)
	final private static int MAX_MIN_DIFF					= count++;	//(Novel)
	final private static int MAX_WELL_DEPTH					= count++;	//(Novel)maximum well depth
	final private static int TOTAL_WELL_DEPTH				= count++;	//(PD)total depth of all wells on the tetris wall.
	final private static int TOTAL_BLOCKS					= count++;	//(CF)total number of blocks in the wall
	final private static int COL_TRANS						= count++;	//(PD)
	final private static int ROW_TRANS						= count++;	//(PD)
	final private static int DIFF_AVG_HEIGHT				= count++;	//(LSPI paper)
	final private static int SUM_ADJ_DIFF					= count++;	//(Handout)
	final private static int DIFF_COVERED_GAPS				= count++;	//(Novel)		
	final private static int WEIGHTED_WELL_DEPTH			= count++;	//(CF)the deeper the well is, the heavier the "weightage".
	final private static int LANDING_HEIGHT					= count++;	//(PD)
	final private static int COL_STD_DEV					= count++;	//(Novel)
//	final private static int ERODED_PIECE_CELLS				= count++;	//Intemediary step for WEIGHTED_ERODED_PIECE_CELLS
//	final private static int WEIGHTED_ERODED_PIECE_CELLS	= count++;	//(PD)
//	final private static int CENTER_DEV						= count++;	//(PD) priority value used to break tie in PD
//	final private static int SUM_ADJ_DIFF_SQUARED			= count++;	//(Novel)(sum of the difference between adjacent columns)^2
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
				-0.180164448251231,
				-0.4000820857296077,
				24.77849828060256,
				0.2064192581442733,
				0.045032898990512216,
				0.021320544290795714,
				0.08917677230797055,
				0.00819042152617841,
				-0.03390773883019901,
				23.781352321845173,
				-0.010694297443763184,
				-2.3906661321409253,
				-0.08863636063644799,
				-0.005240305848723416,
				-0.1524174444288417,
//				-6.525668167122544E-4,
//				-8.272234730529223E-4
			};

	}
	private double[] features = new double[FEATURE_COUNT]; 
	private double[] past     = new double[FEATURE_COUNT];

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
		features[DIFF_AVG_HEIGHT]   = 	features[DIFF_AVG_HEIGHT] - past[DIFF_AVG_HEIGHT];
		features[DIFF_COVERED_GAPS] =	features[COVERED_GAPS]-past[COVERED_GAPS];

		//features[DIFF_MAX_HEIGHT] = 	features[MAX_HEIGHT]-past[MAX_HEIGHT];
		//features[DIFF_SUM_ADJ_DIFF] = 	features[SUM_ADJ_DIFF]-past[SUM_ADJ_DIFF];
		//features[DIFF_TOTAL_WELL_DEPTH] =	features[TOTAL_WELL_DEPTH]-past[TOTAL_WELL_DEPTH];

		//features[CENTER_DEV] = Math.abs(move[State.SLOT] - State.COLS/2.0);
//		features[WEIGHTED_ERODED_PIECE_CELLS] = (fs.getRowsCleared()- s.getRowsCleared())*features[ERODED_PIECE_CELLS];
		features[LANDING_HEIGHT] = s.getTop()[move[State.SLOT]];
		return features;
	}

	public void  cellOperations(int[]top,int[][] field,double[] vals,int turnNo){
		int rowTrans=0;
		int colTrans=0;
		int coveredGaps=0;
		int totalBlocks = 0;
		int currentPieceCells = 0;
		for(int i=0;i<State.ROWS-1;i++){
			if(field[i][0]==0) rowTrans++;
			if(field[i][State.COLS-1]==0)rowTrans++;
			for(int j=0;j<State.COLS;j++){
				if(j>0 &&((field[i][j]==0)!=(field[i][j-1]==0)))	rowTrans++;
				if((field[i][j]==0)!=(field[i+1][j]==0))			colTrans++;
				if(i<top[j] && field[i][j]==0) 						coveredGaps++; 
				if(field[i][j]!=0) 									totalBlocks++;
				if(field[i][j]==turnNo)								currentPieceCells++;
			}
		}
//		vals[ERODED_PIECE_CELLS] = 4 - currentPieceCells;
		vals[COL_TRANS] = colTrans;
		vals[ROW_TRANS] = rowTrans;
		vals[COVERED_GAPS] = coveredGaps;
		vals[TOTAL_BLOCKS] = totalBlocks;
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
		double maxWellDepth=0,
		totalWellDepth=0,
		totalWeightedWellDepth=0,
		maxHeight=0,
		minHeight = Integer.MAX_VALUE,
		total=0,
		totalHeightSquared = 0,
		diffTotal = 0,
		squaredDiffTotal=0;
		for(int j=0;j<State.COLS;j++){ //by column
			total += top[j];
			totalHeightSquared += Math.pow(top[j], 2);	
			diffTotal += (j>0)?Math.abs(top[j-1]-top[j]):0;
			squaredDiffTotal += (j>0)?Math.abs(Math.pow(top[j-1],2)-Math.pow(top[j],2)):0;
			maxHeight = Math.max(maxHeight,top[j]);
			minHeight = Math.min(minHeight,top[j]);

			if((j==0||top[j-1]>top[j]) && (j==c||top[j+1]>top[j])) {
				int wellDepth = (j==0)?top[j+1]-top[j]: (
						(j==c)?top[j-1]-top[j]:
							Math.min(top[j-1],top[j+1])-top[j]
				);
				maxWellDepth 			= Math.max(wellDepth, maxWellDepth);
				totalWellDepth 			+= maxWellDepth;
				totalWeightedWellDepth 	+= (wellDepth*(wellDepth+1))/2;
			}
		}
		cellOperations(top,field,vals,currentTurn);
		vals[MAX_WELL_DEPTH] = maxWellDepth;
		vals[TOTAL_WELL_DEPTH] = totalWellDepth;
		vals[WEIGHTED_WELL_DEPTH] = totalWeightedWellDepth;
		vals[DIFF_AVG_HEIGHT] = ((double)total)/State.COLS;
		vals[SUM_ADJ_DIFF] = diffTotal;
		//vals[SUM_ADJ_DIFF_SQUARED] = squaredDiffTotal;
		vals[MAX_MIN_DIFF] = maxHeight-minHeight;
		vals[MAX_HEIGHT] = maxHeight;
		vals[COL_STD_DEV] = (totalHeightSquared - total*((double)total)/State.COLS)/(double)(State.COLS-1);
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
		//printField(mWeight);
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
