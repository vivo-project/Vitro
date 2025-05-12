<div>
    <h2>Site Styles</h2>
    <div class="button-group" id="cssLinks">
        <span class="loading">Loading...</span>
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

        function addCssLinks(list) {
            list.forEach((link) => {
                const lastSegment = link.split("/").pop();
                const anchor = document.createElement("a");
                anchor.className = "button blue";
                anchor.download = "";
                anchor.href = link;
                anchor.textContent = lastSegment;
                document.getElementById("cssLinks").appendChild(anchor);
            });
        }

        console.log("load data");
        document.addEventListener("DOMContentLoaded", function () {
            const sharedCss = [
                "${urls.base}/css/vitro.css",
                "${urls.base}/css/login.css",
                "${urls.base}/css/edit.css",
                "${urls.base}/css/individual/individual",
                "${urls.base}/css/developer/developerPanel",
            ];
            addCssLinks(sharedCss);

            fetch("${urls.base}/${themeDir}/theme-config.json")
                .then((response) => {
                    if (!response.ok) {
                        console.error("Error fetching theme-config.json");
                        alert(
                            "Failed to load theme configuration. Please check if the file exists and is accessible."
                        );
                        
                        return;
                    }
                    return response.json();
                })
                .then((data) => {
                    document.querySelector("#cssLinks > .loading").style.display = "none";
                    if (!data) return;
                    if (
                        !data ||
                        !data.themeStyle ||
                        !Array.isArray(data.themeStyle.css)
                    ) {
                        console.error("Invalid theme-config.json format");
                        alert(
                            "The theme configuration file is not in the expected format. Please verify its structure."
                        );

                        return;
                    }

                    console.log("Theme Config JSON:", data);
                    addCssLinks(data.themeStyle.css);
                })
                .catch((error) => {
                    console.error("Error fetching theme-config.json:", error);
                    alert(
                        "An unexpected error occurred while loading the theme configuration. Please verify its structure."
                    );
                    document.querySelector("#cssLinks > .loading").style.display = "none";
                });
            });



    </script>
</div>
