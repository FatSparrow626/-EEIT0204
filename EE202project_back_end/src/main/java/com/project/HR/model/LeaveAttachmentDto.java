package com.project.HR.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveAttachmentDto {

    private Long id;
    private String fileName;
    private String storedFileName;
    private String fileType;
    private long fileSize;
    private String downloadUrl;

}
