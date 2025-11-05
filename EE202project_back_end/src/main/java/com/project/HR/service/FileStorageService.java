package com.project.HR.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface FileStorageService {

    /**
     * 儲存用戶上傳的資料
     *
     * @param file            The multipart file from the upload request.
     * @param leaveRecordUuid A unique identifier for the leave record to create a
     *                        sub-directory.
     * @return String 回傳唯一的檔名
     */
    String store(MultipartFile file, String leaveRecordUuid);

    /**
     * 載入檔案 as a resource.
     *
     * @param leaveRecordUuid The UUID of the leave record.
     * @param filename        The name of the file to load.
     * @return The path to the loaded file.
     */
    Path load(String leaveRecordUuid, String filename);

    /**
     * 載入檔案 as a Spring Resource.
     *
     * @param leaveRecordUuid The UUID of the leave record.
     * @param filename        The name of the file to load.
     * @return The file as a Resource.
     */
    Resource loadAsResource(String leaveRecordUuid, String filename);

    /**
     * 刪除指定檔案
     *
     * @param leaveRecordUuid The UUID of the leave record.
     * @param filename        The name of the file to delete.
     */
    void delete(String leaveRecordUuid, String filename);

}
