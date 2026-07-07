package com.example.utilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;

@Component
public class FileDownloadUtil {

    private Path foundFile;

    public Resource getFileAsResource(String fileCode) throws IOException {

        /**
         * Buscar en la carpeta donde se han subido los archivos (imagenes de los
         * productos)
         * al servidor, a ver si hay alguno que comience por el fileCode suministrado,
         * es decir,
         * recibido como parametro en este metodo
         */

        Path dirPath = Paths.get("Files-Upload");

        try {
            foundFile = Files.list(dirPath)
                    .filter(file -> file.getFileName().toString().startsWith(fileCode))
                    .findFirst().get();
        } catch (IOException ioe) {
            throw new IOException("Error fatal bucando el fichero ", ioe);
        }

        if (foundFile != null)
            return new UrlResource(foundFile.toUri());

        return null;

    }
}
