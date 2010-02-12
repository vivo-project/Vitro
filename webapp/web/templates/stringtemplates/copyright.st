$if (copyrightText)$
    <div class="copyright">
        &copy;$copyrightYear$&nbsp;
        
        $if (copyrightUrl)$
            <a href="$copyrightUrl$">$copyrightText$</a>
        $else$
            $copyrightText$
        $endif$       
    </div>
$endif$