#include <iostream>
#include <opencv/highgui.h>
#include <string>
#include <tbb/task_scheduler_init.h>
#include <tbb/parallel_for.h>
#include <tbb/blocked_range.h>

using namespace std;
using namespace tbb;

const int BLUR_WINDOW = 15;
const int GRAIN = 1000;

enum color {BLUE, GREEN, RED};

class Filter{
private:
  IplImage * src, * dest;
	char * tech;

  void blur(int ren, int col) const{
    int side_pixels, i, j, cells;
    int tmp_ren, tmp_col, step;
    float r, g, b;

    side_pixels = (BLUR_WINDOW - 1) / 2;
    cells = (BLUR_WINDOW * BLUR_WINDOW);
    step = src->widthStep / sizeof(uchar);

    r = 0; g = 0; b = 0;

    for (i = -side_pixels; i <= side_pixels; i++) {
      for (j = -side_pixels; j <= side_pixels; j++) {
        tmp_ren = MIN( MAX(ren + i, 0), src->height - 1 );
        tmp_col = MIN( MAX(col + j, 0), src->width - 1);

        r += (float) src->imageData[(tmp_ren * step) + (tmp_col * src->nChannels) + RED];
        g += (float) src->imageData[(tmp_ren * step) + (tmp_col * src->nChannels) + GREEN];
        b += (float) src->imageData[(tmp_ren * step) + (tmp_col * src->nChannels) + BLUE];
      }
    }

      dest->imageData[(ren * step) + (col * dest->nChannels) + RED] =  (unsigned char) (r / cells);
      dest->imageData[(ren * step) + (col * dest->nChannels) + GREEN] = (unsigned char) (g / cells);
      dest->imageData[(ren * step) + (col * dest->nChannels) + BLUE] = (unsigned char) (b / cells);
  }

	void gray(int row, int col) const{
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

	void edge(int row, int col) const{
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

public:
  Filter(IplImage *source, IplImage *destination, char * technology): src(source), dest(destination), tech(technology){}

  void operator() (const blocked_range<int> &r) const {
		if(strcmp(tech,"blur") == 0){
			for (int i = r.begin(); i != r.end(); i++){
      	int row = i / src->width;
      	int col = i % src->width;		
	    	blur(row, col);
    	}
		}

		if(strcmp(tech,"gray") == 0){
			for (int i = r.begin(); i != r.end(); i++){
      	int row = i / src->width;
      	int col = i % src->width;		
	    	gray(row, col);
    	}
		}

		if(strcmp(tech,"edge") == 0){
			for (int i = r.begin(); i != r.end(); i++){
      	int row = i / src->width;
      	int col = i % src->width;		
	    	edge(row, col);
    	}		
		}    
  }

};

int main(int argc, char *argv[]) {
		char dest_path[256] = "img/";

    IplImage * src = cvLoadImage(argv[1], CV_LOAD_IMAGE_COLOR);
    IplImage * dest = cvCreateImage(cvSize(src->width, src->height),IPL_DEPTH_8U, 3);

    int size = src->width * src->height;

    parallel_for(blocked_range<int>(0,size,GRAIN), Filter(src, dest,argv[2]));

		strcat(dest_path,argv[3]);

    cvSaveImage(dest_path, dest);

  return 0;
}
