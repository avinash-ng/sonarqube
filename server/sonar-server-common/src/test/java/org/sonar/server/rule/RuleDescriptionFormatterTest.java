/*
 * SonarQube
 * Copyright (C) 2009-2022 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.rule;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.sonar.db.rule.RuleDefinitionDto;
import org.sonar.db.rule.RuleDescriptionSectionDto;
import org.sonar.db.rule.RuleDto;
import org.sonar.db.rule.RuleForIndexingDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.db.rule.RuleDescriptionSectionDto.createDefaultRuleDescriptionSection;

public class RuleDescriptionFormatterTest {

  private static final RuleDescriptionSectionDto HTML_SECTION = createDefaultRuleDescriptionSection("uuid", "<span class=\"example\">*md* ``description``</span>");
  private static final RuleDescriptionSectionDto MARKDOWN_SECTION = createDefaultRuleDescriptionSection("uuid", "*md* ``description``");

  @Test
  public void getMarkdownDescriptionAsHtml() {
    RuleDefinitionDto rule = new RuleDefinitionDto().setDescriptionFormat(RuleDto.Format.MARKDOWN).addRuleDescriptionSectionDto(MARKDOWN_SECTION);
    String html = RuleDescriptionFormatter.getDescriptionAsHtml(rule);
    assertThat(html).isEqualTo("<strong>md</strong> <code>description</code>");
  }

  @Test
  public void getHtmlDescriptionAsIs() {
    RuleDefinitionDto rule = new RuleDefinitionDto().setDescriptionFormat(RuleDto.Format.HTML).addRuleDescriptionSectionDto(HTML_SECTION);
    String html = RuleDescriptionFormatter.getDescriptionAsHtml(rule);
    assertThat(html).isEqualTo(HTML_SECTION.getContent());
  }

  @Test
  public void handleEmptyDescription() {
    RuleDefinitionDto rule = new RuleDefinitionDto().setDescriptionFormat(RuleDto.Format.HTML);
    String result = RuleDescriptionFormatter.getDescriptionAsHtml(rule);
    assertThat(result).isNull();
  }

  @Test
  public void handleNullDescriptionFormat() {
    RuleDescriptionSectionDto sectionWithNullFormat = createDefaultRuleDescriptionSection("uuid", "whatever");
    RuleDefinitionDto rule = new RuleDefinitionDto().addRuleDescriptionSectionDto(sectionWithNullFormat);
    String result = RuleDescriptionFormatter.getDescriptionAsHtml(rule);
    assertThat(result).isNull();
  }

  @Test
  public void getHtmlDescriptionForRuleForIndexingDtoAsIs() {
    Set<RuleDescriptionSectionDto> sectionsDtos = Sets.newHashSet(
      createDefaultRuleDescriptionSection("uuid", HTML_SECTION.getContent()));
    RuleForIndexingDto rule = createRuleForIndexingDto(sectionsDtos, RuleDto.Format.HTML);
    String html = RuleDescriptionFormatter.getDescriptionAsHtml(rule);
    assertThat(html).isEqualTo(HTML_SECTION.getContent());
  }

  @Test
  public void handleEmptyDescriptionForRuleForIndexingDto() {
    RuleForIndexingDto rule = createRuleForIndexingDto(Collections.emptySet(), RuleDto.Format.HTML);
    String result = RuleDescriptionFormatter.getDescriptionAsHtml(rule);
    assertThat(result).isNull();
  }

  @Test
  public void handleNullDescriptionFormatForRuleForIndexingDto() {
    Set<RuleDescriptionSectionDto> sectionsDtos = Sets.newHashSet(
      createDefaultRuleDescriptionSection("uuid", HTML_SECTION.getContent()));
    RuleForIndexingDto rule = createRuleForIndexingDto(sectionsDtos, null);
    String result = RuleDescriptionFormatter.getDescriptionAsHtml(rule);
    assertThat(result).isNull();
  }

  @NotNull
  private static RuleForIndexingDto createRuleForIndexingDto(Set<RuleDescriptionSectionDto> sectionsDtos, RuleDto.Format format) {
    RuleForIndexingDto rule = new RuleForIndexingDto();
    rule.setRuleDescriptionSectionsDtos(sectionsDtos);
    rule.setDescriptionFormat(format);
    rule.setRepository("repository");
    rule.setPluginRuleKey("pluginKey");
    return rule;
  }
}
