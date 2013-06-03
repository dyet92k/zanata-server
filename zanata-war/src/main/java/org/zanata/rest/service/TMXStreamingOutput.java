/*
 * Copyright 2013, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.zanata.rest.service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import lombok.Cleanup;
import net.sf.okapi.common.XMLWriter;
import net.sf.okapi.common.filterwriter.TMXWriter;

import org.zanata.common.LocaleId;
import org.zanata.model.DocumentWithId;
import org.zanata.model.SourceContents;
import org.zanata.util.OkapiUtil;
import org.zanata.util.VersionUtility;

import com.google.common.collect.Iterables;

/**
 * Exports a collection of NamedDocument (ie a project iteration) to an
 * OutputStream in TMX format.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class TMXStreamingOutput implements StreamingOutput
{
   private static final String creationTool = "Zanata " + TMXStreamingOutput.class.getSimpleName();
   private static final String creationToolVersion =
         VersionUtility.getVersionInfo(TMXStreamingOutput.class).getVersionNo();
   private final @Nonnull Iterable<DocumentWithId> documents;
   private final @Nullable LocaleId targetLocale;
   private final ExportTUStrategy exportTUStrategy;

   public TMXStreamingOutput(@Nonnull Iterable<DocumentWithId> documents,
         @Nullable LocaleId targetLocale)
   {
      this.documents = documents;
      this.targetLocale = targetLocale;
      this.exportTUStrategy = new ExportTUStrategy(targetLocale);
   }

   net.sf.okapi.common.LocaleId toOkapiLocaleOrEmpty(@Nullable LocaleId locale)
   {
      if (locale == null)
      {
         // TMXWriter demands a non-null target locale, but if you write
         // your TUs with writeTUFull(), it is never actually used.
         return net.sf.okapi.common.LocaleId.EMPTY;
      }
      return OkapiUtil.toOkapiLocale(locale);
   }

   @Override
   public void write(OutputStream output) throws IOException, WebApplicationException
   {
      @Cleanup
      Writer writer = new PrintWriter(output);
      @Cleanup
      XMLWriter xmlWriter = new XMLWriter(writer);
      @Cleanup
      ZanataTMXWriter tmxWriter = new ZanataTMXWriter(xmlWriter);
      String segType = "block"; // TODO other segmentation types
      String dataType = "unknown"; // TODO track data type metadata throughout the system

      net.sf.okapi.common.LocaleId allLocale = new net.sf.okapi.common.LocaleId("*all*", false);

      tmxWriter.writeStartDocument(
            allLocale,
            toOkapiLocaleOrEmpty(targetLocale),
            creationTool, creationToolVersion,
            segType, null, dataType);

      for (DocumentWithId doc : documents)
      {
         exportDocument(tmxWriter, doc);
      }
      tmxWriter.writeEndDocument();
   }

   private void exportDocument(ZanataTMXWriter tmxWriter, DocumentWithId doc)
   {
      String tuidPrefix = doc.getQualifiedDocId() + ":";
      // TODO option to export obsolete TFs to TMX?
      for (SourceContents tf : doc)
      {
         exportTUStrategy.exportTranslationUnit(tmxWriter, tuidPrefix, tf, toOkapiLocaleOrEmpty(doc.getSourceLocaleId()));
      }
   }

}
