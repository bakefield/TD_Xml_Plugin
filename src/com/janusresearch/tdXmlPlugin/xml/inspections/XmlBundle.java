package com.janusresearch.tdXmlPlugin.xml.inspections;

import com.intellij.CommonBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 */
public class XmlBundle {
  @NonNls
  private static final String BUNDLE = "com.janusresearch.tdXmlPlugin.xml.inspections.XmlBundle";

  @Nullable
  private static Reference<ResourceBundle> _bundle;

  @NotNull
  public static String message( @NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object... params ) {
    return CommonBundle.message(Objects.requireNonNull(getBundle()), key, params );
  }

  @Nullable
  private static ResourceBundle getBundle() {
    ResourceBundle bundle = null;

    if( _bundle != null ) {
      bundle = _bundle.get();
    }

    if( bundle == null ) {
      bundle = ResourceBundle.getBundle( BUNDLE );
      _bundle = new SoftReference<>(bundle);
    }
    return bundle;
  }
}
