package com.eScheduler.services;

import com.eScheduler.model.MasterClasses;
import com.eScheduler.repositories.MasterClassesRepository;
import com.eScheduler.responses.customDTOClasses.MasterClassDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MasterClassesService {

    private final MasterClassesRepository repository;

    public MasterClassesService(MasterClassesRepository repository) {
        this.repository = repository;
    }



    public List<MasterClassDTO> getMasterPredmetiZaNastavnika(Long nastavnikId) {
        return repository.findByNastavnikIdOrderByDatumUnosaDesc(nastavnikId)
                .stream()
                .map(mp -> new MasterClassDTO(
                        mp.getId(),
                        mp.getPredmetNaMasterStudijama(),
                        mp.getOdrzanoCasova(),
                        mp.getDatumOdrzavanjaCasova(),
                        mp.getDatumUnosa()
                ))
                .collect(Collectors.toList());
    }


    public MasterClasses save(MasterClasses masterPredmet) {
        return repository.save(masterPredmet);
    }

}
