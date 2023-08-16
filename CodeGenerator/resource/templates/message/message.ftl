package ${class[0..(class?last_index_of(".") - 1)]};

import com.alibaba.fastjson.JSON;
<#list imports as import>
import ${import};
</#list>
import pers.winter.framework.message.AbstractBaseMessage;

<#if comment??>
/**
 * ${comment}
 * @author CodeGenerator
 */
 </#if>
public class ${class?split(".")?last} extends AbstractBaseMessage {
<#list fields as field>
    <#if field.comment!="">
    /**
     * ${field.comment}
     */
    </#if>
    public ${field.briefType} ${field.name};
</#list>

    @Override
    public byte[] serialized() {
        return JSON.toJSONString(this).getBytes();
    }
}