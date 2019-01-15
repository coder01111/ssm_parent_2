package cn.itcast.bos.domain.security;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "t_user")
@Data
public class User   {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer id;//主键
    @Column
    private String username;//用户名
    @Column
    private String password;//密码
    @ManyToMany(mappedBy = "users")
//    @JoinColumn()
    private List<Role> roles;

}
