import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File("image.txt"));
        String[] input = scanner.nextLine().split(",");
        scanner.close();

        int size = (int) Math.sqrt(input.length);
        BufferedImage output = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);

        for(int row = 0; row < size; row++) {
            for(int col = 0; col < size; col++) {
                output.setRGB(col, row, Integer.parseInt(input[row*size + col]));
            }
        }

        JFrame frame = new JFrame("Image Decoder");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 539);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(output, 0, 0, 500, 500, null);
            }
        };

        frame.add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}