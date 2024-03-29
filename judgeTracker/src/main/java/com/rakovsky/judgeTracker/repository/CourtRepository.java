package com.rakovsky.judgeTracker.repository;

import com.rakovsky.judgeTracker.model.CourtCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourtRepository extends JpaRepository<CourtCase, Long> {

    CourtCase findByCaseNumber(String caseNumber);
}
