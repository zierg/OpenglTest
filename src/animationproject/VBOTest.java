/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package animationproject;

import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

/**
 *
 * @author ivko0314
 */
public class VBOTest {

    public static void main(String[] args) {
        int vID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vID);
    }

    /*public static int createVBOID() {
        IntBuffer buffer = BufferUtils.createIntBuffer(1);
        GL15.glGenBuffers(buffer);
    //return buffer.get(0);
        //Or alternatively you can simply use the convenience method:
        return GL15.glGenBuffers(); //Which can only supply you with a single id.
    }*/
}
