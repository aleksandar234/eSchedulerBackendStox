package com.eScheduler.controllers;


import com.eScheduler.model.MasterClasses;
import com.eScheduler.responses.customDTOClasses.MasterClassDTO;
import com.eScheduler.services.MasterClassesService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/master_predmeti")
@Tag(name = "Master Classes API", description = "API for managing master classes")
public class MasterClassesController {


    private final MasterClassesService service;

    public MasterClassesController(MasterClassesService service) {
        this.service = service;
    }

    // GET: sve aktivnosti za jednog nastavnika
    @GetMapping("/{nastavnikId}")
    public List<MasterClassDTO> getMasterPredmetiZaNastavnika(@PathVariable("nastavnikId") Long nastavnikId) {
        // poziva servis koji vraća listu DTO objekata
        return service.getMasterPredmetiZaNastavnika(nastavnikId);
    }


    // POST: dodavanje nove aktivnosti
    @PostMapping
    public MasterClasses dodajMasterPredmet(@RequestBody MasterClasses masterPredmet) {
        System.out.println("Prosledjeno sa fonta:" + masterPredmet);
        return service.save(masterPredmet);
    }

}
