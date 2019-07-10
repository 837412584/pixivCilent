package com.example.webmagic.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @program: webmagic
 * @description:
 * @author: kashen
 * @create: 2019-07-04 16:09
 **/
@Component
public class Constants {

    /**
     * P站预登陆url
     */
    public static  String PIXIV_BASE_URL;

    /**
     * P站登录请求url
     */
    public static  String PIXIV_LOGIN_URL;

    /**
     * P站搜索请求url
     */
    public static  String PIXIV_SEARCH_URL;

    /**
     * P站单图详情页url
     */
    public static  String PIXIV_ILLUST_MEDIUM_URL;

    /**
     * P站多图详情页url
     */
    public static  String PIXIV_ILLUST_MANGA_URL;
    /**
     * P站今日排行榜json数据url
     */
    public static  String PIXIV_DAILY_RANK_URL;
    /**
     * P站每今日排行榜json数据url
     */
    public static  String PIXIV_DAILY_R18_RANK_URL;
    /**
     * P站指定日期排行榜json数据url
     */
    public static  String PIXIV_DAY_RANK_URL;
    /**
     * P站指定日期排行榜json数据url
     */
    public static  String PIXIV_DAY_RANK_R18_URL;
    /**
     * 图片本地保存根目录
     */
    public static  String IMG_DOWNLOAD_BASE_PATH;

    /**
     * 用户名
     */
    public static  String USERNAME;

    /**
     * 密码
     */
    public static  String PASSWORD;

    /**
     * 搜索关键词
     */
    public static String KEY_WORD;

    /**
     * 是否只搜索r18结果
     */
    public static boolean IS_R18;

    /**
     * 点赞数（不低于）
     */
    public static int STARS;

    @Value("${PIXIV_BASE_URL}")
    String pixivBaseUrl;
    @Value("${PIXIV_LOGIN_URL}")
    String pixivLoginUrl;
    @Value("${PIXIV_SEARCH_URL}")
    String pixivSearchUrl;
    @Value("${PIXIV_ILLUST_MEDIUM_URL}")
    String pixivIllustMediumUrl;
    @Value("${PIXIV_ILLUST_MANGA_URL}")
    String pixivIllustMangaUrl;
    @Value("${PIXIV_DAILY_RANK_URL}")
    String pixivDailyRankUrl;
    @Value("${PIXIV_DAILY_R18_RANK_URL}")
    String pixivDailyR18RankUrl;
    @Value("${PIXIV_DAY_RANK_URL}")
    String pixivDayRankUrl;
    @Value("${PIXIV_DAY_RANK_R18_URL}")
    String pixivDayRankR18Url;
    @Value("${IMG_DOWNLOAD_BASE_PATH}")
    String imgDownloadBasePath;
    @Value("${PIXIV_USERNAME}")
    String username;
    @Value("${PIXIV_PASSWORD}")
    String password;
    @Value("${IS_R18}")
    String isR18;
    @Value("${STARS}")
    String starts;

    @PostConstruct
    public void init(){
        Constants.PIXIV_BASE_URL=pixivBaseUrl;
        Constants.PIXIV_LOGIN_URL=pixivLoginUrl;
        Constants.PIXIV_SEARCH_URL=pixivSearchUrl;
        Constants.PIXIV_ILLUST_MEDIUM_URL=pixivIllustMediumUrl;
        Constants.PIXIV_ILLUST_MANGA_URL=pixivIllustMangaUrl;
        Constants.PIXIV_DAILY_RANK_URL=pixivDailyRankUrl;
        Constants.PIXIV_DAILY_R18_RANK_URL=pixivDailyR18RankUrl;
        Constants.PIXIV_DAY_RANK_URL=pixivDayRankUrl;
        Constants.PIXIV_DAY_RANK_R18_URL=pixivDayRankR18Url;
        Constants.IMG_DOWNLOAD_BASE_PATH=imgDownloadBasePath;
        Constants.USERNAME=username;
        Constants.PASSWORD=password;
        Constants.IS_R18=Boolean.valueOf(isR18);
        Constants.STARS=Integer.parseInt(starts );
    }
}


