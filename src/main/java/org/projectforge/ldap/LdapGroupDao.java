/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.ldap;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.ModificationItem;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class LdapGroupDao extends LdapDao<LdapGroup>
{
  private static final String[] ADDITIONAL_OBJECT_CLASSES = { "posixGroup"};// null;//{ "groupOfNames"};

  /**
   * @see org.projectforge.ldap.LdapDao#getObjectClass()
   */
  @Override
  protected String getObjectClass()
  {
    return "groupOfUniqueNames";
  }

  /**
   * @see org.projectforge.ldap.LdapDao#getAdditionalObjectClasses()
   */
  @Override
  protected String[] getAdditionalObjectClasses()
  {
    return ADDITIONAL_OBJECT_CLASSES;
  }

  /**
   * @see org.projectforge.ldap.LdapDao#getIdAttrId()
   */
  @Override
  public String getIdAttrId()
  {
    return "gidNumber";
  }

  /**
   * Used for bind and update.
   * @param person
   * @return
   * @see org.projectforge.ldap.LdapDao#getModificationItems(org.projectforge.ldap.LdapObject)
   */
  @Override
  protected ModificationItem[] getModificationItems(final LdapGroup group)
  {
    final List<ModificationItem> list = new ArrayList<ModificationItem>();
    createAndAddModificationItems(list, "gidNumber", group.getGidNumber().toString());
    createAndAddModificationItems(list, "o", group.getOrganization());
    createAndAddModificationItems(list, "description", group.getDescription());
    if (group.getMembers() != null) {
      for (final String member : group.getMembers()) {
        createAndAddModificationItems(list, "uniqueMember", member);
      }
    }
    return list.toArray(new ModificationItem[list.size()]);
  }

  /**
   * @see org.projectforge.ldap.LdapDao#mapToObject(java.lang.String, javax.naming.directory.Attributes)
   */
  @Override
  protected LdapGroup mapToObject(final Attributes attributes) throws NamingException
  {
    final LdapGroup group = new LdapGroup();
    group.setDescription(LdapUtils.getAttributeStringValue(attributes, "description"));
    group.setOrganization(LdapUtils.getAttributeStringValue(attributes, "o"));
    return group;
  }
}
