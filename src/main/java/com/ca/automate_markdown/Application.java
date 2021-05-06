package com.ca.automate_markdown;

public class Application {
    public static void main(String[] args) {
        var markdownFile = "src/main/resources/test.md";
        var parser = new Parser();
        parser.parse(markdownFile);
    }
}
