/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package animationproject;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 *
 * @author ivko0314
 */
@XStreamAlias("level")
public class Level {
    
    @XStreamAsAttribute
    public int id;
    
    @XStreamAlias("grounds")
    public short[][] grounds;
}
