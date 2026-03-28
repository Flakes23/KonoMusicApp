package com.konomusic.repository;

import com.konomusic.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * AuditLogRepository - Truy cập dữ liệu AuditLog
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByAdminId(Long adminId);

    List<AuditLog> findByAction(String action);

}

