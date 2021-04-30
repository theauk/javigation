package bfst21.file_io;

import bfst21.exceptions.NoOSMInZipFileException;
import bfst21.exceptions.UnsupportedFileFormatException;
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
    public static InputStream load(String file) throws IOException, NoOSMInZipFileException, UnsupportedFileFormatException {
        if(file.endsWith(".osm") || file.endsWith(".bmapdata")) return loadFile(file);
        else if(file.endsWith(".zip")) return loadZIP(file);
        throw new UnsupportedFileFormatException(file);
    }

    /**
     * Returns a ZipInputStream for the first OSM zip entry or
     * throws a {@link NoOSMInZipFileException} exception if no OSM file was found.
     *
     * @param file the path to the file to be processed
     * @return a ZipInputStream for the found OSM zip entry.
     * @throws IOException if the file is not found.
     * @throws NoOSMInZipFileException if there is no OSM file in the zip file.
     */
    private static ZipInputStream loadZIP(String file) throws IOException, NoOSMInZipFileException {
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file));
        ZipEntry entry;

        while((entry = zipInputStream.getNextEntry()) != null) {
            if(entry.getName().endsWith(".osm")) return zipInputStream;
        }

        throw new NoOSMInZipFileException(file);
    }

    private static FileInputStream loadFile(String file) throws IOException {
        return new FileInputStream(file);
    }

    public static InputStream loadResource(String file) {
        return Loader.class.getResourceAsStream(file);
    }

    public static long getResourceFileSize(String file) throws IOException {
        return Loader.class.getResource(file).openConnection().getContentLengthLong();
    }

    /**
     * Finds the first OSM zip entry in a zip file and returns the uncompressed
     * file size of the entry.
     *
     * @param file the zip file to be processed
     * @return the file size of the first found OSM entry in the zip file (in bytes).
     * @throws IOException if file is not found.
     * @throws NoOSMInZipFileException if there are no OSM zip entries in the zip file.
     */
    public static long getZipFileEntrySize(String file) throws IOException, NoOSMInZipFileException {
        ZipFile zipFile = new ZipFile(file);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        while(entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();

            if(entry.getName().endsWith(".osm")) {
                zipFile.close();
                return entry.getSize();
            }
        }

        throw new NoOSMInZipFileException(file);
    }

    public static Theme loadTheme(String file) {
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(loadResource("/themes/" + file)))) {
            Theme theme = new Theme();

            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if(line.isBlank()) continue;
                theme.parseData(line, lineNumber);
            }

            URL cssPath = Loader.class.getResource("/themes/" + file.replace(".mtheme", ".css"));
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
     * @param directory the directory to be searched.
     * @param extension the file extension, we want to filter the list after.
     * @return A List of Strings containing the name of the files/directories.
     */
    public static List<String> getFilesIn(String directory, String extension) {
        List<String> files = new ArrayList<>();

        try {
            URI uri = Loader.class.getResource(directory).toURI();
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