package com.softeq.crawler;

public class Main {

    // Input Data:
    private static final String ROOT_URI = "https://en.wikipedia.org/wiki/Elon_Musk";
    private static final String [] PATTERNS = new String[] {"Musk", "Tesla", "Elon"};
    private static int linkDeap = 8;
    private static int countPages = 1000;

    public static void main(String[] args) {
        HtmlWebCrawler crawler = new HtmlWebCrawler(PATTERNS);
        crawler.setLinkDeap(0);
        System.out.println(crawler.start(ROOT_URI));
    }
}
