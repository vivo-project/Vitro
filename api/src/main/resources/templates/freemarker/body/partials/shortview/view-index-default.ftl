<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#-- Display of an individual in a list (on /individuallist and menu pages). -->

<#import "lib-properties.ftl" as p>

<a href="${individual.profileUrl}" title="${i18n().name}">${individual.name}</a>

<@p.mostSpecificTypes individual  />
