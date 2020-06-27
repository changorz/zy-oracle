package chang.dao;

import chang.pojo.User;
import chang.vo.UserPerformance;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface UserMapper {

    int getUserCount();

    void addUser(User user);

    UserPerformance findUserPerformanceByXh(String xh);

}
