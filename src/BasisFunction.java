
public class BasisFunction {
	
	private static int count = 0;
	
	//feature list
	final private static int CONSTANT				= count++;
	final private static int ROWS_COMPLETED 		= count++;
	final private static int DIFF_ROWS_COMPLETED	= count++;
	final private static int MAX_HEIGHT				= count++;
	final private static int DIFF_MAX_HEIGHT		= count++;
	final private static int AVG_HEIGHT				= count++;
	final private static int DIFF_AVG_HEIGHT		= count++;
	final private static int SUM_ADJ_DIFF			= count++;
	final private static int DIFF_SUM_ADJ_DIFF		= count++;
	final private static int COVERED_GAPS			= count++;
	final private static int DIFF_COVERED_GAPS		= count++;
	final private static int NEXT_MOVE_LOSE			= count++;
	
	final public static int FEATURE_COUNT = count;
	static double[] weight = new double[FEATURE_COUNT];
	static {
		//initial weights
		weight[CONSTANT]			=	1;
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
		weight[NEXT_MOVE_LOSE]		=	-10;
	}
	

	private static double[] features = new double[FEATURE_COUNT]; 
	private static double[] future = new double[FEATURE_COUNT];
	
	public static double[] getFeatureArray(State s, FutureState fs) {
		//simple features
		features[CONSTANT] = 1;
		features[ROWS_COMPLETED] = s.getRowsCleared();
		features[DIFF_ROWS_COMPLETED] = fs.getRowsCleared()-features[ROWS_COMPLETED];
		
		//compute height features
		heightFeatures(s, features);
		heightFeatures(fs, future);
		
		features[DIFF_MAX_HEIGHT] = future[MAX_HEIGHT]-features[MAX_HEIGHT];
		features[DIFF_AVG_HEIGHT] = future[AVG_HEIGHT]-features[AVG_HEIGHT];
		features[DIFF_SUM_ADJ_DIFF] = future[SUM_ADJ_DIFF]-features[SUM_ADJ_DIFF];
		features[DIFF_COVERED_GAPS] = future[COVERED_GAPS]-features[COVERED_GAPS];
		features[NEXT_MOVE_LOSE] = fs.hasLost()?1:0;
		return features;
	}

	public static void heightFeatures(State s,double[] vals) {
		//max height
		int maxHeight = -1;
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
			if(maxHeight<i) maxHeight = height;
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
}
