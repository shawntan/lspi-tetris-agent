import java.util.Arrays;

public class FutureState extends State {
	public static final int COLS = 10;
	public static final int ROWS = 21;
	public static final int N_PIECES = 7;

	public boolean lost = false;

	//current turn
	private int turn = 0;
	private int cleared = 0;

	//each square in the grid - int means empty - other values mean the turn it was placed
	private int[][] field = new int[ROWS][COLS];
	//top row+1 of each column
	//0 means empty
	private int[] top = new int[COLS];


	//number of next piece
	protected int nextPiece;



	//all legal moves - first index is piece type - then a list of 2-length arrays
	protected static int[][][] legalMoves = State.legalMoves;

	//indices for legalMoves
	public static final int ORIENT = 0;
	public static final int SLOT = 1;

	//possible orientations for a given piece type
	protected static int[] pOrients = {1,2,4,4,4,2,2};

	//the next several arrays define the piece vocabulary in detail
	//width of the pieces [piece ID][orientation]
	protected static int[][] pWidth = {
		{2},
		{1,4},
		{2,3,2,3},
		{2,3,2,3},
		{2,3,2,3},
		{3,2},
		{3,2}
	};
	//height of the pieces [piece ID][orientation]
	private static int[][] pHeight = {
		{2},
		{4,1},
		{3,2,3,2},
		{3,2,3,2},
		{3,2,3,2},
		{2,3},
		{2,3}
	};
	private static int[][][] pBottom = {
		{{0,0}},
		{{0},{0,0,0,0}},
		{{0,0},{0,1,1},{2,0},{0,0,0}},
		{{0,0},{0,0,0},{0,2},{1,1,0}},
		{{0,1},{1,0,1},{1,0},{0,0,0}},
		{{0,0,1},{1,0}},
		{{1,0,0},{0,1}}
	};
	private static int[][][] pTop = {
		{{2,2}},
		{{4},{1,1,1,1}},
		{{3,1},{2,2,2},{3,3},{1,1,2}},
		{{1,3},{2,1,1},{3,3},{2,2,2}},
		{{3,2},{2,2,2},{2,3},{1,2,1}},
		{{1,2,2},{3,2}},
		{{2,2,1},{2,3}}
	};

	public int[][] getField() {
		return field;
	}

	public int getNextPiece() {
		return nextPiece;
	}

	public boolean hasLost() {
		return lost;
	}

	public int getRowsCleared() {
		return cleared;
	}

	public int getTurnNumber() {
		return turn;
	}

	public void resetToCurrentState(State s){
		int[][] field = s.getField();
		this.nextPiece = s.getNextPiece();
		this.lost = s.hasLost();
		this.cleared = s.getRowsCleared();
		this.turn = s.getTurnNumber();
		Arrays.fill(this.top,0);
		for(int i=field.length-1;i>=0;i--) {
			System.arraycopy(field[i], 0, this.field[i], 0, field[i].length);
			for(int j=0;j<top.length;j++) if(top[j]==0 && field[i][j]>0) top[j]=i+1;
		}
	}

	//random integer, returns 0-6
	private int randomPiece() {
		return 0;
	}

	//gives legal moves for 
	public int[][] legalMoves() {
		return legalMoves[nextPiece];
	}

	//make a move based on the move index - its order in the legalMoves list
	public void makeMove(int move) {
		makeMove(legalMoves[nextPiece][move]);
	}

	//make a move based on an array of orient and slot
	public void makeMove(int[] move) {
		makeMove(move[ORIENT],move[SLOT]);
	}

	//returns false if you lose - true otherwise
	public boolean makeMove(int orient, int slot) {
		turn++;
		//height if the first column makes contact
		int height = top[slot]-pBottom[nextPiece][orient][0];
		//for each column beyond the first in the piece
		for(int c = 1; c < pWidth[nextPiece][orient];c++) {
			height = Math.max(height,top[slot+c]-pBottom[nextPiece][orient][c]);
		}

		//check if game ended
		if(height+pHeight[nextPiece][orient] >= ROWS) {
			lost = true;
			return false;
		}


		//for each column in the piece - fill in the appropriate blocks
		for(int i = 0; i < pWidth[nextPiece][orient]; i++) {

			//from bottom to top of brick
			for(int h = height+pBottom[nextPiece][orient][i]; h < height+pTop[nextPiece][orient][i]; h++) {
				field[h][i+slot] = turn;
			}
		}

		//adjust top
		for(int c = 0; c < pWidth[nextPiece][orient]; c++) {
			top[slot+c]=height+pTop[nextPiece][orient][c];
		}

		int rowsCleared = 0;

		//check for full rows - starting at the top
		for(int r = height+pHeight[nextPiece][orient]-1; r >= height; r--) {
			//check all columns in the row
			boolean full = true;
			for(int c = 0; c < COLS; c++) {
				if(field[r][c] == 0) {
					full = false;
					break;
				}
			}
			//if the row was full - remove it and slide above stuff down
			if(full) {
				rowsCleared++;
				cleared++;
				//for each column
				for(int c = 0; c < COLS; c++) {

					//slide down all bricks
					for(int i = r; i < top[c]; i++) {
						field[i][c] = field[i+1][c];
					}
					//lower the top
					top[c]--;
					while(top[c]>=1 && field[top[c]-1][c]==0)	top[c]--;
				}
			}
		}


		//pick a new piece
		return true;
	}
}