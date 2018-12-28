import cn.lyq.dao.UserDao;
import cn.lyq.domain.UserInfo;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.List;

public class AppTest
{

    public static void main(String[] args) {

        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext_dao.xml");
        UserDao userDao = applicationContext.getBean(UserDao.class);
//        UserInfo user = userDao.findUserByUserName("tom");
        UserInfo userInfo = userDao.findById("1");
        float f =  3.4000f;//默认double类型
        long i = 1;//默认int类型
//        Class.forName("");
        System.out.println(userInfo);
    }
    @Test
    public void test1(){
        List<String> list1 = List.of("1", "23", "44", "55");
        for (String str : list1) {
            if (str=="23"){
                System.out.println(str);
            }
            System.out.println(str);
        }
    }
}
