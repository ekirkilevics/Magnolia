This CodePress distribution has been patched to support chrome+safari. 
See http://sourceforge.net/tracker/index.php?func=detail&aid=2209244&group_id=186981&atid=919469
It also disables Opera support (falls back to plain textarea) which is problematic at least in version 10.
On firefox 3.x TAB indentation is broken. Replaced original implementation of snippets function
with one found on http://sourceforge.net/projects/codepress/forums/forum/654507/topic/2108862
which fixes the problem. 