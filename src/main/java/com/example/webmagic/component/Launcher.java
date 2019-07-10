package com.example.webmagic.component;

import com.example.webmagic.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * @program: webmagic
 * @description: 启动方法
 * @author: kashen
 * @create: 2019-07-10 14:23
 **/
@Component
public class Launcher {
    @Autowired
    private PixivCilent pixivCilent;

    SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");

    public void launcher() {
        boolean isContinue = true;
        Scanner scanner = new Scanner(System.in);
        if(!pixivCilent.login()){
            System.out.println("请检查配置后重启");
            return;
        }
        while (isContinue) {
            System.out.println("是否开启R18选项（Y/N）");
            String isR18 = scanner.next();
            switch (isR18.toLowerCase()) {
                case "y":
                    Constants.IS_R18 = true;
                    break;
                case "n":
                    Constants.IS_R18 = true;
                    break;
                default:
                    System.out.println("请输入正确的选项");
                    continue;
            }
            dailyOrSearch:while (isContinue) {
                System.out.println("1.关键词搜索");
                System.out.println("2.每日排行榜");
                String dailyOrSearch = scanner.next();
                switch (dailyOrSearch) {
                    case "1":
                        System.out.println("输入你要搜索的关键词");
                        String keyWord = scanner.next();
                        Constants.KEY_WORD=keyWord;
                        pixivCilent.search();
                        break;
                    case "2":
                        System.out.println("输入你要搜索的日期，输入1默认昨天 (格式yyyyMMdd)");
                        String date = scanner.next();
                        if (date.equals("1")){
                            pixivCilent.daily(null);
                        }else {
                            String pattern="(?<!\\d)(?:(?:20\\d{2})(?:(?:(?:0[13578]|1[02])31)|(?:(?:0[1,3-9]|1[0-2])(?:29|30)))|(?:(?:20(?:0[48]|[2468][048]|[13579][26]))0229)|(?:20\\d{2})(?:(?:0?[1-9])|(?:1[0-2]))(?:0?[1-9]|1\\d|2[0-8]))(?!\\d)";
                            if(Pattern.matches(pattern,date)){
                                if(Integer.valueOf(date)>20070910&&Integer.valueOf(date)<Integer.valueOf(sdf.format(new Date()))){
                                    pixivCilent.daily(date);
                                }else {
                                    System.out.println("请输入正确的日期");
                                    continue dailyOrSearch;
                                }
                            }else {
                                System.out.println("请输入正确的格式");
                                continue dailyOrSearch;
                            }
                        }
                        break;
                    default:
                        System.out.println("请输入正确的选项");
                        continue dailyOrSearch;
                }
            }
        }
    }
}

