<#include "../mcitems.ftl">
{
    "replace": false,
    "values": [
        <#assign elements = []>
        <#list w.getElementsOfType("dimension") as me>
            <#assign ge = me.getGeneratableElement() />

            <#assign elements += ["${mappedMCItemToRegistryName(ge.mainFillerBlock)}"]>
            <#list w.filterBrokenReferences(ge.biomesInDimension) as biome>
                <#if biome.getUnmappedValue().startsWith("CUSTOM:")>
                    <#assign gebiome = w.getWorkspace().getModElementByName(biome.getUnmappedValue().replace("CUSTOM:", "")).getGeneratableElement()/>
                    <#assign elements += ["${mappedMCItemToRegistryName(gebiome.groundBlock)}"]>
                    <#assign elements += ["${mappedMCItemToRegistryName(gebiome.undergroundBlock)}"]>
                </#if>
            </#list>
        </#list>

        <#assign elementsUnique = [] />
        <#list elements as element>
            <#if ! elementsUnique?seq_contains(element)>
                <#assign elementsUnique = elementsUnique + [element] />
            </#if>
        </#list>

        <#list elementsUnique as e>
            "${e}"<#if e?has_next>,</#if>
        </#list>
    ]
}