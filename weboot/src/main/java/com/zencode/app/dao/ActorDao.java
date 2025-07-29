package com.zencode.app.dao;

import com.zencode.app.dao.beans.Actor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;


@Repository
public class ActorDao {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public void setDataSource(DataSource ds){
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(ds);
    }

    public List<Actor> getAllActors(){
        String sql = "SELECT firstName, lastName from actor";
        return namedParameterJdbcTemplate.query(sql, (resultSet, rowNum) -> {
            Actor actor = new Actor();
            actor.setFirstName(resultSet.getString("firstName"));
            actor.setLastName(resultSet.getString("lastName"));
            return actor;
        });
    }
}
