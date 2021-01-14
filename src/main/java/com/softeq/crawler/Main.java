package com.softeq.crawler;

import com.softeq.crawler.jsoup.JsoupHandler;
import com.softeq.crawler.service.HtmlWebCrawler;

import java.io.IOException;

public class Main {

    // Input Data:
    private static final String ROOT_URI = "https://en.wikipedia.org/wiki/Elon_Musk";
    private static final String [] PATTERNS = new String[] {"Musk", "Tesla", "Elon"};
    private static int linkDeap = 8;
    private static int countPages = 1000;

    public static void main(String[] args) {
//        HtmlWebCrawler crawler = new HtmlWebCrawler(PATTERNS);
//        crawler.setLinkDeap(0);
//        System.out.println(crawler.start(ROOT_URI));
        try {
            new JsoupHandler().parse(ROOT_URI);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
