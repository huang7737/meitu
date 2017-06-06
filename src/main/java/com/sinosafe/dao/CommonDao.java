package com.sinosafe.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.session.SqlSession;

public abstract interface CommonDao
{
  public abstract void insertByMap(String paramString, Map<String, Object> paramMap);

  public abstract void insertByMaps(String paramString, List<Map<String, Object>> paramList);

  public abstract void updateByIdWithMap(String paramString1, String paramString2, Serializable paramSerializable, Map<String, Object> paramMap);

  public abstract void updateByIdsWithMap(String paramString1, String paramString2, List<Serializable> paramList, Map<String, Object> paramMap);

  public abstract void deleteById(String paramString1, String paramString2, Serializable paramSerializable);

  public abstract void deleteByIds(String paramString1, String paramString2, List<Serializable> paramList);

  public abstract Map<String, Object> selectMapById(String paramString1, String paramString2, Serializable paramSerializable);

  public abstract List<Map<String, Object>> selectMapByIds(String paramString1, String paramString2, List<Serializable> paramList);

  public abstract void insert(String paramString, Object paramObject);

  public abstract void update(String paramString, Object paramObject);

  public abstract void delete(String paramString, Object paramObject);

  public abstract <T> T selectOne(String paramString, Object paramObject);

  public abstract <T> List<T> selectList(String paramString, Object paramObject);

  public abstract SqlSession getSqlSession();

}
