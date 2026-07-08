package com.example.utilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Component;

@Component
public class FileUtil {

    public void eliminarArchivo(String fileName) {

        Path uploadPath = Paths.get("Files-Upload");
        Path fileNamePath = uploadPath.resolve(fileName);

        try {
            Files.deleteIfExists(fileNamePath);
        } catch (IOException ioe) {
            throw new RuntimeException("No ha podido ser eliminada la imagen");
        }
    }
}
