package chang.controller;

import chang.dao.UserMapper;
import chang.service.UserPageProcessor;
import chang.service.UserPipeline;
import chang.vo.UserPerformance;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import us.codecraft.webmagic.Spider;

@RestController
@Api
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/getUserCount")
    public int getUserCount(){
        return userMapper.getUserCount();
    }

    @GetMapping("/updataUser/{xh}/{pwd}")
    public String updataUser(@PathVariable String xh,@PathVariable String pwd){
        Spider.create(new UserPageProcessor())
                .addUrl("http://zswxyjw.minghuaetc.com/znlykjdxswxy/app.do?method=authUser&xh="+xh+"&pwd="+pwd)
                .addPipeline(new UserPipeline(userMapper))
                .thread(100)
                .run();
        return "正在爬取数据!";
    }

    @GetMapping("/findUserPerformance/{xh}")
    public UserPerformance findUserPerformanceByXh(@PathVariable String xh){
        return userMapper.findUserPerformanceByXh(xh);
    }

}
