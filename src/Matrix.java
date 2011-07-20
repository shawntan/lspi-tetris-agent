import java.text.DecimalFormat;
import java.util.Arrays;


public class Matrix {
	private static DecimalFormat formatter = new DecimalFormat("#0.0");
	

	
	//preprocessing
	public static void arrayToCol(double[] w, double[][] colW) {
		for(int i=0;i<w.length;i++) colW[i][0]=w[i];
	}
	public static void arrayToRow(double[] w, double[][] rowW) {
		for(int i=0;i<w.length;i++) rowW[0][i]=w[i];
	}
	public static void colToArray(double[][] colW, double[] w) {
		for(int i=0;i<w.length;i++) w[i]=colW[i][0];
	}
	
	public static double[][] copy(double[][] a,double[][] b){
		for(int i=0;i<a.length;i++) System.arraycopy(a[i],0, b[i], 0, a[i].length);
		return b;
	}
	private static int findFirstNonZero(double[][] m, int col) {
		for(int i=col;i<m.length;i++) if(m[i][col]!=0) return i;
		return -1;
	}
	private static void swapRow(double[][] m, int r1,int r2) {
		if(r1==r2) return;
		double[] t = m[r1];
		m[r1] = m[r2];
		m[r2] = t;
	}
	private static void multiplyRow(double[][] m, int r, double factor) {
		for(int i=0;i<m[r].length;i++) m[r][i] = factor*m[r][i];
	}
	private static void addMultiple(double[][] m, int r1,int r2,double factor) {
		double[] arr1=m[r1];
		double[] arr2=m[r2];
		for(int i=0;i<arr1.length;i++) arr1[i] = arr1[i] + factor * arr2[i];
	}
	
	
	private static double[][] identity(double[][] r){
		for(int i=0;i<r.length;i++)	{
			Arrays.fill(r[i],0);
			r[i][i] = 1;
		}
		return r;
	}
	
	public static void main(String[] args) {

		double[][] A = new double[][] {
				{1,2,3},
				{2,5,3},
				{1,0,8}
		};
		double[][] I = new double[3][3];
		double[][] result = new double[3][3];
		double[][] tmp = new double[3][3];
		identity(I);
		premultiplyInverse(A,I,result,tmp);
		
		printField(result);
	}
	
	public static double[][] multiply(double f,double[][] m){
		for(int i=0;i<m.length;i++) for(int j=0;j<m[0].length;j++) m[i][j] = m[i][j]*f;
		return m;
	}
	public static double[][] sum(double[][] a,double[][] b) {
		for(int i=0;i<b.length;i++) for(int j=0;j<b[0].length;j++) a[i][j] = a[i][j] + b[i][j];
		return a;
	}
	


	public static double[][] premultiplyInverse(double[][] l,double[][] r, double[][] result,double[][] tmp){
		l = copy(l,tmp);
		result = copy(r,result);
		for(int col=0;col<l.length;col++){
			int nonZero = findFirstNonZero(l,col);
			if(nonZero<0) {
				System.out.println(col+" FULL EMPTY COLUMN");
				return null;
			}
			swapRow(l,col,nonZero);
			swapRow(result,col,nonZero);
			double f = 1.0f/l[col][col];
			multiplyRow(l,col,f);
			multiplyRow(result,col,f);
			for(int row = col+1;row<l.length;row++){
				if(l[row][col]!=0) {
					double e = -1.0f*l[row][col];
					addMultiple(l,row,col,e);
					addMultiple(result,row,col,e);
				}
			}
		}
		for(int col=l.length-1;col>=0;col--) {
			for(int row = col-1;row>=0;row--){
				if(l[row][col]!=0) {
					double e = -1.0f*l[row][col];
					addMultiple(l,row,col,e);
					addMultiple(result,row,col,e);
				}
			}
		}
		return result;
	}

	public static double[][] product(double[][] a, double[][] b, double[][] result) {
		if(a[0].length != b.length) return null;
		else if(!(result.length==a.length && result[0].length==b[0].length)) return null;
		for(int i=0;i<result.length;i++) {
			for(int j=0;j<result[0].length;j++){
				result[i][j] = 0;
				for(int k=0;k<a[0].length;k++) {
					result[i][j] += a[i][k]*b[k][j];
				}
			}
		}
		return result;
	}
	
	
	private static void printField(double[][] field){
		for(int i=0;i<field.length;i++) {
			for(int j=0;j<field[0].length;j++){
				System.out.print(" "+field[i][j]);
			}
			System.out.println();
		}
	}
}
