<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#if gatracker??>
    <script type="text/javascript">
        var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www."); document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
    </script>

    <script type="text/javascript">
        try {
            var pageTracker = _gat._getTracker("${gatracker}");
            <#if gadomain??>
                pageTracker._setDomainName("${gadomain});
            </#if>
            pageTracker._setAllowLinker(true);
            pageTracker._trackPageview();

            <#if gatrackerrollup??>
                var rollupTracker = _gat._getTracker("${gatrackerrollup}");
                rollupTracker._setDomainName("none");
                rollupTracker._setAllowLinker(true);
                rollupTracker._trackPageview(location.host+location.pathname);
            </#if>
    }
    catch(err) {}
    </script>
</#if>
