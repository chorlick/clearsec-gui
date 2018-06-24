package sample;

import entities.Case;
import entities.Year;
import org.apache.commons.io.FilenameUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.solr.util.FileUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;


/**
 * @author chorl_000
 */
public class OsdInterface {

    public final Pattern p = Pattern.compile("([0-9])");
    public List<String> yearArrays = new ArrayList<>();
    public List<String> pdfArrays = new ArrayList<>();

    /**
     * Filters WhitespaceTokenizer with PorterStemFilter,StopFilter.
     */
    private class SavedStreams {

        Tokenizer source;
        TokenStream result;
    }

    ;

    private static final String[] ENGLISH_STOP_WORDS = {
            "a", "an", "and", "are", "as", "at", "be", "but", "by",
            "for", "if", "in", "into", "is", "it", "no", "not", "of",
            "on", "or", "such", "that", "the", "their", "then", "there", "these",
            "they", "this", "to", "was", "will", "with",
            "@author", "@param", "@return", "@deprecated", "@see", "@serial", "@since", "@serial",
            "@throws", "@code", "@link", "<i>", "</i>", "<pre>", "</pre>", "<blockquote>", "</blockquote>",
            "<br>", "</br>", "<b>", "</b>", "<tt>", "</tt>", "@linkplain", "<p>", "<a href=", "</a>",
            "public", "void", "private", "protected", "final", "asbtract", "class", "sttatic", "return"};

    private Set<String> getStopWordsList() {

        Set<String> list = new HashSet<String>();

        for (int i = 0; i < ENGLISH_STOP_WORDS.length; i++) {
            String string = ENGLISH_STOP_WORDS[i];
            list.add(string);
        }

        return list;

    }

    Set<String> stopwords = getStopWordsList();


    public Map<Integer, Year> getYears() throws IOException {
        Map<Integer, Year> yearMap = new TreeMap<>();
        Document doc = Jsoup.connect(ClearSecDefines.OSD_URL)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.87 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("Accept-Encoding", "gzip, deflate")
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("Host", "ogc.osd.mil")
                .header("Connection", "keep-alive")
                .get();

        Elements links = doc.select("a[href]");

        for (Element link : links) {
            if (p.matcher(link.attr("abs:href")).find()) {
                yearMap.put(Integer.valueOf(FilenameUtils.removeExtension(FilenameUtils.getName(new URL(link.attr("abs:href")).getPath()))),
                        new Year(Integer.valueOf(FilenameUtils.removeExtension(FilenameUtils.getName(new URL(link.attr("abs:href")).getPath()))),
                                link.attr("abs:href")));
            }
        }

        return ((TreeMap<Integer, Year>) yearMap).descendingMap();
    }

    public Map<String, Case> getPdfsForPage(Year year) throws IOException {
        Map<String, Case> pdfMap = new HashMap<>();
        Document pdfs = Jsoup.connect(year.getUrl())
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.87 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("Accept-Encoding", "gzip, deflate")
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("Host", "ogc.osd.mil")
                .header("Connection", "keep-alive")
                .header("Upgrade-Insecure-Requests", "1")
                .get();

        Elements pdfLinks = pdfs.select("a[href$=.pdf]");
        for (Element link : pdfLinks) {
            pdfMap.put(FilenameUtils.getName(new URL(link.attr("abs:href")).getPath()),
                    new Case(FilenameUtils.getName(new URL(link.attr("abs:href")).getPath()),
                            link.attr("abs:href"),
                            year));
        }

        return pdfMap;
    }

    public String getCaseInformation(Case _case) throws IOException {
        System.out.println("Getting cass desc");
        Document desc = Jsoup.connect(_case.getYear().getUrl())
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.87 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("Accept-Encoding", "gzip, deflate")
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("Host", "ogc.osd.mil")
                .header("Connection", "keep-alive")
                .header("Upgrade-Insecure-Requests", "1")
                .get();

        System.out.println(desc.title());
        System.out.println("Search for case " + _case.getFileName());
        Elements caseData = desc.select("a[href*=" + _case.getFileName() + "]");
        for (Element link : caseData) {
            return link.parent().parent().toString();
        }
        return "";
    }

    public void getPdf(String pdf) throws FileNotFoundException, IOException {
        URL url = new URL(pdf);
        if (FileUtils.fileExists(ClearSecDefines.DOWNLOAD_DIR + "\\" + FilenameUtils.getName(url.getPath()))) {
            return;
        }
        System.out.printf("Getting file %s\n", pdf);
        Connection.Response pdfFile = Jsoup.connect(pdf)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.87 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("Accept-Encoding", "gzip, deflate")
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("Host", "ogc.osd.mil")
                .header("Connection", "keep-alive")
                .header("Upgrade-Insecure-Requests", "1")
                .ignoreContentType(true)
                .method(Connection.Method.GET).execute();

        int length = pdfFile.bodyAsBytes().length;
        byte[] byteArray = new byte[1024];
        FileOutputStream fileOutput = new FileOutputStream(ClearSecDefines.DOWNLOAD_DIR + "\\" + FilenameUtils.getName(pdfFile.url().getPath()));
        fileOutput.write(pdfFile.bodyAsBytes(), 0, length);
        fileOutput.flush();
        fileOutput.close();
    }

    public void parsePdf(String pdf) throws IOException {
        PDDocument document = PDDocument.load(new File(ClearSecDefines.DOWNLOAD_DIR + "\\" + pdf));
        boolean found = true;
        try {
            document.getClass();
            if (!document.isEncrypted()) {

                PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                stripper.setSortByPosition(true);

                PDFTextStripper tStripper = new PDFTextStripper();

                String pdfFileInText = tStripper.getText(document);
                //System.out.println("Text:" + st);

                // split by whitespace
                String lines[] = pdfFileInText.split("\\r?\\n");
                for (String line : lines) {
                    System.out.println(line);
                }


            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
