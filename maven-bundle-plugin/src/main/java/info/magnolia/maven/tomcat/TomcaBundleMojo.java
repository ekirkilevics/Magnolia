/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.maven.tomcat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;


/**
 * Run <code>mvn info.magnolia:maven-tomcatbundle-plugin:bundle</code>
 * @goal tomcat
 * @requiresProject false
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class TomcaBundleMojo extends AbstractMojo {

    /**
     * @parameter expression="http://www.apache.org/dist/jakarta/tomcat-5/v5.0.28/bin/jakarta-tomcat-5.0.28.tar.gz"
     * @required
     */
    private URL tomcatDownloadUrl;

    /**
     * @parameter expression="${basedir}/src/release/tomcat"
     * @required
     */
    private File tomcatdir;

    /**
     * @parameter expression="target/release/bundle"
     * @required
     */
    private File releaseDest;

    /**
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        String tomcatDistributionName = StringUtils.substringAfterLast(tomcatDownloadUrl.toString(), "/");

        File tomcatFile = new File(tomcatdir, tomcatDistributionName);

        if (tomcatFile.exists()) {
            getLog().info("Tomcat distribution already available, not downloading");
        }
        else {
            getLog().info("Downloading Tomcat distribution from " + tomcatDownloadUrl);
            tomcatFile.getParentFile().mkdirs();

            try {
                FileUtils.copyURLToFile(tomcatDownloadUrl, tomcatFile);
            }
            catch (IOException e) {
                throw new MojoExecutionException(
                    "Unable to download tomcat distribution. You can manually download tomcat from "
                        + tomcatDownloadUrl
                        + " and save it to "
                        + tomcatdir.getAbsolutePath(),
                    e);
            }
        }

        releaseDest.mkdirs();

        getLog().info("untarring to " + releaseDest.getAbsolutePath());
        try {
            untar(tomcatFile, releaseDest);
            // rename to 'tomcat'
            File extracted = new File(releaseDest, StringUtils.removeEnd(tomcatFile.getName(), ".tar.gz"));
            File renamed = new File(releaseDest, "tomcat");
            if(renamed.exists()){
                FileUtils.deleteDirectory(renamed);
            }
            extracted.renameTo(renamed);
            // replace some scripts
            FileUtils.copyDirectory(new File(tomcatdir, "bin"), new File(renamed, "bin"));
            // delete not delivered tomcat documentation
            FileUtils.deleteDirectory(new File(renamed, "webapps/tomcat-docs"));
            FileUtils.deleteDirectory(new File(renamed, "webapps/jsp-examples"));
            FileUtils.deleteDirectory(new File(renamed, "webapps/servlets-examples"));
        }
        catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private void untar(File tarFile, File untarDir) throws IOException {

        InputStream in;
        if ("gz".equalsIgnoreCase(StringUtils.substringAfterLast(tarFile.getName(), "."))) {
            in = new GZIPInputStream(new FileInputStream(tarFile));
        }
        else {
            in = new FileInputStream(tarFile);
        }

        TarInputStream tin = new TarInputStream(in);
        TarEntry tarEntry = tin.getNextEntry();

        while (tarEntry != null) {
            File destPath = new File(untarDir, tarEntry.getName());
            getLog().debug("Processing " + destPath.getAbsoluteFile());
            if (!tarEntry.isDirectory()) {
                destPath.getParentFile().mkdirs();
                FileOutputStream fout = new FileOutputStream(destPath);
                tin.copyEntryContents(fout);
                fout.close();
            }
            else {
                destPath.mkdir();
            }
            tarEntry = tin.getNextEntry();
        }
        tin.close();

    }

}
