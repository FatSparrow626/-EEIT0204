package com.project.core.service;

import com.project.core.dao.SystemLogRepository;
import com.project.core.model.SystemLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SystemLogService {

    @Autowired
    private SystemLogRepository systemLogRepository;

    // [FIXED] Changed operatorEmployeeId type from Long to Integer
    public void log(String logType, String operation, Integer operatorEmployeeId, String operatorUsername,
                    String targetType, String targetId, String targetName, String oldValue, String newValue,
                    String description, String ipAddress, String userAgent, String module) {
        SystemLog log = new SystemLog();
        log.setLogType(logType);
        log.setOperation(operation);
        log.setOperatorEmployeeId(operatorEmployeeId); // Now the types match
        log.setOperatorUsername(operatorUsername);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setTargetName(targetName);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setDescription(description);
        log.setIpAddress(ipAddress);
        log.setUserAgent(userAgent);
        log.setModule(module);
        log.setCreatedAt(LocalDateTime.now());
        systemLogRepository.save(log);
    }
}
