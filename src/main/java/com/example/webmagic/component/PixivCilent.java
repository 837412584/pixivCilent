package com.example.webmagic.component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.webmagic.util.Constants;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @program: webmagic
 * @description:
 * @author: kashen
 * @create: 2019-07-04 16:18
 **/


@Component
public class PixivCilent {

    private static int filtedCount = 0;
    private static HttpGet get;
    private static HttpPost post;
    private static CookieStore cookieStore;
    private static CloseableHttpResponse response;

    private final static Logger logger = LoggerFactory.getLogger(PixivCilent.class);


    static {
        cookieStore = new BasicCookieStore();
    }

    /**
     * 登录前的预备方法，用于获取登录时的动态参数：post_key
     *
     * @return
     */
    private String preLogin() {
        String post_keyStr = "";
        get = new HttpGet(Constants.PIXIV_BASE_URL);
        try (
                //创建client时传入一个cookieStore
                CloseableHttpClient client = HttpClients.custom().setDefaultCookieStore(cookieStore).build()
        ) {
            response = client.execute(get);
            String responseContent = EntityUtils.toString(response.getEntity());

            //解析返回的网页，获取到post_key
            Document doc = Jsoup.parse(responseContent);
            Element post_key = doc.select("input[name=post_key]").get(0);
            post_keyStr = post_key.attr("value");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return post_keyStr;
    }

    /**
     * 登录
     */
    public boolean login() {
        boolean isSuccess = false;
        String post_keyStr = preLogin();
        post = new HttpPost(Constants.PIXIV_LOGIN_URL);
        try (
                //创建client时传入一个cookieStore
                CloseableHttpClient client = HttpClients.custom().setDefaultCookieStore(cookieStore).build()
        ) {

            //准备参数
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("pixiv_id", Constants.USERNAME));
            params.add(new BasicNameValuePair("password", Constants.PASSWORD));
            params.add(new BasicNameValuePair("post_key", post_keyStr));
            params.add(new BasicNameValuePair("captcha", ""));
            params.add(new BasicNameValuePair("g_reaptcha_response", ""));
            params.add(new BasicNameValuePair("source", "pc"));
            params.add(new BasicNameValuePair("ref", ""));
            params.add(new BasicNameValuePair("return_to", "https://www.pixiv.net/"));
            post.setEntity(new UrlEncodedFormEntity(params, Charset.forName("UTF-8")));
            response = client.execute(post);
            String responseContent = EntityUtils.toString(response.getEntity());
            //解析返回的json
            JSONObject responseJson = JSON.parseObject(responseContent);
            JSONObject responseJsonBody = JSON.parseObject(responseJson.get("body").toString());
            if (responseJsonBody.containsKey("success")) {
                logger.info("登录成功");
                isSuccess = true;
            } else {
                logger.error(String.valueOf(responseJsonBody.get("validation_errors")));
                throw new Exception("登录失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return isSuccess;
    }

    /**
     * 执行搜索入口方法
     */
    public void search() {
        int i = 1;
        boolean hasNextPage = true;
        while (hasNextPage) {
            String responseContent = getPage(buildSearchUrl(Constants.KEY_WORD, Constants.IS_R18, i));
            parseSearchResult(responseContent);
            hasNextPage = hasNextPage(responseContent);
            i++;
        }
    }

    /**
     * 执行每日排行榜方法
     */
    public void daily(String date) {
        String responseContent = null;
        if (StringUtils.isEmpty(date)) {
            responseContent = getPage(buildDailyUrl(null, Constants.IS_R18));
            Calendar now = Calendar.getInstance();
            now.setTime(new Date());
            now.add(Calendar.DATE, -1);
            date = new SimpleDateFormat("yyyyMMdd").format(now.getTime());
        } else {
            responseContent = getPage(buildDailyUrl(date, Constants.IS_R18));
        }
        parseDailyResult(responseContent, date);
    }

    /**
     * 发送HTTP/HTTPS请求并返回整个网页
     *
     * @param url
     * @return
     */
    public String getPage(String url) {
        String responseContent = "";
        get = new HttpGet(url);
        logger.info("geturl:" + url);
        try (
                //创建client时传入一个cookieStore
                CloseableHttpClient client = HttpClients.custom().setDefaultCookieStore(cookieStore).build()
        ) {
            response = client.execute(get);
            responseContent = EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return responseContent;
    }

    /**
     * 解析搜索请求返回的结果
     *
     * @param responseContent
     */
    public void parseSearchResult(String responseContent) {
        Document doc = Jsoup.parse(responseContent);
        Element dataListElement = doc.select("#js-mount-point-search-result-list").get(0);
        JSONArray myJsonArray = (JSONArray) JSONArray.parse(dataListElement.attr("data-items"));
        myJsonArray.forEach((Object json) -> {
            JSONObject jsonObject = (JSONObject) json;
            logger.info("data-item:" + jsonObject.toJSONString());
            if (StringUtils.isEmpty(jsonObject.getBoolean("isAdContainer")) || !jsonObject.getBoolean("isAdContainer")) {
                String illustTitle = jsonObject.getString("illustTitle");
                int bookmarkCount = jsonObject.getInteger("bookmarkCount");
                String illustType = jsonObject.getString("illustType");
                int pageCount = jsonObject.getInteger("pageCount");
                String illustId = jsonObject.getString("illustId");
                //点赞数过滤
                if (bookmarkCount >= Constants.STARS) {
                    filtedCount++;
                    //作品为图片
                    if (illustType.equals("0")) {

                        //创建文件夹（文件名不能有空格）
                        illustTitle.replaceAll(" ", "_");
                        String directoryPath = illustId;
//                        String directoryPath = illustTitle + "_stars_" + bookmarkCount;
//                        makeDirectory(directoryPath);
                        makeDirectory(directoryPath, null);
                        //只有一张图，访问图片主页
                        if (pageCount == 1) {
                            String mediumContent = (getPage(Constants.PIXIV_ILLUST_MEDIUM_URL + illustId));
                            //解析网页中的js脚本，过滤出大图的url
                            Document mangaDoc = Jsoup.parse(mediumContent);
                            Elements mangaElements = mangaDoc.getElementsByTag("script");
                            for (Element element : mangaElements) {
                                String data = element.data();
                                //包含所需数据的script标签以以下内容开头，其他忽略
                                if (data.startsWith("'use strict';var globalInitData")) {
                                    String imgUrl = data.substring(data.indexOf("regular") + 10, data.indexOf("original") - 3).replaceAll("\\\\", "");


                                    try {
                                        imgDownload(imgUrl, directoryPath + "/" + illustId + "_" + "0", null);
                                    } catch (Exception e) {
                                        logger.error(imgUrl + "\r\n" + "下载失败");
                                        continue;
                                    }


                                }
                            }
                        }
                        //多图，访问图片列表页
                        else if (pageCount > 1) {
                            String mediumContent = (getPage(Constants.PIXIV_ILLUST_MEDIUM_URL + illustId));
                            //解析网页中的js脚本，过滤出大图的url
                            Document mangaDoc = Jsoup.parse(mediumContent);
                            Elements mangaElements = mangaDoc.getElementsByTag("script");
                            for (Element element : mangaElements) {
                                String data = element.data();
                                //包含所需数据的script标签以以下内容开头，其他忽略
                                if (data.startsWith("'use strict';var globalInitData")) {
                                    for (int i = 0; i < pageCount; i++) {
                                        String url = data.substring(data.indexOf("regular") + 10, data.indexOf("original") - 3).replaceAll("\\\\", "");
                                        String imgUrl = url.replace("img-master/", "img-original/").replaceAll("_p0_master1200", "_p" + i);
                                        try {
                                            imgDownload(imgUrl, directoryPath + "/" + illustId + "_" + i, null);
                                        } catch (Exception e) {
                                            logger.error(imgUrl + "\r\n" + "下载失败");
                                            continue;
                                        }
                                    }

                                }
                            }
                        } else {
                            logger.info("作品张数异常");
                        }
                    }
                    //作品为视频
                    else if (illustType.equals("2")) {

                    }
                }
            }
        });
    }

    /**
     * @Description: 解析每日排行榜
     * @Param: [responseContent]
     * @return: void
     * @Author: kashen
     * @Date: 2019/7/9
     */
    public void parseDailyResult(String responseContent, String date) {
        JSONObject contentsJsonObject = (JSONObject) JSONObject.parse(responseContent);
        String contents = contentsJsonObject.getString("contents");
        JSONArray jsonArray = (JSONArray) JSONArray.parse(contents);
        jsonArray.forEach((Object json) -> {
            JSONObject jsonObject = (JSONObject) json;
            logger.info(jsonObject.toJSONString());
            String directoryPath = jsonObject.getString("illust_id");
            String illustType = jsonObject.getString("illust_type");
            int pageCount = jsonObject.getInteger("illust_page_count");
            String illustId = jsonObject.getString("illust_id");
            if (illustType.equals("0")) {
                makeDirectory(directoryPath, date + (Constants.IS_R18 ? "_r18" : "_common"));
                if (pageCount == 1) {
                    String imgUrl = jsonObject.getString("url").replace("c/240x480/img-master/", "img-original/").replaceAll("_p0_master1200", "_p0");

                        try {
                            imgDownload(imgUrl, directoryPath + "/" + illustId + "_" + "0", date + (Constants.IS_R18 ? "_r18" : "_common"));

                        } catch (Exception e) {
                            logger.error(imgUrl + "\r\n" + "下载失败");
                        }

                } else if (pageCount > 1) {
                    for (int i = 0; i < pageCount; i++) {
                        String imgUrl = jsonObject.getString("url").replace("c/240x480/img-master/", "img-original/").replaceAll("_p0_master1200", "_p" + i);
                        try {

                                imgDownload(imgUrl, directoryPath + "/" + illustId + "_" + i, date + (Constants.IS_R18 ? "_r18" : "_common"));


                        } catch (Exception e) {
                            logger.error(imgUrl + "\r\n" + "下载失败");
                            continue;
                        }
                    }
                } else {

                }
            } else {

            }
        });
        logger.info("搜索完成");
    }


    /**
     * 根据url将图片下载到本地
     *
     * @param url
     * @param filePathName
     */
    public void imgDownload(String url, String filePathName, String date) {
        get = new HttpGet(url);
        File storeFile = null;
        try (
                //创建client时传入一个cookieStore
                CloseableHttpClient client = HttpClients.custom().setDefaultCookieStore(cookieStore).build()
        ) {
            get.setHeader("referer", url);
            response = client.execute(get);
            if (StringUtils.isEmpty(date)) {
                storeFile = new File(Constants.IMG_DOWNLOAD_BASE_PATH + Constants.KEY_WORD + "/" + filePathName + url.substring(url.lastIndexOf(".")));
            } else {
                storeFile = new File(Constants.IMG_DOWNLOAD_BASE_PATH + date + "/" + filePathName + url.substring(url.lastIndexOf(".")));
            }
            FileOutputStream output = new FileOutputStream(storeFile);
            InputStream inputStream = response.getEntity().getContent();
            byte data[] = new byte[1024];
            int len;
            while ((len = inputStream.read(data)) != -1) {
                output.write(data, 0, len);
            }
            output.flush();
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (storeFile.length() < 60 * 1024) {
            storeFile.delete();
            imgDownload(url.replace(".jpg", ".png"), filePathName, date);
        }
    }

    /**
     * 判断是否还有下一页
     *
     * @param responseContent
     * @return
     */
    public boolean hasNextPage(String responseContent) {
        Document doc = Jsoup.parse(responseContent);
        Elements elementsZH = doc.select("a[title=继续]");
        Elements elementsJP = doc.select("a[title=次へ]");
        //可能是中文或者日文
        if (elementsZH.size() == 1 || elementsJP.size() == 1) {
            logger.info("有下一页");
            return true;
        } else {
            logger.info("没有下一页");
            logger.info("共搜索出结果：" + doc.select(".count-badge").get(0).text());
            logger.info("过滤出的结果：" + filtedCount + "件");
            return false;
        }
    }

    public String buildSearchUrl(String word, boolean isR18, int pageNum) {
        return Constants.PIXIV_SEARCH_URL + "?word=" + word + (isR18 ? "&mode=r18" : "") + "&p=" + pageNum;
    }

    public String buildDailyUrl(String date, Boolean isR18) {
        if (StringUtils.isEmpty(date)) {
            if (isR18) {
                return Constants.PIXIV_DAILY_R18_RANK_URL;
            } else {
                return Constants.PIXIV_DAILY_RANK_URL;
            }
        } else {
            if (isR18) {
                return Constants.PIXIV_DAY_RANK_R18_URL + date;
            } else {
                return Constants.PIXIV_DAY_RANK_URL + date;
            }
        }
    }

    public boolean makeDirectory(String path, String date) {
        File file;
        try {
            if (StringUtils.isEmpty(date)) {
                file = new File(Constants.IMG_DOWNLOAD_BASE_PATH + Constants.KEY_WORD + "/" + path);
                if (!file.exists()) {
                    return file.mkdirs();
                } else {
                    return false;
                }
            } else {
                file = new File(Constants.IMG_DOWNLOAD_BASE_PATH + date + "/" + path);
                if (!file.exists()) {
                    return file.mkdirs();
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}


