package com.ca.automate_markdown;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Parser {

    private State state = State.INITIAL;
    private final StringBuilder jsonReturned = new StringBuilder("{\n");

    private static final String EOL = "\",\n";
    private static final String APPEND_CODE = "\"code\": \"";
    private static final String APPEND_PARAGRAPHE = "\"p\": \"";

    private static final Logger LOGGER = Logger.getLogger(Parser.class);

    public void parse(String pathFile) {
        State newState;
        List<String> listStrings = new ArrayList<>();

        try (var bufferedReader = new BufferedReader(new FileReader(pathFile))) {
            String line;

            while (!state.equals(State.FINISHED)) {
                line = bufferedReader.readLine();
                LOGGER.debug("Line : " + line);

                newState = checkNextState(line);
                if (newState == State.TITLE || newState == State.LIST || newState == State.PARAGRAPH) {
                    listStrings.add(line);
                }

                if (newState == State.CODE) {
                    do {
                        line = bufferedReader.readLine();
                        newState = checkNextState(line);
                        if (newState != State.CODE) {
                            listStrings.add(line);
                        }
                    } while (newState != State.CODE);

                    appendCodeAndParagraphe(APPEND_CODE, listStrings);
                    listStrings = new ArrayList<>();
                } else if (state == State.TITLE && newState != State.TITLE) {
                    appendTitle(listStrings);
                    listStrings = new ArrayList<>();
                    if (newState != State.EMPTY_LINE && newState != State.INITIAL) {
                        listStrings.add(line);
                    }
                } else if (state == State.LIST && newState != State.LIST) {
                    appendList(listStrings);
                    listStrings = new ArrayList<>();
                    if (newState != State.EMPTY_LINE && newState != State.INITIAL) {
                        listStrings.add(line);
                    }
                } else if (state == State.PARAGRAPH && newState != State.PARAGRAPH) {
                    appendCodeAndParagraphe(APPEND_PARAGRAPHE, listStrings);
                    listStrings = new ArrayList<>();
                    if (newState != State.EMPTY_LINE && newState != State.INITIAL) {
                        listStrings.add(line);
                    }
                } else if (newState == State.EOF) {
                    newState = State.FINISHED;
                }

                state = newState;
                LOGGER.debug("State : " + state);
            }

        } catch (IOException e) {
            LOGGER.debug(e.getStackTrace());
        }

        jsonReturned.append("}");
        LOGGER.info("This is the equivalent JSON :\n" + jsonReturned);
    }

    private State checkNextState(String line) {
        State toReturn;

        if (line == null) {
            toReturn = State.EOF;
        } else if (line.matches("```.*")) {
            toReturn = State.CODE;
        } else if (line.matches("^(?![\\s\\S])")) {
            toReturn = State.EMPTY_LINE;
        } else if (line.matches("#.*")) {
            toReturn = State.TITLE;
        } else if (line.matches("\\*.*")) {
            toReturn = State.LIST;
        } else {
            toReturn = State.PARAGRAPH;
        }

        return toReturn;
    }

    private void appendTitle(List<String> listStrings) {
        var titleSize = 0;
        var tmp = listStrings.get(0);

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

    private void appendCodeAndParagraphe(String appendType, List<String> listStrings) {
        this.jsonReturned.append(appendType);

        for (var i = 0; i < listStrings.size(); i++) {
            var str = listStrings.get(i);

            if (str.contains("\"")) {
                str = str.replace("\"", "\\\"");
            }
            this.jsonReturned.append(str);

            if (i + 1 < listStrings.size()) {
                this.jsonReturned.append("\\n");
            }
        }

        this.jsonReturned.append(EOL);
    }

    private void appendList(List<String> listStrings) {
        this.jsonReturned.append("\"ul\": [");

        for(var i = 0; i < listStrings.size(); i++) {
            var str = listStrings.get(i).substring(2);

            jsonReturned
                    .append("\"")
                    .append(str);

            if (i + 1 < listStrings.size()) {
                jsonReturned.append("\", ");
            } else {
                jsonReturned.append("\"");
            }
        }

        jsonReturned.append("]")
                .append(EOL);
    }
}
