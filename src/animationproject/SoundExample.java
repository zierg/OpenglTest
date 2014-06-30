/* 
 * Copyright (c) 2004 LWJGL Project
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are 
 * met:
 * 
 * * Redistributions of source code must retain the above copyright 
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'LWJGL' nor the names of 
 *   its contributors may be used to endorse or promote products derived 
 *   from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.util.WaveData;
import org.lwjgl.Sys;

public class SoundExample {

    /**
     * Buffers hold sound data.
     */
    IntBuffer buffer = BufferUtils.createIntBuffer(1);

    /**
     * Sources are points emitting sound.
     */
    IntBuffer source = BufferUtils.createIntBuffer(1);

    /**
     * Position of the source sound.
     */
    FloatBuffer sourcePos = (FloatBuffer) BufferUtils.createFloatBuffer(3).put(new float[]{-5.0f, 0.0f, 0.0f}).rewind();

    /**
     * Velocity of the source sound.
     */
    FloatBuffer sourceVel = (FloatBuffer) BufferUtils.createFloatBuffer(3).put(new float[]{0.0f, 0.0f, 0.1f}).rewind();

    /**
     * Position of the listener.
     */
    FloatBuffer listenerPos = (FloatBuffer) BufferUtils.createFloatBuffer(3).put(new float[]{0.0f, 0.0f, 0.0f}).rewind();

    /**
     * Velocity of the listener.
     */
    FloatBuffer listenerVel = (FloatBuffer) BufferUtils.createFloatBuffer(3).put(new float[]{0.0f, 0.0f, 0.0f}).rewind();

    /**
     * Orientation of the listener. (first 3 elements are "at", second 3 are
     * "up")
     */
    FloatBuffer listenerOri
            = (FloatBuffer) BufferUtils.createFloatBuffer(6).put(new float[]{0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f}).rewind();

    /**
     * boolean LoadALData()
     *
     * This function will load our sample data from the disk using the Alut
     * utility and send the data into OpenAL as a buffer. A source is then also
     * created to play that buffer.
     */
    int loadALData() {
        // Load wav data into a buffer.
        AL10.alGenBuffers(buffer);

        if (AL10.alGetError() != AL10.AL_NO_ERROR) {
            return AL10.AL_FALSE;
        }
        /* 
         //Loads the wave file from your file system
         java.io.FileInputStream fin = null;
         try {
         fin = new java.io.FileInputStream("Footsteps.wav");
         } catch (java.io.FileNotFoundException ex) {
         System.out.println("Datei nicht gefunden.");
         ex.printStackTrace();
         return AL10.AL_FALSE;
         }
         System.out.println("Datei geöffnet.");
         WaveData waveFile = WaveData.create(fin);
         try {
         fin.close();
         } catch (java.io.IOException ex) {
         }
         */
        //Loads the wave file from this class's package in your classpath
        WaveData waveFile = null;
        try {
            waveFile = WaveData.create(new BufferedInputStream(new FileInputStream("1.wav")));
        } catch (FileNotFoundException ex) {
            System.out.println("omg");
        }

        AL10.alBufferData(buffer.get(0), waveFile.format, waveFile.data, waveFile.samplerate);
        waveFile.dispose();

        // Bind the buffer with the source.
        AL10.alGenSources(source);

        if (AL10.alGetError() != AL10.AL_NO_ERROR) {
            return AL10.AL_FALSE;
        }

        AL10.alSourcei(source.get(0), AL10.AL_BUFFER, buffer.get(0));
        AL10.alSourcef(source.get(0), AL10.AL_PITCH, 1.0f);
        //AL10.alSourcef(source.get(0), AL10.AL_GAIN, 1.0f);
        AL10.alSource(source.get(0), AL10.AL_POSITION, sourcePos);
        //AL10.alSource(source.get(0), AL10.AL_VELOCITY, sourceVel);
        //AL10.alSourcei(source.get(0), AL10.AL_LOOPING, AL10.AL_TRUE);

        // Do another error check and return.
        if (AL10.alGetError() == AL10.AL_NO_ERROR) {
            return AL10.AL_TRUE;
        }

        return AL10.AL_FALSE;
    }

    /**
     * void setListenerValues()
     *
     * We already defined certain values for the Listener, but we need to tell
     * OpenAL to use that data. This function does just that.
     */
    void setListenerValues() {
        /*AL10.alListener(AL10.AL_POSITION, listenerPos);
        AL10.alListener(AL10.AL_VELOCITY, listenerVel);
        AL10.alListener(AL10.AL_ORIENTATION, listenerOri);*/
    }

    /**
     * void killALData()
     *
     * We have allocated memory for our buffers and sources which needs to be
     * returned to the system. This function frees that memory.
     */
    void killALData() {
        AL10.alDeleteSources(source);
        AL10.alDeleteBuffers(buffer);
    }

    public static void main(String[] args) {
        new SoundExample().execute();
    }

    public void execute() {

        // Initialize OpenAL and clear the error bit.
        try {
            AL.create();
        } catch (LWJGLException le) {
            le.printStackTrace();
            return;
        }

        AL10.alGetError();

        // Load the wav data.
        if (loadALData() == AL10.AL_FALSE) {
            System.out.println("Error loading data.");
            return;
        }

        setListenerValues();

        AL10.alSourcePlay(source.get(0));

        // Loop.
        long time = Sys.getTime();
        long elapse = 0;

        System.out.println("Press ENTER to exit");

        while (!kbhit()) {
            elapse += Sys.getTime() - time;
            time += elapse;

            if (elapse > 1) {
                elapse = 0;

                //sourcePos.put(0, sourcePos.get(0) + sourceVel.get(0));
                //sourcePos.put(1, sourcePos.get(1) + sourceVel.get(1));
                sourcePos.put(2, sourcePos.get(2) + sourceVel.get(2));

                AL10.alSource(source.get(0), AL10.AL_POSITION, sourcePos);
            };
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.out.println("Sleep Interrupted");
            }
        }
        killALData();
        AL.destroy();
    }

    /**
     * Check for keyboard hit
     */
    private boolean kbhit() {
        try {
            return (System.in.available() != 0);
        } catch (IOException ioe) {
        }
        return false;
    }
}
