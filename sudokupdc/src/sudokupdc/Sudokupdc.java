package sudokupdc;

import java.util.Stack;

public class Sudokupdc {
    public static void main(String[] args) {
        ColumnNode head = new ColumnNode();
        MakeData md = new MakeData(head);
        ColumnNode[] columns = md.makeColumns(4 * 9 * 9);
        Node[] matrix = md.makeMatrix(columns);
        Stack<Integer> input = new Stack<>();
        input.push(-1);
        AlgorithmX ax = new AlgorithmX(columns[0], matrix, input, new Stack<>());
        ax.search(0, false, false, false, false, false);
        
        int[][] data = new int[9][9], sol = new int[9][9];
        for (int node : ax.solution)
            data[(node / 4) / 81][(node / 4) / 9 % 9] = ((node / 4) % 9) + 1;
        for (int node : ax.solution2)
            sol[(node / 4) / 81][(node / 4) / 9 % 9] = ((node / 4) % 9) + 1;
        
        SudokuDB sudokudb = new SudokuDB();
        //sudokudb.deletePuzzleTable();
        sudokudb.insertPuzzleTable(data, sol);
        new BoardUI(data);
    }
}
