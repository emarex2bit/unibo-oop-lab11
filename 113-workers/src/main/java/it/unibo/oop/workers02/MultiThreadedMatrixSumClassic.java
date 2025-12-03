package it.unibo.oop.workers02;

import java.util.ArrayList;
import java.util.List;

public class MultiThreadedMatrixSumClassic implements SumMatrix{

    private final int nthread;

    /**
     * Builds a multithreaded list sum.
     *
     * @param nthread
     *            no. of thread performing the sum.
     */
    public MultiThreadedMatrixSumClassic(int nthread) {
        this.nthread = nthread;
    }

    @Override
    public double sum(double[][] matrix) {
        long sum = 0;

        int matrixW = matrix.length;
        int matrixH = matrix[0].length;
        int matrixSize = matrixW * matrixH;

        final int size = matrixSize % nthread + matrixSize / nthread;
        /*
        * Build a list of workers
        */
        final List<Worker> workers = new ArrayList<>(nthread);
        for (int start = 0; start < matrixSize; start += size) {
            workers.add(new Worker(matrix, start, size, matrixW, matrixH));
        }
        /*
        * Start them
        */
        for (final Worker w: workers) {
            w.start();
        }
        /*
        * Wait for every one of them to finish. This operation is _way_ better done by
        * using barriers and latches, and the whole operation would be better done with
        * futures.
        */
        for (final Worker w: workers) {
            try {
                w.join();
                sum += w.getResult();
            } catch (final InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }

        /*
         * Return the sum
         */
        return sum;
    }

    private static class Worker extends Thread {
        private final double[][] elements;
        private final int startpos;
        private final int nelem;
        private final int matrixW;
        private final int matrixH;
        private long res;

        /**
         * Build a new worker.
         *
         * @param elements
         *            the list to sum
         * @param startpos
         *            the initial position for this worker
         * @param nelem
         *            the no. of elems to sum up for this worker
         */
        Worker(final double[][] elements, final int startpos, final int nelem, final int matrixW, final int matrixH) {
            super();
            this.elements = elements;
            this.startpos = startpos;
            this.nelem = nelem;
            this.matrixW = matrixW;
            this.matrixH = matrixH;
        }

        @Override
        public synchronized void run() {
            // Println used to show the working ranges for debugging purposes
            System.out.println("Working from position " + startpos + " to position " + (startpos + nelem - 1)); // NOPMD
            for (int i = startpos; i < matrixH * matrixW && i < startpos + nelem; i++) {
                int x = i / matrixW;
                int y = i % matrixW;
                this.res += this.elements[x][y];
            }
        }

        /**
         * Returns the result of summing up the integers within the list.
         *
         * @return the sum of every element in the array
         */
        public synchronized long getResult() {
            return this.res;
        }
    }
    
}
