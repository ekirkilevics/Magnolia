package info.magnolia.cms.gui.misc;

/**
 *
 * User: enz
 * Date: Oct 12, 2004
 * Time: 5:00:20 PM
 *
 */
public class Icon {
	public final static String BASEPATH="/admindocroot/icons";

	public final static int SIZE_SMALL=16;
	public final static int SIZE_MEDIUM=24;
	public final static int SIZE_LARGE=32;
	public final static int SIZE_XLARGE=48;

	public final static String PAGE="folder_cubes";
	public final static String CONTENTNODE="cubes";
	public final static String NODEDATA="cube_green";
	public final static String WEBPAGE="document_plain_earth";
	public final static String ROLE="hat_white";
	public final static String USER="pawn_glass_yellow";



	public Icon() {

	}

	public String getSrc(String iconName,int size) {
		return Icon.BASEPATH+"/"+size+"/"+iconName+".gif";
	}
}
