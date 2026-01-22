package com.eScheduler.controllers;

import com.eScheduler.model.CommissionMentor;
import com.eScheduler.model.MasterClasses;
import com.eScheduler.responses.customDTOClasses.CommissionMentorDTO;
import com.eScheduler.responses.customDTOClasses.MasterClassDTO;
import com.eScheduler.services.CommissionMentorService;
import com.eScheduler.services.MasterClassesService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/commission-mentor")
public class CommissionMentorController {

    private final CommissionMentorService service;

    public CommissionMentorController(CommissionMentorService service) {
        this.service = service;
    }


    @GetMapping("/{nastavnikId}")
    public List<CommissionMentorDTO> getMentorKomisijuZaNastavnika(@PathVariable("nastavnikId") Long nastavnikId) {
        // poziva servis koji vraća listu DTO objekata
        return service.getMentorKomisija(nastavnikId);
    }

    @PostMapping
    public CommissionMentor dodajKomisijuMentora(@RequestBody CommissionMentor obj) {
        System.out.println("Prosledjeno sa fonta:" + obj);
        return service.save(obj);
    }
}
