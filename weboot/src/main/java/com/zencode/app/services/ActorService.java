package com.zencode.app.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import com.zencode.app.dao.ActorDao;
import com.zencode.app.dao.beans.Actor;


@Service
public class ActorService {
    private ActorDao actorDao;

    @Autowired
    public void setActorDao(ActorDao dao){
        this.actorDao = dao;
    }

    @Transactional(readOnly = true)
    public List<Actor> getActors(){
        return actorDao.getAllActors();
    }
}
