
package animationproject;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import java.io.File;

/**
 *
 * @author ivko0314
 */
@XStreamAlias("ground")
public class Ground {
      
    @XStreamAsAttribute
    public int id;
    
    public String imageFile;
    public String soundFile;
    
    public int row = 1;
    public int col = 1;
    
    public Ground() {
        String SEP = File.separator;
        final String groundFolder = "gr";
        imageFile = groundFolder + SEP + "img" + SEP + "default.png";
        soundFile = groundFolder + SEP + "snd" + SEP + "default.wav";
    }
}
