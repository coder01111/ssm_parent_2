package cn.itcast.bos.domain.security;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "t_role")
@Data
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer id;//主键
    @Column
    private  String rolename;//用户名称
    @Column
    private  String  rolename_zh;//中文名
    //多对多关系映射,配置级联操作
    @ManyToMany(cascade = CascadeType.ALL)
    //建立中间表关系
    @JoinTable(name="user_role",//中间表的名称
            //中间表user_role_rel字段关联sys_role表的主键字段role_id
            joinColumns={@JoinColumn(name="role_id",referencedColumnName="role_id")},
            //中间表user_role_rel的字段关联sys_user表的主键user_id
            inverseJoinColumns={@JoinColumn(name="user_id",referencedColumnName="user_id")}
    )
    private List<User> users;
}
