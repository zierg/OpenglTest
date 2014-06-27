package animationproject;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.io.xml.Dom4JDriver;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 *
 * @author Michael
 */

public class LevelCreator {

    private static final XStream xstream = new XStream(new PureJavaReflectionProvider(), new Dom4JDriver()); 
    // new PureJavaReflectionProvider() - будет использоваться конструктор по умолчанию, чтобы отсутствующие поля 
    static {
        xstream.processAnnotations(Ground.class);
        xstream.processAnnotations(Level.class);
        Ground[] gr = new Ground[1];
        xstream.alias("Grounds", gr.getClass());
    }
    /**
     * Точка входа в программу. С этого места начинается выполнение кода.
     *
     * @param args Аргументы командной строки (не используется)
     */
    public static void main(String[] args) throws IOException {
        writeLevel();
    }
    
    private static void writeLevel() throws IOException {
        Level level = new Level();
        level.id = 1;

        int[] x = new int[0];
        xstream.alias("row", x.getClass());
        final int SIZE = 120;
        level.grounds = new int[SIZE][SIZE];

        for (int[] g : level.grounds) {           
            for (int i =0; i < g.length; i++) {
                g[i] = (int) (Math.random()*16);
                if (g[i] == 16) {
                    g[i] = 15;
                }
            }
        }
        Writer writer = new FileWriter("level.xml");
        xstream.toXML(level, writer);
    }
    
    private static void write() throws IOException {
        Ground[] gr = new Ground[16];

        int id = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                gr[id] = new Ground();
                gr[id].id = id;
                gr[id].col = j;
                gr[id].row = i;
                id++;
            }
        }
        
        Writer writer = new FileWriter("Grounds.xml");
        xstream.toXML(gr, writer);
    }
    
    private static void read() throws FileNotFoundException {
        Reader reader = new FileReader("Grounds.xml");
        Ground[] gr = (Ground[]) xstream.fromXML(reader);
        for (Ground g : gr) {
            System.out.println(g.imageFile);
            System.out.println(g.id);
        }
    }
}
