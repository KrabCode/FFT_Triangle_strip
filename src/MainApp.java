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
    int xDetail = 6;
    int yDetail = 32;

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
        img = loadImage("crop.png");
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

//        float xSide = xDetail*xScl;
//        float ySide = (yDetail-1)*yScl;
//        quad(0,0,xSide, 0, xSide,ySide, 0, ySide);

        for(float y = 0; y < yDetail-1; y++) {
            beginShape(TRIANGLE_STRIP);
//            texture(img);
            for(float x = 0; x < xDetail; x++){

                float z0 = plane[round(x)][round(y)];
                float z1 = plane[round(x)][round(y+1)];

                float hue = map(max(z0, z1), -10, 5,0,255);

                if(x == 0){
                    strokeWeight(5);
                    stroke((hue)%255, 255,255);
                }else{
                    strokeWeight(1);
                    stroke((hue)%255, 255,200);
                }

//                noStroke();
//                float u = x/yDetail/12;
//                float v = y/yDetail/12;
//                println(u + " : " + v);

                vertex(x*xScl,y*yScl,      z0*zScl);
                vertex(x*xScl,y*yScl+yScl, z1*zScl);
            }
            endShape();
        }
    }
}
