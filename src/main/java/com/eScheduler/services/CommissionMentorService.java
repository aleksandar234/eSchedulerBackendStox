package com.eScheduler.services;

import com.eScheduler.model.CommissionMentor;
import com.eScheduler.model.MasterClasses;
import com.eScheduler.repositories.CommissionMentorRepository;
import com.eScheduler.repositories.MasterClassesRepository;
import com.eScheduler.responses.customDTOClasses.CommissionMentorDTO;
import com.eScheduler.responses.customDTOClasses.MasterClassDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CommissionMentorService {

    private final CommissionMentorRepository repository;


    public CommissionMentorService(CommissionMentorRepository repository) {
        this.repository = repository;
    }

    public CommissionMentor save(CommissionMentor obj) {
        return repository.save(obj);
    }

    public List<CommissionMentorDTO> getMentorKomisija(Long nastavnikId) {
        return repository.findByNastavnikIdOrderByDatumUnosaDesc(nastavnikId)
                .stream()
                .map(mp -> new CommissionMentorDTO(
                        mp.getId(),
                        mp.getType(),
                        mp.getDegree(),
                        mp.getStudentName(),
                        mp.getTopic(),
                        mp.getNote(),
                        mp.getDatumUnosa()
                ))
                .collect(Collectors.toList());
    }
}
