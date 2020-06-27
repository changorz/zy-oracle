package chang.service;

import chang.pojo.Performance;
import chang.pojo.User;
import com.alibaba.fastjson.JSON;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CJPageProcessor implements PageProcessor {

    private String TOKEN;
    private int start = 20150000;
    private int end = 20199999;
//    private static int start = 20177583;
//    private static int end = 20177583;
    private static String arr[] = new String[]{
            "2015-2016-1",
            "2015-2016-2",
            "2016-2017-1",
            "2016-2017-2",
            "2017-2018-1",
            "2017-2018-2",
            "2018-2019-1",
            "2018-2019-2",
            "2019-2020-1"
    };

    @Override
    public void process(Page page) {
        if(JSON.parseObject(page.getJson().toString()).get("token")!=null&&!"-1".equals(JSON.parseObject(page.getJson().toString()).get("token"))){
            this.TOKEN = JSON.parseObject(page.getJson().toString()).get("token").toString();
            // 添加爬取的学号
            for (int i = start; i <= end ; i++) {
                if (i%10000 >= 6000) {
                    for (int j = 0; j < arr.length; j++) {
                        page.addTargetRequest(
                                new Request("http://zswxyjw.minghuaetc.com/znlykjdxswxy/app.do?method=getCjcx&xnxqid=" + arr[j] + "&xh=" + i).addHeader("token", TOKEN)
                        );
                    }
                }
            }
        }else{
            if(JSON.parseObject(page.getJson().toString()).get("result")==null ||
                    JSON.parseObject(page.getJson().toString()).get("result")==null||
                    "[]".equals(JSON.parseObject(page.getJson().toString()).get("result").toString())){
                return;
            }
            String url = page.getUrl().toString();
            String xh = url.substring(url.lastIndexOf("=") + 1);
            List<Performance> result = JSON.parseArray(JSON.parseObject(page.getJson().toString()).get("result").toString(), Performance.class);
            result.forEach(e -> {
                e.setXh(xh);
            });
            page.putField("cj", result);
        }
    }

    private Site site = Site.me()
            // 编码
            .setCharset("UTF-8")
            // 抓取间隔时间
            .setSleepTime(1)
            // 超时时间
            .setTimeOut(1000*10)
            // 重试时间
            .setRetrySleepTime(3000)
            // 重试次数
            .setRetryTimes(3);

    @Override
    public Site getSite() {
        return site;
    }


}
