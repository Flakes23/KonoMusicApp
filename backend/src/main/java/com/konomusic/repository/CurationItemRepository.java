package com.konomusic.repository;

import com.konomusic.entity.CurationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * CurationItemRepository - Truy cập dữ liệu CurationItem
 */
@Repository
public interface CurationItemRepository extends JpaRepository<CurationItem, Long> {

    List<CurationItem> findByStatus(CurationItem.CurationStatus status);

}

