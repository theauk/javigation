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
    private Map map;

    public Loader(Map map)
    {
        this.map = map;
    }

    public void load(String filename) throws IOException, XMLStreamException, FactoryConfigurationError
    {
        if(filename.endsWith(".osm")) loadOSM(filename);
        else if(filename.endsWith(".zip")) loadZIP(filename);
    }

    private void loadZIP(String filename) throws IOException, XMLStreamException, FactoryConfigurationError {
        var zip = new ZipInputStream(new FileInputStream(filename));
        zip.getNextEntry();
        loadOSM(zip);
    }

    private void loadOSM(String filename) throws IOException, XMLStreamException, FactoryConfigurationError {
        FileInputStream fileInputStream = new FileInputStream(filename);
        loadOSM(fileInputStream);
    }

    private void loadOSM(InputStream inputStream) throws XMLStreamException {
        Creator creator = new Creator(map, inputStream);
    }
}