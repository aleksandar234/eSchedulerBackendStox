package com.eScheduler.services;

import com.eScheduler.model.MasterClasses;
import com.eScheduler.repositories.MasterClassesRepository;
import com.eScheduler.responses.customDTOClasses.MasterClassDTO;
import com.eScheduler.responses.customDTOClasses.MasterDoctoralClassSYDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
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
                .filter(mp -> Objects.equals(mp.getStepenStudija(), "master"))
                .map(mp -> new MasterClassDTO(
                        mp.getId(),
                        mp.getPredmetNaPostakademskimStudijama(),
                        mp.getOdrzanoCasova(),
                        mp.getDatumOdrzavanjaCasova(),
                        mp.getDatumUnosa(),
                        mp.getNapomena()
                ))
                .collect(Collectors.toList());
    }

    public List<MasterClassDTO> getDoktorskiPredmetiZaNastavnika(Long nastavnikId) {
        return repository.findByNastavnikIdOrderByDatumUnosaDesc(nastavnikId)
                .stream()
                .filter(mp -> Objects.equals(mp.getStepenStudija(), "doktorat"))
                .map(mp -> new MasterClassDTO(
                        mp.getId(),
                        mp.getPredmetNaPostakademskimStudijama(),
                        mp.getOdrzanoCasova(),
                        mp.getDatumOdrzavanjaCasova(),
                        mp.getDatumUnosa(),
                        mp.getNapomena()
                ))
                .collect(Collectors.toList());
    }

    public List<MasterDoctoralClassSYDTO> getMasterDoktorskiPredmetiPoSkolskojGodini(Long skolskaGodinaId) {
        return repository.findBySkolskaGodinaId(skolskaGodinaId)
                .stream()
                .map(mp -> new MasterDoctoralClassSYDTO(
                        mp.getId(),
                        mp.getPredmetNaPostakademskimStudijama(),
                        mp.getOdrzanoCasova(),
                        mp.getDatumOdrzavanjaCasova(),
                        mp.getDatumUnosa(),
                        mp.getNapomena(),
                        mp.getSkolskaGodinaId(),
                        mp.getStepenStudija(),
                        mp.getNastavnikId()
                ))
                .collect(Collectors.toList());
    }


    public MasterClasses save(MasterClasses masterPredmet) {
        return repository.save(masterPredmet);
    }

}
