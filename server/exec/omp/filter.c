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

void gray(IplImage *src, IplImage *dest, int ren, int col){

}

void edge(IplImage *src, IplImage *dest, int ren, int col){

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
  char dest_path[256];

  IplImage *src = cvLoadImage(argv[1], CV_LOAD_IMAGE_COLOR);
  IplImage *dest = cvCreateImage(cvSize(src->width, src->height), IPL_DEPTH_8U, 3);

  apply(src, dest, argv[2]);

  strcat(dest_path,"img/");
  strcat(dest_path,argv[2]);

  cvSaveImage(dest_path, dest, 0);

  return 0;
}
