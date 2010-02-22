$! $This file is distributed under the terms of the license in /doc/license.txt$ !$

$doctype()$

$head()$

<body>
    <div id="wrap" class="container">
        <div id="header">
            $identity()$
            
            <div id="navAndSearch" class="block">
            	$menu()$
            	$search()$
            </div> <!-- navAndSearch -->
        </div> <!-- header --> 
        
        <hr class="hidden" />
        
        <div id="contentwrap">       
        	<div id="content" $if (contentClass)$ class="$contentClass$" $endif$>
            	$body$  
           	</div> <!-- content -->  
        </div> <!-- contentwrap -->
    
        $footer()$
    
    </div> <!-- wrap -->
</body>
</html>
