package bfst21;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;

// Loads the file given from the Filechooser
public class Loader {
    private static Creator creator;
    //private Creator creator;

    public Loader(){}



    public void load(String filename) throws IOException, XMLStreamException, FactoryConfigurationError {
       if (filename.endsWith(".zip")) {
            loadZIP(filename);
        }
    }

    private void loadZIP(String filename) throws IOException, XMLStreamException, FactoryConfigurationError {
        var zip = new ZipInputStream(new FileInputStream(filename));
        zip.getNextEntry();
        loadOSM(zip);
    }

    private void loadOSM(InputStream input) throws IOException, XMLStreamException, FactoryConfigurationError {
        creator = new Creator(input);
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


        //var waterTemp = new ArrayList<ArrayList<Way>>();

    // Load default .osm(binary) file
