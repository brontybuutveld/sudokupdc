package sudokupdc;

import java.io.*;
import java.util.*;

public class AlgorithmX {
    public List<Integer> solution, solution2;
    private final Node[] matrix;
    private int count = 0;
    private final ColumnNode head;
    private final Stack<Integer> input, input2 = new Stack<>();
    enum en {UNSET, DEFAULT, USER, CONSTRAINT, DEFAULT_CONSTRAINT}

    public AlgorithmX(ColumnNode head, Node[] matrix, Stack<Integer> input, List<Integer> solution) {
        this.solution = solution;
        this.solution2 = new ArrayList<>();
        this.matrix = matrix;
        this.head = head;
        this.input = input;
    }

    public void search(int k, boolean game, boolean print, boolean solcpy, boolean first, boolean early) {

        // no columns left
        if (head.right == head) {
            count++;
            if (solcpy) this.solution2 = new ArrayList<>(solution);
            return;
        }

        if (count > 1 && early || solcpy && count > 0) return;

        // input sudoku by removing rows
        if (!input.isEmpty()) {
            handleInput(game);
            if (!input2.isEmpty()) return;
        }

        if (!input2.isEmpty() && k == 0) handleInput2();

        ColumnNode column = chooseColumn(false);
        cover(column);

        // go through all rows in the column
        for (Node node = column.down; node != column; node = node.down) {
            solution.add(node.value);

            // cover columns in row
            for (Node row2 = getRight(node); row2 != node; row2 = getRight(row2))
                cover(row2.top);

            // recur with the reduced matrix
            search(k + 1, game, print, solcpy, first, early);

            // bad path undo
            for (Node row2 = getLeft(node); row2 != node; row2 = getLeft(row2))
                uncover(row2.top);

            solution.remove(solution.size() - 1);
        }
        uncover(column);

        // keep adding rows until theres 1 solution
        if (k == 0 && count == 1 && first) firstSolution(game);
    }

    private ColumnNode chooseColumn(boolean in) {
        if (in) {
            return matrix[input.pop()].top;
        }
        ColumnNode chosenColumn = head.right;
        int minSize = head.right.len;
        for (ColumnNode i = head.right; i.right != head; i = i.right) {
            if (i.len < minSize) {
                minSize = i.len;
                chosenColumn = i;
            }
        }
        return chosenColumn;
    }


    private void cover(ColumnNode column) {
        // Remove the column from the header row
        column.right.left = column.left;
        column.left.right = column.right;

        // hide row in column
        for (Node row = column.down; row != column; row = row.down) {
            for (Node row2 = getRight(row); row2 != row; row2 = getRight(row2)) {
                row2.up.down = row2.down;
                row2.down.up = row2.up;
                row2.top.len--;
            }
        }
    }

    private void uncover(ColumnNode column) {
        // undo
        for (Node row = column.up; row != column; row = row.up) {
            for (Node row2 = getLeft(row); row2 != row; row2 = getLeft(row2)) {
                row2.top.len++;
                row2.down.up = row2;
                row2.up.down = row2;
            }
        }

        column.right.left = column;
        column.left.right = column;
    }

    private Node getRight(Node node) {
        if ((node.value % 4) != 3) // if not end of row
            node = matrix[node.value + 1];
        else
            node = matrix[node.value - 3]; //loop to start of row
        return node;
    }

    private Node getLeft(Node node) {
        if (node.value % 4 != 0) // if not end of row
            node = matrix[node.value - 1];
        else
            node = matrix[node.value + 3]; //loop to end of row
        return node;
    }

    private void handleInput2() {
        Node row = matrix[input2.peek()];
        this.solution.add(row.value);
        ColumnNode column = matrix[input2.pop()].top;
        cover(column);
        for (Node row2 = getRight(row); row != row2; row2 = getRight(row2))
            cover(row2.top);
    }

    private void handleInput(boolean game) {
        if (input.get(0) == -1) {
            input.remove(0);
            Random rand = new Random();

            for (int i = 0; i < 4; i++) {
                ColumnNode headcpy = head.right;
                int x = rand.nextInt(324 - i * 4 - 1);

                for (int j = 0; j < x; j++)
                    headcpy = headcpy.right;

                cover(headcpy);
                Node row = headcpy.down;

                while (row.down != headcpy && rand.nextInt(9) != 0)
                    row = row.down;

                for (Node row2 = getRight(row); row2 != row; row2 = getRight(row2))
                    cover(row2.top);

                solution.add(row.value);
            }

            search(0, game, false, true, true, true);
            System.out.println();
        } else {
            while (!input.isEmpty()) {
                Node row = matrix[input.peek()];
                this.solution.add(row.value);
                ColumnNode column = chooseColumn(true);
                cover(column);
                for (Node row2 = getRight(row); row != row2; row2 = getRight(row2))
                    cover(row2.top);
            }
        }
    }

    private void firstSolution(boolean game) {
        Random rand = new Random();
        solution2.remove(0);
        solution2.remove(0);
        solution2.remove(0);
        solution2.remove(0);
        while (!solution2.isEmpty()) {
            int randint = rand.nextInt(solution2.size());
            this.input2.push(solution2.get(randint));
            solution2.remove(randint);
        }
        boolean flag = true;
        count = 2;
        while (flag) {
            search(0, false, false, false, false, true);
            if (count > 1) {
                count = 0;
                Node row = matrix[this.input2.peek()];
                this.solution.add(row.value);
                cover(row.top);
                for (Node row2 = getRight(row); row != row2; row2 = getRight(row2))
                    cover(row2.top);
            } else {
                if (!game) {
                    count--;
                    search(0, false, true, true, false, false);
                }
                flag = false;
            }
        }
    }
}