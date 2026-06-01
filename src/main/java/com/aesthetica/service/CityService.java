package com.aesthetica.service;

import com.google.gson.JsonObject;
import com.aesthetica.entity.City;
import com.aesthetica.util.AppUtil;
import com.aesthetica.util.HibernateUtil;
import org.hibernate.Session;

import java.util.List;

public class CityService {
    public String loadAllCities(){
        JsonObject responseObject =  new JsonObject();

        Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
        List<City> cityList = hibernateSession.createQuery("FROM City c", City.class).getResultList();
        responseObject.add("cities", AppUtil.GSON.toJsonTree(cityList));
        hibernateSession.close();

        return AppUtil.GSON.toJson(responseObject);
    }
}
