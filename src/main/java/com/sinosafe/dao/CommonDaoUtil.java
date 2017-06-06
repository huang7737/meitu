package com.sinosafe.dao;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

abstract class CommonDaoUtil
{
  private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

  private static SimpleDateFormat dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  public static void setSdf(SimpleDateFormat sdf1)
  {
    sdf = sdf1;
  }

  public static String[] parseMapByInsert(Map<String, Object> dataMap)
  {
    String[] result = new String[2];
    StringBuffer setSql = new StringBuffer();
    StringBuffer valSql = new StringBuffer();

    Iterator dataIt = dataMap.entrySet()
      .iterator();
    Map.Entry me = null;
    while (dataIt.hasNext()) {
      me = (Map.Entry)dataIt.next();
      setSql.append(((String)me.getKey()).toString()).append(",");
      valSql.append(parseType(me.getValue())).append(",");
    }
    result[0] = setSql.substring(0, setSql.length() - 1);
    result[1] = valSql.substring(0, valSql.length() - 1);
    return result;
  }

  public static String parseType(Object value)
  {
    if (value == null) {
      return null;
    }
    if (value instanceof String) {
      return "'" + value.toString() + "'";
    }
    if (value instanceof Integer) {
      return value + "";
    }
    if (value instanceof Long) {
      return value + "";
    }
    if (value instanceof Double) {
      return value + "";
    }
    if (value instanceof Float) {
      return value + "";
    }
    if (value instanceof Timestamp) {
      return "TO_DATE('" + dateTime.format(value).toString() + "','yyyy-mm-dd hh24:mi:ss')";
    }
    if (value instanceof java.sql.Date) {
      return "TO_DATE('" + sdf.format(value).toString() + "','yyyy-mm-dd')";
    }
    if (value instanceof java.util.Date) {
      return "TO_DATE('" + sdf.format(value).toString() + "','yyyy-mm-dd')";
    }

    throw new RuntimeException("匹配不到规定的数据格式 ==> valueType=" + value
      .getClass());
  }

  public static void checkTableName(String tableName)
  {
    if ((tableName == null) || (tableName.trim().equals("")))
      throw new RuntimeException("缺少对应的数据表名称 ==> tableName=" + tableName);
  }

  public static void checkDataMap(Map<String, Object> dataMap)
  {
    if ((dataMap == null) || (dataMap.size() == 0))
      throw new RuntimeException("需要添加的数据参数不能为空或没有任何内容 ==> dataMap=" + dataMap);
  }

  public static void checkIdName(String idName)
  {
    if ((idName == null) || (idName.trim().equals("")))
      throw new RuntimeException("ID字段名为空或null ==> idName=" + idName);
  }

  public static void checkIdValue(Serializable idValue)
  {
    if (idValue == null)
      throw new RuntimeException("ID字段值为null ==> idValue=" + idValue);
  }

  public static void checkIdValues(List<Serializable> ids)
  {
    if ((ids == null) || (ids.size() == 0))
      throw new RuntimeException("ID集合为空或为null ==> ids=" + ids);
  }

  public static void checkDataMapList(List<Map<String, Object>> dataMaps)
  {
    if ((dataMaps == null) || (dataMaps.size() == 0))
      throw new RuntimeException("要添加的数据集合为空或为null ==> dataMaps=" + dataMaps);
  }

  public static String parseMapByUpdate(Map<String, Object> dataMap)
  {
    StringBuffer sql = new StringBuffer();

    Iterator mapIt = dataMap.entrySet()
      .iterator();
    Map.Entry me = null;
    while (mapIt.hasNext()) {
      me = (Map.Entry)mapIt.next();
      sql.append((String)me.getKey()).append("=")
        .append(parseType(me
        .getValue())).append(",");
    }
    String result = sql.substring(0, sql.length() - 1);
    return result;
  }

  public static void checkMap(Map<String, String> list) {
    if ((list == null) || (list.size() == 0))
      throw new RuntimeException("要添加的数据集合为空或为null ==> clobMap=" + list);
  }

  public static List<String>[] parseMapByClob(Map<String, String> clobs)
  {
    List[] result = new List[2];
    List data = new ArrayList();
    List column = new ArrayList();

    Iterator dataIt = clobs.entrySet()
      .iterator();
    Map.Entry me = null;
    while (dataIt.hasNext()) {
      me = (Map.Entry)dataIt.next();
      column.add(me.getKey());
      data.add(me.getValue());
    }
    result[0] = column;
    result[1] = data;
    return result;
  }

  public static Map<String, Object> parseMap(Map<String, Object> data)
  {
    Map result = new HashMap();
    StringBuffer setSql = new StringBuffer();
    List valList = new ArrayList();
    Iterator dataIt = data.entrySet().iterator();
    Map.Entry me = null;
    while (dataIt.hasNext()) {
      me = (Map.Entry)dataIt.next();
      setSql.append(((String)me.getKey()).toString()).append(",");
      valList.add(me.getValue());
    }
    result.put("column", setSql.substring(0, setSql.length() - 1));
    result.put("valList", valList);
    return result;
  }
}
