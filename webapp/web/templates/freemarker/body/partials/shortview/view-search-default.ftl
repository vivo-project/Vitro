<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Default individual search view -->

<#import "lib-properties.ftl" as p>

<a href="${individual.profileUrl}" title="name">${individual.name}</a>

<@p.mostSpecificTypes individual />

<p class="snippet">${individual.snippet}</p>