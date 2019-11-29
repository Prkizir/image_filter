/*nvcc filter.cu `pkg-config --cflags --libs opencv`*/

#include <stdlib.h>
#include <string>
#include <opencv/highgui.h>
//#include "utils/cheader.h"

#define BLUR_WINDOW 15

typedef enum color {BLUE, GREEN, RED} Color;

__global__ void blur(unsigned char *src, unsigned char *dest, int width, int heigth, int blur_window, int step, int channels) {
	int i, j, side_pixels, cells;
	int ren, col, tmp_ren, tmp_col;
	float r, g, b;

	ren = blockIdx.x;
	col = threadIdx.x;
	side_pixels = (blur_window - 1) / 2;
	cells = (blur_window * blur_window);
	r = 0; g = 0; b = 0;
	for (i = -side_pixels; i <= side_pixels; i++) {
		for (j = -side_pixels; j <= side_pixels; j++) {
			tmp_ren = MIN( MAX(ren + i, 0), heigth - 1 );
			tmp_col = MIN( MAX(col + j, 0), width - 1);

			r += (float) src[(tmp_ren * step) + (tmp_col * channels) + RED];
			g += (float) src[(tmp_ren * step) + (tmp_col * channels) + GREEN];
			b += (float) src[(tmp_ren * step) + (tmp_col * channels) + BLUE];
		}
	}

	dest[(ren * step) + (col * channels) + RED] =  (unsigned char) (r / cells);
	dest[(ren * step) + (col * channels) + GREEN] = (unsigned char) (g / cells);
	dest[(ren * step) + (col * channels) + BLUE] = (unsigned char) (b / cells);
}

__global__ void gray(unsigned char *src, unsigned char *dest, int width, int height, int step, int channels){
  int ren, col;
	float r, g, b, avg;

	ren = blockIdx.x;
	col = threadIdx.x;
	r = 0; g = 0; b = 0;

	r = (float) src[(ren * step) + (col * channels) + RED];
	g = (float) src[(ren * step) + (col * channels) + GREEN];
	b = (float) src[(ren * step) + (col * channels) + BLUE];

  avg = (r + g + b)/3.0;

	dest[(ren * step) + (col * channels) + RED] =  (unsigned char) (avg);
	dest[(ren * step) + (col * channels) + GREEN] = (unsigned char) (avg);
	dest[(ren * step) + (col * channels) + BLUE] = (unsigned char) (avg);
}

__global__ void edge(unsigned char *src, unsigned char *dest, int width, int height, int step, int channels){

}

int main(int argc, char* argv[]) {
	int step, size;
	unsigned char *dev_src, *dev_dest;

	char src_name[255];
	char filter_t[255];
	char dest_name[255];
  char dir[255] = "img/";

	strcpy(src_name, argv[1]);
	strcpy(filter_t, argv[2]);
	strcpy(dest_name, argv[3]);

	IplImage *src = cvLoadImage(src_name, CV_LOAD_IMAGE_COLOR);
	IplImage *dest = cvCreateImage(cvSize(src->width, src->height), IPL_DEPTH_8U, 3);

	size = src->width * src->height * src->nChannels * sizeof(uchar);
	step = src->widthStep / sizeof(uchar);

	cudaMalloc((void**) &dev_src, size);
	cudaMalloc((void**) &dev_dest, size);

	cudaMemcpy(dev_src, src->imageData, size, cudaMemcpyHostToDevice);

  if(compare(filter_t,"blur") == 0){
    blur<<<src->height, src->width>>>(dev_src, dev_dest, src->width, src->height, BLUR_WINDOW, step, src->nChannels);
  }

  if(compare(filter_t,"gray") == 0){
    gray<<<src->height, src->width>>>(dev_src, dev_dest, src->width, src->height, step, src->nChannels);
  }

	cudaMemcpy(dest->imageData, dev_dest, size, cudaMemcpyDeviceToHost);

	cvSaveImage(strcat(dir,dest_name) , dest);

	cudaFree(dev_dest);
	cudaFree(dev_src);

	return 0;
}
