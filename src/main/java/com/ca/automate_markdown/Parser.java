package com.ca.automate_markdown;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Parser {

    private State state = State.INITIAL;
    private final StringBuffer jsonReturned = new StringBuffer("{\n");

    public void parse(String pathFile) {
        State newState;
        List<String> listStrings = new ArrayList<>();

        try {
            File file = new File(pathFile);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                System.out.println("Line : " + line);

                newState = checkNextState(listStrings, line);

                if (state == State.CODE) {
                    while (checkNextState(listStrings, line = bufferedReader.readLine()) != State.CODE) {
                        // WHILE while we're not in a CODE state
                    }
                        appendCode(listStrings);
                        listStrings = new ArrayList<>();
                        // Skip line for end of code
                        bufferedReader.readLine();
                } else if (state == State.TITLE) {
                    if (newState != State.TITLE) {
                        appendTitle(listStrings);
                        listStrings = new ArrayList<>();
                        if (newState != State.EMPTY_LINE && newState != State.INITIAL) {
                            listStrings.add(line);
                        }
                    }
                } else if (state == State.LIST) {
                    if (newState != State.LIST) {
                        appendList(listStrings);
                        listStrings = new ArrayList<>();
                        if (newState != State.EMPTY_LINE && newState != State.INITIAL) {
                            listStrings.add(line);
                        }
                    }
                } else if (state == State.PARAGRAPH) {
                    if (newState != State.PARAGRAPH) {
                        appendParagraph(listStrings);
                        listStrings = new ArrayList<>();
                        if (newState != State.EMPTY_LINE && newState != State.INITIAL) {
                            listStrings.add(line);
                        }
                    }
                } else if (state == State.EMPTY_LINE || state == State.INITIAL) {
                    // DO NOTHING.
                }

                state = newState;
                System.out.println("State : " + state);

            }

            if (this.state == State.CODE) {
                appendCode(listStrings);
            } else if (this.state == State.LIST) {
                appendList(listStrings);
            } else if (this.state == State.TITLE) {
                appendTitle(listStrings);
            } else if (this.state == State.PARAGRAPH) {
                appendParagraph(listStrings);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        jsonReturned.append("}");
        System.out.println("This is JSON :\n" + jsonReturned);
    }

    private State checkNextState(List<String> buffer, String line) {
        State toReturn;

        if (line.matches("```.*")) {
            toReturn = State.CODE;
        } else if (line.matches("^(?![\\s\\S])")) {
            toReturn = State.EMPTY_LINE;
        } else if (line.matches("#.*")) {
            buffer.add(line);
            toReturn = State.TITLE;
        } else if (line.matches("\\*.*")) {
            buffer.add(line);
            toReturn = State.LIST;
        } else {
            buffer.add(line);
            toReturn = State.PARAGRAPH;
        }

        return toReturn;
    }

    private void appendTitle(List<String> listStrings) {
        int titleSize = 0;
        String tmp = listStrings.get(0);

        while (tmp.charAt(titleSize) == '#') {
            titleSize++;
        }

        tmp = tmp.substring(titleSize);

        this.jsonReturned
                .append("\"h")
                .append(titleSize)
                .append("\": \"")
                .append(tmp.trim())
                .append("\",")
                .append("\n");
    }

    private void appendCode(List<String> listStrings) {

        this.jsonReturned.append("\"code\": \"");

        for (String str: listStrings) {
            this.jsonReturned.append(str);
        }

        this.jsonReturned.append("\",\n");
    }

    private void appendParagraph(List<String> listStrings) {
        this.jsonReturned.append("\"p\": \"");

        for (String str: listStrings) {
            this.jsonReturned.append(str);
        }

        this.jsonReturned.append("\",\n");
    }

    private void appendList(List<String> listStrings) {
        this.jsonReturned.append("\"ul\": [");

        for(String str: listStrings) {
            str = str.substring(2);
            jsonReturned
                    .append("\"")
                    .append(str)
                    .append("\", ");
        }

        jsonReturned.append("]")
                .append("\",\n");    }
}
