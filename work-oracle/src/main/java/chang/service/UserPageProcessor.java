package chang.service;

import chang.pojo.User;
import com.alibaba.fastjson.JSON;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

public class UserPageProcessor implements PageProcessor {

    private String TOKEN;
    private int start = 20150000;
    private int end = 20199999;

    @Override
    public void process(Page page) {
        if(JSON.parseObject(page.getJson().toString()).get("token")!=null&&!"-1".equals(JSON.parseObject(page.getJson().toString()).get("token"))){
            this.TOKEN = JSON.parseObject(page.getJson().toString()).get("token").toString();
            // 添加爬取的学号
            for (int i = start; i <= end ; i++) {
                if (i%10000 >= 6000) {
                    page.addTargetRequest(
                            new Request("http://zswxyjw.minghuaetc.com/znlykjdxswxy/app.do?method=getUserInfo&xh=" + i).addHeader("token", TOKEN)
                    );
                }
            }
        }else{
            page.putField("user", JSON.parseObject(page.getJson().toString(), User.class));
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
