/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.skillmatrix;

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.convert.IConverter;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteTextField;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

/**
 * @author Billy Duong (b.duong@micromata.de)
 * 
 */
public abstract class SkillSelectAutoCompleteFormComponent extends PFAutoCompleteTextField<SkillDO>
{

  private static final long serialVersionUID = -3142796647323340935L;

  public static final String I18N_KEY_ERROR_SKILL_NOT_FOUND = "plugins.skillmatrix.error.skillNotFound";

  @SpringBean(name = "skillDao")
  private SkillDao skillDao;

  private FieldsetPanel fieldsetPanel;

  private SkillDO skill;

  public SkillSelectAutoCompleteFormComponent(final String id)
  {
    this(id, null);
    setModel(new PropertyModel<SkillDO>(this, "skill"));
  }

  /**
   * @param id
   * @param model
   */
  public SkillSelectAutoCompleteFormComponent(final String id, final IModel<SkillDO> model)
  {
    super(id, model);
    getSettings().withLabelValue(true).withMatchContains(true).withMinChars(2).withAutoSubmit(false).withWidth(400);

    // Prevents a submit with an empty autocomplete textfield by pressing enter
    add(AttributeModifier.append("onkeypress", "if ( event.which == 13 ) { return false; }"));

    add(AttributeModifier.append("class", "mm_delayBlur"));

    add(new AjaxFormComponentUpdatingBehavior("onChange") {
      private static final long serialVersionUID = 5394951486514219126L;

      @Override
      protected void onUpdate(final AjaxRequestTarget target)
      {
        // AjaxRequestTarget needs this.
      }
    });
  }

  @Override
  protected List<SkillDO> getChoices(final String input)
  {
    final BaseSearchFilter filter = new BaseSearchFilter();
    filter.setSearchFields("title");
    filter.setSearchString(input);
    final List<SkillDO> list = skillDao.getList(filter);
    return list;
  }

  @Override
  protected String formatLabel(final SkillDO skill)
  {
    if (skill == null) {
      return "";
    }
    return createPath(skill.getId());
  }

  @Override
  protected String formatValue(final SkillDO skill)
  {
    if (skill == null) {
      return "";
    }
    return "" + skill.getId();
  }

  /**
   * create path to root
   * 
   * @return
   */
  private String createPath(final Integer skillId)
  {
    final StringBuilder builder = new StringBuilder();
    final List<SkillNode> nodeList = getSkillTree().getPathToRoot(skillId);
    final String pipeSeparator = " | ";
    String separator = "";
    for (final SkillNode node : nodeList) {
      builder.append(separator);
      builder.append(node.getSkill().getTitle());
      separator = pipeSeparator;
    }
    return builder.toString();
  }

  @Override
  protected void onBeforeRender()
  {
    super.onBeforeRender();
    // this panel should always start with an empty input field, therefore delete the current model
    skill = null;
  }

  protected void notifyChildren()
  {
    final AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
    if (target != null) {
      onModelSelected(target, skill);
    }
  }

  /**
   * Hook method which is called when the model is changed with a valid durin an ajax call
   * 
   * @param target
   * @param taskDo
   */
  protected abstract void onModelSelected(final AjaxRequestTarget target, SkillDO skill);

  @SuppressWarnings({ "unchecked", "rawtypes"})
  @Override
  public <C> IConverter<C> getConverter(final Class<C> type)
  {
    return new IConverter() {
      private static final long serialVersionUID = 6824608901238845695L;

      @Override
      public Object convertToObject(final String value, final Locale locale)
      {
        if (StringUtils.isEmpty(value) == true) {
          getModel().setObject(null);
          return null;
        }
        try {
          final SkillDO skill = getSkillTree().getSkillById(Integer.valueOf(value));
          if (skill == null) {
            error(getString(I18N_KEY_ERROR_SKILL_NOT_FOUND));
          }
          getModel().setObject(skill);

          notifyChildren();

          return skill;
        } catch (final NumberFormatException e) {
          // just ignore the NumberFormatException, because this could happen during wrong inputs
          return null;
        }
      }

      @Override
      public String convertToString(final Object value, final Locale locale)
      {
        if (value == null) {
          return "";
        }
        final SkillDO skill = (SkillDO) value;
        return skill.getTitle();
      }
    };
  }

  /**
   * Optional. The parameter is used for ajax updates.
   * @param fieldsetPanel
   * @return
   */
  public SkillSelectAutoCompleteFormComponent setFieldsetPanel(FieldsetPanel fieldsetPanel)
  {
    this.fieldsetPanel = fieldsetPanel;
    return this;
  }

  public SkillTree getSkillTree()
  {
    return skillDao.getSkillTree();
  }

}