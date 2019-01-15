package cn.itcast.repository;

import cn.itcast.bos.domain.security.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepostory extends JpaRepository<User,Integer> {
    User findByUsername(String username);
}
