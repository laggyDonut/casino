package de.edvschuleplattling.irgendwieanders;

import de.edvschuleplattling.irgendwieanders.service.TestdatenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DbInitializer implements CommandLineRunner {

    private final TestdatenService testdatenService;

    @Override
    public void run(String... args) throws Exception {

        testdatenService.anlegenTestdaten();

        testdatenService.anlegenTestdatenYannick();

    }


}