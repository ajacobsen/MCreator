<#--
 # MCreator (https://mcreator.net/)
 # Copyright (C) 2012-2020, Pylo
 # Copyright (C) 2020-2022, Pylo, opensource contributors
 #
 # This program is free software: you can redistribute it and/or modify
 # it under the terms of the GNU General Public License as published by
 # the Free Software Foundation, either version 3 of the License, or
 # (at your option) any later version.
 #
 # This program is distributed in the hope that it will be useful,
 # but WITHOUT ANY WARRANTY; without even the implied warranty of
 # MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 # GNU General Public License for more details.
 #
 # You should have received a copy of the GNU General Public License
 # along with this program.  If not, see <https://www.gnu.org/licenses/>.
 #
 # Additional permission for code generator templates (*.ftl files)
 #
 # As a special exception, you may create a larger work that contains part or
 # all of the MCreator code generator templates (*.ftl files) and distribute
 # that work under terms of your choice, so long as that work isn't itself a
 # template for code generation. Alternatively, if you modify or redistribute
 # the template itself, you may (at your option) remove this special exception,
 # which will cause the template and the resulting code generator output files
 # to be licensed under the GNU General Public License without this special
 # exception.
-->

<#-- @formatter:off -->

<#include "../mcitems.ftl">

<#assign tabMap = w.getCreativeTabMap()>
<#assign itemsInVanillaTabs = tabMap.keySet()?filter(e -> !e?starts_with('CUSTOM:'))?size != 0>

/*
 *    MCreator note: This file will be REGENERATED on each build.
 */

package ${package}.init;

<#if itemsInVanillaTabs>
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
</#if>
public class ${JavaModName}Tabs {

	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ${JavaModName}.MODID);

	<#list w.getElementsOfType("tab") as tabME>
		<#if tabMap.containsKey("CUSTOM:" + tabME.getName())>
			<#assign tab = tabME.getGeneratableElement()>
			<#assign tabContents = tabMap.get("CUSTOM:" + tabME.getName())>
			public static final RegistryObject<CreativeModeTab> ${tabME.getRegistryNameUpper()} = REGISTRY.register("${tabME.getRegistryName()}", () ->
				CreativeModeTab.builder().title(Component.translatable("item_group.${modid}.${tabME.getRegistryName()}"))
					.icon(() -> ${mappedMCItemToItemStackCode(tab.icon, 1)})
					.displayItems((parameters, tabData) -> {
						<#list tabContents as tabElement>
						tabData.accept(${mappedMCItemToItem(tabElement)});
						</#list>
					})
					<#if tab.showSearch>.withSearchBar()</#if>
					.build()
			);
		</#if>
	</#list>

	<#if itemsInVanillaTabs>
	@SubscribeEvent public static void buildTabContentsVanilla(BuildCreativeModeTabContentsEvent tabData) {
		<#assign first = true>
		<#list tabMap.keySet() as tabName>
			<#if !tabName.startsWith("CUSTOM:")>
				<#if !first>else <#assign first = false></#if>
				if (tabData.getTabKey() == ${generator.map(tabName, "tabs")}) {
					<#list tabMap.get(tabName) as tabElement>
					tabData.accept(${mappedMCItemToItem(tabElement)});
					</#list>
				}
			</#if>
		</#list>
	}
	</#if>

}

<#-- @formatter:on -->