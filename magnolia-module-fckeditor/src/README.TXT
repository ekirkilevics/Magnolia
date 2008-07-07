Build notes

* How to upgrade FCKEditor
    * Extract the archive of FCKEditor under src/temp/fckeditor
    * run the "update" task of the src/build.xml ant build file (possibly you have to adjust the properties in there)
    * Of course, check diffs. A full-blown diff is generated
      by the ant build in temp/current.diff
