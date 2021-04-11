package bfst21;

import bfst21.exceptions.NoOSMInZipFileException;
import bfst21.view.Theme;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class Loader {
    public InputStream load(String filename) throws IOException, NoOSMInZipFileException {
        if(filename.endsWith(".osm")) return loadOSM(filename);
        else if(filename.endsWith(".zip")) return loadZIP(filename);
        return null;
    }

    private InputStream loadZIP(String filename) throws IOException, NoOSMInZipFileException {
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(filename));
        ZipEntry entry;

        while((entry = zipInputStream.getNextEntry()) != null) {
            if(entry.getName().endsWith(".osm")) return zipInputStream;
        }

        throw new NoOSMInZipFileException("Zip file does not contain a zipped OSM file.");
    }

    private InputStream loadOSM(String filename) throws IOException {
        return new FileInputStream(filename);
    }

    public ZipEntry getOSMZipEntry(String file) throws IOException, NoOSMInZipFileException {
        ZipFile zipFile = new ZipFile(file);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        while(entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();

            if(entry.getName().endsWith(".osm")) {
                zipFile.close();
                return entry;
            }
        }

        throw new NoOSMInZipFileException("Zip file does not contain a zipped OSM file.");
    }

    public InputStream loadResource(String filename) {
        return getClass().getResourceAsStream(filename);
    }

    public int getResourceFileSize(String file) throws IOException {
        return getClass().getResource(file).openConnection().getContentLength();
    }

    public Theme loadTheme(String file) {
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(loadResource("/themes/" + file)))) {
            Theme theme = new Theme();

            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if(line.isBlank()) continue;
                theme.parseData(line, lineNumber);
            }

            URL cssPath = getClass().getResource("/themes/" + file.replace(".mtheme", ".css"));
            if(cssPath != null) theme.setStylesheet(cssPath.toString());

            return theme;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Returns a list of Strings containing file/directory names in the specified directory.
     * Creates a FileSystem depending on being run from a jar file or locally. Then goes through every directory (including files)
     * and saves the name of the file if it has the specified extension.
     *
     * @param directory The directory to be searched
     * @param extension The file extension, we want to filter the list after
     * @return A List of Strings containing the name of the files/directories.
     */
    public List<String> getFilesIn(String directory, String extension) {
        List<String> files = new ArrayList<>();

        try {
            URI uri = getClass().getResource(directory).toURI();
            try (FileSystem fileSystem = (uri.getScheme().equals("jar") ? FileSystems.newFileSystem(uri, Collections.emptyMap()) : null)) {
                Path path = Paths.get(uri);
                Files.walkFileTree(path, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        String filePath = file.toString().replace("\\", "/");
                        filePath = filePath.substring(filePath.lastIndexOf("/") + 1);

                        if (filePath.endsWith(extension)) files.add(filePath);

                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return files;
    }
}