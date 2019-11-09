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

  public void blur(int row, int col){
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

  private void gray(int row, int col){
    int pixel, dpixel;

    float r, g, b, avg;

    pixel = src[(row * width) + col];

    r = (float) ((pixel & 0x00ff0000) >> 16);
    g = (float) ((pixel & 0x0000ff00) >> 8);
    b = (float) ((pixel & 0x000000ff) >> 0);

    avg = (r + g + b)/3;

    dpixel = (0xff000000)
                         | ((int) avg << 16)
                         | ((int) avg << 8)
                         | ((int) avg << 0);

    dest[(row * width) + col] = dpixel;
  }

  public void hue(int row, int col){

  }

  public void edge(int row, int col){
    int topPixel, lowPixel, dpixel;
    int tmp_row;

    float rH, gH, bH, avgH;
    float rL, gL, bL, avgL;

    tmp_row = Math.min( Math.max(row + 1, 0), height - 1);

    topPixel = src[(row * width) + col];
    lowPixel = src[(tmp_row * width) + col];

    rH = (float) ((topPixel & 0x00ff0000) >> 16);
    gH = (float) ((topPixel & 0x0000ff00) >> 8);
    bH = (float) ((topPixel & 0x000000ff) >> 0);

    rL = (float) ((lowPixel & 0x00ff0000) >> 16);
    gL = (float) ((lowPixel & 0x0000ff00) >> 8);
    bL = (float) ((lowPixel & 0x000000ff) >> 0);

    avgH = (rH + gH + bH)/3;
    avgL = (rL + gL + bL)/3;

    if(0.65 >= Math.abs(avgH - avgL) &&
       0.70 >= Math.abs(avgH - avgL)){
      dpixel = (0xffffffff);
    }else{
      dpixel = (0xff000000);
    }

    dest[(row * width) + col] = dpixel;
  }

  protected void computeDirectly(){
    int index;
    int row, col;

    for(index = start; index < end; index++){
      row = index / width;
      col = index % width;

      edge(row, col);
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

    File output = new File("output.png");
    ImageIO.write(destination, "png", output);
  }
}
