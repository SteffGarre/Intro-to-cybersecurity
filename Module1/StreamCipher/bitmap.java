
import java.util.Random;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;


public class bitmap {

  public static BufferedImage map(int width, int height, long seed) {

    MyRandom random = new MyRandom(seed);
    //Random random = new Random(seed);

    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        if ((random.nextInt(256) % 2) == 0) {
          image.setRGB(x, y, Color.BLACK.getRGB());
        } else {
          image.setRGB(x, y, Color.WHITE.getRGB());
        }
      }
    }

    return image;
  }

  private static void savePNG(final BufferedImage bi, final String path) {
    try {
      RenderedImage rendImage = bi;
      ImageIO.write(rendImage, "bmp", new File(path));
      //ImageIO.write(rendImage, "PNG", new File(path));
      //ImageIO.write(rendImage, "jpeg", new File(path));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void testRandom(int seed) {
    MyRandom myRandom = new MyRandom(seed);
    Random random = new Random(seed);

    System.out.println("StreamCipher.StreamCipher.MyRandom");
    for (int i = 0; i < 10; i++) {

      System.out.println(myRandom.nextInt(256));
    }
    
    System.out.println("Random");
    for (int i = 0; i < 10; i++) {
      System.out.println(random.nextInt(256));

    }
  }

  public static void main(String[] args) {
    
    //testRandom(123123323);
    BufferedImage img = map(512, 512, 12321312);
    savePNG(img, "bitmapRandom.bmp");
  }

}