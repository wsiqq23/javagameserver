package ${class[0..(class?last_index_of(".") - 1)]};

import org.bson.Document;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import pers.winter.framework.db.AbstractBaseEntity;
import pers.winter.framework.db.AnnTable;
import pers.winter.framework.db.Constants;
<#if daoClass??>
import ${daoClass};
</#if>
<#list imports as import>
import ${import};
</#list>

<#if comment??>
/**
 * ${comment}
 * @author CodeGenerator
 */
 </#if>
@AnnTable(key = "${key}", dbType = Constants.DBType.${dbType?upper_case}, <#if daoClass??>daoClass = ${daoClass?split(".")?last}.class,</#if> cacheType = Constants.CacheType.${cacheType?upper_case},userCache = ${userCache})
public class ${class?split(".")?last} extends AbstractBaseEntity {
<#list fields as field>
    private ${field.briefType} ${field.name};
</#list>

<#list fields as field>
    <#if field.comment!="">
    /**
     * ${field.comment}
     */
    </#if>
    public ${field.briefType} get${field.name?cap_first}(){
        return ${field.name};
    }
    <#if field.comment!="">
    /**
     * ${field.comment}
     */
    </#if>
    public void set${field.name?cap_first}(${field.briefType} ${field.name}){
        this.${field.name} = ${field.name};
    }
</#list>

    @Override
    public String getKey() {
        return "${key}";
    }

    @Override
    public long getKeyID() {
        return get${key?cap_first}();
    }

    @Override
    public void fromDocument(Document document) {
<#if dbType?upper_case == "MONGO">
        this.setId((long) document.get("id"));
        this.setEntityVersion((int) document.get("entityVersion"));
    <#list fields as field>
        <#if field.briefType=="byte" || field.briefType=="short">
        this.set${field.name?cap_first}((${field.briefType}) (int) document.get("${field.name}"));
        <#elseif field.briefType=="float">
        this.set${field.name?cap_first}((${field.briefType}) (double) document.get("${field.name}"));
        <#elseif field.briefType=="boolean" || field.briefType=="int" || field.briefType=="long" || field.briefType=="String" || field.briefType=="double">
        this.set${field.name?cap_first}((${field.briefType}) document.get("${field.name}"));
        <#elseif field.list>
        this.set${field.name?cap_first}(MongoCodecUtil.getList(document,"${field.name}",${field.valueClass}.class));
        <#elseif field.map>
        this.set${field.name?cap_first}(MongoCodecUtil.getMap(document,"${field.name}",${field.valueClass}.class));
        <#elseif field.bean>
        this.set${field.name?cap_first}(MongoCodecUtil.getBean(document,"${field.name}",${field.valueClass}.class));
        </#if>
    </#list>
</#if>
    }

    @Override
    public void fromResultSet(ResultSet rest) throws SQLException {
<#if dbType?upper_case == "MYSQL">
        this.setId(rest.getLong(1));
        this.setEntityVersion(rest.getInt(2));
    <#assign index=3>
    <#list fields as field>
        <#if field.briefType=="boolean" || field.briefType=="byte" || field.briefType=="short" || field.briefType=="int" || field.briefType=="long" || field.briefType=="float" || field.briefType=="double" || field.briefType=="String">
        this.set${field.name?cap_first}(rest.get${field.briefType?cap_first}(${index}));
        <#elseif field.list>
        this.set${field.name?cap_first}(JSON.parseArray(rest.getString(${index}),${field.valueClass}.class));
        <#elseif field.map>
        this.set${field.name?cap_first}(JSON.parseObject(rest.getString(${index}),new TypeReference<>(){}));
        <#elseif field.bean>
        this.set${field.name?cap_first}(JSON.parseObject(rest.getString(${index}),${field.valueClass}.class));
        </#if>
        <#assign index=index+1>
    </#list>
</#if>
    }

    @Override
    public Document toDocument() {
<#if dbType?upper_case == "MONGO">
        Document document = new Document();
        document.put("id", this.getId());
        document.put("entityVersion", this.getEntityVersion());
    <#list fields as field>
        <#if field.briefType=="boolean" || field.briefType=="byte" || field.briefType=="short" || field.briefType=="int" || field.briefType=="long" || field.briefType=="float" || field.briefType=="double" || field.briefType=="String">
        document.put("${field.name}",this.get${field.name?cap_first}());
        <#elseif field.list>
        MongoCodecUtil.putList(document,"${field.name}", this.get${field.name?cap_first}(), ${field.valueClass}.class);
        <#elseif field.map>
        MongoCodecUtil.putMap(document,"${field.name}", this.get${field.name?cap_first}(), ${field.valueClass}.class);
        <#elseif field.bean>
        MongoCodecUtil.putBean(document,"${field.name}", this.get${field.name?cap_first}());
        </#if>
    </#list>
        return document;
<#else>
        return null;
</#if>
    }

    @Override
    public void toPreparedStatement(PreparedStatement stat) throws SQLException {
<#if dbType?upper_case == "MYSQL">
        stat.setLong(1, this.getId());
        stat.setInt(2, this.getEntityVersion());
    <#assign index=3>
    <#list fields as field>
        <#if field.briefType=="boolean" || field.briefType=="byte" || field.briefType=="short" || field.briefType=="int" || field.briefType=="long" || field.briefType=="float" || field.briefType=="double" || field.briefType=="String">
        stat.set${field.briefType?cap_first}(${index}, this.get${field.name?cap_first}());
        <#else>
        stat.setString(${index}, JSON.toJSONString(this.get${field.name?cap_first}()));
        </#if>
        <#assign index=index+1>
    </#list>
</#if>
    }

    @Override
    public ${class?split(".")?last} deepClone(){
<#assign object = class?split(".")?last?uncap_first>
        ${class?split(".")?last} ${object} = new ${class?split(".")?last}();
        ${object}.setId(getId());
        ${object}.setEntityVersion(getEntityVersion());
    <#list fields as field>
        <#if field.briefType=="boolean" || field.briefType=="byte" || field.briefType=="short" || field.briefType=="int" || field.briefType=="long" || field.briefType=="float" || field.briefType=="double" || field.briefType=="String">
        ${object}.set${field.name?cap_first}(this.get${field.name?cap_first}());
        <#else>
        ${object}.set${field.name?cap_first}(CloneUtil.deepClone(this.get${field.name?cap_first}()));
        </#if>
    </#list>
        return ${object};
    }
}