package com.rakovsky.judgeTracker.service;

import com.rakovsky.judgeTracker.model.CourtCase;
import com.rakovsky.judgeTracker.repository.CourtRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.*;
@Service
public class CourtService {

    @Autowired
    private CourtRepository courtRepository;

    public List<CourtCase> getAllCases() {
        return courtRepository.findAll();
    }

    public void saveCourtCase(CourtCase courtCase) {
        courtRepository.save(courtCase);
    }

    public void saveCases(List<CourtCase> courtCases) {
        if (!courtCases.isEmpty()) {
            courtRepository.saveAll(courtCases);
        }
    }

    public CourtCase findByCaseNumber(String caseNumber) {
        return courtRepository.findByCaseNumber(caseNumber);
    }

    public void deleteCourtCase(CourtCase courtCase) {
        courtRepository.delete(courtCase);
    }

}
