import cn.lyq.dao.UserDao;
import cn.lyq.domain.UserInfo;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AppTest
{

    public static void main(String[] args) {

        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext_dao.xml");
        UserDao userDao = applicationContext.getBean(UserDao.class);
//        UserInfo user = userDao.findUserByUserName("tom");
        UserInfo userInfo = userDao.findById("1");
        System.out.println(userInfo);
    }
}
