package de.edvschuleplattling.irgendwieanders.service;

import de.simonaltschaeffl.poker.dto.CardDTO;
import de.simonaltschaeffl.poker.dto.GameStateDTO;
import de.simonaltschaeffl.poker.dto.PlayerDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Field;

public class PokerDtoInspector {
    public static void inspect() {
        System.out.println("--- DTO Inspection ---");
        printFields(GameStateDTO.class);
        printFields(PlayerDTO.class);
        printFields(CardDTO.class);

        // Also check if Jackson works correctly (e.g. record fields)
        try {
            ObjectMapper mapper = new ObjectMapper();
            // We can't easily instantiate records with dummy data without knowing constructor,
            // but we can check if Jackson sees the fields.
            // Or just rely on field inspection.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printFields(Class<?> clazz) {
        System.out.println("Fields of " + clazz.getSimpleName() + ":");
        for (Field f : clazz.getDeclaredFields()) {
            System.out.println("  - " + f.getName() + " (" + f.getType().getSimpleName() + ")");
        }
    }

    public static void main(String[] args) {
        inspect();
    }
}

