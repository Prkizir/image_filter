import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ForkJoinPool;


public class Filter extends RecursiveAction{
  private static final int BLUR_WINDOW = 15;
  private static final int GRAIN = 10_000;
  private int src[], dest[], width, height, start, end;

  public Filter(int start, int end, int src[], int dest[], int width, int height){
    this.start = start;
    this.end = end;
    this.src = src;
    this.dest = dest;
    this.width = width;
    this.height = height;
  }

  private void blur(int row, int col){
    int border, i, j, cells;
    int tmp_row, tmp_col, pixel, dpixel;

    float r, g, b;

    border = (BLUR_WINDOW - 1)/2;
    cells = (BLUR_WINDOW * BLUR_WINDOW);

    r = 0;
    g = 0;
    b = 0;

    for(i = -border; i <= border; i++){
      for(j = -border; j <= border; j++){
        tmp_row = Math.min( Math.max(row + i, 0), height - 1);
        tmp_col = Math.min( Math.max(col + i, 0), width - 1);

        pixel = src[(tmp_row * width) + tmp_col];

        r += (float) ((pixel & 0x00ff0000) >> 16);
        g += (float) ((pixel & 0x0000ff00) >> 8);
        b += (float) ((pixel & 0x000000ff) >> 0);
      }
    }

    dpixel = (0xff000000)
                         | (((int) (r / cells)) << 16)
                         | (((int) (g / cells)) << 8)
                         | (((int) (b / cells)) << 0);

    dest[(row * width) + col] = dpixel;
  }

  public void grey(int row, int col){

  }

  public void hue(int row, int col){

  }

  public void edge(int row, int col){

  }

  protected void computeDirectly(){
    int index;
    int row, col;

    for(index = start; index < end; index++){
      row = index / width;
      col = index % width;
      blur(row, col);
    }
  }

  @Override
  protected void compute(){
    if((end - start) <= GRAIN){
      computeDirectly();
    }else{
      int mid = start + ((end - start) / 2);
      invokeAll(new Filter(start, mid, src, dest, width, height),
                new Filter(mid, end, src, dest, width, height));
    }
  }

  public static void main(String[] args) throws Exception{
    ForkJoinPool pool;

    final String srcName = args[0];
    File srcFile = new File(srcName);

    final BufferedImage source = ImageIO.read(srcFile);

    int w = source.getWidth();
    int h = source.getHeight();
    int src[] = source.getRGB(0, 0, w, h, null, 0, w);
    int dest[] = new int[src.length];

    pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
    pool.invoke(new Filter(0, w * h, src, dest, w, h));

    final BufferedImage destination = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    destination.setRGB(0, 0, w, h, dest, 0, w);

    File output = new File("blur.png");
    ImageIO.write(destination, "png", output);
  }
}
