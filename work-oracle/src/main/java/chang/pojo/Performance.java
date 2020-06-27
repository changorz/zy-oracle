package chang.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Performance implements Serializable {

    private String xh;
    private String bz;
    private String cjbsmc;
    private String kclbmc;
    private String zcj;
    private String xm;
    private String xqmc;
    private String kcxzmc;
    private String kcywmc;
    private String ksxzmc;
    private String kcmc;
    private String xf;

}
