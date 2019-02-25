package com.lzg.extend.jackson;

import android.annotation.SuppressLint;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON转换工具类
 */
public class JsonUtil {
    private static ObjectMapper mapper;
    private static JsonGenerator jsonGenerator;

    private static final JsonFactory JSONFACTORY = new JsonFactory();
    private static SimpleFilterProvider filter;

    /**
     * 获取ObjectMapper实例
     */
    public static synchronized JsonGenerator getJsonGenerator() {
        try {
            jsonGenerator = getMapperInstance().getFactory().createGenerator(System.out, JsonEncoding.UTF8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonGenerator;
    }

    /**
     * 获取ObjectMapper实例
     */
    public static synchronized JsonGenerator getResponseJsonGenerator() {
        try {
            jsonGenerator = getMapperInstance().getFactory().createGenerator(System.out, JsonEncoding.UTF8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonGenerator;
    }

    public static void setFilter(String filterName, String... property) {
        filter = new SimpleFilterProvider().setFailOnUnknownId(false);
        filter.addFilter(filterName, SimpleBeanPropertyFilter.serializeAllExcept(property));
    }

    /**
     * 获取ObjectMapper实例 Inclusion Inclusion.ALWAYS 全部列入 Inclusion
     * Inclusion.NON_DEFAULT 字段和对象默认值相同的时候不会列入 Inclusion Inclusion.NON_EMPTY
     * 字段为NULL或者""的时候不会列入 Inclusion Inclusion.NON_NULL 字段为NULL时候不会列入
     */
    @SuppressLint("SimpleDateFormat")
    public static synchronized ObjectMapper getMapperInstance() {
        if (mapper == null) {
            mapper = new ObjectMapper();
            mapper.getSerializerProvider().setNullValueSerializer(new JsonSerializer<Object>() {

                @Override
                public void serialize(Object o,
                                      JsonGenerator jsonGenerator,
                                      SerializerProvider serializerProvider) throws IOException {
                    jsonGenerator.writeString("");
                }

            });

            SimpleModule module = new SimpleModule();
            module.addSerializer(String.class, new StringUnicodeSerializer()); //使Jackson支持Unicode编码非ASCII字符
            mapper.registerModule(module);

            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); //当找不到对应的序列化器时 忽略此字段
            mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
            mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
            mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);  //忽略不存在的属性
            mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true); //允许空字符串转换为空数组
            mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);          //允许空字符串转换为空对象
            mapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);           //允许空数组转换为空对象
            mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));                  //所有日期格式都统一为以下样式
            mapper.setSerializationInclusion(Include.ALWAYS);                                  //设置null值不参与序列化(字段不被显示)
            // 设置输入时忽略在JSON字符串中存在但Java对象实际没有的属性
            // mapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            // 禁止使用int代表Enum的order()來反序列化Enum,非常危险
            // mapper.configure(Feature.FAIL_ON_NUMBERS_FOR_ENUMS, true);
            // 设置输出时包含属性的风格
        }
        return mapper;
    }

    /**
     * 将java对象转换成json字符串
     */
    public static String beanToJson(Object obj) {
        ObjectMapper objectMapper = getMapperInstance();
        String json = "";
        try {
            json = objectMapper.writeValueAsString(obj);
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }

    /**
     * 将json字符串转换成java对象
     *
     * @param json 准备转换的json字符串
     * @param cls  准备转换的类
     */
    public static <T> T jsonToBean(String json, Class<T> cls) {

        ObjectMapper objectMapper = getMapperInstance();
        T object = null;
        try {
            object = objectMapper.readValue(json, cls);
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return object;
    }

    /**
     * 将json字符串转换成java对象
     *
     * @param json         准备转换的json字符串
     * @param valueTypeRef 引用类型
     */
    public static <T> T jsonToBean(String json, TypeReference valueTypeRef) {

        ObjectMapper objectMapper = getMapperInstance();
        T object = null;
        try {
            object = objectMapper.readValue(json, valueTypeRef);
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return object;
    }

    @SuppressWarnings("deprecation")
    public static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
        ObjectMapper objectMapper = getMapperInstance();
        return objectMapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }

    public static <T> List<T> toList(String json, Class<?> class1) {
        JavaType javaType = getCollectionType(ArrayList.class, class1);
        ObjectMapper objectMapper = getMapperInstance();
        List<T> beanList = new ArrayList<>();
        try {
            beanList = objectMapper.readValue(json, javaType);
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return beanList;

    }

    /**
     * 转换Json String 为 HashMap
     */
    public static Map<String, Object> jsonToMap(String json, boolean collToString) {
        try {
            Map<String, Object> map = getMapperInstance().readValue(json, HashMap.class);
            if (collToString) {
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    if (entry.getValue() instanceof Collection || entry.getValue() instanceof Map) {
                        entry.setValue(beanToJson(entry.getValue()));
                    }
                }
            }
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * List 转换成json
     */
    public static String listToJson(Collection<?> list) {
        JsonGenerator jsonGenerator = null;
        StringWriter sw = new StringWriter();
        try {
            jsonGenerator = getJsonGenerator();
            if (filter != null) {
                getMapperInstance().writer(filter).writeValue(sw, list);
            } else {
                getMapperInstance().writeValue(sw, list);
            }
            jsonGenerator.flush();
            return sw.toString();
        } catch (Exception e) {
            return null;
        } finally {
            if (jsonGenerator != null) {
                try {
                    jsonGenerator.flush();
                    jsonGenerator.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 转换Java Bean 为 HashMap
     */
    public static Map<String, Object> beanToMap(Object o) {
        try {
            return getMapperInstance().readValue(beanToJson(o), HashMap.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * json 转List
     */
    public static List<?> jsonToList(String json) {
        ObjectMapper objectMapper = getMapperInstance();
        try {
            if (json != null && !"".equals(json.trim())) {
                JsonParser jsonParse = JSONFACTORY.createParser(new StringReader(json));
                return (ArrayList<Map<String, String>>) objectMapper.readValue(jsonParse, ArrayList.class);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
