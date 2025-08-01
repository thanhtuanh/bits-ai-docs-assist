package com.bits.aidocassist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.bits.aidocassist.model.Document;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
}