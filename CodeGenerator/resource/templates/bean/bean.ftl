package ${class[0..(class?last_index_of(".") - 1)]};

<#if inMongo?? && inMongo=="true">
import org.bson.Document;
import pers.winter.framework.db.mongo.ISerializableMongoObject;
</#if>
import pers.winter.framework.entity.ICloneable;
<#list imports as import>
import ${import};
</#list>

<#if comment??>
/**
 * ${comment}
 * @author CodeGenerator
 */
 </#if>
public class ${class?split(".")?last} implements ICloneable<#if inMongo?? && inMongo=="true">, ISerializableMongoObject</#if> {
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
<#if inMongo?? && inMongo=="true">
    @Override
    public void fromDocument(Document document) {
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
    }
    @Override
    public Document toDocument() {
        Document document = new Document();
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
    }
</#if>
    @Override
    public ${class?split(".")?last} deepClone(){
<#assign object = class?split(".")?last?uncap_first>
        ${class?split(".")?last} ${object} = new ${class?split(".")?last}();
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