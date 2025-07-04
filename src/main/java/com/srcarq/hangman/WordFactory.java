/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.srcarq.hangman;

import java.util.List;
import java.util.Random;
import org.springframework.stereotype.Component;

/**
 *
 * @author srcarq
 */
@Component
public class WordFactory {

    private List<String> wordListEasy = List.of("CONEJO", "RATON");
    private List<String> wordListMedium = List.of("CAMPANA", "BOSQUE");
    private List<String> wordListHard = List.of("BICICLETA", "ASTRONOMIA");

    public String selectWordByLevel(int level) {
        Random random = new Random();
        List<String> words;
        switch (level) {
            case 1:
                words = wordListEasy;
                break;
            case 2:
                words = wordListMedium;
                break;
            case 3:
                words = wordListHard;
                break;
            default:
                throw new IllegalArgumentException("Error");

        }

        int randomNum = random.nextInt(words.size());
        String selectedWord = words.get(randomNum);
        
        return selectedWord;
    }

}
