package steparrik.code.payticketsbot.service.globalVar;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class GlobalVar {
    private static GlobalVar instance;
    private GlobalVar() {}
    public static synchronized GlobalVar getInstance(){
        if(instance==null){
            instance = new GlobalVar();
        }
        return instance;
    }

     int globalPrice = 10000;
     String globalPassword="1";
}
