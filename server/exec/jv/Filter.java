/*
*  Filter source code: Applies a given filter from three implementations:
*     -Blur
*     -Grayscale
*     -Edge detection
*  Copyright (C) 2019  Sergio Isaac Mercado Silvano
*
*  This program is free software: you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation, either version 3 of the License, or
*  (at your option) any later version.
*
*  This program is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU General Public License for more details.
*
*  You should have received a copy of the GNU General Public License
*  along with this program.  If not, see <https://www.gnu.org/licenses/>
*/

//Required libraries

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ForkJoinPool;

//Must extend recursive action (does not return any value)

public class Filter extends RecursiveAction{
  public static final long serialVersionUID = 10293918281249501L;
  private static final int BLUR_WINDOW = 15;
  private static final int GRAIN = 10_000;
  private int src[], dest[], width, height, start, end;
  private String filter;

  //Constructor

  public Filter(int start, int end, int src[], int dest[], int width, int height, String filter){
    this.start = start;
    this.end = end;
    this.src = src;
    this.dest = dest;
    this.width = width;
    this.height = height;
    this.filter = filter;
  }

  //Blur method

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

  //Grayscale method

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

  //Edge detection method

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


  //Serial filtering

  protected void computeDirectly(){
    int index;
    int row, col;

    for(index = start; index < end; index++){
      row = index / width;
      col = index % width;

      //Check which filter to apply on each iteration

      switch(filter){
        case "blur":
          blur(row, col);
          break;
        case "edge":
          edge(row, col);
          break;
        case "gray":
          gray(row, col);
          break;
        default:
          break;
      }
    }
  }

  //RecursiveAction required method (compute())

  @Override
  protected void compute(){

    //Check if problem can be solved serially
    if((end - start) <= GRAIN){
      computeDirectly();
    }else{

      //Split the size in half on each iteration

      int mid = start + ((end - start) / 2);

      //Let all threads work on upper and lower half on each iteration
      invokeAll(new Filter(start, mid, src, dest, width, height, filter),
                new Filter(mid, end, src, dest, width, height, filter));
    }
  }

  public static void main(String[] args) throws Exception{
    ForkJoinPool pool;

    //Command parameters

    final String srcName = args[0];
    final String filterT = args[1];
    final String destName = args[2];

    File srcFile = new File(srcName);

    final BufferedImage source = ImageIO.read(srcFile);

    //Obtain image parameters

    int w = source.getWidth();
    int h = source.getHeight();

    //Transform image into array
    int src[] = source.getRGB(0, 0, w, h, null, 0, w);

    //Prepare destination array of same size as source
    int dest[] = new int[src.length];

    //Begin Thread Pool work

    pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
    pool.invoke(new Filter(0, w * h, src, dest, w, h, filterT));

    //End Thread Pool work

    //Transform array into image
    final BufferedImage destination = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    destination.setRGB(0, 0, w, h, dest, 0, w);

    //Write image file into "img" directory with new filename
    File output = new File("img/" + destName);
    ImageIO.write(destination, "png", output);
  }
}
