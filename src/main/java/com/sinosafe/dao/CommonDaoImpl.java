package com.sinosafe.dao;

import com.alibaba.fastjson.JSONObject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mybatis.spring.support.SqlSessionDaoSupport;

public class CommonDaoImpl extends SqlSessionDaoSupport
  implements CommonDao
{
  private String nameSpace;

  public CommonDaoImpl()
  {
    this.nameSpace = ""; }

  public String getNameSpace() {
    return this.nameSpace;
  }

  public void setNameSpace(String nameSpace) {
    this.nameSpace = nameSpace;
  }

  public void insertByMap(String tableName, Map<String, Object> dataMap)
  {
    CommonDaoUtil.checkTableName(tableName);
    CommonDaoUtil.checkDataMap(dataMap);
    Map map = new HashMap();
    map.put("tableName", tableName);
    String[] strArray = CommonDaoUtil.parseMapByInsert(dataMap);
    map.put("column", strArray[0]);
    map.put("valueSql", strArray[1]);
    try {
      getSqlSession().insert(this.nameSpace + ".insertByMap", map);
    } catch (Exception e) {
      throw new RuntimeException("insertByMap方法参数\ntableName:" + tableName + "\ndataMap:" + dataMap + "\n提交SQL参数:" + map, e);
    }
  }

  public void insertByMaps(String tableName, List<Map<String, Object>> dataMaps)
  {
    CommonDaoUtil.checkTableName(tableName);
    CommonDaoUtil.checkDataMapList(dataMaps);
    for (Map map : dataMaps)
      insertByMap(tableName, map);
  }

  public void updateByIdWithMap(String tableName, String idName, Serializable idValue, Map<String, Object> dataMap)
  {
    CommonDaoUtil.checkTableName(tableName);
    CommonDaoUtil.checkTableName(tableName);
    CommonDaoUtil.checkIdName(idName);
    CommonDaoUtil.checkIdValue(idValue);
    CommonDaoUtil.checkDataMap(dataMap);
    Map map = new HashMap();
    map.put("tableName", tableName);
    map.put("idName", idName);
    map.put("idValue", idValue);
    map.put("setColumn", CommonDaoUtil.parseMapByUpdate(dataMap));
    try {
      getSqlSession().update(this.nameSpace + ".updateByIdWithMap", map);
    } catch (Exception e) {
      throw new RuntimeException("updateByIdWithMap方法参数\ntableName:" + tableName + "\nidName:" + idName + "\nidValue:" + idValue + "\ndataMap" + dataMap + "\n提交SQL参数:" + map, e);
    }
  }

  public void updateByIdsWithMap(String tableName, String idName, List<Serializable> idValues, Map<String, Object> dataMap)
  {
    CommonDaoUtil.checkTableName(tableName);
    CommonDaoUtil.checkIdName(idName);
    CommonDaoUtil.checkIdValues(idValues);
    CommonDaoUtil.checkDataMap(dataMap);
    Map map = new HashMap();
    map.put("tableName", tableName);
    map.put("idName", idName);
    map.put("idList", idValues);
    map.put("setColumn", CommonDaoUtil.parseMapByUpdate(dataMap));
    try {
      getSqlSession().update(this.nameSpace + ".updateByIdsWithMap", map);
    } catch (Exception e) {
      throw new RuntimeException("updateByIdsWithMap方法参数\ntableName:" + tableName + "\nidName" + idName + "\nidValues:" + idValues + "\ndataMap:" + dataMap + "\n提交sql参数:" + map, e);
    }
  }

  public void deleteById(String tableName, String idName, Serializable idValue)
  {
    CommonDaoUtil.checkTableName(tableName);
    CommonDaoUtil.checkIdValue(idValue);
    CommonDaoUtil.checkIdName(idName);
    Map map = new HashMap();
    map.put("tableName", tableName);
    map.put("idName", idName);
    map.put("idValue", idValue);
    try {
      getSqlSession().delete(this.nameSpace + ".deleteById", map);
    } catch (Exception e) {
      throw new RuntimeException("deleteById方法参数\ntableName:" + tableName + "\nidName" + idName + "\nidValue:" + idValue + "\n提交sql参数:" + map, e);
    }
  }

  public void deleteByIds(String tableName, String idName, List<Serializable> idValues)
  {
    CommonDaoUtil.checkIdValues(idValues);
    CommonDaoUtil.checkIdName(idName);
    CommonDaoUtil.checkIdValues(idValues);
    Map map = new HashMap();
    map.put("tableName", tableName);
    map.put("idName", idName);
    map.put("idList", idValues);
    try {
      getSqlSession().delete(this.nameSpace + ".deleteByIds", map);
    } catch (Exception e) {
      throw new RuntimeException("deleteByIds方法参数\ntableName:" + tableName + "\nidName" + idName + "\nidValues:" + idValues + "\n提交sql参数:" + map, e);
    }
  }

  public Map<String, Object> selectMapById(String tableName, String idName, Serializable idValue)
  {
    Map resultMap = new HashMap();
    CommonDaoUtil.checkIdName(idName);
    CommonDaoUtil.checkIdValue(idValue);
    CommonDaoUtil.checkTableName(tableName);
    Map map = new HashMap();
    map.put("tableName", tableName);
    map.put("idName", idName);
    map.put("idValue", idValue);
    try {
      List result = getSqlSession().selectList(this.nameSpace + ".selectMapById", map);

      if ((result != null) && (result.size() > 0))
        return ((Map)result.get(0));
    }
    catch (Exception e) {
      throw new RuntimeException("selectMapById方法参数\ntableName:" + tableName + "\nidName" + idName + "\nidValue:" + idValue + "\n提交sql参数:" + map, e);
    }

    return resultMap;
  }

  public void insert(String sqlId, Object data)
  {
    if (data != null)
      try {
        getSqlSession().insert(sqlId, data);
      }
      catch (Exception e)
      {
        throw new RuntimeException("insert方法参数\nsqlId:" + sqlId + "\n提交sql参数:" + 
          JSONObject.toJSONString(data)
          .toString(), e);
      }
    else
      try {
        getSqlSession().insert(sqlId);
      } catch (Exception e) {
        throw new RuntimeException("insert方法参数\nsqlId:" + sqlId + "\n提交sql参数:null", e);
      }
  }

  public void update(String sqlId, Object data)
  {
    if (data != null)
      try {
        getSqlSession().update(sqlId, data);
      }
      catch (Exception e)
      {
        throw new RuntimeException("update方法参数\nsqlId:" + sqlId + "\n提交sql参数:" + 
          JSONObject.toJSONString(data)
          .toString(), e);
      }
    else
      try {
        getSqlSession().update(sqlId, data);
      } catch (Exception e) {
        throw new RuntimeException("update方法参数\nsqlId:" + sqlId + "\n提交sql参数:null", e);
      }
  }

  public void delete(String sqlId, Object data)
  {
    if (data != null)
      try {
        getSqlSession().delete(sqlId, data);
      }
      catch (Exception e)
      {
        throw new RuntimeException("delete方法参数\nsqlId:" + sqlId + "\n提交sql参数:" + 
          JSONObject.toJSONString(data)
          .toString(), e);
      }
    else
      try {
        getSqlSession().delete(sqlId);
      } catch (Exception e) {
        throw new RuntimeException("delete方法参数\nsqlId:" + sqlId + "\n提交sql参数:null", e);
      }
  }

  public <T> T selectOne(String sqlId, Object whereData)
  {
    if (whereData != null)
      try {
        return getSqlSession().selectOne(sqlId, whereData);
      }
      catch (Exception e)
      {
        throw new RuntimeException("selectOne方法参数\nsqlId:" + sqlId + "\n提交sql参数:" + 
          JSONObject.toJSONString(whereData)
          .toString(), e);
      }
    try
    {
      return getSqlSession().selectOne(sqlId);
    } catch (Exception e) {
      throw new RuntimeException("selectOne方法参数\nsqlId:" + sqlId + "\n提交sql参数:null", e);
    }
  }

  public <T> List<T> selectList(String sqlId, Object whereData)
  {
    if (whereData != null)
      try {
        return getSqlSession().selectList(sqlId, whereData);
      }
      catch (Exception e)
      {
        throw new RuntimeException("selectList方法参数\nsqlId:" + sqlId + "\n提交sql参数:" + 
          JSONObject.toJSONString(whereData)
          .toString(), e);
      }
    try
    {
      return getSqlSession().selectList(sqlId);
    } catch (Exception e) {
      throw new RuntimeException("selectList方法参数\nsqlId:" + sqlId + "\n提交sql参数:null", e);
    }
  }

  public List<Map<String, Object>> selectMapByIds(String tableName, String idName, List<Serializable> idValues)
  {
    CommonDaoUtil.checkTableName(tableName);
    CommonDaoUtil.checkIdName(idName);
    CommonDaoUtil.checkIdValues(idValues);
    Map map = new HashMap();
    map.put("tableName", tableName);
    map.put("idName", idName);
    map.put("idList", idValues);
    try {
      List result = getSqlSession().selectList(this.nameSpace + ".selectMapByIds", map);

      if ((result != null) && (result.size() > 0)) {
        return result;
      }
      return new ArrayList();
    } catch (Exception e) {
      throw new RuntimeException("selectMapByIds方法参数\ntableName:" + tableName + "\nidName:" + idName + "\nidValues:" + idValues + "\n提交sql参数:null", e);
    }
  }

  private void closeResouce(ResultSet rs, Connection conn, Statement[] stm)
  {
    if (rs != null) {
      try {
        rs.close();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
    if (conn != null) {
      try {
        conn.close();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
    for (int i = 0; i < stm.length; ++i) {
      if (stm[i] == null) continue;
      try {
        stm[i].close();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
  }

}