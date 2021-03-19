package bfst21;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

// Loads the file given from the Filechooser
public class Loader
{
    private static Creator creator;
    //private Creator creator;
    private Map map;

    public Loader(Map map)
    {
        this.map = map;
    }

    public void load(String filename) throws IOException, XMLStreamException, FactoryConfigurationError
    {
        if (filename.endsWith(".zip"))
        {
            loadZIP(filename);
        }
    }

    private void loadZIP(String filename) throws IOException, XMLStreamException, FactoryConfigurationError {
        var zip = new ZipInputStream(new FileInputStream(filename));
        zip.getNextEntry();
        loadOSM(zip);
    }

    private void loadOSM(InputStream input) throws IOException, XMLStreamException, FactoryConfigurationError {
        creator = new Creator(map, input);
    }
    //Test
    /* public static void main(String[] args) throws IOException, XMLStreamException {
        var filename = "C:\\Users\\Stribe\\IdeaProjects\\BFST21Group10\\app\\src\\main\\resources\\map.zip";
        var loader = new Loader(filename);
    }
     */

    public Creator getCreator() {
        return creator;
    }
}