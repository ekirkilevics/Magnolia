Build notes

* How to upgrade FCKEditor
    * Extract the archive of FCKEditor under src/main/resources/mgnl-resources/fckeditor
    * Remove the following:
      * **/_*.*
      * htaccess.txt
      * fckeditor.{afp|asp|cfc|cfm|lasso|php|pl|py}
      * src/main/resources/mgnl-resources/fckeditor/editor/filemanager/browser/default/connectors/* (we use custom stuff)
      * src/main/resources/mgnl-resources/fckeditor/editor/filemanager/upload/* (we use custom stuff)
    * Everything that's under src/main/resources/mgnl-resources/fckeditor/custom should remain, since, as the name
      states, it is custom
    * If they have not yet been applied to the main fck sources, apply the patches found in src/patches.
    * Of course, check diffs and adapt any customized file that might have changed.

* Patches
    The src/patches directory contain patches that we applied to the original fck sources

    * Accessible popup links:
      - origin     : http://sourceforge.net/tracker/index.php?func=detail&aid=1577202&group_id=75348&atid=543655
      - target     : src/main/resources/mgnl-resources/fckeditor/editor/dialog/fck_link/fck_link.js
      - needed for : http://jira.magnolia.info/browse/MAGNOLIA-1135
      - patch file : accessible_popup_links.patch
