package com.moearthur.idsdownloader;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class GetUrl {
    Scanner mScanner = new Scanner(System.in);
    MainClass mainDownloader = new MainClass();
    ArrayList<String> urlList = new ArrayList<String>();
    ArrayList<String> imgUrlList = new ArrayList<String>();
    ArrayList<String> pathList = new ArrayList<String>();
    boolean isHaveSame = false;

    int mLastSeek = 0;// 当前数据流游标位置
    int mNowPrograss = 0;// 当前进度0-100

    public void start() {
        System.out.print("输入起始编号:");
        int a = Integer.valueOf(mScanner.nextLine());
        System.out.print("输入结束编号:");
        int b = Integer.valueOf(mScanner.nextLine());
        System.out.println("");
        if (a <= 1)
            a = 1;
        if (b >= 50)
            b = 50;
        if (b <= 1)
            b = 1;
        try {
            getUrls();
            downloadImg(a, b);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void downloadImg(int startNum, int endNum) throws IOException {
        for (int i = startNum - 1; i < endNum; i++) {
            getImgUrls(urlList.get(i));
            new File(pathList.get(i)).mkdir();
            for (int j = 0; j < imgUrlList.size(); j++) {
                download(imgUrlList.get(j), pathList.get(i));
            }
        }
        for (String string : imgUrlList) {
            System.out.println(string);
        }
        System.out.println("全部下载完毕");
    }

    void download(String url, String path) throws IOException {
        String fileName = url.substring(url.lastIndexOf("/") + 1, url.length());
        File file = new File(path + "/" + fileName);
        if (file.exists()) {
        } else {
            HttpURLConnection mHttpURLConnection = (HttpURLConnection) new URL(url).openConnection();
            mHttpURLConnection.connect();
            if (mHttpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream mInputStream = mHttpURLConnection.getInputStream();
                RandomAccessFile mRandomAccessFile = new RandomAccessFile(path + "/" + fileName, "rwd");
                mRandomAccessFile.setLength(mHttpURLConnection.getContentLengthLong());
                mRandomAccessFile.seek(0);
                int readLength = 0;
                byte buffer[] = new byte[1024 * 8];
                System.out.print("正在下载→" + fileName + "------");
                while ((readLength = mInputStream.read(buffer)) != -1) {
                    mRandomAccessFile.write(buffer, 0, readLength);
                    mLastSeek = mLastSeek + readLength;
                    mNowPrograss = (int) (mLastSeek / (mRandomAccessFile.length() / 100));
                    System.out.print(mNowPrograss + "%");
                    for (int i = 0; i <= String.valueOf(mNowPrograss).length(); i++) {
                        System.out.print("\b");
                    }
                }
                System.out.print("下载完成\n");
                mLastSeek = 0;
                mNowPrograss = 0;
                mInputStream.close();
                mRandomAccessFile.close();
            }
        }
    }

    void getUrls() throws IOException {
        Connection mConnection = Jsoup.connect("http://www.project-imas.com/wiki/THE_iDOLM@STER:_Million_Live!");
        mConnection.timeout(60 * 1000);
        mConnection.userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.122 Safari/537.36");
        Response mResponse = mConnection.method(Method.GET).execute();
        Document mDocument = Jsoup.parse(mResponse.body());
        for (int i = 0; i < 3; i++) {
            Elements mElements = mDocument.getElementsByClass("wikitable").get(i).select("a[href]");
            for (Element element : mElements) {
                for (String url : urlList) {
                    if (url.equals("http://www.project-imas.com" + element.attr("href"))) {
                        isHaveSame = true;
                    }
                }
                if (isHaveSame) {
                    isHaveSame = false;
                    continue;
                } else {
                    urlList.add("http://www.project-imas.com" + element.attr("href"));
                }
            }
        }
        for (String url : urlList) {
            pathList.add(url.substring(url.lastIndexOf("/") + 1, url.length()));
        }
        for (String string : pathList) {
            System.out.println(string);
        }
        System.out.println("列表加载完成！");
    }

    void getImgUrls(String wikiUrl) throws IOException {
        imgUrlList.clear();
        Connection mConnection = Jsoup.connect(wikiUrl);
        mConnection.timeout(60 * 1000);
        mConnection.userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.122 Safari/537.36");
        Response mResponse = mConnection.method(Method.GET).execute();
        Document mDocument = Jsoup.parse(mResponse.body());
        Elements mElements = mDocument.select("[class=gallery mw-gallery-traditional]");
        if (mDocument.toString().indexOf("id=\"THE_iDOLM.40STER_Million_Live.21:_Theater_Days_Cards\"") != -1) {
            for (Element element : mElements.get(mElements.size() - 2).select("[src]")) {
                String baseUrl = element.attr("src");
                imgUrlList.add("http://www.project-imas.com/w/images" + baseUrl.substring(15, baseUrl.lastIndexOf("/")));
            }
        }
        for (Element element : mElements.get(mElements.size() - 1).select("[src]")) {
            String baseUrl = element.attr("src");
            imgUrlList.add("http://www.project-imas.com/w/images" + baseUrl.substring(15, baseUrl.lastIndexOf("/")));
        }
        System.out.println("下载地址加载完成！");
    }
}