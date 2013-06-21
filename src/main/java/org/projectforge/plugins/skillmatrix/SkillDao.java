/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.plugins.skillmatrix;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.projectforge.core.BaseDao;
import org.projectforge.core.UserException;
import org.projectforge.user.UserRightId;

/**
 * 
 * @author Billy Duong (b.duong@micromata.de)
 * 
 */
public class SkillDao extends BaseDao<SkillDO>
{
  public static final String UNIQUE_PLUGIN_ID = "PLUGIN_SKILL_MATRIX_SKILL";

  public static final String I18N_KEY_SKILL_PREFIX = "plugins.skillmatrix.skill";

  public static final UserRightId USER_RIGHT_ID = new UserRightId(UNIQUE_PLUGIN_ID, "plugin20", I18N_KEY_SKILL_PREFIX);

  public static final String I18N_KEY_ERROR_CYCLIC_REFERENCE = "plugins.skillmatrix.error.cyclicReference";

  public static final String I18N_KEY_ERROR_DUPLICATE_CHILD_SKILL = "plugins.skillmatrix.error.duplicateChildSkill";

  private static final String[] ADDITIONAL_SEARCH_FIELDS = new String[] { "parent.title"};

  private final SkillTree skillTree = new SkillTree(this);

  // private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SkillDao.class);

  public SkillDao()
  {
    super(SkillDO.class);
    userRightId = USER_RIGHT_ID;
  }

  @Override
  public SkillDO newInstance()
  {
    return new SkillDO();
  }

  /**
   * @see org.projectforge.core.BaseDao#onSaveOrModify(org.projectforge.core.ExtendedBaseDO)
   */
  @Override
  protected void onSaveOrModify(final SkillDO obj)
  {
    synchronized (this) {
      checkConstraintViolation(obj);
    }
  }

  @Override
  protected void afterSaveOrModify(final SkillDO obj)
  {
    skillTree.setExpired();
  }

  public SkillTree getSkillTree()
  {
    return skillTree;
  }

  @SuppressWarnings("unchecked")
  public void checkConstraintViolation(final SkillDO skill) throws UserException
  {
    // TODO: Check for valid Tree structure (root) -> example TaskDao.checkConstraintVilation
    List<SkillDO> list;
    final StringBuilder sb = new StringBuilder();
    sb.append("from SkillDO s where s.title=? and deleted=false and s.parent.id");
    final List<Object> params= new LinkedList<Object>();
    params.add(skill.getTitle());
    if (skill.getParentId() != null) {
      sb.append("=?");
      params.add(skill.getParentId());
    } else {
      sb.append(" is null ");
    }
    if (skill.getId() != null) {
      sb.append(" and s.id != ?");
      params.add(skill.getId());
    }
    list = getHibernateTemplate().find(sb.toString(), params.toArray());
    if (CollectionUtils.isNotEmpty(list) == true) {
      throw new UserException(I18N_KEY_ERROR_DUPLICATE_CHILD_SKILL);
    }
  }

  /**
   * @see org.projectforge.core.BaseDao#getAdditionalSearchFields()
   */
  @Override
  protected String[] getAdditionalSearchFields()
  {
    return ADDITIONAL_SEARCH_FIELDS;
  }
}
