import ch.bildspur.postfx.builder.PostFX;
import ddf.minim.AudioInput;
import ddf.minim.Minim;
import ddf.minim.analysis.BeatDetect;
import ddf.minim.analysis.FFT;

import peasy.*;
import processing.core.PApplet;

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
    int yDetail = 128;

    float xScl = 6;
    float yScl = 6;
    float zScl = 24;

    public static void main(String[] args) {
        PApplet.main("MainApp");
    }

    public void settings() {
        fullScreen(P3D);
        noSmooth();
    }

    public void setup() {
        colorMode(HSB);
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

        float xSide = xDetail*xScl;
        float ySide = (yDetail-1)*yScl;
        quad(0,0,xSide, 0, xSide,ySide, 0, ySide);

        for(int y = 0; y < yDetail-1; y++) {
            beginShape(TRIANGLE_STRIP);
            for(int x = 0; x < xDetail; x++){

                float z0 = plane[x][y];
                float z1 = plane[x][y+1];

                float hue = map(z0, -10, 5,0,255);

                if(x == 0){
                    strokeWeight(5);
                    stroke((hue)%255, 120,255);
                }else{
                    strokeWeight(1);
                    stroke((hue)%255, 255,220);
                }
                vertex(x*xScl,y*yScl, z0*zScl);
                vertex(x*xScl,y*yScl+yScl, z1*zScl);
            }
            endShape();
        }
    }
}
