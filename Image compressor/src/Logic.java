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
            PrintWriter writer = new PrintWriter("output.csv");

            for(File img : data.getImages()){
                BufferedImage input = ImageIO.read(img);

                if(input.getHeight() % Settings.outputSize != 0){
                    System.out.println("Please use output size that can be divided without decimal points");
                    return;
                }

                int pixelRatio = input.getHeight() / Settings.outputSize;
                StringBuilder output = new StringBuilder();
                output.append(Settings.Class).append(",");
                int count = 0;

                int r = 0; int g = 0; int b = 0;

                for(int row = 0; row < input.getHeight() - 1; row += pixelRatio){
                    for(int col = 0; col < input.getWidth(); col++){

                        for(int i = 0; i < pixelRatio; i++){
                            int rgb = input.getRGB(col, i + row);
                            r += (rgb >> 16) & 0xFF;
                            g += (rgb >> 8) & 0xFF;
                            b += rgb & 0xFF;
                        }
                        count++;
                        if(count == pixelRatio){
                            count = 0;
                            r = r/(pixelRatio*pixelRatio);
                            g = g/(pixelRatio*pixelRatio);
                            b = b/(pixelRatio*pixelRatio);
                            int finalRGB = (0xFF << 24) | (r << 16) | (g << 8) | b;
                            output.append(finalRGB).append(",");
                            r = 0; g = 0; b = 0;
                        }
                    }
                }
                output.deleteCharAt(output.length()-1); 
                writer.write(output.toString());
                writer.write("\n");
            }
            writer.close();
        }catch(Exception e){
            e.fillInStackTrace();
        }
    }
}
