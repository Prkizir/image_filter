/*nvcc filter.cu `pkg-config --cflags --libs opencv`*/

#include <stdlib.h>
#include <string>
#include <opencv/highgui.h>
//#include "utils/cheader.h"

#define BLUR_WINDOW 15

typedef enum color {BLUE, GREEN, RED} Color;

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

int main(int argc, char* argv[]) {
	int step, size;
	unsigned char *dev_src, *dev_dest;

	char src_name[255];
	char filter_t[255];
	char dest_name[255];

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

 
    gray<<<src->height, src->width>>>(dev_src, dev_dest, src->width, src->height, step, src->nChannels);
 

	cudaMemcpy(dest->imageData, dev_dest, size, cudaMemcpyDeviceToHost);

	cudaFree(dev_dest);
	cudaFree(dev_src);

	return 0;
}
