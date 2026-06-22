package com.babygearpass.repository;

import com.babygearpass.entity.QualityCheckMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QualityCheckMaterialRepository extends JpaRepository<QualityCheckMaterial, Long> {

    List<QualityCheckMaterial> findByQualityCheckId(Long qualityCheckId);

    void deleteByQualityCheckId(Long qualityCheckId);
}
