import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;

public class Logic {
    private final Data data;

    public Logic() {
        this.data = new Data();
        convert();
    }

    private void convert(){
        try {
            PrintWriter output = new PrintWriter("output.csv");

            for(File img : data.getImages()){
                BufferedImage input = ImageIO.read(img);

                if(input.getHeight() % Settings.outputSize != 0){
                    System.out.println("Please use output size that can be divided without decimal points");
                    return;
                }

                int pixelRatio = input.getHeight() / Settings.outputSize;

                for(int row = 1; row < input.getHeight(); row += pixelRatio){
                    System.out.println("Row " + row);
                }

            }
        }catch(Exception e){
            e.fillInStackTrace();
        }
    }
}
