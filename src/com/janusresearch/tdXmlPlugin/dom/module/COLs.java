// Generated on Fri Feb 09 21:22:29 EST 2018
// DTD/Schema  :    null

package com.janusresearch.tdXmlPlugin.dom.module;

import com.intellij.util.xml.SubTag;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;
import com.intellij.util.xml.DomElement;

/**
 * null:COLsType interface.
 */
public interface COLs extends DomElement {

	/**
	 * Returns the list of COL children.
	 * @return the list of COL children.
	 */
	@NotNull
	@SubTagList("COL")
	java.util.List<COL> getCOLs();
	/**
	 * Adds new child to the list of COL children.
	 * @return created child
	 */
	@SubTag("COL")
	COL addCOL();


}
