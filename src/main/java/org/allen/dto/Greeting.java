package org.allen.dto;

import java.io.Serializable;

public class Greeting implements Serializable {
    private String word;

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}
