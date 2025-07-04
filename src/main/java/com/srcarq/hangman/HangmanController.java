/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.srcarq.hangman;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author srcarq
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class HangmanController {

    @Autowired
    private WordFactory wordFactory;

    private int failAttempts;
    private String selectedWord;
    private List<String> maskedWord = new ArrayList<>();

    @PostMapping("/selectLevel")
    public Map<String, Object> selectLevel(@RequestBody Map<String, Integer> body) {
        maskedWord = new ArrayList<>();
        int selectedLevel = body.get("level");
        selectedWord = wordFactory.selectWordByLevel(selectedLevel);

        int sizeWord = selectedWord.length();
        for (int i = 0; i < sizeWord; i++) {
            maskedWord.add("_");
        }
        
        //Select attemps by level
        if(selectedLevel == 1){
            failAttempts = 10;
        } else if (selectedLevel == 2 ){
            failAttempts = 7;
        } else{
            failAttempts = 5;
        }

        Map<String, Object> response = Map.of(
                "attempts", failAttempts,
                "maskedWord", maskedWord,
                "selectedWord", selectedWord
        );
        return response;
    }

    @PostMapping("/checkLetter")
    public Map<String, Object> checkLetter(@RequestBody Map<String, String> body) {
        String selectedLetter = body.get("letter");

        int containsLetter = selectedWord.indexOf(selectedLetter);

        if (containsLetter == -1) {
            failAttempts -= 1;
        } else {
            for (int i = 0; i < selectedWord.length(); i++) {
                if (String.valueOf(selectedWord.charAt(i)).equals(selectedLetter)) {
                    maskedWord.set(i, selectedLetter);
                }

            }
        }
        
        
        boolean isWinner = !maskedWord.contains("_");
        
        Map<String, Object> response = Map.of(
                "attempts", failAttempts,
                "maskedWord", maskedWord,
                "isGameOver", failAttempts <=0,
                "isWinner", isWinner,
                "resultWord", failAttempts <=0 ? selectedWord : ""
        );
        return response;
    }
    

}
