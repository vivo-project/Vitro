<div>
    <h2>Site Styles</h2>


   <h3>Custom CSS Style</h3>

   <#if customCssPath?? && customCssPath != "null">
       <button
       style="font-weight: normal !important; color: white !important; background-color: #b40000 !important; font-size: 16px !important; display: inline !important; visibility: visible !important; opacity: 1 !important; z-index: 10 !important; width: 201px !important; height: 31px !important;"
        id="resetAction" type="button" class="button red" onclick="resetStyles()">Remove Custom CSS</button>
       <a href="${customCssPath}" download="custom.css" class="button blue">Download Custom CSS</a>
   </#if>
   <form action="${actionUpload}" method="post" enctype="multipart/form-data">
       <label for="fileUpload" class="">Upload new css file</label>
       <input type="file" id="fileUpload" name="fileUpload" accept=".css" class="form-item">
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
