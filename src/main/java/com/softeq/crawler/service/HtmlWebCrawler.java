package com.softeq.crawler.service;

import com.softeq.crawler.jsoup.JsoupHandler;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/***
 * Service:
* */
public class HtmlWebCrawler {
    private static final int DEFAULT_MAX_LINK_DEAP = 8;
    private static final int DEFAULT_MAX_PROCESSING_PAGES = 1000;
    private static final String DEFAULT_REPORT_PATH = "report.txt";

    private String root;
    private JsoupHandler handler;
    private Deque<String> processLinks = new LinkedList<>();
    private int linkDeap;
    private int processingPages;
    private String reportPath;



    public HtmlWebCrawler(JsoupHandler handler) {
        this.handler = handler;
    }

    public HtmlWebCrawler(String ... regexp) {
        if (regexp.length == 1) {
            Pattern pattern = Pattern.compile(regexp[0]);
            this.handler = new JsoupHandler(pattern);
        } else {
            this.handler = new JsoupHandler(regexp);
        }
        this.linkDeap = DEFAULT_MAX_LINK_DEAP;
        this.processingPages = DEFAULT_MAX_PROCESSING_PAGES;
        this.reportPath = DEFAULT_REPORT_PATH;
    }

    public HtmlWebCrawler(int linkDeap, int processingPages, String reportPath, String ... regexp) {
        if (regexp.length == 1) {
            Pattern pattern = Pattern.compile(regexp[0]);
            this.handler = new JsoupHandler(pattern);
        } else {
            this.handler = new JsoupHandler(regexp);
        }
        this.linkDeap = linkDeap;
        this.processingPages = processingPages;
        this.reportPath = reportPath;
    }



    public int getLinkDeap() {
        return linkDeap;
    }

    public void setLinkDeap(int linkDeap) {
        this.linkDeap = linkDeap;
    }

    public int getProcessingPages() {
        return processingPages;
    }

    public void setProcessingPages(int processingPages) {
        this.processingPages = processingPages;
    }

    public String getReportPath() {
        return reportPath;
    }

    public void setReportPath(String reportPath) {
        this.reportPath = reportPath;
    }



    public String start(String URL) {
        this.root = URL;
        processLinks.add(this.root);
        String lastLinkInCurrentBlock = URL;
        File file = new File(reportPath);
        if (file.exists()) {
            file.delete();
        }

        while (!processLinks.isEmpty() && linkDeap >= 0 && processingPages >= 0) {
            String currentUrl = processLinks.poll();
            try {
                handler.parse(currentUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }

            writeCsvLine(file, currentUrl);

            Set<String> links = handler.getLinks();
            processLinks.addAll(links);

            if (currentUrl.equals(lastLinkInCurrentBlock)) {
                linkDeap--;
                lastLinkInCurrentBlock = processLinks.peekLast();
            }

            processingPages--;
        }

        return file.getAbsolutePath();
    }



    private void writeCsvLine(File file, String currentUrl) {
        try (PrintWriter wr = new PrintWriter(new FileOutputStream(file, true))) {
            if (this.root.equals(currentUrl)) {
                String t = createTitleCSV();
                wr.println(t);
            }
            String l = createLineCSV(currentUrl);
            wr.println(l);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String createLineCSV(String URL) {
        String str = handler.getResult().values()
                .stream()
                .map((item) -> "," + item)
                .collect(Collectors.joining());
        return URL + str;
    }

    private String createTitleCSV() {
        String title = handler.getResult().keySet()
                .stream()
                .collect(Collectors.joining(",", ",", ""));
        return "URL" + title;
    }
}
