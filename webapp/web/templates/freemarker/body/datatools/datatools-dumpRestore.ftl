<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<h2>Dump/Restore knowledge base</h2>

<h3>Before you start:</h3>
<ul>
    <li>
        <b>Content models</b> hold the data that VIVO contains and operates on. 
        This includes the ABox and the TBox, instances and ontologies, assertions and inferences.
    </li>
    <li>
        <b>Configuration models</b> hold the data that controls VIVO, 
        including display options, privacy restrictions, and user accounts.
    </li>
    <li>
        Dumping the content models make take several minutes, and may produce large files. 
        For example, dumping a fully populated VIVO instance may take 20 minutes and produce a file of 3 gigabytes.
    </li>
    <li>
        The restore process is additive: it will not delete existing triples. 
        However, duplicate triples will not be stored.
    </li>
    <li>
        After restoring, the search index should be re-built.
        The data will probably not require re-inferencing, 
        since the dump includes both assertions and inferences.
    </li>
</ul>

<hr>

<h3>Dump</h3>

<form action="${selectUrl}" method="get">
    <table>
        <tr>
            <td>Select models</td>
            <td>Select format</td>
            <td>&nbsp;</td>
        </tr>
        <tr>
            <td>
                <select name="which">
                    <option value="CONFIGURATION">Configuration models</option>
                    <option value="CONTENT">Content models</option>
                </select>
            </td>
            <td>
                <select name="format">
                    <option value="NQUADS">N-Quads</option>
                    <option value="JSON">RS-JSON</option>
                    <option value="XML">RS-XML</option>
                </select>
            </td>
            <td>
                <input type="submit" value="Dump" />
            </td>
        </tr>
    </table>
</form>

<hr>

<h3>Restore</h3>

<#if tripleCount?? >
    <section class="restore-feedback">
        <p>Loaded ${tripleCount} triples</p>
    </section>
</#if>

<form action="${restoreUrl}" enctype="multipart/form-data" method="post">
    <table>
        <tr>
            <td>Select models</td>
            <td>Select a file to restore from</td>
            <td>Select format</td>
            <td>&nbsp;</td>
        </tr>
        <tr>
            <td>
                <select name="which">
                    <option value="CONFIGURATION">Configuration models</option>
                    <option value="CONTENT">Content models</option>
                </select>
            </td>
            <td>
                <input type="file" name="sourceFile" size="60"/>
            </td>
            <td>
                <select name="format">
                    <option value="NQUADS">N-Quads</option>
<!--                    <option value="JSON">RS-JSON</option>    TODO -->
<!--                    <option value="XML">RS-XML</option>      TODO -->
                </select>
            </td>
            <td>
                <input type="submit" value="Restore" />
            </td>
        </tr>
        <tr>
            <td colspan="4">
                <label>
                    <input type="checkbox" value="purge" name="purge" />
                    Purge the models before restoring.
                </label>
            </td>
        </tr>
    </table>
</form>
