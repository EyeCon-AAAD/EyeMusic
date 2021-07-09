package com.projectx.eyemusic;

import com.projectx.eyemusic.Model.MatrixUtils;

import org.junit.Assert;
import org.junit.Test;


public class MatrixUtilsTest {

    @Test
    // Testing the transpose for the matrix with the same dimensions
    public void TransposeTest1() {
        int r = 2;
        int c = 2;

        Float[][] arr1 = {{Float.valueOf(1), Float.valueOf(2)}, {Float.valueOf(3), Float.valueOf(4)}};
        Float[][] expectedArr = {{Float.valueOf(1), Float.valueOf(3)}, {Float.valueOf(2), Float.valueOf(4)}};
        Float[][] actualArr = null;

        actualArr = MatrixUtils.transpose(arr1, r, c);

        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                Assert.assertEquals(expectedArr[i][j], actualArr[i][j]);
            }
            System.out.println();
        }
    }


    @Test
    // Testing the transpose for the matrix with the different dimensions
    public void TransposeTest2() {
        int r = 2;
        int c = 4;

        Float[][] arr1 = {{Float.valueOf(1), Float.valueOf(2), Float.valueOf(6), Float.valueOf(-9)},
                {Float.valueOf(3), Float.valueOf(4), Float.valueOf(5), Float.valueOf(2)}};

        Float[][] expectedArr = {{Float.valueOf(1), Float.valueOf(3)},
                {Float.valueOf(2), Float.valueOf(4)},
                {Float.valueOf(6), Float.valueOf(5)},
                {Float.valueOf(-9), Float.valueOf(2)}};

        Float[][] actualArr = null;

        actualArr =MatrixUtils.transpose(arr1, r, c);

        for (int i = 0; i < c; i++) {
            for (int j = 0; j < r; j++) {
                Assert.assertEquals(expectedArr[i][j], actualArr[i][j]);
            }
            System.out.println();
        }
    }


    @Test
    // Testing the transpose when the matrix is null
    public void TransposeTest3() {
        int r = 2;
        int c = 2;

        Float[][] arr1 = null;
        Float[][] actualArr = null;

        actualArr = MatrixUtils.transpose(arr1, r, c);

        Assert.assertNull(actualArr);
    }


    @Test
    // Testing the transpose when the matrix is empty
    // out of bounds Exception
    public void TransposeTest4() {
        int r = 2;
        int c = 2;

        Float[][] arr1 = new Float[r][c];
        Float[][] actualArr = null;

        actualArr = MatrixUtils.transpose(arr1, r, c);
        Assert.assertNull(actualArr);
    }


    @Test
    // When the matrix has an inverse
    public void inverseTest1() {

        int r = 2;
        int c = 2;
        Float[][] arr1 = {{Float.valueOf(4), Float.valueOf(3)}, {Float.valueOf(3), Float.valueOf(2)}};
        Float[][] arr2 = {{Float.valueOf(-2), Float.valueOf(3)}, {Float.valueOf(3), Float.valueOf(-4)}};
        Float[][] arr3;

        arr3 = MatrixUtils.inverse(arr1, r, c);

        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                Assert.assertEquals(arr2[i][j], arr3[i][j]);
            }
            System.out.println();
        }
    }


    @Test
    // When the matrix does not have an inverse (have the same dimensions)
    public void inverseTest2() {
        int r = 2;
        int c = 2;
        Float[][] arr1 = {{Float.valueOf(9), Float.valueOf(6)}, {Float.valueOf(12), Float.valueOf(8)}};

        Assert.assertNull(MatrixUtils.inverse(arr1, r, c));
    }

    @Test
    // When the matrix does not have an inverse (have different dimensions)
    public void inverseTest3() {
        int r = 3;
        int c = 2;
        Float[][] arr1 = {{Float.valueOf(9), Float.valueOf(6)},
                {Float.valueOf(12), Float.valueOf(8)},
                {Float.valueOf(3), Float.valueOf(2)}};

        Assert.assertNull(MatrixUtils.inverse(arr1, r, c));
    }


    @Test
    // This is when the matrix has different dimensions than the actual matrix
    // out of bounds Exception
    public void inverseTest4() {
        int r = 3;
        int c = 3;
        Float[][] arr1 = {{Float.valueOf(9), Float.valueOf(6), Float.valueOf(6)},
                {Float.valueOf(12), Float.valueOf(8), Float.valueOf(6)}};

        Assert.assertNull(MatrixUtils.inverse(arr1, r, c));
    }


    @Test
    // multiplying two matrices with the same dimensions
    public void multiplyMatrixTest1() {
        int r = 2;
        int c = 2;
        Float[][] arr1 = {{Float.valueOf(1), Float.valueOf(2)}, {Float.valueOf(3), Float.valueOf(4)}};
        Float[][] arr2 = {{Float.valueOf(5), Float.valueOf(6)}, {Float.valueOf(0), Float.valueOf(7)}};
        Float[][] expectedArr = {{Float.valueOf(5), Float.valueOf(20)}, {Float.valueOf(15), Float.valueOf(46)}};
        Float[][] actualArr;

        actualArr = MatrixUtils.multiplyMatrix(r, c, arr1, r, c, arr2);


        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                Assert.assertEquals(expectedArr[i][j], actualArr[i][j]);
            }
            System.out.println();
        }
    }


    @Test
    // When multiplying two matrices with the different dimensions
    public void multiplyMatrixTest2() {
        int r1 = 2;
        int c1 = 2;
        Float[][] arr1 = {{Float.valueOf(1), Float.valueOf(5)}, {Float.valueOf(-1), Float.valueOf(2)}};
        int r2 = 2;
        int c2 = 3;
        Float[][] arr2 = {{Float.valueOf(1), Float.valueOf(5), Float.valueOf(0)},
                {Float.valueOf(4), Float.valueOf(0), Float.valueOf(2)}};
        Float[][] expectedArr = {{Float.valueOf(21), Float.valueOf(5), Float.valueOf(10)},
                {Float.valueOf(7), Float.valueOf(-5), Float.valueOf(4)}};
        Float[][] actualArr;

        actualArr = MatrixUtils.multiplyMatrix(r1, c1, arr1, r2, c2, arr2);


        for (int i = 0; i < r1; i++) {
            for (int j = 0; j < c2; j++) {
                Assert.assertEquals(expectedArr[i][j], actualArr[i][j]);
            }
            System.out.println();
        }
    }


    @Test
    // When multiplying two matrices with the different dimensions  (multiplication is not applicable)
    public void multiplyMatrixTest3() {
        int r1 = 2;
        int c1 = 2;
        Float[][] arr1 = {{Float.valueOf(1), Float.valueOf(5)}, {Float.valueOf(-1), Float.valueOf(2)}};
        int r2 = 2;
        int c2 = 3;
        Float[][] arr2 = {{Float.valueOf(1), Float.valueOf(5), Float.valueOf(0)},
                {Float.valueOf(4), Float.valueOf(0), Float.valueOf(2)}};

        Float[][] expectedArr;
        Float[][] actualArr;

        Assert.assertNull(MatrixUtils.multiplyMatrix(r2, c2, arr2, r1, c1, arr1));
    }


    @Test
    // When multiplying two matrices with the different dimensions (not applicable for multiplication)
    public void multiplyMatrixTest4() {
        int r1 = 2;
        int c1 = 2;
        Float[][] arr1 = {{Float.valueOf(1), Float.valueOf(5)}, {Float.valueOf(-1), Float.valueOf(2)}};
        int r2 = 3;
        int c2 = 3;
        Float[][] arr2 = {{Float.valueOf(1), Float.valueOf(5), Float.valueOf(0)},
                {Float.valueOf(4), Float.valueOf(0), Float.valueOf(2)},
                {Float.valueOf(3), Float.valueOf(5), Float.valueOf(1)}};

        Float[][] expectedArr;
        Float[][] actualArr;

        Assert.assertNull(MatrixUtils.multiplyMatrix(r1, c1, arr1, r2, c2, arr2));
    }


    @Test
    // Testing when is one of the matrix is null
    // #needs handling
    public void multiplyMatrixTest5() {
        int r1 = 2;
        int c1 = 2;
        Float[][] arr1 = null;
        int r2 = 2;
        int c2 = 3;
        Float[][] arr2 = {{Float.valueOf(1), Float.valueOf(5), Float.valueOf(0)},
                {Float.valueOf(4), Float.valueOf(0), Float.valueOf(2)}};

        Float[][] expectedArr;
        Float[][] actualArr;

        Assert.assertNull(MatrixUtils.multiplyMatrix(r1, c1, arr1, r2, c2, arr2));
    }


    @Test
    // Testing when is one of the matrix is empty
    // out of bounds exception
    public void multiplyMatrixTest6() {
        int r1 = 2;
        int c1 = 2;
        Float[][] arr1 = new Float[r1][c1];
        int r2 = 2;
        int c2 = 3;
        Float[][] arr2 = {{Float.valueOf(1), Float.valueOf(5), Float.valueOf(0)},
                {Float.valueOf(4), Float.valueOf(0), Float.valueOf(2)}};

        Float[][] expectedArr;
        Float[][] actualArr;

        Assert.assertNull(MatrixUtils.multiplyMatrix(r1, c1, arr1, r2, c2, arr2));
    }


    @Test
    // When multiplying two matrices one of them has lower dimensions than what is sent to the function)
    public void multiplyMatrixTest7() {
        int r1 = 2;
        int c1 = 2;
        Float[][] arr1 = {{Float.valueOf(1), Float.valueOf(5)}, {Float.valueOf(-1), Float.valueOf(2)}};
        int r2 = 2;
        int c2 = 3;
        Float[][] arr2 = {{Float.valueOf(1), Float.valueOf(5), Float.valueOf(0)},
                {Float.valueOf(4), Float.valueOf(0), Float.valueOf(2)}};
        Float[][] expectedArr;
        Float[][] actualArr;

        Assert.assertNull(MatrixUtils.multiplyMatrix(3, c1, arr1, r2, c2, arr2));
    }

}
