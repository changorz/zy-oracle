package chang.vo;


import chang.pojo.Performance;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class UserPerformance implements Serializable {

    private String fxzy;
    private String xh;
    private String xm;
    private String dqszj;
    private String usertype;
    private String yxmc;
    private String xz;
    private String bj;
    private String dh;
    private String email;
    private String rxnf;
    private String xb;
    private String ksh;
    private String nj;
    private String qq;
    private String zymc;
    private List<Performance> list;

}
