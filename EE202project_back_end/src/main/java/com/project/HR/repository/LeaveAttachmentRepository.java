package com.project.HR.repository;

import com.project.HR.model.LeaveAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeaveAttachmentRepository extends JpaRepository<LeaveAttachment, Long> {

    Optional<LeaveAttachment> findByLeaveRecordUuidAndStoredFileName(String uuid, String storedFileName);
}
