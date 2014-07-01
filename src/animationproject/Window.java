/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package animationproject;

/**
 *
 * @author ivko0314
 */
public class Window {
    public int x;
    public int y;
    public int width;
    public int height;
    
    public Window() {
        width = 200;
        height = 100;
    }
    
    public Window(int width, int height) {
        this.height = height;
        this.width = width;
    }
}
