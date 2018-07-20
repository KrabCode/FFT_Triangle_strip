import ch.bildspur.postfx.builder.PostFX;
import ddf.minim.AudioInput;
import ddf.minim.Minim;
import ddf.minim.analysis.BeatDetect;
import ddf.minim.analysis.FFT;

import peasy.*;
import processing.core.PApplet;
import processing.core.PImage;

public class MainApp extends PApplet{

    Minim m;
    AudioInput in;
    FFT fft;
    BeatDetect beat;

    PostFX fx;
    PeasyCam cam;

    float[][] plane;
    float[][] nextPlane;
    float[][] tempPlane;

    //TODO hook these up to sliders
    int xDetail = 64;
    int yDetail = 64;

    float xScl = 80;
    float yScl = 120;
    float zScl = 32;
    PImage img;

    public static void main(String[] args) {
        PApplet.main("MainApp");
    }

    public void settings() {
        fullScreen(P3D);
        noSmooth();
    }

    public void setup() {
        colorMode(HSB);
        textureMode(NORMAL);
        img = loadImage("seamless_denim.jpg");
        fx = new PostFX(this);
        m = new Minim(this);
        in = m.getLineIn();
        beat = new BeatDetect();
        fft = new FFT(in.mix.size(),in.sampleRate());
        fft.linAverages(yDetail);
        cam = new PeasyCam(this, 700);
        plane = new float[xDetail][yDetail];
        tempPlane = new float[xDetail][yDetail];
        nextPlane = new float[xDetail][yDetail];
    }

    public void draw() {
        fft.forward(in.mix);
        beat.detect(in.mix);
        background(0);
        updatePlane();
        pushMatrix();
//        rotateX(HALF_PI-QUARTER_PI/2);
        rotateZ(HALF_PI);
        translate(-xDetail*xScl/2, -yDetail*yScl/2);
        drawPlane();
        popMatrix();
    }

    private void updatePlane() {
        for(int y = 0; y < yDetail; y++) {
            for(int x = 0; x < xDetail; x++){
                if(x == 0){
                    int myBand = round(map(abs(yDetail/2-y), 0, yDetail/2, 0, fft.avgSize()-1));
                    float myFft = 2*log( 200 * fft.getAvg(myBand) / fft.timeSize() );
                    nextPlane[x][y] = myFft;
                }else{
                    nextPlane[x][y] = plane[x-1][y];
                }
            }
        }
        tempPlane = plane;
        plane = nextPlane;
        nextPlane = tempPlane;
    }


    void drawPlane(){
        stroke(255);
        noFill();

        for(float y = 0; y < yDetail-1; y++) {
            beginShape(TRIANGLE_STRIP);
//            texture(img);
            for(float x = 0; x < xDetail; x++){

                float elevation0 = plane[round(x)][round(y)];
                float elevation1 = plane[round(x)][round(y+1)];

                float hue = map(max(elevation0, elevation1), -10, 0,0,255);
                stroke((hue)%255, 255,255);
                strokeWeight(map(x, 0, xDetail, 3, 0));

                float x0 = x*xScl;
                float y0 = y*yScl;
                float z0 = elevation0*zScl;
                float x1 = x*xScl;
                float y1 = y*yScl+yScl;
                float z1 = elevation1*zScl;

                vertex(x0,y0,z0);
                vertex(x1,y1,z1);

//                float u0 = map(x0, 0, xDetail*xScl, 0, 1);
//                float v0 = map(y0, 0, yDetail*yScl, 0, 1);
//                float u1 = map(x1, 0, xDetail*xScl, 0, 1);
//                float v1 = map(y1, 0, yDetail*yScl, 0, 1);

//                vertex(x0,y0,z0, u0, v0);
//                vertex(x1,y1,z1, u1, v1);
            }
            endShape();
        }
    }
}
