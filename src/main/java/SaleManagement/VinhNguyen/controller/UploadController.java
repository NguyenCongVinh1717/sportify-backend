package SaleManagement.VinhNguyen.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/upload")
public class UploadController {

    @Value("${upload.path}")
    private String uploadDir;

    @PostMapping("/multiple")
    public List<String> uploadMultiple(@RequestParam("files") List<MultipartFile> files) throws IOException {

        List<String> fileNames = new ArrayList<>();

        // Get path
        Path rootLocation = Paths.get(uploadDir);

        // Create new directory if not existed
        if (!Files.exists(rootLocation)) {
            Files.createDirectories(rootLocation);
        }

        for (MultipartFile file : files) {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            // Dùng resolve để nối đường dẫn an toàn theo OS (Windows/Ubuntu)
            Path destinationFile = rootLocation.resolve(fileName).normalize().toAbsolutePath();

            // Save file
            Files.copy(file.getInputStream(), destinationFile);

            fileNames.add(fileName);
        }

        return fileNames;
    }
}