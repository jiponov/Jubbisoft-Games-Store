package app.treasury.service;

import app.treasury.model.Treasury;
import app.treasury.repository.TreasuryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
public class TreasuryInitialize implements CommandLineRunner {

    private final TreasuryService treasuryService;


    @Autowired
    public TreasuryInitialize(TreasuryService treasuryService) {
        this.treasuryService = treasuryService;
    }


    @Override
    public void run(String... args) throws Exception {

        // създаваме Treasury ако не съществува
        treasuryService.initializeTreasury();
    }
}