package chang.dao;

import chang.pojo.Performance;
import chang.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface PerformanceMapper {

    int getPerformanceCount();

    void addPerformance(Performance performance);

    void addPerformanceList(List<Performance> performances);

}
