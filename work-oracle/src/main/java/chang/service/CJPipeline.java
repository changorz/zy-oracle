package chang.service;

import chang.dao.PerformanceMapper;
import chang.pojo.Performance;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.List;


public class CJPipeline implements Pipeline {

    public CJPipeline(PerformanceMapper performanceMapper){
        this.performanceMapper = performanceMapper;
    }

    private PerformanceMapper performanceMapper;

    @Override
    public void process(ResultItems resultItems, Task task) {
        List<Performance> cjs = resultItems.get("cj");
        //判断获取到的数据不为空
        if(cjs != null && cjs.size() > 0) {
            performanceMapper.addPerformanceList(cjs);
        }

    }
}
