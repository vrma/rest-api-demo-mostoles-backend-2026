package com.example.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileUploadUtil {

    public String saveFile(String fileName, MultipartFile multipartFile) throws IOException {

        String fileCode = null;

        // Definimos la ruta donde se va a guardar el archivo recibido, es decir, la
        // imagen
        Path uploadPath = Paths.get("Files-Upload");

        // Comprobamos si existe esta ruta, que será una carpeta (folder), y de lo
        // contrario la creamos
        if (!Files.exists(uploadPath))
            Files.createDirectories(uploadPath);

        // Generar el codigo alfanumerico de 8 caracteres

        // Genera un código que incluye números (0-9) y letras (a-z, A-Z)
        RandomStringGenerator generator = RandomStringGenerator.builder()
                .withinRange('0', 'z')
                .filteredBy(Character::isLetterOrDigit)
                .get();

        // Uso en tu código (ejemplo para 8 caracteres):
        fileCode = generator.generate(8);

        try (InputStream inputStream = multipartFile.getInputStream()) {
            Path destino = uploadPath.resolve(fileCode + '-' + fileName);
            Files.copy(inputStream, destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioe) {
            throw new IOException("Error guardando el archivo de imagen " + fileName, ioe);
        }

        return fileCode;
    }

}
