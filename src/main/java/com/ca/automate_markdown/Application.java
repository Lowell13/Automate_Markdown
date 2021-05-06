package com.ca.automate_markdown;

public class Application {
    public static void main(String[] args) {
        String markdownFile = "src/main/resources/test.md";
        Parser parser = new Parser();
        parser.parse(markdownFile);
    }
}
