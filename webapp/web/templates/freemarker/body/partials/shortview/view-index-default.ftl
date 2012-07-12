<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Display of an individual in a list (on /individuallist and menu pages). -->

<#import "lib-properties.ftl" as p>

<a href="${individual.profileUrl}" title="name">${individual.name}</a>

<@p.mostSpecificTypes individual  />