<div>
    <h2>Site Styles</h2>
    <div class="button-group">
        <a class="button blue" download href="${urls.base}/css/vitro.css">vitro.css</a>
        <a class="button blue" download href="${urls.base}/css/login.css">login.css</a>
        <a class="button blue" download href="${urls.base}/css/edit.css">edit.css</a>
        <a class="button blue" download href="${urls.base}/css/individual/individual-property-groups.css">individual-property-groups.css</a>
        <a class="button blue" download href="${urls.base}/css/developer/developerPanel.css">developerPanel.css</a>
        
        <!-- Samo Wilma i TF-->
        <a class="button blue" download href="${urls.base}/css/home-page-maps.css">page-createAndLink.css</a> 


        <#if themeDir == "themes/wilma">
            <a class="button blue" download href="${urls.base}/themes/wilma/css/reset.css">reset.css</a>
            <a class="button blue" download href="${urls.base}/themes/wilma/css/wilma.css">wilma.css</a>
            <a class="button blue" download href="${urls.base}/themes/wilma/css/page-createAndLink.css">page-createAndLink.css</a>
            <a class="button blue" download href="${urls.base}/themes/wilma/css/screen.css">screen.css</a>
        </#if>

        <#if themeDir == "themes/willow">
            <a class="button blue" download href="${urls.base}/themes/willow/css/reset.css">reset.css</a>
            <a class="button blue" download href="${urls.base}/themes/willow/css/wilma.css">wilma.css</a>
            <a class="button blue" download href="${urls.base}/themes/willow/css/page-createAndLink.css">page-createAndLink.css</a>
            <a class="button blue" download href="${urls.base}/themes/willow/css/screen.css">screen.css</a>
        </#if>

        <#if themeDir == "themes/vitro">
            <a class="button blue" download href="${urls.base}/themes/vitro/css/screen.css">screen.css</a>
            <a class="button blue" download href="${urls.base}/themes/vitro/css/reset.css">reset.css</a>
            <a class="button blue" download href="${urls.base}/themes/vitro/css/vitroTheme.css">vitroTheme.css</a>
        </#if>
        
        <#if themeDir == "themes/tenderfoot">
            <a class="button blue" download href="${urls.base}/themes/tenderfoot/css/screen.css">screen.css</a>
            <a class="button blue" download href="${urls.base}/themes/tenderfoot/css/reset.css">reset.css</a>
            <a class="button blue" download href="${urls.base}/themes/tenderfoot/css/tenderfoot.css">tenderfoot.css</a>
            <a class="button blue" download href="${urls.base}/themes/tenderfoot/css/edit.css">edit.css</a>
            <a class="button blue" download href="${urls.base}/themes/tenderfoot/css/navbar-breakpoint.css">navbar-breakpoint.css</a>
            <a class="button blue" download href="${urls.base}/themes/tenderfoot/css/page-admin.css">page-admin.css</a>
            <a class="button blue" download href="${urls.base}/themes/tenderfoot/css/page-browse.css">page-browse.css</a>
            <a class="button blue" download href="${urls.base}/themes/tenderfoot/css/page-home.css">page-home.css</a>
            <a class="button blue" download href="${urls.base}/themes/tenderfoot/css/page-imageupload.css">page-imageupload.css</a>
            <a class="button blue" download href="${urls.base}/themes/tenderfoot/css/page-index.css">page-index.css</a>
            <a class="button blue" download href="${urls.base}/themes/tenderfoot/css/page-individual.css">page-individual.css</a>
            <a class="button blue" download href="${urls.base}/themes/tenderfoot/css/page-login.css">page-login.css</a>
            <a class="button blue" download href="${urls.base}/themes/tenderfoot/css/page-menu.css">page-menu.css</a>
            <a class="button blue" download href="${urls.base}/themes/tenderfoot/css/page-createAndLink.css">page-createAndLink.css</a>
            <a class="button blue" download href="${urls.base}/webjars/fonts/noto-sans/index.css">index.css</a>
        </#if>

        <#if themeDir == "themes/nemo">
            <a class="button blue" download href="${urls.base}/themes/nemo/css/theme.css">theme.css</a>
            <a class="button blue" download href="${urls.base}/themes/nemo/css/homepage.css">homepage.css</a>
            <a class="button blue" download href="${urls.base}/themes/nemo/css/individual.css">individual.css</a>
        </#if>

   </div>
   <hr>

   <br>

   <h2>Custom CSS Style</h2>

   <#if customCssPath?? && customCssPath != "null">
       <button
       style="font-weight: normal !important; color: white !important; background-color: #b40000 !important; font-size: 16px !important; display: inline !important; visibility: visible !important; opacity: 1 !important; z-index: 10 !important; width: 201px !important; height: 31px !important;"
        id="resetAction" type="button" class="button red" onclick="resetStyles()">Remove Custom CSS</button>
       <a href="${customCssPath}" download="custom.css" class="button blue">Download Custom CSS</a>
   </#if>
   <form action="${actionUpload}" method="post" enctype="multipart/form-data">
       <label for="fileUpload" class="">Upload new custom style.css:</label>
       <input type="file" id="fileUpload" name="fileUpload" accept=".css" class="form-item">
       <p class="note">Note: Avoid using relative links inside the CSS file to ensure proper loading of resources.</p>
       <div class="button-group">
           <a href="${backLocation}" class="button">Back</a>
           <button id="primaryAction" type="submit" class="button green">Upload</button>
       </div>
   </form>

    <script>
        function resetStyles() {
            const form = document.createElement('form');
            form.method = 'POST';
            form.action = '${actionRemove}';
            document.body.appendChild(form);
            form.submit();
        }


        function ensureElementAndParentsVisible(element) {
            console.log("fix reset btn position")

            if (!element) {
                console.error("The element is not provided or doesn't exist.");
                return;
            }

            let currentElement = element;
            let shoudlContinue = true;
            while (currentElement && shoudlContinue) {
                currentElement.style.setProperty("display", "inline", "important");
                currentElement.style.setProperty("opacity", "1", "important");
                currentElement.style.setProperty("visibility", "visible", "important");
                shoudlContinue = false

                const rect = currentElement.getBoundingClientRect();
                if (rect.width < 150 || rect.height < 25) {
                    currentElement.style.width = '200px';
                    currentElement.style.height = '30px';
                    shoudlContinue = true;
                }
                if (rect.top < 0 || rect.left < 0 || rect.bottom > window.innerHeight || rect.right > window.innerWidth) {
                    currentElement.style.position = 'absolute';
                    currentElement.style.top = '0';
                    currentElement.style.left = '0';
                    shoudlContinue = true;
                }
                currentElement = currentElement.parentElement;
            }
        }

        document.addEventListener('DOMContentLoaded', function() {
            setTimeout(() => {
                const element = document.getElementById('resetAction');
                ensureElementAndParentsVisible(element);
            }, 300);
        });
    </script>
</div>