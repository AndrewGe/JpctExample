package com.example.d2dadv.jpctexample_1;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.invertM;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.multiplyMV;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.setLookAtM;
import static android.opengl.Matrix.translateM;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

import com.example.d2dadv.jpctexample_1.programs.TextureShaderProgram;
import com.example.d2dadv.jpctexample_1.util.MatrixHelper;
import com.example.d2dadv.jpctexample_1.util.TextureHelper;


import java.lang.reflect.Field;
import com.threed.jpct.Camera;
import com.threed.jpct.Config;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Loader;
import com.threed.jpct.Logger;
import com.threed.jpct.Object3D;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.MemoryHelper;



/**
 * Created by d2dadv on 11/24/14.
 */
public class JpctRenderer implements Renderer {

    private final Context context;

    private FrameBuffer fb=null;
    private World world=null;
    private RGBColor back=new RGBColor(100,100,100);
    private Object3D plane=null;//
    private Object3D tree1=null;//
    private Object3D tree2=null;
    private Object3D rock=null;//
    private Object3D grass=null;//
    private Object3D body=null;//
    private Light light=null;

    private long time=System.currentTimeMillis();//
    private SimpleVector lightRot=new SimpleVector(-100,-100f,100);

    private float touchTurn = 0;
    private float touchTurnUp = 0;
    private float xpos = -1;
    private float ypos = -1;


    public JpctRenderer(Context context){
        this.context = context;
        Texture.defaultToMipmapping(true);
        Texture.defaultTo4bpp(true);
    }

    public void handleTouchPress(float normalizedX, float normalizedY) {
        xpos = -1;
        ypos = -1;
        touchTurn = 0;
        touchTurnUp = 0;

    }


    public void handleTouchDrag(float normalizedX, float normalizedY) {
        float xd = normalizedX - xpos;
        float yd = normalizedY - ypos;

        xpos = normalizedX;
        ypos = normalizedY;

        touchTurn = xd / 100f;
        touchTurnUp = yd / 100f;
    }




    public void onSurfaceCreated(GL10 glUnused, EGLConfig config){
       // glClearColor(0.5f, 0.0f, 0.5f, 0.0f);

        world=new World();
        Resources res = context.getResources();
        //
        TextureManager tm=TextureManager.getInstance();
        //
        Texture grass2 = new Texture(res.openRawResource(R.raw.grassy));
        Texture leaves = new Texture(res.openRawResource(R.raw.tree2y));
        Texture leaves2 = new Texture(res.openRawResource(R.raw.tree3y));
        Texture rocky = new Texture(res.openRawResource(R.raw.rocky));
        Texture planetex = new Texture(res.openRawResource(R.raw.planetex));

        //
        tm.addTexture("grass2", grass2);
        tm.addTexture("leaves", leaves);
        tm.addTexture("leaves2", leaves2);
        tm.addTexture("rock", rocky);
        tm.addTexture("grassy", planetex);

        plane = Loader.loadSerializedObject(res.openRawResource(R.raw.serplane));

        rock=Loader.load3DS(res.openRawResource(R.raw.rock), 30f)[0];
        tree1=Loader.load3DS(res.openRawResource(R.raw.tree2), 5f)[0];
        tree2=Loader.load3DS(res.openRawResource(R.raw.tree3), 5f)[0];
        grass=Loader.load3DS(res.openRawResource(R.raw.grass), 2f)[0];
        body = Loader.loadOBJ(res.openRawResource(R.raw.body), null, 40f)[0]; //No mtlStream

        grass.translate(-45, -17, -50);//
        grass.rotateZ((float) Math.PI);
        rock.translate(0, 0, -90);
        //X90??rotateX??
        rock.rotateX(-(float) Math.PI / 2);
        tree1.translate(-50, -100, -50);
        tree1.rotateZ((float) Math.PI);
        tree2.translate(60, -95, -10);
        tree2.rotateZ((float) Math.PI);
        plane.rotateX((float) Math.PI / 2f);

        body.translate(-60, -105, 0);
        body.rotateX((float)Math.PI);


        plane.setName("plane");
        tree1.setName("tree1");
        tree2.setName("tree2");
        grass.setName("grass");
        rock.setName("rock");
        body.setName("body");
        //

        tree1.setTexture("leaves");
        tree2.setTexture("leaves2");
        rock.setTexture("rock");
        grass.setTexture("grass2");

        //world
        world.addObject(plane);
       // world.addObject(tree1);
       // world.addObject(tree2);
        world.addObject(grass);
        //world.addObject(rock);
        world.addObject(body);

        //
        plane.strip();
        tree1.strip();
        tree2.strip();
        grass.strip();
        rock.strip();


        world.setAmbientLight(250, 250, 250);//
        world.buildAllObjects();//Calls build() for every object in the world.

        light = new Light(world);//
        light.setIntensity(250, 250, 0);//

        Camera cam = world.getCamera();
        cam.moveCamera(Camera.CAMERA_MOVEOUT, 250);
        cam.moveCamera(Camera.CAMERA_MOVEUP, 100);
        //plane
        cam.lookAt(plane.getTransformedCenter());

        SimpleVector sv = new SimpleVector();
        sv.set(plane.getTransformedCenter());
        Log.e("X:", plane.getTransformedCenter().toString());
        sv.y -= 500;
        sv.x -= 300;
        sv.z += 200;
        //?svplane
        light.setPosition(sv);
        //
        MemoryHelper.compact();



    }
    public void onSurfaceChanged(GL10 glUnused, int width, int height){
       // glViewport(0,0,width, height); Framebuffer overwrites this command
        if(fb!=null)
            fb.dispose();//
        fb=new FrameBuffer(width,height);
    }

    public void onDrawFrame(GL10 glUnused){
       // glClear(GL_COLOR_BUFFER_BIT); Framebuffer overwrites this command

        try {
            if (true) {
                Camera cam = world.getCamera();
                if (touchTurn != 0) {
                    world.getCamera().rotateY(touchTurn);
                    touchTurn = 0;
                }

                if (touchTurnUp != 0) {
                    world.getCamera().rotateX(touchTurnUp);
                    touchTurnUp = 0;
                }

                fb.clear(back);
                world.renderScene(fb);//

                world.draw(fb);
                fb.display();//

                if (light != null) {
                    light.rotate(lightRot, plane.getTransformedCenter());
                }
            } else {
                if (fb != null) {
                    fb.dispose();
                    fb = null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.log("Drawing thread terminated!", Logger.MESSAGE);
        }



    }


}
