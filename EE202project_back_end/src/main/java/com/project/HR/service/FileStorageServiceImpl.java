package com.project.HR.service;

import com.project.HR.config.FileStorageProperties;
import com.project.HR.exception.FileStorageException;
import com.project.HR.exception.MyFileNotFoundException;

import lombok.AllArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path fileStorageLocation;

    @Autowired
    public FileStorageServiceImpl(FileStorageProperties properties) {
        // 1. 透過Configuration從application.properties 取得資料夾路徑字串->絕對路徑
        this.fileStorageLocation = Paths.get(properties.getUploadDir()).toAbsolutePath();
        try {
            // 2. 建立上傳用的資料夾
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("沒辦法建立上傳用的資料夾",
                    ex);
        }
    }

    @Override
    public String store(MultipartFile file, String leaveRecordUuid) {
        // 1. 取得檔案名稱
        String originalFileName = file.getOriginalFilename();

        // 2. 取得副檔名 因為只是內部請假證明(故省略複雜的例外考量與驗證設計)
        String fileExtension = "";
        try {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        } catch (Exception e) {
            fileExtension = "";
        }

        // 3. 拼接安全檔案名稱 + 產生檔案路徑
        String storedFileName = UUID.randomUUID().toString() + fileExtension;
        Path fileStorePath = this.fileStorageLocation.resolve(storedFileName);

        try {
            // 4. 從前端儲存檔案到本地端
            Files.copy(file.getInputStream(), fileStorePath, StandardCopyOption.REPLACE_EXISTING);
            // 5. 回傳檔案名稱
            return storedFileName;
        } catch (Exception e) {
            throw new FileStorageException("無法儲存檔案 " + originalFileName, e);
        }

    }

    @Override
    public Path load(String leaveRecordUuid, String filename) {
        return this.fileStorageLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String leaveRecordUuid, String filename) {
        try {
            Path filePath = this.load(leaveRecordUuid, filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found " + filename);
            }
        } catch (MalformedURLException ex) {
            throw new MyFileNotFoundException("File not found " + filename, ex);
        }
    }

    @Override
    public void delete(String leaveRecordUuid, String filename) {
        try {
            Path filePath = this.load(leaveRecordUuid, filename);
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new FileStorageException("Could not delete file " + filename + ". Please try again!", ex);
        }
    }

}
