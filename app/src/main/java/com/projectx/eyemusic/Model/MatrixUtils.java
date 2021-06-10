package com.projectx.eyemusic.Model;

/*
* reference from GeeksforGeeks
* https://www.geeksforgeeks.org/java-program-to-multiply-two-matrices-of-any-size/
* https://www.geeksforgeeks.org/adjoint-inverse-matrix/
* https://www.geeksforgeeks.org/program-to-find-transpose-of-a-matrix/
*/


import android.util.Log;

public class MatrixUtils {
    private static final String TAG = "MatrixUtils";

    // Function to multiply
    // two matrices A[][] and B[][]
    public static Float[][] multiplyMatrix(
            int row1, int col1, Float A[][],
            int row2, int col2, Float B[][])
    {

        if(A == null || B == null) {

            return null;
        }

        for (Float[] row: A) {
            if (row == null){
                Log.e(TAG, "multiplyMatrix: matrix A is not initialized correctly");
                return null;
            }
        }

        for (Float[] row: B) {
            if (row == null){
                Log.e(TAG, "multiplyMatrix: matrix B is not initialized correctly");
                return null;
            }
        }

        int i, j, k;

        // Check if multiplication is Possible
        if (row2 != col1) {

            Log.e(TAG, "multiplyMatrix: the multiplication is not possible because of the incompatible dimentions row2:" + row2+" col1:" +  col1);
            return null;
        }

        // Matrix to store the result
        // The product matrix will
        // be of size row1 x col2
        Float C[][] = new Float[row1][col2];

        for (i = 0; i < row1; i++) {
            for (j = 0; j < col2; j++)
                C[i][j] = (float) 0;
        }

        // Multiply the two marices
        for (i = 0; i < row1; i++) {
            for (j = 0; j < col2; j++) {
                for (k = 0; k < row2; k++)
                    C[i][j] += A[i][k] * B[k][j];
            }
        }

        return C;
    }

    // This function stores transpose
    // of A[][] in B[][]
    public static  Float [] [] transpose(Float A[][], int rows, int cols)
    {
        if (A == null){
            Log.e(TAG, "inverse: matrix A is null");
            return null;
        }

        if(A.length != rows){
            Log.e(TAG, "inverse: the argument rows does not match the length of the matrix");
            return null;
        }
        for (Float[] row: A) {
            if(row.length != cols){
                Log.e(TAG, "inverse: the argument cols does not match structure of the matrix");
                return null;
            }
        }

        Float B[][];
        B = new Float[cols][rows];

        int i, j;
        for (i = 0; i < rows; i++) {
            for (j = 0; j < cols; j++) {
                B[j][i] = A[i][j];
            }
        }
        return B;
    }

    // Function to get inverse of A[N][N]
    public static Float[][] inverse(Float A[][], int rows, int cols){
        if (rows != cols){
            Log.e(TAG, "inverse: the rows and cols should be same size");
            return null;
        }

        if (A == null){
            Log.e(TAG, "inverse: matrix A is null");
            return null;
        }

        if(A.length != rows){
            Log.e(TAG, "inverse: the argument rows does not match the length of the matrix");
            return null;
        }
        for (Float[] row: A) {
            if(row.length != cols){
                Log.e(TAG, "inverse: the argument cols does not match structure of the matrix");
                return null;
            }
        }

        int N = rows;


        Float [][]adj =  new Float[N][N];
        Float [][]inv = new Float[N][N];

        adjoint(A, adj, N);
        if (inverseCal(A, inv, N))
            return inv;
        else
            return null;
    }

    // Function to calculate and store inverse, returns false if
// matrix is singular
    static boolean inverseCal(Float A[][], Float [][]inverse, int N)
    {
        // Find determinant of A[][]
        Float det = determinant(A, N, N);
        if (det == 0)
        {
            System.out.print("Singular matrix, can't find its inverse");
            return false;
        }

        // Find adjoint
        Float [][]adj = new Float[N][N];
        adjoint(A, adj, N);

        // Find Inverse using formula "inverse(A) = adj(A)/det(A)"
        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++)
                inverse[i][j] = adj[i][j]/(float)det;

        return true;
    }

    // Function to get adjoint of A[N][N] in adj[N][N].
    static Float[][] adjoint(Float A[][], Float [][]adj,  int N)
    {
        if (N == 1)
        {
            adj[0][0] = 1f;
            return adj;
        }

        // temp is used to store cofactors of A[][]
        int sign = 1;
        Float [][]temp = new Float[N][N];

        for (int i = 0; i < N; i++)
        {
            for (int j = 0; j < N; j++)
            {
                // Get cofactor of A[i][j]
                getCofactor(A, temp, i, j, N);

                // sign of adj[j][i] positive if sum of row
                // and column indexes is even.
                sign = (((i + j) % 2 == 0)? 1: -1);

                // Interchanging rows and columns to get the
                // transpose of the cofactor matrix
                adj[j][i] = (float)(sign)*(determinant(temp, N-1, N));
            }
        }

        return adj;
    }


    /* Recursive function for finding determinant of matrix.
        n is current dimension of A[][]. */
    static Float determinant(Float A[][], int n, int N)
    {
        Float D = 0f; // Initialize result

        // Base case : if matrix contains single element
        if (n == 1)
            return A[0][0];

        Float [][]temp = new Float[N][N]; // To store cofactors

        int sign = 1; // To store sign multiplier

        // Iterate for each element of first row
        for (int f = 0; f < n; f++)
        {
            // Getting Cofactor of A[0][f]
            getCofactor(A, temp, 0, f, n);
            D += (float) sign * A[0][f] * determinant(temp, n - 1, N);

            // terms are to be added with alternate sign
            sign = -sign;
        }

        return D;
    }

    // Function to get cofactor of A[p][q] in temp[][]. n is current
// dimension of A[][]
    static void getCofactor(Float A[][], Float temp[][], int p, int q, int n)
    {
        int i = 0, j = 0;

        // Looping for each element of the matrix
        for (int row = 0; row < n; row++)
        {
            for (int col = 0; col < n; col++)
            {
                // Copying into temporary matrix only those element
                // which are not in given row and column
                if (row != p && col != q)
                {
                    temp[i][j++] = A[row][col];

                    // Row is filled, so increase row index and
                    // reset col index
                    if (j == n - 1)
                    {
                        j = 0;
                        i++;
                    }
                }
            }
        }
    }

}
