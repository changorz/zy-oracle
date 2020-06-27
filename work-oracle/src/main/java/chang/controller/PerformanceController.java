package chang.controller;

import chang.dao.PerformanceMapper;
import chang.service.CJPageProcessor;
import chang.service.CJPipeline;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import us.codecraft.webmagic.Spider;

@RestController
@Api
public class PerformanceController {

    @Autowired
    private PerformanceMapper performanceMapper;

    @GetMapping("/getPerformanceCount")
    public int getUserCount(){
        return performanceMapper.getPerformanceCount();
    }

    @GetMapping("/updataPerformance/{xh}/{pwd}")
    public String updataUser(@PathVariable String xh, @PathVariable String pwd){
        Spider.create(new CJPageProcessor())
                .addUrl("http://zswxyjw.minghuaetc.com/znlykjdxswxy/app.do?method=authUser&xh="+xh+"&pwd="+pwd)
                .addPipeline(new CJPipeline(performanceMapper))
                .thread(100)
                .run();
        return "正在爬取成绩!";
    }

}
