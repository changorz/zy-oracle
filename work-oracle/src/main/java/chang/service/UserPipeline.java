package chang.service;

import chang.dao.UserMapper;
import chang.pojo.User;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;


public class UserPipeline implements Pipeline {

    public UserPipeline(UserMapper userMapper){
        this.userMapper = userMapper;
    }

    private UserMapper userMapper;

    @Override
    public void process(ResultItems resultItems, Task task) {
        User user = resultItems.get("user");
        //判断获取到的数据不为空
        if(user != null && user.getXh() != null) {
            //如果有值则进行保存
            System.out.println(user);
            userMapper.addUser(user);
        }

    }
}
