/*
 * This class is not currently used. It's was created in effort to implement
 * the DomFileDescription object and register it as an extension point since
 * the method registerFileDescription is deprecated. However, with no luck
 * with this route the current method used to get the plugin working is
 * the deprecated on mentioned here.
 */

package com.janusresearch.tdXmlPlugin.xml.descriptions;

import com.intellij.util.xml.DomFileDescription;
import com.janusresearch.tdXmlPlugin.dom.config.Configuration;

public class XmlConfigDomFileDescription extends DomFileDescription<Configuration> {
    private static final String ROOT_TAG = "Configuration";

    public XmlConfigDomFileDescription() {
        super(Configuration.class, ROOT_TAG);

    }
}