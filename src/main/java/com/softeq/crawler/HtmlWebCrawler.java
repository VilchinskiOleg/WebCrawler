package com.softeq.crawler;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import java.io.*;
import java.nio.file.Paths;
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

    private XMLReader parser = new CustomHTMLReader();
    private ContentHandler handler;

    private Deque<String> processLinks = new LinkedList<>();

    private int linkDeap;
    private int processingPages;
    private String reportPath;

    public HtmlWebCrawler(CustomParserHandler handler) {
        this.handler = handler;
        this.linkDeap = DEFAULT_MAX_LINK_DEAP;
        this.processingPages = DEFAULT_MAX_PROCESSING_PAGES;
        this.reportPath = DEFAULT_REPORT_PATH;
    }

    public HtmlWebCrawler(String ... regexp) {
        if (regexp.length == 1) {
            Pattern pattern = Pattern.compile(regexp[0]);
            this.handler = new CustomParserHandler(pattern);
        } else {
            this.handler = new CustomParserHandler(regexp);
        }
        this.linkDeap = DEFAULT_MAX_LINK_DEAP;
        this.processingPages = DEFAULT_MAX_PROCESSING_PAGES;
        this.reportPath = DEFAULT_REPORT_PATH;
    }

    public HtmlWebCrawler(int linkDeap, int processingPages, String reportPath, String ... regexp) {
        if (regexp.length == 1) {
            Pattern pattern = Pattern.compile(regexp[0]);
            this.handler = new CustomParserHandler(pattern);
        } else {
            this.handler = new CustomParserHandler(regexp);
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

    public void setHandler(CustomParserHandler handler) {
        if (handler != null){
            this.handler = handler;
        }
    }



    public String start(String URL) {
        parser.setContentHandler(handler);
        processLinks.add(URL);

        String lastLinkInCurrentBlock = URL;
        File report = new File(reportPath);
        if (report.exists()) {
            report.delete();
        }

        while (!processLinks.isEmpty() && linkDeap >= 0 && processingPages >= 0) {
            String currentLink = processLinks.poll();
            try {
                parser.parse(currentLink);
            } catch (IOException | SAXException e) {
                e.printStackTrace();
            }

            Map<String, Integer> result = ((CustomParserHandler) handler).getResult();
            if (!report.exists()) {
                String titleCSV = createTitleCSV(result);
                writeCsvLine(titleCSV, report);
            }
            String lineCSV = convertToCSV(result, currentLink);
            writeCsvLine(lineCSV, report);

            Set<String> links = ((CustomParserHandler) handler).getLinks();
            processLinks.addAll(links);

            if (currentLink.equals(lastLinkInCurrentBlock)) {
                linkDeap--;
                lastLinkInCurrentBlock = processLinks.peekLast();
            }

            processingPages--;
            ((CustomParserHandler) handler).clear();
        }

        return report.getAbsolutePath();
    }



    private void writeCsvLine(String strCSV, File file) {
        try (PrintWriter writer = new PrintWriter(new FileOutputStream(file, true))) {
            writer.println(strCSV);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    BufferedOutputStream
    private String convertToCSV(Map<String, Integer> result, String URL) {
        String str = result.values()
                .stream()
                .map((item) -> "," + item)
                .collect(Collectors.joining());
        return URL + str;
    }

    private String createTitleCSV(Map<String, Integer> result) {
        String title = result.keySet()
                .stream()
                .collect(Collectors.joining(",", ",", ""));
        return "URL" + title;
    }
}
