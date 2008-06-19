Build notes

* How to upgrade FCKEditor
    * Extract the archive of FCKEditor under src/temp/fckeditor
    * run the "update" task of the src/build.xml ant build file (possibly you have to adjust the properties in there)
    * If they have not yet been applied to the main fck sources, apply the patches found in src/patches.
    * Of course, check diffs and adapt any customized file that might have changed. A full-blown diff is generated 
      by the ant build in temp/current.diff
      
* Ensure that the link browser gets the focus (if not closed)
   - use SetFocusOnLinkBrowser.patch