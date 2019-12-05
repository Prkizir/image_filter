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


#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <opencv/highgui.h>
#include <omp.h>

#define BLUR_WINDOW 15

typedef enum color {BLUE, GREEN, RED} Color;

void blur(IplImage *src, IplImage *dest, int ren, int col){
  int side_pixels, i, j, cells;
  int tmp_ren, tmp_col, step;
  float r, g, b;

  side_pixels = (BLUR_WINDOW - 1)/2;
  cells = (BLUR_WINDOW * BLUR_WINDOW);
  step = src->widthStep/sizeof(uchar);

  r = 0;
  g = 0;
  b = 0;

  for(i = -side_pixels; i <= side_pixels; i++){
    for(j = -side_pixels; j <= side_pixels; j++){
      tmp_ren = MIN(MAX(ren + i, 0), src->height - 1);
      tmp_col = MIN(MAX(col + j, 0), src->width - 1);

      r += (float) src->imageData[(tmp_ren * step) +
                                  (tmp_col * src->nChannels) + RED];

      g += (float) src->imageData[(tmp_ren * step) +
                                  (tmp_col * src->nChannels) + GREEN];

      b += (float) src->imageData[(tmp_ren * step) +
                                  (tmp_col * src->nChannels) + BLUE];
    }
  }

  dest -> imageData[(ren * step) + (col * dest->nChannels) + RED] = (unsigned char) (r / cells);
  dest -> imageData[(ren * step) + (col * dest->nChannels) + GREEN] = (unsigned char) (g / cells);
  dest -> imageData[(ren * step) + (col * dest->nChannels) + BLUE] = (unsigned char) (b / cells);
}

void gray(IplImage *src, IplImage *dest, int row, int col){
  float r, g, b, avg;
  int step;

  step = src->widthStep/sizeof(uchar);

  r = 0; g = 0; b = 0;

  r = (float) src->imageData[(row * step) + (col * src->nChannels) + RED];
  g = (float) src->imageData[(row * step) + (col * src->nChannels) + GREEN];
  b = (float) src->imageData[(row * step) + (col * src->nChannels) + BLUE];

  avg = (r + g + b)/3.0;

  dest -> imageData[(row * step) + (col * dest->nChannels) + RED] =  (unsigned char) (avg);
  dest -> imageData[(row * step) + (col * dest->nChannels) + GREEN] = (unsigned char) (avg);
  dest -> imageData[(row * step) + (col * dest->nChannels) + BLUE] = (unsigned char) (avg);
}

void edge(IplImage *src, IplImage *dest, int row, int col){
  int tmp_row, step;

  step = src->widthStep/sizeof(uchar);

  float rH, gH, bH, avgH;
  float rL, gL, bL, avgL;

  rH = 0; gH = 0; bH = 0;
  rL = 0; gL = 0; bL = 0;

  tmp_row = MIN(MAX(row + 1, 0), src->height - 1);

  rH = (float) src->imageData[(row * step) + (col * src->nChannels) + RED];
  gH = (float) src->imageData[(row * step) + (col * src->nChannels) + GREEN];
  bH = (float) src->imageData[(row * step) + (col * src->nChannels) + BLUE];

  rL = (float) src->imageData[(tmp_row * step) + (col * src->nChannels) + RED];
  gL = (float) src->imageData[(tmp_row * step) + (col * src->nChannels) + GREEN];
  bL = (float) src->imageData[(tmp_row * step) + (col * src->nChannels) + BLUE];

  avgH = (rH + gH + bH)/3.0;
  avgL = (rL + gL + bL)/3.0;

  if((0.65 >= fabs(avgH - avgL)) && (0.70 >= fabs(avgH - avgL))){
    dest -> imageData[(row * step) + (col * dest->nChannels) + RED] = (unsigned char) (0xFF);
    dest -> imageData[(row * step) + (col * dest->nChannels) + GREEN] = (unsigned char) (0xFF);
    dest -> imageData[(row * step) + (col * dest->nChannels) + BLUE] = (unsigned char) (0xFF);
  }else{
    dest -> imageData[(row * step) + (col * dest->nChannels) + RED] = (unsigned char) (0);
    dest -> imageData[(row * step) + (col * dest->nChannels) + GREEN] = (unsigned char) (0);
    dest -> imageData[(row * step) + (col * dest->nChannels) + BLUE] = (unsigned char) (0);
  }
}

void apply(IplImage *src, IplImage *dest, char * flt){
  int index, size;
  int ren, col;

  size = src->width * src->height;

  if(strcmp(flt,"blur") == 0){
    #pragma omp parallel for shared(src, dest, size) private(ren, col)
    for(index = 0; index < size; index++){
      ren = index / src->width;
      col = index % src->width;
      blur(src,dest,ren,col);
    }
  }

  if(strcmp(flt,"gray") == 0){
    #pragma omp parallel for shared(src, dest, size) private(ren, col)
    for(index = 0; index < size; index++){
      ren = index / src->width;
      col = index % src->width;
      gray(src,dest,ren,col);
    }
  }

  if(strcmp(flt,"edge") == 0){
    #pragma omp parallel for shared(src, dest, size) private(ren, col)
    for(index = 0; index < size; index++){
      ren = index / src->width;
      col = index % src->width;
      edge(src,dest,ren,col);
    }
  }
}

int main(int argc, char *argv[]) {
  int i;
  char dest_path[256] = "img/";

  IplImage *src = cvLoadImage(argv[1], CV_LOAD_IMAGE_COLOR);
  IplImage *dest = cvCreateImage(cvSize(src->width, src->height), IPL_DEPTH_8U, 3);

  apply(src, dest, argv[2]);


  strcat(dest_path,argv[3]);

  cvSaveImage(dest_path, dest, 0);

  return 0;
}
