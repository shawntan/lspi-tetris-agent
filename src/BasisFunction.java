import java.util.LinkedList;

public class BasisFunction {

	// discount value, part of the LRQ algo.
	final private static double DISCOUNT = 0.95f;

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
	final private static int DIFF_ROWS_COMPLETED			= count++;	
	final private static int AVG_HEIGHT						= count++;	//average height
	final private static int DIFF_AVG_HEIGHT				= count++;
	final private static int MAX_MIN_DIFF					= count++;
	final private static int SUM_ADJ_DIFF					= count++;
	final private static int SUM_ADJ_DIFF_SQUARED			= count++;	//(sum of the difference between adjacent columns)^2
	final private static int COVERED_GAPS					= count++;	//holes in the tetris wall which are inaccessible from the top
	final private static int DIFF_COVERED_GAPS				= count++;
	final private static int TOTAL_BLOCKS					= count++;	//total number of blocks in the wall
	final private static int TOTAL_WELL_DEPTH				= count++;	//total depth of all wells on the tetris wall.
	final private static int MAX_WELL_DEPTH					= count++;	//maximum well depth
	final private static int WEIGHTED_WELL_DEPTH			= count++;	//the deeper the well is, the heavier the "weightage".
	final private static int COL_TRANS						= count++;
	final private static int ROW_TRANS						= count++;
	final private static int LANDING_HEIGHT					= count++;
	final private static int COL_STD_DEV					= count++;
	final private static int CENTER_DEV						= count++;
	final private static int ERODED_PIECE_CELLS				= count++;
	final private static int WEIGHTED_ERODED_PIECE_CELLS	= count++;

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
				 20.07482336367446,
				 2.504107165297028,
				 19.072732065292442,
				 0.024889433608669784,
				 2.0548837887730985E-4,
				 4.49087596401086E-6,
				 -0.7035532536673019,
				 -1.8997079669477919,
				 -0.16419005900025146,
				 0.018896366295955338,
				 0.05076552158059042,
				 -0.07417944589739581,
				 -0.012147833155578873,
				 -0.03597940128825008,
				 -0.0038972989874879694,
				 -0.09229158816408412,
				 -3.4512876575453324E-4,
				 -6.149474578748582E-4,
				 8.745117810322921E-4,
		};
		
		/*
 	23.74474417053898,
	1.2024033183171208,
	22.74032086120765,
	0.021145298053612593,
	-1.486942609572223E-4,
	1.0445304501815667E-4,
	-0.614538615850557,
	-2.2652395193741364,
	-0.03102014474216557,
	0.018737764423348156,
	0.04928081052583025,
	-0.07107835720443426,
	0.017148581644446166,
	-0.033105218224697856,
	-0.027968541588988448,
	-0.08716907387981934,
	-2.509824430697765E-4,
	-2.504475618057079E-5,
	-1.4530678881998514E-6
	
	 19.38657887363557
 1.0363325655090119
 18.384335122536275
 0.036817213703497616
 8.276605200598446E-4
 -9.427837820890066E-5
 -0.6452955446655471
 -1.8330123643710923
 -0.018928605061267405
 0.02103671138821449
 0.05857736152626049
 -0.08027420951261242
 0.01977545879033711
 -0.03740666459819019
 -0.00449495924275224
 -0.10379041405947873
 -3.238881705854106E-4
 -3.8139762119788204E-4
 0.0015247893524023282	
 
Max of 3 MILLION!!!
  19.785722725493283
 20.434023617877553
 18.783517510442884
 0.026979339172688164
 1.8513103789886357E-4
 -2.538411794208279E-6
 -2.523664432529036
 -1.871700407275115
 -1.9573364952495529
 0.0183510750178004
 0.052154965374291165
 -0.07496741347861241
 -4.4338392815110837E-4
 -0.03596361639341536
 -0.0039219472620906515
 -0.09588280476157182
 -2.8868170566731885E-4
 -5.545583985700238E-4
 8.661120069768446E-4

Average of 5: 1 mil
  20.589898787386733
 3.692363883642943
 19.58782311455419
 0.023268588108206333
 3.5407575764002415E-4
 -6.400299946394538E-6
 -0.7971899131999098
 -1.950379454759538
 -0.28308331330672465
 0.01925696095449016
 0.050355974911090666
 -0.0735425646943785
 -0.028509449641577412
 -0.03702501818692896
 -0.003990954296708827
 -0.0883412955239141
 -4.104209313557069E-4
 -7.338941241529237E-4
 9.004628225105676E-4

 20.07482336367446
 2.504107165297028
 19.072732065292442
 0.024889433608669784
 2.0548837887730985E-4
 4.49087596401086E-6
 -0.7035532536673019
 -1.8997079669477919
 -0.16419005900025146
 0.018896366295955338
 0.05076552158059042
 -0.07417944589739581
 -0.012147833155578873
 -0.03597940128825008
 -0.0038972989874879694
 -0.09229158816408412
 -3.4512876575453324E-4
 -6.149474578748582E-4
 8.745117810322921E-4

		 */
		/*Pierre Dellacherie*/
/*
		weight[LANDING_HEIGHT] = -1;
		weight[ROW_TRANS] = -1;
		weight[COL_TRANS] = -1;
		weight[COVERED_GAPS] = -4;
		weight[TOTAL_WELL_DEPTH] = -1;
		weight[WEIGHTED_ERODED_PIECE_CELLS] = 1;
		//weight[CENTER_DEV] = 0.5;
		 * 
		 */
		
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
		features[DIFF_AVG_HEIGHT] = 	features[AVG_HEIGHT] - past[AVG_HEIGHT];
		features[DIFF_COVERED_GAPS] =	features[COVERED_GAPS]-past[COVERED_GAPS];

		//features[DIFF_MAX_HEIGHT] = 	features[MAX_HEIGHT]-past[MAX_HEIGHT];
		//features[DIFF_SUM_ADJ_DIFF] = 	features[SUM_ADJ_DIFF]-past[SUM_ADJ_DIFF];
		//features[DIFF_TOTAL_WELL_DEPTH] =	features[TOTAL_WELL_DEPTH]-past[TOTAL_WELL_DEPTH];

		features[CENTER_DEV] = Math.abs(move[State.SLOT] - State.COLS/2.0);
		features[WEIGHTED_ERODED_PIECE_CELLS] = (fs.getRowsCleared()- s.getRowsCleared())*features[ERODED_PIECE_CELLS];
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
				if((field[i][j]==0)!=(field[i+1][j]==0))	colTrans++;
				if(i<top[j] && field[i][j]==0) 				coveredGaps++; 
				if(field[i][j]!=0) 							totalBlocks++;
				if(field[i][j]==turnNo)						currentPieceCells++;
				if(i==0 && field[i][j]==0)	colTrans++; 
			}
		}
		vals[ERODED_PIECE_CELLS] = 4 - currentPieceCells;
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
			diffTotal += (j>0)?top[j-1]-top[j]:0;
			squaredDiffTotal += (j>0)?Math.pow(top[j-1],2)-Math.pow(top[j],2):0;
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
		vals[AVG_HEIGHT] = ((double)total)/State.COLS;
		vals[SUM_ADJ_DIFF] = diffTotal;
		vals[SUM_ADJ_DIFF_SQUARED] = squaredDiffTotal;
		vals[MAX_MIN_DIFF] = maxHeight-minHeight;
		vals[COL_STD_DEV] = (totalHeightSquared - total*vals[AVG_HEIGHT])/(double)(State.COLS-1);
		
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
		printField(mWeight);
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
