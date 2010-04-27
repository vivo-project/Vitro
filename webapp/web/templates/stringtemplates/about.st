$! $This file is distributed under the terms of the license in /doc/license.txt$ !$

<h2>$title$</h2>

<h3>$testBean.name$</h3>
$! Doesn't work - can't call arbitrary methods on an in-context object
<h3>$testBean.sayHello()$</h3> !$

$if (aboutText)$
    <div class="pageGroupBody" id="aboutText">$aboutText$</div>
$endif$
    
$if (acknowledgeText)$
   <div class="pageGroupBody" id="acknowledgementText">$acknowledgeText$</div> 
$endif$
