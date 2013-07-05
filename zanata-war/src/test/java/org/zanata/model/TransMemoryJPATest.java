/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.model;

import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.model.tm.TMTransUnitVariant;
import org.zanata.model.tm.TMTranslationUnit;
import org.zanata.model.tm.TransMemory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class TransMemoryJPATest extends ZanataDbunitJpaTest
{
   @Override
   protected void prepareDBUnitOperations()
   {
   }

   private TransMemory createDefaultTransMemoryInstance()
   {
      TransMemory tm = new TransMemory();
      tm.setName("New Trans Memory");
      tm.setSlug("new-trans-memory");
      return tm;
   }

   private TransMemory getTransMemory(String slug)
   {
      return (TransMemory)
            super.getSession().createCriteria(TransMemory.class).add(Restrictions.naturalId().set("slug", slug))
                  .uniqueResult();
   }

   @Test
   public void save() throws Exception
   {
      TransMemory tm = createDefaultTransMemoryInstance();
      super.getEm().persist(tm);

      TransMemory stored = getTransMemory("new-trans-memory");

      assertThat(stored.getName(), is(tm.getName()));
   }

   @Test
   public void saveWithTransUnits() throws Exception
   {
      TransMemory tm = createDefaultTransMemoryInstance();

      // add some units
      for( int i = 0; i<5; i++ )
      {
         TMTranslationUnit unit = new TMTranslationUnit();
         unit.setTranslationMemory(tm);
         unit.setSourceLanguage("en-US");
         unit.setTransUnitId("unit-id-" + i);
         tm.getTranslationUnits().add(unit);
      }

      super.getEm().persist(tm);

      // Fetch it, should have the same elements
      TransMemory stored = getTransMemory("new-trans-memory");

      assertThat(stored.getTranslationUnits().size(), is(5));
   }

   @Test
   public void saveTransUnitVariants() throws Exception
   {
      // Save them from the bottom up, as that is probably how it will need to be done due to the large amount of them
      saveWithTransUnits();

      // Fetch the translation memory
      TransMemory stored = getTransMemory("new-trans-memory");

      // For each trans unit, generate some variants
      for(TMTranslationUnit tu : stored.getTranslationUnits())
      {
         TMTransUnitVariant tuvES = new TMTransUnitVariant("es", "Mensaje de Prueba");
         TMTransUnitVariant tuvEN = new TMTransUnitVariant("en-US", "Test Message");
         TMTransUnitVariant tuvFR = new TMTransUnitVariant("fr", "Message de test");

         tu.getTransUnitVariants().put(tuvES.getLanguage(), tuvES);
         tu.getTransUnitVariants().put(tuvEN.getLanguage(), tuvEN);
         tu.getTransUnitVariants().put(tuvFR.getLanguage(), tuvFR);

         super.getEm().merge(tu);
      }

      // Verify they were saved
      List results = getEm().createQuery("from TMTransUnitVariant").getResultList();
      assertThat(results.size(), greaterThan(0));
   }
}
