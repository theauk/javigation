package bfst21;

import bfst21.Exceptions.InvalidRGBValueException;
import bfst21.view.Theme;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipInputStream;

public class Loader {
    private final String regex = "^(?:\"(?<key>[A-Za-z_]*)\" *= *\\[ *(?<red>-?\\d{1,3}) *, *(?<green>-?\\d{1,3}) *, *(?<blue>-?\\d{1,3}) *]; *(?:#.*)*)*$|^(?:#.*)$|^name = \"(?<name>[A-Za-z0-9 ]+)\"; *$";
    private final Pattern pattern = Pattern.compile(regex);

    public InputStream load(String filename) throws IOException {
        if(filename.endsWith(".osm")) return loadOSM(filename);
        else if(filename.endsWith(".zip")) return loadZIP(filename);
        return null;
    }

    private InputStream loadZIP(String filename) throws IOException {
        ZipInputStream zip = new ZipInputStream(new FileInputStream(filename));
        zip.getNextEntry();
        return zip;
    }

    private InputStream loadOSM(String filename) throws IOException {
        return new FileInputStream(filename);
    }

    public Theme loadTheme(String file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/themes/" + file)))) {
            Theme theme = new Theme();

            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if(line.isBlank()) continue;
                theme.parseData(line, lineNumber);
            }
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
        URI uri;

        try {
            uri = getClass().getResource(directory).toURI();
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